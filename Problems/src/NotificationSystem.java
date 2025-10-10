import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

enum Channel{
    SMS, EMAIL, WHATSAPP
}

interface NotificationChannel{
    public void notify(Notification notification);
}

class SMS implements  NotificationChannel{
    @Override
    public void notify(Notification notification){
        System.out.println("SMS: "+notification.getMessage());
    }
}

class Email implements  NotificationChannel{
    @Override
    public void notify(Notification notification){
        System.out.println("Email: "+notification.getMessage());
    }
}

class Whatsapp implements  NotificationChannel{
    @Override
    public void notify(Notification notification){
        System.out.println("Whatsapp: "+notification.getMessage());
    }
}

class NotificationWorker implements Runnable{
    BlockingDeque<Notification>queue;
    NotificationDispatcher dispatcher;

    public NotificationWorker(BlockingDeque<Notification>queue, NotificationDispatcher dispatcher){
        this.queue = queue;
        this.dispatcher = dispatcher;
    }
    @Override
    public void run() {
        while(true){
            try {
                Notification notification = queue.take();
                dispatcher.dispatch(notification);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class NotificationDispatcher{
    HashMap<Channel, NotificationChannel> channelMapping = new HashMap<>();
    public NotificationDispatcher(){
        channelMapping.put(Channel.SMS, new SMS());
        channelMapping.put(Channel.EMAIL, new Email());
        channelMapping.put(Channel.WHATSAPP, new Whatsapp());
    }
    public void dispatch(Notification notification){
        NotificationChannel channel = channelMapping.get(notification.getChannel());
        channel.notify(notification);
    }
}

class Notification{
    Channel channel;
    String message;

    public Notification(Channel channel, String message){
        this.channel = channel;
        this.message = message;
    }
    public Channel getChannel(){
        return channel;
    }
    public String getMessage(){
        return message;
    }
}

class NotificationHandler{
    List<Channel>channels = new ArrayList<>();
    BlockingDeque<Notification>queue;
    public NotificationHandler(BlockingDeque<Notification>queue){
        this.queue = queue;
    }
    public void subscribe(Channel channel){
        channels.add(channel);
    }
    public void sendNotification(String message){
        for(Channel channel : channels){
            queue.add(new Notification(channel, message));
        }
    }
}

public class NotificationSystem {
    public static void main(String[] args){
        int numOfWorkers = 5;
        BlockingDeque<Notification>queue = new LinkedBlockingDeque<>();
        NotificationDispatcher dispatcher = new NotificationDispatcher();
        for(int i=0; i<numOfWorkers; i++){
            new Thread(new NotificationWorker(queue, dispatcher)).start();
        }

        NotificationHandler event = new NotificationHandler(queue);
        event.subscribe(Channel.SMS);
        event.subscribe(Channel.EMAIL);
        event.subscribe(Channel.WHATSAPP);

        event.sendNotification("Notifications are working!");
    }
}
