package x.leBellier.photobooth;

public interface GpioService {

	public void setBtnLed();

	public void resetBtnLed();

	public void toggleBtnLed();

	public void setBlueLed();

	public void resetBlueLed();

	public void toggleBlueLed();

	public void close();

}
