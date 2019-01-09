package x.leBellier.photobooth;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import x.mvmn.gp2srv.camera.CameraService;
import x.mvmn.jlibgphoto2.api.CameraFileSystemEntryBean;
import x.mvmn.log.api.Logger;

/**
 *
 * @author Bruno
 */
public class PhotoboothGpio extends Thread implements GpioPinListenerDigital {

	protected final CameraService cameraService;
	protected final File imageDldFolder;
	protected final Logger logger;
	protected final GpioController gpio;
	protected final GpioPinDigitalOutput printLed;
	protected final GpioPinDigitalOutput buttonLed;
	protected final GpioPinDigitalOutput snippedLed;
	protected final GpioPinDigitalInput btnSnip;
	protected final GpioPinDigitalInput btnReset;
	protected final boolean EnabedPrinting = true;
	private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

	private boolean isScriptRunning = false;

	public PhotoboothGpio(Logger logger, CameraService cameraService, File imageDldFolder) {

		this.cameraService = cameraService;
		this.imageDldFolder = imageDldFolder;
		this.logger = logger;

		// create gpio controller
		gpio = GpioFactory.getInstance();

		// provision gpio pin #01 as an output pin and turn on
		printLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_08, "MyLED", PinState.HIGH);
		buttonLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_09, "MyLED", PinState.HIGH);
		snippedLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "MyLED", PinState.HIGH);

		btnSnip = gpio.provisionDigitalInputPin(RaspiPin.GPIO_15, "btn Snipe", PinPullResistance.PULL_DOWN);
		btnSnip.addListener(this);

		btnReset = gpio.provisionDigitalInputPin(RaspiPin.GPIO_16, "btn reset", PinPullResistance.PULL_DOWN);

	}

	@Override
	public void run() {
		// keep program running until user aborts (CTRL-C)
		try {
			while (true) {

				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				buttonLed.setState(false);
				logger.debug("Press enter to snap :p");
				String s = br.readLine();
				handleGpioPinDigitalStateChangeEvent(null);

				Thread.sleep(8500);

			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		} catch (IOException ex) {
			logger.error(ex.getMessage());

		} finally {
			gpio.shutdown();
		}
	}

	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		if (event != null && event.getState().isHigh()) {
			logger.trace("Bouton relaché");
			return;
		}
		if (isScriptRunning) {
			logger.warn("Le script est en execution ! Attend un peu !");
			return;
		}

		isScriptRunning = true;
		int snap = 0;
		int coefDebug = 1;
		List<String> photoFilenames = new LinkedList<String>();
		try {
			while (snap < 4) {
				logger.debug("pose!");
				buttonLed.setState(true);
				snippedLed.setState(false);
				Thread.sleep(1500 * coefDebug);

				int blinking = 1000;
				blinkRampe(blinking, 15000 * coefDebug, snippedLed, PinState.LOW);

				snippedLed.setState(false);
				logger.debug("SNAP !!!!");

				// take photo and save with gphoto2
				Date date = new Date();
				logger.debug(sdf.format(date));
				String output = String.format("%s/photobooth%s.jpg", imageDldFolder, sdf.format(date));
				//captureDownload(output);
				snippedLed.setState(true);

				// if sucess
				snap += 1;
				photoFilenames.add(output);
				// photo_files += ((photo_file,PHOTO_MIMETYPE),)
			}

			// Google Drive uploading #drive = gdrive.authorize_gdrive_api()
			// gdrive.upload_files_to_gdrive(drive, photo_files)
			//
			if (EnabedPrinting) {
				logger.debug("please wait while your photos print...");
				printLed.setState(false);

				// build image and send to printer
				ProcessBuilder pb = new ProcessBuilder("/home/pi/scripts/photobooth/assemble_and_print"); // TODO:
				// sudo
				// is
				// needed
				// ?
				Process p = pb.start();
				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					logger.debug(line);
				}

				// TODO: implement a reboot button
				// Wait to ensure that print queue doesn't pile up
				// TODO: check status of printer instead of using this arbitrary wait time
			}

			printLed.setState(true);
			logger.debug("ready for next round");

		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(PhotoboothGpio.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			isScriptRunning = false;
		}
	}

	void captureDownload(String dstFilename) {
		CameraFileSystemEntryBean cfseb = cameraService.capture();
		cameraService.downloadFile(cfseb.getPath(), cfseb.getName(), imageDldFolder);
		cameraService.fileDelete(cfseb.getPath(), cfseb.getName());
	}

	void blink(long delay, long duration, GpioPinDigitalOutput led, PinState blinkState) throws InterruptedException {
		long fin = System.currentTimeMillis() + duration;
		led.setState(blinkState);
		while (System.currentTimeMillis() < fin) {
			led.toggle();
			Thread.sleep(delay / 2);
		}
		led.setState(blinkState.isLow());
	}

	void blinkRampe(long delayMax, long durationTotal, GpioPinDigitalOutput led, PinState blinkState) throws InterruptedException {
		int nbStep = 30;
		//logger.trace("Debut :" + System.currentTimeMillis());
		led.setState(blinkState);
		for (int i = 0; i < nbStep; i++) {
			long currentDelay = delayMax - delayMax * i / (nbStep) * 9 / 10;
			long currentDuration = durationTotal / nbStep;
			//logger.trace("Delay : " + currentDelay);
			long fin = System.currentTimeMillis() + currentDuration;
			while (System.currentTimeMillis() < fin) {
				led.toggle();
				Thread.sleep(currentDelay / 2);
			}
		}
		led.setState(blinkState);
		//logger.trace("Fin   :" + System.currentTimeMillis());
	}

}
