import java.util.Scanner;

public class prac_2b_3 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int rem, res = 0;

        System.out.print("Enter a number to find it's sum of digits: ");
        int n = sc.nextInt();
        int temp = n;

        do{
            rem = temp % 10;
            res += rem;
            temp /= 10;
        }while(temp > 0);
        
        System.out.println("The sum of digits of num " + n + " is " + res);
    }
}
