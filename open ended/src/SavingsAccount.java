/**
 * Savings account with minimum balance enforcement
 */
public class SavingsAccount extends BankAccount {
    private final double minimumBalance;

    public SavingsAccount(String accountNumber, String customerId, double initialBalance, double minimumBalance) {
        super(accountNumber, customerId, initialBalance);
        this.minimumBalance = minimumBalance;
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
        if (balance - amount < minimumBalance) {
            addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.FAILED_INSUFFICIENT_FUNDS, "Minimum balance violation"));
            throw new InsufficientFundsException("Withdrawal would breach minimum balance");
        }
        balance -= amount;
        addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.SUCCESS, "Withdrawal"));
    }

    @Override
    public String getAccountType() { return "Savings"; }
}

