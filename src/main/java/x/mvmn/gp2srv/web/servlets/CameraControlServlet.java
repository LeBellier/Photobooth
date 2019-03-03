package x.mvmn.gp2srv.web.servlets;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.context.Context;
import x.leBellier.photobooth.BeanSession;
import x.mvmn.gp2srv.GPhoto2Server;
import x.mvmn.gp2srv.camera.CameraService;
import x.mvmn.jlibgphoto2.api.CameraConfigEntryBean;
import x.mvmn.jlibgphoto2.api.CameraConfigEntryBean.CameraConfigEntryType;
import x.mvmn.jlibgphoto2.api.CameraFileSystemEntryBean;
import x.mvmn.jlibgphoto2.exception.GP2Exception;
import x.mvmn.log.api.Logger;

public class CameraControlServlet extends AbstractGP2Servlet {

	private static final long serialVersionUID = 7389681375772493366L;

	protected final Properties favouredCamConfSettings;
	private final BeanSession beanSession = BeanSession.getInstance();

	public CameraControlServlet(final Properties favouredCamConfSettings) {
		super();
		this.favouredCamConfSettings = favouredCamConfSettings;
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) {
		final String requestPath = request.getServletPath() + (request.getPathInfo() != null ? request.getPathInfo() : "");
		try {
			CameraService cameraService = beanSession.getCameraService();

			GPhoto2Server.liveViewEnabled.set(false);
			GPhoto2Server.waitWhileLiveViewInProgress(50);
			if ("/favsetting".equals(requestPath)) {
				final String key = request.getParameter("key");
				final String value = request.getParameter("value");
				final Boolean valueToSet = Boolean.valueOf(value.toLowerCase());
				if (valueToSet) {
					favouredCamConfSettings.setProperty(key, Boolean.TRUE.toString());
				} else {
					favouredCamConfSettings.remove(key);
				}
				serveJson(valueToSet, response);
			} else if ("/allsettingset".equals(requestPath)) {
				final String type = request.getParameter("type");
				final String key = request.getParameter("key");
				final String value = request.getParameter("value");

				final Map<String, CameraConfigEntryBean> configAsMap = getConfigAsMap(true);
				final CameraConfigEntryBean configEntry = configAsMap.get(key);
				CameraConfigEntryBean updatedConfigEntry = null;
				switch (CameraConfigEntryType.valueOf(type).getValueType()) {
					case FLOAT:
						updatedConfigEntry = configEntry.cloneWithNewValue(Float.parseFloat(value));
						break;
					case INT:
						updatedConfigEntry = configEntry.cloneWithNewValue(Integer.parseInt(value));
						break;
					case STRING:
						updatedConfigEntry = configEntry.cloneWithNewValue(value);
						break;
					default:
						returnForbidden(request, response);
						break;
				}
				if (updatedConfigEntry != null) {
					cameraService.setConfig(updatedConfigEntry);
					getConfigAsMap(false);
				}
				serveJson(updatedConfigEntry, response);
			} else if ("/deletefile".equals(requestPath)) {
				final String fileName = request.getParameter("name");
				final String filePath = request.getParameter("folder");

				cameraService.fileDelete(filePath, fileName);

				serveJson(Boolean.TRUE, response);
			} else if ("/capture".equals(requestPath)) {
				cameraService.capture();
				serveJson(Boolean.TRUE, response);
			} else if ("/capture_dld_del".equals(requestPath)) {
				CameraFileSystemEntryBean cfseb = cameraService.capture();
				cameraService.downloadFile(cfseb.getPath(), cfseb.getName(), beanSession.getImagesFolder());
				cameraService.fileDelete(cfseb.getPath(), cfseb.getName());

				serveJson(Boolean.TRUE, response);
			} else if ("/printLast".equals(requestPath)) {
				logger.trace("J'imprime la dernière photo");
				beanSession.getImageUtils().printLastAssembly();

				serveJson(Boolean.TRUE, response);
			} else if ("/downloadcamfile".equals(requestPath)) {
				final String fileName = request.getParameter("name");
				final String filePath = request.getParameter("folder");

				String result = cameraService.downloadFile(filePath, fileName, beanSession.getImagesFolder());
				response.setStatus(result.equalsIgnoreCase("ok") ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				serveJson(result, response);
			} else {
				returnNotFound(request, response);
			}
		} catch (final Exception e) {
			logger.error("Error processing POST to " + requestPath, e);
			serveGenericErrorPage(request, response, -1, e.getMessage());
		} finally {
			GPhoto2Server.liveViewEnabled.set(true);
		}
	}

	protected Context makeVelocityContext(final HttpServletRequest request, final HttpServletResponse response) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("request", request);
		params.put("response", response);
		params.put("currentTimeMillis", System.currentTimeMillis());
		return createContext(request, response, params);
	}

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) {
		final String path = request.getServletPath() + (request.getPathInfo() != null ? request.getPathInfo() : "");
		processRequestByPath(path, makeVelocityContext(request, response), request, response, logger);
	}

	protected void processRequestByPath(final String requestPath, final Context velocityContext, final HttpServletRequest request,
			final HttpServletResponse response, final Logger logger) {
		try {
			if (requestPath.equals("/") || requestPath.equals("") || requestPath.equals("/index") || requestPath.equals("/index.html")) {
				serveTempalteUTF8Safely("camera/index.vm", velocityContext, response, logger);
			} else if (requestPath.equals("/photoboothDriver")) {
				serveTempalteUTF8Safely("camera/photoboothDriver.vm", velocityContext, response, logger);
			} else if ("/camdisconnect".equals(requestPath)) {
				if (beanSession.getCamera() != null) {
					synchronized (beanSession) {
						if (beanSession.getCamera() != null) {
							beanSession.getCamera().close();
							beanSession.setCamera(null);
						}
					}
				}
				response.sendRedirect("/");
			} else if (requestPath.endsWith("/cameraConfig.json")) {
				final boolean reRead = Boolean.parseBoolean(request.getParameter("reRead"));
				final Map<String, CameraConfigEntryBean> cameraConfig = getConfigAsMap(!reRead);
				serveJson(cameraConfig, response);
			} else if (requestPath.endsWith("/favedConfigs.json")) {
				serveJson(favouredCamConfSettings, response);
			} else if (requestPath.endsWith("/browse.json")) {
				Map<String, Object> result = new HashMap<String, Object>();
				String path = request.getParameter("path");
				if (path == null || path.trim().isEmpty()) {
					path = "/";
				}
				if (!path.startsWith("/")) {
					path = "/" + path;
				}
				if (!path.endsWith("/")) {
					path += "/";
				}

				result.put("currentBrowsePath", path);
				final List<CameraFileSystemEntryBean> fileList = BeanSession.getInstance().getCameraService().filesList(path, true, false, false);
				Collections.sort(fileList);
				result.put("filesList", fileList);
				final List<CameraFileSystemEntryBean> folderList = BeanSession.getInstance().getCameraService().filesList(path, false, true, false);
				Collections.sort(folderList);
				result.put("folderList", folderList);
				serveJson(result, response);
			} else if (requestPath.endsWith("/camfilepreview")) {
				final String fileName = request.getParameter("name");
				final String filePath = request.getParameter("folder");

				byte[] fileContents;

				if (Boolean.valueOf(request.getParameter("thumb"))) {
					fileContents = BeanSession.getInstance().getCameraService().fileGetThumb(filePath, fileName);
				} else {
					fileContents = BeanSession.getInstance().getCameraService().fileGetContents(filePath, fileName);
				}

				response.setContentType("image/jpeg");
				response.getOutputStream().write(fileContents);
				response.flushBuffer();
			} else {
				returnNotFound(request, response);
			}
		} catch (final GP2Exception e) {
			serveGenericErrorPage(request, response, e.getCode(), e.getMessage());
		} catch (final Exception e) {
			logger.error("Error processing GET to " + requestPath, e);
			serveGenericErrorPage(request, response, -1, e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	protected Map<String, CameraConfigEntryBean> getConfigAsMap(final boolean useCache) {
		Map<String, CameraConfigEntryBean> configAsMap = null;
		if (useCache) {
			configAsMap = (Map<String, CameraConfigEntryBean>) BeanSession.getInstance().getVelocityContextService().getGlobalContext().get("lastReadCameraConfig");
		}
		if (configAsMap == null) {
			try {
				GPhoto2Server.liveViewEnabled.set(false);
				GPhoto2Server.waitWhileLiveViewInProgress(50);
				configAsMap = BeanSession.getInstance().getCameraService().getConfigAsMap();
				BeanSession.getInstance().getVelocityContextService().getGlobalContext().put("lastReadCameraConfig", configAsMap);
			} finally {
				GPhoto2Server.liveViewEnabled.set(true);
			}
		}

		return configAsMap;
	}
}
