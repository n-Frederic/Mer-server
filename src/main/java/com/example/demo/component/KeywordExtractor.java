package com.example.demo.component;

import com.example.demo.dto.Keyword;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class KeywordExtractor {

    public List<Keyword> extractTopKeywords(String text) {
        return Arrays.stream(text.split("\\W+"))
                .filter(s -> s.length() > 1)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new Keyword(e.getKey(), e.getValue().intValue()))
                .toList();
    }
}
