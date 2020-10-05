package banking;

import java.sql.*;
import java.util.*;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        BankingSystem system = new BankingSystem(args);
        system.mainMenu();
    }
}



class BankingSystem {

    Map<String, Integer> cardNumbers = new HashMap<>();
    Set<UserAccount> accounts = new HashSet<>();
    static Scanner scanner = new Scanner(System.in);
    static DBManager dbManager = new DBManager();
    final String[] params;

    public BankingSystem(String[] args) {
        this.params = args;
        dbManager.setup(this.params);
    }

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

    public int readUsersChoice() {
        int choice = -1;
        try {
            choice = scanner.nextInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return choice;
    }


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

    public String generateCardNumber() {
        int[] nums = new Random().ints(9, 0,9).toArray();
        String IIN = "400000" + Arrays.toString(nums).replaceAll("\\[|\\]|,|\\s", "");
        IIN = IIN.concat(String.valueOf(findChecksum(IIN)));
        System.out.println("Your card has been created");
        System.out.printf("Your card number: \n%s\n", IIN);
        return IIN;
    }

    public int generatePIN() {
        int[] nums =  new Random().ints(4, 1, 9).toArray();
        int pin = 0;
        for (int i : nums) {
            pin = 10 * pin + i;
        }
        System.out.printf("Your card PIN: \n%d\n", pin);
        return pin;
    }

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

    public void checkCredentials(UserAccount user) {
        if (accounts.contains(user)) {
            System.out.println("\nYou have successfully logged in!");
            userMenu(user);
        } else {
            System.out.println("\nWrong card number or PIN!");
            mainMenu();
        }
    }


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

    public boolean accountExists(String accountNumber) {
        return cardNumbers.containsKey((String) accountNumber);
    }

    public void closeAccount(UserAccount user) {
        accounts.remove(user);
        int id = dbManager.selectIDByCard(user.getCardNumber());
        dbManager.delete(id);
        System.out.println("The account has been closed!");
        userMenu(user);
    }

    public void logOut() {
        System.out.println("You have successfully logged out!");
        mainMenu();
    }


    public void exit() {
        System.out.println("Bye!");
        System.exit(0);
    }
}

class UserAccount {
    private String cardNumber;
    private int PIN;
    private int balance;

    public UserAccount(String cardNumber, int PIN, int balance) {
        this.cardNumber = cardNumber;
        this.PIN = PIN;
        this.balance = balance;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getPIN() {
        return PIN;
    }

    public void setPIN(int PIN) {
        this.PIN = PIN;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (! (obj instanceof UserAccount)) return false;
        UserAccount acc = (UserAccount) obj;
        return Objects.equals(this.getCardNumber(), acc.getCardNumber())
                && Objects.equals(this.getPIN(), acc.getPIN());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getCardNumber(), this.getPIN());
    }
}

class DBManager {
    String db;

    public void setup(String[] args) {
        db = args[1];
        createNewTable(db);
    }


    public String getFileName(String[] args) {
        String fileName = "";
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-fileName")) {
                    fileName = args[i+1];
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.db = fileName;
        return fileName;
    }



    public void createNewTable(String db) {

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS card (\n" +
                "        id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "        number  TEXT,\n" +
                "        pin     TEXT,\n" +
                "        balance INTEGER DEFAULT 0\n" +
                "      );";

        try (Connection conn = this.connect(db);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    private Connection connect(String db) {
        // SQLite connection string
        String url = "jdbc:sqlite:C:\\Users\\Mirek\\IdeaProjects\\Simple Banking System\\Simple Banking System\\task\\" + db;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public int selectIDByCard(String card){
        String sql = "SELECT id FROM card WHERE number = ?";
        int ID = 0;
        try (Connection conn = this.connect(db);
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1, card);
            ResultSet rs    = pstmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                ID = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return ID;
    }

    public void insert(String number, int pin, int balance) {
        String sql = "INSERT INTO card(number,pin,balance) VALUES(?,?,?)";

        try (Connection conn = this.connect(db);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, number);
            pstmt.setInt(2, pin);
            pstmt.setInt(3, balance);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void update(int id, int newBalance) {
        String sql = "UPDATE card SET balance = ?"
                + "WHERE id = ?";

        try (Connection conn = this.connect(db);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setInt(1, newBalance);
            pstmt.setInt(2, id);
            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM card WHERE id = ?";
        System.out.println(sql);
        try (Connection conn = this.connect(db);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setInt(1, id);
            // delete
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
