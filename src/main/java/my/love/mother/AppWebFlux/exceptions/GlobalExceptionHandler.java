package my.love.mother.AppWebFlux.exceptions;

import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties.*;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler
            (ErrorAttributes errorAttributes,
             Resources resources,
             ApplicationContext applicationContext,
             ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(codecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::formatErrorResponse);
    }

    private Mono<ServerResponse> formatErrorResponse(ServerRequest serverRequest) {
        String query = serverRequest.uri().getQuery();
        ErrorAttributeOptions errorAttributeOptions = isTraceEnabled(query) ?
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE) :
                ErrorAttributeOptions.defaults();
        Map<String, Object> errorAttributeMap = getErrorAttributes(serverRequest, errorAttributeOptions);
        int status = (int) Optional.ofNullable(errorAttributeMap.get("status")).orElse(500);
        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorAttributeMap));
    }

    private boolean isTraceEnabled(String query) {
        return !StringUtils.isEmpty(query) && query.contains("trace-true");
    }
}
