package lt.revolut.backendtest.common.response.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lt.revolut.backendtest.common.exception.AppBusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class AppBusinessExceptionMapper implements ExceptionMapper<AppBusinessException> {

  private static final Logger log = LoggerFactory.getLogger(AppBusinessExceptionMapper.class);

  @Override
  public Response toResponse(AppBusinessException ex) {
    log.error("AppBusinessException caught in restapi", ex);
    ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());

    return Response.status(Status.BAD_REQUEST).entity(errorResponse).build();
  }
}
