/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package x.leBellier.photobooth;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

	// Constructeur privé
	private BeanSession() {
		logger = new PrintStreamLogger(System.out);

		userHome = new File(System.getProperty("user.home"));
		appHomeFolder = new File(userHome, ".gp2srv");
		appHomeFolder.mkdir();

		velocityContextService = new VelocityContextService();

	}

	//Instance unique pré-initialisée
	private static BeanSession INSTANCE = new BeanSession();

	/**
	 * Point d'accès pour l'instance unique du singleton
	 */
	public static synchronized BeanSession getInstance() {
		return INSTANCE;
	}

	private final ImageUtils imageUtils = new ImageUtils();
	private final Logger logger;
	private final File userHome;
	private final File appHomeFolder;
	private File imagesFolder;

	private volatile TemplateEngine templateEngine;
	private final VelocityContextService velocityContextService;

	private volatile GP2Camera camera;
	private CameraService cameraService;

	private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

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

	public VelocityContextService getVelocityContextService() {
		return velocityContextService;
	}

	public TemplateEngine getTemplateEngine() {
		return templateEngine;
	}

	public void setTemplateEngine(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public CameraService getCameraService() {
		return cameraService;
	}

	public void setCameraService(Boolean usemocks) {
		if (cameraService != null) { // final set
			return;
		}
		if (usemocks) {
			cameraService = new MockCameraServiceImpl();
		} else {
			cameraService = new CameraServiceImpl(this);
		}
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

	public static DateFormat getSdf() {
		return sdf;
	}

	public TemplateEngine provide() {
		return templateEngine;
	}

}
