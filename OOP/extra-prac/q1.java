import java.util.Scanner;

// 1. Write a Java Program that check whether user entered number is special number or not. For example,  Consider the number is 59. First, find the sum of all digits (5+9=14). Second, find multiplication of all digits (5*9=45). Then find addition of sum and multiplication of all digits (14+45=59). If it is same as number itself, than it is a special number. 
public class q1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a num: ");
        int num = sc.nextInt();
        int add = 0; 
        int res = 1; 
        
        for(int i = num; i > 0; i/=10){
            int rem = i % 10;
            add += rem;
            res *= rem;
        }

        System.out.println("Sum of digit: " + add);
        System.out.println("Multiply of digit: " + res);
        System.out.println("Sum of added and result: " + (add + res));
    }
}
