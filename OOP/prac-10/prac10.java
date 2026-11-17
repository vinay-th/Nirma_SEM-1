
// Write a program in Java to copy one binary file to other using File Input
// Stream and Output Stream classes
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class prac10 {
    public static void main(String[] args) {
        try {
            FileInputStream fis = new FileInputStream("input.bin");
            FileOutputStream fos = new FileOutputStream("output.bin");

            int byteData;
            while ((byteData = fis.read()) != -1) {
                fos.write(byteData);
            }

            fis.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
