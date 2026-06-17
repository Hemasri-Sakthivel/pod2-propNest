import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

/**
 * Minimal JUnit Platform launcher used to run the rentDepositManagement test
 * suite from the terminal without Maven Surefire (which can't be downloaded in
 * this offline/proxy environment). Discovers every test under the module package.
 */
public class RunTests {
    public static void main(String[] args) {
        LauncherDiscoveryRequest req = request()
                .selectors(selectPackage("com.cog.propNest.module.rentDepositManagement"))
                .build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(req);

        TestExecutionSummary summary = listener.getSummary();
        PrintWriter out = new PrintWriter(System.out, true);
        summary.printTo(out);
        summary.printFailuresTo(out, 100);
        System.out.println("RESULT found=" + summary.getTestsFoundCount()
                + " succeeded=" + summary.getTestsSucceededCount()
                + " failed=" + summary.getTestsFailedCount()
                + " skipped=" + summary.getTestsSkippedCount());
        System.exit(summary.getTotalFailureCount() == 0 ? 0 : 1);
    }
}
