import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ---------- ENUMS ----------
enum Status {
    SUCCESS,
    FAILURE,
    PARTIAL
}

// ---------- INTERFACES ----------
interface Vehicle {
    double calculateCharge();
    Slot getSlot();
    void setSlot(Slot slot);
    void setEntryTime();
    void setExitTime();
    double getArea();
    boolean isPaid();
    void markPaid();
}

interface Payment {
    Status makePayment(Vehicle vehicle);
}

// ---------- ABSTRACT BASE CLASS ----------
abstract class AbstractVehicle implements Vehicle {
    protected double costPerHour;
    protected double charge;
    protected boolean paid;
    protected double area;
    protected LocalDateTime entryTime, exitTime;
    protected Slot slot;

    public AbstractVehicle(double area, double costPerHour) {
        this.area = area;
        this.costPerHour = costPerHour;
    }

    @Override
    public double calculateCharge() {
        if (entryTime != null && exitTime != null) {
            long minutes = Duration.between(entryTime, exitTime).toMinutes();
            double hours = minutes / 60.0;
            this.charge = hours * costPerHour;
            return this.charge;
        }
        return -1.0; // invalid charge
    }

    @Override
    public Slot getSlot() {
        return this.slot;
    }

    @Override
    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    @Override
    public void setEntryTime() {
        this.entryTime = LocalDateTime.now();
    }

    @Override
    public void setExitTime() {
        this.exitTime = LocalDateTime.now();
    }

    @Override
    public double getArea() {
        return this.area;
    }

    @Override
    public boolean isPaid() {
        return this.paid;
    }

    @Override
    public void markPaid() {
        this.paid = true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                " [area=" + area + ", slot=" + (slot != null ? slot.getArea() : "NONE") + "]";
    }
}

// ---------- VEHICLE TYPES ----------
class Car extends AbstractVehicle {
    public Car(double area) {
        super(area, 20); // Rs. 20/hour
    }
}

class Bike extends AbstractVehicle {
    public Bike(double area) {
        super(area, 10); // Rs. 10/hour
    }
}

class Bus extends AbstractVehicle {
    public Bus(double area) {
        super(area, 30); // Rs. 30/hour
    }
}

// ---------- PAYMENT IMPLEMENTATION ----------
class Gpay implements Payment {
    @Override
    public Status makePayment(Vehicle vehicle) {
        double amount = vehicle.calculateCharge();
        if (amount < 0) {
            return Status.FAILURE;
        }
        vehicle.markPaid();
        System.out.println("Payment of Rs. " + amount + " done successfully via GPay.");
        return Status.SUCCESS;
    }
}

// ---------- PARKING COMPONENTS ----------
class Slot {
    private double area;
    private boolean occupied;

    public Slot(double area) {
        this.area = area;
        this.occupied = false;
    }

    public double getArea() {
        return area;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void useSlot() {
        this.occupied = true;
    }

    public void emptySlot() {
        this.occupied = false;
    }

    @Override
    public String toString() {
        return "Slot [area=" + area + ", occupied=" + occupied + "]";
    }
}

class Floor {
    private int size;
    List<Slot> slots = new ArrayList<>();

    public Floor(int size, double defaultArea) {
        this.size = size;
        for (int i = 0; i < size; i++) {
            this.slots.add(new Slot(defaultArea));
        }
    }

    public Slot getFirstEmptySlot(double requiredArea) {
        for (Slot slot : slots) {
            if (!slot.isOccupied() && slot.getArea() >= requiredArea) {
                return slot;
            }
        }
        return null;
    }

    public int getAvailableSlotsCount() {
        return (int) slots.stream().filter(s -> !s.isOccupied()).count();
    }
}

// ---------- SLOT MANAGER ----------
class SlotManager {
    static List<Floor> floors = new ArrayList<>();

    public static Status allocateSlot(Vehicle vehicle) {
        for (Floor floor : floors) {
            Slot availableSlot = floor.getFirstEmptySlot(vehicle.getArea());
            if (availableSlot != null) {
                vehicle.setSlot(availableSlot);
                vehicle.setEntryTime();
                availableSlot.useSlot();
                System.out.println(vehicle + " allocated to slot: " + availableSlot);
                return Status.SUCCESS;
            }
        }
        System.out.println("No available slot for " + vehicle);
        return Status.FAILURE;
    }

    public static void exitSlot(Vehicle vehicle) {
        vehicle.setExitTime();
        if (vehicle.getSlot() != null) {
            vehicle.getSlot().emptySlot();
            System.out.println(vehicle + " has exited. Slot is now free.");
        }
    }
}

// ---------- MAIN ----------
public class ParkingLot {
    public static void main(String[] args) {
        int numOfFloors = 3;
        int numOfSlots = 5;

        // Initialize Floors and Slots
        for (int i = 0; i < numOfFloors; i++) {
            Floor floor = new Floor(numOfSlots, 30);
            SlotManager.floors.add(floor);
        }

        Vehicle car = new Car(20);
        Vehicle bike = new Bike(10);
        Vehicle bus = new Bus(30);

        SlotManager.allocateSlot(car);
        SlotManager.allocateSlot(bike);
        SlotManager.allocateSlot(bus);

        try {
            Thread.sleep(2000); // Simulate 2 seconds of parking time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SlotManager.exitSlot(car);
        Payment payment = new Gpay();
        payment.makePayment(car);
    }
}
