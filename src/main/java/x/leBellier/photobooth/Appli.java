package x.leBellier.photobooth;

import java.awt.GridLayout;

import javax.swing.JFrame;

public class Appli {

    private JFrame frame;

    public JFrame getFrame() {
	return frame;
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
	frame.getContentPane().add(BeanSession.getInstance().getVideoPanel());
	frame.getContentPane().add(BeanSession.getInstance().getHtmlPanel());
    }

}
