
package ClientServerArchitureProject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.*;

public class ServerGUI extends JFrame {

    private static final long serialVersionUID = -6167569334213042018L;
    private final int WIDTH = 500;
    private final int HEIGHT = 400;

    private ControlPanelInner controlPanel;
    private Server server = new Server();;

    public ServerGUI() {
        super();
        this.setTitle("Server");
        this.setSize(WIDTH, HEIGHT);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        controlPanel = new ControlPanelInner();
        this.add(controlPanel, BorderLayout.CENTER);

        this.setResizable(false);
        this.setVisible(true);
    }

    public class ControlPanelInner extends JPanel {

        private static final long serialVersionUID = -8776438726683578403L;

        private JButton button;
        private JButton startServer;
        private JTextArea textArea;
        private JScrollPane scrollPane;
        private boolean showingText = false;

        public ControlPanelInner() {
            setLayout(new FlowLayout());

            startServer = new JButton("Start Server");
            button = new JButton("See Server Stats");
            textArea = new JTextArea("This is the server", 15, 25);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setVisible(false);

            scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(scrollPane.getPreferredSize());
            scrollPane.setVisible(false);
            //NICO EDIT
            button.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                // shows the “Users Connected:” line
                                textArea.setText(server.getStats()[0] + "\n"
                                        + server.getStats()[1] + "\n"
                                        + server.getStats()[2] + "\n"
                                        + server.getStats()[3] + "\n"
                                        + server.getStats()[4] + "\n");
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                            showingText = !showingText;
                            scrollPane.setVisible(showingText);
                            button.setText(showingText ? "Back" : "Server");
                            textArea.setVisible(true);
                            revalidate();
                            repaint();
                        }
                    }
            );
            startServer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new Thread(() -> {
                        server.startServer();
                    }).start();
                }
            });
            add(button);
            add(scrollPane);
            add(startServer);
        }
    }

    public static void main(String[] args) {
        new ServerGUI();
        System.out.println("Server Starting");
    }
}