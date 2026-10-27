// (B)Write a Java program to create a class called "Bank" with a
// collection of accounts and methods to add and remove accounts,
// and to deposit and withdraw money. Also define a class called
// "Account" to maintain account details of a particular customer.

class Bank {
    Account[] accounts;

    public Bank(int size) {
        accounts = new Account[size];
    }

    public void addAccount(Account account, int index) {
        if (index >= 0 && index < accounts.length) {
            accounts[index] = account;
        } else {
            System.out.println("Invalid index. Account not added.");
        }
    }

    public void removeAccount(int index) {
        if (index >= 0 && index < accounts.length) {
            accounts[index] = null;
        } else {
            System.out.println("Invalid index. Account not removed.");
        }
    }

    public void displayAccounts() {
        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i] != null) {
                System.out.println("Account at index " + i + ":");
                accounts[i].displayAccountDetails();
                System.out.println();
            } else {
                System.out.println("No account at index " + i);
            }
        }
    }
}

class Account {
    public String accountNumber;
    public String accountHolderName;
    private double balance;

    public Account(String accountNumber, String accountHolderName, double balance) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.balance = balance;
    }

    public void displayAccountDetails() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Account Holder Name: " + accountHolderName);
        System.out.println("Balance: " + balance);
    }
}

public class prac4_b {
    public static void main(String[] args) {
        Bank bank = new Bank(3);

        Account account1 = new Account("123456", "Kanishka", 1000.0);
        Account account2 = new Account("789012", "Krutika", 2000.0);
        Account account3 = new Account("345678", "Kirti", 3000.0);

        bank.addAccount(account1, 0);
        bank.addAccount(account2, 1);
        bank.addAccount(account3, 2);

        bank.displayAccounts();
    }
}