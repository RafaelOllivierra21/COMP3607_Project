# Use case 2: partially correct, medium activity

A student writes a bank account class with one compile mistake. They fix it, then the code runs but crashes on an edge case because it divides by zero when there are no deposits yet.

## Before you start

Close BlueJ completely and start it fresh so the report only has this session in it. If a step goes wrong, quit BlueJ, delete the project folder and start again. Do not delete and recreate the class in the same session.

## Steps

1. In BlueJ click Project then New Project and call it UseCase2_Partial.
2. Wait about 10 seconds so the plugin can see the class being created.
3. Click New Class and name it BankAccount.
4. Open BankAccount, select all the code and replace it with the code in start/BankAccount.java. Leave the editor open.
5. Click Compile. It fails with cannot find symbol because averageDeposit uses depositcount with a small c.
6. Change depositcount to depositCount and click Compile again. It compiles. The code now matches fixed/BankAccount.java. Close the editor.
7. Right click BankAccount and choose new BankAccount. Enter "Ben" for the owner, typed with the quote marks because it is a String, and 100 for the balance.
8. Right click the red account object and run averageDeposit. It crashes with an ArithmeticException (divide by zero) because there are no deposits yet.
9. Run deposit with 50, then run averageDeposit again. This time it works and returns 50.
10. Click Tools then Export telemetry report to save a snapshot.
11. Click Project then Quit to save the final report.

## Where the reports are

In the UseCase2_Partial project folder, named like UseCase2_Partial_final_2026-11-18_18-28-27.json and .pdf.

## What the report should show

* Project: UseCase2_Partial
* Compile attempts 2, successful compiles 1, success rate 50.0%
* Errors by code: E01 cannot find symbol 1, with one bar in the chart
* Program runs: total 4, normal 3, exception 1
* Timeline: Class created BankAccount, then Method defined for deposit, averageDeposit and getBalance
* More edits than use case 1 because of the fix

## Result

Partial. The student got the code to compile after one failed attempt, and it works in normal use, but one run crashed with an exception on an edge case they did not handle.
