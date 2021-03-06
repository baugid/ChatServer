import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    private final ArrayList<User> users;
    private Thread acceptor;
    private MainGui gui;
    private ConnectedUsers visUserList;
    private int cores;
    private ScheduledExecutorService msgReader;

    public Main(int port, int coreCount) {
        users = new ArrayList<>();
        //create guis
        gui = MainGui.init(this);
        visUserList = ConnectedUsers.init(this);

        cores = coreCount;
        msgReader = Executors.newScheduledThreadPool(1);
        msgReader.scheduleAtFixedRate(this::checkUserMessages, 0, 500, TimeUnit.MILLISECONDS);
        //accept new clients
        acceptor = new Thread(() -> connectionAcceptor(port));
        acceptor.setDaemon(true);
        acceptor.start();
    }

    private void checkUserMessages() {
        ExecutorService checker = Executors.newFixedThreadPool(cores);
        synchronized (users) {
            for (User u : users) {
                checker.submit(u);
            }
        }
        //wait till all tasks are finished
        checker.shutdown();
        try {
            checker.awaitTermination(100, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port;
        int coreCount = 6;
        //create a new main object on default or specified port
        switch (args.length) {
            case 1:
                port = Integer.parseInt(args[0]);
                break;

            case 0:
                port = getUserPort();
                break;

            default:
                port = Integer.parseInt(args[0]);
                coreCount = Integer.parseInt(args[1]);
                break;
        }
        new Main(port, coreCount);
    }

    private static int getUserPort() {
        //ask user for port number
        do {
            try {
                return Integer.parseInt(JOptionPane.showInputDialog("Enter port here: "));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Please enter a correct port number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } while (true);
    }

    private void connectionAcceptor(int port) {
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
            synchronized (users) {
                addUser(new User(s, this));
            }
        }
        try {
            soc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUser(User u) {
        synchronized (users) {
            users.add(u);
            String name = u.getName();
            if (name == null || name.equals("")) {
                visUserList.addNewUser(u.getIP() + ":" + u.getPort());
            } else {
                visUserList.addNewUser(name);
            }
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
        gui.addMessage(message);
        synchronized (users) {
            for (User u : users) {
                if (u.getName() != null && !u.getName().equals("")) {
                    u.sendMessage(message);
                }
            }
        }
    }

    public void disconnectUser(int index) {
        User u;
        String name;
        synchronized (users) {
            if (index < users.size() && index >= 0) {
                u = users.remove(index);
                visUserList.removeUser(index);
                name = u.getName();
            }
            else{
                return;
            }
        }
        if (name != null && !name.equals("")) {
            sendMessageToAll(name + " left the server!");
        }
            u.sendMessage("disconnect!");
            u.close();
        }

    private void disconnectAll() {
        int userCount;
        ExecutorService remover = Executors.newFixedThreadPool(cores);
        synchronized (users) {
            userCount = users.size();
        }
        for (int i = 0; i < userCount; i++) {
            final int iFinal = i;
            remover.submit(() -> disconnectUser(iFinal));
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

    public void exit() {
        acceptor.interrupt();
        visUserList.close();
        gui.close();
        msgReader.shutdown();
        try {
            msgReader.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        disconnectAll();
        System.exit(0);
    }

    public void correctShownUserName(User u) {
        synchronized (users) {
            visUserList.updateUser(u.getName(), users.indexOf(u));
        }
    }
}
