import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    private final ArrayList<User> users;
    private Thread acceptor;
    private MainGui gui;
    private ConnectedUsers visUserList;

    public Main(int port) {
        users = new ArrayList<>();
        //create guis
        gui = MainGui.init(this);
        visUserList = ConnectedUsers.init(this);
        //accept new clients
        acceptor = new Thread(() -> connectionAcceptor(port));
        acceptor.start();
    }

    public static void main(String[] args) {
        //create a new main object on default or specified port
        if (args.length > 0) {
            new Main(Integer.parseInt(args[0]));
        } else {
            //ask user for port number
            int port;
            do {
                try {
                    port = Integer.parseInt(JOptionPane.showInputDialog("Enter port here: "));
                    break;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Please enter a correct port number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } while (true);
            new Main(port);
        }
    }

    private  void connectionAcceptor(int port) {
        ServerSocket soc = null;
        Socket s = null;
        //create server socket
        try {
            soc = new ServerSocket(port);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Can't use this port.\nExiting", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }


        while (!Thread.currentThread().isInterrupted()) {
            //init Connection
            try {
                s = soc.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //create user
            new User(s, this);
            System.out.println("user added");
        }
    }

    public void addUser(User u) {
        synchronized (users) {
            users.add(u);
            visUserList.addNewUser(u);
        }
    }

    public User accessUser(int index) {
        synchronized (users) {
            return users.get(index);
        }
    }

    public int getAmountOfUsers() {
        synchronized (users) {
            return users.size();
        }
    }

    public void sendMessageToAll(String message) {
        synchronized (users) {
            for (User u : users) {
                u.sendMessage(message);
            }
        }
        gui.addMessage(message);
    }

    public void disconnectUser(int index) {
        synchronized (users) {
            if (index < users.size() && index >= 0) {
                users.get(index).sendMessage("disconnect!");
                sendMessageToAll(users.get(index).getName() + " left the server!");
                users.get(index).close();
                users.remove(index);
                visUserList.removeUser(index);
            }
        }
    }

    public void disconnectAll() {
        for (int index = 0; index < users.size(); index++) {
            disconnectUser(index);
        }
    }

    public void disconnectUser(User user) {
        int index;
        synchronized (users) {
            index = users.indexOf(user);
        }
        if (index >= 0) {
            disconnectUser(index);
        }
    }

    public void exit(){
        disconnectAll();
        acceptor.interrupt();
        visUserList.close();
        gui.close();
        System.exit(0);
    }
}
