import java.awt.*;
import java.awt.event.*;

/*
 * This class creates & runs the outer layer of the GUI
 * in the main function. Could use some cleaning
 * up, but the GUI isn't the main focus of this.
 * It'll pass this time. If I ever use this project
 * for anything more than just starting at the beauty
 * of the set I'll change it up.
 */
public class Mandelbrot {
	public static void main(String[] args) {
		Frame frame = new Frame("Mandelbrot Calculator");
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		MandelbrotFrame mf = new MandelbrotFrame();
		Container top = new Container();
		Container controls = new Container();
		top.setLayout(new BorderLayout());
		controls.setLayout(new BorderLayout());
		top.add(controls, BorderLayout.SOUTH);

		Choice iterationTypes = new Choice();
		for(MandelbrotCalculator.ColorGenType type : MandelbrotCalculator.ColorGenType.values()) {
			iterationTypes.add(type.toString());
		}
		iterationTypes.select(mf.getCalculator().getColorGenType().toString());
		controls.add(iterationTypes, BorderLayout.CENTER);
		iterationTypes.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mf.getCalculator().setColorGenType(MandelbrotCalculator.ColorGenType.valueOf(iterationTypes.getSelectedItem()));
				mf.repaint();
			}
		});
		Container zoomButtons = new Container();
		zoomButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		Button resetZoom = new Button("Reset Zoom");
		resetZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				mf.getCalculator().resetZoom();
				mf.repaint();
			}
		});
		Button zoomOut = new Button("-");
		zoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				mf.getCalculator().zoomOut();
				mf.repaint();
			}
		});
		Button zoomIn = new Button("+");
		zoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				mf.getCalculator().zoomIn();
				mf.repaint();
			}
		});
		zoomButtons.add(resetZoom);
		zoomButtons.add(zoomOut);
		zoomButtons.add(zoomIn);
		controls.add(zoomButtons, BorderLayout.WEST);
		Label iterations = new Label("Iterations: " + MandelbrotCalculator.DEFAULT_MAX_ITERATIONS);
		Scrollbar scroll = new Scrollbar(Scrollbar.HORIZONTAL, MandelbrotCalculator.DEFAULT_MAX_ITERATIONS, 5, 0, 200);
		scroll.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent ae) {
				if(ae.getValueIsAdjusting()) return;
				int num_iterations = scroll.getValue();
				int iterations_thresh = 50;
				if(num_iterations > iterations_thresh) {
					num_iterations = iterations_thresh + (num_iterations-iterations_thresh)*(num_iterations-iterations_thresh);
				}
				iterations.setText("Iterations: " + num_iterations);
				mf.getCalculator().setIterations(num_iterations);
				mf.repaint();
			}
		});
		top.add(scroll, BorderLayout.CENTER);
		top.add(iterations, BorderLayout.WEST);
		frame.setLayout(new BorderLayout());
		frame.add(top, BorderLayout.NORTH);
		frame.add(mf, BorderLayout.CENTER);
		frame.setVisible(true);
	}
}
