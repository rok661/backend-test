package lt.revolut.backendtest.common.response.error;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

  private static final Logger logger = LoggerFactory.getLogger(NotFoundExceptionMapper.class);

  @Override
  public Response toResponse(NotFoundException ex) {
    logger.error("NotFoundException caught in restapi", ex);
    ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());

    return Response.status(Status.NOT_FOUND).entity(errorResponse).build();
  }
}
