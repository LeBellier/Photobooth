package x.leBellier.photobooth;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;

public class ImageUtils {

	private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

	public static void main(String[] args) throws InterruptedException {

		System.out.println("please wait while your photos print...");
		List<String> photoFilenames = new LinkedList<String>();
		photoFilenames.add("capt0001.jpg");
		photoFilenames.add("capt0002.jpg");
		photoFilenames.add("capt0003.jpg");
		photoFilenames.add("capt0004.jpg");

		File imageDldFolder = new File("C:\\Users\\Bruno\\Desktop");
		append4(imageDldFolder, photoFilenames, sdf.format(new Date()));
	}

	public static void append4(File imageDldFolder, List<String> photoFilenames, String outFileSuffixe) {
		try {

			BufferedImage imageHaut = ImageUtils.appendH(imageDldFolder, photoFilenames.get(0), photoFilenames.get(1));
			BufferedImage imageBasse = ImageUtils.appendH(imageDldFolder, photoFilenames.get(2), photoFilenames.get(3));

			BufferedImage image = ImageUtils.appendV(imageHaut, imageBasse);

			ImageIO.write(image, "JPEG", new File(String.format("%s/Montage%s.jpg", imageDldFolder, outFileSuffixe)));

		} catch (IOException ex) {
			System.out.println(ex);
		}
	}

	// ASSEMBLAGE des deux images
	private static BufferedImage appendV(BufferedImage img1, BufferedImage img2) throws IOException {

		BufferedImage buf = null;
		if (img1 != null && img2 != null) {
			int w1 = img1.getWidth(null);
			int h1 = img1.getHeight(null);
			int w2 = img2.getWidth(null);
			int h2 = img2.getHeight(null);
			int hMax = 0;
			int wMax = 0;
			// hMax = h1 si h1>=h2  sinon hMax = h2
			hMax = (h1 >= h2) ? h1 : h2;
			wMax = w1 + w2;
			buf = new BufferedImage(wMax, hMax, BufferedImage.TYPE_INT_RGB); // ligne 27
			Graphics2D g2 = buf.createGraphics();
			g2.drawImage(img1, 0, 0, null);
			g2.drawImage(img2, w1, 0, null);
		}
		return buf;
	}

	// ASSEMBLAGE des deux images
	private static BufferedImage appendH(File imageDldFolder, String img1Filename, String img2Filename) throws IOException {
		BufferedImage img1 = readPhotoFile(imageDldFolder, img1Filename);
		BufferedImage img2 = readPhotoFile(imageDldFolder, img2Filename);

		BufferedImage buf = null;
		if (img1 != null && img2 != null) {
			int w1 = img1.getWidth(null);
			int h1 = img1.getHeight(null);
			int w2 = img2.getWidth(null);
			int h2 = img2.getHeight(null);
			int hMax = 0;
			int wMax = 0;
			// wMax = w1 si w1>=w2  sinon wMax = w2
			wMax = (w1 >= w2) ? w1 : w2;
			hMax = h1 + h2;
			buf = new BufferedImage(wMax, hMax, BufferedImage.TYPE_3BYTE_BGR); // ligne 27
			Graphics2D g2 = buf.createGraphics();
			g2.drawImage(img1, 0, 0, null);
			g2.drawImage(img2, 0, h1, null);
		}
		return buf;
	}

	private static BufferedImage resize(BufferedImage img, int newW, int newH) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
		g.dispose();
		return dimg;
	}

	private static BufferedImage readPhotoFile(File imageDldFolder, String filename) throws IOException {
		String photoPath = String.format("%s/%s", imageDldFolder, filename);
		BufferedImage res = ImageUtils.resize(ImageIO.read(new File(photoPath)), 1296, 864);
		return res;
	}

	private static void printFreeMemorie() {
		Runtime runtime = Runtime.getRuntime();
		NumberFormat format = NumberFormat.getInstance();

		StringBuilder sb = new StringBuilder();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();

		sb.append("free memory: " + format.format(freeMemory / 1024) + "\n");
		sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\n");
		sb.append("max memory: " + format.format(maxMemory / 1024) + "\n");
		sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\n");

		System.out.println(sb.toString());
	}
}
