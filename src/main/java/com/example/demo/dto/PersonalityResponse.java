package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalityResponse {
    private String type;
    private String analysisSummary;
    private List<Breakdown> breakdown;

    @Data
    public static class Breakdown {
        private String dimension;
        private String type;
        private String description;
    }
}
