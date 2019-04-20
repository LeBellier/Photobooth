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
import x.mvmn.log.api.Logger;

public class CameraServiceImpl implements CameraService, Closeable {

	private final CameraProvider cameraProvider;
	protected byte[] mockPicture;
	private int haveSendMock;

	private static final Logger logger = BeanSession.getInstance().getLogger();

	public CameraServiceImpl(final CameraProvider camera) {
		this.cameraProvider = camera;
	}

	@Override
	public void close() {
		cameraProvider.getCamera().close();
	}

	@Override
	public synchronized byte[] capturePreview() {
		byte[] res = new byte[1];
		if (!Objects.isNull(mockPicture)) {
			if (haveSendMock < 2) {// peut etre que 1
				haveSendMock++;
				res = mockPicture;
			}
		} else {
			res = cameraProvider.getCamera().capturePreview();
		}
		return res;
	}

	@Override
	public boolean isSlowRefresh() {
		return haveSendMock == 2;
	}

	@Override
	public synchronized CameraServiceImpl releaseCamera() {
		cameraProvider.getCamera().release();
		return this;
	}

	@Override
	public synchronized CameraFileSystemEntryBean capture() {
		return cameraProvider.getCamera().captureImage();
	}

	@Override
	public synchronized CameraFileSystemEntryBean capture(final GP2CameraCaptureType captureType) {
		return cameraProvider.getCamera().capture(captureType);
	}

	@Override
	public synchronized String getSummary() {
		return cameraProvider.getCamera().getSummary();
	}

	@Override
	public GP2CameraEventType waitForSpecificEvent(int timeout, GP2CameraEventType expectedEventType) {
		return cameraProvider.getCamera().waitForSpecificEvent(timeout, expectedEventType);
	}

	@Override
	public GP2CameraEventType waitForEvent(int timeout) {
		return cameraProvider.getCamera().waitForEvent(timeout);
	}

	@Override

	public synchronized List<CameraConfigEntryBean> getConfig() {
		return cameraProvider.getCamera().getConfig();
	}

	@Override
	public synchronized CameraServiceImpl setConfig(CameraConfigEntryBean configEntry) {
		cameraProvider.getCamera().setConfig(configEntry);
		return this;
	}

	@Override
	public synchronized List<CameraFileSystemEntryBean> filesList(final String path, boolean includeFiles, boolean includeFolders, boolean recursive) {
		return cameraProvider.getCamera().listCameraFiles(path, includeFiles, includeFolders, recursive);
	}

	@Override
	public synchronized CameraServiceImpl fileDelete(final String filePath, final String fileName) {
		cameraProvider.getCamera().deleteCameraFile(filePath, fileName);
		return this;
	}

	@Override
	public CameraProvider getCameraProvider() {
		return cameraProvider;
	}

	@Override
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

	@Override
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

	@Override
	public synchronized byte[] fileGetContents(final String filePath, final String fileName) {
		return cameraProvider.getCamera().getCameraFileContents(filePath, fileName);
	}

	@Override
	public synchronized byte[] fileGetThumb(final String filePath, final String fileName) {
		return cameraProvider.getCamera().getCameraFileContents(filePath, fileName, true);
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

	/**
	 *
	 * @param imageFilePath = "null" => delete the actual image and return to
	 * liveView
	 * @throws IOException
	 */
	@Override
	public synchronized void setImageForLiveView(String imageFilePath) {
		byte[] imageTemp = null;
		if (imageFilePath != "null") {
			InputStream inputStream = null;
			try {
				try {
					// open image externe
					File imgPath = new File(imageFilePath);
					if (imgPath.exists()) {
						inputStream = new FileInputStream(imgPath);
					} else {
						// open image interne
						inputStream = this.getClass().getResourceAsStream(imageFilePath);
					}
				} catch (Exception ex) {
					logger.trace(ex.getMessage());
					logger.trace("Echec de la vie avec cette image : " + imageFilePath);
				}

				if (inputStream == null) {
					inputStream = this.getClass().getResourceAsStream("/x/mvmn/gp2srv/mock/error.jpg");
				}

				imageTemp = IOUtils.toByteArray(inputStream);
				releaseCamera();
			} catch (Exception ex) {
				logger.trace(ex);
			} finally {
				try {
					inputStream.close();
				} catch (IOException ex) {
					logger.trace(ex);
				}
			}
		}
		if (mockPicture != imageTemp) {
			mockPicture = imageTemp;
			haveSendMock = 0;
			logger.trace("New picture set : " + imageFilePath);
		}
	}
}
