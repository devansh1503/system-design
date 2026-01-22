import java.util.*;

enum GatewayType{
    PAYTM, RAZORPAY, GOOGLE_PAY
}
class PaymentRequest{
    String sender;
    String receiver;
    double amount;
    String currency;

    public PaymentRequest(String sender, String receiver, double amount, String currency) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.currency = currency;
    }
}

abstract class BankingHandler{
    public abstract boolean processPayment(double amount);
}
class PaytmBank extends BankingHandler{
    Random rand = new Random();
    public PaytmBank() {}
    @Override
    public boolean processPayment(double amount) {
        int r = rand.nextInt(100);
        return r >50;
    }
}
class RazorpayBank extends BankingHandler{
    Random rand = new Random();
    public RazorpayBank() {}
    @Override
    public boolean processPayment(double amount) {
        int r = rand.nextInt(100);
        return r >70;
    }
}
class GooglePayBank extends BankingHandler{
    Random rand = new Random();
    public GooglePayBank() {}
    @Override
    public boolean processPayment(double amount) {
        int r = rand.nextInt(100);
        return r >90;
    }
}

abstract class PaymentGateway{
    BankingHandler bank;
    public PaymentGateway(){}

    public boolean processPayment(PaymentRequest paymentRequest){
        if(!validate(paymentRequest)){
            System.out.println("Invalid request");
            return false;
        }
        if(!initialize(paymentRequest)){
            System.out.println("Initialization Failed");
            return false;
        }
        if(!confirm(paymentRequest)){
            System.out.println("Confirmation Failed");
            return false;
        }
        return true;
    }

    public abstract boolean validate(PaymentRequest paymentRequest);
    public abstract boolean initialize(PaymentRequest paymentRequest);
    public abstract boolean confirm(PaymentRequest paymentRequest);
}

class PaytmGateway extends PaymentGateway{
    PaytmBank bank;
    public PaytmGateway(){
        this.bank = new PaytmBank();
    }
    @Override
    public boolean validate(PaymentRequest req){
        if(req.amount < 0){
            System.out.println("Invalid amount");
            return false;
        }
        return true;
    }

    @Override
    public boolean initialize(PaymentRequest req){
        return bank.processPayment(req.amount);
    }

    @Override
    public boolean confirm(PaymentRequest req){
        System.out.println("Payment Confirmed For- "+req.sender+" "+req.receiver+" "+req.amount+" "+req.currency);
        return true;
    }
}
class RazorpayGateway extends PaymentGateway{
    RazorpayBank bank;
    public RazorpayGateway(){
        this.bank = new RazorpayBank();
    }
    @Override
    public boolean validate(PaymentRequest req){
        if(req.amount < 100){
            System.out.println("Invalid amount");
            return false;
        }
        return true;
    }

    @Override
    public boolean initialize(PaymentRequest req){
        return bank.processPayment(req.amount);
    }

    @Override
    public boolean confirm(PaymentRequest req){
        System.out.println("Payment Confirmed For- "+req.sender+" "+req.receiver+" "+req.amount+" "+req.currency);
        return true;
    }
}
class GooglePayGateway extends PaymentGateway{
    GooglePayBank bank;
    public GooglePayGateway(){
        this.bank = new GooglePayBank();
    }
    @Override
    public boolean validate(PaymentRequest req){
        if(req.amount < 200){
            System.out.println("Invalid amount");
            return false;
        }
        return true;
    }

    @Override
    public boolean initialize(PaymentRequest req){
        return bank.processPayment(req.amount);
    }

    @Override
    public boolean confirm(PaymentRequest req){
        System.out.println("Payment Confirmed For- "+req.sender+" "+req.receiver+" "+req.amount+" "+req.currency);
        return true;
    }
}

class PaymentGatewayProxy extends PaymentGateway{
    BankingHandler bank;
    PaymentGateway actualGateway;
    int retries;

    public PaymentGatewayProxy(int retries, PaymentGateway actualGateway){
        this.retries = retries;
        this.actualGateway = actualGateway;
    }

    public boolean processPayment(PaymentRequest paymentRequest){
        boolean result = false;
        for(int i = 0; i < retries; i++){
            if(actualGateway.processPayment(paymentRequest)){
                result = true;
                break;
            }
        }
        return result;
    }
    @Override
    public boolean validate(PaymentRequest paymentRequest){
        return actualGateway.validate(paymentRequest);
    }
    @Override
    public boolean initialize(PaymentRequest paymentRequest){
        return actualGateway.initialize(paymentRequest);
    }
    @Override
    public boolean confirm(PaymentRequest paymentRequest){
        return actualGateway.confirm(paymentRequest);
    }
}

class GatewayFactory{
    private static final GatewayFactory instance = new GatewayFactory();
    public GatewayFactory(){}

    public GatewayFactory getInstance(){
        return instance;
    }

    public PaymentGateway getPaymentGateway(GatewayType type){
        switch(type){
            case PAYTM: return new PaymentGatewayProxy(3, new PaytmGateway());
            case RAZORPAY: return new PaymentGatewayProxy(10, new RazorpayGateway());
            case GOOGLE_PAY: return new PaymentGatewayProxy(20, new GooglePayGateway());
            default: return null;
        }
    }
}

class PaymentService{
    private static final PaymentService instance = new PaymentService();
    PaymentGateway gateway;

    public PaymentService(){}
    public PaymentService getInstance(){
        return instance;
    }
    public void setGateway(PaymentGateway gateway){
        this.gateway = gateway;
    }

    public boolean processPayment(PaymentRequest request){
        if(gateway == null){
            System.out.println("Gateway is initialized");
            return false;
        }
        return gateway.processPayment(request);
    }
}

class PaymentController{
    private static final PaymentController instance = new PaymentController();
    public PaymentController(){}
    public PaymentController getInstance(){
        return instance;
    }
    public boolean processPayment(GatewayType type, PaymentRequest req){
        PaymentGateway gateway = new GatewayFactory().getInstance().getPaymentGateway(type);
        PaymentService service = new PaymentService().getInstance();
        service.setGateway(gateway);
        return service.processPayment(req);
    }
}
public class PaymentGatewayLLD {
    public static void main(String[]args){
        PaymentRequest request = new PaymentRequest(
                "Devansh",
                "Kamal Kishore",
                25000,
                "INR"
        );

        boolean result = new PaymentController().getInstance().processPayment(GatewayType.PAYTM, request);

        System.out.println(result ? "Payment with PAYTM Success" : "Payment with PAYTM Failed");

        result = new PaymentController().getInstance().processPayment(GatewayType.RAZORPAY, request);

        System.out.println(result ? "Payment with Razorpay Success" : "Payment with Razorpay Failed");

        result = new PaymentController().getInstance().processPayment(GatewayType.GOOGLE_PAY, request);

        System.out.println(result ? "Payment with Google Pay Success" : "Payment with Google Pay Failed");

    }
}
