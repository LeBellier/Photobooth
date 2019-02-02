package x.mvmn.gp2srv.camera.service.impl;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import x.leBellier.photobooth.BeanSession;
import x.mvmn.gp2srv.camera.CameraProvider;
import x.mvmn.gp2srv.camera.CameraService;
import x.mvmn.jlibgphoto2.api.CameraConfigEntryBean;
import x.mvmn.jlibgphoto2.api.CameraFileSystemEntryBean;
import x.mvmn.jlibgphoto2.api.GP2Camera.GP2CameraCaptureType;
import x.mvmn.jlibgphoto2.api.GP2Camera.GP2CameraEventType;

public class CameraServiceImpl implements CameraService, Closeable {

	private final CameraProvider cameraProvider;
	protected byte[] mockPicture;

	public CameraServiceImpl(final CameraProvider camera) {
		this.cameraProvider = camera;
	}

	public void close() {
		cameraProvider.getCamera().close();
	}

	public synchronized byte[] capturePreview() {
		if (!Objects.isNull(mockPicture)) {
			return mockPicture;
		}
		return cameraProvider.getCamera().capturePreview();
	}

	public synchronized CameraServiceImpl releaseCamera() {
		cameraProvider.getCamera().release();
		return this;
	}

	public synchronized CameraFileSystemEntryBean capture() {
		return cameraProvider.getCamera().captureImage();
	}

	public synchronized CameraFileSystemEntryBean capture(final GP2CameraCaptureType captureType) {
		return cameraProvider.getCamera().capture(captureType);
	}

	public synchronized String getSummary() {
		return cameraProvider.getCamera().getSummary();
	}

	public GP2CameraEventType waitForSpecificEvent(int timeout, GP2CameraEventType expectedEventType) {
		return cameraProvider.getCamera().waitForSpecificEvent(timeout, expectedEventType);
	}

	public GP2CameraEventType waitForEvent(int timeout) {
		return cameraProvider.getCamera().waitForEvent(timeout);
	}

	public synchronized List<CameraConfigEntryBean> getConfig() {
		return cameraProvider.getCamera().getConfig();
	}

	public synchronized CameraServiceImpl setConfig(CameraConfigEntryBean configEntry) {
		cameraProvider.getCamera().setConfig(configEntry);
		return this;
	}

	public synchronized List<CameraFileSystemEntryBean> filesList(final String path, boolean includeFiles, boolean includeFolders, boolean recursive) {
		return cameraProvider.getCamera().listCameraFiles(path, includeFiles, includeFolders, recursive);
	}

	public synchronized CameraServiceImpl fileDelete(final String filePath, final String fileName) {
		cameraProvider.getCamera().deleteCameraFile(filePath, fileName);
		return this;
	}

	public CameraProvider getCameraProvider() {
		return cameraProvider;
	}

	public String downloadFile(final String cameraFilePath, final String cameraFileName, final File downloadFolder) {
		String result = null;
		byte[] content = fileGetContents(cameraFilePath, cameraFileName);
		File targetFile = new File(downloadFolder, cameraFileName);
		long i = 0;
		while (targetFile.exists()) {
			System.out.println(targetFile.getAbsolutePath());

			int dotIndex = cameraFileName.lastIndexOf(".");
			if (dotIndex > 0) {
				targetFile = new File(downloadFolder, cameraFileName.substring(0, dotIndex) + "_" + (i++) + "." + cameraFileName.substring(dotIndex + 1));
			} else {
				targetFile = new File(downloadFolder, cameraFileName + (i++));
			}
		}
		try {
			FileUtils.writeByteArrayToFile(targetFile, content);
			result = "Ok";
		} catch (Exception e) {
			result = "Error: " + e.getClass().getName() + " " + e.getMessage();
		}
		return result.trim();
	}

	public String downloadFile(final String cameraFilePath, final String cameraFileName, final File downloadFolder, final String downloadFileName) {
		String result = null;
		byte[] content = fileGetContents(cameraFilePath, cameraFileName);
		File targetFile = new File(downloadFolder, downloadFileName);
		long i = 0;
		while (targetFile.exists()) {
			System.out.println(targetFile.getAbsolutePath());

			int dotIndex = cameraFileName.lastIndexOf(".");
			if (dotIndex > 0) {
				targetFile = new File(downloadFolder, cameraFileName.substring(0, dotIndex) + "_" + (i++) + "." + cameraFileName.substring(dotIndex + 1));
			} else {
				targetFile = new File(downloadFolder, cameraFileName + (i++));
			}
		}
		try {
			FileUtils.writeByteArrayToFile(targetFile, content);
			result = "Ok";
		} catch (Exception e) {
			result = "Error: " + e.getClass().getName() + " " + e.getMessage();
		}
		return result.trim();
	}

	public synchronized byte[] fileGetContents(final String filePath, final String fileName) {
		return cameraProvider.getCamera().getCameraFileContents(filePath, fileName);
	}

	public synchronized byte[] fileGetThumb(final String filePath, final String fileName) {
		return cameraProvider.getCamera().getCameraFileContents(filePath, fileName, true);
	}

	public Map<String, CameraConfigEntryBean> getConfigAsMap() {
		final List<CameraConfigEntryBean> config = this.getConfig();
		final Map<String, CameraConfigEntryBean> configMap = new TreeMap<String, CameraConfigEntryBean>();
		for (CameraConfigEntryBean configEntry : config) {
			configMap.put(configEntry.getPath(), configEntry);
		}
		return configMap;
	}

	public void extractBytes(String imageFilePath) throws IOException {
		if (imageFilePath == "null") {
			mockPicture = null;
			return;
		}
		/*mockPicture = IOUtils.toByteArray(this.getClass().getResourceAsStream("/x/mvmn/gp2srv/mock/picture.jpg"));

		// open image
		File imgPath = new File(imageFilePath);
		// get DataBufferBytes from Raster
		//DataBufferByte data = (DataBufferByte) ImageIO.read(imgPath).getRaster().getDataBuffer();
		//mockPicture = (data.getData());
		FileInputStream fileInputStream = new FileInputStream(imgPath);
		BeanSession.getInstance().getLogger().trace(fileInputStream.toString());
		mockPicture = IOUtils.toByteArray(fileInputStream);*/

		InputStream inputStream = null;
		try {
			// open image
			try {
				File imgPath = new File(imageFilePath);
				inputStream = new FileInputStream(imgPath);
			} catch (Exception e) {
				BeanSession.getInstance().getLogger().trace("Echec de la vie avec cette image : " + imageFilePath);
			}

			if (inputStream == null) {
				inputStream = this.getClass().getResourceAsStream("/x/mvmn/gp2srv/mock/picture.jpg");

			}

			mockPicture = IOUtils.toByteArray(inputStream);
			releaseCamera();
		} catch (Exception ex) {
			BeanSession.getInstance().getLogger().trace(ex);
		} finally {
			try {
				inputStream.close();
			} catch (IOException ex) {
				BeanSession.getInstance().getLogger().trace(ex);
			}
		}
	}
}
