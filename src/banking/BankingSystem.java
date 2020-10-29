package banking;

import java.util.*;

/**
 * Course: JetBrains Academy, Java Developer Track
 * Project: Simple Banking System
 * Purpose: A console-based program to simulate operations in a bank.
 *
 * BankingSystem is the main class representing the bank.
 * It holds references to:
 * <ul>
 *     <li>card numbers</li>
 *     <li>accounts</li>
 *     <li>database manager</li>
 * </ul>
 *
 * @author Mirek Drozd
 * @version 1.1
 */
public class BankingSystem {
    Map<String, Integer> cardNumbers = new HashMap<>();
    Set<UserAccount> accounts = new HashSet<>();
    static Scanner scanner = new Scanner(System.in);
    static DBManager dbManager = new DBManager();
    final String[] params;

    public BankingSystem(String[] args) {
        this.params = args;
        dbManager.setup(this.params);
    }

    /**
     * Displays main program menu and reads user choice.
     */
    public void mainMenu() {
        System.out.println("\n1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit\n");
        int choice = readUsersChoice();

        switch (choice) {
            case 1: {
                createAccount();
                break;
            }
            case 2: {
                logIn();
                break;
            }
            case 0: {
                exit();
                break;
            }
            default: {
                readUsersChoice();
                break;
            }
        }
    }

    /**
     * Reads what menu option was selected by user.
     *
     * @return Number typed in by user.
     */
    public int readUsersChoice() {
        int choice = -1;
        try {
            choice = scanner.nextInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return choice;
    }

    /**
     * Creates new user account.
     * To do this, it generates new card number and PIN.
     * By default, balance on a newly created account is 0.
     * When created, the account and its card number are registered
     * in their respective collections.
     */
    public void createAccount() {
        String cardNumber = generateCardNumber();
        int PIN = generatePIN();
        int balance = 0;
        UserAccount account = new UserAccount(cardNumber, PIN, balance);
        cardNumbers.put(cardNumber, PIN);
        accounts.add(account);
        dbManager.insert(cardNumber, PIN, balance);
        mainMenu();
    }

    /**
     * Computes checksum for bank account number.
     *
     * @param accountNumber Number of the bank account.
     * @return Computed checksum.
     */
    public int findChecksum(String accountNumber) {
        StringBuilder sb = new StringBuilder();
        //Luhn algorithm
        //step 1: multiply odd digits by 2 (count starts at 1, NOT 0!)
        for (int i = 1; i <= accountNumber.length(); i++) {
            if (i % 2 != 0) {
                int num = Integer.valueOf(String.valueOf(accountNumber.charAt(i - 1)));
                num = num * 2;
                if (num > 9) {
                    //step 2: subtract 9 from numbers higher than 9
                    num = num - 9;
                }
                sb.append(num);
            } else {
                sb.append(accountNumber.charAt(i - 1));
            }
        }
        String processedNumber = sb.toString();
        //step 3: add up all digits
        int sum = 0;
        for (int j = 0; j < processedNumber.length(); j++) {
            sum += Integer.valueOf(processedNumber.charAt(j));
        }
        int checksum = 10 - (sum % 10);
        return checksum == 10 ? 0 : checksum;
    }

    /**
     * Generates new card number based on Issuer Identification Number (IIN).
     * By convention, IIN always starts with 400000 in this program.
     * The remaining 9 numbers are randomly generated using Java's Random.
     *
     * @return Generated card number as String.
     */
    public String generateCardNumber() {
        int[] nums = new Random().ints(9, 0,9).toArray();
        String IIN = "400000" + Arrays.toString(nums).replaceAll("\\[|\\]|,|\\s", "");
        IIN = IIN.concat(String.valueOf(findChecksum(IIN)));
        System.out.println("Your card has been created");
        System.out.printf("Your card number: \n%s\n", IIN);
        return IIN;
    }

    /**
     * Generates 4-digit PIN for the card using Java's Random.
     * Possible PIN range is from 0000 to 9999.
     *
     * @return Generated PIN as int.
     */
    public int generatePIN() {
        int[] nums =  new Random().ints(4, 1, 9).toArray();
        int pin = 0;
        for (int i : nums) {
            pin = 10 * pin + i;
        }
        System.out.printf("Your card PIN: \n%d\n", pin);
        return pin;
    }

    /**
     * Simulates logging in procedure.
     * User has to enter their card number and PIN.
     * These are then verified against database.
     */
    public void logIn() {
        try (Scanner scanner = new Scanner(System.in)){
            System.out.println("Enter your card number:");
            String cardNumber = scanner.nextLine();
            System.out.println("Enter your PIN:");
            int pin =  scanner.nextInt();
            checkCredentials(new UserAccount(cardNumber, pin, 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks user authentication data (card number & PIN) against database.
     *
     * @param user The user to be authenticated.
     */
    public void checkCredentials(UserAccount user) {
        if (accounts.contains(user)) {
            System.out.println("\nYou have successfully logged in!");
            userMenu(user);
        } else {
            System.out.println("\nWrong card number or PIN!");
            mainMenu();
        }
    }

    /**
     * Displays program menu after user logs in.
     *
     * @param user The logged-in user.
     */
    public void userMenu(UserAccount user) {
        System.out.println("\n1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit\n");
        int choice = readUsersChoice();

        switch (choice) {
            case 1: {
                System.out.println(user.getBalance());
                userMenu(user);
                break;
            }
            case 2: {
                addIncome(user);
                break;
            }
            case 3: {
                doTransfer(user);
                break;
            }
            case 4: {
                closeAccount(user);
                break;
            }
            case 5: {
                logOut();
                break;
            }
            case 0: {
                exit();
                break;
            }
        }
    }

    /**
     * Adds the amount of money specified by user to user's account.
     *
     * @param user The logged-in user.
     */
    public void addIncome(UserAccount user) {
        System.out.println("Enter income:");
        int income = scanner.nextInt();
        int currentBalance = user.getBalance();
        int newBalance = currentBalance + income;
        user.setBalance(newBalance);
        int id = dbManager.selectIDByCard(user.getCardNumber());
        dbManager.update(id, newBalance);
        System.out.println("Income was added!");

        userMenu(user);
    }

    /**
     * Transfers money from users account to some different account.
     * User cannot transfer more money than is available on their account.
     * Target account has to have a valid number & has to exist in database.
     *
     * @param user The logged-in user.
     */
    public void doTransfer(UserAccount user) {
        System.out.println("Enter card number:");
        try(Scanner scanner = new Scanner(System.in)) {
            String input = scanner.nextLine();
            int checksum = findChecksum(input.substring(0, input.length()-1));
            if (checksum == Character.getNumericValue(input.charAt(input.length()-1)) && accountExists(input)) {
                System.out.println("Enter how much money you want to transfer:");
                int money = scanner.nextInt();
                int balance = user.getBalance();
                if (balance >= money) {
                    user.setBalance(balance - money);
                    System.out.println("Success!");
                    userMenu(user);
                } else {
                    System.out.println("Not enough money!");
                    userMenu(user);
                }

            } else {
                System.out.println("Probably you made mistake in the card number. Please try again!");
                userMenu(user);
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether the account exists in the database.
     *
     * @param accountNumber The number of the account.
     * @return true if account exists, or false if account doesn't exist.
     */
    public boolean accountExists(String accountNumber) {
        return cardNumbers.containsKey((String) accountNumber);
    }

    /**
     * Closes the account by removing it from database.
     *
     * @param user The user account to be closed.
     */
    public void closeAccount(UserAccount user) {
        accounts.remove(user);
        int id = dbManager.selectIDByCard(user.getCardNumber());
        dbManager.delete(id);
        System.out.println("The account has been closed!");
        userMenu(user);
    }

    /**
     * Prints message to confirm logging out and displays main menu.
     */
    public void logOut() {
        System.out.println("You have successfully logged out!");
        mainMenu();
    }

    /**
     * Prints message to confirm program shutdown and exits the program.
     */
    public void exit() {
        System.out.println("Bye!");
        System.exit(0);
    }
}
