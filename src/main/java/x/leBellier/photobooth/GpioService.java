package x.leBellier.photobooth;

public interface GpioService {

	enum StateMachine {
		StandBy, SnapAsk, Snap, WaitPrintAck, PositivePrintAck, NegativePrintAck
	}

	public void setStateBtnLed(Boolean state);

	public void setStateBlueLed(Boolean state);

	public void close();

}
