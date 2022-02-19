package x.leBellier.photobooth;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 *
 * @author Bruno
 */
public class ImageUtils {

	private String assemblyFilePath = "";

	private int offsetW = 50;
	private int offsetH = 50;
	private int paddingW = 10;
	private int paddingH = 10;
	private int imgSizeW = 920;
	private int imgSizeH = 890;

	private class ImageBuffer {
		BufferedImage image = null;
	}

	private ImageBuffer imageHaut = null;
	private ImageBuffer imageBasse = null;
	private ImageBuffer imageFinale = null;

	/**
	 * Append 4 images.
	 *
	 * @param imagesFolder    : must contain images and will contains the result
	 * @param photoFilenames: must have 4 filenames
	 * @param outFilePath     : the output is "Montage%outFileSuffixe%.jpg"
	 * @throws IOException
	 */
	public void append4(File imagesFolder, List<String> photoFilenames, String outFilePath) throws IOException {

		imageHaut = new ImageBuffer();
		imageBasse = new ImageBuffer();
		imageFinale = new ImageBuffer();
		appendH(imagesFolder, photoFilenames.get(0), photoFilenames.get(1), imageHaut);
		appendH(imagesFolder, photoFilenames.get(2), photoFilenames.get(3), imageBasse);

		appendV(imageHaut, imageBasse, imageFinale);

		ImageIO.write(imageFinale.image, "JPEG", new File(outFilePath));
		assemblyFilePath = outFilePath;
	}

	/**
	 * Append 4 images.
	 *
	 * @param imagesFolder    : must contain images and will contains the result
	 * @param photoFilenames: must have 4 filenames
	 * @param outFilePath     : the output is "Montage%outFileSuffixe%.jpg"
	 * @throws IOException
	 */
	public void append4mariage(File imagesFolder, List<String> photoFilenames, String outFilePath) throws IOException {

		BufferedImage outRefImg = ImageIO.read(new File(String.format("%s/%s", imagesFolder, "dessin.png")));
		if (outRefImg != null) {
			BufferedImage buf = new BufferedImage(outRefImg.getWidth(), outRefImg.getHeight(),
					BufferedImage.TYPE_3BYTE_BGR); // ligne 27
			Graphics2D g2 = buf.createGraphics();

			g2.drawImage(outRefImg, 0, 0, null);

			String addinPath = "";

			Float ratio = (float) imgSizeW / imgSizeH;
			int widthTemp = 0;
			BufferedImage addin = null;
			for (byte i = 0; i < 4; i++) {
				addinPath = String.format("%s/%s", imagesFolder, photoFilenames.get(i));
				addin = ImageIO.read(new File(addinPath));

				widthTemp = (int) (addin.getHeight() * ratio);
				addin = addin.getSubimage((addin.getWidth() - widthTemp) / 2, 0, widthTemp, addin.getHeight());
				addin = resize(addin, imgSizeW, imgSizeH);

				if (outRefImg != null && addin != null) {
					g2.drawImage(addin, offsetW + (i & 1) * (paddingW + imgSizeW),
							offsetH + (i / 2 & 1) * (paddingH + imgSizeH), null);

				}

			}

			ImageIO.write(buf, "JPEG", new File(outFilePath));
			assemblyFilePath = outFilePath;
		}

	}

	/**
	 * Merge img1Filename and img2Filename from imageDldFolder Horizontally
	 *
	 * @param imageDldFolder
	 * @param img1Filename
	 * @param img2Filename
	 * @return
	 * @throws IOException
	 */
	private BufferedImage appendH(File imageDldFolder, String img1Filename, String img2Filename, ImageBuffer imageRes)
			throws IOException {
		BufferedImage img1 = readPhotoFile(imageDldFolder, img1Filename);
		BufferedImage img2 = readPhotoFile(imageDldFolder, img2Filename);
		int padding = 10;
		if (img1 != null && img2 != null) {
			int w1 = img1.getWidth(null);
			int h1 = img1.getHeight(null);
			int w2 = img2.getWidth(null);
			int h2 = img2.getHeight(null);
			int hMax = 0;
			int wMax = 0;
			// hMax = h1 si h1>=h2 sinon hMax = h2
			hMax = (h1 >= h2) ? h1 : h2;
			wMax = w1 + w2 + padding;
			imageRes.image = new BufferedImage(wMax, hMax, BufferedImage.TYPE_INT_RGB); // ligne 27
			Graphics2D g2 = imageRes.image.createGraphics();
			g2.setColor(Color.WHITE);
			g2.fillRect(w1, 0, w1 + padding, h1);
			g2.drawImage(img1, 0, 0, null);
			g2.drawImage(img2, w1 + padding, 0, null);
		}
		return null;
	}

	/**
	 * Merge img1 and img2 vertically
	 *
	 * @param img1
	 * @param img2
	 * @return
	 * @throws IOException
	 */
	private BufferedImage appendV(ImageBuffer img1, ImageBuffer img2, ImageBuffer imageRes) throws IOException {

		int padding = 10;
		if (img1 != null && img2 != null) {
			int w1 = img1.image.getWidth(null);
			int h1 = img1.image.getHeight(null);
			int w2 = img2.image.getWidth(null);
			int h2 = img2.image.getHeight(null);
			int hMax = 0;
			int wMax = 0;
			// wMax = w1 si w1>=w2 sinon wMax = w2
			wMax = (w1 >= w2) ? w1 : w2;
			hMax = h1 + h2;
			imageRes.image = new BufferedImage(wMax, hMax + padding, BufferedImage.TYPE_3BYTE_BGR); // ligne 27
			Graphics2D g2 = imageRes.image.createGraphics();
			g2.setColor(Color.WHITE);
			g2.fillRect(0, h1, w1, h1 + padding);
			g2.drawImage(img1.image, 0, 0, null);
			g2.drawImage(img2.image, 0, h1 + padding, null);
		}
		return null;
	}

	private BufferedImage resize(BufferedImage img, int newW, int newH) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
		g.dispose();
		return dimg;
	}

	public BufferedImage readPhotoFile(File imageDldFolder, String filename) throws IOException {
		String photoPath = String.format("%s/%s", imageDldFolder, filename);
		return resize(ImageIO.read(new File(photoPath)), 1296, 864);
	}

//	private void printFreeMemorie() {
//		Runtime runtime = Runtime.getRuntime();
//		NumberFormat format = NumberFormat.getInstance();
//
//		StringBuilder sb = new StringBuilder();
//		long maxMemory = runtime.maxMemory();
//		long allocatedMemory = runtime.totalMemory();
//		long freeMemory = runtime.freeMemory();
//
//		sb.append("free memory: " + format.format(freeMemory / 1024) + "\n");
//		sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\n");
//		sb.append("max memory: " + format.format(maxMemory / 1024) + "\n");
//		sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\n");
//
//		System.out.println(sb.toString());
//	}

	public void printLastAssembly() {
		printImage(assemblyFilePath);
	}

	public void printImage(String filename) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command("lp", "-d", "KODAK_EasyShare", filename);

			Process process = processBuilder.start();
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line).append("\n");
			}

			int exitVal = process.waitFor();
			System.out.println(output);
			if (exitVal == 0) {
				System.out.println("Success!");

			}
		} catch (Exception ex) {
			Logger.getLogger(ImageUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Merge img1Filename and img2Filename from imageDldFolder Horizontally
	 *
	 * @param imageDldFolder
	 * @param img1Filename
	 * @param x
	 * @param y
	 * @return
	 * @throws IOException
	 */
//	private BufferedImage writeAt(BufferedImage outRefImg, File imageDldFolder, String imgInFilename, int x, int y)
//			throws IOException {
//
//		String addinPath = String.format("%s/%s", imageDldFolder, imgInFilename);
//		BufferedImage addin = cadrage(ImageIO.read(new File(addinPath)));
//		addin = resize(addin, imgSize, imgSize);
//
//		if (outRefImg != null && addin != null) {
//			BufferedImage buf = new BufferedImage(outRefImg.getWidth(), outRefImg.getHeight(),
//					BufferedImage.TYPE_3BYTE_BGR); // ligne 27
//			Graphics2D g2 = buf.createGraphics();
//
//			g2.drawImage(outRefImg, 0, 0, null);
//			g2.drawImage(addin, offsety + y * (padding + imgSize), offsetx + x * (padding + imgSize), null);
//
//			return buf;
//		}
//		return null;
//
//	}

//	private BufferedImage cadrage(BufferedImage img) {
//		return img.getSubimage((img.getWidth() - img.getHeight()) / 2, 0, img.getHeight(), img.getHeight());
//	}
}
