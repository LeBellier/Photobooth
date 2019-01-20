package x.mvmn.gp2srv.web.servlets;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.VelocityContext;
import x.leBellier.photobooth.BeanSession;
import x.mvmn.log.api.Logger;

public abstract class AbstractErrorHandlingServlet extends HttpServletWithTemplates {

	private static final long serialVersionUID = 8638499002251355635L;
	protected final Logger logger;

	public AbstractErrorHandlingServlet() {
		super();
		this.logger = BeanSession.getInstance().getLogger();
	}

	protected void returnForbidden(final HttpServletRequest request, final HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		serveGenericErrorPage(request, response, HttpServletResponse.SC_FORBIDDEN, "forbidden");
	}

	protected void returnInternalError(final HttpServletRequest request, final HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		serveGenericErrorPage(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error");
	}

	protected void returnNotFound(final HttpServletRequest request, final HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		serveGenericErrorPage(request, response, HttpServletResponse.SC_NOT_FOUND, "not found");
	}

	public void serveGenericErrorPage(final HttpServletRequest request, final HttpServletResponse response, final int errorCode, String errorMessage) {
		VelocityContext context = new VelocityContext();
		if (errorMessage == null && errorCode == 404) {
			errorMessage = "not found";
		}
		if (errorCode == 404) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		context.put("request", request);
		context.put("response", response);
		context.put("errorCode", errorCode);
		context.put("errorMessage", errorMessage);
		try {
			Writer writer = response.getWriter();
			BeanSession.getInstance().provide().renderTemplate("error.vm", "UTF-8", context, writer);
			writer.flush();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void serveGenericErrorPage(final HttpServletRequest request, final Writer writer, final int errorCode, String errorMessage) {
		VelocityContext context = new VelocityContext();
		if (errorMessage == null && errorCode == 404) {
			errorMessage = "not found";
		}
		context.put("request", request);
		context.put("errorCode", errorCode);
		context.put("errorMessage", errorMessage);
		try {
			BeanSession.getInstance().provide().renderTemplate("error.vm", "UTF-8", context, writer);
			writer.flush();
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
