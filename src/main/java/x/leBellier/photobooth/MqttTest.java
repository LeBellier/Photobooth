package x.leBellier.photobooth;

import java.io.File;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public final class MqttTest implements MqttCallback {

    private MqttClient mqttClient;

    public static void main(String[] args) throws Exception {
	new MqttTest(args);
    }

    public MqttTest(String[] args) {

	try {

	    String broker = "tcp://192.168.50.111:1883";
	    String topicName = "switchTC/cmnd";
	    int qos = 1;

	    mqttClient = new MqttClient(broker, "messagerTest");

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

	    // We're using eclipse paho library so we've to go with MqttCallback
	    MqttClient client = new MqttClient("tcp://192.168.50.111:1883", "clienttest");
	    client.setCallback(this);
	    MqttConnectOptions mqOptions = new MqttConnectOptions();
	    mqOptions.setCleanSession(true);
	    client.connect(mqOptions); // connecting to broker
	    client.subscribe("switchTC/cmnd/#"); // subscribing to the topic name test/topic
	    client.close();
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
    File imagesFolder = null;
    Integer port = null;

    public MqttBean() {
    }

}
