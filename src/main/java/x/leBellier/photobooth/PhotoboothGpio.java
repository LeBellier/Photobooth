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
	StandBy, Snap, PhotoAssembly, WaitPrintAck, PositivePrintAck, NegativePrintAck
    }

    public final boolean EnabedPrinting = true;

    private StateMachine currentState = StateMachine.StandBy;
    private StateMachine lastState = StateMachine.StandBy;

    public StateMachine getCurrentState() {
	return currentState;
    }

    public void setCurrentState(StateMachine state) {
	lastState = currentState;
	currentState = state;

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
		StateMachine state_start = getCurrentState();
		if (lastState != state_start)
		    System.out.println("Current State = " + state_start);

		switch (state_start) {
		case StandBy:
		    if (lastState != state_start) {
			/* TODO: Set livestream image in video panel */
			gpio.setBtnLed();
		    }

		    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    if (br.ready()) {
			String s = br.readLine();
			if (s.contains("stop")) {
			    throw new InterruptedException();
			}
			setCurrentState(StateMachine.Snap);

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

			    // Debug
//			    if (snap == 0) {
//				// logger.debug("Blink long : " + beanSession.getInitTime());
//				blinkRampe(blinking, beanSession.getInitTime());
//			    } else {
//				// logger.debug("Blink court : " + beanSession.getIntervalTime());
//				blinkRampe(blinking, beanSession.getIntervalTime());
//			    }
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
			System.out.println("ready for next round");
			gpio.resetBlueLed();

			// Montage

			setCurrentState(StateMachine.PhotoAssembly);
		    } catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			setCurrentState(StateMachine.StandBy);
		    }
		    break;
		case PhotoAssembly:
		    System.out.println("Please wait I make assembly...");
		    gpio.setBlueLed();
		    path = String.format("%s/Montage%s.jpg", beanSession.getImagesFolder(),
			    beanSession.getSdf().format(new Date()));

		    System.out.println(String.format("%s/%s", beanSession.getImagesFolder(), "dessin.png"));
		    try {
			beanSession.getImageUtils().append4mariage(beanSession.getImagesFolder(), photoFilenames, path);
		    } catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			setCurrentState(StateMachine.StandBy);
		    }
		    /* TODO: Set Static image in video panel */
		    if (!EnabedPrinting) {
			setCurrentState(StateMachine.StandBy);
		    } else {
			System.out.println("Do you want to print ?");
			setCurrentState(StateMachine.WaitPrintAck);
		    }

		    break;
		case WaitPrintAck:
		    gpio.toggleBlueLed();
		    Thread.sleep(500);
		    System.out.println("Jattends");
		    break;
		case NegativePrintAck:
		    System.out.println("Jimprime pas  c'est de la merde");
		    setCurrentState(StateMachine.StandBy);
		    break;
		case PositivePrintAck:
		    beanSession.getImageUtils().printImage(path);
		    System.out.println("Jimprime");
		    setCurrentState(StateMachine.StandBy);
		    break;
		}
		if (state_start == getCurrentState())
		    lastState = state_start;

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
