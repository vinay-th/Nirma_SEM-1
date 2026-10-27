import java.util.Scanner;
public class prac_2a_2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int i = 1;
        int n, fact = 1;

        System.out.print("Enter a number to find it's factorial: ");
        n = sc.nextInt();

        do { 
            fact = fact * i;
            i++;
        } while (i <= n);
        
        System.out.println("The factorial of " + n + " is " + fact);
    }
}
