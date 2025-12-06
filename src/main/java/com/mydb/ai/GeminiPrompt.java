package com.mydb.ai;

import java.util.ArrayList;
import java.util.List;

public class GeminiPrompt {

    // API expects the root field to be "contents"
    public List<Content> contents;

    public GeminiPrompt(String textPrompt) {
        this.contents = new ArrayList<>();
        Content content = new Content("user", textPrompt);
        this.contents.add(content);
    }

    public void setTextPrompt(String textPrompt) {
        this.contents = new ArrayList<>();
        Content content = new Content("user", textPrompt);
        this.contents.add(content);
    }

    public static class Content {
        public String role;
        public List<Part> parts;

        public Content(String role, String text) {
            this.role = role;
            this.parts = new ArrayList<>();
            this.parts.add(new Part(text));
        }
    }

    public static class Part {
        public String text;

        public Part(String text) {
            this.text = text;
        }
    }
}
