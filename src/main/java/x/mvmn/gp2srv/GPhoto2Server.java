package x.mvmn.gp2srv;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.HashMap;
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
import x.mvmn.gp2srv.camera.CameraProvider;
import x.mvmn.gp2srv.camera.CameraService;
import x.mvmn.gp2srv.camera.service.impl.CameraServiceImpl;
import x.mvmn.gp2srv.mock.service.impl.MockCameraServiceImpl;
import x.mvmn.gp2srv.scripting.service.impl.ScriptExecutionServiceImpl;
import x.mvmn.gp2srv.scripting.service.impl.ScriptsManagementServiceImpl;
import x.mvmn.gp2srv.web.service.velocity.TemplateEngine;
import x.mvmn.gp2srv.web.service.velocity.VelocityContextService;
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
import x.mvmn.jlibgphoto2.api.GP2Camera;
import x.mvmn.lang.util.Provider;
import x.mvmn.lang.util.WaitUtil;
import x.mvmn.log.PrintStreamLogger;
import x.mvmn.log.api.Logger;
import x.mvmn.log.api.Logger.LogLevel;
import x.mvmn.util.FileBackedProperties;

public class GPhoto2Server implements Provider<TemplateEngine> {

	private static final String DEFAULT_CONTEXT_PATH = "/";
	private static final int DEFAULT_PORT = 8080;

	private final Server server;
	private final Logger logger;
	private volatile TemplateEngine templateEngine;
	private final VelocityContextService velocityContextService;
	private volatile GP2Camera camera;
	private final File userHome;
	private final File appHomeFolder;
	private final File imagesFolder;
	private final File scriptsFolder;
	private final File favouredCamConfSettingsFile;
	private final FileBackedProperties favouredCamConfSettings;
	private final ScriptsManagementServiceImpl scriptManagementService;
	private final ScriptExecutionServiceImpl scriptExecService;
	private final ScriptExecWebSocketNotifier scriptExecWebSocketNotifier;

	public static final AtomicBoolean liveViewEnabled = new AtomicBoolean(true);
	public static final AtomicBoolean liveViewInProgress = new AtomicBoolean(false);

	public GPhoto2Server(final Logger a_logger, CameraService cameraService, File imagesFolder) {
		this(a_logger, cameraService, imagesFolder, DEFAULT_PORT, DEFAULT_CONTEXT_PATH);
	}

	public GPhoto2Server(final Logger a_logger, CameraService cameraService, File imagesFolder, Integer port) {
		this(a_logger, cameraService, imagesFolder, port, DEFAULT_CONTEXT_PATH);
	}

	public GPhoto2Server(final Logger a_logger, CameraService cameraService, File imagesFolder, Integer port,
			String contextPath) {
		this(a_logger, cameraService, imagesFolder, port, null, contextPath);
	}

	public GPhoto2Server(final Logger a_logger, CameraService cameraService, File imagesFolder, Integer port,
			String[] requireAuthCredentials) {
		this(a_logger, cameraService, imagesFolder, port, requireAuthCredentials, DEFAULT_CONTEXT_PATH);
	}

	public GPhoto2Server(final Logger a_logger, CameraService a_cameraService, File a_imagesFolder, Integer port,
			String[] requireAuthCredentials, String contextPath) {
		try {
			if (a_logger == null || a_cameraService == null || a_imagesFolder == null)
				throw new RuntimeException("An argument is missing in the GPhoto2Server constructor");

			this.logger = a_logger;
			logger.info("Initializing...");

			if (contextPath == null) {
				contextPath = DEFAULT_CONTEXT_PATH;
			}
			if (port == null) {
				port = DEFAULT_PORT;
			}
			logger.info("Arguments are : " + port.toString() + ", " + logger.getLevel().toString() + ", "
					+ requireAuthCredentials + ", " + a_imagesFolder.getAbsolutePath());

			this.templateEngine = makeTemplateEngine();
			this.velocityContextService = new VelocityContextService();
			this.imagesFolder = a_imagesFolder;

			this.server = new Server(port);
			this.server.setStopAtShutdown(true);

			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath(contextPath);
			userHome = new File(System.getProperty("user.home"));
			appHomeFolder = new File(userHome, ".gp2srv");
			appHomeFolder.mkdir();

			logger.info("Images download folder: " + imagesFolder.getCanonicalPath());
			if (!imagesFolder.exists()) {
				imagesFolder.mkdirs();
			} else if (!imagesFolder.isDirectory()) {
				throw new RuntimeException("Not a directory: " + imagesFolder);
			}
			scriptsFolder = new File(appHomeFolder, "scripts");
			scriptsFolder.mkdirs();
			favouredCamConfSettingsFile = new File(appHomeFolder, "favouredConfs.properties");
			if (!favouredCamConfSettingsFile.exists()) {
				favouredCamConfSettingsFile.createNewFile();
			}
			favouredCamConfSettings = new FileBackedProperties(favouredCamConfSettingsFile);
			velocityContextService.getGlobalContext().put("favouredCamConfSettings", favouredCamConfSettings);

			context.setErrorHandler(new ErrorHandler() {
				private final AbstractErrorHandlingServlet eh = new AbstractErrorHandlingServlet(GPhoto2Server.this,
						GPhoto2Server.this.getLogger()) {
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
			context.addFilter(new FilterHolder(new RootAuthFilter("pi", "bbr")), "/",
					EnumSet.of(DispatcherType.REQUEST));
			final CameraProvider camProvider = a_cameraService.getCameraProvider();

			context.addFilter(
					new FilterHolder(new CameraChoiceFilter(camProvider, velocityContextService, this, logger)), "/*",
					EnumSet.of(DispatcherType.REQUEST));

			AtomicBoolean scriptDumpVars = new AtomicBoolean(true);
			scriptManagementService = new ScriptsManagementServiceImpl(scriptsFolder, logger);
			scriptExecService = new ScriptExecutionServiceImpl(logger);
			scriptExecWebSocketNotifier = new ScriptExecWebSocketNotifier(logger, scriptDumpVars);

			context.addServlet(
					new ServletHolder(new ScriptExecutionReportingWebSocketServlet(scriptExecService, logger)),
					"/scriptws");
			context.addServlet(new ServletHolder(new ScriptingServlet(a_cameraService, scriptManagementService,
					scriptExecService, scriptExecWebSocketNotifier, scriptDumpVars, velocityContextService, this,
					imagesFolder, logger)), "/scripts/*");

			// context.addServlet(new ServletHolder(new ImagesServlet(this, imagesFolder,
			// logger)), "/img/*");
			context.addServlet(new ServletHolder(new StaticsResourcesServlet(this, logger)), "/static/*");
			context.addServlet(new ServletHolder(new CameraControlServlet(a_cameraService, favouredCamConfSettings,
					velocityContextService, this, imagesFolder, logger)), "/");
			context.addServlet(new ServletHolder(new DevModeServlet(this)), "/devmode/*");
			context.addServlet(new ServletHolder(new LiveViewServlet(a_cameraService, logger)), "/stream.mjpeg");

			server.setHandler(context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		logger.info("Initializing: done.");
	}

	public void reReadTemplates() {
		try {
			this.templateEngine = makeTemplateEngine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected Logger makeLogger(final LogLevel logLevel) {
		return new PrintStreamLogger(System.out).setLevel(logLevel);
	}

	public Logger getLogger() {
		return this.logger;
	}

	public File getImagesFolder() {
		return this.imagesFolder;
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

	public TemplateEngine provide() {
		return templateEngine;
	}

	public GP2Camera getCamera() {
		return camera;
	}

	public void setCamera(GP2Camera camera) {
		this.camera = camera;
	}

	public boolean hasCamera() {
		return camera != null;
	}
}
