package x.leBellier.photobooth;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

public class ImageUtilsTest {

	private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
	// private String fileName;

	@Test
	public void testExecution() {
		System.out.println("Test is running");
		BeanSession beanSession = BeanSession.getInstance();
		List<String> photoFilenames = new LinkedList<String>();
		photoFilenames.add("capt0001.jpg");
		photoFilenames.add("capt0002.jpg");
		photoFilenames.add("capt0003.jpg");
		photoFilenames.add("capt0004.jpg");

		try {
			// Get folder path from resources folder
			ClassLoader classLoader = getClass().getClassLoader();

			File file = new File(classLoader.getResource("x/leBellier/photobooth/capt0001.jpg").getFile());
			beanSession.setImagesFolder(file.getParent());
			File imageDldFolder = file.getParentFile();
			String date = sdf.format(new Date());
			String path = String.format("%s/Montage%s.jpg", imageDldFolder, date);

			beanSession.getImageUtils().append4(BeanSession.getInstance().getImagesFolder(), photoFilenames, path);

			BufferedImage imgRes = beanSession.getImageUtils().readPhotoFile(imageDldFolder,
					String.format("Montage%s.jpg", date));
			BufferedImage imgRef = beanSession.getImageUtils().readPhotoFile(imageDldFolder, "Montage.jpg");
			Assert.assertTrue(compareImages(imgRes, imgRef));

			new File(imageDldFolder, String.format("Montage%s.jpg", date)).delete();
		} catch (IOException ex) {
			Logger.getLogger(ImageUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
			Assert.assertNull(ex);
		}
	}

	@Test
	public void testExecutionMariage() {
		System.out.println("Test is running");
		BeanSession beanSession = BeanSession.getInstance();
		List<String> photoFilenames = new LinkedList<String>();
		photoFilenames.add("capt0001.jpg");
		photoFilenames.add("capt0002.jpg");
		photoFilenames.add("capt0003.jpg");
		photoFilenames.add("capt0004.jpg");

		try {
			// Get folder path from resources folder
			ClassLoader classLoader = getClass().getClassLoader();

			File file = new File(classLoader.getResource("x/leBellier/photobooth/capt0001.jpg").getFile());
			beanSession.setImagesFolder(file.getParent());
			File imageDldFolder = file.getParentFile();
			String date = sdf.format(new Date());
			String path = String.format("%s/Montage%s.jpg", imageDldFolder, date);

			long start = System.currentTimeMillis();

			beanSession.getImageUtils().append4mariage(BeanSession.getInstance().getImagesFolder(), photoFilenames,
					path);
			long fin = System.currentTimeMillis();

			System.out.println("Methode runnning during :" + (fin - start));

			BufferedImage imgRes = beanSession.getImageUtils().readPhotoFile(imageDldFolder,
					String.format("Montage%s.jpg", date));
			BufferedImage imgRef = beanSession.getImageUtils().readPhotoFile(imageDldFolder, "MontageMariage.jpg");
			Assert.assertTrue(compareImages(imgRes, imgRef));

			new File(imageDldFolder, String.format("Montage%s.jpg", date)).delete();
		} catch (IOException ex) {
			Logger.getLogger(ImageUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
			Assert.assertNull(ex);
		}
	}

	public static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
		// The images must be the same size.
		if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
			return false;
		}

		int width = imgA.getWidth();
		int height = imgA.getHeight();

		// Loop over every pixel.
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Compare the pixels for equality.
				if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
					return false;
				}
			}
		}

		return true;
	}

}
