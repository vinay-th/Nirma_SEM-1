import java.util.Scanner;
class q3 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your percentage: ");
        double perc = sc.nextDouble();
        char grade;
        
        if(perc > 95){
            grade = 'O';
        }else if(perc > 85 && perc <= 95){
            grade = 'A';
        }else if(perc > 70 && perc <= 85){
            grade = 'B';
        }else if(perc > 55 && perc <= 70){
            grade = 'C';
        }else if(perc > 33 && perc <= 55){
            grade = 'D';
        }else{
            grade = 'F';
        }
        System.out.print("Your grade for percentage: " + perc + "% is " + grade);
    }
}
