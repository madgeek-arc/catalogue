package gr.athenarc.catalogue.ui.dao;

import gr.athenarc.catalogue.ui.domain.Model;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends CrudRepository<Model, String> {

    List<Model> findAll(Pageable pageable);
    List<Model> findAllByType(String type, Pageable pageable);

}
