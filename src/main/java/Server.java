import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private List<ClientListener> clientList;
    private ServerSocket serverSocket = null;

    public Server() {
        try {
            this.serverSocket = new ServerSocket(8080);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        this.clientList = new ArrayList<ClientListener>();
    }

    public void Listen() {
        while (true) {
            Socket clientSocket;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                return;
            }
            ClientListener cl = new ClientListener(clientSocket);
            this.clientList.add(cl);
            Thread clientThread = new Thread(cl);
            clientThread.start();
        }
    }

    public void close() {
        System.out.println("Closing connections...");
        try {
            this.serverSocket.close();
            for (ClientListener cl : this.clientList) {
                if (!cl.socket.isClosed()) {
                    cl.socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeClient(ClientListener clientListener) {
        if (clientList.contains(clientListener)) {
            if (!clientListener.socket.isClosed()) {
                try {
                    clientListener.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            clientList.remove(clientListener);
        }
        if (clientList.isEmpty()) {
            System.out.println("No clients, shutting down...");
            close();
        }
    }

    public void updateAll(ClientListener clientListener, String msg) {
        for (ClientListener cl : this.clientList) {
            if (cl == clientListener) {
                continue;
            }
            String formattedMsg = clientListener.name + ": " + msg;
            cl.send(formattedMsg);
        }
    }

    public class ClientListener implements Runnable {

        private Socket socket;
        private String name;
        private BufferedReader clientIn;
        private BufferedWriter clientOut;

        ClientListener(Socket socket) {
            this.socket = socket;
            try {
                this.clientOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String line = clientIn.readLine();
                String[] nameLine = line.split(":");
                if (nameLine.length < 2 || !nameLine[0].equals("name")) {
                    System.out.println("Client without name, disconnecting.");
                    socket.close();
                    return;
                }
                this.name = nameLine[1];
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void send(String msg) {
            try {
                this.clientOut.write(msg);
                this.clientOut.newLine();
                this.clientOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            String line;
            try {
                while (true) {
                    line = this.clientIn.readLine();
                    if (line == null || line.equals("Exit")) {
                        removeClient(this);
                        updateAll(this, this.name + " has left the chat..");
                        return;
                    }
                    updateAll(this, line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.Listen();
    }
}
