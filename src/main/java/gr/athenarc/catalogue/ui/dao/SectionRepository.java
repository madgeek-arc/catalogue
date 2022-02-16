package gr.athenarc.catalogue.ui.dao;

import gr.athenarc.catalogue.ui.domain.Section;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepository extends CrudRepository<Section, String> {
}
