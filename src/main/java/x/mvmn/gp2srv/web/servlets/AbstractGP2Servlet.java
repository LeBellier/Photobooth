package x.mvmn.gp2srv.web.servlets;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import x.leBellier.photobooth.BeanSession;

/**
 * This type of servlets adds shared global context to template engine +
 * inherits from {@link AbstractErrorHandlingServlet} error handling with error
 * pages rendered from properly templates + {@link AbstractErrorHandlingServlet}
 * itself inherits from {@link HttpServletWithTemplates} a template engine +
 * methods for outputting render of templates.<br/>
 * <br/>
 * Thus we get templating in general, default error handing with proper
 * templates and global context in templates.
 *
 * @author Mykola Makhin
 */
public class AbstractGP2Servlet extends AbstractErrorHandlingServlet {

	private static final long serialVersionUID = 7210482012835862732L;

	public AbstractGP2Servlet() {
		super();
	}

	@Override
	public Context createContext(final HttpServletRequest request, final HttpServletResponse response) {
		Context result = new VelocityContext(BeanSession.getInstance().getVelocityContextService().getGlobalContext());
		result.put("request", request);
		result.put("response", response);
		return result;
	}

	public Context createContext(final HttpServletRequest request, final HttpServletResponse response, final Map<String, Object> values) {
		Context result = new VelocityContext(values, BeanSession.getInstance().getVelocityContextService().getGlobalContext());
		result.put("request", request);
		result.put("response", response);
		return result;
	}
}
