import java.io.*;
import java.net.Socket;
import java.util.Optional;

public class User implements Runnable {
    private Socket soc = null;
    private String name = null;
    private BufferedReader in = null;
    private PrintStream out = null;
    private Main receiver;

    public User(Socket soc, Main receiver) {
        this.soc = soc;
        this.receiver = receiver;
        //open streams
        try {
            in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            out = new PrintStream(soc.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            if (name == null) {
                if (fetchName()) {
                    //Inform user that he got accepted
                    out.println("accepted!");
                    //send joined message
                    receiver.sendMessageToAll(name + " joined the server!");
                    receiver.correctShownUserName(this);
                }
            } else {
                String message = "";
                //read message
                while (in.ready()) {
                    message = in.readLine();
                    //close connection if requested
                    if (message.equals("closing!")) {
                        receiver.disconnectUser(this);
                        return;
                    }
                    //ignore messages that don't fit in the format [name]:text
                    if (message.matches("^\\[" + name + "]:.+$") && !message.substring(name.length() + 4).matches("[\\t\\s]*")) {
                        receiver.sendMessageToAll(message);
                    }
                }
            }
        } catch (IOException e) {
            receiver.disconnectUser(this);
        }

    }

    //send a single message
    public void sendMessage(String message) {
        out.println(message);
    }

    public String getName() {
        return name != null ? name : "";
    }

    //close streams
    public void close() {
        try {
            soc.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
    }

    private boolean fetchName() {
        String nameHelper = "";
//get and verify name
        try {
            while (in.ready()) {
                nameHelper = in.readLine();
                if (!nameHelper.matches("(\\d|\\w)+")) {
                    out.println("otherName");
                    return false;
                }

                for (int i = 0; i < receiver.getAmountOfUsers(); i++) {
                    String name = receiver.accessUser(i).getName();
                    if (name != null && !name.equals("") && name.equals(nameHelper)) {
                        out.println("otherName");
                        return false;
                    }
                }
                name = nameHelper;
                return true;
            }
        } catch (IOException e) {
            receiver.disconnectUser(this);
        }
        return false;
    }

    public int getPort() {
        return soc.getPort();
    }

    public String getIP() {
        return soc.getInetAddress().getHostAddress();
    }
}
