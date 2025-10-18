/** Exception thrown when an account/customer is blocked */
public class AccountBlockedException extends Exception {
    public AccountBlockedException(String message) {
        super(message);
    }
}

