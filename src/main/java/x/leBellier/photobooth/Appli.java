package x.leBellier.photobooth;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JFrame;

import x.leBellier.photobooth.lumix.StreamViewer;
import x.leBellier.photobooth.lumix.VideoPanel;

public class Appli {

    private JFrame frame;
    static VideoPanel videoPanel;
    HtmlEditorKitTest htmlPanel;

    private static Thread streamViewerThread;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    Appli window = new Appli();

		    String cameraIp = "";
		    int cameraNetMaskBitSize = 24;

		    System.out.println("Trying to connect to camera " + cameraIp + " on subnet with mask size "
			    + cameraNetMaskBitSize);
		    try {
			StreamViewer streamViewer = new StreamViewer(videoPanel::displayNewImage, cameraIp,
				cameraNetMaskBitSize);
			streamViewerThread = new Thread(streamViewer);
			streamViewerThread.start();
		    } catch (SocketException e) {
			System.out.println("Socket creation error : " + e.getMessage());
			System.exit(1);
		    } catch (UnknownHostException e) {
			System.out.println("Cannot parse camera IP address: " + cameraIp + ".");
			System.exit(2);
		    }

		    window.frame.setTitle("Lumix Live Stream viewer on " + cameraIp + ":49199");
		    window.frame.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    /**
     * Create the application.
     */
    public Appli() {
	initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
	frame = new JFrame();
	frame.setBounds(100, 100, 650, 479);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.getContentPane().setLayout(new GridLayout(0, 2, 0, 0));

	videoPanel = new VideoPanel();
	frame.getContentPane().add(videoPanel);

	htmlPanel = new HtmlEditorKitTest();
	frame.getContentPane().add(htmlPanel);
    }

}
