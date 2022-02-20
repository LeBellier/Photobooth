package x.leBellier.photobooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.pi4j.io.gpio.PinState;

import x.leBellier.photobooth.GpioService.StateMachine;
import x.leBellier.photobooth.impl.GpioRaspberry;
import x.mvmn.jlibgphoto2.api.CameraFileSystemEntryBean;
import x.mvmn.jlibgphoto2.exception.GP2CameraBusyException;
import x.mvmn.log.api.Logger;

/**
 *
 * @author Bruno
 */
public class PhotoboothGpio extends Thread {

	protected final Logger logger;

	public final boolean EnabedPrinting = true;

	private StateMachine state = StateMachine.StandBy;

	public StateMachine getCurrentState() {
		return state;
	}

	public void setCurrentState(StateMachine state) {
		this.state = state;
	}

	private GpioService gpio;

	public PhotoboothGpio() {
		logger = BeanSession.getInstance().getLogger();
		// create gpio controller
		gpio = new GpioRaspberry(this);
	}

	@Override
	public void run() {
		// keep program running until user aborts (CTRL-C)
		try {
			logger.debug("Press enter to snap :p");
			while (true) {

				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				gpio.setStateBtnLed(false);
				if (br.ready()) {
					String s = br.readLine();
					if (s.contains("stop")) {
						throw new InterruptedException();
					}
					state = StateMachine.SnapAsk;

				}
				if (state == StateMachine.SnapAsk) {
					RunScript();
				}

				Thread.sleep(1500);
			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} finally {
			gpio.close();
			System.exit(0);
		}
	}

	private void RunScript() {
		state = StateMachine.Snap;
		int snap = 0;
		// int coefDebug = 100;
		List<String> photoFilenames = new LinkedList<String>();
		try {
			BeanSession beanSession = BeanSession.getInstance();

			beanSession.getCameraService().setImageForLiveView("/x/mvmn/gp2srv/web/static/attente.jpg");
			int blinking = 350;
			while (snap < 4) {
				gpio.setStateBtnLed(true);
				snippedLed.setState(false);
				if (snap == 0) {
					// logger.debug("Blink long : " + beanSession.getInitTime());
					blinkRampe(blinking, beanSession.getInitTime(), snippedLed, PinState.LOW);
				} else {
					// logger.debug("Blink court : " + beanSession.getIntervalTime());
					blinkRampe(blinking, beanSession.getIntervalTime(), snippedLed, PinState.LOW);
				}
				snippedLed.setState(false);
				logger.debug("SNAP !!!!");

				// take photo and save with gphoto2
				Date date = new Date();
				String output = String.format("photobooth%s.jpg", beanSession.getSdf().format(date));
				captureDownload(output);
				snippedLed.setState(true);

				// if sucess
				snap += 1;
				photoFilenames.add(output);
			}
			// Google Drive uploading #drive = gdrive.authorize_gdrive_api()
			// gdrive.upload_files_to_gdrive(drive, photo_files)
			if (EnabedPrinting) {
				logger.debug("please wait while your photos print...");
				printLed.setState(false);
				String path = String.format("%s/Montage%s.jpg", beanSession.getImagesFolder(),
						beanSession.getSdf().format(new Date()));

				logger.debug(String.format("%s/%s", beanSession.getImagesFolder(), "dessin.png"));
				beanSession.getImageUtils().append4mariage(beanSession.getImagesFolder(), photoFilenames, path);

				beanSession.getCameraService().setImageForLiveView(path);

				logger.debug("Do you want to print ?");
				state = StateMachine.WaitPrintAck;
				while (state == StateMachine.WaitPrintAck) {
					printLed.toggle();
					Thread.sleep(500);
					logger.trace("Jattends");

				}
				beanSession.getCameraService().setImageForLiveView("null");

				if (state == StateMachine.PositivePrintAck) {
					beanSession.getImageUtils().printImage(path);
					logger.trace("Jimprime");

				} else {
					logger.trace("Jimprime pas  c'est de la merde");
				}
				state = StateMachine.StandBy;

				// TODO: implement a reboot button
				// Wait to ensure that print queue doesn't pile up
				// TODO: check status of printer instead of using this arbitrary wait time
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} finally {
			snippedLed.setState(true);
			printLed.setState(true);
			logger.debug("ready for next round");
			gpio.setStateBtnLed(false);
			state = StateMachine.StandBy;
		}
	}

	private void captureDownload(String dstFilename) {
		int photoFail = 0;
		while (photoFail < 5) {
			try {
				BeanSession beanSession = BeanSession.getInstance();

				CameraFileSystemEntryBean cfseb = beanSession.getCameraService().capture();
				logger.trace("cfseb = " + cfseb.toString());

				beanSession.getCameraService().downloadFile(cfseb.getPath(), cfseb.getName(),
						beanSession.getImagesFolder(), dstFilename);
				beanSession.getCameraService().fileDelete(cfseb.getPath(), cfseb.getName());
				break;
			} catch (GP2CameraBusyException e) {
				photoFail++;
			}
		}

	}

}
