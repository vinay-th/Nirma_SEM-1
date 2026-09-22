import java.util.Scanner;

public class q5_v {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a number: ");
        int num = sc.nextInt();

        for(int i = 0; i < num; i++){
            int count = 1;
            for(int j = num - 1; j >= 0; j--){
                if(j <= i){
                    System.out.print(" " + count);
                    count++;
                } else {
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
    }
}
