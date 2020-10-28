package banking;

import java.util.Objects;

public class UserAccount {
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
