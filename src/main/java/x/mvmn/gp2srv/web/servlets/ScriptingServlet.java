package x.mvmn.gp2srv.web.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import x.leBellier.photobooth.BeanSession;
import x.mvmn.gp2srv.scripting.model.ScriptExecution;
import x.mvmn.gp2srv.scripting.model.ScriptStep;
import x.mvmn.gp2srv.scripting.service.impl.ScriptExecutionServiceImpl;
import x.mvmn.gp2srv.scripting.service.impl.ScriptsManagementServiceImpl;

public class ScriptingServlet extends AbstractGP2Servlet {

	private static final long serialVersionUID = -8824710026933050754L;
	protected static final Gson GSON = new GsonBuilder().create();

	protected final ScriptsManagementServiceImpl scriptManagementService;
	protected final ScriptExecutionServiceImpl scriptExecService;
	protected final ScriptExecWebSocketNotifier scriptExecWebSocketNotifier;
	protected final AtomicBoolean scriptDumpVars;

	public ScriptingServlet(ScriptsManagementServiceImpl scriptManagementService,
			ScriptExecutionServiceImpl scriptExecService, ScriptExecWebSocketNotifier scriptExecWebSocketNotifier, AtomicBoolean scriptDumpVars) {
		super();
		this.scriptManagementService = scriptManagementService;
		this.scriptExecService = scriptExecService;
		this.scriptExecWebSocketNotifier = scriptExecWebSocketNotifier;
		this.scriptDumpVars = scriptDumpVars;
	}

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) {
		final String path = request.getServletPath() + (request.getPathInfo() != null ? request.getPathInfo() : "");
		try {
			if ("/scripts/list".equals(path)) {
				serveJson(scriptManagementService.listScriptFiles(), response);
			} else if ("/scripts/get".equals(path)) {
				serveJson(scriptManagementService.load(request.getParameter("name")), response);
			} else if ("/scripts/exec/current".equals(path)) {
				ScriptExecution currentExecution = scriptExecService.getCurrentExecution();
				Map<String, Object> result = Collections.emptyMap();
				if (currentExecution != null) {
					result = ScriptExecWebSocketNotifier.toExecutionInfoDTO(currentExecution, "___", null, scriptDumpVars.get(), true);
				}
				serveJson(result, response);
			} else if ("/scripts/exec/current/steps".equals(path)) {
				List<ScriptStep> result;
				ScriptExecution currentExecution = scriptExecService.getCurrentExecution();
				if (currentExecution != null) {
					result = Arrays.asList(currentExecution.getScriptSteps());
				} else {
					result = Collections.emptyList();
				}
				serveJson(result, response);
			} else if ("/scripts/exec/finished".equals(path)) {
				ScriptExecution finishedExecution = scriptExecService.getLatestFinishedExecution();
				Map<String, Object> result = Collections.emptyMap();
				if (finishedExecution != null) {
					result = ScriptExecWebSocketNotifier.toExecutionInfoDTO(finishedExecution, "___", null, scriptDumpVars.get(), true);
				}
				serveJson(result, response);
			}
		} catch (final Exception e) {
			logger.error("Error processing GET to " + path, e);
			serveGenericErrorPage(request, response, -1, e.getMessage());
		}
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) {
		final String path = request.getServletPath() + (request.getPathInfo() != null ? request.getPathInfo() : "");
		try {
			if ("/scripts/put".equals(path)) {
				String scriptName = scriptManagementService.normalizeScriptName(request.getParameter("name"));

				ScriptStep[] script = null;
				try {
					script = GSON.fromJson(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8), ScriptStep[].class);
				} catch (Exception e) {
					logger.warn("Failed to parse incoming script steps ", e);
					serveGenericErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, e.toString());
				}
				if (script != null) {
					serveJson(scriptManagementService.save(scriptName, Arrays.asList(script)), response);
				}
			} else if ("/scripts/delete".equals(path)) {
				String scriptName = scriptManagementService.normalizeScriptName(request.getParameter("name"));
				serveJson(scriptManagementService.delete(scriptName), response);
			} else if ("/scripts/exec/dumpvars".equals(path)) {
				this.scriptDumpVars.set(Boolean.valueOf(request.getParameter("enable")));
			} else if ("/scripts/exec/afterstepdelay".equals(path)) {
				ScriptExecution execution = this.scriptExecService.getCurrentExecution();
				if (execution != null) {
					execution.setAfterStepDelay(Integer.parseInt(request.getParameter("value")));
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
			} else if ("/scripts/exec/stoponerror".equals(path)) {
				ScriptExecution execution = this.scriptExecService.getCurrentExecution();
				if (execution != null) {
					execution.setStopOnError(Boolean.valueOf(request.getParameter("enable")));
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
			} else if ("/scripts/exec/stop".equals(path)) {
				boolean result = false;
				ScriptExecution currentExecution = scriptExecService.getCurrentExecution();
				if (currentExecution != null) {
					currentExecution.requestStop();
					result = true;
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
				serveJson(result, response);
			} else if ("/scripts/exec/start".equals(path)) {
				ScriptExecution execution = null;
				final String scriptName = scriptManagementService.normalizeScriptName(request.getParameter("name"));
				List<ScriptStep> scriptContent = scriptManagementService.load(scriptName);
				String result;
				if (scriptContent != null) {
					execution = scriptExecService.execute(BeanSession.getInstance().getCameraService(), BeanSession.getInstance().getImagesFolder(), logger, scriptName, scriptContent, scriptExecWebSocketNotifier);
					if (execution != null) {
						result = "Script has been run";
					} else {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						result = "Another execution already in progress";
					}
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					result = "Script not found for name " + scriptName;
				}
				serveJson(result, response);
			}
		} catch (final Exception e) {
			logger.error("Error processing POST to " + path, e);
			serveGenericErrorPage(request, response, -1, e.getMessage());
		}
	}
}
