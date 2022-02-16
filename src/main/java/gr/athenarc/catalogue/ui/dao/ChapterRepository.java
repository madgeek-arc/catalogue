package gr.athenarc.catalogue.ui.dao;

import gr.athenarc.catalogue.ui.domain.Chapter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends CrudRepository<Chapter, String> {
}
