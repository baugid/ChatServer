import java.io.*;
import java.net.Socket;

public class User implements Runnable {
    private Socket soc = null;
    private String name;
    private BufferedReader in = null;
    private PrintStream out = null;
    private Main receiver;
    private Thread ownThread;
    private volatile boolean isOpen = true;

    public User(Socket soc, Main receiver) {
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
        fetchName();
        //Inform user that he got accepted
        out.println("accepted!");
        //send joined message
        receiver.sendMessageToAll(name + " joined the server!");
        receiver.addUser(this);
        String message = "";
        do {
            //read message
            try {
                message = in.readLine();
            } catch (IOException e) {
                receiver.disconnectUser(this);
                return;
            }
            //close connection if requested
            if (message.equals("closing!")) {
                receiver.disconnectUser(this);
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

    private void fetchName(){
        boolean error;
//get name and verify name
        do {
            error = false;

            try {
                name = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!name.matches("(\\d|\\w)+")) {
                error = true;
                out.println("otherName");
                continue;
            }

            for (int i=0;i<receiver.getAmountOfUsers();i++) {
                if (receiver.accessUser(i).getName().equals(name)) {
                    error = true;
                    out.println("otherName");
                    break;
                }
            }
        } while (error);
    }
}
