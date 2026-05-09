import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

enum Direction {
    UP,
    DOWN,
    IDLE
}

class ElevatorStatus {
    int currentFloor;
    Direction direction;

    public ElevatorStatus(int currentFloor, Direction direction) {
        this.currentFloor = currentFloor;
        this.direction = direction;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }
    public Direction getDirection() {
        return direction;
    }
    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
class ElevatorCar {
    private ElevatorStatus status;
    private final Queue<Integer> targetFloors;
    private int startingFloor;

    public ElevatorCar(int startingFloor){
        this.startingFloor = startingFloor;
        targetFloors = new LinkedList<>();
    }

    public ElevatorStatus getStatus() {
        return status;
    }

    public void addFloorRequest(int floor){
        if(!targetFloors.contains(floor)) {
            targetFloors.add(floor);
            updateDirection(floor);
        }
    }
    private void updateDirection(int targetFloor){
        if(status.getCurrentFloor() > targetFloor){
            status = new ElevatorStatus(status.getCurrentFloor(), Direction.DOWN);
        }
        else if(status.getCurrentFloor() < targetFloor){
            status = new ElevatorStatus(status.getCurrentFloor(), Direction.UP);
        }
    }
    public boolean isIdle(){
        return targetFloors.isEmpty();
    }
    public Direction getCurrentDirection(){
        return status.getDirection();
    }
    public int getCurrentFloor(){
        return status.getCurrentFloor();
    }
}


class ElevatorDispatch {
    private final DispatchStrategy strategy;
    public ElevatorDispatch(DispatchStrategy strategy){
        this.strategy = strategy;
    }

    public void dispatchElevator(int floor, Direction direction, List<ElevatorCar> elevators){
        ElevatorCar selectedElevator = strategy.selectElevator(elevators, floor, direction);
        if(selectedElevator != null){
            //This isn't the user request, this is a command to stop the elevator
            //At that floor to pick the user
            selectedElevator.addFloorRequest(floor);
        }
    }

}

interface DispatchStrategy {
    ElevatorCar selectElevator(List<ElevatorCar> elevators, int floor, Direction direction);
}

class FirstComeFirstServeStrategy implements DispatchStrategy {
    @Override
    public ElevatorCar selectElevator(List<ElevatorCar> elevators, int floor, Direction direction){
        for(ElevatorCar elevator : elevators){
            if(elevator.isIdle() && elevator.getCurrentDirection() == direction){
                return elevator;
            }
        }
        return elevators.get((int)(Math.random()*elevators.size()));
    }
}

class ShortestSeekTimeFirstStrategy implements DispatchStrategy {
    @Override
    public ElevatorCar selectElevator(List<ElevatorCar> elevators, int floor, Direction direction){
        ElevatorCar bestElevator = null;
        int shortestDistance = Integer.MAX_VALUE;
        for(ElevatorCar elevator : elevators){
            int dist = Math.abs(elevator.getCurrentFloor() - floor);
            if(elevator.isIdle() && elevator.getCurrentDirection() == direction){
                if(dist < shortestDistance){
                    bestElevator = elevator;
                    shortestDistance = dist;
                }
            }
        }

        return bestElevator;
    }
}

public class ElevatorSystem {
    private final List<ElevatorCar> elevators;
    private final ElevatorDispatch elevatorDispatchController;

    public ElevatorSystem(List<ElevatorCar> elevators, DispatchStrategy strategy){
        this.elevators = elevators;
        this.elevatorDispatchController = new ElevatorDispatch(strategy);
    }

    public List<ElevatorStatus> getAllElevatorStatus(){
        List<ElevatorStatus> elevatorStatuses = new ArrayList<>();
        for(ElevatorCar elevator : elevators){
            elevatorStatuses.add(elevator.getStatus());
        }
        return elevatorStatuses;
    }

    public void requestElevator(int currentFloor, Direction direction){
        elevatorDispatchController.dispatchElevator(currentFloor, direction, elevators);
    }

    public void selectFloor(ElevatorCar car, int floor){
        //This is where user selects the car
        car.addFloorRequest(floor);
    }

}
