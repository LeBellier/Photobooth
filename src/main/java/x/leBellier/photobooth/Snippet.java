package x.leBellier.photobooth;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import x.leBellier.photobooth.lumix.StreamViewer;

public final class Snippet {

    private static Thread streamViewerThread;

    public static void main(String[] args) throws Exception {
	new Snippet(args);
    }

    public Snippet(String[] args) {
	parseCmdArgs(args);
	BeanSession bean = BeanSession.getInstance();
	try {
	    new PhotoboothGpio().start();
	    Appli window = new Appli();

	    System.out.println("Trying to connect to camera " + bean.getCameraIp() + " on subnet with mask size "
		    + bean.getMask());
	    try {
		StreamViewer streamViewer = new StreamViewer(bean.getCameraIp(), bean.getMask());
		streamViewerThread = new Thread(streamViewer);
		streamViewerThread.start();
	    } catch (SocketException e) {
		System.out.println("Socket creation error : " + e.getMessage());
		System.exit(1);
	    } catch (UnknownHostException e) {
		System.out.println("Cannot parse camera IP address: " + bean.getCameraIp() + ".");
		System.exit(2);
	    }

	    window.getFrame().setTitle("Lumix Live Stream viewer on " + bean.getCameraIp() + ":49199");
	    window.getFrame().setVisible(true);

	} catch (Exception e) {
	    System.err.println(e);
	} finally {
	}
    }

    protected void parseCmdArgs(String[] args) {
	final Options cliOptions = new Options();
	cliOptions.addOption("auth", true, "Require authentication (login:password).");
	cliOptions.addOption("initTime", true, "Entre bouton vert et 1ere photo( en ms)");
	cliOptions.addOption("intervalTime", true, "entre deux photos( en ms)");
	// cliOptions.addOption("gphoto2path", true, "Path to gphoto2 executable.");
	cliOptions.addOption("imgfolder", true, "Path to store downloaded images at.");
	cliOptions.addOption("logLevel", true, "Log level (TRACE, DEBUG, INFO, WARN, ERROR, SEVERE, FATAL).");
	cliOptions.addOption("port", true, "HTTP port.");
	cliOptions.addOption("usemocks", false, "Use mocks instead of real gphoto2 - for code testing.");
	cliOptions.addOption("ip", true, "Ip of the camera on network");
	cliOptions.addOption("mask", true, "Mask for network");

	CommandLine commandLine;
	try {
	    commandLine = new PosixParser().parse(cliOptions, args);
	    BeanSession beanSession = BeanSession.getInstance();

	    if (commandLine.hasOption("initTime")) {
		String timeOptionVal = commandLine.getOptionValue("initTime");
		try {
		    int initTime = Integer.parseInt(timeOptionVal.trim());

		    beanSession.setInitTime(initTime);

		} catch (NumberFormatException e) {
		    throw new RuntimeException("Unable to parse port parameter as integer: '" + timeOptionVal + "'.");
		}

	    }
	    if (commandLine.hasOption("intervalTime")) {
		String timeOptionVal = commandLine.getOptionValue("intervalTime");
		try {
		    int intervalTime = Integer.parseInt(timeOptionVal.trim());

		    beanSession.setIntervalTime(intervalTime);

		} catch (NumberFormatException e) {
		    throw new RuntimeException("Unable to parse port parameter as integer: '" + timeOptionVal + "'.");
		}
	    }

	    beanSession.setImagesFolder(commandLine.getOptionValue("imgfolder"));
	    System.out.println("Images download folder: " + beanSession.getImagesFolder().getCanonicalPath());

	    if (commandLine.hasOption("ip")) {
		beanSession.setCameraIp(commandLine.getOptionValue("ip"));
	    }

	    if (commandLine.hasOption("mask")) {
		String portOptionVal = commandLine.getOptionValue("mask");
		try {
		    beanSession.setMask(Integer.parseInt(portOptionVal.trim()));

		} catch (NumberFormatException e) {
		    throw new RuntimeException("Unable to parse port parameter as integer: '" + portOptionVal + "'.");
		}
	    }
//	    beanSession.setUseMock(commandLine.hasOption("usemocks"));

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
