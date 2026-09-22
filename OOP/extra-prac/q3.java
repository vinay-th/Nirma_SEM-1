// Write a Java program that demonstrate the concepts of automatic and explicit type casting. 
public class q3 {
    public static void main(String[] args) {
        int intNum = 50;
        double num = intNum;
        double doNum = (int)intNum;
        int num2 = (int)doNum;

        System.out.println("Implicit type casted number: " + num);
        System.out.println("Explict type casted number: " + num2);
    }
}
