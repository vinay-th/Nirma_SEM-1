import java.util.Scanner;
public class prac_2a_1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int i = 1;
        int n, fact = 1;

        System.out.print("Enter a number to find it's factorial: ");
        n = sc.nextInt();

        while(i <= n){
            fact = fact * i;
            i++;
        }
        System.out.println("The factorial of " + n + " is " + fact);
    }
}
