package com.Jobtrackr.jta.ai.dto;

import java.util.List;

public class SkillSuggestionResponse {
    private List<SkillCategory> requiredSkills;
    private List<SkillCategory> preferredSkills;
    private List<LearningResource> learningResources;
    private List<String> careerAdvice;

    public SkillSuggestionResponse() {}

    public SkillSuggestionResponse(List<SkillCategory> requiredSkills, List<SkillCategory> preferredSkills,
                                    List<LearningResource> learningResources, List<String> careerAdvice) {
        this.requiredSkills = requiredSkills;
        this.preferredSkills = preferredSkills;
        this.learningResources = learningResources;
        this.careerAdvice = careerAdvice;
    }

    public List<SkillCategory> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<SkillCategory> requiredSkills) { this.requiredSkills = requiredSkills; }

    public List<SkillCategory> getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(List<SkillCategory> preferredSkills) { this.preferredSkills = preferredSkills; }

    public List<LearningResource> getLearningResources() { return learningResources; }
    public void setLearningResources(List<LearningResource> learningResources) { this.learningResources = learningResources; }

    public List<String> getCareerAdvice() { return careerAdvice; }
    public void setCareerAdvice(List<String> careerAdvice) { this.careerAdvice = careerAdvice; }

    public static class SkillCategory {
        private String category;
        private List<Skill> skills;

        public SkillCategory() {}

        public SkillCategory(String category, List<Skill> skills) {
            this.category = category;
            this.skills = skills;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public List<Skill> getSkills() { return skills; }
        public void setSkills(List<Skill> skills) { this.skills = skills; }
    }

    public static class Skill {
        private String name;
        private String importance;
        private boolean hasSkill;

        public Skill() {}

        public Skill(String name, String importance, boolean hasSkill) {
            this.name = name;
            this.importance = importance;
            this.hasSkill = hasSkill;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getImportance() { return importance; }
        public void setImportance(String importance) { this.importance = importance; }

        public boolean isHasSkill() { return hasSkill; }
        public void setHasSkill(boolean hasSkill) { this.hasSkill = hasSkill; }
    }

    public static class LearningResource {
        private String skillName;
        private String resourceType;
        private String title;
        private String description;

        public LearningResource() {}

        public LearningResource(String skillName, String resourceType, String title, String description) {
            this.skillName = skillName;
            this.resourceType = resourceType;
            this.title = title;
            this.description = description;
        }

        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }

        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
