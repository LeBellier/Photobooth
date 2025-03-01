package x.mvmn.gp2srv.scripting.model;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import x.mvmn.gp2srv.camera.CameraService;
import x.mvmn.gp2srv.camera.service.impl.LightMeterImpl;
import x.mvmn.gp2srv.camera.service.impl.ScriptHelper;
import x.mvmn.gp2srv.scripting.service.impl.JexlMapContext;
import x.mvmn.gp2srv.scripting.service.impl.ScriptExecutionServiceImpl.ScriptExecutionObserver;
import x.mvmn.jlibgphoto2.api.CameraConfigEntryBean;
import x.mvmn.lang.util.WaitUtil;
import x.mvmn.log.api.Logger;

public class ScriptExecution implements Runnable {

	public static interface ScriptExecutionFinishListener {

		public void onFinish(ScriptExecution scriptExecution);
	}

	protected final ScriptStep[] steps;
	protected final List<String> errors = new ArrayList<String>();
	protected volatile String latestError;
	protected final JexlEngine engine;
	protected final JexlMapContext context;
	protected volatile boolean stopRequest = false;
	protected final ScriptExecutionObserver scriptExecutionObserver;
	protected final ScriptExecutionFinishListener finishListener;
	protected volatile int nextStep = 0;
	protected volatile int currentStep = 0;
	protected volatile long loopCount = 0;
	protected volatile long totalStepsPassed = 0;
	protected final String scriptName;
	protected final CameraService cameraService;
	protected volatile boolean stopOnError = false;
	protected volatile int afterStepDelay = 0;
	protected final File imgDownloadPath;

	public ScriptExecution(final CameraService cameraService, final File imgDownloadPath, final Logger logger, final String scriptName,
			final List<ScriptStep> steps, final JexlEngine engine, final ScriptExecutionObserver scriptExecutionObserver,
			final ScriptExecutionFinishListener finishListener) {
		this.steps = steps.toArray(new ScriptStep[steps.size()]);
		this.engine = engine;
		this.context = new JexlMapContext();
		context.set("__lightmeter", new LightMeterImpl(cameraService, logger));
		context.set("__helper", new ScriptHelper());
		this.scriptExecutionObserver = scriptExecutionObserver;
		this.finishListener = finishListener;
		this.scriptName = scriptName;
		this.cameraService = cameraService;
		this.imgDownloadPath = imgDownloadPath;
	}

	public ScriptStep[] getScriptSteps() {
		return steps;
	}

	public Set<String> getVariables() {
		return context.variableNames();
	}

	public Object getVariableValue(String variableName) {
		return context.get(variableName);
	}

	public Map<String, Object> dumpVariables(boolean asStrings) {
		Map<String, Object> result = new HashMap<String, Object>();

		for (String varName : getVariables()) {
			Object value = getVariableValue(varName);
			if (asStrings) {
				if (value != null) {
					if (value.getClass().isArray()) {
						StringBuilder valueArrayStr = new StringBuilder("[");
						for (int i = 0; i < Array.getLength(value); i++) {
							if (i > 0) {
								valueArrayStr.append(", ");
							}
							Object val = Array.get(value, i);
							valueArrayStr.append(val != null ? val.toString() : "null");
						}
						value = valueArrayStr.append("]").toString();
					} else {
						value = value.toString();
					}
				} else {
					value = null;
				}
			}
			result.put(varName, value);
		}

		return result;
	}

	protected void handleError(JexlMapContext context, String message) {
		long loopNum = getLoopCount();
		message = "Loop " + loopNum + ": " + message;
		context.set("__latestErrorLoop", loopNum);
		latestError = message;
		errors.add(message);
		if (stopOnError) {
			requestStop();
		}
	}

	protected void populateStepData(int stepNumber) {
		long ctm = System.currentTimeMillis();
		context.set("__currentTimeMillis", ctm);
		context.set("___currentStep", stepNumber);
		context.set("___totalStepsPassed", totalStepsPassed);
		context.set("___loopCount", loopCount);
		if (stepNumber == 0) {
			context.set("__loopStartTime", ctm);
		}
	}

	protected int getAndAdvanceNextStepNumber() {
		int stepNum = 0;
		if (nextStep >= steps.length) {
			nextStep = 0;
			loopCount++;
		}
		stepNum = nextStep++;
		return stepNum;
	}

	public void requestStop() {
		this.stopRequest = true;
	}

	public int getCurrentStep() {
		return currentStep;
	}

	public long getLoopCount() {
		return loopCount;
	}

	public long getTotalStepsPassed() {
		return totalStepsPassed;
	}

	public void run() {
		scriptExecutionObserver.onStart(this);
		while (!stopRequest) {
			int stepNumber = getAndAdvanceNextStepNumber();
			currentStep = stepNumber;
			populateStepData(stepNumber);
			ScriptStep currentStepObj = null;
			try {
				currentStepObj = steps[stepNumber];
				boolean execute = currentStepObj.evalCondition(engine, context);
				context.set("___evaldCondition", execute);
				CameraConfigEntryBean confEntry = null;
				Object evaluatedValue = null;
				if (execute) {
					confEntry = currentStepObj.getConfigEntryForEval(cameraService);
					evaluatedValue = currentStepObj.evalExpression(engine, context, confEntry);
				}
				context.set("___evaldExpression", evaluatedValue);
				scriptExecutionObserver.preStep(this);

				if (execute) {
					boolean stopRequest = currentStepObj.execute(cameraService, evaluatedValue, engine, context, confEntry, imgDownloadPath);
					totalStepsPassed++;
					if (stopRequest) {
						requestStop();
					}
				}
			} catch (JexlException e) {
				handleError(context, "Evaluation error on step #" + stepNumber + " " + currentStepObj + ": "
						+ ((e.getCause() != null ? e.getCause().getClass().getName() : "") + " " + e.getMessage()).trim());
			} catch (NumberFormatException e) {
				handleError(context, "Number format error on step #" + stepNumber + " " + currentStepObj + ": " + e.getMessage());
			} catch (Exception e) {
				handleError(context, "Error on step #" + stepNumber + " " + currentStepObj + ": " + (e.getClass().getName() + " " + e.getMessage()).trim());
			}
			if (afterStepDelay > 0) {
				WaitUtil.ensuredWait(afterStepDelay);
			}
			scriptExecutionObserver.postStep(this);
		}
		finishListener.onFinish(this);
		scriptExecutionObserver.onStop(this);
	}

	public String getScriptName() {
		return scriptName;
	}

	public List<String> getErrors() {
		return errors;
	}

	public String getLatestError() {
		return latestError;
	}

	public boolean isStopOnError() {
		return stopOnError;
	}

	public void setStopOnError(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}

	public int getAfterStepDelay() {
		return afterStepDelay;
	}

	public void setAfterStepDelay(int traceDelay) {
		this.afterStepDelay = traceDelay;
	}
}
