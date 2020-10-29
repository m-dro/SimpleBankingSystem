package banking;

public class Main {

    /**
     * The main method begins execution of the program.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        BankingSystem system = new BankingSystem(args);
        system.mainMenu();
    }
}
