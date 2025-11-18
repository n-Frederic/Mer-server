package com.example.demo.dto;

import lombok.Data;

@Data
public class FortuneResponse {
    private String analysis;
    private Suggestion suggestion;

    @Data
    public static class Suggestion {
        private String color;
        private String time;
        private String direction;
        private int number;
    }
}

