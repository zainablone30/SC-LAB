/**
 * Checking account supporting overdraft
 */
public class CheckingAccount extends BankAccount {
    private final double overdraftLimit;

    public CheckingAccount(String accountNumber, String customerId, double initialBalance, double overdraftLimit) {
        super(accountNumber, customerId, initialBalance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void deposit(double amount) throws AccountBlockedException {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountBlockedException("Account is not active");
        }
        if (amount <= 0) return;
        balance += amount;
        addTransaction(new Transaction(TransactionType.DEPOSIT, amount, null, accountNumber, TransactionStatus.SUCCESS, "Deposit"));
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException, AccountBlockedException {
        if (status != AccountStatus.ACTIVE) {
            addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.FAILED_ACCOUNT_BLOCKED, "Account blocked"));
            throw new AccountBlockedException("Account is not active");
        }
        if (amount <= 0) return;
        if (balance - amount < -overdraftLimit) {
            addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.FAILED_INSUFFICIENT_FUNDS, "Overdraft limit exceeded"));
            throw new InsufficientFundsException("Overdraft limit exceeded");
        }
        balance -= amount;
        addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.SUCCESS, "Withdrawal"));
    }

    @Override
    public String getAccountType() { return "Checking"; }
}

