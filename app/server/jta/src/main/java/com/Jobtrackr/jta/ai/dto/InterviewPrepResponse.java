package com.Jobtrackr.jta.ai.dto;

import java.util.List;

public class InterviewPrepResponse {
    private String jobTitle;
    private List<InterviewQuestion> technicalQuestions;
    private List<InterviewQuestion> behavioralQuestions;
    private List<String> preparationTips;
    private List<String> commonMistakes;

    public InterviewPrepResponse() {}

    public InterviewPrepResponse(String jobTitle, List<InterviewQuestion> technicalQuestions,
                                  List<InterviewQuestion> behavioralQuestions,
                                  List<String> preparationTips, List<String> commonMistakes) {
        this.jobTitle = jobTitle;
        this.technicalQuestions = technicalQuestions;
        this.behavioralQuestions = behavioralQuestions;
        this.preparationTips = preparationTips;
        this.commonMistakes = commonMistakes;
    }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public List<InterviewQuestion> getTechnicalQuestions() { return technicalQuestions; }
    public void setTechnicalQuestions(List<InterviewQuestion> technicalQuestions) { this.technicalQuestions = technicalQuestions; }

    public List<InterviewQuestion> getBehavioralQuestions() { return behavioralQuestions; }
    public void setBehavioralQuestions(List<InterviewQuestion> behavioralQuestions) { this.behavioralQuestions = behavioralQuestions; }

    public List<String> getPreparationTips() { return preparationTips; }
    public void setPreparationTips(List<String> preparationTips) { this.preparationTips = preparationTips; }

    public List<String> getCommonMistakes() { return commonMistakes; }
    public void setCommonMistakes(List<String> commonMistakes) { this.commonMistakes = commonMistakes; }

    public static class InterviewQuestion {
        private String question;
        private String category;
        private String difficulty;
        private String sampleAnswer;
        private List<String> tips;

        public InterviewQuestion() {}

        public InterviewQuestion(String question, String category, String difficulty, 
                                  String sampleAnswer, List<String> tips) {
            this.question = question;
            this.category = category;
            this.difficulty = difficulty;
            this.sampleAnswer = sampleAnswer;
            this.tips = tips;
        }

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

        public String getSampleAnswer() { return sampleAnswer; }
        public void setSampleAnswer(String sampleAnswer) { this.sampleAnswer = sampleAnswer; }

        public List<String> getTips() { return tips; }
        public void setTips(List<String> tips) { this.tips = tips; }
    }
}
