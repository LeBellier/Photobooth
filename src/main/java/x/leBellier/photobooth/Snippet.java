package x.leBellier.photobooth;

import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import x.mvmn.gp2srv.GPhoto2Server;
import x.mvmn.gp2srv.camera.CameraService;
import x.mvmn.log.api.Logger;
import x.mvmn.log.api.Logger.LogLevel;

public final class Snippet {

	public static void main(String[] args) throws Exception {
		new Snippet(args);
	}

	public Snippet(String[] args) {
		LauncherBean bean = parseCmdArgs(args);
		try {
			runScriptPhotobooth(bean.logger, bean.cameraService, bean.imagesFolder);

			GPhoto2Server server = new GPhoto2Server(bean.port, bean.auth);
			server.start().join();
		} catch (Exception e) {
			bean.logger.error(e);
		} finally {
		}
	}

	private PhotoboothGpio runScriptPhotobooth(final Logger a_logger, final CameraService a_cameraService, File a_imagesFolder) {
		PhotoboothGpio photoboothGpio = new PhotoboothGpio();
		photoboothGpio.start();

		return photoboothGpio;
	}

	protected LauncherBean parseCmdArgs(String[] args) {
		final Options cliOptions = new Options();
		cliOptions.addOption("auth", true, "Require authentication (login:password).");
		// cliOptions.addOption("gphoto2path", true, "Path to gphoto2 executable.");
		cliOptions.addOption("imgfolder", true, "Path to store downloaded images at.");
		cliOptions.addOption("logLevel", true, "Log level (TRACE, DEBUG, INFO, WARN, ERROR, SEVERE, FATAL).");
		cliOptions.addOption("port", true, "HTTP port.");
		cliOptions.addOption("usemocks", false, "Use mocks instead of real gphoto2 - for code testing.");

		CommandLine commandLine;
		try {
			commandLine = new PosixParser().parse(cliOptions, args);
			BeanSession beanSession = BeanSession.getInstance();
			Logger logger = beanSession.getLogger();
			if (commandLine.hasOption("logLevel")) {
				try {
					logger.setLevel(LogLevel.valueOf(commandLine.getOptionValue("logLevel").trim()));
				} catch (Exception e) {
					throw new RuntimeException("Unable to parse logLevel parameter: '"
							+ cliOptions.getOption("logLevel").getValue() + "'.s");
				}
			} else {
				logger.setLevel(LogLevel.TRACE);
			}

			String[] auth = null;
			if (commandLine.hasOption("auth")) {
				final String authStr = commandLine.getOptionValue("auth");
				final int separatorIndex = authStr.indexOf(":");
				if (separatorIndex > 0) {
					final String username = authStr.substring(0, separatorIndex);
					final String password = authStr.substring(separatorIndex + 1);
					auth = new String[]{username, password};
				}
			}

			beanSession.setImagesFolder(commandLine.getOptionValue("imgfolder"));
			logger.info("Images download folder: " + beanSession.getImagesFolder().getCanonicalPath());

			Integer port = null;
			if (commandLine.hasOption("port")) {
				String portOptionVal = commandLine.getOptionValue("port");
				try {
					int parsedPort = Integer.parseInt(portOptionVal.trim());
					if (parsedPort < 1 || parsedPort > 65535) {
						throw new RuntimeException("Bad port value: " + parsedPort);
					} else {
						port = parsedPort;
					}
				} catch (NumberFormatException e) {
					throw new RuntimeException("Unable to parse port parameter as integer: '" + portOptionVal + "'.");
				}
			}

			beanSession.setCameraService(commandLine.hasOption("usemocks"));

			LauncherBean bean = new LauncherBean();
			bean.auth = auth;
			bean.port = port;
			return bean;

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new LauncherBean();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new LauncherBean();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new LauncherBean();
		}

	}

}

class LauncherBean {

	String[] auth = null;
	CameraService cameraService;
	File imagesFolder = null;
	Logger logger = null;
	Integer port = null;

	public LauncherBean() {
	}

}
