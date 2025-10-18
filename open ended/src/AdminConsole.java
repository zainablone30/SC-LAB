import java.util.Map;

/**
 * Simple bank administrator console
 */
public class AdminConsole {
    private final Bank bank;
    private final String adminUser = "admin";
    private final String adminPass = "password";

    public AdminConsole(Bank bank) {
        this.bank = bank;
    }

    public boolean loginAdmin(String user, String pass) {
        return adminUser.equals(user) && adminPass.equals(pass);
    }

    public void viewAllCustomers() {
        System.out.println("---- All Customers ----");
        for (Map.Entry<String, Customer> e : bank.getAllCustomers().entrySet()) {
            Customer c = e.getValue();
            System.out.println(c);
            for (BankAccount a : c.getAccounts()) System.out.println("   " + a);
        }
    }

    public void viewAllAccounts() {
        System.out.println("---- All Accounts ----");
        for (Map.Entry<String, BankAccount> e : bank.getAllAccounts().entrySet()) {
            BankAccount a = e.getValue();
            System.out.println(a.getAccountType() + " | " + a.getAccountNumber() + " | Customer: " + a.getCustomerId() + " | Balance: " + a.getBalance() + " | Status: " + a.getStatus());
        }
    }

    public boolean createSavingsAccount(String accountNumber, String customerId, double initialBalance, double minimumBalance) {
        SavingsAccount acc = bank.createSavingsAccount(accountNumber, customerId, initialBalance, minimumBalance);
        return acc != null;
    }

    public boolean createCheckingAccount(String accountNumber, String customerId, double initialBalance, double overdraftLimit) {
        CheckingAccount acc = bank.createCheckingAccount(accountNumber, customerId, initialBalance, overdraftLimit);
        return acc != null;
    }

    /**
     * Create/register a new customer. Returns false if customerId already exists.
     */
    public boolean createCustomer(String customerId, String name, String pin) {
        if (bank.getCustomerById(customerId) != null) return false;
        bank.registerCustomer(customerId, name, pin);
        return true;
    }

    public boolean unblockCustomerATM(String customerId) {
        Customer c = bank.getCustomerById(customerId);
        if (c == null) return false;
        c.unblockAtm();
        return true;
    }

    public boolean unblockAccount(String accountNumber) {
        BankAccount a = bank.getAccountByNumber(accountNumber);
        if (a == null) return false;
        a.setStatus(AccountStatus.ACTIVE);
        return true;
    }
}
