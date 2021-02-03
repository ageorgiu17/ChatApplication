
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

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
    
    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
    
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);
        if(!client.connect()){
            System.err.println("Connection failed!");
        }else {
            System.out.println("Connection succesfull!");
            if(client.login("guest", "guest")){
                System.out.println("Login Succesful!");
            }else {
                System.err.println("Error Login!");
            }
            
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
            return true;
        } else {
            return false;
        }
        
    }
}
