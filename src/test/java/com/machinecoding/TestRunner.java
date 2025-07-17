package com.machinecoding;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Comprehensive test runner for the machine coding project.
 * Executes all test suites and generates detailed reports.
 */
public class TestRunner {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("MACHINE CODING PROJECT - COMPREHENSIVE TEST SUITE");
        System.out.println("=".repeat(80));
        System.out.println("Started at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.println();
        
        TestRunner runner = new TestRunner();
        
        // Run different test categories
        TestSuiteResult unitTests = runner.runTestSuite("Unit Tests", "com.machinecoding", 
                                                       "**/*Test.class", "integration", "performance", "quality");
        
        TestSuiteResult integrationTests = runner.runTestSuite("Integration Tests", 
                                                              "com.machinecoding.integration");
        
        TestSuiteResult performanceTests = runner.runTestSuite("Performance Benchmarks", 
                                                              "com.machinecoding.performance");
        
        TestSuiteResult qualityTests = runner.runTestSuite("Code Quality Analysis", 
                                                          "com.machinecoding.quality");
        
        // Generate summary report
        runner.generateSummaryReport(unitTests, integrationTests, performanceTests, qualityTests);
    }
    
    private TestSuiteResult runTestSuite(String suiteName, String packageName, String... excludePackages) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RUNNING: " + suiteName);
        System.out.println("=".repeat(60));
        
        LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage(packageName));
        
        // Add exclusions if specified
        for (String excludePackage : excludePackages) {
            // Note: JUnit 5 doesn't have direct package exclusion, so we'll handle this differently
            // For now, we'll run all tests in the specified package
        }
        
        LauncherDiscoveryRequest request = requestBuilder.build();
        
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        long startTime = System.currentTimeMillis();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        long endTime = System.currentTimeMillis();
        
        TestExecutionSummary summary = listener.getSummary();
        
        // Print results
        System.out.println("\nResults for " + suiteName + ":");
        System.out.println("  Tests found: " + summary.getTestsFoundCount());
        System.out.println("  Tests started: " + summary.getTestsStartedCount());
        System.out.println("  Tests successful: " + summary.getTestsSucceededCount());
        System.out.println("  Tests failed: " + summary.getTestsFailedCount());
        System.out.println("  Tests skipped: " + summary.getTestsSkippedCount());
        System.out.println("  Execution time: " + (endTime - startTime) + "ms");
        
        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\nFailures:");
            summary.getFailures().forEach(failure -> {
                System.out.println("  - " + failure.getTestIdentifier().getDisplayName());
                System.out.println("    " + failure.getException().getMessage());
            });
        }
        
        return new TestSuiteResult(suiteName, summary, endTime - startTime);
    }
    
    private void generateSummaryReport(TestSuiteResult... results) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COMPREHENSIVE TEST SUMMARY REPORT");
        System.out.println("=".repeat(80));
        
        long totalTests = 0;
        long totalSuccessful = 0;
        long totalFailed = 0;
        long totalSkipped = 0;
        long totalTime = 0;
        
        System.out.println("\nDetailed Results:");
        System.out.println("-".repeat(80));
        System.out.printf("%-25s %8s %8s %8s %8s %10s%n", 
                         "Test Suite", "Found", "Success", "Failed", "Skipped", "Time(ms)");
        System.out.println("-".repeat(80));
        
        for (TestSuiteResult result : results) {
            TestExecutionSummary summary = result.summary;
            
            totalTests += summary.getTestsFoundCount();
            totalSuccessful += summary.getTestsSucceededCount();
            totalFailed += summary.getTestsFailedCount();
            totalSkipped += summary.getTestsSkippedCount();
            totalTime += result.executionTime;
            
            System.out.printf("%-25s %8d %8d %8d %8d %10d%n",
                             result.name,
                             summary.getTestsFoundCount(),
                             summary.getTestsSucceededCount(),
                             summary.getTestsFailedCount(),
                             summary.getTestsSkippedCount(),
                             result.executionTime);
        }
        
        System.out.println("-".repeat(80));
        System.out.printf("%-25s %8d %8d %8d %8d %10d%n",
                         "TOTAL",
                         totalTests,
                         totalSuccessful,
                         totalFailed,
                         totalSkipped,
                         totalTime);
        
        // Calculate success rate
        double successRate = totalTests > 0 ? (totalSuccessful * 100.0) / totalTests : 0.0;
        
        System.out.println("\nOverall Statistics:");
        System.out.println("  Success Rate: " + String.format("%.2f%%", successRate));
        System.out.println("  Total Execution Time: " + formatTime(totalTime));
        System.out.println("  Average Test Time: " + 
                         String.format("%.2f ms", totalTests > 0 ? (double) totalTime / totalTests : 0.0));
        
        // Quality assessment
        System.out.println("\nQuality Assessment:");
        if (successRate >= 95.0) {
            System.out.println("  ✅ EXCELLENT - Test suite is highly reliable");
        } else if (successRate >= 90.0) {
            System.out.println("  ✅ GOOD - Test suite is reliable with minor issues");
        } else if (successRate >= 80.0) {
            System.out.println("  ⚠️  FAIR - Test suite needs attention");
        } else {
            System.out.println("  ❌ POOR - Test suite requires significant fixes");
        }
        
        if (totalTime < 30000) { // 30 seconds
            System.out.println("  ⚡ FAST - Test execution is efficient");
        } else if (totalTime < 60000) { // 1 minute
            System.out.println("  🐌 MODERATE - Test execution time is acceptable");
        } else {
            System.out.println("  🐌 SLOW - Consider optimizing test execution");
        }
        
        // Recommendations
        System.out.println("\nRecommendations:");
        if (totalFailed > 0) {
            System.out.println("  • Fix " + totalFailed + " failing test(s)");
        }
        if (totalSkipped > 0) {
            System.out.println("  • Review " + totalSkipped + " skipped test(s)");
        }
        if (totalTime > 60000) {
            System.out.println("  • Consider parallelizing slow tests");
        }
        if (successRate < 95.0) {
            System.out.println("  • Improve test reliability and stability");
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Test execution completed at: " + 
                         LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.println("=".repeat(80));
        
        // Exit with appropriate code
        System.exit(totalFailed > 0 ? 1 : 0);
    }
    
    private String formatTime(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    private static class TestSuiteResult {
        final String name;
        final TestExecutionSummary summary;
        final long executionTime;
        
        TestSuiteResult(String name, TestExecutionSummary summary, long executionTime) {
            this.name = name;
            this.summary = summary;
            this.executionTime = executionTime;
        }
    }
}