import java.util.Scanner;

/**
 * ATM interface that interacts with the Bank. Provides both programmatic and simple
 * interactive methods for demo purposes.
 */
public class ATM {
    private final Bank bank;

    public ATM(Bank bank) {
        this.bank = bank;
    }

    public Customer login(String customerId, String pin) throws AccountBlockedException {
        Customer c = bank.getCustomerById(customerId);
        if (c == null) return null;
        if (c.isAtmBlocked()) throw new AccountBlockedException("Customer ATM access is blocked");
        boolean ok = c.verifyPin(pin);
        if (!ok) {
            if (c.isAtmBlocked()) {
                throw new AccountBlockedException("Customer ATM access has been blocked after too many failed attempts");
            }
            return null;
        }
        return c;
    }

    public BankAccount selectAccount(Customer c, String accountNumber) {
        for (BankAccount a : c.getAccounts()) {
            if (a.getAccountNumber().equals(accountNumber)) return a;
        }
        return null;
    }

    public TransactionStatus deposit(BankAccount account, double amount) {
        try {
            account.deposit(amount);
            Transaction t = account.getTransactions().get(account.getTransactions().size()-1);
            printReceipt(t, account.getBalance());
            return TransactionStatus.SUCCESS;
        } catch (AccountBlockedException e) {
            System.out.println("Deposit failed: " + e.getMessage());
            return TransactionStatus.FAILED_ACCOUNT_BLOCKED;
        }
    }

    public TransactionStatus withdraw(BankAccount account, double amount) {
        try {
            account.withdraw(amount);
            Transaction t = account.getTransactions().get(account.getTransactions().size()-1);
            printReceipt(t, account.getBalance());
            return TransactionStatus.SUCCESS;
        } catch (AccountBlockedException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
            return TransactionStatus.FAILED_ACCOUNT_BLOCKED;
        } catch (InsufficientFundsException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
            return TransactionStatus.FAILED_INSUFFICIENT_FUNDS;
        }
    }

    public TransactionStatus transferWithin(String fromAccountNumber, String toAccountNumber, double amount) {
        return bank.transfer(fromAccountNumber, toAccountNumber, amount);
    }

    public void printReceipt(Transaction t, double newBalance) {
        System.out.println("----- RECEIPT -----");
        System.out.println("Transaction ID: " + t.getTransactionId());
        System.out.println("Type: " + t.getType());
        System.out.printf("Amount: %.2f\n", t.getAmount());
        System.out.println("From: " + (t.getFromAccount() == null ? "-" : t.getFromAccount()));
        System.out.println("To: " + (t.getToAccount() == null ? "-" : t.getToAccount()));
        System.out.println("Status: " + t.getStatus());
        System.out.printf("New Balance: %.2f\n", newBalance);
        System.out.println("-------------------");
    }

    // Simple interactive session (not used in smoke demo)
    public void interactive() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Welcome to the ATM");
            System.out.print("Enter customer ID: ");
            String cid = sc.nextLine().trim();
            System.out.print("Enter PIN: ");
            String pin = sc.nextLine().trim();
            Customer c;
            try {
                c = login(cid, pin);
            } catch (AccountBlockedException e) {
                System.out.println(e.getMessage());
                return;
            }
            if (c == null) {
                System.out.println("Invalid credentials");
                return;
            }
            System.out.println("Welcome, " + c.getName());
            boolean exit = false;
            while (!exit) {
                System.out.println("1) List accounts\n2) Select account\n3) Exit");
                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1":
                        for (BankAccount a : c.getAccounts()) System.out.println(a);
                        break;
                    case "2":
                        System.out.print("Enter account number: ");
                        String accNo = sc.nextLine().trim();
                        BankAccount acc = selectAccount(c, accNo);
                        if (acc == null) System.out.println("Invalid account");
                        else accountMenu(sc, acc);
                        break;
                    default:
                        exit = true;
                }
            }
        }
    }

    private void accountMenu(Scanner sc, BankAccount acc) {
        boolean back = false;
        while (!back) {
            System.out.println("Account: " + acc.getAccountNumber() + " | Balance: " + acc.getBalance());
            System.out.println("1) Check Balance\n2) Deposit\n3) Withdraw\n4) View Transactions\n5) Back");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1": System.out.println("Balance: " + acc.getBalance()); break;
                case "2":
                    System.out.print("Amount: ");
                    double d = Double.parseDouble(sc.nextLine().trim());
                    deposit(acc, d);
                    break;
                case "3":
                    System.out.print("Amount: ");
                    double w = Double.parseDouble(sc.nextLine().trim());
                    withdraw(acc, w);
                    break;
                case "4":
                    for (Transaction t : acc.getTransactions()) System.out.println(t);
                    break;
                default: back = true;
            }
        }
    }
}

