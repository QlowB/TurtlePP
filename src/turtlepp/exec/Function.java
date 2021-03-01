package turtlepp.exec;

import java.util.HashMap;
import java.util.Stack;

import turtlepp.InterpreterException;
import turtlepp.LogoRenderer2D;
import turtlepp.datatypes.Reference;
import turtlepp.datatypes.Value;
import turtlepp.datatypes.Variable;

/**
 * 
 * @author Nicolas Winkler
 *
 */
public class Function extends Invokeable {
	Block block;
	protected Variable temp;

	public Function() {
		super("");
		block = null;
	}

	public Function(String funcCode, int lineOffset) {
		super("");
		String first = Tokenizer.getLines(funcCode)[0];
		String[] firstLine = first.split(" +");
		if (firstLine.length >= 2 && firstLine[0].equalsIgnoreCase("function")) {
			name = firstLine[1].toLowerCase();
		} else {
			throw new InterpreterException(
					"internal error; check your use of the \"Function\" statement");
		}

		arguments = new String[firstLine.length - 2];
		for (int i = 2; i < firstLine.length; i++) {
			arguments[i - 2] = firstLine[i];
		}

		String rest = funcCode.substring(first.length());
		block = new Block(rest, null, lineOffset);
	}

	public Function(String name) {
		super(name);
	}

	/**
	 * is called before any variable is initialized, used e.g by
	 * {@link Function} to push the return variable on the stack.
	 */
	protected void justInvoked() {
		pushVariable(new Reference(name));
	}

	/**
	 * is called before the variables are destroyed.
	 */
	protected void justFinished() {
		Variable nam = getVariable(name);
		if (nam instanceof Reference)
			temp = ((Reference) nam).wrapped;
		else
			temp = nam;
	}

	public Variable getVarValue(LogoRenderer2D renderer, OrderInterpreter oi,
			Value[] args, CodeLocationInfo cli) {
		invoke(renderer, oi, args, cli);
		return temp;
	}

	@Override
	public void invoke(LogoRenderer2D renderer, OrderInterpreter oi,
			Value[] args, CodeLocationInfo cli) {
		if (arguments.length != args.length)
			throw new InterpreterException("subroutine " + name
					+ " called with " + args.length + " arguments; "
					+ arguments.length + " required.");

		Stack<Variable> tempStack = stack; // store the old stack
		HashMap<String, Integer> tempRef = variableIndices;

		variableIndices = new HashMap<String, Integer>();
		stack = new Stack<Variable>();

		for (int i = 0; i < arguments.length; i++) {
			Stack<Variable> stTemp = stack;
			HashMap<String, Integer> hmTemp = variableIndices;
			stack = tempStack;
			variableIndices = tempRef;
			Variable v = args[i].getVarValue(renderer, oi, cli).getCopy(
					arguments[i]);
			stack = stTemp;
			variableIndices = hmTemp;
			pushVariable(v);
		}

		justInvoked();

		oi.pushInvokable(this);
		block.execute(renderer, oi);
		oi.popInvokable();

		justFinished();

		stack = tempStack;
		variableIndices = tempRef;
	}

	@Override
	public void optimize(SubroutineOptimizer so) {
		block = (Block) block.getOptimized(so);
	}
}
