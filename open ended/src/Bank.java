import java.util.HashMap;
import java.util.Map;

/**
 * Central bank class that manages customers and accounts
 */
public class Bank {
    private final Map<String, Customer> customers = new HashMap<>();
    private final Map<String, BankAccount> accounts = new HashMap<>();

    public Bank() {}

    public Customer registerCustomer(String customerId, String name, String pin) {
        Customer c = new Customer(customerId, name, pin);
        customers.put(customerId, c);
        return c;
    }

    public SavingsAccount createSavingsAccount(String accountNumber, String customerId, double initialBalance, double minimumBalance) {
        Customer c = customers.get(customerId);
        if (c == null) return null;
        SavingsAccount acc = new SavingsAccount(accountNumber, customerId, initialBalance, minimumBalance);
        c.addAccount(acc);
        accounts.put(accountNumber, acc);
        return acc;
    }

    public CheckingAccount createCheckingAccount(String accountNumber, String customerId, double initialBalance, double overdraftLimit) {
        Customer c = customers.get(customerId);
        if (c == null) return null;
        CheckingAccount acc = new CheckingAccount(accountNumber, customerId, initialBalance, overdraftLimit);
        c.addAccount(acc);
        accounts.put(accountNumber, acc);
        return acc;
    }

    public Customer getCustomerById(String customerId) {
        return customers.get(customerId);
    }

    public BankAccount getAccountByNumber(String accountNumber) {
        return accounts.get(accountNumber);
    }

    /**
     * Transfer funds between two accounts (can be cross-customer)
     */
    public TransactionStatus transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        BankAccount from = accounts.get(fromAccountNumber);
        BankAccount to = accounts.get(toAccountNumber);
        if (from == null || to == null) {
            if (from != null) {
                Transaction t = new Transaction(TransactionType.TRANSFER, amount, fromAccountNumber, toAccountNumber, TransactionStatus.FAILED_INVALID_ACCOUNT, "Destination account not found");
                from.getTransactions().add(t);
            }
            return TransactionStatus.FAILED_INVALID_ACCOUNT;
        }
        if (from.getStatus() != AccountStatus.ACTIVE) {
            Transaction t = new Transaction(TransactionType.TRANSFER, amount, fromAccountNumber, toAccountNumber, TransactionStatus.FAILED_ACCOUNT_BLOCKED, "Source account blocked");
            from.getTransactions().add(t);
            return TransactionStatus.FAILED_ACCOUNT_BLOCKED;
        }
        if (to.getStatus() != AccountStatus.ACTIVE) {
            Transaction t = new Transaction(TransactionType.TRANSFER, amount, fromAccountNumber, toAccountNumber, TransactionStatus.FAILED_ACCOUNT_BLOCKED, "Destination account blocked");
            from.getTransactions().add(t);
            return TransactionStatus.FAILED_ACCOUNT_BLOCKED;
        }

        try {
            from.withdraw(amount);
        } catch (AccountBlockedException e) {
            Transaction t = new Transaction(TransactionType.TRANSFER, amount, fromAccountNumber, toAccountNumber, TransactionStatus.FAILED_ACCOUNT_BLOCKED, e.getMessage());
            from.getTransactions().add(t);
            return TransactionStatus.FAILED_ACCOUNT_BLOCKED;
        } catch (InsufficientFundsException e) {
            Transaction t = new Transaction(TransactionType.TRANSFER, amount, fromAccountNumber, toAccountNumber, TransactionStatus.FAILED_INSUFFICIENT_FUNDS, e.getMessage());
            from.getTransactions().add(t);
            return TransactionStatus.FAILED_INSUFFICIENT_FUNDS;
        }

        try {
            to.deposit(amount);
        } catch (AccountBlockedException e) {
            // rollback: refund from
            try {
                from.deposit(amount);
            } catch (AccountBlockedException ex) {
                // This should not happen: source was active earlier.
            }
            Transaction t = new Transaction(TransactionType.TRANSFER, amount, fromAccountNumber, toAccountNumber, TransactionStatus.FAILED_ACCOUNT_BLOCKED, "Destination blocked during transfer");
            from.getTransactions().add(t);
            return TransactionStatus.FAILED_ACCOUNT_BLOCKED;
        }

        Transaction success = new Transaction(TransactionType.TRANSFER, amount, fromAccountNumber, toAccountNumber, TransactionStatus.SUCCESS, "Transfer completed");
        from.getTransactions().add(success);
        to.getTransactions().add(success);
        return TransactionStatus.SUCCESS;
    }

    public void initSampleData() {
        // Create sample customers and accounts with Pakistani names and default PIN 0000
        Customer c1 = registerCustomer("CUST1001", "Muhammad Ali", "0000");
        Customer c2 = registerCustomer("CUST1002", "Ayesha Khan", "0000");
        Customer c3 = registerCustomer("CUST1003", "Ahmed Raza", "0000");

        createSavingsAccount("SAV1001", c1.getCustomerId(), 1000.00, 100.00);
        createCheckingAccount("CHK1001", c1.getCustomerId(), 500.00, 200.00);

        createSavingsAccount("SAV2001", c2.getCustomerId(), 1500.00, 100.00);
        createCheckingAccount("CHK2001", c2.getCustomerId(), 300.00, 100.00);

        createSavingsAccount("SAV3001", c3.getCustomerId(), 2000.00, 200.00);
    }

    public Map<String, Customer> getAllCustomers() { return customers; }
    public Map<String, BankAccount> getAllAccounts() { return accounts; }
}
