package gr.athenarc.catalogue.annotations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Operation(parameters = {
        @Parameter(in = ParameterIn.QUERY, name = "query", description = "Keyword to refine the search", content = @Content(schema = @Schema(type = "string", defaultValue = ""))),
        @Parameter(in = ParameterIn.QUERY, name = "from", description = "Starting index in the result set", content = @Content(schema = @Schema(type = "string", defaultValue = ""))),
        @Parameter(in = ParameterIn.QUERY, name = "quantity", description = "Quantity to be fetched", content = @Content(schema = @Schema(type = "string", defaultValue = ""))),
        @Parameter(in = ParameterIn.QUERY, name = "order", description = "asc / desc", content = @Content(schema = @Schema(type = "string", defaultValue = ""))),
        @Parameter(in = ParameterIn.QUERY, name = "orderField", description = "Order field", content = @Content(schema = @Schema(type = "string", defaultValue = "")))
})
public @interface Browse {

}
