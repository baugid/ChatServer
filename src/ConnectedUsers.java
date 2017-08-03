import javax.swing.*;

public class ConnectedUsers {
    private JPanel panel1;
    private JList<String> users;
    private JButton kick;
    private DefaultListModel<String> model;

    private ConnectedUsers(Main main) {
        kick.setEnabled(false);
        model = new DefaultListModel<>();
        users.setModel(model);
        //kick selected user
        kick.addActionListener((e) -> main.disconnectUser(users.getSelectedIndex()));
        //enable kick if user is selected
        users.addListSelectionListener(e -> kick.setEnabled(users.getSelectedIndex()>=0));
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

    public void close() {
        panel1.getRootPane().setVisible(false);
    }
}
