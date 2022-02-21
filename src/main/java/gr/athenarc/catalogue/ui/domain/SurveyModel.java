package gr.athenarc.catalogue.ui.domain;

import java.util.ArrayList;
import java.util.List;

public class SurveyModel {

    String surveyId;
    List<ChapterModel> chapterModels = new ArrayList<>();

    public SurveyModel() {}

    public SurveyModel(String surveyId, List<ChapterModel> chapterModels) {
        this.surveyId = surveyId;
        this.chapterModels = chapterModels;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(String surveyId) {
        this.surveyId = surveyId;
    }

    public List<ChapterModel> getChapterModels() {
        return chapterModels;
    }

    public void setChapterModels(List<ChapterModel> chapterModels) {
        this.chapterModels = chapterModels;
    }
}
