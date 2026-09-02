package ClientServerArchitureProject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionThread extends Thread {

    private boolean go;
    private String name;
    private int id;
    private BufferedReader datain;
    private DataOutputStream dataout;
    private Server server;

    public ConnectionThread(int id, Socket socket, Server server) {
        this.server = server;
        this.id = id;
        this.name = "Client-" + id;
        this.go = true;

        try {
            datain = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataout = new DataOutputStream(socket.getOutputStream());

            //increment server’s connection count when this thread starts
            Server.incrementConnections();

        } catch (IOException e) {
            System.err.println("Error setting up I/O streams.");
            System.exit(1);
        }
    }

    public String getname() {
        return name;
    }

    public void run() {
        while (go) {
            try {
                String txt = datain.readLine();
                System.out.println("SERVER got from client: " + txt);

                if (txt == null || txt.equals("disconnect")) {
                    datain.close();
                    Server.decrementConnections();  //when user disconnects decrement the connection
                    go = false;
                } else if (txt.equals("hello")) {
                    int online = server.getConnectedClients();
                    dataout.writeBytes("CONNECTED:" + online + "\n");
                    dataout.flush();
                } else {
                    dataout.writeBytes("Unknown command: " + txt + "\n");
                    dataout.flush();
                }

            } catch (IOException e) {
                System.err.println("Connection lost with client " + id);
                Server.decrementConnections();//same thing here 
                go = false;
            }
        }
    }
}
