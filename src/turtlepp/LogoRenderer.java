package turtlepp;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * base class for a renderer
 * 
 * @author Nicolas Winkler
 */
public abstract class LogoRenderer {
	/**
	 * the image to draw to
	 */
	protected BufferedImage bi;

	/**
	 * graphics to provide access to drawing functions
	 */
	protected Graphics graphics;

	/**
	 * set the image to draw to
	 * 
	 * @param bi
	 *            the new image
	 */
	public void setImage(BufferedImage bi) {
		this.bi = bi;
		graphics = bi.getGraphics();
	}

	/**
	 * gets the image on which the stuff is drawn
	 * 
	 * @return the image on which the stuff is drawn
	 */
	public BufferedImage getImage() {
		return bi;
	}

	/**
	 * @return the graphics used to draw
	 */
	public Graphics getGraphics() {
		return graphics;
	}

	/**
	 * two-dimensional vector with double components
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	protected static class Vec2d {
		/**
		 * the x-value
		 */
		public double x;

		/**
		 * the y-value
		 */
		public double y;

		/**
		 * initializes the components to 0
		 */
		public Vec2d() {
			x = 0;
			y = 0;
		}

		/**
		 * initializes the components to the specified values
		 */
		public Vec2d(double x, double y) {
			this.x = x;
			this.y = y;
		}

		/**
		 * adds two vectors together
		 * 
		 * @param b
		 *            the other vector
		 * @return this vector + the other vector
		 */
		public Vec2d plus(Vec2d b) {
			return new Vec2d(x + b.x, y + b.y);
		}
	}

	/**
	 * three-dimensional vector with double components
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	protected static class Vec3d {
		/**
		 * the x-value
		 */
		public double x;

		/**
		 * the y-value
		 */
		public double y;

		/**
		 * the z-value
		 */
		public double z;

		/**
		 * initializes the components to 0
		 */
		public Vec3d() {
			x = 0;
			y = 0;
			z = 0;
		}

		/**
		 * initializes the components to the specified values
		 */
		public Vec3d(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		/**
		 * adds two vectors together
		 * 
		 * @param b
		 *            the other vector
		 * @return this vector + the other vector
		 */
		public Vec3d plus(Vec3d b) {
			return new Vec3d(x + b.x, y + b.y, z + b.z);
		}
	}
}
