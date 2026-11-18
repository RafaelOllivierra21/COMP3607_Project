/**
 * A bank account that keeps a balance and tracks deposits.
 * Use case 2 after both compile mistakes are fixed. averageDeposit still
 * crashes if it is called before any deposits, which is the runtime bug.
 */
public class BankAccount
{
    private String owner;
    private int balance;
    private int depositCount;
    private int totalDeposited;

    public BankAccount(String owner, int balance)
    {
        this.owner = owner;
        this.balance = balance;
    }

    public void deposit(int amount)
    {
        balance = balance + amount;
        depositCount = depositCount + 1;
        totalDeposited = totalDeposited + amount;
    }

    public int averageDeposit()
    {
        return totalDeposited / depositCount;
    }

    public int getBalance()
    {
        return balance;
    }
}