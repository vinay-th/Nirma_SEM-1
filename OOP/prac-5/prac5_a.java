// (A)Write a java program which will demonstrate the use of the static
// variable which is shared with all the objects.
class Demo {
    static int num = 100;
}

public class prac5_a {
    public static void main(String[] args) {
        Demo obj1 = new Demo();
        Demo obj2 = new Demo();

        System.out.println("Value of static variable num: " + Demo.num);
        System.out.println("Value of static variable num using obj1: " + obj1.num);
        System.out.println("Value of static variable num using obj2: " + obj2.num);
    }
}
