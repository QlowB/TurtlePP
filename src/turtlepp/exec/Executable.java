package turtlepp.exec;

import turtlepp.LogoRenderer2D;

/**
 * Base class for anything that can be run. (command, code block etc.)
 * 
 * @author Nicolas Winkler
 */
public abstract class Executable {

	/**
	 * Every command returns one of these
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public enum ReturnValue {
		NOTHING, EXIT_SUB, NEW_VARIABLE_CREATED, EXIT_FUNCTION, EXIT_WHILE, EXIT_REPEAT, EXIT
	}

	/**
	 * Run the {@link Executable}
	 * 
	 * @param renderer
	 *            the renderer to draw on
	 * @param oi
	 *            stores the variables and subroutines
	 */
	public abstract ReturnValue execute(LogoRenderer2D renderer,
			OrderInterpreter oi);

	/**
	 * tries to create an optimized version of the executable (doing exactly the
	 * same on execution) but with an increase in performance.
	 * 
	 * @param oi
	 *            should contain already prepared executables
	 * @return a faster {@link Executable} or itself
	 */
	public Executable getOptimized(OrderOptimizer oo) {
		return this;
	}
}
