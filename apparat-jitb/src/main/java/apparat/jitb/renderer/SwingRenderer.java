package apparat.jitb.renderer;

import apparat.jitb.math.Twips;
import apparat.swf.Swf;

import javax.swing.*;

/**
 * @author Joa Ebert
 */
public class SwingRenderer implements IRenderer {
	public void prepare(final Swf swf) {
        final Runnable guiCreator = new Runnable() {
            final public void run() {
                final JFrame frame = new JFrame("jitB");
				final int width = Twips.toPixel(swf.frameSize().maxX());
				final int height = Twips.toPixel(swf.frameSize().maxY());
				//final Image screen = frame.createImage(width, height);

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(width,
						height);
                frame.setVisible(true);
            }
        };

        SwingUtilities.invokeLater(guiCreator);
	}
}
