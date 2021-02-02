
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    private final Server server;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();

    ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
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
       this.outputStream = clientSocket.getOutputStream();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null){
            String[] tokens = StringUtils.split(line);
            if(tokens != null && tokens.length > 0){ 
                String cmd = tokens[0];
                if("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)){
                    handleLogoff();
                    break;
                }else if("login".equalsIgnoreCase(cmd)){
                    handleLogin(outputStream, tokens);
                }else if ("msg".equalsIgnoreCase(cmd)){
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                }else if ("join".equalsIgnoreCase(cmd)){
                    handleJoin(tokens);
                }else if("leave".equalsIgnoreCase(cmd)){
                    handleLeave(tokens);
                }else{
                    String msg = "Unknown " + line + "\n";
                    outputStream.write(msg.getBytes());
                }
                
            }
        }
        clientSocket.close();
    }    
    
    public String getLogin(){
        return login;
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
                
                
                List<ServerWorker> workerList = server.getWorkerList();
                for(ServerWorker worker : workerList){
                    if (worker.getLogin() != null){
                        if (!login.equals(worker.getLogin())){
                            String msg2 = "User <" + worker.getLogin() + "> is online.\n";
                            send(msg2);
                        }
                    }
                   
                }
                
                String onlineMsg = "User <" + login + "> is online.\n"; 
                
                for(ServerWorker worker : workerList){
                    if (!login.equals(worker.getLogin())){
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "Error Login\n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String msg) throws IOException {
        if (login != null){
            outputStream.write(msg.getBytes());
        }
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();
        String onlineMsg = "User <" + login + "> has logged out.\n"; 
                
        for(ServerWorker worker : workerList){
            if (!login.equals(worker.getLogin())){
                worker.send(onlineMsg);
                }
            }
        clientSocket.close();
    }

    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String msg = tokens[2];
        
        boolean isTopic ;
        isTopic = sendTo.charAt(0) == '#';
        
        
        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList){
            if (isTopic){
                if (worker.MemberOfTopic(sendTo)){
                    String outMsg = "msg to group " + sendTo + " from: <" + login + ">:  " + msg + "\n";
                    worker.send(outMsg);
                }
            }else{
                
            
                if(sendTo.equalsIgnoreCase(worker.getLogin())){
                    String outMsg = "msg from: <" + login + ">:  " + msg + "\n";
                    worker.send(outMsg);
                }
            }
        }
    }
    
    public boolean MemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if(tokens.length > 1){
            String topic = tokens[1];
            topicSet.add(topic);
            
        }
    }

    private void handleLeave(String[] tokens) {
         if(tokens.length > 1){
            String topic = tokens[1];
            topicSet.remove(topic);
            
        }
    }
}
