// (A)Write a Java program to create a class called "Dog" with a name
// and breed attribute. Create two instances of the "Dog" class, set
// their attributes using the constructor and modify the attributes
// using the setter methods and print the updated values.
class Dog {
    public String name;
    public String breed;

    Dog(String name, String breed) {
        this.name = name;
        this.breed = breed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void display() {
        System.out.println("Dog Name: " + name);
        System.out.println("Dog Breed: " + breed);
    }
}

public class prac4_a {
    public static void main(String[] args) {
        Dog dog1 = new Dog("Buddy", "Golden Retriever");
        Dog dog2 = new Dog("Max", "German Shepherd");

        System.out.println("Initial values:");
        dog1.display();
        dog2.display();

        dog1.setName("Charlie");
        dog1.setBreed("Labrador Retriever");

        dog2.setName("Rocky");
        dog2.setBreed("Bulldog");

        System.out.println("\nUpdated values:");
        dog1.display();
        dog2.display();
    }
}
