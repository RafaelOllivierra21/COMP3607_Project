# Use case 1: fully correct, low activity

A student writes a small class that is correct the first time. It compiles with no errors and every method runs normally.

## Before you start

1. Build and install the plugin (see the [Setup page](https://github.com/RafaelOllivierra21/COMP3607_Project/wiki/Setup) of the wiki) and close BlueJ completely.
2. Start BlueJ fresh for each use case so the report only has this session in it. If a step goes wrong, quit BlueJ, delete the project folder and start again. Do not delete and recreate the class in the same session.

## Steps

1. In BlueJ click Project then New Project and call it UseCase1_Correct.
2. Wait about 10 seconds. The plugin checks the project every 5 seconds and needs one check before the class exists so it can see the class being created.
3. Click New Class, name it Student, and keep Class selected.
4. Open Student, select all the code and replace it with the code in Student.java from this folder. Close the editor.
5. Click Compile. It compiles first time.
6. Right click Student and choose new Student. Enter "Ana" for the name, typed with the quote marks because it is a String, and 75 for the grade.
7. Right click the red student object and run getName, then run isPassing.
8. Click Tools then Export telemetry report to save a snapshot.
9. Click Project then Quit. The final report is saved as the plugin closes.

## Where the reports are

In the UseCase1_Correct project folder, named like UseCase1_Correct_final_2026-11-18_10-30-05.json and .pdf.

## What the report should show

* Project: UseCase1_Correct
* Compile attempts 1, successful compiles 1, success rate 100.0%
* Errors by code: No compile errors this session
* Program runs: total 3, normal 3, exception 0
* Timeline: Class created Student, then Method defined for getName, getGrade and isPassing
* A few edits and no idle time if each step is done within 2 minutes

## Result

Success. The student wrote correct code, it compiled first time and every run finished normally.