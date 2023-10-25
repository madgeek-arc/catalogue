package gr.athenarc.catalogue.ui.domain;

import java.util.List;

public class ModelConfiguration {
    
    boolean prefillable;
    List<String> importFrom;

    public boolean isPrefillable() {
        return prefillable;
    }

    public ModelConfiguration setPrefillable(boolean prefillable) {
        this.prefillable = prefillable;
        return this;
    }

    public List<String> getImportFrom() {
        return importFrom;
    }

    public ModelConfiguration setImportFrom(List<String> importFrom) {
        this.importFrom = importFrom;
        return this;
    }
}


//public class ModelConfiguration {
//
//    String id;
//    String modelId;
//    Prefill prefill;
//
//
//    public static class Prefill {
//        boolean prefillable;
//        List<Import> importFrom;
//    }
//
//
//    public static class Import {
//        String modelId;
//        String type;
//        String series;
//    }
//
//}
