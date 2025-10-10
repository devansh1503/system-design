import java.util.*;

// ---------- ENUMS ----------
enum BookStatus {
    AVAILABLE, ISSUED, RESERVED
}

enum Genre {
    FICTION, SCIENCE, HISTORY, TECHNOLOGY, OTHER
}

// ---------- ENTITIES ----------
class Book {
    private final String id;
    private final String title;
    private final String author;
    private final Genre genre;
    private BookStatus status;

    public Book(String id, String title, String author, Genre genre) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.status = BookStatus.AVAILABLE;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Genre getGenre() { return genre; }
    public BookStatus getStatus() { return status; }

    public void setStatus(BookStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s (%s) - %s", id, title, author, genre, status);
    }
}

class Shelf {
    private final String shelfId;
    private final int capacity;
    private List<Book> books;

    public Shelf(String shelfId, int capacity) {
        this.shelfId = shelfId;
        this.capacity = capacity;
        this.books = new ArrayList<>();
    }

    public boolean addBook(Book book) {
        if (books.size() >= capacity) {
            System.out.println("Shelf is full!");
            return false;
        }
        books.add(book);
        return true;
    }

    public boolean removeBook(String bookId) {
        return books.removeIf(book -> book.getId().equals(bookId));
    }

    public List<Book> getBooks() { return books; }
}

class Library {
    private final String name;
    public List<Shelf> shelves;

    public Library(String name) {
        this.name = name;
        this.shelves = new ArrayList<>();
    }

    public void addShelf(Shelf shelf) { shelves.add(shelf); }

    public Optional<Book> searchBookByTitle(String title) {
        return shelves.stream()
                .flatMap(shelf -> shelf.getBooks().stream())
                .filter(book -> book.getTitle().equalsIgnoreCase(title))
                .findFirst();
    }

    public void displayBooks() {
        shelves.forEach(shelf -> {
            shelf.getBooks().forEach(System.out::println);
        });
    }
}

// ---------- USERS ----------
abstract class User {
    protected final String userId;
    protected final String name;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
}

class Member extends User {
    private List<Book> borrowedBooks;

    public Member(String userId, String name) {
        super(userId, name);
        this.borrowedBooks = new ArrayList<>();
    }

    public boolean borrowBook(Book book) {
        if (book.getStatus() != BookStatus.AVAILABLE) {
            System.out.println("Book not available!");
            return false;
        }
        book.setStatus(BookStatus.ISSUED);
        borrowedBooks.add(book);
        return true;
    }

    public boolean returnBook(Book book) {
        if (borrowedBooks.remove(book)) {
            book.setStatus(BookStatus.AVAILABLE);
            return true;
        }
        return false;
    }

    public void showBorrowedBooks() {
        borrowedBooks.forEach(System.out::println);
    }
}

class Librarian extends User {
    public Librarian(String userId, String name) {
        super(userId, name);
    }

    public void addBookToLibrary(Library library, Book book) {
        // Add book to first shelf with space
        for (Shelf shelf : library.shelves) {
            if (shelf.addBook(book)) return;
        }
        System.out.println("No shelf with space found!");
    }
}

// ---------- TEST / MAIN ----------
public class LibraryManagementSystem {
    public static void main(String[] args) {
        Library library = new Library("City Library");

        Shelf shelf1 = new Shelf("S1", 3);
        library.addShelf(shelf1);

        Librarian librarian = new Librarian("L1", "John");
        librarian.addBookToLibrary(library, new Book("B1", "Java Basics", "James", Genre.TECHNOLOGY));
        librarian.addBookToLibrary(library, new Book("B2", "World History", "Alex", Genre.HISTORY));

        library.displayBooks();

        Member member = new Member("M1", "Alice");
        library.searchBookByTitle("Java Basics").ifPresent(member::borrowBook);

        System.out.println("\nBorrowed Books by Alice:");
        member.showBorrowedBooks();

        System.out.println("\nLibrary After Borrow:");
        library.displayBooks();
    }
}
