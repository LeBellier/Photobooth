package x.mvmn.gp2srv.web.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import x.leBellier.photobooth.BeanSession;
import x.mvmn.gp2srv.camera.CameraProvider;
import x.mvmn.jlibgphoto2.impl.CameraDetectorImpl;
import x.mvmn.jlibgphoto2.impl.GP2CameraImpl;
import x.mvmn.jlibgphoto2.impl.GP2PortInfoList;
import x.mvmn.jlibgphoto2.impl.GP2PortInfoList.GP2PortInfo;

public class CameraChoiceFilter extends AbstractGP2Servlet implements Filter {

	private static final long serialVersionUID = 1827967172388376853L;

	private final CameraProvider camProvider;

	public CameraChoiceFilter() {
		super();
		this.camProvider = BeanSession.getInstance();
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			if (httpRequest.getRequestURI().startsWith("/static") || camProvider.hasCamera()) {
				chain.doFilter(request, response);
			} else {
				synchronized (camProvider) {
					if (!camProvider.hasCamera()) {
						String cameraPortParam = httpRequest.getParameter("cameraPort");
						if (cameraPortParam != null) {
							// Set camera
							GP2PortInfoList portList = new GP2PortInfoList();
							GP2PortInfo gp2PortInfo = portList.getByPath(cameraPortParam);
							if (gp2PortInfo != null) {
								camProvider.setCamera(new GP2CameraImpl(gp2PortInfo));
								httpResponse.sendRedirect(httpRequest.getRequestURI());

							} else {
								httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							}
						} else {
							// Show camera choice
							Map<String, Object> tempalteModel = new HashMap<String, Object>();
							tempalteModel.put("cameras", new CameraDetectorImpl().detectCameras());
							serveTempalteUTF8Safely("camera/choice.vm", createContext(httpRequest, httpResponse, tempalteModel), httpResponse, logger);
						}
					}
				}
			}
		}
	}

	public void destroy() {
	}
}
