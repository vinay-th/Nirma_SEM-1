// B)Write a java program which will show the use of the custom
// exception in the program.

class CustomException extends Exception {
    CustomException(String message) {
        super(message);
    }
}

public class prac8_b {
    public static void main(String[] args) {
        try {
            throw new CustomException("This is a custom exception");
        } catch (CustomException e) {
            System.out.println("Caught custom exception: " + e.getMessage());
        }
    }
}
