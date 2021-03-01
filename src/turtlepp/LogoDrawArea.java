package turtlepp;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import turtlepp.LogoRenderer.Vec2d;
import turtlepp.LogoRenderer.Vec3d;
import turtlepp.datatypes.Value;
import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.Invokeable;
import turtlepp.exec.OrderInterpreter;

/**
 * The panel on which the whole drawing is done
 * 
 * @author Nicolas Winkler
 */
public class LogoDrawArea extends JPanel implements MouseListener,
		MouseMotionListener, ComponentListener {
	private static final long serialVersionUID = 4574057871549443746L;

	private class RunMainPart implements Runnable {
		public void run() {
			if (interpreter != null) {
				interpreter.run(logoRenderer);
			}
		}
	}

	private class RunDrawFunction implements Runnable {
		@Override
		public void run() {
			if (interpreter != null) {
				Invokeable inv = interpreter.getSubroutine("draw", 0);
				if (inv != null)
					inv.invoke(logoRenderer, interpreter, new Value[0], null);
			}
		}
	}

	private class RunDrawEveryFrame extends Thread {
		private boolean pleaseStop = false;

		@Override
		public void run() {
			while (!pleaseStop) {
				try {
					Thread.sleep(1000 / 60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (interpreter != null
						&& interpreter.getSubroutine("draw", 0) != null) {
					runDrawCode(new RunDrawFunction());
					repaint();
				}
			}
		}

		@SuppressWarnings("deprecation")
		public void pleaseStop() {
			pleaseStop = true;
			stop();
			if (onFinish != null)
				onFinish.run();
			onFinish = null;
			isRunning = false;
			interpreter = null;
		}
	}

	/**
	 * renderer that draws stuff on this panel
	 */
	private LogoRenderer2D logoRenderer;

	/**
	 * interpreter that supports the running of the program
	 */
	private OrderInterpreter interpreter;

	/**
	 * is executed after the execution
	 */
	private Runnable onFinish;

	/**
	 * runs the draw function repeatedly
	 */
	private RunDrawEveryFrame runDraw;

	/**
	 * if there is an execution running
	 */
	private volatile boolean isRunning;

	/**
	 * the image which the whole stuff is drawn onto
	 */
	private BufferedImage bufferedImage;

	/**
	 * error output
	 */
	private ConsolePanel errorOutput;

	/**
	 * the code source, where to highlight errors
	 */
	private CodeTextPane codeSource;

	private Lock runLock;

	private boolean errorHighlighting;

	/**
	 * default constructor
	 */
	public LogoDrawArea(CodeTextPane codeSource) {
		logoRenderer = new LogoRenderer2D(this);
		this.codeSource = codeSource;
		isRunning = false;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		errorOutput = logoRenderer.getConsole();
		runLock = new ReentrantLock();
		this.addComponentListener(this);
	}

	/**
	 * initializes the {@link LogoDrawArea} so that it is ready to be drawn onto
	 */
	void initialize() {
		logoRenderer.resetMatrix();
		logoRenderer.translate(getWidth() / 2, getHeight() / 2);
		logoRenderer.pushMatrix();
		// logoRenderer.setPosition(getWidth() / 2, getHeight() / 2);
		if (getWidth() <= 0 || getHeight() <= 0) {
			bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		} else {
			bufferedImage = new BufferedImage(getWidth(), getHeight(),
					BufferedImage.TYPE_INT_RGB);
		}
		repaint();
		setErrorHighlighting(true);
		/*
		 * this.run("", new Runnable() {
		 * 
		 * @Override public void run() { } });
		 */
	}

	public boolean isErrorHighlighting() {
		return errorHighlighting;
	}

	public void setErrorHighlighting(boolean errorHighlighting) {
		this.errorHighlighting = errorHighlighting;
	}

	public CodeTextPane getCodeSource() {
		return codeSource;
	}

	public void setCodeSource(CodeTextPane codeSource) {
		this.codeSource = codeSource;
	}

	/**
	 * draws the finished image to the panel
	 */
	@Override
	public void paintComponent(Graphics g) {
		if (tryLock(3000)) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.drawImage(bufferedImage, 0, 0, this);

			if (logoRenderer.isShowTurtle()) {

				Vec2d a = new Vec2d(20, 0);
				Vec2d b = new Vec2d(-4, -8);
				Vec2d c = new Vec2d(-4, 8);

				a = logoRenderer.rotation.mult(a);
				b = logoRenderer.rotation.mult(b);
				c = logoRenderer.rotation.mult(c);

				Vec3d pos1 = new Vec3d(logoRenderer.pos.x + a.x,
						logoRenderer.pos.y + a.y, 1f);
				Vec3d pos2 = new Vec3d(logoRenderer.pos.x + b.x,
						logoRenderer.pos.y + b.y, 1f);
				Vec3d pos3 = new Vec3d(logoRenderer.pos.x + c.x,
						logoRenderer.pos.y + c.y, 1f);

				pos1 = logoRenderer.transformation.mult(pos1);
				pos2 = logoRenderer.transformation.mult(pos2);
				pos3 = logoRenderer.transformation.mult(pos3);

				int[] xpoints = new int[] { (int) pos1.x, (int) pos2.x,
						(int) pos3.x };
				int[] ypoints = new int[] { (int) pos1.y, (int) pos2.y,
						(int) pos3.y };
				g2d.setColor(logoRenderer.getCurrentColor());
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.drawPolygon(xpoints, ypoints, 3);
			}
			checkForFinish();
			unlock();
		}
	}

	public void checkForFinish() {
		if (interpreter != null && !interpreter.hasCallbacks()) {
			isRunning = false;
			interpreter = null;
			if (onFinish != null)
				onFinish.run();
			onFinish = null;
		} else if (interpreter == null) {
			if (onFinish != null)
				onFinish.run();
		}
	}

	private void runDrawCode(Runnable runOnRepaint) {
		try {
			logoRenderer.setImage(bufferedImage);
			if (runOnRepaint != null)
				runOnRepaint.run();
		} catch (InterpreterException ex) {
			CodeLocationInfo cli = ex.getCodeLocationInfo();
			if (cli != null && errorHighlighting) {
				// highlight the critical code part for 10 seconds
				codeSource.highlightLine(cli.getLineNumber(), 10000);
			}
			ex.printStackTrace();
			stopExecution();
			errorOutput.printErrLine(ex.getMessage());
			// showErrorMessage(ex.getMessage());
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			System.err.println(ex.getMessage());
			stopExecution();
			// JOptionPane.showMessageDialog(this, ex.getMessage(),
			// "Severe error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setConsole(ConsolePanel errorOutput) {
		this.errorOutput = errorOutput;
	}

	/**
	 * executes the given code and displays the result
	 * 
	 * @param code
	 *            the code to execute
	 * @param onFinish
	 *            invoked when the execution has stopped
	 */
	void run(final String code, final Runnable onFinish) {
		if (onFinish == null)
			throw new RuntimeException(
					"Bisch tumm?! Doefsch noed ufrueefe mit onFinish == null!");
		this.onFinish = onFinish;
		if (runDraw != null && runDraw.isAlive())
			runDraw.pleaseStop();

		final LogoDrawArea lda = this;

		new Thread() {
			public void run() {
				try {
					interpreter = new OrderInterpreter(code);
					isRunning = true;

					if (tryLock(1000)) {
						// ===============
						// ===== RUN =====
						// ===============

						runDrawCode(new RunMainPart());

						// ===============
						// === END RUN ===
						// ===============

						lda.repaint();
						unlock();
					}

					if (interpreter != null && interpreter.hasCallbacks()) {
						runDraw = new RunDrawEveryFrame();
						runDraw.start();
					}
				} catch (InterpreterException ex) {
					ex.printStackTrace();
					CodeLocationInfo cli = ex.getCodeLocationInfo();
					if (cli != null && errorHighlighting) {
						// highlight the critical code part for 10 seconds
						codeSource.highlightLine(cli.getLineNumber(), 10000);
					}
					stopExecution();
					errorOutput.printErrLine(ex.getMessage());
					// showErrorMessage(ex.getMessage());
				} catch (RuntimeException ex) {
					ex.printStackTrace();
					stopExecution();
				}
				checkForFinish();
			}
		}.start();
	}

	public synchronized boolean isRunning() {
		if (runDraw != null)
			return isRunning || runDraw.isAlive();
		else
			return isRunning;
	}

	public synchronized void stopExecution() {
		if (runDraw != null)
			runDraw.pleaseStop();
		runDraw = null;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (interpreter != null && isRunning()) {
			Value[] args = new Value[] {
					new Value.IntegerValue(e.getX()),
					new Value.IntegerValue(e.getY()) };
			invokeCallbackFunction("mouseClicked", args);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (interpreter != null && isRunning()) {
			Value[] args = new Value[] {
					new Value.IntegerValue(e.getX()),
					new Value.IntegerValue(e.getY()) };
			invokeCallbackFunction("mousePressed", args);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (interpreter != null && isRunning()) {
			Value[] args = new Value[] {
					new Value.IntegerValue(e.getX()),
					new Value.IntegerValue(e.getY()) };
			invokeCallbackFunction("mouseReleased", args);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (interpreter != null && isRunning()) {
			Value[] args = new Value[] {
					new Value.IntegerValue(e.getX()),
					new Value.IntegerValue(e.getY()) };
			invokeCallbackFunction("mouseDragged", args);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (interpreter != null && isRunning()) {
			Value[] args = new Value[] {
					new Value.IntegerValue(e.getX()),
					new Value.IntegerValue(e.getY()) };
			invokeCallbackFunction("mouseMoved", args);
		}
	}

	/**
	 * invokes a subroutine on the {@link OrderInterpreter}
	 * 
	 * @param name
	 *            the name of the subroutine
	 * @param args
	 *            the arguments
	 */
	private void invokeCallbackFunction(final String name, final Value[] args) {
		if (tryLock(300)) {
			new Thread() {
				public void run() {
					if (interpreter != null && isRunning()) {
						if (tryLock(100)) {
							Invokeable inv = interpreter.getSubroutine(name,
									args.length);
							if (inv != null) {
								try {
									inv.invoke(logoRenderer, interpreter, args,
											null);
									repaint();
								} catch (InterpreterException ex) {
									ex.printStackTrace();
									CodeLocationInfo cli = ex
											.getCodeLocationInfo();
									if (cli != null && errorHighlighting) {
										// highlight the critical code part
										// for
										// 10
										// seconds
										codeSource.highlightLine(
												cli.getLineNumber(), 10000);
									}
									stopExecution();
									if (interpreter != null
											&& !interpreter.hasCallbacks()) {
										if (onFinish != null)
											onFinish.run();
										isRunning = false;
										onFinish = null;
									}
									interpreter = null;
									errorOutput.printErrLine(ex.getMessage());
									// showErrorMessage(ex.getMessage());
								}
							}
							unlock();
						}
					}
				}
			}.start();
			unlock();
		}
	}

	@SuppressWarnings("unused")
	private void showErrorMessage(String text) {
		JOptionPane.showMessageDialog(this, text, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	public boolean tryLock(int millisTimeout) {
		boolean rn = false;
		try {
			rn = runLock.tryLock(millisTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return rn;
	}

	public void unlock() {
		runLock.unlock();
	}

	public LogoRenderer2D getRenderer() {
		return logoRenderer;
	}

	public OrderInterpreter getOrderInterpreter() {
		return interpreter;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		initialize();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}
}
