package com.machinecoding.quality;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Code quality metrics and analysis for the machine coding project.
 * Analyzes code complexity, documentation coverage, and adherence to best practices.
 */
public class CodeQualityMetrics {
    
    private static final String SOURCE_DIR = "src/main/java";
    private static final String TEST_DIR = "src/test/java";
    
    @Test
    @DisplayName("Code Coverage Analysis")
    void analyzeCodeCoverage() throws IOException {
        System.out.println("\n=== Code Coverage Analysis ===");
        
        List<Path> sourceFiles = getJavaFiles(SOURCE_DIR);
        List<Path> testFiles = getJavaFiles(TEST_DIR);
        
        Map<String, Boolean> packageCoverage = new HashMap<>();
        
        for (Path sourceFile : sourceFiles) {
            String packageName = extractPackageName(sourceFile);
            String className = extractClassName(sourceFile);
            
            boolean hasTest = testFiles.stream()
                .anyMatch(testFile -> {
                    try {
                        String testContent = Files.readString(testFile);
                        return testContent.contains(className + "Test") || 
                               testContent.contains(className + ".class") ||
                               testContent.contains("new " + className + "(");
                    } catch (IOException e) {
                        return false;
                    }
                });
            
            packageCoverage.put(packageName + "." + className, hasTest);
        }
        
        long coveredClasses = packageCoverage.values().stream().mapToLong(b -> b ? 1 : 0).sum();
        double coveragePercentage = (coveredClasses * 100.0) / packageCoverage.size();
        
        System.out.println("Total classes: " + packageCoverage.size());
        System.out.println("Classes with tests: " + coveredClasses);
        System.out.println("Coverage percentage: " + String.format("%.2f%%", coveragePercentage));
        
        // Show uncovered classes
        System.out.println("\nUncovered classes:");
        packageCoverage.entrySet().stream()
            .filter(entry -> !entry.getValue())
            .forEach(entry -> System.out.println("  - " + entry.getKey()));
        
        assertTrue(coveragePercentage >= 70.0, "Code coverage should be at least 70%");
    }
    
    @Test
    @DisplayName("Code Complexity Analysis")
    void analyzeCodeComplexity() throws IOException {
        System.out.println("\n=== Code Complexity Analysis ===");
        
        List<Path> sourceFiles = getJavaFiles(SOURCE_DIR);
        List<ComplexityMetric> complexityMetrics = new ArrayList<>();
        
        for (Path sourceFile : sourceFiles) {
            String content = Files.readString(sourceFile);
            ComplexityMetric metric = analyzeFileComplexity(sourceFile, content);
            complexityMetrics.add(metric);
        }
        
        // Sort by complexity
        complexityMetrics.sort((a, b) -> Integer.compare(b.cyclomaticComplexity, a.cyclomaticComplexity));
        
        // Calculate statistics
        double avgComplexity = complexityMetrics.stream()
            .mapToInt(m -> m.cyclomaticComplexity)
            .average()
            .orElse(0.0);
        
        int maxComplexity = complexityMetrics.stream()
            .mapToInt(m -> m.cyclomaticComplexity)
            .max()
            .orElse(0);
        
        System.out.println("Total files analyzed: " + complexityMetrics.size());
        System.out.println("Average cyclomatic complexity: " + String.format("%.2f", avgComplexity));
        System.out.println("Maximum cyclomatic complexity: " + maxComplexity);
        
        // Show most complex files
        System.out.println("\nMost complex files:");
        complexityMetrics.stream()
            .limit(10)
            .forEach(metric -> System.out.println("  " + metric));
        
        // Quality assertions
        assertTrue(avgComplexity <= 15.0, "Average complexity should be reasonable (≤15)");
        assertTrue(maxComplexity <= 50, "No single file should be extremely complex (≤50)");
    }
    
    @Test
    @DisplayName("Documentation Coverage Analysis")
    void analyzeDocumentationCoverage() throws IOException {
        System.out.println("\n=== Documentation Coverage Analysis ===");
        
        List<Path> sourceFiles = getJavaFiles(SOURCE_DIR);
        List<DocumentationMetric> docMetrics = new ArrayList<>();
        
        for (Path sourceFile : sourceFiles) {
            String content = Files.readString(sourceFile);
            DocumentationMetric metric = analyzeDocumentation(sourceFile, content);
            docMetrics.add(metric);
        }
        
        // Calculate overall statistics
        int totalClasses = docMetrics.stream().mapToInt(m -> m.totalClasses).sum();
        int documentedClasses = docMetrics.stream().mapToInt(m -> m.documentedClasses).sum();
        int totalMethods = docMetrics.stream().mapToInt(m -> m.totalMethods).sum();
        int documentedMethods = docMetrics.stream().mapToInt(m -> m.documentedMethods).sum();
        
        double classDocCoverage = (documentedClasses * 100.0) / Math.max(totalClasses, 1);
        double methodDocCoverage = (documentedMethods * 100.0) / Math.max(totalMethods, 1);
        
        System.out.println("Class documentation coverage: " + 
                         String.format("%.2f%% (%d/%d)", classDocCoverage, documentedClasses, totalClasses));
        System.out.println("Method documentation coverage: " + 
                         String.format("%.2f%% (%d/%d)", methodDocCoverage, documentedMethods, totalMethods));
        
        // Show files with poor documentation
        System.out.println("\nFiles needing better documentation:");
        docMetrics.stream()
            .filter(m -> m.getOverallCoverage() < 50.0)
            .forEach(metric -> System.out.println("  " + metric));
        
        assertTrue(classDocCoverage >= 80.0, "Class documentation coverage should be at least 80%");
        assertTrue(methodDocCoverage >= 60.0, "Method documentation coverage should be at least 60%");
    }
    
    @Test
    @DisplayName("Code Style and Best Practices Analysis")
    void analyzeCodeStyle() throws IOException {
        System.out.println("\n=== Code Style and Best Practices Analysis ===");
        
        List<Path> sourceFiles = getJavaFiles(SOURCE_DIR);
        List<StyleViolation> violations = new ArrayList<>();
        
        for (Path sourceFile : sourceFiles) {
            String content = Files.readString(sourceFile);
            violations.addAll(analyzeCodeStyle(sourceFile, content));
        }
        
        // Group violations by type
        Map<String, Long> violationCounts = violations.stream()
            .collect(Collectors.groupingBy(v -> v.type, Collectors.counting()));
        
        System.out.println("Total style violations: " + violations.size());
        System.out.println("\nViolation breakdown:");
        violationCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(entry -> System.out.println("  " + entry.getKey() + ": " + entry.getValue()));
        
        // Show sample violations
        if (!violations.isEmpty()) {
            System.out.println("\nSample violations:");
            violations.stream()
                .limit(10)
                .forEach(violation -> System.out.println("  " + violation));
        }
        
        // Quality thresholds
        assertTrue(violations.size() <= sourceFiles.size() * 5, 
                  "Should have reasonable number of style violations (≤5 per file)");
    }
    
    @Test
    @DisplayName("Package Structure Analysis")
    void analyzePackageStructure() throws IOException {
        System.out.println("\n=== Package Structure Analysis ===");
        
        List<Path> sourceFiles = getJavaFiles(SOURCE_DIR);
        Map<String, List<String>> packageStructure = new HashMap<>();
        
        for (Path sourceFile : sourceFiles) {
            String packageName = extractPackageName(sourceFile);
            String className = extractClassName(sourceFile);
            
            packageStructure.computeIfAbsent(packageName, k -> new ArrayList<>()).add(className);
        }
        
        System.out.println("Total packages: " + packageStructure.size());
        System.out.println("\nPackage breakdown:");
        
        packageStructure.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                System.out.println("  " + entry.getKey() + " (" + entry.getValue().size() + " classes)");
                if (entry.getValue().size() <= 3) {
                    entry.getValue().forEach(className -> System.out.println("    - " + className));
                }
            });
        
        // Check for package organization
        long wellOrganizedPackages = packageStructure.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 2 && entry.getValue().size() <= 10)
            .count();
        
        double organizationScore = (wellOrganizedPackages * 100.0) / packageStructure.size();
        System.out.println("\nPackage organization score: " + String.format("%.2f%%", organizationScore));
        
        assertTrue(organizationScore >= 70.0, "Package structure should be well organized");
    }
    
    // Helper methods
    
    private List<Path> getJavaFiles(String directory) throws IOException {
        Path dir = Paths.get(directory);
        if (!Files.exists(dir)) {
            return Collections.emptyList();
        }
        
        try (Stream<Path> paths = Files.walk(dir)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(Collectors.toList());
        }
    }
    
    private String extractPackageName(Path javaFile) throws IOException {
        String content = Files.readString(javaFile);
        Pattern packagePattern = Pattern.compile("package\\s+([a-zA-Z0-9_.]+);");
        var matcher = packagePattern.matcher(content);
        return matcher.find() ? matcher.group(1) : "default";
    }
    
    private String extractClassName(Path javaFile) {
        String fileName = javaFile.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
    
    private ComplexityMetric analyzeFileComplexity(Path file, String content) {
        // Simple cyclomatic complexity calculation
        int complexity = 1; // Base complexity
        
        // Count decision points
        complexity += countOccurrences(content, "if\\s*\\(");
        complexity += countOccurrences(content, "else\\s+if\\s*\\(");
        complexity += countOccurrences(content, "while\\s*\\(");
        complexity += countOccurrences(content, "for\\s*\\(");
        complexity += countOccurrences(content, "do\\s*\\{");
        complexity += countOccurrences(content, "switch\\s*\\(");
        complexity += countOccurrences(content, "case\\s+");
        complexity += countOccurrences(content, "catch\\s*\\(");
        complexity += countOccurrences(content, "&&");
        complexity += countOccurrences(content, "\\|\\|");
        complexity += countOccurrences(content, "\\?.*:");
        
        int linesOfCode = content.split("\n").length;
        int methods = countOccurrences(content, "(public|private|protected).*\\(.*\\)\\s*\\{");
        
        return new ComplexityMetric(file.getFileName().toString(), complexity, linesOfCode, methods);
    }
    
    private DocumentationMetric analyzeDocumentation(Path file, String content) {
        int totalClasses = countOccurrences(content, "(class|interface|enum)\\s+\\w+");
        int documentedClasses = countOccurrences(content, "/\\*\\*[^*]*\\*/\\s*(public\\s+)?(class|interface|enum)");
        
        int totalMethods = countOccurrences(content, "(public|private|protected).*\\(.*\\)\\s*\\{");
        int documentedMethods = countOccurrences(content, "/\\*\\*[^*]*\\*/\\s*(public|private|protected).*\\(.*\\)\\s*\\{");
        
        return new DocumentationMetric(file.getFileName().toString(), 
                                     totalClasses, documentedClasses, 
                                     totalMethods, documentedMethods);
    }
    
    private List<StyleViolation> analyzeCodeStyle(Path file, String content) {
        List<StyleViolation> violations = new ArrayList<>();
        String fileName = file.getFileName().toString();
        String[] lines = content.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;
            
            // Check line length
            if (line.length() > 120) {
                violations.add(new StyleViolation(fileName, lineNumber, "Long line", 
                                                "Line exceeds 120 characters (" + line.length() + ")"));
            }
            
            // Check for tabs
            if (line.contains("\t")) {
                violations.add(new StyleViolation(fileName, lineNumber, "Tab usage", 
                                                "Use spaces instead of tabs"));
            }
            
            // Check for trailing whitespace
            if (line.endsWith(" ") || line.endsWith("\t")) {
                violations.add(new StyleViolation(fileName, lineNumber, "Trailing whitespace", 
                                                "Remove trailing whitespace"));
            }
            
            // Check for TODO/FIXME comments
            if (line.contains("TODO") || line.contains("FIXME")) {
                violations.add(new StyleViolation(fileName, lineNumber, "TODO/FIXME", 
                                                "Unresolved TODO or FIXME comment"));
            }
        }
        
        // Check for missing final on fields
        if (content.contains("private ") && !content.contains("private final")) {
            violations.add(new StyleViolation(fileName, 0, "Missing final", 
                                            "Consider making fields final where possible"));
        }
        
        return violations;
    }
    
    private int countOccurrences(String text, String pattern) {
        return (int) Pattern.compile(pattern).matcher(text).results().count();
    }
    
    // Metric classes
    
    private static class ComplexityMetric {
        final String fileName;
        final int cyclomaticComplexity;
        final int linesOfCode;
        final int methods;
        
        ComplexityMetric(String fileName, int cyclomaticComplexity, int linesOfCode, int methods) {
            this.fileName = fileName;
            this.cyclomaticComplexity = cyclomaticComplexity;
            this.linesOfCode = linesOfCode;
            this.methods = methods;
        }
        
        @Override
        public String toString() {
            return String.format("%s: CC=%d, LOC=%d, Methods=%d", 
                               fileName, cyclomaticComplexity, linesOfCode, methods);
        }
    }
    
    private static class DocumentationMetric {
        final String fileName;
        final int totalClasses;
        final int documentedClasses;
        final int totalMethods;
        final int documentedMethods;
        
        DocumentationMetric(String fileName, int totalClasses, int documentedClasses, 
                          int totalMethods, int documentedMethods) {
            this.fileName = fileName;
            this.totalClasses = totalClasses;
            this.documentedClasses = documentedClasses;
            this.totalMethods = totalMethods;
            this.documentedMethods = documentedMethods;
        }
        
        double getOverallCoverage() {
            int total = totalClasses + totalMethods;
            int documented = documentedClasses + documentedMethods;
            return total > 0 ? (documented * 100.0) / total : 100.0;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %.1f%% (Classes: %d/%d, Methods: %d/%d)", 
                               fileName, getOverallCoverage(), 
                               documentedClasses, totalClasses, 
                               documentedMethods, totalMethods);
        }
    }
    
    private static class StyleViolation {
        final String fileName;
        final int lineNumber;
        final String type;
        final String description;
        
        StyleViolation(String fileName, int lineNumber, String type, String description) {
            this.fileName = fileName;
            this.lineNumber = lineNumber;
            this.type = type;
            this.description = description;
        }
        
        @Override
        public String toString() {
            return String.format("%s:%d - %s: %s", fileName, lineNumber, type, description);
        }
    }
}