import java.util.Scanner;

public class prac_2b_2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int rem, res = 0;

        System.out.print("Enter a number to find it's sum of digits: ");
        int n = sc.nextInt();

        for(int i = n; i > 0; i /= 10){
            rem = i % 10;
            res += rem;
        }
        
        System.out.println("The sum of digits of num " + n + " is " + res);
    }
}
