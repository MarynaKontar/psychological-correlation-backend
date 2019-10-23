package com.psycorp;

import com.psycorp.controller.api.*;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.ClassNameFilter.excludeClassNamePatterns;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * Runs all tests specified in {@link LauncherDiscoveryRequest}.
 */
public class RunAllTests {
    SummaryGeneratingListener listener = new SummaryGeneratingListener();

    public void runAll() {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(selectPackage("com.psycorp.controller.api"))
//                .filters(includeClassNamePatterns(".*Test"))
//                .filters(excludeClassNamePatterns("Abstract.*"))
//                .filters(excludeClassNamePatterns(".*User.*"))
//                .filters(excludeClassNamePatterns(".*Registration.*"))
//                .filters(excludeClassNamePatterns(".*ValueCompatibilityAnswers.*"))
//                .filters(excludeClassNamePatterns(".*Authentication.*"))
                .build();
        Launcher launcher = LauncherFactory.create();

        TestPlan testPlan = launcher.discover(request);

        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);
    }

    public static void main(String[] args) {
        RunAllTests runner = new RunAllTests();
        runner.runAll();

        TestExecutionSummary summary = runner.listener.getSummary();
//        summary.printFailuresTo(new PrintWriter(System.out));
        summary.printTo(new PrintWriter(System.out));
    }

}
