
import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andreig
 */
public class UserListPane extends JPanel implements UserStatusListener{
    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 8818);
        
        UserListPane userListPane = new UserListPane(client);
        JFrame frame = new JFrame("User List Pane");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        
        frame.getContentPane().add(userListPane, BorderLayout.CENTER);
        frame.setVisible(true);
        
        if(client.connect()){
            try {
                client.login("guest", "guest");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    private final ChatClient client;
    private final DefaultListModel<String> userListModel;
    private final JList<String> userListUI;

    private UserListPane(ChatClient client) {
        this.client = client;
        this.client.addUserStatusListener(this);
        
        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);
    }

    @Override
    public void online(String login) {
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }
}
