package x.leBellier.photobooth;

import java.util.Collection;
import java.util.List;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinAnalog;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.GpioPinAnalogOutput;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinInput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.GpioPinShutdown;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioTrigger;

public class MockGpioController implements GpioController {

	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void export(PinMode mode, PinState defaultState, GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public void export(PinMode mode, GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public boolean isExported(GpioPin... pin) {
		// TODO Auto-generated method stub
		return false;
	}

	public void unexport(Pin... pin) {
		// TODO Auto-generated method stub
		
	}

	public void unexport(GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public void unexportAll() {
		// TODO Auto-generated method stub
		
	}

	public void setMode(PinMode mode, GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public PinMode getMode(GpioPin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isMode(PinMode mode, GpioPin... pin) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPullResistance(PinPullResistance resistance, GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public PinPullResistance getPullResistance(GpioPin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isPullResistance(PinPullResistance resistance, GpioPin... pin) {
		// TODO Auto-generated method stub
		return false;
	}

	public void high(GpioPinDigitalOutput... pin) {
		// TODO Auto-generated method stub
		
	}

	public boolean isHigh(GpioPinDigital... pin) {
		// TODO Auto-generated method stub
		return false;
	}

	public void low(GpioPinDigitalOutput... pin) {
		// TODO Auto-generated method stub
		
	}

	public boolean isLow(GpioPinDigital... pin) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setState(PinState state, GpioPinDigitalOutput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void setState(boolean state, GpioPinDigitalOutput... pin) {
		// TODO Auto-generated method stub
		
	}

	public boolean isState(PinState state, GpioPinDigital... pin) {
		// TODO Auto-generated method stub
		return false;
	}

	public PinState getState(GpioPinDigital pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public void toggle(GpioPinDigitalOutput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void pulse(long milliseconds, GpioPinDigitalOutput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void setValue(double value, GpioPinAnalogOutput... pin) {
		// TODO Auto-generated method stub
		
	}

	public double getValue(GpioPinAnalog pin) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addListener(GpioPinListener listener, GpioPinInput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void addListener(GpioPinListener[] listeners, GpioPinInput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void removeListener(GpioPinListener listener, GpioPinInput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void removeListener(GpioPinListener[] listeners, GpioPinInput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void removeAllListeners() {
		// TODO Auto-generated method stub
		
	}

	public void addTrigger(GpioTrigger trigger, GpioPinInput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void addTrigger(GpioTrigger[] triggers, GpioPinInput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void removeTrigger(GpioTrigger trigger, GpioPinInput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void removeTrigger(GpioTrigger[] triggers, GpioPinInput... pin) {
		// TODO Auto-generated method stub
		
	}

	public void removeAllTriggers() {
		// TODO Auto-generated method stub
		
	}

	public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider provider, Pin pin, String name,
			PinMode mode, PinPullResistance resistance) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider provider, Pin pin, PinMode mode,
			PinPullResistance resistance) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider provider, Pin pin, String name,
			PinMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider provider, Pin pin, PinMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, String name, PinMode mode,
			PinPullResistance resistance) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, PinMode mode,
			PinPullResistance resistance) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, String name, PinMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, PinMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider provider, Pin pin, String name,
			PinPullResistance resistance) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider provider, Pin pin, PinPullResistance resistance) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider provider, Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider provider, Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, String name, PinPullResistance resistance) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, PinPullResistance resistance) {
		// TODO: use this
		return null;
	}

	public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalInput provisionDigitalInputPin(Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider provider, Pin pin, String name,
			PinState defaultState) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider provider, Pin pin, PinState defaultState) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider provider, Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider provider, Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, String name, PinState defaultState) {
		// TODO: use this
		return null;
	}

	public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, PinState defaultState) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogInput provisionAnalogInputPin(GpioProvider provider, Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogInput provisionAnalogInputPin(GpioProvider provider, Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogInput provisionAnalogInputPin(Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogInput provisionAnalogInputPin(Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider provider, Pin pin, String name,
			double defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider provider, Pin pin, double defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider provider, Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider provider, Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, String name, double defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, double defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider provider, Pin pin, String name, int defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider provider, Pin pin, int defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider provider, Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider provider, Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, String name, int defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, int defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionPwmOutputPin(Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider provider, Pin pin, String name, int defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider provider, Pin pin, int defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider provider, Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider provider, Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, String name, int defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, int defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPin provisionPin(GpioProvider provider, Pin pin, String name, PinMode mode, PinState defaultState) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPin provisionPin(GpioProvider provider, Pin pin, String name, PinMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPin provisionPin(GpioProvider provider, Pin pin, PinMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPin provisionPin(Pin pin, String name, PinMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPin provisionPin(Pin pin, PinMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setShutdownOptions(GpioPinShutdown options, GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public void setShutdownOptions(Boolean unexport, GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public void setShutdownOptions(Boolean unexport, PinState state, GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public void setShutdownOptions(Boolean unexport, PinState state, PinPullResistance resistance, GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public void setShutdownOptions(Boolean unexport, PinState state, PinPullResistance resistance, PinMode mode,
			GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public Collection<GpioPin> getProvisionedPins() {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPin getProvisionedPin(Pin pin) {
		// TODO Auto-generated method stub
		return null;
	}

	public GpioPin getProvisionedPin(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void unprovisionPin(GpioPin... pin) {
		// TODO Auto-generated method stub
		
	}

	public boolean isShutdown() {
		// TODO Auto-generated method stub
		return false;
	}

	public void shutdown() {
		// TODO Auto-generated method stub
		
	}


}
