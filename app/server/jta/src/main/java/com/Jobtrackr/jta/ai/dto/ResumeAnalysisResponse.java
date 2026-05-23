package com.Jobtrackr.jta.ai.dto;

import java.util.List;

public class ResumeAnalysisResponse {
    private int matchScore;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> missingSkills;
    private List<String> suggestions;
    private List<String> keywords;
    private String summary;

    public ResumeAnalysisResponse() {}

    public ResumeAnalysisResponse(int matchScore, List<String> strengths, List<String> weaknesses,
                                   List<String> missingSkills, List<String> suggestions,
                                   List<String> keywords, String summary) {
        this.matchScore = matchScore;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.missingSkills = missingSkills;
        this.suggestions = suggestions;
        this.keywords = keywords;
        this.summary = summary;
    }

    public int getMatchScore() { return matchScore; }
    public void setMatchScore(int matchScore) { this.matchScore = matchScore; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
