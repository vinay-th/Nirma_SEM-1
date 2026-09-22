
import java.util.Scanner;

// 4. Write a Java program to print the ASCII values for characters entered by the user. 
public class q4 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a char: ");
        char ch = sc.next().charAt(0);
        
        System.out.println("Ascii of char: " + ((int)ch));

    }
}
