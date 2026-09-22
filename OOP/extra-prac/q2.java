// 2. Write a Java program using class that prints the numbers 1 to N (N must be scan from the user). For all multiples of 3 print “Bizz” and for all multiples of 5 print “Fizz”. For multiples of both 3 and 5 print “Bizz-Fizz”. 

import java.util.Scanner;

public class q2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a num: ");
        int num = sc.nextInt();

        for(int i = 0; i < num; i++){
            if(i % 3 == 0 && i % 5 == 0){
                System.out.println("Bizz-Fizz");
                continue;
            }
            if(i % 3 == 0){
                System.out.println("Bizz");
            }
            if(i % 5 == 0){
                System.out.println("Fizz");
            }
            else
                System.out.println(i);
        }
    }
}
