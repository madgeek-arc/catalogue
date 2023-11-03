package gr.athenarc.catalogue.ui.service;

import gr.athenarc.catalogue.ui.domain.DataRequest;
import reactor.core.publisher.Mono;

/**
 * Can be used to obtain values from an external API.
 */
public interface DataRequestService {

    /**
     * Performs an API call, based on the {@link DataRequest} given, to retrieve a value.
     *
     * @param request The request object
     * @return The value matching the {@link DataRequest#expression}
     */
    Mono<?> retrieve(DataRequest request);

}
