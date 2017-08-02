import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {
    private ArrayList<User> users;
    private Thread acceptor;
    private MainGui gui;
    private ConnectedUsers visUserList;

    public Main(int port) {
        users = new ArrayList<>();
        //create guis
        gui = MainGui.init(this);
        visUserList = ConnectedUsers.init(this);
        //accept new clients
        acceptor = new Thread(new Runnable() {
            @Override
            public void run() {
                connectionAcceptor(port);
            }
        });
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

    public void connectionAcceptor(int port) {
        ServerSocket soc = null;
        Socket s = null;
        BufferedReader sockListener = null;
        PrintStream sockPrinter = null;
        String name = "";
        boolean error = false;
        //create server socket
        try {
            soc = new ServerSocket(port);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Can't use this port.\nExiting", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }


        while (true) {
            //init Connection
            try {
                s = soc.accept();
                sockListener = new BufferedReader(new InputStreamReader(s.getInputStream()));
                sockPrinter = new PrintStream(s.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //get name and verify name
            do {
                error = false;

                try {
                    name = sockListener.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!name.matches("(\\d|\\w)+")) {
                    error = true;
                    sockPrinter.println("otherName");
                    continue;
                }

                for (User u : users) {
                    if (u.getName().equals(name)) {
                        error = true;
                        sockPrinter.println("otherName");
                        break;
                    }
                }
            } while (error);
            //Inform user that he got accepted
            sockPrinter.println("accepted!");
            //send joined message
            sendMessageToAll(name + " joined the server!");
            //add user
            addUser(new User(s, name, this));
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

    public void removeUser(User u) {
        synchronized (users) {
            visUserList.removeUser(users.indexOf(u));
            users.remove(u);
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
}
