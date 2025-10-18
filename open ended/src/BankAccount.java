/**
 * Abstract base class for bank accounts
 */
import java.util.ArrayList;

public abstract class BankAccount {
    protected final String accountNumber;
    protected final String customerId;
    protected double balance;
    protected AccountStatus status;
    protected final ArrayList<Transaction> transactions = new ArrayList<>();

    public BankAccount(String accountNumber, String customerId, double initialBalance) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = initialBalance;
        this.status = AccountStatus.ACTIVE;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerId() { return customerId; }
    public double getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public ArrayList<Transaction> getTransactions() { return transactions; }

    protected void addTransaction(Transaction t) { transactions.add(t); }

    public abstract void deposit(double amount) throws AccountBlockedException;
    public abstract void withdraw(double amount) throws InsufficientFundsException, AccountBlockedException;

    public abstract String getAccountType();

    @Override
    public String toString() {
        return String.format("%s Account %s | Customer: %s | Balance: %.2f | Status: %s",
                getAccountType(), accountNumber, customerId, balance, status);
    }
}

