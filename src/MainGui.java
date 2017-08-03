import javax.swing.*;
import java.awt.event.*;

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
                if (input.getText() != null && !input.getText().equals("")) {
                    main.sendMessageToAll("[SERVER]:" + input.getText());
                    input.setText("");
                }
            }
        });
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && input.getText() != null && !input.getText().equals("")) {
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
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        //close clients on close
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                main.exit();
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

    public void close() {
        panel.getRootPane().setVisible(false);
    }
}
