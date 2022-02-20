package x.leBellier.photobooth;

import java.io.File;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import x.mvmn.gp2srv.camera.CameraService;
import x.mvmn.log.api.Logger;

public final class MqttTest implements MqttCallback {

	private MqttClient mqttClient;

	public static void main(String[] args) throws Exception {
		new MqttTest(args);
	}

	public MqttTest(String[] args) {

		try {

			String broker = "tcp://localhost:1883";
			String topicName = "test/topic";
			int qos = 1;

			mqttClient = new MqttClient(broker, String.valueOf(System.nanoTime()));

			MqttConnectOptions connOpts = new MqttConnectOptions();

			connOpts.setCleanSession(true); // no persistent session
			connOpts.setKeepAliveInterval(1000);

			MqttMessage message = new MqttMessage("E Sheeran".getBytes());

			// here ed sheeran is a message

			message.setQos(qos); // sets qos level 1
			message.setRetained(true); // sets retained message

			MqttTopic topic2 = mqttClient.getTopic(topicName);

			mqttClient.connect(connOpts); // connects the broker with connect options
			topic2.publish(message); // publishes the message to the topic(test/topic)

//			// We're using eclipse paho library so we've to go with MqttCallback
//			MqttClient client = new MqttClient("tcp://localhost:1883", "clientid");
//			client.setCallback(this);
//			MqttConnectOptions mqOptions = new MqttConnectOptions();
//			mqOptions.setCleanSession(true);
//			client.connect(mqOptions); // connecting to broker
//			client.subscribe("test/topic"); // subscribing to the topic name test/topic

		} catch (Exception e) {
			System.err.println(e);
		} finally {
		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("message is : " + message);

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}
}

class MqttBean {

	String[] auth = null;
	CameraService cameraService;
	File imagesFolder = null;
	Logger logger = null;
	Integer port = null;

	public MqttBean() {
	}

}
