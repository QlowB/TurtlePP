package turtlepp.exec;

import java.util.HashMap;
import java.util.Stack;

import turtlepp.LogoRenderer2D;
import turtlepp.datatypes.Value;
import turtlepp.datatypes.Variable;

/**
 * Base class for subroutines
 * 
 * @author nicolas.winkler
 * 
 */
public abstract class Invokeable {
	/**
	 * name of the invokeable
	 */
	protected String name;

	/**
	 * names of the arguments
	 */
	protected String[] arguments;

	/**
	 * variable stack
	 */
	protected Stack<Variable> stack;

	/**
	 * Contains keys and indices of variable variableIndices.get("blah").
	 * Returns the index of the variable blah in the stack (if this variable
	 * exists)
	 */
	protected HashMap<String, Integer> variableIndices;

	protected Invokeable(String name) {
		this.name = name.toLowerCase();
	}

	/**
	 * Searches for a specified variable
	 * 
	 * @param name
	 *            the name of the variable
	 * @return the variable, if it exists, {@code null} otherwise
	 */
	public Variable getVariable(String name) {
		Integer index = variableIndices.get(name);
		if (index != null)
			return stack.get(index);
		return null;
	}

	public Variable getVariableFromStack(int fromTop) {
		return stack.get(stack.size() - fromTop);
	}

	/**
	 * finds a variable and returns its index from the top of the stack
	 * 
	 * @param name
	 * @return
	 */
	public int getStackIndex(String name) {
		Integer index = variableIndices.get(name);
		return stack.size() - index - 1;
	}

	/**
	 * Pushes a variable on the stack
	 * 
	 * @param var
	 *            the variable
	 */
	public void pushVariable(Variable var) {
		stack.push(var);
		variableIndices.put(var.getName(), stack.size() - 1);
	}

	/**
	 * Removes the last pushed variable
	 * 
	 * @return
	 */
	public Variable popVariable() {
		Variable v = stack.pop();
		variableIndices.remove(v.getName());
		return v;
	}

	/**
	 * invoke the routine
	 * 
	 * @param renderer
	 *            the renderer to draw stuff
	 * @param oi
	 *            where the global variables are stored
	 * @param args
	 *            the arguments for the subroutine
	 * @param cli
	 *            information for exceptions
	 * 
	 */
	public abstract void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
			Value[] args, CodeLocationInfo cli);

	/**
	 * gets the number of arguments requested for this function
	 * 
	 * @return the number of arguments
	 */
	public int getNArguments() {
		return arguments.length;
	}

	/**
	 * gets the name of the subroutine
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public abstract void optimize(SubroutineOptimizer subroutineOptimizer);
}
