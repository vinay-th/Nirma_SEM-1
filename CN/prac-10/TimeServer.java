import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class TimeServer {

    public static void main(String[] args) {
        try {
            String serverAddress = args.length > 0 ? args[0] : "localhost";
            int serverPort = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

            System.out.println("Server will start on " + serverAddress + ":" + serverPort);

            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("Server started on " + serverAddress + ":" + serverPort + ". Waiting for a client...");

            while (true) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("The client has connected.");

                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

                Date now = new Date();
                writer.println(now.toString());

                writer.close();
                clientSocket.close();
            }

        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }
}