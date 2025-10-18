Design Document for Java Bank System

Overview
--------
This document describes the architecture and design decisions for the Java Bank System implemented in the `src/` folder. The system simulates a bank with multiple customers and account types, a user-facing ATM interface, and a basic administrator console. Key features include:

- Customer management (register, PIN-based ATM login with blocking)
- Multiple account types (Savings and Checking)
- Transaction logging per account
- ATM operations (balance, deposit, withdrawal, intra- and cross-customer transfer)
- Administrator functions (view customers/accounts, create customers/accounts, unblock ATM access and accounts)

UML-like Class Diagram (text)
-----------------------------
Note: this is a lightweight textual UML representation focusing on key attributes and methods.

Bank
- customers: HashMap<String, Customer>
- accounts: HashMap<String, BankAccount>
+ registerCustomer(customerId, name, pin): Customer
+ createSavingsAccount(accountNumber, customerId, initialBalance, minimumBalance): SavingsAccount
+ createCheckingAccount(accountNumber, customerId, initialBalance, overdraftLimit): CheckingAccount
+ getCustomerById(customerId): Customer
+ getAccountByNumber(accountNumber): BankAccount
+ transfer(fromAccountNumber, toAccountNumber, amount): TransactionStatus
+ initSampleData()

Customer
- customerId: String
- name: String
- pin: String
- accounts: ArrayList<BankAccount>
- failedPinAttempts: int
- atmBlocked: boolean
+ verifyPin(attempt): boolean
+ blockAtm()
+ unblockAtm()
+ addAccount(a: BankAccount)

BankAccount (abstract)
- accountNumber: String
- customerId: String
- balance: double
- status: AccountStatus
- transactions: ArrayList<Transaction>
+ deposit(amount): void
+ withdraw(amount): void
+ getTransactions(): ArrayList<Transaction>
+ getAccountType(): String

SavingsAccount extends BankAccount
- minimumBalance: double
+ deposit(amount)
+ withdraw(amount) // enforces minimumBalance
+ getAccountType(): "Savings"

CheckingAccount extends BankAccount
- overdraftLimit: double
+ deposit(amount)
+ withdraw(amount) // enforces overdraft limit
+ getAccountType(): "Checking"

Transaction
- transactionId: String (UUID)
- type: TransactionType
- amount: double
- timestamp: LocalDateTime
- fromAccount: String
- toAccount: String
- status: TransactionStatus
- message: String
+ toString(): String

ATM
- bank: Bank
+ login(customerId, pin): Customer
+ selectAccount(customer, accountNumber): BankAccount
+ deposit(account, amount)
+ withdraw(account, amount)
+ transferWithin(fromAccountNumber, toAccountNumber, amount)
+ interactive() // optional CLI

AdminConsole
- bank: Bank
+ loginAdmin(user, pass): boolean
+ viewAllCustomers()
+ viewAllAccounts()
+ createCustomer(customerId, name, pin)
+ createSavingsAccount(...)
+ createCheckingAccount(...)
+ unblockCustomerATM(customerId)
+ unblockAccount(accountNumber)

Enums
- TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER }
- TransactionStatus { SUCCESS, FAILED_INSUFFICIENT_FUNDS, FAILED_INVALID_ACCOUNT, FAILED_ACCOUNT_BLOCKED, FAILED_OTHER }
- AccountStatus { ACTIVE, BLOCKED, CLOSED }

Data Structure Justification
----------------------------
- Bank.customers: HashMap<String, Customer>
  - Rationale: fast O(1) lookup by customer ID. The system frequently retrieves customers for login, admin lookups, and account creation.
- Bank.accounts: HashMap<String, BankAccount>
  - Rationale: fast O(1) lookup by account number is required for transfers and admin operations.
- Customer.accounts: ArrayList<BankAccount>
  - Rationale: customers typically have a small number of accounts; ArrayList offers simple iteration and indexing (used for display and account selection).
- BankAccount.transactions: ArrayList<Transaction>
  - Rationale: preserves chronological order, easy to append; typical usage is to show the transaction history for an account.

These structures balance simplicity and performance for the intended in-memory simulation. For a production system, persistent storage (RDBMS/NoSQL) would replace in-memory maps and lists.

Inheritance & Polymorphism
--------------------------
- `BankAccount` is an abstract base class containing shared state and behavior (accountNumber, balance, status, transactions) and abstract operations `deposit()` and `withdraw()`.
- `SavingsAccount` and `CheckingAccount` extend `BankAccount` and implement account-specific rules:
  - `SavingsAccount.withdraw()` enforces a `minimumBalance` (withdrawals that would drop balance below `minimumBalance` are rejected).
  - `CheckingAccount.withdraw()` permits negative balances up to `-overdraftLimit` (withdrawal rejected if it would exceed overdraft limit).
- Polymorphism is used so `ATM` and `Bank` can interact with a `BankAccount` reference without needing to know the concrete subclass. This allows adding new account types later with minimal changes to ATM/Bank code.

Transaction Logging
-------------------
- Every financial operation (deposit, withdrawal, transfer) creates a `Transaction` object with:
  - unique transactionId (UUID)
  - type (TransactionType)
  - amount
  - timestamp (java.time.LocalDateTime)
  - from/to account numbers (nullable where not applicable)
  - status (TransactionStatus) and an optional message
- Each `BankAccount` keeps an `ArrayList<Transaction>` appended in chronological order. Transfers append the same successful Transaction object to both source and destination accounts to keep logs consistent.

Error Handling Strategy
-----------------------
- Custom exceptions are used for business rule violations:
  - `InsufficientFundsException` — thrown when a withdrawal/transfer would violate a minimum balance or overdraft rules.
  - `AccountBlockedException` — thrown when operations are attempted on a blocked account or when ATM login is attempted with a blocked customer.
  - `InvalidAccountException` — reserved for invalid account references (currently not extensively used but available for extension).
- Methods that perform state changes catch exceptions to create appropriate `Transaction` entries with failed status and messages.
- Input parsing uses try/catch for `NumberFormatException` to validate numeric inputs in CLI flows.

Security & ATM Blocking Behavior
--------------------------------
- Customers authenticate at the ATM using `customerId` and a `PIN` stored in plain text in the `Customer` object for simplicity (assumption: this is a lab simulation; production systems must use secure password/PIN hashing and secure storage).
- `Customer` tracks `failedPinAttempts`. After `MAX_PIN_ATTEMPTS` (3) consecutive wrong attempts, `atmBlocked` is set to `true` and further login attempts throw `AccountBlockedException`.
- The block is ATM-level (prevents ATM login) and must be cleared by an administrator using `AdminConsole.unblockCustomerATM(customerId)`; admin can also unblock an account via `unblockAccount(accountNumber)`.

ATM and Admin Flows (high level)
--------------------------------
- ATM (customer flow):
  1. Customer enters ID and PIN — `ATM.login()` uses `Customer.verifyPin()` which increments failed attempts and blocks on N failures.
  2. After login, customer lists/selects accounts.
  3. Available operations on an account: check balance, deposit, withdraw, view transaction history, transfer to own accounts, transfer to other accounts by account number.
  4. Transfers call `Bank.transfer()` which performs validation (accounts exist, statuses active, source funds sufficiency) and records transactions.
  5. Successful deposit/withdraw/transfer produce a printed text receipt containing transaction id, type, amount, and new balance.

- Admin flow:
  1. Admin logs in using a hardcoded username/password ("admin"/"password").
  2. Admin can view all customers and their accounts, view all accounts, create a new customer, create savings/checking accounts for existing customers, unblock customers' ATM access, and unblock individual accounts.
  3. Admin-created customers default PINs are configurable at creation time (the sample data sets PINs to "0000").

Implementation Notes & Assumptions
--------------------------------
- The system is an in-memory simulation (no persistence). All data is lost when the process exits.
- PINs are stored in plaintext (for lab purposes). In real systems, use hashing and secure key management.
- Customer IDs and account numbers are strings; uniqueness is the admin's responsibility when creating entries via CLI. The code prevents duplicate customer creations by checking the `Bank`'s `customers` map.
- ATM blocking is manual to unblock (administrator) and affects only ATM login (not internal account `status` unless admin unblocks account status explicitly).
- Transaction IDs use UUIDs for uniqueness; timestamps use java.time.LocalDateTime.
- The sample initialization uses Pakistani names (e.g., Muhammad Ali, Ayesha Khan, Ahmed Raza) and sample customers have default PIN "0000", per request.

Edge cases covered
------------------
- Deposits of non-positive amounts are ignored (no-op). You may extend validation to reject these with an error message.
- Withdrawals check account-specific rules and log failed transactions.
- Transfers validate both source and destination existence and activity status; destination blocked leads to rollback where possible.
- Admin actions validate entity existence and return boolean status to the CLI to present friendly messages.

Extensibility & Next Steps
--------------------------
- Persist data to a database (JDBC or an ORM) so state survives restarts.
- Use secure PIN storage (salted hash) and add account ownership verification for transfers.
- Add role-based authentication and stronger admin auth (config file or environment variable for credentials).
- Add unit tests to verify business rules (withdrawal constraints, transfers, admin functions).

Files of interest
-----------------
- src/Bank.java — central repository and orchestrator
- src/Customer.java — customer model and ATM security logic
- src/BankAccount.java, src/SavingsAccount.java, src/CheckingAccount.java — account hierarchy
- src/Transaction.java — transaction log model
- src/ATM.java — ATM interface and CLI helper methods
- src/AdminConsole.java — admin functionality
- src/Main.java — top-level interactive CLI runner

Requirements coverage (brief)
-----------------------------
- Customer management: Done (Customer class, createCustomer in AdminConsole, registerCustomer in Bank)
- Diverse account types: Done (SavingsAccount, CheckingAccount) with business rules
- Transaction history/logging: Done (Transaction class and account-level logs)
- ATM functionality: Done (login with PIN and blocking, account selection, check balance, deposit, withdraw, view transactions, print receipts)
- Bank backend: Done (Bank class with maps, initSampleData)
- Transfers: Done (intra-customer via ATM, cross-customer via account number transfers through Bank.transfer)
- Admin interface: Done (view/create accounts, unblock customer/account, create customers)

If you want, I can:
- Add unit tests (JUnit) for key behaviors (withdrawal rules, transfers, PIN blocking).
- Add persistence (simple file-based serialization or a tiny SQLite-backed DAO).
- Improve input validation and add confirmations for destructive actions.


Document end.

