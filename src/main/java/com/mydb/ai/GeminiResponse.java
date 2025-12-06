package com.mydb.ai;

import java.util.List;

public class GeminiResponse {
    public List<Candidate> candidates;
    public UsageMetadata usageMetadata;
    
    public static class Candidate {
        public Content content;
        public String finishReason;
        public int index;
        public List<SafetyRating> safetyRatings;
    }
    
    public static class Content {
        public List<Part> parts;
        public String role;
    }
    
    public static class Part {
        public String text;
    }
    
    public static class SafetyRating {
        public String category;
        public String probability;
    }
    
    public static class UsageMetadata {
        public int promptTokenCount;
        public int candidatesTokenCount;
        public int totalTokenCount;
    }
}
