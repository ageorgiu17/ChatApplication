
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ServerWorker extends Thread{

    private final Socket clientSocket;
    private String login = null;

    ServerWorker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null){
            String[] tokens = StringUtils.split(line);
            if(tokens != null && tokens.length > 0){ 
                String cmd = tokens[0];
                if("quit".equalsIgnoreCase(cmd)){
                    break;
                }else if("login".equalsIgnoreCase(cmd)){
                    handleLogin(outputStream, tokens);
                }else{
                    String msg = "Unknown " + line + "\n";
                    outputStream.write(msg.getBytes());
                }
                
            }
        }
        clientSocket.close();
    }    

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException{
        if (tokens.length == 3){
            String login = tokens[1];
            String password = tokens[2];
            
            if ((login.equals("guest") && password.equals("guest")) || login.equals("user") && password.equals("user")){
                String msg = "Login Succescful\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User has succesfully loged in" + login);
            } else {
                String msg = "Error Login\n";
                outputStream.write(msg.getBytes());
            }
        }
    }
}
