package x.leBellier.photobooth;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import x.leBellier.photobooth.impl.GpioMqtt;

/**
 *
 * @author Bruno
 */
public class PhotoboothGpio extends Thread {

    public enum StateMachine {
	StandBy, Snap, PrintPhoto, WaitPrintAck, PositivePrintAck, NegativePrintAck
    }

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
	// create gpio controller
	gpio = new GpioMqtt(this);
    }

    @Override
    public void run() {
	// keep program running until user aborts (CTRL-C)
	try {
	    BeanSession beanSession = BeanSession.getInstance();
	    List<String> photoFilenames = new LinkedList<String>();
	    int snap = 0;
	    // int coefDebug = 100;
	    String path = "";

	    System.out.println("Press enter to snap :p");
	    while (true) {
		System.out.println("Current State = " + state);

		switch (state) {
		case StandBy:
		    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    gpio.setBtnLed();
		    if (br.ready()) {
			String s = br.readLine();
			if (s.contains("stop")) {
			    throw new InterruptedException();
			}
			state = StateMachine.Snap;

		    }
		    break;
		case Snap:
		    try {
			gpio.resetBtnLed();
			snap = 0;
			photoFilenames.clear();

			/* TODO Set HTML sympa */
			int blinking = 350;
			while (snap < 4) {
			    gpio.setBtnLed();

			    if (snap == 0) {
				// logger.debug("Blink long : " + beanSession.getInitTime());
				blinkRampe(blinking, beanSession.getInitTime());
			    } else {
				// logger.debug("Blink court : " + beanSession.getIntervalTime());
				blinkRampe(blinking, beanSession.getIntervalTime());
			    }
			    gpio.resetBtnLed();
			    System.out.println("SNAP !!!!");

			    // take photo and save with gphoto2
			    Date date = new Date();
			    String output = String.format("photobooth%s.jpg", beanSession.getSdf().format(date));
			    captureDownload(output);
//							snippedLed.setState(true);
			    System.out.println(String.format("Take the picture %s", output));
			    // if success
			    snap += 1;
			    photoFilenames.add(output);

			}
			state = StateMachine.PrintPhoto;
		    } catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		    } finally {
			gpio.resetBlueLed();
			System.out.println("ready for next round");
			state = StateMachine.StandBy;
		    }
		    break;
		case PrintPhoto:
		    if (!EnabedPrinting) {
			state = StateMachine.StandBy;
		    } else {
			System.out.println("please wait while your photos print...");
			gpio.setBlueLed();
			path = String.format("%s/Montage%s.jpg", beanSession.getImagesFolder(),
				beanSession.getSdf().format(new Date()));

			System.out.println(String.format("%s/%s", beanSession.getImagesFolder(), "dessin.png"));
			beanSession.getImageUtils().append4mariage(beanSession.getImagesFolder(), photoFilenames, path);

			/* TODO: Set Static image in video panel */

			System.out.println("Do you want to print ?");
			state = StateMachine.WaitPrintAck;
		    }

		    break;
		case WaitPrintAck:
		    gpio.toggleBlueLed();
		    Thread.sleep(500);
		    System.out.println("Jattends");
		    break;
		case NegativePrintAck:
		    /* TODO: Set livestream image in video panel */
		    System.out.println("Jimprime pas  c'est de la merde");
		    state = StateMachine.StandBy;
		    break;
		case PositivePrintAck:
		    /* TODO: Set livestream image in video panel */
		    beanSession.getImageUtils().printImage(path);
		    System.out.println("Jimprime");
		    state = StateMachine.StandBy;
		    break;
		}

		// TODO: implement a reboot button
		// Wait to ensure that print queue doesn't pile up
		// TODO: check status of printer instead of using this arbitrary wait time

		Thread.sleep(1500);
	    }
	} catch (InterruptedException e) {
	    System.out.println(e.getMessage());
	} catch (IOException ex) {
	    System.out.println(ex.getMessage());
	} finally {
	    gpio.close();
	    System.exit(0);
	}
    }

    private void captureDownload(String dstFilename) {

	BeanSession beanSession = BeanSession.getInstance();
	BufferedImage buf = beanSession.getLiveStreamImage();
	String path = String.format("%s/%s", beanSession.getImagesFolder(), dstFilename);
	try {
	    ImageIO.write(buf, "JPEG", new File(path));
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    private void blink(long delay, long duration) throws InterruptedException {
	long fin = System.currentTimeMillis() + duration;
	gpio.setBtnLed();
	while (System.currentTimeMillis() < fin) {
	    gpio.toggleBtnLed();
	    Thread.sleep(delay / 2);
	}
	gpio.resetBtnLed();
    }

    private void blinkRampe(long startDelay, long totalDuration) throws InterruptedException {

	int nbStep = (int) (2 * totalDuration / startDelay - 1);
	// long debut = System.currentTimeMillis();
	// logger.trace("Debut :" + System.currentTimeMillis());
	gpio.setBtnLed();
	for (int i = 0; i < nbStep; i++) {
	    long currentDelay = startDelay - startDelay * i / (nbStep);
	    long fin = System.currentTimeMillis() + currentDelay;
	    gpio.toggleBtnLed();
	    while (System.currentTimeMillis() < fin) {
	    }

	}
	gpio.resetBtnLed();
	// logger.trace("Dur�e :" + (System.currentTimeMillis() - debut));
    }

}
