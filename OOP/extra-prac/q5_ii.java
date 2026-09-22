
import java.util.Scanner;

// ii. check the category of a given character. (using if…else…if ladder)
public class q5_ii {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a char: ");
        char ch = sc.next().charAt(0);

        int ascci_val = (int) ch;

        if(ascci_val >= 0 && ascci_val <= 31)
            System.out.println("The character belongs to control characters");
        else if(ascci_val == 32)
            System.out.println("The character is space");
        else if(ascci_val >= 33 && ascci_val <= 47)
            System.out.println("The character belongs to numbers");
        else if(ascci_val >= 48 && ascci_val <= 57)
            System.out.println("The character belongs to numbers");
        else if(ascci_val >= 65 && ascci_val <= 90)
            System.out.println("The character belongs to Capital alphabets");
        else if(ascci_val >= 97 && ascci_val <= 122)
            System.out.println("The character belongs to Small alphabets");
    }
}
