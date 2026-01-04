import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {

    private final RateLimiterService rateLimiterService;
    private final RuleService ruleService;

    public RateLimiter(RateLimiterService rateLimiterService,
                       RuleService ruleService) {
        this.rateLimiterService = rateLimiterService;
        this.ruleService = ruleService;
    }

    public boolean isAllowed(String api, String ip) {
        Rule rule = ruleService.getRule(api);
        String key = api + ":" + ip; // explicit rate-limit key
        return rateLimiterService.isAllowed(rule, key);
    }
}

class RateLimiterService {

    private final AlgorithmFactory algorithmFactory;

    public RateLimiterService(AlgorithmFactory algorithmFactory) {
        this.algorithmFactory = algorithmFactory;
    }

    public boolean isAllowed(Rule rule, String key) {
        RateLimitingAlgorithm algorithm =
                algorithmFactory.getAlgorithm(rule.getAlgorithm());
        return algorithm.isAllowed(rule, key);
    }
}

interface RateLimitingAlgorithm {
    boolean isAllowed(Rule rule, String key);
}

//marker interface
interface RateLimitStore { }

interface TokenBucketStore extends RateLimitStore{
    public TokenBucketState get(String key);
    public TokenBucketState put(String key, TokenBucketState tokenBucketState);
}

interface SlidingWindowStore extends RateLimitStore{
    Deque<Long> get(String key);
}


class InMemoryStore implements TokenBucketStore{

    private final Map<String, TokenBucketState> store = new ConcurrentHashMap<>();

    public TokenBucketState get(String key) {
        return store.get(key);
    }

    public TokenBucketState put(String key, TokenBucketState tokenBucketState) {
        store.put(key, tokenBucketState);
        return tokenBucketState;
    }
}

class InMemoryStoreSlidingWindow implements SlidingWindowStore{
    private final Map<String, Deque<Long>> store = new ConcurrentHashMap<>();
    public Deque<Long> get(String key) {
        return store.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
    }
}

class TokenBucketState{
    int tokens;
    long lastRefilled;
    TokenBucketState(int tokens, long lastRefilled) {
        this.tokens = tokens;
        this.lastRefilled = lastRefilled;
    }
}

class TokenBucket implements RateLimitingAlgorithm {

    private final TokenBucketStore store;

    public TokenBucket(TokenBucketStore store) {
        this.store = store;
    }

    @Override
    public synchronized boolean isAllowed(Rule rule, String key) {
        long now = System.currentTimeMillis();

        int limit = rule.getLimit();
        int timeLimit = rule.getTimeWindowSec();

        TokenBucketState currentState = store.get(key);

        if(currentState == null) {
            currentState = store.put(key, new TokenBucketState(limit, now));
        }

        long elapsedTime = now -  currentState.lastRefilled;

        // For cases where elapsedTime exceeds the window we need to refill tokens
        int tokensToRefill = (int)(elapsedTime/(timeLimit*1000))*limit;

        if(tokensToRefill > 0) {
            currentState.tokens = Math.min(limit, currentState.tokens + tokensToRefill);
            currentState.lastRefilled = now;
        }

        if(currentState.tokens > 0){
            currentState.tokens--;
            store.put(key, currentState);
            return true;
        }

        return false;
    }
}

class SlidingWindow implements RateLimitingAlgorithm {

    private final SlidingWindowStore store;

    public SlidingWindow(SlidingWindowStore store) {
        this.store = store;
    }

    @Override
    public synchronized boolean isAllowed(Rule rule, String key) {
        long now = System.currentTimeMillis();
        long windowStart = now - rule.getTimeWindowSec()*1000;

        int limit = rule.getLimit();

        Deque<Long> timestamps = store.get(key);

        while(!timestamps.isEmpty() && timestamps.peekFirst() < windowStart){
            timestamps.pollFirst();
        }

        if(timestamps.size() >= limit){
            return false;
        }

        timestamps.addLast(now);
        return true;
    }
}

class AlgorithmFactory {

    private final Map<Algorithm, RateLimitingAlgorithm> algorithms =
            new EnumMap<>(Algorithm.class);

    public AlgorithmFactory(
            TokenBucketStore tokenBucketStore,
            SlidingWindowStore slidingWindowStore
    ) {
        algorithms.put(Algorithm.TOKEN_BUCKET, new TokenBucket(tokenBucketStore));
        algorithms.put(Algorithm.SLIDING_WINDOW, new SlidingWindow(slidingWindowStore));
    }

    public RateLimitingAlgorithm getAlgorithm(Algorithm algorithm) {
        return algorithms.get(algorithm);
    }
}

class RuleService {

    public Rule getRule(String api) {
        // In real systems: DB / Config Service + cache
        return new Rule(60, 20, Algorithm.TOKEN_BUCKET);
    }
}

class Rule {

    private final int timeWindowSec;
    private final int limit;
    private final Algorithm algorithm;

    public Rule(int timeWindowSec, int limit, Algorithm algorithm) {
        this.timeWindowSec = timeWindowSec;
        this.limit = limit;
        this.algorithm = algorithm;
    }

    public int getTimeWindowSec() {
        return timeWindowSec;
    }

    public int getLimit() {
        return limit;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }
}

enum Algorithm{
    SLIDING_WINDOW, TOKEN_BUCKET
}