import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

class Movie {
    private String title;
    private int duration;
    public Movie(String title, int duration) {
        this.title = title;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }
    public int getDuration() {
        return duration;
    }
}



class Seat {
    private int seatNumber;
    private PricingStrategy pricingStrategy;
    private boolean isAvailable;

    public Seat(int seatNumber, PricingStrategy pricingStrategy) {
        this.seatNumber = seatNumber;
        this.pricingStrategy = pricingStrategy;
        this.isAvailable = true;
    }

    public int getSeatNumber() {
        return seatNumber;
    }
    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }
    public BigDecimal getRate() {
        return pricingStrategy.getRate();
    }
    public boolean isAvailable() {
        return isAvailable;
    }
    public void occupy() {
        isAvailable = false;
    }
    public void vacate() {
        isAvailable = true;
    }
}

interface PricingStrategy {
    BigDecimal getRate();
}

class NormalRate implements PricingStrategy {
    public BigDecimal getRate() {
        return new BigDecimal("100.0");
    }
}

class PremiumRate implements PricingStrategy {
    public BigDecimal getRate() {
        return new BigDecimal("500.0");
    }
}

class VIPRate implements PricingStrategy {
    public BigDecimal getRate() {
        return new BigDecimal("1000.0");
    }
}







interface Layout {
    void addSeat(Seat seat, int row, int col);
    Seat getSeatByNumber(int seatNumber);
    Seat getSeatByPosition(int row, int col);
    List<Seat> getAllSeats();
}

class ClassicLayout implements Layout {
    int rows;
    int cols;
    HashMap<Integer, HashMap<Integer, Seat>> seats;
    HashMap<Integer, Seat> seatByNumber;

    public ClassicLayout(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    public void addSeat(Seat seat, int row, int col) {
        seats.putIfAbsent(row, new HashMap<>());
        seats.get(row).put(col, seat);
        seatByNumber.put(seat.getSeatNumber(), seat);
    }
    public Seat getSeatByNumber(int seatNumber) {
        if(seatByNumber.containsKey(seatNumber)) {
            return seatByNumber.get(seatNumber);
        }
        return null;
    }
    public Seat getSeatByPosition(int row, int col) {
        if(seats.containsKey(row) && seats.get(row).containsKey(col)) {
            return seats.get(row).get(col);
        }
        return null;
    }
    public List<Seat> getAllSeats() {
        return List.copyOf(seatByNumber.values());
    }
}







class Room {
    String roomNumber;
    Layout layout;

    public Room(String roomNumber, Layout layout) {
        this.roomNumber = roomNumber;
        this.layout = layout;
    }
}

class Cinema {
    String name;
    String location;
    List<Room> rooms;
    public Cinema(String name, List<Room> rooms, String location) {
        this.name = name;
        this.rooms = rooms;
        this.location = location;
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }
}






class Screening {
    Movie movie;
    Room room;
    LocalDateTime startTime;
    LocalDateTime endTime;

    public Screening(Movie movie, Room room, LocalDateTime startTime, LocalDateTime endTime) {
        this.movie = movie;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public BigDecimal getDuration(){
        return new BigDecimal(
                Duration.between(startTime, endTime).toMinutes()
        );
    }

    public Movie getMovie() {
        return movie;
    }

    public Room getRoom() {
        return room;
    }
}





class MovieTicket {
    Screening screening;
    Seat seat;
    BigDecimal price;

    public MovieTicket(Screening screening, Seat seat) {
        this.screening = screening;
        this.seat = seat;
        this.price = seat.getRate();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Screening getScreening() {
        return screening;
    }
}





class Order {
    List<MovieTicket> movieTickets;
    LocalDateTime orderDate;
    public Order(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public void addMovieTicket(MovieTicket movieTicket) {
        movieTickets.add(movieTicket);
    }

    public BigDecimal getPrice() {
        BigDecimal price = new BigDecimal("0");
        for(MovieTicket movieTicket : movieTickets) {
            price.add(movieTicket.getPrice());
        }
        return price;
    }
}





class ScreeningManager {
    Map<Movie, List<Screening>> moviesByScreenings;
    Map<Screening, List<MovieTicket>> ticketsByScreenings;

    public void addScreening(Screening screening) {
        Movie movie = screening.getMovie();
        moviesByScreenings.putIfAbsent(movie, new ArrayList<>());
        moviesByScreenings.get(movie).add(screening);

        ticketsByScreenings.putIfAbsent(screening, new ArrayList<>());
    }

    public void addTicket(MovieTicket movieTicket) {
        Screening screening = movieTicket.getScreening();
        ticketsByScreenings.putIfAbsent(screening, new ArrayList<>());
        ticketsByScreenings.get(screening).add(movieTicket);
    }

    public List<Screening> getScreenings(Movie movie) {
        return moviesByScreenings.get(movie);
    }

    public List<MovieTicket> getTickets(Screening screening) {
        return ticketsByScreenings.get(screening);
    }

    public List<Seat> getAvailableSeats(Screening screening) {
        Room room = screening.getRoom();
        Layout layout = room.layout;

        List<Seat> availableSeats = new ArrayList<>();

        for(Seat seat : layout.getAllSeats()) {
            if(seat.isAvailable()) {
                availableSeats.add(seat);
            }
        }

        return availableSeats;
    }
}





public class MovieTicketBooking {
    private final List<Movie>  movies;
    private final List<Cinema> cinemas;
    private final ScreeningManager screeningManager;

    public MovieTicketBooking(){
        movies = new ArrayList<>();
        cinemas = new ArrayList<>();
        screeningManager = new ScreeningManager();
    }

    public void addMovie(Movie movie) {
        movies.add(movie);
    }
    public void addCinema(Cinema cinema) {
        cinemas.add(cinema);
    }
    public void addScreening(Screening screening) {
        screeningManager.addScreening(screening);
    }
    public void bookTicket(Screening screening, Seat seat) {
        MovieTicket ticket = new MovieTicket(screening, seat);
        screeningManager.addTicket(ticket);
    }

    public List<Seat> getAvailableSeats(Screening screening) {
        return screeningManager.getAvailableSeats(screening);
    }

    public int getTicketCount(Screening screening) {
        return screeningManager.ticketsByScreenings.get(screening).size();
    }

    public List<MovieTicket> getTicketsForScreening(Screening screening) {
        return screeningManager.getTickets(screening);
    }

}
