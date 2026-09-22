
import java.util.Scanner;

// i. check whether a number is odd or even (using if – else statement)
public class q5_i {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a number: ");
        int num = sc.nextInt();

        if(num % 2 == 0)
            System.out.println("The number " + num + " is even.");
        else
            System.out.println("The number " + num + " is odd.");
    }
}
