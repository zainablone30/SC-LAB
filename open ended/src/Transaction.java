/**
 * Records a financial transaction.
 */
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String fromAccount; // may be null for deposits
    private final String toAccount;   // may be null for withdrawals
    private final TransactionStatus status;
    private final String message;

    public Transaction(TransactionType type, double amount, String fromAccount, String toAccount, TransactionStatus status, String message) {
        this.transactionId = UUID.randomUUID().toString();
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.status = status;
        this.message = message;
    }

    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getFromAccount() { return fromAccount; }
    public String getToAccount() { return toAccount; }
    public TransactionStatus getStatus() { return status; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s -> %s | %.2f | %s | %s",
                timestamp, transactionId, fromAccount == null ? "-" : fromAccount,
                toAccount == null ? "-" : toAccount, amount, type, status + (message != null ? (" (" + message + ")") : ""));
    }
}

