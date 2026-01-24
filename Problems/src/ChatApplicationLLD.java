import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

class ChatUser{
    String username;
    public ChatUser(String username){
        this.username = username;
    }
}

class Mediator{
    HashMap<String, ChatUser> chatUsers = new HashMap<>();
    HashMap<ChatUser, HashSet<ChatUser>> mutedUsers = new HashMap<>();

    public Mediator(){}

    public ChatUser getChatUser(String username){
        if(chatUsers.containsKey(username)){
            return chatUsers.get(username);
        }
        System.out.println("#ERROR: "+username+" User Is Not Registered, Or Does not Exist");
        return null;
    }

    public boolean isMuted(ChatUser user1, ChatUser user2){
        return (mutedUsers.containsKey(user1) &&  mutedUsers.get(user1).contains(user2));
    }

    public void registerUser(ChatUser user){
        chatUsers.put(user.username,user);
        System.out.println(user.username+" Registered Successfully!");
    }

    public void muteUser(String username1, String username2){
        ChatUser chatUser1 = getChatUser(username1);
        ChatUser chatUser2 = getChatUser(username2);

        if(chatUser1 == null || chatUser2 == null){ return;}

        mutedUsers.computeIfAbsent(chatUser1, k -> new HashSet<>()).add(chatUser2);
        mutedUsers.computeIfAbsent(chatUser2, k -> new HashSet<>()).add(chatUser1);

        System.out.println("Successfully muted "+username1+" "+username2);
    }

    public void SendToAll(String username, String message){
        ChatUser chatUser1 = getChatUser(username);
        if(chatUser1 == null){ return;}
        for(ChatUser user : chatUsers.values()){
            if(user == chatUser1 || isMuted(chatUser1, user)){continue;}
            System.out.println("Message For: "+user.username+ " From: "+username + " : "+message);
        }
    }

    public void SendTo(String username1, String username2, String message){
        ChatUser chatUser1 = getChatUser(username1);
        ChatUser chatUser2 = getChatUser(username2);
        if(chatUser1 == null || chatUser2 == null){ return; }

        if(mutedUsers.containsKey(chatUser1) && mutedUsers.get(chatUser1).contains(chatUser2)){
            System.out.println("ERROR: Can't Send message between "+username1+" "+username2+ " As they are muted");
            return;
        }

        System.out.println("Message For: "+username2+ "From: "+username1+ " : "+message);
    }

    public void unmuteUser(String username1, String username2){
        ChatUser chatUser1 = getChatUser(username1);
        ChatUser chatUser2 = getChatUser(username2);
        if(chatUser1 == null || chatUser2 == null){ return; }

        if(mutedUsers.containsKey(chatUser1)){
            mutedUsers.get(chatUser1).remove(chatUser2);
            mutedUsers.get(chatUser2).remove(chatUser1);
            if(mutedUsers.get(chatUser1).isEmpty()){
                mutedUsers.remove(chatUser1);
            }
            if(mutedUsers.get(chatUser2).isEmpty()){
                mutedUsers.remove(chatUser2);
            }
            System.out.println("Successfully unmuted "+username1+" "+username2);
        }
    }


}

class ChatRoomManager{
    private HashMap<String, Mediator> chatrooms;
    private static ChatRoomManager instance = new ChatRoomManager();
    public ChatRoomManager(){
        chatrooms = new HashMap<>();
    }
    public static ChatRoomManager getInstance(){
        return instance;
    }

    public void createChatRoom(String chaatRoomName){
        Mediator mediator = new Mediator();
        chatrooms.put(chaatRoomName, mediator);
    }

    public Mediator getChatRoom(String chaatRoomName){
        return chatrooms.get(chaatRoomName);
    }
}

class ChatService{
    private static final ChatService instance = new ChatService();
    private ChatRoomManager chatRoomManager = ChatRoomManager.getInstance();

    private ChatService(){
    }
    public static ChatService getInstance(){
        return instance;
    }

    public void createChatRoom(String chaatRoomName){
        chatRoomManager.createChatRoom(chaatRoomName);
    }



    public void registerUser(String chatRoom, ChatUser user){
        Mediator mediator = chatRoomManager.getChatRoom(chatRoom);
        mediator.registerUser(user);
    }
    public void muteUser(String chatRoom, String username1, String username2){
        Mediator mediator = chatRoomManager.getChatRoom(chatRoom);
        mediator.muteUser(username1, username2);
    }
    public void SendToAll(String chatRoom, String username, String message){
        Mediator mediator = chatRoomManager.getChatRoom(chatRoom);
        mediator.SendToAll(username,message);
    }
    public void SendTo(String chatRoom, String username1, String username2, String message){
        Mediator mediator = chatRoomManager.getChatRoom(chatRoom);
        mediator.SendTo(username1,username2,message);
    }
    public void unmuteUser(String chatRoom, String username1, String username2){
        Mediator mediator = chatRoomManager.getChatRoom(chatRoom);
        mediator.unmuteUser(username1, username2);
    }
}

class ChatController{
    private static final ChatController instance = new ChatController();
    private ChatService chatService;
    public ChatController(){
        chatService = ChatService.getInstance();
    }
    public static ChatController getInstance(){
        return instance;
    }
    public void createChatRoom(String chaatRoomName){
        chatService.createChatRoom(chaatRoomName);
    }
    public void registerUser(String chatRoom, ChatUser user){
        chatService.registerUser(chatRoom, user);
    }
    public void muteUser(String chatRoom, String username1, String username2){
        chatService.muteUser(chatRoom, username1, username2);
    }
    public void SendToAll(String chatRoom, String username, String message){
        chatService.SendToAll(chatRoom, username,message);
    }
    public void SendTo(String chatRoom, String username1, String username2, String message){
        chatService.SendTo(chatRoom, username1,username2,message);
    }
    public void unmuteUser(String chatRoom, String username1, String username2){
        chatService.unmuteUser(chatRoom, username1, username2);
    }


}

public class ChatApplicationLLD {
    public static void main(String[] args) {
        ChatController controller = ChatController.getInstance();


        controller.createChatRoom("ChatRoom1");

        ChatUser user1 = new ChatUser("devanshabrol");
        ChatUser user2 = new ChatUser("xazzwizzy");
        ChatUser user3 = new ChatUser("bandit");

        controller.registerUser("ChatRoom1", user1);
        controller.registerUser("ChatRoom1", user2);
        controller.registerUser("ChatRoom1", user3);

        controller.SendToAll("ChatRoom1", "devanshabrol","Hi Guys");
        controller.SendToAll("ChatRoom1", "xazzwizzy", "Hi Everyone!!");
        controller.SendTo("ChatRoom1", "bandit", "xazzwizzy", "Fuck You");

        controller.muteUser("ChatRoom1", "xazzwizzy", "bandit");

        controller.SendToAll("ChatRoom1", "xazzwizzy", "Hi everyone!");

        controller.SendTo("ChatRoom1", "bandit", "xazzwizzy", "I'm sorry bro!");

        controller.unmuteUser("ChatRoom1", "xazzwizzy", "bandit");

        controller.SendTo("ChatRoom1", "devanshabrol", "jaadu", "Hi Bro!");

        controller.SendToAll("ChatRoom1", "devanshabrol", "Good Bye!");
    }
}
