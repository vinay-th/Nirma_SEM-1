// Write a sender and receiver side bit stuffing program in which a frame is to be simulated with header, payload and trailer. In that, demonstrate the bit stuffing concept. Use files to read payload and
// write the payload back to the file. Use C/C++/Java [Starting and ending flags/indicators are 01111110].

// Data will be stored in data.txt
// Bits will be stuffed in the payload and sent to the receiver. The receiver will unstuff the bits and write the original data back to the file.
// │ Flag  │   Header    │ Payload   │ Trailer │ Flag  │
import java.io.*;
import java.util.*;
import java.nio.file.*;

public class Sender {
    static final String FLAG = "01111110";
    static final String HEADER = "10101010";
    static final String TRAILER = "01010101";

    public static String bitStuff(String payload) {
        StringBuilder stuffed = new StringBuilder();
        int count = 0;

        for (char bit : payload.toCharArray()) {
            stuffed.append(bit);
            if (bit == '1') {
                count++;
                if (count == 5) {
                    stuffed.append('0');
                    count = 0;
                }
            } else {
                count = 0;
            }

        }
        return stuffed.toString();
    }

    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter data in binary: ");
        String data = sc.nextLine();
        String stuffedPayload = bitStuff(data);
        System.out.println("Stuffed payload: " + stuffedPayload);

        String frame = FLAG + HEADER + stuffedPayload + TRAILER + FLAG;
        System.out.println("Frame to be sent: " + frame);

        try {
            Files.write(Paths.get("data.txt"), frame.getBytes());
            System.out.println("Frame written to data.txt");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }

        sc.close();
    }
}
