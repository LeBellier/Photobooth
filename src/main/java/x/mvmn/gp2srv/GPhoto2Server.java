package x.mvmn.gp2srv;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import x.leBellier.photobooth.BeanSession;
import x.mvmn.gp2srv.scripting.service.impl.ScriptExecutionServiceImpl;
import x.mvmn.gp2srv.scripting.service.impl.ScriptsManagementServiceImpl;
import x.mvmn.gp2srv.web.service.velocity.TemplateEngine;
import x.mvmn.gp2srv.web.servlets.AbstractErrorHandlingServlet;
import x.mvmn.gp2srv.web.servlets.BasicAuthFilter;
import x.mvmn.gp2srv.web.servlets.CameraChoiceFilter;
import x.mvmn.gp2srv.web.servlets.CameraControlServlet;
import x.mvmn.gp2srv.web.servlets.DevModeServlet;
import x.mvmn.gp2srv.web.servlets.LiveViewServlet;
import x.mvmn.gp2srv.web.servlets.RootAuthFilter;
import x.mvmn.gp2srv.web.servlets.ScriptExecWebSocketNotifier;
import x.mvmn.gp2srv.web.servlets.ScriptExecutionReportingWebSocketServlet;
import x.mvmn.gp2srv.web.servlets.ScriptingServlet;
import x.mvmn.gp2srv.web.servlets.StaticsResourcesServlet;
import x.mvmn.jlibgphoto2.api.CameraListItemBean;
import x.mvmn.jlibgphoto2.impl.CameraDetectorImpl;
import x.mvmn.jlibgphoto2.impl.GP2CameraImpl;
import x.mvmn.jlibgphoto2.impl.GP2PortInfoList;
import x.mvmn.lang.util.WaitUtil;
import x.mvmn.log.api.Logger;
import x.mvmn.util.FileBackedProperties;

public class GPhoto2Server {

	private static final String DEFAULT_CONTEXT_PATH = "/";
	private static final int DEFAULT_PORT = 8080;

	private final Server server;

	private final File scriptsFolder;
	private final File favouredCamConfSettingsFile;
	private final FileBackedProperties favouredCamConfSettings;
	private final ScriptsManagementServiceImpl scriptManagementService;
	private final ScriptExecutionServiceImpl scriptExecService;
	private final ScriptExecWebSocketNotifier scriptExecWebSocketNotifier;

	public static final AtomicBoolean liveViewEnabled = new AtomicBoolean(true);
	public static final AtomicBoolean liveViewInProgress = new AtomicBoolean(false);

	private static final Logger logger = BeanSession.getInstance().getLogger();

	public GPhoto2Server() {
		this(DEFAULT_PORT, null, DEFAULT_CONTEXT_PATH);
	}

	public GPhoto2Server(Integer port, String[] requireAuthCredentials) {
		this(port, requireAuthCredentials, DEFAULT_CONTEXT_PATH);
	}

	public GPhoto2Server(Integer port, String[] requireAuthCredentials, String contextPath) {
		try {
			logger.info("Initializing...");

			BeanSession beanSession = BeanSession.getInstance();

			if (contextPath == null) {
				contextPath = DEFAULT_CONTEXT_PATH;
			}
			if (port == null) {
				port = DEFAULT_PORT;
			}
			logger.info("Arguments are : " + port.toString() + ", " + logger.getLevel().toString() + ", "
					+ requireAuthCredentials + ", " + beanSession.getImagesFolder().getAbsolutePath());

			beanSession.setTemplateEngine(makeTemplateEngine());

			this.server = new Server(port);
			this.server.setStopAtShutdown(true);

			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath(contextPath);

			scriptsFolder = new File(beanSession.getAppHomeFolder(), "scripts");
			scriptsFolder.mkdirs();
			favouredCamConfSettingsFile = new File(beanSession.getAppHomeFolder(), "favouredConfs.properties");
			if (!favouredCamConfSettingsFile.exists()) {
				favouredCamConfSettingsFile.createNewFile();
			}
			favouredCamConfSettings = new FileBackedProperties(favouredCamConfSettingsFile);
			beanSession.getVelocityContextService().getGlobalContext().put("favouredCamConfSettings", favouredCamConfSettings);

			context.setErrorHandler(new ErrorHandler() {
				private final AbstractErrorHandlingServlet eh = new AbstractErrorHandlingServlet() {
					private static final long serialVersionUID = -30520483617261093L;
				};

				@Override
				protected void handleErrorPage(final HttpServletRequest request, final Writer writer, final int code,
						final String message) {
					eh.serveGenericErrorPage(request, writer, code, message);
				}
			});

			if (requireAuthCredentials != null && requireAuthCredentials.length > 1 && requireAuthCredentials[0] != null
					&& requireAuthCredentials[1] != null && !requireAuthCredentials[0].trim().isEmpty()
					&& !requireAuthCredentials[1].trim().isEmpty()) {
				context.addFilter(
						new FilterHolder(new BasicAuthFilter(requireAuthCredentials[0], requireAuthCredentials[1])),
						"/*", EnumSet.of(DispatcherType.REQUEST));
			}
			context.addFilter(new FilterHolder(new RootAuthFilter("pi", "bbr")), "/", EnumSet.of(DispatcherType.REQUEST));

			context.addFilter(new FilterHolder(new CameraChoiceFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));

			AtomicBoolean scriptDumpVars = new AtomicBoolean(true);
			scriptManagementService = new ScriptsManagementServiceImpl(scriptsFolder, logger);
			scriptExecService = new ScriptExecutionServiceImpl(logger);
			scriptExecWebSocketNotifier = new ScriptExecWebSocketNotifier(logger, scriptDumpVars);

			context.addServlet(new ServletHolder(new ScriptExecutionReportingWebSocketServlet(scriptExecService, logger)), "/scriptws");
			context.addServlet(new ServletHolder(new ScriptingServlet(scriptManagementService, scriptExecService, scriptExecWebSocketNotifier, scriptDumpVars)), "/scripts/*");

			// context.addServlet(new ServletHolder(new ImagesServlet(this, imagesFolder, logger)), "/img/*");
			context.addServlet(new ServletHolder(new StaticsResourcesServlet()), "/static/*");
			context.addServlet(new ServletHolder(new CameraControlServlet(favouredCamConfSettings)), "/");
			context.addServlet(new ServletHolder(new DevModeServlet(this)), "/devmode/*");
			context.addServlet(new ServletHolder(new LiveViewServlet()), "/stream.mjpeg");

			server.setHandler(context);

			List<CameraListItemBean> list = new CameraDetectorImpl().detectCameras();
			if (list.size() > 0) {
				GP2PortInfoList portList = new GP2PortInfoList();
				GP2PortInfoList.GP2PortInfo gp2PortInfo = portList.getByPath(list.get(0).getPortName());
				if (gp2PortInfo != null) {
					beanSession.setCamera(new GP2CameraImpl(gp2PortInfo));
					logger.info("A camera is found  ! :) I connect the " + list.get(0).getCameraModel());
				} else {
					logger.info("No camer is connected :( ");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		logger.info("Initializing: done.");
	}

	public void reReadTemplates() {
		try {
			BeanSession.getInstance().setTemplateEngine(makeTemplateEngine());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected TemplateEngine makeTemplateEngine() throws IOException {
		final Map<String, String> templatesRegistrations = new HashMap<String, String>();
		{
			final Properties templatesListProps = new Properties();
			templatesListProps.load(GPhoto2Server.class.getResourceAsStream(
					TemplateEngine.DEFAULT_TEMPLATES_CLASSPATH_PREFIX + "templates_list.properties"));
			for (Object templateNameObj : templatesListProps.keySet()) {
				String key = templateNameObj.toString();
				templatesRegistrations.put(key, templatesListProps.getProperty(key));
			}
		}
		return new TemplateEngine(templatesRegistrations);
	}

	public static void waitWhileLiveViewInProgress(int waitTime) {
		while (GPhoto2Server.liveViewInProgress.get() && waitTime-- > 0) {
			Thread.yield();
			WaitUtil.ensuredWait(100);
		}
	}

	public GPhoto2Server start() throws Exception {
		logger.info("Starting server...");
		this.server.start();
		logger.info("Starting server: done.");
		return this;
	}

	public GPhoto2Server stop() throws Exception {
		this.server.stop();
		return this;
	}

	public GPhoto2Server join() throws Exception {
		this.server.join();
		return this;
	}
}
