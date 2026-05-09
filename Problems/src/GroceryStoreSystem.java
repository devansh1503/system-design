import java.math.BigDecimal;
import java.util.*;

enum Category {
    FRUIT,
    VEGETABLE,
    CANNED,
    PULSES,
    SNACKS
}

class Item{
    private final String name;
    private final String barcode;
    private final Category category;
    private final BigDecimal price;
    public Item(String name, String barcode, Category category, BigDecimal price) {
        this.name = name;
        this.barcode = barcode;
        this.category = category;
        this.price = price;
    }

    public String getName() {
        return name;
    }
    public String getBarcode() {
        return barcode;
    }
    public Category getCategory() {
        return category;
    }
    public BigDecimal getPrice() {
        return price;
    }
}

class Catelogue{
    private final Map<String, Item> barcodeToItemMap;
    public Catelogue(){
        barcodeToItemMap = new HashMap<>();
    }
    public void updateItem(Item item){
        barcodeToItemMap.put(item.getBarcode(), item);
    }
    public void removeItem(Item item){
        barcodeToItemMap.remove(item.getBarcode());
    }
    public Item getItem(String barcode){
        return barcodeToItemMap.get(barcode);
    }
}

class Inventory{
    private final Map<String, Integer> stock;
    public Inventory(){
        stock = new HashMap<>();
    }
    public void addStock(String barcode, int quantity){
        stock.put(barcode, stock.getOrDefault(barcode, 0) + quantity);
    }
    public void reduceStock(String barcode, int quantity){
        stock.replace(barcode, stock.get(barcode) - quantity);
    }
    public int getStock(String barcode){
        return stock.get(barcode);
    }
}

class GroceryOrder{
    private final String orderId;
    private final List<OrderItem> orderItems;
    private final Map<OrderItem, Discount> itemToDiscount;
    private BigDecimal paymentAmount = BigDecimal.ZERO;

    public GroceryOrder(String orderId){
        this.orderId = orderId;
        this.itemToDiscount = new HashMap<>();
        this.orderItems = new ArrayList<>();
    }
    public void addItem(OrderItem item){
        orderItems.add(item);
    }
    public BigDecimal calculateSubtotal(){
        return orderItems.stream()
                .map(OrderItem::calculatePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal calculateTotal(){
        return orderItems.stream()
                .map(
                        item -> {
                            Discount discount = itemToDiscount.get(item);
                            return discount!=null
                                    ? item.calculatePriceWithDiscount(discount)
                                    : item.calculatePrice();
                        }
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public void applyDiscount(OrderItem item, Discount discount){
        itemToDiscount.put(item, discount);
    }
    public void setPaymentAmount(BigDecimal paymentAmount){
        this.paymentAmount = paymentAmount;
    }
    public BigDecimal calculateChange(){
        return paymentAmount.subtract(calculateTotal());
    }
    public Map<OrderItem, Discount> getItemToDiscount(){
        return itemToDiscount;
    }
}

class OrderItem{
    private final Item item;
    private final int qty;
    public OrderItem(Item item, int qty){
        this.item = item;
        this.qty = qty;
    }

    public BigDecimal calculatePrice(){
        return item.getPrice().multiply(new BigDecimal(qty));
    }

    public BigDecimal calculatePriceWithDiscount(Discount discount){
        return discount.calculateDiscountedPrice(this);
    }
}

interface DiscountCriteria {
    boolean isApplicable(Item item);
}

interface DiscountCalculationStrategy {
    BigDecimal calculateDiscountedPrice(BigDecimal price);
}

class Discount {
    private final DiscountCriteria criteria;
    private final DiscountCalculationStrategy strategy;
    private final String name;
    public Discount(DiscountCriteria criteria, DiscountCalculationStrategy strategy, String name) {
        this.criteria = criteria;
        this.strategy = strategy;
        this.name = name;
    }
    public boolean isApplicable(Item item) {
        return criteria.isApplicable(item);
    }
    public BigDecimal calculateDiscountedPrice(OrderItem orderItem) {
        return strategy.calculateDiscountedPrice(orderItem.calculatePrice());
    }
    public String getName() {
        return name;
    }
}

class Receipt {
    String receiptId;
    GroceryOrder order;
    Date issueDate;
    public Receipt(String receiptId, GroceryOrder order) {
        this.receiptId = receiptId;
        this.order = order;
        this.issueDate = new Date();
    }
    public void printReceipt(){
        System.out.println(receiptId);
    }
}

class Checkout {
    private GroceryOrder currentOrder;
    private List<Discount> discounts;
    public Checkout(List<Discount> discounts){
        this.discounts = discounts;
        startNewOrder();
    }
    public void startNewOrder(){
        currentOrder = new GroceryOrder(UUID.randomUUID().toString());
    }

    public BigDecimal processPayment(BigDecimal paymentAmount){
        currentOrder.setPaymentAmount(paymentAmount);
        return currentOrder.calculateChange();
    }

    public void addItemToOrder(Item item, int quantity){
        OrderItem orderItem = new OrderItem(item, quantity);
        currentOrder.addItem(orderItem);

        for(Discount discount : discounts){
            if(discount.isApplicable(item)){
                if(currentOrder.getItemToDiscount().containsKey(orderItem)){
                    Discount existingDiscount = currentOrder.getItemToDiscount().get(orderItem);
                    if(orderItem.calculatePriceWithDiscount(discount).compareTo(orderItem.calculatePriceWithDiscount(existingDiscount)) > 0){
                        currentOrder.applyDiscount(orderItem, discount);
                    }
                }
                else{
                    currentOrder.applyDiscount(orderItem, discount);
                }
            }
        }
    }
    public Receipt getReceipt(){
        return new Receipt(UUID.randomUUID().toString(), currentOrder);
    }

    public BigDecimal getOrderTotal(){
        return currentOrder.calculateTotal();
    }
}

public class GroceryStoreSystem {
    private final Catelogue catelogue;
    private final Inventory inventory;
    private final Checkout checkout;
    private List<Discount> discounts;

    public GroceryStoreSystem() {
        this.discounts = new ArrayList<>();
        this.catelogue = new Catelogue();
        this.inventory = new Inventory();
        this.checkout = new Checkout(discounts);
    }

    public void addOrUpdateItem(Item item){
        catelogue.updateItem(item);
    }

    public void updateInventory(String barcode, int quantity){
        inventory.addStock(barcode, quantity);
    }
    public void addDiscount(Discount discount){
        discounts.add(discount);
    }
    public Item getItemByBarcode(String barcode){
        return catelogue.getItem(barcode);
    }
    public void removeItemFromCatelogue(Item item){
        catelogue.removeItem(item);
    }
}
