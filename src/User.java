import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Gideon on 02.08.2017.
 */
public class User implements Runnable {
    private Socket soc = null;
    private String name;
    private BufferedReader in = null;
    private PrintStream out = null;
    private Main receiver;
    private Thread ownThread;
    private volatile boolean isOpen = true;

    public User(Socket soc, String name, Main receiver) {
        this.soc = soc;
        this.name = name;
        this.receiver = receiver;
        //open streams
        try {
            in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            out = new PrintStream(soc.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //start accepting messages
        ownThread = new Thread(this);
        ownThread.start();
    }

    @Override
    public void run() {
        String message = "";
        do {
            //read message
            try {
                message = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //close connection if requested
            if (message.equals("closing!")) {
                close();
                receiver.sendMessageToAll(name + " left the server!");
                receiver.removeUser(this);
                return;
            }
            //ignore messages that don't fit in the format [name]:text
            if (message.matches("^\\[" + name + "\\]:.*$")) {
                receiver.sendMessageToAll(message);
            }
        } while (isOpen);
    }

    //send a single message
    public void sendMessage(String message) {
        out.println(message);
    }

    public String getName() {
        return name;
    }

    //close streams
    public void close() {
        isOpen = false;
        try {
            soc.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
    }
}
