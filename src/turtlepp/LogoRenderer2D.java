package turtlepp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Stack;
import java.awt.Graphics2D;

/**
 * a renderer for two dimensional obejcts
 * 
 * @author Nicolas Winkler
 */
public class LogoRenderer2D extends LogoRenderer {

	/**
	 * if penDown or penUp
	 */
	private boolean drawing;

	/**
	 * the console output
	 */
	private ConsolePanel output;

	/**
	 * the position of the turtle
	 */
	Vec2d pos;

	/**
	 * the rotation of the turtle
	 */
	Matrix2x2d rotation;

	/**
	 * the transformation matrix to rotate, translate, scale and shear the whole
	 * coordinate system
	 */
	Matrix3x3d transformation;

	/**
	 * the pen color
	 */
	private Color currentColor;

	/**
	 * the stack containing the matrices when pushMatrix and popMatrix is used
	 */
	private Stack<Matrix3x3d> matrices;

	/**
	 * the stack containing the positions when pushPosition and popPosition is
	 * used
	 */
	private Stack<Vec2d> positions;

	/**
	 * showTurtle or hideTurtle
	 */
	private boolean showTurtle;

	/**
	 * initializes the renderer
	 */
	public LogoRenderer2D(LogoDrawArea lda) {
		transformation = new Matrix3x3d();
		rotation = new Matrix2x2d();
		pos = new Vec2d(0, 0);

		drawing = true;

		matrices = new Stack<Matrix3x3d>();
		positions = new Stack<Vec2d>();

		showTurtle = true;

		output = null;

		currentColor = Color.white;
	}

	public ConsolePanel getOutputPanel() {
		return output;
	}

	public void setOutputPanel(ConsolePanel output) {
		this.output = output;
	}

	/**
	 * @return the current pen color
	 */
	public Color getCurrentColor() {
		return currentColor;
	}

	/**
	 * lets the turtle walk forward by a specific amount of pixels
	 * 
	 * @param d
	 *            the amount of pixels to walk
	 */
	public void forward(double d) {
		Vec2d walk = new Vec2d(d, 0);
		walk = rotation.mult(walk);
		Vec2d newPos = pos.plus(walk);

		if (drawing) {
			Vec3d posTransformed = new Vec3d(pos.x, pos.y, 1f);
			posTransformed = transformation.mult(posTransformed);
			Vec3d newPosTransformed = new Vec3d(newPos.x, newPos.y, 1f);
			newPosTransformed = transformation.mult(newPosTransformed);
			graphics.drawLine((int) posTransformed.x, (int) posTransformed.y,
					(int) newPosTransformed.x, (int) newPosTransformed.y);
		}

		pos = newPos;
	}

	/**
	 * draws a point
	 * 
	 * @param d
	 *            the x-coordinate of the point
	 * @param e
	 *            the y-coordinate of the point
	 */
	public void point(double d, double e) {
		Vec3d pt = new Vec3d(d, e, 1f);
		pt = transformation.mult(pt);
		graphics.drawLine((int) pt.x, (int) pt.y, (int) pt.x, (int) pt.y);
	}

	public void ellipse(double x, double y, double w, double h) {
		Vec3d pt = new Vec3d(x, y, 1f);
		pt = transformation.mult(pt);
		graphics.fillOval((int) x, (int) y, (int) w, (int) h);
	}

	/**
	 * draws a line
	 * 
	 * @param vx1
	 *            the x-coordinate of the first point
	 * @param vy1
	 *            the y-coordinate of the first point
	 * @param vx2
	 *            the x-coordinate of the second point
	 * @param vy2
	 *            the y-coordinate of the second point
	 */
	public void line(double vx1, double vy1, double vx2, double vy2) {
		Vec3d pt = new Vec3d(vx1, vy1, 1f);
		Vec3d pt2 = new Vec3d(vx2, vy2, 1f);
		pt = transformation.mult(pt);
		pt2 = transformation.mult(pt2);
		graphics.drawLine((int) pt.x, (int) pt.y, (int) pt2.x, (int) pt2.y);
	}

	/**
	 * draws a triangle
	 * 
	 * @param vx1
	 *            the x-coordinate of the first point
	 * @param vy1
	 *            the y-coordinate of the first point
	 * @param vx2
	 *            the x-coordinate of the second point
	 * @param vy2
	 *            the y-coordinate of the second point
	 * @param vx3
	 *            the x-coordinate of the third point
	 * @param vy3
	 *            the y-coordinate of the third point
	 */
	public void triangle(double vx1, double vy1, double vx2, double vy2,
			double vx3, double vy3) {
		Vec3d pt = new Vec3d(vx1, vy1, 1f);
		Vec3d pt2 = new Vec3d(vx2, vy2, 1f);
		Vec3d pt3 = new Vec3d(vx3, vy3, 1f);
		pt = transformation.mult(pt);
		pt2 = transformation.mult(pt2);
		pt3 = transformation.mult(pt3);
		int[] xpos = { (int) pt.x, (int) pt2.x, (int) pt3.x };
		int[] ypos = { (int) pt.y, (int) pt2.y, (int) pt3.y };
		graphics.fillPolygon(xpos, ypos, 3);
	}

	/**
	 * draws a polygon
	 * 
	 * @param xPositions
	 *            the x-coordinates of the positions
	 * @param yPositions
	 *            the y-coordinates of the positions
	 */
	public void polygon(double[] xPositions, double[] yPositions) {
		int[] xPos = new int[xPositions.length];
		int[] yPos = new int[yPositions.length];
		for (int i = 0; i < xPos.length; i++) {
			Vec3d pt = new Vec3d(xPositions[i], yPositions[i], 1f);
			pt = transformation.mult(pt);
			xPos[i] = (int) pt.x;
			yPos[i] = (int) pt.y;
		}
		graphics.fillPolygon(xPos, yPos, xPos.length);
	}

	/**
	 * move the turtle backwards
	 * 
	 * @param d
	 *            the amount of pixels to move backwards
	 */
	public void backward(double d) {
		forward(-d);
	}

	/**
	 * turn to the right
	 * 
	 * @param val
	 *            the angle in degrees
	 */
	public void right(double val) {
		Matrix2x2d rotationMult = new Matrix2x2d(val * Math.PI / 180.0);
		rotation.mult(rotationMult);
	}

	/**
	 * turn to the left
	 * 
	 * @param val
	 *            the angle in degrees
	 */
	public void left(double val) {
		right(-val);
	}

	/**
	 * reset the turtle's rotation
	 */
	public void resetRotation() {
		rotation = new Matrix2x2d();
	}

	/**
	 * rotate the whole coordinate system by a specific angle
	 * 
	 * @param d
	 *            the angle
	 */
	public void rotate(double d) {
		Matrix3x3d rotationMatrix = Matrix3x3d.createRotation(d * Math.PI
				/ 180.0);
		transformation.mult(rotationMatrix);
	}

	/**
	 * translate the whole coordinate system
	 * 
	 * @param d
	 *            the amount in x-direction
	 * @param e
	 *            the amount in y-direction
	 */
	public void translate(double d, double e) {
		Matrix3x3d translateMatrix = Matrix3x3d.createTranslation(d, e);
		transformation.mult(translateMatrix);
	}

	public void scale(double d, double e) {
		Matrix3x3d scaleMatrix = Matrix3x3d.createScalation(d, e);
		transformation.mult(scaleMatrix);
	}

	public void skewX(float x) {
		Matrix3x3d shearMatrix = new Matrix3x3d();
		shearMatrix.b = x;
		transformation.mult(shearMatrix);
	}

	public void skewY(float y) {
		Matrix3x3d shearMatrix = new Matrix3x3d();
		shearMatrix.d = y;
		transformation.mult(shearMatrix);
	}

	public void skew(double d, double e) {
		Matrix3x3d shearMatrix = new Matrix3x3d();
		shearMatrix.b = d;
		shearMatrix.d = e;
		transformation.mult(shearMatrix);
	}

	public void pushMatrix() {
		matrices.push(transformation.getCopy());
	}

	public void popMatrix() {
		if (matrices.size() > 1) // standard-matrix must be on the bottom of the
									// stack
			transformation = matrices.pop();
	}

	public void resetMatrix() {
		if (matrices.size() >= 1)
			transformation = matrices.get(0).getCopy();
		else
			transformation = new Matrix3x3d();
	}

	public void pushPosition() {
		positions.push(new Vec2d(pos.x, pos.y));
	}

	public void popPosition() {
		pos = positions.pop();
	}

	public void penUp() {
		drawing = false;
	}

	public void penDown() {
		drawing = true;
	}

	public void penColor(float r, float g, float b) {
		if (r > 255)
			r = 255;
		if (g > 255)
			g = 255;
		if (b > 255)
			b = 255;
		if (r < 0)
			r = 0;
		if (g < 0)
			g = 0;
		if (b < 0)
			b = 0;
		r *= 1f / 255f;
		g *= 1f / 255f;
		b *= 1f / 255f;
		currentColor = new Color(r, g, b);
		graphics.setColor(currentColor);
	}

	public void setPen(boolean set) {
		drawing = set;
	}

	public void setPosition(double d, double e) {
		pos = new Vec2d(d, e);
	}

	public void setAntialiasing(boolean b) {
		if (b)
			((Graphics2D) graphics).setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		else
			((Graphics2D) graphics).setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public double getX() {
		return pos.x;
	}

	public double getY() {
		return pos.y;
	}

	public boolean isShowTurtle() {
		return showTurtle;
	}

	public void setShowTurtle(boolean showTurtle) {
		this.showTurtle = showTurtle;
	}

	public void println(String str) {
		output.printLine(str);
		if (!output.isVisible()) {
			output.setVisible(true);
		}
	}
	


	public void print(String str) {
		output.print(str);
		if (!output.isVisible()) {
			output.setVisible(true);
		}
	}

	public void printErrLine(String str) {
		output.printErrLine(str);
		if (!output.isVisible()) {
			output.setVisible(true);
		}
	}

	public void clearOutput() {
		output.clear();
	}

	/**
	 * blocking call
	 * 
	 * @return
	 */
	public String popInputString() {
		return output.popInputString();
	}

	public ConsolePanel getConsole() {
		return output;
	}

	public void clear(java.awt.Color backgroundColor) {
		Graphics g = getGraphics();
		java.awt.Color temp = g.getColor();
		g.setColor(backgroundColor);
		g.fillRect(0, 0, 20000, 20000);
		g.setColor(temp);
	}

	public void reset() {
		resetRotation();
		resetMatrix();
		clearOutput();
		clear(Color.black);
		setPosition(bi.getWidth() / 2, bi.getHeight() / 2);
		setPen(true);
		setShowTurtle(true);
		penColor(255, 255, 255);
	}

	protected static class Matrix2x2d {
		double a;
		double b;
		double c;
		double d;

		Matrix2x2d() {
			a = 1f;
			b = 0f;
			c = 0f;
			d = 1f;
		}

		Matrix2x2d(Matrix2x2d mat) {
			a = mat.a;
			b = mat.b;
			c = mat.c;
			d = mat.d;
		}

		Matrix2x2d(double angle) {
			double sine = Math.sin(angle);
			double cosine = Math.cos(angle);

			a = cosine;
			b = -sine;
			c = sine;
			d = cosine;
		}

		void mult(Matrix2x2d mat) {
			double newA = a * mat.a + b * mat.c;
			double newB = a * mat.b + b * mat.d;
			double newC = c * mat.a + d * mat.c;
			double newD = c * mat.b + d * mat.d;

			a = newA;
			b = newB;
			c = newC;
			d = newD;
		}

		Vec2d mult(Vec2d vec) {
			return new Vec2d(vec.x * a + vec.y * b, vec.x * c + vec.y * d);
		}

		@Override
		public String toString() {
			return "Matrix2x2d [a=" + a + ", b=" + b + "]\n[c=" + c + ", d="
					+ d + "]";
		}

		/**
		 * multiplies the given vector but ignores its z-coordinate (since this
		 * is a two-dimensional rotation matrix)
		 * 
		 * @param vec
		 * @return
		 */
		public Vec2d mult(Vec3d vec) {
			return new Vec2d(vec.x * a + vec.y * b, vec.x * c + vec.y * d);
		}
	}

	protected static class Matrix3x3d {

		double a;
		double b;
		double c;
		double d;
		double e;
		double f;
		double g;
		double h;
		double i;

		Matrix3x3d() {

			a = 1d;
			b = 0d;
			c = 0d;
			d = 0d;
			e = 1d;
			f = 0d;
			g = 0d;
			h = 0d;
			i = 1d;
		}

		static Matrix3x3d createRotation(double angle) {
			Matrix3x3d rot = new Matrix3x3d();
			double sine = Math.sin(angle);
			double cosine = Math.cos(angle);

			rot.a = cosine;
			rot.b = -sine;
			rot.d = sine;
			rot.e = cosine;

			return rot;
		}

		static Matrix3x3d createTranslation(double x, double y) {
			Matrix3x3d trans = new Matrix3x3d();

			trans.c = x;
			trans.f = y;

			return trans;
		}

		static Matrix3x3d createScalation(double x, double y) {
			Matrix3x3d scale = new Matrix3x3d();

			scale.a = x;
			scale.e = y;

			return scale;
		}

		Matrix3x3d getCopy() {
			Matrix3x3d mat = new Matrix3x3d();

			mat.a = a;
			mat.b = b;
			mat.c = c;
			mat.d = d;
			mat.e = e;
			mat.f = f;
			mat.g = g;
			mat.h = h;
			mat.i = i;

			return mat;
		}

		void mult(Matrix3x3d mat) {

			double newA = a * mat.a + b * mat.d + c * mat.g;
			double newB = a * mat.b + b * mat.e + c * mat.h;
			double newC = a * mat.c + b * mat.f + c * mat.i;

			double newD = d * mat.a + e * mat.d + f * mat.g;
			double newE = d * mat.b + e * mat.e + f * mat.h;
			double newF = d * mat.c + e * mat.f + f * mat.i;

			double newG = g * mat.a + h * mat.d + i * mat.g;
			double newH = g * mat.b + h * mat.e + i * mat.h;
			double newI = g * mat.c + h * mat.f + i * mat.i;

			a = newA;
			b = newB;
			c = newC;
			d = newD;
			e = newE;
			f = newF;
			g = newG;
			h = newH;
			i = newI;
		}

		Vec3d mult(Vec3d vec) {
			return new Vec3d(vec.x * a + vec.y * b + vec.z * c, vec.x * d
					+ vec.y * e + vec.z * f, vec.x * g + vec.y * h + vec.z * i);
		}
	}
}
