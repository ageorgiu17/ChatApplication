
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andreig
 */
public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferIn;
    
    private ArrayList<UserStatusListener> userStatusListener = new ArrayList<>();
    private ArrayList<MessageListener> messageListener = new ArrayList<>();
    
    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
    
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener(){
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
    });
        
        client.addMessageListener(new MessageListener(){
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from: "+ fromLogin + "==> " + msgBody);
            }
            
        });
    
        if(!client.connect()){
            System.err.println("Connection failed!");
        }else {
            System.out.println("Connection succesfull!");
            if(client.login("guest", "guest")){
                System.out.println("Login Succesful!");
                
                client.msg("Jim", "Hello");
            }else {
                System.err.println("Error Login!");
            }
            
            //client.logoff();
            
        }
    }

    private boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("The client port is: "+ socket.getLocalPort() + "\n");
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());
        
        String response = bufferIn.readLine();
        System.out.println("Response Line: " + response);
        
        if("Login Succescful".equalsIgnoreCase(response)){
            startMessageReader();
            return true;
        } else {
            return false;
        }
        
    }
    
    public void addUserStatusListener(UserStatusListener listener){
        userStatusListener.add(listener);
    }
    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListener.remove(listener);
    }

    private void startMessageReader() {
        Thread t = new Thread(){
            @Override
            public void run(){
                readMessageLoop();
            }

            
        };
        t.start();
    }
    
    private void readMessageLoop(){
        try {
            String line;
            while((line = bufferIn.readLine()) != null){
                String[] tokens = StringUtils.split(line);
                if(tokens != null && tokens.length > 0){
                    String cmd = tokens[0];
                    if("online".equalsIgnoreCase(cmd)){
                        handleOnlineTokens(tokens);
                    }else if("offline".equalsIgnoreCase(cmd)){
                        handleOfflineTokens(tokens);
                    }else if("msg".equalsIgnoreCase(cmd)){
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            try{
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void handleOnlineTokens(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener: userStatusListener){
            listener.online(login);
        }
    }

    private void handleOfflineTokens(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener: userStatusListener){
            listener.offline(login);
        }
    }

    private void logoff() throws IOException {
        String cmd = "logoff!\n";
        serverOut.write(cmd.getBytes());
    }

    private void msg(String sendTo, String msgBody) throws IOException {
       String cmd = "login <" + sendTo + ">: " + msgBody + "\n";
       serverOut.write(cmd.getBytes());
    }
    
    public void addMessageListener(MessageListener listener){
        messageListener.add(listener);
    }
    
    public void removeMessageListener(MessageListener listener){
        messageListener.remove(listener);
    }

    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];
        
        for(MessageListener listener : messageListener){
            listener.onMessage(login, msgBody);
        }
    }
}
