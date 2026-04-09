package tn.recruti.recruti_backend.utils;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CvScoringUtil {

    public double score(String cvText, List<String> keywords) {
        if (keywords == null || keywords.isEmpty())
            return 0.0;

        String lowerCv = cvText.toLowerCase();

        long matched = keywords.stream()
                .filter(keyword -> lowerCv.contains(keyword.toLowerCase()))
                .count();

        return ((double) matched / keywords.size()) * 100;
    }

    // Optional: know which ones matched and which didn't
    public Map<String, Boolean> details(String cvText, List<String> keywords) {
        String lowerCv = cvText.toLowerCase();

        return keywords.stream()
                .collect(Collectors.toMap(
                        keyword -> keyword,
                        keyword -> lowerCv.contains(keyword.toLowerCase())));
    }
}