package gr.athenarc.catalogue.ui.domain;

import java.util.List;

public class ChapterModel {

    Chapter chapter;
    List<GroupedFields<FieldGroup>> groupedFieldsList;

    public ChapterModel() {}

    public ChapterModel(Chapter chapter, List<GroupedFields<FieldGroup>> groupedFieldsList) {
        this.chapter = chapter;
        this.groupedFieldsList = groupedFieldsList;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public List<GroupedFields<FieldGroup>> getGroupedFieldsList() {
        return groupedFieldsList;
    }

    public void setGroupedFieldsList(List<GroupedFields<FieldGroup>> groupedFieldsList) {
        this.groupedFieldsList = groupedFieldsList;
    }
}
