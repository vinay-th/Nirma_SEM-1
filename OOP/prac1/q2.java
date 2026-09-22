import java.util.Scanner;
class q2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter length: ");
        int l = sc.nextInt();
        System.out.print("Enter breadth: ");
        int b = sc.nextInt();
        
        System.out.print("The area of rectangle is " + (l * b));
    }
}
