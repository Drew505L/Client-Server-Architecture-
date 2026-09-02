package ClientServerArchitureProject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;

public class Client {

    private String username;
    private DBOperations dbc = new DBOperations(); // deals with DB-side stuff
    private EmailService es = new EmailService();  // handles password recovery emails
    //NICO EDIT
    // added socket connection support so that connection is actaully accurate and isnt just reading DB stats
    private Socket socket;
    private DataOutputStream out;
    private BufferedReader in;

    // constructor
    public Client() {}

    public static void main(String[] args) {
        Client client = new Client();
        client.connectToServer("127.0.0.1", 8000);
    }

    public void disconnect ()
    {
        String text = "disconnect";
        try {
            // -- the server only receives String objects that are
            //    terminated with a newline "\n"

            // -- send a special message to let the server know
            //    that this client is shutting down
            text += "\n";
            out.writeBytes(text);
            out.flush();
            // -- close the peer to peer socket
            socket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }

    }

    // let  client connect to a real server socket
    public boolean connectToServer(String ip, int port) {
        try {
            // try to open a socket connection
            socket = new Socket(ip, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // send a handshake to tell the server we’re here
            out.writeBytes("hello\n");
            out.flush();

            // read response
            String response = in.readLine();
            System.out.println("Server response: " + response);

            return response != null && response.startsWith("CONNECTED");
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    private boolean checkPassFormat(String password){
        return RegexEmail.validPassword(password);
    }

    private boolean checkEmail(String email) throws SQLException {
        return RegexEmail.validEmailAddress(email);
    }

    public String login(String username, String password) throws SQLException {
        return getResult(dbc.login(username, password));
    }

    public User createUser(String username){
        return new User(username);
    }

    public String getResult(int loginResult){
        switch (loginResult) {
            case 1: return "Locked Out";
            case 2: return "UR IN";
            case 3: return "Wrong Password";
            case 4: return "Non-existing Username";
            case 5: return "Pre-existing Username";
            case 6: return "Account created successfully!";
            default: return "";
        }
    }

    public String register(String username, String password, String email) throws SQLException {
        if (!checkEmail(email)) return "Email Wrong Format";
        if (!checkPassFormat(password)) return "Password Wrong Format";
        return getResult(dbc.newUserRegistration(username, password, email));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean recoverPassword(String username) throws SQLException {
        if(dbc.checkUsername(username)){
            es.recoverPass(username);
            return true;
        }
        return false;
    }
}