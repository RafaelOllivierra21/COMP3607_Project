# Demo use cases

These three use cases are the ones shown in our demo video. Each one is a different kind of student interaction with BlueJ, from fully correct to completely incorrect, so the plugin's reports can be compared side by side.

The files in this folder are not part of the plugin. Maven only builds src/main/java, so the starting code here can have errors on purpose without breaking the build. Some of these files show up red in an IDE for that reason.

| Use case | Activity | Code | Compiles | Runs | Result |
| --- | --- | --- | --- | --- | --- |
| [1. Fully correct](use-case-1-fully-correct/STEPS.md) | Low | Student | 1 of 1 (100%) | 3 normal | Success |
| [2. Partially correct](use-case-2-partially-correct/STEPS.md) | Medium | BankAccount | 1 of 3 (33.3%), E01 and E02 | 3 normal, 1 exception | Partial |
| [3. Completely incorrect](use-case-3-completely-incorrect/STEPS.md) | High | Library | 0 of 7 (0%), 7 different codes | none | Failure |

## How to run them

1. Build and install the plugin as shown on the How to Run page of the wiki.
2. Close BlueJ completely before each use case and start it fresh. The plugin treats one BlueJ run as one session, so starting fresh keeps each report to just that use case.
3. Follow the STEPS.md file in the use case's folder.
4. The JSON and PDF reports are saved in the BlueJ project folder that the use case creates, named with the project name and the time.