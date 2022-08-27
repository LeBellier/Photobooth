package x.leBellier.photobooth;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.SwingUtilities;

import org.apache.commons.jexl3.internal.TemplateEngine;

import x.leBellier.photobooth.lumix.VideoPanel;

/**
 *
 * @author Bruno
 */
public class BeanSession {

    // Instance unique pr�-initialis�e
    private static BeanSession INSTANCE = new BeanSession();

    /**
     * Point d'acc�s pour l'instance unique du singleton
     */
    public static synchronized BeanSession getInstance() {
	return INSTANCE;
    }

    private final DateFormat sdf;
    private final ImageUtils imageUtils;
    private final File userHome;
    private final File appHomeFolder;
    private Integer initTime;
    private Integer intervalTime;

    private volatile TemplateEngine templateEngine;

    private BufferedImage liveStreamImage;

    private PhotoboothGpio gpio; // Singleton
    private File imagesFolder; // Final

    private VideoPanel videoPanel;
    private HtmlEditorKitTest htmlPanel;

    private String cameraIp = "192.168.50.73";
    private Integer mask = 24;

    // Constructeur priv�
    private BeanSession() {
	sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
	imageUtils = new ImageUtils();

	userHome = new File(System.getProperty("user.home"));
	appHomeFolder = new File(userHome, ".gp2srv");
	appHomeFolder.mkdir();

	initTime = 5000;
	intervalTime = 2000;

	liveStreamImage = null;

	videoPanel = new VideoPanel();
	htmlPanel = new HtmlEditorKitTest();
    }

    public DateFormat getSdf() {
	return sdf;
    }

    public ImageUtils getImageUtils() {
	return imageUtils;
    }

    public File getUserHome() {
	return userHome;
    }

    public File getAppHomeFolder() {
	return appHomeFolder;
    }

    public Integer getInitTime() {
	return initTime;
    }

    public void setInitTime(Integer initTime) {
	this.initTime = initTime;
    }

    public Integer getIntervalTime() {
	return intervalTime;
    }

    public void setIntervalTime(Integer intervalTime) {
	this.intervalTime = intervalTime;
    }

    public BufferedImage getLiveStreamImage() {
	return liveStreamImage;
    }

    public void setLiveStreamImage(BufferedImage liveStreamImage) {
	this.liveStreamImage = liveStreamImage;
	SwingUtilities.invokeLater(videoPanel::repaint);
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

    public VideoPanel getVideoPanel() {
	return videoPanel;
    }

    public HtmlEditorKitTest getHtmlPanel() {
	return htmlPanel;
    }

    public String getCameraIp() {
	return cameraIp;
    }

    public void setCameraIp(String cameraIp) {
	this.cameraIp = cameraIp;
    }

    public Integer getMask() {
	return mask;
    }

    public void setMask(Integer mask) {
	this.mask = mask;
    }
}
