package x.mvmn.gp2srv.mock.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import x.mvmn.gp2srv.camera.CameraProvider;
import x.mvmn.gp2srv.camera.CameraService;
import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.jlibgphoto2.api.CameraConfigEntryBean;
import x.mvmn.jlibgphoto2.api.CameraFileSystemEntryBean;
import x.mvmn.jlibgphoto2.api.GP2Camera;
import x.mvmn.jlibgphoto2.api.GP2Camera.GP2CameraCaptureType;
import x.mvmn.jlibgphoto2.api.GP2Camera.GP2CameraEventType;
import x.mvmn.jlibgphoto2.exception.GP2Exception;

public class MockCameraServiceImpl implements CameraService {

	protected volatile boolean closed = false;
	protected final Map<String, CameraConfigEntryBean> initialConfig;
	protected final Map<String, CameraConfigEntryBean> config = new ConcurrentHashMap<String, CameraConfigEntryBean>();
	protected final Map<String, CameraFileSystemEntryBean> fsEntries = new ConcurrentHashMap<String, CameraFileSystemEntryBean>();
	protected final AtomicInteger counter = new AtomicInteger(0);
	protected final byte[] mockPicture;
	protected final CameraProvider cameraProviderMock;

	public MockCameraServiceImpl() {
		cameraProviderMock = new CameraProvider() {
			public void setCamera(GP2Camera camera) {
			}

			public boolean hasCamera() {
				return true;
			}

			public GP2Camera getCamera() {
				return null;
			}
		};
		try {
			final Map<String, CameraConfigEntryBean> mockConfig = new Gson().fromJson(
					IOUtils.toString(this.getClass().getResourceAsStream("/x/mvmn/gp2srv/mock/config.json")),
					new TypeToken<Map<String, CameraConfigEntryBean>>() {
					}.getType());
			initialConfig = Collections.unmodifiableMap(mockConfig);
			mockPicture = IOUtils.toByteArray(this.getClass().getResourceAsStream("/x/mvmn/gp2srv/mock/picture.jpg"));
			reset();
		} catch (Exception e) {
			throw new RuntimeException("Failed to set-up mock camera service", e);
		}
	}

	protected void checkClosed() {
		if (closed) {
			throw new RuntimeException("This GP2Camera instance has already been closed.");
		}
	}

	public void reset() {
		this.closed = false;
		config.clear();
		config.putAll(initialConfig);
		fsEntries.clear();
		fsEntries.put("/photos", new CameraFileSystemEntryBean("photos", "/", true));
		fsEntries.put("/photos/image10.jpg", new CameraFileSystemEntryBean("image10.jpg", "/photos", false));
		fsEntries.put("/photos/image11.jpg", new CameraFileSystemEntryBean("image11.jpg", "/photos", false));
		fsEntries.put("/photos/image12.jpg", new CameraFileSystemEntryBean("image12.jpg", "/photos", false));
		fsEntries.put("/photos/empty", new CameraFileSystemEntryBean("empty", "/photos", true));
		fsEntries.put("/photos/stuff", new CameraFileSystemEntryBean("stuff", "/photos", true));
		for (int i = 0; i < 100; i++) {
			fsEntries.put(String.format("/photos/stuff/image%02d.jpg", i),
					new CameraFileSystemEntryBean(String.format("image%02d.jpg", i), "/photos/stuff", false));
		}
		counter.set(0);
	}

	@Override
	public void close() {
		checkClosed();
		this.closed = true;
	}

	@Override
	public byte[] capturePreview() {
		checkClosed();

		byte[] result = mockPicture;

		if (System.getProperty("gp2mock.screengrab") != null) {
			try {
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				int captureWidth = Math.min(800, screenSize.width);
				int captureHeight = Math.min(600, screenSize.height);
				Rectangle screenRect = new Rectangle(screenSize.width - captureWidth, screenSize.height - captureHeight, captureWidth, captureHeight);
				BufferedImage capture = new Robot().createScreenCapture(screenRect);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(capture, "jpg", baos);
				result = baos.toByteArray();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return result;
	}

	@Override
	public boolean isSlowRefresh() {
		return false;
	}

	@Override
	public byte[] fileGetContents(String filePath, String fileName) {
		checkFileExists(filePath, fileName);
		return capturePreview();
	}

	@Override
	public CameraFileSystemEntryBean capture() {
		checkClosed();
		CameraFileSystemEntryBean newCapture = new CameraFileSystemEntryBean(String.format("photo%08d.jpg", counter.incrementAndGet()), "/photos", false);
		fsEntries.put(newCapture.getPath() + "/" + newCapture.getName(), newCapture);
		return newCapture;
	}

	@Override
	public CameraFileSystemEntryBean capture(GP2CameraCaptureType captureType) {
		if (!captureType.equals(GP2CameraCaptureType.IMAGE)) {
			throw new UnsupportedOperationException();
		}
		return capture();
	}

	@Override
	public String getSummary() {
		checkClosed();
		return "Mock";
	}

	@Override
	public GP2CameraEventType waitForSpecificEvent(int timeout, GP2CameraEventType expectedEventType) {
		checkClosed();
		return null;
	}

	@Override
	public GP2CameraEventType waitForEvent(int timeout) {
		checkClosed();
		return null;
	}

	protected CameraFileSystemEntryBean checkFileExists(String filePath, String fileName) {
		if (!filePath.endsWith("/")) {
			filePath = filePath + "/";
		}
		if (!filePath.startsWith("/")) {
			filePath = "/" + filePath;
		}
		final CameraFileSystemEntryBean fsEntry = fsEntries.get(filePath + fileName);
		if (fsEntry == null || !fsEntry.isFile()) {
			throw new GP2Exception(Gphoto2Library.GP_ERROR_FILE_NOT_FOUND, "File not found");
		}
		return fsEntry;
	}

	@Override
	public CameraService fileDelete(String filePath, String fileName) {
		checkClosed();
		if (!filePath.endsWith("/")) {
			filePath = filePath + "/";
		}
		if (!filePath.startsWith("/")) {
			filePath = "/" + filePath;
		}
		final CameraFileSystemEntryBean fsEntry = checkFileExists(filePath, fileName);
		fsEntries.remove(fsEntry.getPath() + "/" + fsEntry.getName());
		return this;
	}

	@Override
	public List<CameraFileSystemEntryBean> filesList(String path, boolean includeFiles, boolean includeFolders, boolean recursive) {
		checkClosed();

		final List<CameraFileSystemEntryBean> result;
		if (recursive) {
			result = new ArrayList<CameraFileSystemEntryBean>(fsEntries.values());
		} else {
			result = new ArrayList<CameraFileSystemEntryBean>();
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			for (CameraFileSystemEntryBean cfseb : fsEntries.values()) {
				if (cfseb.getPath().equals(path)) {
					result.add(cfseb);
				}
			}
		}
		for (Iterator<CameraFileSystemEntryBean> iterator = result.iterator(); iterator.hasNext();) {
			CameraFileSystemEntryBean t = iterator.next();
			if (!((t.isFile() && includeFiles) || (t.isFolder() && includeFolders))) {
				iterator.remove();
			}
		}

		return result;
	}

	@Override
	public List<CameraConfigEntryBean> getConfig() {
		checkClosed();
		return new ArrayList<CameraConfigEntryBean>(config.values());
	}

	@Override
	public CameraService setConfig(CameraConfigEntryBean configEntry) {
		checkClosed();
		config.put(configEntry.getPath(), configEntry);
		return this;
	}

	@Override
	public CameraService releaseCamera() {
		checkClosed();
		return this;
	}

	@Override
	public Map<String, CameraConfigEntryBean> getConfigAsMap() {
		final List<CameraConfigEntryBean> config = this.getConfig();
		final Map<String, CameraConfigEntryBean> configMap = new TreeMap<String, CameraConfigEntryBean>();
		for (CameraConfigEntryBean configEntry : config) {
			configMap.put(configEntry.getPath(), configEntry);
		}
		return configMap;
	}

	@Override
	public byte[] fileGetThumb(String filePath, String fileName) {
		return fileGetContents(filePath, fileName);
	}

	@Override
	public String downloadFile(String cameraFilePath, String cameraFileName, File downloadFolderPath) {
		return "Ok";
	}

	@Override
	public String downloadFile(String cameraFilePath, String cameraFileName, File downloadFolderPath, String downloadFileName) {
		return "Ok";
	}

	@Override
	public CameraProvider getCameraProvider() {
		return cameraProviderMock;
	}

	@Override
	public void setImageForLiveView(String imageFilePath) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
