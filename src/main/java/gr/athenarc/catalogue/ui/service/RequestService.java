package gr.athenarc.catalogue.ui.service;

import gr.athenarc.catalogue.ui.domain.DataRequest;
import reactor.core.publisher.Mono;

public interface RequestService {

    Mono<?> perform(DataRequest request);


}
