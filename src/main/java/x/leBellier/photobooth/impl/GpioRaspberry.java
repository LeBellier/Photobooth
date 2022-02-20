package x.leBellier.photobooth.impl;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import x.leBellier.photobooth.BeanSession;
import x.leBellier.photobooth.GpioService;
import x.leBellier.photobooth.PhotoboothGpio;
import x.mvmn.log.api.Logger;

public class GpioRaspberry implements GpioService, GpioPinListenerDigital {

	protected final Logger logger;

	protected final GpioController gpio;
	protected final GpioPinDigitalOutput printLed;
	protected final GpioPinDigitalOutput buttonLed;
	protected final GpioPinDigitalOutput snippedLed;
	protected final GpioPinDigitalInput btnSnip;
	protected final GpioPinDigitalInput btnReset;
	protected final GpioPinDigitalInput btnValidate;
	protected PhotoboothGpio photobooth;

	public GpioRaspberry(PhotoboothGpio a_photobooth) {
		photobooth = a_photobooth;
		logger = BeanSession.getInstance().getLogger();
		// create gpio controller
		gpio = GpioFactory.getInstance();

		// provision gpio pin #01 as an output pin and turn on
		printLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "MyLED", PinState.HIGH);
		buttonLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "MyLED", PinState.HIGH);
		snippedLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "MyLED", PinState.HIGH);

		btnSnip = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01, "btn Snipe", PinPullResistance.PULL_DOWN);
		btnSnip.addListener(this);
		btnReset = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, "btn reset", PinPullResistance.PULL_DOWN);
		btnReset.addListener(this);
		btnValidate = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, "btn validate", PinPullResistance.PULL_DOWN);
		btnValidate.addListener(this);

	}

	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		if (event != null && event.getState().isHigh()) {
			logger.trace("Bouton relach�");
			return;
		}

		if (event.getPin() == btnSnip && photobooth.getCurrentState() == StateMachine.StandBy) {
			photobooth.setCurrentState(StateMachine.SnapAsk);
		}
		if (photobooth.getCurrentState() == StateMachine.WaitPrintAck) {
			if (event.getPin() == btnReset) {
				photobooth.setCurrentState(StateMachine.NegativePrintAck);
			} else if (event.getPin() == btnValidate) {
				photobooth.setCurrentState(StateMachine.PositivePrintAck);
			}
		}
	}

	@Override
	public void setStateBtnLed(Boolean state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStateBlueLed(Boolean state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		gpio.shutdown();

	}

	private void blink(long delay, long duration, GpioPinDigitalOutput led, PinState blinkState)
			throws InterruptedException {
		long fin = System.currentTimeMillis() + duration;
		led.setState(blinkState);
		while (System.currentTimeMillis() < fin) {
			led.toggle();
			Thread.sleep(delay / 2);
		}
		led.setState(blinkState.isLow());
	}

	private void blinkRampe(long startDelay, long totalDuration, GpioPinDigitalOutput led, PinState blinkState)
			throws InterruptedException {

		int nbStep = (int) (2 * totalDuration / startDelay - 1);
		// long debut = System.currentTimeMillis();
		// logger.trace("Debut :" + System.currentTimeMillis());
		led.setState(blinkState);
		for (int i = 0; i < nbStep; i++) {
			long currentDelay = startDelay - startDelay * i / (nbStep);
			long fin = System.currentTimeMillis() + currentDelay;
			led.toggle();
			while (System.currentTimeMillis() < fin) {
			}

		}
		led.setState(blinkState);
		// logger.trace("Dur�e :" + (System.currentTimeMillis() - debut));
	}
}
