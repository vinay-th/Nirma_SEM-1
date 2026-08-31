import java.io.*;
import java.nio.file.*;

public class Receiver {
    static final String FLAG = "01111110";
    static final String HEADER = "10101010";
    static final String TRAILER = "01010101";

    public static String bitUnstuff(String stuffedPayload) {
        StringBuilder unstuffed = new StringBuilder();
        int count = 0;

        for (int i = 0; i < stuffedPayload.length(); i++) {
            char bit = stuffedPayload.charAt(i);
            unstuffed.append(bit);
            if (bit == '1') {
                count++;
                if (count == 5) {
                    if (i + 1 < stuffedPayload.length() && stuffedPayload.charAt(i + 1) == '0') {
                        i++;
                    }
                    count = 0;
                }
            } else {
                count = 0;
            }
        }
        return unstuffed.toString();
    }

    public static void main(String args[]) {
        try {
            String frame = new String(Files.readAllBytes(Paths.get("data.txt")));
            System.out.println("Received frame: " + frame);

            if (frame.startsWith(FLAG) && frame.endsWith(FLAG)) {
                String payload = frame.substring(FLAG.length() + HEADER.length(), frame.length() - FLAG.length() - TRAILER.length());
                System.out.println("Extracted stuffed payload: " + payload);

                String unstuffedPayload = bitUnstuff(payload);
                System.out.println("Unstuffed payload: " + unstuffedPayload);

                String data = FLAG + HEADER + unstuffedPayload + TRAILER + FLAG;
                Files.write(Paths.get("data.txt"), data.getBytes());
                System.out.println("Original data written to data.txt");
            } else {
                System.out.println("Invalid frame received.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading from the file.");
            e.printStackTrace();
        }
    }
}
