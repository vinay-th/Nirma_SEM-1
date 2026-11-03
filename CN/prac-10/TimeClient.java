import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class TimeClient {

    public static void main(String[] args) {
        try {
            String serverAddress = args.length > 0 ? args[0] : "localhost";
            int serverPort = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

            Socket socket = new Socket(serverAddress, serverPort);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String time = reader.readLine();
            System.out.println("The time of the server is: " + time);

            reader.close();
            socket.close();

        } catch (Exception e) {
            System.out.println("Server not connected: " + e.getMessage());
        }
    }
}