import java.awt.*;
import java.util.function.*;

/*
 * This class does all the actual calculation
 * and determines how to convert the calcualated
 * value into an image, which is then displayed
 * by the MandelbrotFrame class.
 */
public class MandelbrotCalculator {
	private Consumer<int[][]> callback; //called as soon as a recalculation completes
	private int width, height;
	private double startX, startY, endX, endY, stepX, stepY;
	private int[][] array;
	public static final int DEFAULT_MAX_ITERATIONS = 25;
	private int maxIterations = DEFAULT_MAX_ITERATIONS;
	public enum ColorGenType {
		SQUARE,
		CUBE,
		BLACK_AND_WHITE,
		BLACK_AND_WHITE_INVERSE,
		HSL,
		HSL_REVERSE,
		HSL_BRIGHT,
		ROUGH,
		SQRT,
		WHITE
	}
	private ColorGenType colorGen = ColorGenType.HSL;
	public MandelbrotCalculator(int width, int height, Consumer<int[][]> callback) {
		this.callback = callback;
		setDefaultZoom();
		resize(width, height);
	}
	public void setColorGenType(ColorGenType colorGen) {
		this.colorGen = colorGen;
		recalc();
	}
	public ColorGenType getColorGenType() {
		return colorGen;
	}
	public void selectRectangle(Rectangle rect) {
		startX = ((double)rect.x/width)*(endX - startX) + startX;
		endX = ((double)(rect.x+rect.width)/width)*(endX - startX) + startX;
		startY = ((double)rect.y/height)*(endY - startY) + startY;
		endY = ((double)(rect.y+rect.height)/height)*(endY - startY) + startY;
		recalcStep();
		recalc();
	}
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		if(array == null || array.length != height || array[0].length != width) {
			array = new int[height][width];
		}
		if(array.length == 0) return;
		recalcStep();
		recalc();
	}
	private void recalcStep() {
		stepY = (endY - startY) / array.length;
		endX = ((double)width/height) * (endY - startY) + startX; //normalize
		stepX = (endX - startX) / array[0].length;
	}
	public void setIterations(int maxIterations) {
		this.maxIterations = maxIterations;
		recalc();
	}
	private void setDefaultZoom() {
		startX = -3.5;
		startY = -2;
		endX = 3;
		endY = 2;
	}
	public void resetZoom() {
		setDefaultZoom();
		recalcStep();
		recalc();
	}
	public void zoomIn() {
		double dy = endY - startY;
		double dx = endX - startX;
		double cy = dy/2 + startY;
		double cx = dx/2 + startX;
		startX = cx - dx/4;
		startY = cy - dy/4;
		endY = cy + dy/4;
		endX = cx + dx/4;
		recalcStep();
		recalc();
	}
	public void zoomOut() {
		double dy = endY - startY;
		double dx = endX - startX;
		startX -= dx/4;
		startY -= dy/4;
		endY += dy/4;
		endX += dy/4;
		recalcStep();
		recalc();
	}
	public void recalc() {
		double y = startY;
		for(int y_p = 0; y_p < array.length; y_p++) {
			double x = startX - stepX;
			outer:
			for(int x_p = 0; x_p < array[0].length; x_p++) {
				x += stepX;
				double c_r = x;
				double c_im = y;
				//System.out.println(c_r + " " + c_im);
				double z_r = 0;
				double z_im = 0;
				int iterations;
				for(iterations = 0; iterations < maxIterations; iterations++) {
					double z_r_tmp = z_r * z_r - z_im * z_im + c_r;
					double z_im_tmp = 2 * z_r * z_im + c_im;
					if(z_r_tmp == 0 && z_im_tmp == 0) {
						break;
					}
					if(z_r_tmp * z_im_tmp >= 4) {
						//array[y][x] = 0;
						switch(colorGen) {
						case SQUARE:
							array[y_p][x_p] = iterations * iterations;
							break;
						case CUBE:
							array[y_p][x_p] = iterations * iterations * iterations;
							break;
						case BLACK_AND_WHITE:
							array[y_p][x_p] = Color.HSBtoRGB(0f, 0f, (float)iterations/maxIterations);
							break;
						case BLACK_AND_WHITE_INVERSE:
							array[y_p][x_p] = Color.HSBtoRGB(0f, 0f, 1-(float)iterations/maxIterations);
							break;
						case HSL:
							array[y_p][x_p] = Color.HSBtoRGB((float)iterations/maxIterations, 1f, 0.5f);
							break;
						case HSL_REVERSE:
							array[y_p][x_p] = Color.HSBtoRGB(1-(float)iterations/maxIterations, 1f, 0.5f);
							break;
						case HSL_BRIGHT:
							array[y_p][x_p] = Color.HSBtoRGB((float)iterations/maxIterations, 1f, 1f);
							break;
						case ROUGH:
							array[y_p][x_p] = (int)(16777216.0f*iterations/maxIterations); //16777216 is 2^24
							break;
						case SQRT:
							array[y_p][x_p] = Color.HSBtoRGB((float)Math.sqrt((float)iterations/maxIterations), 1f, 0.5f);
							break;
						case WHITE:
							array[y_p][x_p] = 33554431; //2^25-1
							break;
						}
						continue outer;
					}
					z_r = z_r_tmp;
					z_im = z_im_tmp;
				}
				array[y_p][x_p] = 0;
			}
			y += stepY;
		}
		callback.accept(array);
	}
}
