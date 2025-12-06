package com.mydb.security;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SecurityMonitor {
    
    private static final List<SecurityAlert> alerts = new ArrayList<>();
    private static final List<QueryLog> queryHistory = new ArrayList<>();
    
    // SQL Injection patterns
    private static final Pattern[] INJECTION_PATTERNS = {
        Pattern.compile("('.+--)|(--)|(;)|(\\|\\|)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(union.*select)|(insert.*into)|(delete.*from)|(drop.*table)|(update.*set)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(exec.*xp_)|(exec.*sp_)|(execute.*xp_)|(execute.*sp_)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(script.*>)|(javascript:)|(onerror.*=)|(onload.*=)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(benchmark.*\\()|(sleep.*\\()|(waitfor.*delay)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(information_schema)|(sys\\.)|(mysql\\.)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\bor\\b.*=.*\\bor\\b)|(\\band\\b.*=.*\\band\\b)", Pattern.CASE_INSENSITIVE)
    };
    
    // Suspicious query patterns
    private static final Pattern[] SUSPICIOUS_PATTERNS = {
        Pattern.compile("select.*from.*where.*1.*=.*1", Pattern.CASE_INSENSITIVE),
        Pattern.compile("select.*\\*.*from", Pattern.CASE_INSENSITIVE),
        Pattern.compile("drop.*database", Pattern.CASE_INSENSITIVE),
        Pattern.compile("truncate.*table", Pattern.CASE_INSENSITIVE),
        Pattern.compile("grant.*all.*privileges", Pattern.CASE_INSENSITIVE)
    };
    
    public static SecurityAnalysis analyzeQuery(String query, String username, String database) {
        SecurityAnalysis analysis = new SecurityAnalysis();
        analysis.query = query;
        analysis.timestamp = LocalDateTime.now();
        analysis.username = username;
        analysis.database = database;
        
        // Check for SQL injection
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(query).find()) {
                analysis.threats.add(new SecurityThreat(
                    "SQL_INJECTION", 
                    "CRITICAL",
                    "Possible SQL injection attempt detected",
                    "Pattern: " + pattern.pattern()
                ));
                analysis.riskLevel = "CRITICAL";
            }
        }
        
        // Check for suspicious patterns
        for (Pattern pattern : SUSPICIOUS_PATTERNS) {
            if (pattern.matcher(query).find()) {
                analysis.threats.add(new SecurityThreat(
                    "SUSPICIOUS_QUERY",
                    "WARNING",
                    "Potentially dangerous query detected",
                    "Pattern: " + pattern.pattern()
                ));
                if (!analysis.riskLevel.equals("CRITICAL")) {
                    analysis.riskLevel = "HIGH";
                }
            }
        }
        
        // Check query length (extremely long queries might be suspicious)
        if (query.length() > 5000) {
            analysis.threats.add(new SecurityThreat(
                "UNUSUAL_LENGTH",
                "WARNING",
                "Unusually long query detected",
                "Length: " + query.length() + " characters"
            ));
        }
        
        // If no threats found
        if (analysis.threats.isEmpty()) {
            analysis.riskLevel = "SAFE";
            analysis.threats.add(new SecurityThreat(
                "NO_THREATS",
                "INFO",
                "Query appears safe",
                "No security issues detected"
            ));
        }
        
        // Log the query
        logQuery(query, username, database, analysis.riskLevel);
        
        // Create alert if critical
        if (analysis.riskLevel.equals("CRITICAL")) {
            createAlert("SQL Injection Attempt", query, "CRITICAL");
        }
        
        return analysis;
    }
    
    private static void logQuery(String query, String username, String database, String riskLevel) {
        QueryLog log = new QueryLog();
        log.timestamp = LocalDateTime.now();
        log.query = query;
        log.username = username;
        log.database = database;
        log.riskLevel = riskLevel;
        
        queryHistory.add(0, log); // Add to beginning
        
        // Keep only last 1000 queries
        if (queryHistory.size() > 1000) {
            queryHistory.remove(queryHistory.size() - 1);
        }
    }
    
    private static void createAlert(String title, String description, String severity) {
        SecurityAlert alert = new SecurityAlert();
        alert.timestamp = LocalDateTime.now();
        alert.title = title;
        alert.description = description;
        alert.severity = severity;
        alert.acknowledged = false;
        
        alerts.add(0, alert);
        
        // Keep only last 100 alerts
        if (alerts.size() > 100) {
            alerts.remove(alerts.size() - 1);
        }
    }
    
    public static List<SecurityAlert> getAlerts() {
        return new ArrayList<>(alerts);
    }
    
    public static List<QueryLog> getQueryHistory() {
        return new ArrayList<>(queryHistory);
    }
    
    public static void acknowledgeAlert(SecurityAlert alert) {
        alert.acknowledged = true;
    }
    
    public static void clearAlerts() {
        alerts.clear();
    }
    
    public static SecurityStatistics getStatistics() {
        SecurityStatistics stats = new SecurityStatistics();
        
        stats.totalQueries = queryHistory.size();
        stats.criticalThreats = (int) queryHistory.stream()
            .filter(q -> q.riskLevel.equals("CRITICAL")).count();
        stats.highRiskQueries = (int) queryHistory.stream()
            .filter(q -> q.riskLevel.equals("HIGH")).count();
        stats.safeQueries = (int) queryHistory.stream()
            .filter(q -> q.riskLevel.equals("SAFE")).count();
        stats.unacknowledgedAlerts = (int) alerts.stream()
            .filter(a -> !a.acknowledged).count();
        
        return stats;
    }
    
    // Inner classes
    public static class SecurityAnalysis {
        public String query;
        public LocalDateTime timestamp;
        public String username;
        public String database;
        public String riskLevel = "SAFE";
        public List<SecurityThreat> threats = new ArrayList<>();
    }
    
    public static class SecurityThreat {
        public String type;
        public String severity;
        public String message;
        public String details;
        
        public SecurityThreat(String type, String severity, String message, String details) {
            this.type = type;
            this.severity = severity;
            this.message = message;
            this.details = details;
        }
    }
    
    public static class SecurityAlert {
        public LocalDateTime timestamp;
        public String title;
        public String description;
        public String severity;
        public boolean acknowledged;
    }
    
    public static class QueryLog {
        public LocalDateTime timestamp;
        public String query;
        public String username;
        public String database;
        public String riskLevel;
    }
    
    public static class SecurityStatistics {
        public int totalQueries;
        public int criticalThreats;
        public int highRiskQueries;
        public int safeQueries;
        public int unacknowledgedAlerts;
    }
}
