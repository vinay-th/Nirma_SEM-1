// 6. Design calculator which contains arithmetic & bitwise operators. Operand(s) and operator must be scan from the user.
import java.util.Scanner;

public class q6 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a, b, result;
        int choice;

        System.out.print("Enter first number: ");
        a = sc.nextInt();
        System.out.print("Enter second number: ");
        b = sc.nextInt();

        System.out.println("=== Advanced Calculator ===");
        System.out.println("Arithmetic Operations:");
        System.out.println("1. Addition (+)");
        System.out.println("2. Subtraction (-)");
        System.out.println("3. Multiplication (*)");
        System.out.println("4. Division (/)");
        System.out.println("5. Modulus (%)");
        System.out.println("\nBitwise Operations:");
        System.out.println("6. Bitwise AND (&)");
        System.out.println("7. Bitwise OR (|)");
        System.out.println("8. Bitwise XOR (^)");
        System.out.println("9. Left Shift (<<)");
        System.out.println("10. Right Shift (>>)");
        System.out.println("11. Bitwise NOT (~)");
        System.out.print("Enter your choice (1-11): ");
        
        if (sc.hasNextInt()) {
            choice = sc.nextInt();
        } else {
            System.out.println("Invalid input. Please enter a number.");
            sc.close();
            return;
        }

        switch(choice) {
            case 1:
                result = a + b;
                System.out.println("Result: " + result);
                break;
            case 2:
                result = a - b;
                System.out.println("Result: " + result);
                break;
            case 3:
                result = a * b;
                System.out.println("Result: " + result);
                break;
            case 4:
                if (b != 0) {
                    result = a / b;
                    System.out.println("Result: " + result);
                } else {
                    System.out.println("Error: Division by zero!");
                }
                break;
            case 5:
                if (b != 0) {
                    result = a % b;
                    System.out.println("Result: " + result);
                } else {
                    System.out.println("Error: Modulus by zero!");
                }
                break;
            case 6:
                result = a & b;
                System.out.println("Result: " + result);
                break;
            case 7:
                result = a | b;
                System.out.println("Result: " + result);
                break;
            case 8:
                result = a ^ b;
                System.out.println("Result: " + result);
                break;
            case 9:
                result = a << b;
                System.out.println("Result: " + result);
                break;
            case 10:
                result = a >> b;
                System.out.println("Result: " + result);
                break;
            case 11:
                result = ~a;
                System.out.println("Result: " + result);
                break;
        }

        sc.close();
    }
}
