# BlueJ Telemetry Plugin

A BlueJ extension that quietly records how a student works on a programming task and turns it into a JSON and PDF report. It tracks editing and idle time, compiles and compiler errors, program runs, and when each class and method first appeared. A lecturer can then see how a student got to their answer, not just the final code.

This is our group project for COMP 3607 Object Oriented Programming II at UWI St. Augustine (2026).

| Team member | Student ID | Part |
|---|---|---|
| Jon-Paul Auguste | 816044510 | Part 1: collecting the telemetry |
| Rafael Ollivierra | 816039648 | Part 2: JSON and PDF reports |
| Kristopher Boodoosingh | 816035267 | Part 3: testing |

## Documentation

The full documentation is in the [project wiki](https://github.com/RafaelOllivierra21/COMP3607_Project/wiki).

| Page | What is on it |
|---|---|
| [Home](https://github.com/RafaelOllivierra21/COMP3607_Project/wiki) | Overview, scope and limitations |
| [Setup](https://github.com/RafaelOllivierra21/COMP3607_Project/wiki/Setup) | Full build, install and troubleshooting steps |
| [Design](https://github.com/RafaelOllivierra21/COMP3607_Project/wiki/Design) | Class diagrams, the five design patterns and SOLID |
| [API Reference](https://github.com/RafaelOllivierra21/COMP3607_Project/wiki/API-Reference) | Every class, field and method |
| [Testing](https://github.com/RafaelOllivierra21/COMP3607_Project/wiki/Testing) | The unit tests, coverage and the demo video |

## What it records

- **Projects:** when a project is opened and closed.
- **Classes and methods:** when a class is created, renamed or deleted, and the first time each method shows up after a compile.
- **Editing:** every open editor is checked every 5 seconds. A check where the text changed counts as one edit, and a gap of 2 minutes or more with no edits counts as idle time.
- **Compiles:** every compile the student starts, whether it worked, and each compiler error turned into a short code (E01 to E18, or E99 for anything else).
- **Runs:** every method or constructor run from BlueJ and whether it ended normally, with an exception or was stopped.

## The reports

Reports are saved in the folder of the BlueJ project being worked on, and each file name starts with the project name.

| When | Files |
|---|---|
| Tools menu, **Export telemetry report** | `<Project>_snapshot_<date>_<time>.json` and `.pdf` |
| Every 60 seconds, in case BlueJ crashes | `<Project>_autosave.json` (overwritten each time) |
| When BlueJ closes | `<Project>_final_<date>_<time>.json` and `.pdf` |

The **JSON** has the session times, active and idle time, edit count and edits per active minute, compile totals, the count for each error code, the time between compiles, a run summary and every event that was recorded.

The **PDF** fits on at most 2 A4 pages. It has an activity table, an errors table with a bar chart, a program runs table and a class and method timeline.

## Quick start

You need Windows, BlueJ 6.0.1, JDK 21 or newer and Maven 3.9 or newer. The [Setup page](https://github.com/RafaelOllivierra21/COMP3607_Project/wiki/Setup) has every step in detail.

```powershell
git clone https://github.com/RafaelOllivierra21/COMP3607_Project.git
cd COMP3607_Project

# BlueJ's extensions API is not on Maven Central, so copy it in once
mkdir lib
Copy-Item "C:\Program Files\BlueJ\lib\bluej.jar" lib\

# build the plugin jar (Gson and OpenPDF are packed inside it)
mvn clean package

# close BlueJ first, then install the jar
Copy-Item target\bluej-telemetry-1.0.jar "$env:USERPROFILE\bluej\extensions2\" -Force
```

Start BlueJ again and check that it loaded under **Help**, then **Installed Extensions**. Open a project and work as normal. Use **Tools**, then **Export telemetry report** for a report at any time.

## Tests, coverage and JavaDoc

```powershell
# run the unit tests, the JaCoCo coverage report is then at target\site\jacoco\index.html
mvn clean test

# JavaDoc pages, open target\reports\apidocs\index.html
mvn clean javadoc:javadoc
```

The tests are in `src/test/java` and do not need BlueJ to be running. The [Testing page](https://github.com/RafaelOllivierra21/COMP3607_Project/wiki/Testing) explains what each test class checks and shows the coverage numbers.

## Demo use cases

The [`demo`](demo) folder has the three use cases from our demo video, from fully correct to completely incorrect. Each one has its starting code and a `STEPS.md` that says exactly what to do and what the report should show.

## Design patterns

| Pattern | Where it is used |
|---|---|
| Observer | The monitors listen to BlueJ's compile, run, class and project events |
| Facade | `TelemetryExtension` is the one entry point BlueJ talks to |
| Strategy | `JsonExporter` and `PdfExporter` behind the `Exporter` interface |
| Template Method | `AbstractExporter.export()` fixes the order every report is written in |
| Command | `ExportCommand` jobs run one at a time by `ExportRunner` on a background thread |

Each one is explained with a class diagram and code on the [Design page](https://github.com/RafaelOllivierra21/COMP3607_Project/wiki/Design).

## Project layout

```
src/main/java/comp3607/telemetry/          collecting the telemetry (Part 1)
src/main/java/comp3607/telemetry/report/   JSON and PDF reports (Part 2)
src/test/java/comp3607/telemetry/          unit tests (Part 3)
demo/                                      the three demo use cases
lib/                                       bluej.jar goes here, not in Git
```

## Known limitations

- Close and restart BlueJ between sessions. The plugin treats one BlueJ run as one session, so a report covers everything since BlueJ started.
- BlueJ has no keystroke events, so typing is measured as edits per active minute instead of actual keystrokes.
- The text a program prints to the terminal is not captured, only how each run ended and any simple return value.
- BlueJ does not give a method name for constructor runs or runs that throw an exception, so these show as `Class.unknown`.
- BlueJ does not know who the student is, so the student ID in the reports is a placeholder.
- New classes are found by a 5 second scan, but methods are recorded the moment a class compiles. If a class is made and compiled within the same 5 seconds, or deleted and made again, the timeline can list its methods before "Class created".
