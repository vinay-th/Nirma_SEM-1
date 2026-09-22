
import java.util.Scanner;

// iii. check whether a number is prime or not. (using for loop)
public class q5_iii {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Boolean isPrime = true;

        System.out.print("Enter a number: ");
        int num = sc.nextInt();

        for(int i = 2; i < Math.sqrt(num) + 1; i++){
            if(num % i == 0){
                isPrime = false;
                break;
            }
        }

        if(isPrime)
            System.out.println("The number is prime");
        else
            System.out.println("The number is not prime");

        sc.close(); 
    }
}
