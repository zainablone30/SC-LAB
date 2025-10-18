import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        // Initialize with sample data to make testing easier; users can still create accounts via admin.
        bank.initSampleData();

        ATM atm = new ATM(bank);
        AdminConsole admin = new AdminConsole(bank);

        try (Scanner sc = new Scanner(System.in)) {
            boolean exit = false;
            while (!exit) {
                System.out.println("\n===== Welcome to the Java Bank System =====");
                System.out.println("1) Customer Login (ATM)");
                System.out.println("2) Bank Administrator");
                System.out.println("3) Exit");
                System.out.print("Choose an option: ");
                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1":
                        handleCustomerLogin(bank, atm, sc);
                        break;
                    case "2":
                        handleAdminLogin(admin, sc);
                        break;
                    case "3":
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice, please try again.");
                }
            }
            System.out.println("Thank you for using Java Bank System. Goodbye.");
        }
    }

    private static void handleCustomerLogin(Bank bank, ATM atm, Scanner sc) {
        System.out.print("Enter customer ID: ");
        String cid = sc.nextLine().trim();
        System.out.print("Enter PIN: ");
        String pin = sc.nextLine().trim();
        Customer customer;
        try {
            customer = atm.login(cid, pin);
        } catch (AccountBlockedException e) {
            System.out.println("Login failed: " + e.getMessage());
            return;
        }
        if (customer == null) {
            System.out.println("Invalid customer ID or PIN.");
            return;
        }
        System.out.println("Welcome, " + customer.getName());
        customerMenu(customer, atm, sc);
    }

    private static void customerMenu(Customer customer, ATM atm, Scanner sc) {
        boolean logout = false;
        while (!logout) {
            System.out.println("\n--- Customer Menu for " + customer.getName() + " ---");
            System.out.println("1) List Accounts");
            System.out.println("2) Select Account");
            System.out.println("3) Logout");
            System.out.print("Choose: ");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    for (BankAccount a : customer.getAccounts()) System.out.println(a);
                    break;
                case "2":
                    System.out.print("Enter account number: ");
                    String accNo = sc.nextLine().trim();
                    BankAccount acc = null;
                    for (BankAccount a : customer.getAccounts()) if (a.getAccountNumber().equals(accNo)) acc = a;
                    if (acc == null) {
                        System.out.println("Account not found for this customer.");
                    } else {
                        accountOperations(customer, acc, atm, sc);
                    }
                    break;
                case "3":
                    logout = true;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private static void accountOperations(Customer customer, BankAccount acc, ATM atm, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\nAccount: " + acc.getAccountNumber() + " | Type: " + acc.getAccountType() + " | Balance: " + String.format("%.2f", acc.getBalance()));
            System.out.println("1) Check Balance");
            System.out.println("2) Deposit");
            System.out.println("3) Withdraw");
            System.out.println("4) View Transaction History");
            System.out.println("5) Transfer to my account");
            System.out.println("6) Transfer to another account (by account number)");
            System.out.println("7) Back");
            System.out.print("Choose: ");
            String opt = sc.nextLine().trim();
            switch (opt) {
                case "1":
                    System.out.printf("Current balance: %.2f\n", acc.getBalance());
                    break;
                case "2":
                    System.out.print("Enter amount to deposit: ");
                    try {
                        double amt = Double.parseDouble(sc.nextLine().trim());
                        atm.deposit(acc, amt);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount");
                    }
                    break;
                case "3":
                    System.out.print("Enter amount to withdraw: ");
                    try {
                        double amt = Double.parseDouble(sc.nextLine().trim());
                        atm.withdraw(acc, amt);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount");
                    }
                    break;
                case "4":
                    if (acc.getTransactions().isEmpty()) System.out.println("No transactions yet.");
                    else for (Transaction t : acc.getTransactions()) System.out.println(t);
                    break;
                case "5":
                    System.out.println("Your accounts:");
                    for (BankAccount a : customer.getAccounts()) System.out.println(a.getAccountNumber() + " (" + a.getAccountType() + ")");
                    System.out.print("Enter destination account number (must be one of your accounts): ");
                    String dst = sc.nextLine().trim();
                    if (dst.equals(acc.getAccountNumber())) { System.out.println("Cannot transfer to the same account"); break; }
                    boolean found = false;
                    for (BankAccount a : customer.getAccounts()) if (a.getAccountNumber().equals(dst)) found = true;
                    if (!found) { System.out.println("Destination account not found among your accounts"); break; }
                    System.out.print("Enter amount to transfer: ");
                    try {
                        double amt = Double.parseDouble(sc.nextLine().trim());
                        TransactionStatus st = atm.transferWithin(acc.getAccountNumber(), dst, amt);
                        System.out.println("Transfer result: " + st);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount");
                    }
                    break;
                case "6":
                    System.out.print("Enter destination account number: ");
                    String outAcc = sc.nextLine().trim();
                    System.out.print("Enter amount to transfer: ");
                    try {
                        double amt = Double.parseDouble(sc.nextLine().trim());
                        TransactionStatus st = atm.transferWithin(acc.getAccountNumber(), outAcc, amt);
                        System.out.println("Transfer result: " + st);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid amount");
                    }
                    break;
                case "7":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private static void handleAdminLogin(AdminConsole admin, Scanner sc) {
        System.out.print("Admin username: ");
        String user = sc.nextLine().trim();
        System.out.print("Admin password: ");
        String pass = sc.nextLine().trim();
        if (!admin.loginAdmin(user, pass)) {
            System.out.println("Invalid admin credentials");
            return;
        }
        System.out.println("Admin logged in");
        adminMenu(admin, sc);
    }

    private static void adminMenu(AdminConsole admin, Scanner sc) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Administrator Menu ---");
            System.out.println("1) View All Customers");
            System.out.println("2) View All Accounts");
            System.out.println("3) Create New Customer");
            System.out.println("4) Create New Savings Account for existing customer");
            System.out.println("5) Create New Checking Account for existing customer");
            System.out.println("6) Unblock Customer ATM access");
            System.out.println("7) Unblock Account");
            System.out.println("8) Back");
            System.out.print("Choose: ");
            String opt = sc.nextLine().trim();
            switch (opt) {
                case "1":
                    admin.viewAllCustomers();
                    break;
                case "2":
                    admin.viewAllAccounts();
                    break;
                case "3":
                    // Create new customer
                    System.out.print("Enter new customer ID: ");
                    String newCid = sc.nextLine().trim();
                    System.out.print("Enter customer name: ");
                    String newName = sc.nextLine().trim();
                    System.out.print("Enter initial PIN (e.g. 0000): ");
                    String newPin = sc.nextLine().trim();
                    boolean created = admin.createCustomer(newCid, newName, newPin);
                    System.out.println(created ? "Customer created successfully" : "Failed to create customer (ID may already exist)");
                    break;
                case "4":
                    System.out.print("Enter existing customer ID: ");
                    String cid = sc.nextLine().trim();
                    System.out.print("Enter new account number: ");
                    String accNo = sc.nextLine().trim();
                    System.out.print("Enter initial balance: ");
                    try {
                        double init = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("Enter minimum balance: ");
                        double min = Double.parseDouble(sc.nextLine().trim());
                        boolean ok = admin.createSavingsAccount(accNo, cid, init, min);
                        System.out.println(ok ? "Savings account created" : "Failed to create savings account (customer may not exist)");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid numeric input");
                    }
                    break;
                case "5":
                    System.out.print("Enter existing customer ID: ");
                    String cid2 = sc.nextLine().trim();
                    System.out.print("Enter new account number: ");
                    String accNo2 = sc.nextLine().trim();
                    System.out.print("Enter initial balance: ");
                    try {
                        double init2 = Double.parseDouble(sc.nextLine().trim());
                        System.out.print("Enter overdraft limit: ");
                        double od = Double.parseDouble(sc.nextLine().trim());
                        boolean ok2 = admin.createCheckingAccount(accNo2, cid2, init2, od);
                        System.out.println(ok2 ? "Checking account created" : "Failed to create checking account (customer may not exist)");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid numeric input");
                    }
                    break;
                case "6":
                    System.out.print("Enter customer ID to unblock ATM: ");
                    String uc = sc.nextLine().trim();
                    boolean r = admin.unblockCustomerATM(uc);
                    System.out.println(r ? "Customer ATM unblocked" : "Customer not found");
                    break;
                case "7":
                    System.out.print("Enter account number to unblock: ");
                    String ua = sc.nextLine().trim();
                    boolean ra = admin.unblockAccount(ua);
                    System.out.println(ra ? "Account unblocked" : "Account not found");
                    break;
                case "8":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }
}
