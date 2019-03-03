package x.mvmn.gp2srv.web.servlets;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.context.Context;
import x.leBellier.photobooth.BeanSession;
import x.mvmn.gp2srv.GPhoto2Server;

public class DevModeServlet extends AbstractErrorHandlingServlet {

	private static final long serialVersionUID = -810341607948659887L;

	private final GPhoto2Server gPhoto2Server;

	public DevModeServlet(final GPhoto2Server gPhoto2Server) {
		super();
		this.gPhoto2Server = gPhoto2Server;
	}

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) {
		final Context context = createContext(request, response);
		if ("/rst".equals(request.getPathInfo())) {
			BeanSession.getInstance().setTemplateEngine();
			try {
				response.sendRedirect(request.getContextPath() + "/devmode");
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			serveTempalteUTF8Safely("devmode.vm", context, response, logger);
		}
	}
}
