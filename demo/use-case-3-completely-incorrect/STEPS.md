# Use case 3: completely incorrect, high activity

A struggling student writes a library class. Every time they fix one error they break something else, so after 7 compile attempts and 7 different errors the class still does not compile.

At every attempt the code has exactly one error, so each compile records exactly one error code.

## Before you start

Close BlueJ completely and start it fresh so the report only has this session in it.

## Steps

1. In BlueJ click Project then New Project and call it UseCase3_Incorrect.
2. Wait about 10 seconds so the plugin can see the class being created.
3. Click New Class and name it Library.
4. Open Library, select all the code and replace it with the code in Library.java from this folder.

### Attempt 1, expected E02 ';' expected

Click Compile. It fails because `count = count + 1` in addBook has no semicolon.

### Attempt 2, expected E01 cannot find symbol

Add the semicolon. While there, change `books[count] = title;` to `book[count] = title;` (the student renames it by mistake). Compile.

### Attempt 3, expected E04 incompatible types

Change `book[count]` back to `books[count]`. In countBooks change `return count;` to:

```java
return "count";
```

Compile.

### Attempt 4, expected E05 missing return statement

Replace the inside of countBooks with:

```java
int total = count;
```

Compile.

### Attempt 5, expected E06 might not have been initialized

Replace the inside of countBooks with:

```java
int total;
return total;
```

Compile.

### Attempt 6, expected E08 unreachable statement

Replace the inside of countBooks with:

```java
return count;
count = 0;
```

Compile.

### Attempt 7, expected E07 already defined

Fix countBooks back to just `return count;`. Then in hasBook add this line right above the for loop:

```java
int i = 0;
```

Compile. The student gives up here.

### Save the reports

Click Tools then Export telemetry report to save a snapshot, then click Project then Quit to save the final report.

## Where the reports are

In the UseCase3_Incorrect project folder, named like UseCase3_Incorrect_final_2026-11-18_11-02-40.json and .pdf.

## What the report should show

* Project: UseCase3_Incorrect
* Compile attempts 7, successful compiles 0, success rate 0.0%
* Errors by code: E01, E02, E04, E05, E06, E07 and E08, once each, with 7 bars in the chart
* Program runs: No methods or constructors were run this session, because the class never compiled
* Timeline: Class created Library only. No methods are listed because they are only recorded after a successful compile
* The most edits of the three use cases

## Result

Failure. The student made 7 attempts and hit 7 different errors but the class never compiled, so nothing could be run.