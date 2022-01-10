package gr.athenarc.catalogue.ui.domain;

import java.util.ArrayList;
import java.util.List;

public class SurveyModel {

    Survey survey;
    List<ChapterModel> chapterModels = new ArrayList<>();

    public SurveyModel() {}

    public SurveyModel(Survey survey, List<ChapterModel> chapterModels) {
        this.survey = survey;
        this.chapterModels = chapterModels;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public List<ChapterModel> getChapterModels() {
        return chapterModels;
    }

    public void setChapterModels(List<ChapterModel> chapterModels) {
        this.chapterModels = chapterModels;
    }
}
