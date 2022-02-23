package x.leBellier.photobooth.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import x.leBellier.photobooth.BeanSession;
import x.leBellier.photobooth.GpioService;
import x.leBellier.photobooth.PhotoboothGpio;
import x.leBellier.photobooth.PhotoboothGpio.StateMachine;
import x.mvmn.log.api.Logger;

public class GpioMqtt implements GpioService, MqttCallback {

	protected final Logger logger;
	protected PhotoboothGpio photobooth;

	private MqttClient client;

	private static final String USER_AGENT = "Mozilla/5.0";
	private static final String POST_URL = "http://192.168.50.78/cm";

	public GpioMqtt(PhotoboothGpio a_photobooth) {
		photobooth = a_photobooth;
		logger = BeanSession.getInstance().getLogger();

		try {

			String broker = "tcp://192.168.50.111:1883";
			String topicName = "switchTC/cmnd/#";

			MqttConnectOptions mqOptions = new MqttConnectOptions();
			mqOptions.setCleanSession(true);

			client = new MqttClient(broker, "clientphotobooth");
			client.setCallback(this);
			client.connect(mqOptions); // connecting to broker
			client.subscribe(topicName); // subscribing to the topic name test/topic

			// client.close();

		} catch (Exception e) {
			System.err.println(e);
		} finally {
		}

	}

	@Override
	public void setBtnLed() {

		sendCommand("cmnd=Power2%20ON");
	}

	@Override
	public void resetBtnLed() {
		sendCommand("cmnd=Power2%20OFF");
	}

	@Override
	public void toggleBtnLed() {
		sendCommand("cmnd=Power2%20TOGGLE");
	}

	@Override
	public void setBlueLed() {
		sendCommand("cmnd=Power1%20ON");
	}

	@Override
	public void resetBlueLed() {
		sendCommand("cmnd=Power1%20OFF");
	}

	@Override
	public void toggleBlueLed() {
		sendCommand("cmnd=Power1%20TOGGLE");
	}

	public void sendCommand(String cmd) {
		try {
			logger.trace(String.format("Send Cmd : %s", cmd));

			URL obj = new URL(POST_URL);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);

			// For POST only - START
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(cmd.getBytes());
			os.flush();
			os.close();
			// For POST only - END

			int responseCode = con.getResponseCode();
//			logger.trace("POST Response Code :: " + responseCode);

			if (responseCode == HttpURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// print result
//				logger.trace(response.toString());
			} else {
				logger.error("POST request not worked");
			}

		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		logger.trace(String.format("Mqtt receive a message : Topic = %s / Message = %s", topic, message.toString()));
		switch (photobooth.getCurrentState()) {
		case StandBy:
			if (topic.contains("POWER1")) {
				photobooth.setCurrentState(StateMachine.Snap);
			}
			break;
		case Snap:
			break;
		case PrintPhoto:
			break;
		case WaitPrintAck:
			if (topic.contains("POWER3")) {
				photobooth.setCurrentState(StateMachine.NegativePrintAck);
			} else if (topic.contains("POWER2")) {
				photobooth.setCurrentState(StateMachine.PositivePrintAck);
			}
			break;
		case NegativePrintAck:
			break;
		case PositivePrintAck:
			break;
		}

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}

}
