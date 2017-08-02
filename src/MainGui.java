import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Gideon on 02.08.2017.
 */
public class MainGui {

    private JTextArea output;
    private JPanel panel;
    private JTextField input;
    private JButton send;

    private MainGui(Main main) {
        //send message
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!input.getText().equals("")) {
                    main.sendMessageToAll("[SERVER]:" + input.getText());
                    input.setText("");
                }
            }
        });
    }

    public static MainGui init(Main main) {
        MainGui mG = new MainGui(main);
        JFrame frame = new JFrame("Chat");
        frame.setContentPane(mG.panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //close clients on close
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                main.sendMessageToAll("disconnect!");
                System.exit(0);
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return mG;
    }

    //add incoming messages
    public void addMessage(String message) {
        output.setText(output.getText() + message + "\n");
    }
}
