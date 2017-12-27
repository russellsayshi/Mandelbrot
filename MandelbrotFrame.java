import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

/*
 * This class draws the bufferedimage provided
 * by the MandelbrotCalculator class onto an AWT
 * component. Decided to go AWT because I haven't
 * written it in years and wanted to go back
 * for old times' sake. Not a bad choice, even
 * though the buttons are all ugly lmao.
 *
 * This also has lots of threading stuff, that's just
 * so that it only repaints exactly when it needs to.
 * You can draw a rectangle across the frame to zoom in,
 * and that needs to get redrawn at 60FPS which is what
 * the thread is for.
 */
public class MandelbrotFrame extends Canvas {
	private BufferedImage image;
	private MandelbrotCalculator calc;
	private volatile Rectangle selectionArea = null;
	private Thread repaintThread;
	private Object repaintLock = new Object();

	public MandelbrotFrame() {
		calc = new MandelbrotCalculator(getWidth(), getHeight(), MandelbrotFrame.this::generateImage);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				calc.resize(getWidth(), getHeight());
			}
		});
		repaintThread = new Thread(() -> {
			try {
				while(true) {
					synchronized(repaintLock) {
						while(selectionArea == null) {
							repaintLock.wait();
						}
					}
					while(selectionArea != null) {
						Thread.sleep(16);
						repaint();
					}
				}
			} catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		});
		repaintThread.start();
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				selectionArea = new Rectangle(me.getX(), me.getY(), 0, 0);
				synchronized(repaintLock) {
					repaintLock.notifyAll();
				}
			}

			@Override
			public void mouseReleased(MouseEvent me) {
				if(selectionArea != null && selectionArea.width > 0 && selectionArea.height > 0) {
					calc.selectRectangle(selectionArea);
				}
				selectionArea = null;
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent me) {
				if(selectionArea == null) selectionArea = new Rectangle(me.getX(), me.getY(), 0, 0);
				selectionArea.setSize(me.getX() - selectionArea.x,
							me.getY() - selectionArea.y);
			}
		});
	}

	/*
	 * Generates a BufferedImage. Called as callback once calc is done.
	 */
	private void generateImage(int[][] data) {
		if(data == null || data.length == 0) {
			//no data sent
			System.err.println("No data!");
			image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			return;
		}
		if(image == null || image.getHeight() != data.length || image.getWidth() != data[0].length) {
			image = new BufferedImage(data[0].length, data.length, BufferedImage.TYPE_INT_RGB);
		}
		for(int y = 0; y < data.length; y++) {
			for(int x = 0; x < data[0].length; x++) {
				image.setRGB(x, y, data[y][x]);
			}
		}
	}

	public MandelbrotCalculator getCalculator() {
		return calc;
	}

	@Override
	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		if(selectionArea != null) {
			g.setColor(Color.WHITE);
			g.drawRect(selectionArea.x, selectionArea.y, (int)((float)selectionArea.height/getHeight()*getWidth()), selectionArea.height);
		}
	}
}
