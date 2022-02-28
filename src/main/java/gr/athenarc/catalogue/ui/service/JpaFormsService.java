package gr.athenarc.catalogue.ui.service;

import eu.openminted.registry.core.domain.Browsing;
import eu.openminted.registry.core.domain.FacetFilter;
import gr.athenarc.catalogue.exception.ResourceNotFoundException;
import gr.athenarc.catalogue.ui.dao.ChapterRepository;
import gr.athenarc.catalogue.ui.dao.SectionRepository;
import gr.athenarc.catalogue.ui.dao.ModelRepository;
import gr.athenarc.catalogue.ui.dao.UiFieldRepository;
import gr.athenarc.catalogue.ui.domain.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Primary
public class JpaFormsService implements ModelService {

    private static final Logger logger = LogManager.getLogger(JpaFormsService.class);

    private final ModelRepository modelRepository;
    private final ChapterRepository chapterRepository;
    private final SectionRepository sectionRepository;
    private final UiFieldRepository uiFieldRepository;

    @Autowired
    public JpaFormsService(ModelRepository modelRepository, ChapterRepository chapterRepository,
                           SectionRepository sectionRepository, UiFieldRepository uiFieldRepository) {
        this.modelRepository = modelRepository;
        this.chapterRepository = chapterRepository;
        this.sectionRepository = sectionRepository;
        this.uiFieldRepository = uiFieldRepository;
    }

    @Override
    public Model add(Model model) {
        for (Chapter chapter : model.getChapters()) {
            for (Section section : chapter.getSections()) {
                for (UiField field : section.getFields()) {
                    field.setSection(section);
//                    uiFieldRepository.save(field);
                }
                section.setChapter(chapter);
//                sectionRepository.save(section);
            }
            chapter.setModel(model);
//            chapterRepository.save(chapter);
        }

        return modelRepository.save(model);
    }

    @Override
    public Model update(String id, Model model) {
        Model persisted = modelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("") );
        model.setId(persisted.getId());
        return modelRepository.save(model);
    }

    @Override
    public void delete(String id) throws ResourceNotFoundException {
        modelRepository.deleteById(id);
    }

    @Override
    public Model get(String id) {
        return modelRepository.findById(id).orElse(null);
    }

    @Override
    public List<Model> browse(FacetFilter filter) {
        Pageable pageable = PageRequest.of(filter.getFrom(), filter.getQuantity());
        return modelRepository.findAll(pageable);
    }
}
