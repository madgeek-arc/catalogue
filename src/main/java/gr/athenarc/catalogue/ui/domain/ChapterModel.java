package gr.athenarc.catalogue.ui.domain;

import java.util.List;

public class ChapterModel {

    Chapter chapter;
    List<GroupedFields<FieldGroup>> sections;

    public ChapterModel() {}

    public ChapterModel(Chapter chapter, List<GroupedFields<FieldGroup>> sections) {
        this.chapter = chapter;
        this.sections = sections;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public List<GroupedFields<FieldGroup>> getSections() {
        return sections;
    }

    public void setSections(List<GroupedFields<FieldGroup>> sections) {
        this.sections = sections;
    }
}
