// 3 (String Handling)
// Write a program in java that shows the use of different methods
// available in String class.

public class prac_3 {
    public static void main(String[] args) {
        String str = "Hello, World!";
        System.out.println("Original string: " + str);

        System.out.println("Length of the string: " + str.length());
        System.out.println("Character at index 7: " + str.charAt(7));
        System.out.println("Substring from index 7: " + str.substring(7));
        System.out.println("Substring from index 0 to 5: " + str.substring(0, 5));
        System.out.println("Index of 'World': " + str.indexOf("World"));
        System.out.println("String in uppercase: " + str.toUpperCase());
        System.out.println("String in lowercase: " + str.toLowerCase());
        System.out.println("String after replacing 'World' with 'Java': " + str.replace("World", "Java"));
        System.out.println("String after trimming whitespace: " + str.trim());
    }
}