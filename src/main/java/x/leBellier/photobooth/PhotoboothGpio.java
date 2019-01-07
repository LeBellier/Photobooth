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

public class PhotoboothGpio implements GpioPinListenerDigital {

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
	private static final DateFormat sdf = new SimpleDateFormat("yyyy - MM - dd HHmmss");

	public PhotoboothGpio(Logger logger, CameraService cameraService, File imageDldFolder) {

		this.cameraService = cameraService;
		this.imageDldFolder = imageDldFolder;
		this.logger = logger;

		// create gpio controller
		gpio = GpioFactory.getInstance();

		// provision gpio pin #01 as an output pin and turn on
		printLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);
		// provision gpio pin #01 as an output pin and turn on
		buttonLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "MyLED", PinState.HIGH);
		// provision gpio pin #01 as an output pin and turn on
		snippedLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "MyLED", PinState.HIGH);

		// provision gpio pin #02 as an input pin with its internal pull down resistor
		// enabled
		btnSnip = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, "btn Snipe", PinPullResistance.PULL_DOWN);
		btnSnip.addListener(this);

		// provision gpio pin #02 as an input pin with its internal pull down resistor
		// enabled
		btnReset = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, "btn reset", PinPullResistance.PULL_DOWN);

	}

	void run() {
		// keep program running until user aborts (CTRL-C)
		while (true) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("Press enter to snap :p");
				String s = br.readLine();
				handleGpioPinDigitalStateChangeEvent(null);
				Thread.sleep(8500);

			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			} catch (IOException ex) {
				java.util.logging.Logger.getLogger(PhotoboothGpio.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and
		// scheduled tasks)
		// gpio.shutdown(); <--- implement this method call if you wish to terminate the
		// Pi4J GPIO controller
	}

	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		int snap = 0;
		List<String> photoFilenames = new LinkedList<String>();
		while (snap < 4) {
			try {
				logger.debug("pose!");

				buttonLed.setState(false);
				snippedLed.setState(true);
				Thread.sleep(1500);
				snippedLed.blink(800, 3000);
				snippedLed.blink(400, 1000);
				snippedLed.blink(200, 1000);
				snippedLed.setState(true);
				logger.debug("SNAP !!!!");
				// take photo and save with gphoto2
				Date date = new Date();
				logger.debug(sdf.format(date));
				String output = String.format("%s/photobooth%s.jpg", imageDldFolder, sdf.format(date));
				captureDownload(output);
				snippedLed.setState(false);
				// if sucess
				snap += 1;

				// photo_files += ((photo_file,PHOTO_MIMETYPE),)
				// Google Drive uploading #drive = gdrive.authorize_gdrive_api()
				// gdrive.upload_files_to_gdrive(drive, photo_files)
				//
				if (EnabedPrinting) {
					logger.debug("please wait while your photos print...");
					printLed.setState(true);

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
				printLed.setState(false);
				logger.debug("ready for next round");
				buttonLed.setState(true);

			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			} catch (IOException ex) {
				java.util.logging.Logger.getLogger(PhotoboothGpio.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	void captureDownload(String dstFilename) {
		CameraFileSystemEntryBean cfseb = cameraService.capture();
		cameraService.downloadFile(cfseb.getPath(), cfseb.getName(), imageDldFolder);
		cameraService.fileDelete(cfseb.getPath(), cfseb.getName());
	}

}
