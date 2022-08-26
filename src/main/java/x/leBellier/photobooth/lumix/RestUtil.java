package x.leBellier.photobooth.lumix;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class RestUtil {

    public static String sendGetCmd(String url) throws IOException {

	HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

	connection.setRequestMethod("GET");

	String response = "";
	Scanner scanner = new Scanner(connection.getInputStream());
	while (scanner.hasNextLine()) {
	    response += scanner.nextLine();
	    response += "\n";
	}
	scanner.close();

	return response;

    }

    public static String sendPostCmd(String url, String cmd) throws IOException {
	HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

	connection.setRequestMethod("POST");

	connection.setDoOutput(true);
	OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
	wr.write(cmd);
	wr.flush();

	int responseCode = connection.getResponseCode();
	if (responseCode == 200) {
	    System.out.println("POST was successful.");
	}
	String response = "";
	Scanner scanner = new Scanner(connection.getInputStream());
	while (scanner.hasNextLine()) {
	    response += scanner.nextLine();
	    response += "\n";
	}
	scanner.close();

	return response;
    }

}
