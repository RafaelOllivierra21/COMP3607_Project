# Use case 2: partially correct, medium activity

A student writes a bank account class with two compile mistakes. They fix them one at a time, then the code runs but crashes on an edge case because it divides by zero when there are no deposits yet.

## Before you start

Close BlueJ completely and start it fresh so the report only has this session in it.

## Steps

1. In BlueJ click Project then New Project and call it UseCase2_Partial.
2. Wait about 10 seconds so the plugin can see the class being created.
3. Click New Class and name it BankAccount.
4. Open BankAccount, select all the code and replace it with the code in start/BankAccount.java.
5. Click Compile. It fails with ';' expected because the first line in deposit is missing a semicolon.
6. Add the semicolon at the end of balance = balance + amount and click Compile again.
7. It fails with cannot find symbol because averageDeposit uses depositcount with a small c.
8. Change depositcount to depositCount and click Compile again. It compiles. The code now matches fixed/BankAccount.java.
9. Right click BankAccount and choose new BankAccount. Enter "Ben" for the owner and 100 for the balance.
10. Right click the red account object and run averageDeposit. It crashes with an ArithmeticException (divide by zero) because there are no deposits yet.
11. Run deposit with 50, then run averageDeposit again. This time it works and returns 50.
12. Click Tools then Export telemetry report to save a snapshot.
13. Click Project then Quit to save the final report.

## Where the reports are

In the UseCase2_Partial project folder, named like UseCase2_Partial_final_2026-11-18_10-45-12.json and .pdf.

## What the report should show

* Project: UseCase2_Partial
* Compile attempts 3, successful compiles 1, success rate 33.3%
* Errors by code: E01 cannot find symbol 1 and E02 missing semicolon 1, with a bar for each in the chart
* Program runs: total 4, normal 3, exception 1
* Timeline: Class created BankAccount, then Method defined for deposit, averageDeposit and getBalance
* More edits than use case 1 because of the fixes

## Result

Partial. The student got the code to compile after two failed attempts, and it works in normal use, but one run crashed with an exception on an edge case they did not handle.