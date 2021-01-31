/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andreig
 */

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.lang.Thread;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {
    public static void main(String[] args){
        int port = 8818;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true){
                System.out.println("About to accept the connection....");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                Thread t;
                t = new Thread(){
                    @Override
                    public void run() {
                        try {
                            handleClientSocket(clientSocket);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private static void handleClientSocket(Socket clientSocket) throws IOException, InterruptedException {
        OutputStream outputStream = clientSocket.getOutputStream();
        for(int i = 0; i < 10; i ++){
            
            outputStream.write(("Time now is " + new Date() + "\n").getBytes());
            Thread.sleep(1000);
        }
        clientSocket.close();
    }
    
}
