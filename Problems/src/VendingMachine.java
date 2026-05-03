import java.util.*;

class Product {
    private String uid;
    private String desc;
    private Double price;
    public Product(String uid, String desc, Double price) {
        this.uid = uid;
        this.desc = desc;
        this.price = price;
    }
    public Double getPrice() {
        return price;
    }
}

class Rack {
    private String uid;
    private Product product;
    private int count;
    public Rack(String uid, Product product) {
        this.uid = uid;
        this.product = product;
        this.count = 0;
    }

    public void updateCount(int count) {
        this.count = count;
    }
    public int getCount() {
        return count;
    }
    public Product getProduct() {
        return product;
    }
}

class InventoryManager {
    Map<String, Rack> racks = new HashMap<>();
    public InventoryManager() {
        racks = new HashMap<>();
    }

    public Product getProductInRack(String rackID){
        return racks.get(rackID).getProduct();
    }

    public void UpdateRacks(Map<String, Rack> racks){
        this.racks = racks;
    }

    public void dispenseProductFromRack(Rack rack){
        if(rack.getCount() > 0){
            rack.updateCount(rack.getCount() - 1);
        }
    }

    public Rack getRack(String rackID){
        return racks.get(rackID);
    }
}

class PaymentProcessor {
    private Double currentBalance;
    public PaymentProcessor() {
        this.currentBalance = 0.0;
    }

    public void addBalnce(Double balance){
        this.currentBalance += balance;
    }

    public void charge(Double amount){
        this.currentBalance -= amount;
    }

    public Double getCurrentBalance(){
        return currentBalance;
    }

    public Double returnChange(){
        Double change = currentBalance;
        currentBalance = 0.0;
        return change;
    }

}

class Transaction {
    Rack rack;
    Product product;
    Double amount;

    public Transaction() {
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setRack(Rack rack) {
        this.rack = rack;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}

class InvalidStateException extends Throwable {
    public InvalidStateException(String message) {
        super(message);
    }
}

interface VendingMachineState {
    void insertMoney(VendingMachine machine, double amount) throws InvalidStateException;
    Product selectProduct(VendingMachine machine, String rackId)  throws InvalidStateException;
    void dispenseProductFromRack(VendingMachine machine)  throws InvalidStateException;
    void getStateDescription();
}

class NoMoneyInsertedState implements VendingMachineState {
    @Override
    public void insertMoney(VendingMachine machine, double amount) {
        machine.getPaymentProcessor().addBalnce(amount);
    }
    @Override
    public Product selectProduct(VendingMachine machine, String rackId) throws InvalidStateException {
        throw new InvalidStateException("No money inserted");
    }
    @Override
    public void dispenseProductFromRack(VendingMachine machine) throws InvalidStateException {
        throw new InvalidStateException("No money inserted");
    }
    @Override
    public void getStateDescription() {
        System.out.println("Please insert money");
    }
}

class MoneyInsertedState implements VendingMachineState {
    @Override
    public void insertMoney(VendingMachine machine, double amount) throws InvalidStateException {
        throw new InvalidStateException("Money inserted");
    }
    @Override
    public Product selectProduct(VendingMachine machine, String rackId) {
        Transaction currentTransaction = machine.getCurrentTransaction();
        Product product = machine.getInventoryManager().getProductInRack(rackId);
        Rack rack = machine.getInventoryManager().getRack(rackId);
        currentTransaction.setProduct(product);
        currentTransaction.setRack(rack);
        currentTransaction.setAmount(product.getPrice());

        return product;
    }
    @Override
    public void dispenseProductFromRack(VendingMachine machine) throws InvalidStateException {
        throw new InvalidStateException("No Product Selected");
    }
    @Override
    public void getStateDescription() {
        System.out.println("Please Select Product");
    }
}

class DispensedState implements VendingMachineState {
    @Override
    public void insertMoney(VendingMachine machine, double amount) throws InvalidStateException {
        throw new InvalidStateException("Money inserted");
    }
    @Override
    public Product selectProduct(VendingMachine machine, String rackId) throws InvalidStateException {
        throw new InvalidStateException("Product already selected");
    }
    @Override
    public void dispenseProductFromRack(VendingMachine machine) {
        //Before this we need to validate transaction object as well, it should not be invalid or null
        Transaction currentTransaction = machine.getCurrentTransaction();
        machine.getPaymentProcessor().returnChange();
        machine.getInventoryManager().dispenseProductFromRack(currentTransaction.rack);
        machine.updateTransactionHistory();
    }
    @Override
    public void getStateDescription() {
        System.out.println("Product Is Despensing...");
    }
}
public class VendingMachine {
    private final List<Transaction> history;
    private final InventoryManager inventoryManager;
    private final PaymentProcessor paymentProcessor;
    private Transaction currentTransaction;
    private VendingMachineState state;

    public VendingMachine() {
        this.inventoryManager = new InventoryManager();
        this.paymentProcessor = new PaymentProcessor();
        this.history = new ArrayList<>();
        this.currentTransaction = new Transaction();
        this.state = new NoMoneyInsertedState();
    }

    public void setRack(Map<String, Rack> racks) {
        inventoryManager.UpdateRacks(racks);
    }
    public void insertMoney(double amount) throws InvalidStateException {
        state.insertMoney(this, amount);
    }
    public Product selectProduct(String rackId) throws InvalidStateException {
        return state.selectProduct(this, rackId);
    }
    public void dispenseProductFromRack() throws InvalidStateException {
        state.dispenseProductFromRack(this);
    }
    public void updateTransactionHistory(){
        history.add(this.currentTransaction);
    }
    public void cancelTransaction(){
        paymentProcessor.returnChange();
        currentTransaction = new Transaction();
    }
    public PaymentProcessor getPaymentProcessor() {
        return paymentProcessor;
    }
    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public Transaction getCurrentTransaction() {
        return currentTransaction;
    }

}
