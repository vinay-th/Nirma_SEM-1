import java.util.Scanner;
public class q1 {
    public static void main(String[] args) {
        Circle crc = new Circle();
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the raius: ");
        int rad = sc.nextInt();

        System.out.println("The area is: " + crc.area(rad));
    }
}
