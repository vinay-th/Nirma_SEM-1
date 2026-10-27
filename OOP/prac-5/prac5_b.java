// (B)Write a java program to get the cube of the given number using
// static method
class Demo2 {
    static int cube(int num) {
        return num * num * num;
    }
}

public class prac5_b {
    public static void main(String[] args) {
        int num = 5;
        int result = Demo2.cube(num);
        System.out.println("Cube of " + num + " is: " + result);
    }
}
