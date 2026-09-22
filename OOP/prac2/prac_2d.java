
import java.util.Scanner;

public class prac_2d {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the number: ");
        int n = sc.nextInt();
        int count = 1;

        for(int i = 1; i <= n; i++){
            for(int j = 1; j <= i; j++){
                System.out.print(count + "\t");
                count++;
            }
            System.out.println();
        }
    }
}
