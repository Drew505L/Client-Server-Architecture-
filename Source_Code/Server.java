package ClientServerArchitureProject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

//NICO EDIT
public class Server {
    private static int connectedClients = 0;  // private int to track connected clients

    private ArrayList<ConnectionThread> clientThreads = new ArrayList<>();
    private DBOperations dbc = new DBOperations();  // still gives us DB stats

    private ServerSocket serverSocket;
    private final int PORT = 8000; // default port to listen on

    public Server() {

    }

    public void startServer(){
        try {
            //server now "listens" for real client connections
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running and listening on port " + PORT);

            while (true) {
                // accept new client
                Socket clientSocket = serverSocket.accept();
                ConnectionThread ct = new ConnectionThread(clientThreads.size(), clientSocket, this);
                clientThreads.add(ct);
                ct.start();
                System.out.println("Client connected. Total clients: " + connectedClients);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    //NICO EDIT
    //new methods to count clients
    public static synchronized void incrementConnections() {
        connectedClients++;
    }

    public static synchronized void decrementConnections() {
        if (connectedClients > 0) connectedClients--;
    }

    public int getConnectedClients() {
        return connectedClients;
    }

    public String[] getStats() throws SQLException {
        String[] results = dbc.getStats();
        results[4] = "Users Connected: " + getConnectedClients();  // used in ServerGUI
        return results;
    }

}