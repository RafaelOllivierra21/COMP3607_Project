/**
 * Collects telemetry from BlueJ while a student works and keeps it so that the
 * {@code report} sub-package can turn it into a JSON file and a PDF report.
 *
 * BlueJ only knows about {@link comp3607.telemetry.TelemetryExtension}, which is the
 * Facade for the whole plugin. It creates the monitors, connects them to BlueJ and
 * gives them one shared {@link comp3607.telemetry.EventBuffer}.
 *
 * The main classes are:
 * <ul>
 * <li>{@link comp3607.telemetry.TelemetryExtension}, the plugin entry point and the
 * Observer of project open and close;</li>
 * <li>{@link comp3607.telemetry.ClassMonitor}, {@link comp3607.telemetry.CompileMonitor}
 * and {@link comp3607.telemetry.RunMonitor}, the Observers of BlueJ's class, compile
 * and run events;</li>
 * <li>{@link comp3607.telemetry.EditorMonitor}, which samples the open editors because
 * BlueJ gives no event for typing;</li>
 * <li>{@link comp3607.telemetry.EventBuffer} and {@link comp3607.telemetry.TelemetryEvent},
 * which store the recorded events;</li>
 * <li>{@link comp3607.telemetry.ErrorCodes}, which turns compiler messages into error codes.</li>
 * </ul>
 *
 * The monitors only record events. Writing the files is the job of the report package.
 */
package comp3607.telemetry;
