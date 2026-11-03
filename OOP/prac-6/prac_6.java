// Write a java program that shows the use of compile time and run-time
// polymorphism.
// (A) Write a Java program which will the concept of method
// overloading by creating two methods, first add() method
// performs addition of two numbers and second add() method
// performs addition of three numbers.
// (B)Write a Java program to create a class Vehicle with a method
// called speedUp(). Create two subclasses Car and Bicycle.
// Override the speedUp() method in each subclass to increase the
// vehicle's speed differently.

class Vehicle {
    void speedUp() {
        System.out.println("Vehicle is speeding up.");
    }
}

class Car extends Vehicle {
    @Override
    void speedUp() {
        System.out.println("Car is speeding up.");
    }
}

class Bicycle extends Vehicle {
    @Override
    void speedUp() {
        System.out.println("Bicycle is speeding up.");
    }
}

class Calculator {
    int add(int a, int b) {
        return a + b;
    }

    int add(int a, int b, int c) {
        return a + b + c;
    }
}

public class prac_6 {
    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        System.out.println("Addition of two numbers: " + calculator.add(5, 10));
        System.out.println("Addition of three numbers: " + calculator.add(5, 10, 15));

        Vehicle myCar = new Car();
        Vehicle myBicycle = new Bicycle();

        myCar.speedUp();
        myBicycle.speedUp();
    }
}
