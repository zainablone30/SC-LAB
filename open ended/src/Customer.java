/**
 * Represents a bank customer and their accounts
 */
import java.util.ArrayList;

public class Customer {
    private final String customerId;
    private final String name;
    private final String pin; // simple PIN for ATM auth
    private final ArrayList<BankAccount> accounts = new ArrayList<>();

    // ATM security
    private int failedPinAttempts = 0;
    private boolean atmBlocked = false;

    public static final int MAX_PIN_ATTEMPTS = 3;

    public Customer(String customerId, String name, String pin) {
        this.customerId = customerId;
        this.name = name;
        this.pin = pin;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPin() { return pin; }

    public ArrayList<BankAccount> getAccounts() { return accounts; }
    public void addAccount(BankAccount a) { accounts.add(a); }

    public boolean isAtmBlocked() { return atmBlocked; }
    public void blockAtm() { atmBlocked = true; }
    public void unblockAtm() { atmBlocked = false; failedPinAttempts = 0; }

    public boolean verifyPin(String attempt) {
        if (atmBlocked) return false;
        if (pin.equals(attempt)) {
            failedPinAttempts = 0;
            return true;
        }
        failedPinAttempts++;
        if (failedPinAttempts >= MAX_PIN_ATTEMPTS) {
            atmBlocked = true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Customer %s | %s | Accounts: %d | ATM blocked: %s",
                customerId, name, accounts.size(), atmBlocked);
    }
}

