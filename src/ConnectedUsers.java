import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectedUsers {
    private JPanel panel1;
    private JList users;
    private JButton kick;
    private DefaultListModel model;

    private ConnectedUsers(Main main) {
        kick.setEnabled(false);
        model = new DefaultListModel();
        users.setModel(model);
        //kick selected user
        kick.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = users.getSelectedIndex();
                User u = main.accessUser(selected);
                u.sendMessage("disconnect!");
            }
        });
        //enable kick if user is selected
        users.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (users.getSelectedIndex() < 0) {
                    kick.setEnabled(false);
                } else {
                    kick.setEnabled(true);
                }
            }
        });
    }

    public static ConnectedUsers init(Main main) {
        ConnectedUsers cU = new ConnectedUsers(main);
        JFrame frame = new JFrame("connected Users");
        frame.setContentPane(cU.panel1);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return cU;
    }

    //adds users
    public void addNewUser(User u) {
        model.addElement(u.getName());
    }

    //removes users
    public void removeUser(int index) {
        model.remove(index);
    }
}
