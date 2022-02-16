package gr.athenarc.catalogue.ui.dao;

import gr.athenarc.catalogue.ui.domain.UiField;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UiFieldRepository extends CrudRepository<UiField, String> {
}
