import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private String ip;
    private int port;
    private String name;

    public Client(String ip, int port, String name) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public static void main(String[] args) {
        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String name = args[2];
        Client client = new Client(ip, port, name);
        client.login();
    }

    public void login() {
        try {
            final Socket socket = new Socket(ip, port);
            System.out.println("Client " + name + " Connected to: " + socket.getRemoteSocketAddress());
            BufferedWriter outputToServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            outputToServer.write("name:" + name);
            outputToServer.newLine();
            outputToServer.flush();

            ServerInput serverInputRunnable = new ServerInput(socket);
            Thread serverInputThread = new Thread(serverInputRunnable);

            serverInputThread.start();

            Scanner userInput = new Scanner(System.in);
            while (true) {
                if (userInput.hasNext()) {
                    String line = userInput.nextLine();
                    outputToServer.write(line);
                    outputToServer.newLine();
                    outputToServer.flush();
                    if (line.equals("Exit")) {
                        break;
                    }
                }
            }

            serverInputRunnable.exit();
            serverInputThread.join();
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class ServerInput implements Runnable {

        BufferedReader in = null;
        private volatile boolean running = true;

        public ServerInput(Socket socket) {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        public void exit() {
            running = false;
        }

        public void run() {
            String line;
            while (running) {
                try {
                    if (in.ready()) {
                        line = in.readLine();
                        if (line == null) {
                            return;
                        }
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    return;
                }
            }
        }
    }
}
