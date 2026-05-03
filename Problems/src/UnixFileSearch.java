import java.util.* ;
import java.util.regex.Pattern;

enum FileAttribute {
    IS_DIRECTORY,
    FILENAME,
    SIZE,
    OWNER
}

class File{
    private boolean isDirectory;
    private String filename;
    private int size;
    private String owner;
    Set<File> entries;

    public File(boolean isDirectory, String filename, int size, String owner){
        this.isDirectory = isDirectory;
        this.filename = filename;
        this.size = size;
        this.owner = owner;
        this.entries = new HashSet<>();
    }

    public boolean isDirectory() {
        return isDirectory;
    }
    public String getFilename() {
        return filename;
    }
    public int getSize() {
        return size;
    }
    public String getOwner() {
        return owner;
    }
    public Set<File> getEntries() {
        return entries;
    }
    public void addEntry(File file){
        entries.add(file);
    }
    public Object extractAttribute(FileAttribute fileAttribute){
        return switch (fileAttribute) {
            case IS_DIRECTORY -> isDirectory;
            case FILENAME -> filename;
            case SIZE -> size;
            case OWNER -> owner;
            default -> null;
        };
    }
}

interface Predicate {
    boolean isMatch(File file);
}

class SimplePredicate<T> implements Predicate {
    FileAttribute fileAttribute;
    ComparisonOperator<T> operator;
    T expectedValue;

    public SimplePredicate(FileAttribute fileAttribute, ComparisonOperator<T> operator, T expectedValue) {
        this.fileAttribute = fileAttribute;
        this.operator = operator;
        this.expectedValue = expectedValue;
    }
    @Override
    public boolean isMatch(File file) {
        Object attributeValue = file.extractAttribute(fileAttribute);
        if(expectedValue.getClass().isInstance(attributeValue)){
            return operator.operate((T)attributeValue, expectedValue);
        }
        return false;
    }
}

interface CompositePredicate extends Predicate { }

class AndPredicate implements CompositePredicate {
    List<Predicate> operands;

    public AndPredicate(List<Predicate> operands) {
        this.operands = operands;
    }

    @Override
    public boolean isMatch(File file) {
        return operands.stream().allMatch(p -> p.isMatch(file));
    }
}

class OrPredicate implements CompositePredicate {
    List<Predicate> operands;

    public OrPredicate(List<Predicate> operands) {
        this.operands = operands;
    }

    @Override
    public boolean isMatch(File file) {
        return operands.stream().anyMatch(p -> p.isMatch(file));
    }
}

class NotPredicate implements CompositePredicate {
    Predicate operand;

    public NotPredicate(Predicate operand) {
        this.operand = operand;
    }

    @Override
    public boolean isMatch(File file) {
        return !operand.isMatch(file);
    }

}

interface ComparisonOperator<T> {
    boolean operate(T a, T b);
}
class EqualityOperator<T> implements ComparisonOperator<T> {
    @Override
    public boolean operate(T a, T b) {
        return a.equals(b);
    }
}
class GreaterThanOperator<T extends Number> implements ComparisonOperator<T> {
    @Override
    public boolean operate(T a, T b) {
        return Double.compare(a.doubleValue(), b.doubleValue()) > 0;
    }
}
class LessThanOperator<T extends Number> implements ComparisonOperator<T> {
    @Override
    public boolean operate(T a, T b) {
        return Double.compare(a.doubleValue(), b.doubleValue()) < 0;
    }
}
class RegexOperator<T extends String> implements ComparisonOperator<T> {
    @Override
    public boolean operate(T a, T b) {
        Pattern p = Pattern.compile(b);
        return p.matcher(a).matches();
    }
}


class FileSearchCriteria {
    private final Predicate predicate;
    public FileSearchCriteria(Predicate predicate) {
        this.predicate = predicate;
    }

    public boolean isMatch(File file) {
        return predicate.isMatch(file);
    }
}

class FileSearch {
    public List<File> search(File root, FileSearchCriteria criteria) {
        List<File> files = new ArrayList<>();

        Queue<File> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            File file = queue.poll();
            if (criteria.isMatch(file)) {
                files.add(file);
            }
            for(File child : file.getEntries()){
                queue.add(child);
            }
        }
        return files;
    }
}




public class UnixFileSearch {
    public static void main(String[] args) {
        File root = new File(true, "root", 10, "devansh");
        File childA = new File(false, "adam", 200, "adam");
        File childB = new File(false, "bob", 300, "bob");

        root.addEntry(childA);
        root.addEntry(childB);

        Predicate size100 = new SimplePredicate<>(FileAttribute.SIZE, new GreaterThanOperator<>(), 100);
        Predicate size150 = new SimplePredicate<>(FileAttribute.SIZE, new GreaterThanOperator<>(), 150);
        Predicate size300 = new SimplePredicate<>(FileAttribute.SIZE, new LessThanOperator<>(), 300);
        Predicate ownerBob = new SimplePredicate<>(FileAttribute.OWNER, new EqualityOperator<>(), "bob");
        Predicate ownerAdam = new SimplePredicate<>(FileAttribute.OWNER, new EqualityOperator<>(), "adam");

        List<Predicate> firstConditions = new ArrayList<>();
        firstConditions.add(size100);
        firstConditions.add(ownerBob);

        Predicate AndFirst = new AndPredicate(firstConditions);

        List<Predicate> secondConditions = new ArrayList<>();
        secondConditions.add(size150);
        secondConditions.add(ownerAdam);
        secondConditions.add(size300);

        Predicate AndSecond = new AndPredicate(secondConditions);

        List<Predicate> OrOuter = new ArrayList<>();
        OrOuter.add(AndSecond);
        OrOuter.add(AndFirst);

        Predicate OrMain = new OrPredicate(OrOuter);

        FileSearchCriteria criteria = new FileSearchCriteria(OrMain);

        FileSearch fileSearch = new FileSearch();

        List<File> files = fileSearch.search(root, criteria);

        for(File file : files){
            System.out.println(file.getFilename());
        }

    }


}
