package x.leBellier.photobooth;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import x.mvmn.gp2srv.GPhoto2Server;
import x.mvmn.gp2srv.camera.CameraProvider;
import x.mvmn.gp2srv.camera.CameraService;
import x.mvmn.gp2srv.camera.service.impl.CameraServiceImpl;
import x.mvmn.gp2srv.mock.service.impl.MockCameraServiceImpl;
import x.mvmn.gp2srv.web.service.velocity.TemplateEngine;
import x.mvmn.gp2srv.web.service.velocity.VelocityContextService;
import x.mvmn.jlibgphoto2.api.GP2Camera;
import x.mvmn.lang.util.Provider;
import x.mvmn.log.PrintStreamLogger;
import x.mvmn.log.api.Logger;

/**
 *
 * @author Bruno
 */
public class BeanSession implements Provider<TemplateEngine>, CameraProvider {

	//Instance unique pré-initialisée
	private static BeanSession INSTANCE = new BeanSession();

	/**
	 * Point d'accès pour l'instance unique du singleton
	 */
	public static synchronized BeanSession getInstance() {
		return INSTANCE;
	}
	private final DateFormat sdf;
	private final ImageUtils imageUtils;
	private final Logger logger;
	private final File userHome;
	private final File appHomeFolder;
	private final VelocityContextService velocityContextService;

	private volatile TemplateEngine templateEngine;

	private volatile GP2Camera camera;
	private CameraService cameraService;

	private PhotoboothGpio gpio;  // Singleton
	private File imagesFolder;	// Final

	private boolean usemocks = false;

	// Constructeur privé
	private BeanSession() {
		sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		imageUtils = new ImageUtils();
		logger = new PrintStreamLogger(System.out);

		userHome = new File(System.getProperty("user.home"));
		appHomeFolder = new File(userHome, ".gp2srv");
		appHomeFolder.mkdir();

		velocityContextService = new VelocityContextService();
	}

	public DateFormat getSdf() {
		return sdf;
	}

	public ImageUtils getImageUtils() {
		return imageUtils;
	}

	public Logger getLogger() {
		return logger;
	}

	public File getUserHome() {
		return userHome;
	}

	public File getAppHomeFolder() {
		return appHomeFolder;
	}

	public VelocityContextService getVelocityContextService() {
		return velocityContextService;
	}

	public PhotoboothGpio getGpio() {
		if (gpio == null) { // singleton
			gpio = new PhotoboothGpio();
		}
		return gpio;
	}

	public void setGpio(PhotoboothGpio gpio) {
		if (gpio == null) { // final
			this.gpio = gpio;
		}
	}

	public File getImagesFolder() {
		return imagesFolder;
	}

	public void setImagesFolder(String imageDldPath) {
		if (imagesFolder != null) { // final set
			return;
		}
		if (imageDldPath == null || imageDldPath.trim().isEmpty()) {
			imageDldPath = new File(userHome, "gp2srv_images").getAbsolutePath();
		}
		imagesFolder = new File(imageDldPath);
		if (!imagesFolder.exists()) {
			imagesFolder.mkdirs();
		} else if (!imagesFolder.isDirectory()) {
			throw new RuntimeException("Not a directory: " + imagesFolder);
		}
	}

	public TemplateEngine getTemplateEngine() {
		return templateEngine;
	}

	public void setTemplateEngine() {
		try {
			final Map<String, String> templatesRegistrations = new HashMap<String, String>();
			{
				final Properties templatesListProps = new Properties();
				templatesListProps.load(GPhoto2Server.class.getResourceAsStream(
						TemplateEngine.DEFAULT_TEMPLATES_CLASSPATH_PREFIX + "templates_list.properties"));
				for (Object templateNameObj : templatesListProps.keySet()) {
					String key = templateNameObj.toString();
					templatesRegistrations.put(key, templatesListProps.getProperty(key));
				}
				this.templateEngine = new TemplateEngine(templatesRegistrations);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public CameraService getCameraService() {
		return cameraService;
	}

	public void setUseMock(Boolean usemocks) {
		if (cameraService == null) { // final set
			this.usemocks = usemocks;
			if (usemocks) {
				cameraService = new MockCameraServiceImpl(this);
			} else {
				cameraService = new CameraServiceImpl(this);
			}
		}
	}

	@Override
	public GP2Camera getCamera() {
		if (usemocks) {
			return null;
		} else {
			return camera;
		}
	}

	@Override
	public void setCamera(GP2Camera camera) {
		if (!usemocks) {
			this.camera = camera;
		}
	}

	@Override
	public boolean hasCamera() {
		if (usemocks) {
			return true;
		} else {
			return camera != null;
		}
	}

	@Override
	public TemplateEngine provide() {
		return templateEngine;
	}

}
