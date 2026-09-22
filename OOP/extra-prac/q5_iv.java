// iv. display reverse of a number and check whether it is palindrome or not. (using while/do while loop)

import java.util.Scanner;

public class q5_iv {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Boolean isPalindrome = false;

        System.out.print("Enter a number: ");
        int num = sc.nextInt();

        String str = Integer.toString(num);
        String revStr = new StringBuilder(str).reverse().toString();

        if(str.compareTo(revStr) == 0)
            isPalindrome = true;

        if(isPalindrome)
            System.out.println("The number is Palindrome");
        else
            System.out.println("The number is not Palindrome");
    }
}
