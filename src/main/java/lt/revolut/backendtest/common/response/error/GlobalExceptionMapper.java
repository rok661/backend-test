package lt.revolut.backendtest.common.response.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionMapper.class);

  @Override
  public Response toResponse(Exception ex) {
    log.error("Exception caught in restapi", ex);
    ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
  }
}
