import java.util.Scanner;
public class prac_2a_3 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n, fact = 1;

        System.out.print("Enter a number to find it's factorial: ");
        n = sc.nextInt();

        for(int i = 1; i <= n; i++){
            fact *= i;
        }

        System.out.println("The factorial of " + n + " is " + fact);
    }
}
