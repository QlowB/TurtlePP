package turtlepp.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import turtlepp.InterpreterException;
import turtlepp.LogoRenderer2D;
import turtlepp.datatypes.MathConstant;
import turtlepp.datatypes.Variable;
import turtlepp.exec.Executable.ReturnValue;

/**
 * stores variables and subroutines
 * 
 * @author Nicolas Winkler
 * 
 */
public final class OrderInterpreter {
	/**
	 * the block to be executed
	 */
	private Executable code;

	/**
	 * all global variables are stored here
	 */
	private Stack<Variable> globalVariables;

	/**
	 * hashmap to find variables by name faster
	 */
	private HashMap<String, Integer> globalVarsIndices;

	/**
	 * stack where subroutines are placed when they are invoked
	 */
	private Stack<Invokeable> callStack;

	/**
	 * all global functions
	 */
	private InvokeableList subroutines;

	/**
	 * initializes the Interpreter and prepares the code for running
	 * 
	 * @param str
	 *            the program code
	 */
	public OrderInterpreter(String str) {
		globalVariables = new Stack<Variable>();
		globalVarsIndices = new HashMap<String, Integer>();

		callStack = new Stack<Invokeable>();
		callStack.push(null);

		initSubroutines();
		initConstants();

		// remove commands (text after //)
		str += "\n";
		str = str.replaceAll("//(.|\\s)*?\\n", "\n");
		
		str = str.replaceAll("[\\t\\ ]_\\r?\\n\\s*", "\\ ");
		setCode(str);
	}

	/**
	 * initializes the native subroutines
	 */
	private void initSubroutines() {
		subroutines = new InvokeableList();
		ArrayList<Function> natives = NativeFunction.getNativeFunctions();
		for (int i = 0; i < natives.size(); i++)
			subroutines.add(natives.get(i));
	}

	/**
	 * initializes the mathematical constants
	 */
	private void initConstants() {
		ArrayList<Variable> constants = MathConstant.getConstants();
		for (int i = 0; i < constants.size(); i++) {
			pushVariable(constants.get(i));
		}
	}

	/**
	 * sets the code of this interpreter
	 * 
	 * @param str
	 *            the new code
	 */
	private void setCode(String str) {
		code = new Block(str, subroutines, 0, false);
		code = code.getOptimized(new OrderOptimizer(this));
		ArrayList<Invokeable> subs = subroutines.getAsArrayList();
		for (int i = 0; i < subs.size(); i++) {
			subs.get(i).optimize(new SubroutineOptimizer(this));
		}
	}

	/**
	 * gets a variable (either global or not)
	 * 
	 * @param name
	 *            the variable's name
	 * @return the variable
	 */
	public Variable getVariable(String name) {
		name = name.toLowerCase();
		Invokeable sub = callStack.peek();
		if (sub != null) {
			Variable var = sub.getVariable(name);
			if (var != null)
				return var;
		}

		Integer index = globalVarsIndices.get(name);
		if (index != null)
			return globalVariables.get(index);

		return null;
	}

	public Variable getVariableFromCallStack(int fromTop) {
		Invokeable sub = callStack.peek();
		if (sub != null)
			return sub.getVariableFromStack(fromTop);
		else
			return null;
	}

	public Variable getVariableFromGlobalStack(int fromTop) {
		return globalVariables.get(globalVariables.size() - fromTop);
	}

	/**
	 * creates an error message if e.g. "exit repeat" is found inside of a while
	 * statement instead of "exit while"
	 * 
	 * @param cb
	 *            the return value of the exit command
	 * @return the error message
	 */
	public String createShouldBeInStatementMsg(ReturnValue cb) {
		if (cb == ReturnValue.EXIT_FUNCTION)
			return "Exit Function should be in a function statement.";
		if (cb == ReturnValue.EXIT_SUB)
			return "Exit Sub should be in a sub statement.";
		if (cb == ReturnValue.EXIT_REPEAT)
			return "Exit Repeat should be in a repeat statement.";
		if (cb == ReturnValue.EXIT_WHILE)
			return "Exit While should be in a while statement.";
		return null;
	}

	/**
	 * runs the previously set code
	 * 
	 * @param renderer
	 *            the renderer used to draw stuff
	 */
	public void run(LogoRenderer2D renderer) {
		ReturnValue cb = code.execute(renderer, this);
		if (cb != ReturnValue.NEW_VARIABLE_CREATED && cb != ReturnValue.NOTHING) {
			String str = createShouldBeInStatementMsg(cb);
			if (str != null)
				throw new InterpreterException(str);
		}
	}

	/**
	 * Searches the subroutines for a specific subroutine. As a subroutine do
	 * count:
	 * <ul>
	 * <li>subroutines defined with "sub"-keyword</li>
	 * <li>subroutines defined with "function"-keyword</li>
	 * <li>native mathematical functions</li>
	 * </ul>
	 * 
	 * @param name
	 *            the name of the desired routine
	 * @param nArguments
	 *            the amount of argument the subroutine requires
	 * @return
	 */
	public Invokeable getSubroutine(String name, int nArguments) {
		return subroutines.get(name.toLowerCase(), nArguments);
	}

	/**
	 * before invoking a subroutine it has to be pushed onto the current call
	 * stack
	 * 
	 * @param subroutine
	 *            the subroutine to push
	 */
	public void pushInvokable(Invokeable subroutine) {
		callStack.push(subroutine);
	}

	/**
	 * after exiting the subroutine, remove it from the stack
	 * 
	 * @return the subroutine that was on the top of the call stack
	 */
	public Invokeable popInvokable() {
		return callStack.pop();
	}

	/**
	 * initialize a new variable
	 * 
	 * @param var
	 *            the variable
	 */
	public void pushVariable(Variable var) {
		Invokeable sub = callStack.peek();
		if (sub != null) {
			sub.pushVariable(var);
		} else {
			globalVariables.push(var);
			globalVarsIndices.put(var.getName(), globalVariables.size() - 1);
		}
	}

	/**
	 * remove the latest variable
	 * 
	 * @return the removed variable
	 */
	public Variable popVariable() {
		Invokeable sub = callStack.peek();
		if (sub != null) {
			return sub.popVariable();
		} else {
			Variable v = globalVariables.pop();
			globalVarsIndices.remove(v.getName());
			return v;
		}
	}

	/**
	 * Acts similar to a simple list of invokeables, however has better
	 * searching methods. It uses a hash map to easily find stored content.
	 * 
	 * Implements a fast get function.
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static final class InvokeableList extends HashMap<Long, Invokeable> {
		private static final long serialVersionUID = 7798041147051271416L;
		private ArrayList<Invokeable> content;

		public InvokeableList() {
			content = new ArrayList<Invokeable>();
		}

		/**
		 * Adds a new {@link Invokeable} to the list
		 * 
		 * @param inv
		 *            the new element
		 */
		public void add(Invokeable inv) {
			super.put(combineHash(inv.getName(), inv.getNArguments()), inv);
			content.add(inv);
		}

		/**
		 * searches for a specific element
		 * 
		 * @param name
		 *            the name of the subroutine
		 * @param nArguments
		 *            the amount of arguments the subroutine takes
		 * @return the desired subroutine
		 */
		public Invokeable get(String name, int nArguments) {
			return super.get(combineHash(name, nArguments));
		}

		/**
		 * creates the long value used to create the hash for the hash map
		 */
		private long combineHash(String name, int nArguments) {
			return (long) name.hashCode() + ((long) nArguments << 32);
		}

		/**
		 * @return the content of the list, stored in an {@link ArrayList}
		 */
		public ArrayList<Invokeable> getAsArrayList() {
			return content;
		}
	}

	public boolean hasCallbacks() {
		return getSubroutine("draw", 0) != null
				|| getSubroutine("mouseClicked", 2) != null
				|| getSubroutine("mousePressed", 2) != null
				|| getSubroutine("mouseReleased", 2) != null
				|| getSubroutine("mouseDragged", 2) != null
				|| getSubroutine("mouseMoved", 2) != null;
	}

	public ArrayList<Invokeable> getInvokeables() {
		return subroutines.getAsArrayList();
	}

	public Stack<Variable> getGlobalStack() {
		return globalVariables;
	}
}
