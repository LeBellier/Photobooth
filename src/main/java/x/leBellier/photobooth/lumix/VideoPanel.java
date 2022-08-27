package x.leBellier.photobooth.lumix;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import x.leBellier.photobooth.BeanSession;

/**
 * A Swing video panel, created by rapidly changing the underlying BufferedImage
 */
public class VideoPanel extends JPanel {

    private static final long serialVersionUID = -6341634297820396905L;

    @Override
    public Dimension getPreferredSize() {
	return new Dimension(480, 480);
    }

    @Override
    public void paint(Graphics graphics) {
	BufferedImage buff = BeanSession.getInstance().getLiveStreamImage();
	if (buff != null) {
	    graphics.drawImage(buff, 0, 0, null);
	}
    }
}
