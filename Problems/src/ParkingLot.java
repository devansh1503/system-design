import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

enum VehicleSize{
    SMALL,
    MEDIUM,
    LARGE
}

abstract class Vehicle {
    private String licensePlate;
    private VehicleSize vehicleSize;

    public Vehicle(String licensePlate, VehicleSize vehicleSize) {
        this.licensePlate = licensePlate;
        this.vehicleSize = vehicleSize;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public VehicleSize getVehicleSize() {
        return vehicleSize;
    }
    public void setVehicleSize(VehicleSize vehicleSize) {
        this.vehicleSize = vehicleSize;
    }
}

class Motorcycle extends Vehicle {
    public Motorcycle(String licensePlate) {
        super(licensePlate, VehicleSize.SMALL);
    }
}

class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleSize.MEDIUM);
    }
}

class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate, VehicleSize.LARGE);
    }
}

// NOTE: I haven't implemented park and unpark in the abstract class although the implementation is same for now
//      Just to accomodate flexibility in algorithms. In future we may decide park multiple Small vehicles in Medium
//      Or Large.

abstract class ParkingSpot {
    private boolean isAvailable;
    private VehicleSize vehicleSize;
    private int spotNumber;
    private Vehicle vehicle;

    public ParkingSpot(boolean isAvailable, VehicleSize vehicleSize, int spotNumber) {
        this.isAvailable = isAvailable;
        this.vehicleSize = vehicleSize;
        this.spotNumber = spotNumber;
        this.vehicle = null;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
    public VehicleSize getVehicleSize() {
        return vehicleSize;
    }
    public int getSpotNumber() {
        return spotNumber;
    }
    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
    public abstract void park(Vehicle vehicle);
    public abstract void unpark();
}

class CompactSpot extends ParkingSpot {
    public CompactSpot(int spotNumber) {
        super(true, VehicleSize.SMALL, spotNumber);
    }
    @Override
    public void park(Vehicle vehicle) {
        super.setAvailable(false);
        super.setVehicle(vehicle);
    }

    @Override
    public void unpark() {
        super.setAvailable(true);
        super.setVehicle(null);
    }
}

class RegulartSpot extends ParkingSpot {
    public RegulartSpot(int spotNumber) {
        super(true, VehicleSize.MEDIUM, spotNumber);
    }
    @Override
    public void park(Vehicle vehicle) {
        super.setAvailable(false);
        super.setVehicle(vehicle);
    }

    @Override
    public void unpark() {
        super.setAvailable(true);
        super.setVehicle(null);
    }
}

class OversizedSpot extends ParkingSpot {
    public OversizedSpot(int spotNumber) {
        super(true, VehicleSize.LARGE, spotNumber);
    }
    @Override
    public void park(Vehicle vehicle) {
        super.setAvailable(false);
        super.setVehicle(vehicle);
    }

    @Override
    public void unpark() {
        super.setAvailable(true);
        super.setVehicle(null);
    }
}


// Singleton
class ParkingManager {
    HashMap<Vehicle, ParkingSpot> vehicleToSpotMap;
    HashMap<VehicleSize, List<ParkingSpot>> availableSpotMap;
    public ParkingManager(HashMap<VehicleSize, List<ParkingSpot>> availableSpotMap) {
        this.availableSpotMap = availableSpotMap;
        this.vehicleToSpotMap = new HashMap<>();
    }

    public ParkingSpot findSpot(Vehicle vehicle){
        VehicleSize vehicleSize = vehicle.getVehicleSize();

        for(ParkingSpot spot : availableSpotMap.get(vehicleSize)){
            if(spot.isAvailable()){
                return spot;
            }
        }

        return null;
    }

    public ParkingSpot parkVehicle(Vehicle vehicle){
        ParkingSpot spot = findSpot(vehicle);
        if(spot != null){
            spot.park(vehicle);
            vehicleToSpotMap.put(vehicle, spot);
            return spot;
        }
        return null;
    }

    public void unparkVehicle(Vehicle vehicle){
        ParkingSpot spot = vehicleToSpotMap.get(vehicle);
        spot.unpark();
        vehicleToSpotMap.remove(vehicle);
    }

}


class Ticket {
    private int ticketNumber;
    private Vehicle vehicle;
    private ParkingSpot spot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;

    public Ticket(int ticketNumber, Vehicle vehicle, ParkingSpot spot, LocalDateTime entryTime) {
        this.ticketNumber = ticketNumber;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = entryTime;
        this.exitTime = null;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }
    public Vehicle getVehicle() {
        return vehicle;
    }
    public ParkingSpot getSpot() {
        return spot;
    }
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    public LocalDateTime getExitTime() {
        return exitTime;
    }
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
    public BigDecimal calculateDuration(){
        if(exitTime == null) setExitTime(LocalDateTime.now());

        return new BigDecimal(
                Duration.between(
                        entryTime,
                        exitTime
                ).toMinutes()
        );
    }
}

interface FareStrategy {
    BigDecimal calculateFare(Ticket ticket, BigDecimal inputFare);
}

class BaseFareStrategy implements FareStrategy {
    private static final BigDecimal SMALL_RATE = new BigDecimal("1.0");
    private static final BigDecimal MEDIUM_RATE = new BigDecimal("2.0");
    private static final BigDecimal LARGE_RATE = new BigDecimal("3.0");
    @Override
    public BigDecimal calculateFare(Ticket ticket, BigDecimal inputFare) {
        BigDecimal duration = ticket.calculateDuration();
        BigDecimal rate = BigDecimal.ZERO;
        if(ticket.getVehicle().getVehicleSize() == VehicleSize.SMALL){
            rate = inputFare.multiply(SMALL_RATE);
        }
        else if(ticket.getVehicle().getVehicleSize() == VehicleSize.MEDIUM){
            rate = inputFare.multiply(MEDIUM_RATE);
        }
        else if(ticket.getVehicle().getVehicleSize() == VehicleSize.LARGE){
            rate = inputFare.multiply(LARGE_RATE);
        }

        BigDecimal fare = inputFare.add(rate.multiply(duration));
        return fare;
    }
}

class PeakHourMultiplierStrategy implements FareStrategy {
    private static final BigDecimal MULTIPLIER = new BigDecimal("1.5");
    @Override
    public BigDecimal calculateFare(Ticket ticket, BigDecimal inputFare) {
        if(isPeakHours(ticket.getEntryTime())){
            return inputFare.multiply(MULTIPLIER);
        }
        return inputFare;
    }
    public boolean isPeakHours(LocalDateTime time){
        int hour = time.getHour();
        return (hour >= 2 && hour <8);
    }
}


//Singleton
class FareCalculator {
    List<FareStrategy> fareStrategies;
    public FareCalculator(List<FareStrategy> fareStrategies) {
        this.fareStrategies = fareStrategies;
    }

    public BigDecimal calculateFare(Ticket ticket) {
        BigDecimal fare = BigDecimal.ONE;
        for(FareStrategy fareStrategy : fareStrategies){
            fare = fareStrategy.calculateFare(ticket, fare);
        }
        return fare;
    }
}


public class ParkingLot {
    ParkingManager parkingManager;
    FareCalculator fareCalculator;
    public ParkingLot(ParkingManager parkingManager, FareCalculator fareCalculator) {
        this.parkingManager = parkingManager;
        this.fareCalculator = fareCalculator;
    }

    public Ticket enterVehicle(Vehicle vehicle) {
        ParkingSpot spot = parkingManager.parkVehicle(vehicle);
        if(spot == null) return null;
        return new Ticket(123, vehicle, spot, LocalDateTime.now());
    }

    public void leaveVehicle(Ticket ticket) {
        Vehicle vehicle = ticket.getVehicle();
        parkingManager.unparkVehicle(vehicle);
        BigDecimal fare = fareCalculator.calculateFare(ticket);
    }
}
