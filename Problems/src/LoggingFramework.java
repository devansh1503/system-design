import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

enum Level{
    INFO, DEBUG, ERROR
}
enum Appenders{
    CONSOLE, FILE
}
class Logger{
    static Logger _logger;
    public Logger() {
    }

    public static synchronized Logger getLogger(){
        if(_logger == null){
            _logger = new Logger();
            LoggingManager.createChain();
        }
        return _logger;
    }

    public static void addAppender(Appender appender){
        AppenderManager.AddAppender(appender);
    }

    public void info(String message){
        executeLogger(message, Level.INFO);
    }
    public void debug(String message){
        executeLogger(message, Level.DEBUG);
    }
    public void error(String message){
        executeLogger(message, Level.ERROR);
    }
    public void executeLogger(String message, Level level){
        LoggingManager.execute(message, level);
        AppenderManager.notifyAppenders(message, level);
    }


}

class LoggingManager {
    static class LevelNode{
        Level level;
        LevelManager logger;
        LevelNode next;
        public LevelNode(Level level, LevelManager logger){
            this.level = level;
            this.logger = logger;
        }
    }
    static LevelNode head;

    public static void createChain(){
        LevelNode info = new LevelNode(Level.INFO, new InfoLogger());
        LevelNode debug = new LevelNode(Level.DEBUG, new DebugLogger());
        LevelNode error = new LevelNode(Level.ERROR, new ErrorLogger());
        info.next = debug;
        debug.next = error;
        head = info;
    }

    public static void execute(String message, Level level){
        LevelNode temp = head;
        while(temp!=null) {
            temp.logger.executeLog(message, level);
            temp = temp.next;
        }
    }

}

abstract class LevelManager {
    Level level;

    public LevelManager(Level level){
        this.level = level;
    }

    public void executeLog(String message, Level level){
        if(level == this.level){
            Display(message, level);
        }
    }
    public void Display(String message, Level level){
        LocalDateTime timestamp = LocalDateTime.now();
        String log = String.format("[%s] [%s] : %s", timestamp, level, message);
        System.out.println(log);
    }
}
class InfoLogger extends LevelManager {
    public InfoLogger(){
        super(Level.INFO);
    }
}

class DebugLogger extends LevelManager {
    public DebugLogger(){
        super(Level.DEBUG);
    }
}

class ErrorLogger extends LevelManager {
    public ErrorLogger(){
        super(Level.ERROR);
    }
}

class AppenderManager{
    static List<Appender>appenders = new ArrayList<>();
    public static void AddAppender(Appender appender){
        appenders.add(appender);
    }
    public static void notifyAppenders(String message, Level level){
        for(Appender appender : appenders){
            appender.appendLog(message, level);
        }
    }
}

abstract class Appender{
    Appenders appender;
    public Appender(Appenders appender){
        this.appender = appender;
    }
    public void appendLog(String message, Level level){
        String msg = String.format("[%s] [%s] : %s", appender, level, message);
        System.out.println(msg);
    }
}

class ConsoleAppender extends Appender{
    public ConsoleAppender(){
        super(Appenders.CONSOLE);
    }
}

class FileAppender extends Appender{
    public FileAppender(){
        super(Appenders.FILE);
    }
}

public class LoggingFramework {
    public static void main(String[] args){
        Logger logger = Logger.getLogger();
        logger.addAppender(new ConsoleAppender());
        logger.addAppender(new FileAppender());

        logger.info("This is my first Log");
        logger.debug("This is my debug log");
        logger.error("This is my error Log");
    }
}
