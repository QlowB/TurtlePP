package turtlepp.exec;

import java.util.HashMap;
import java.util.Stack;

import turtlepp.InterpreterException;
import turtlepp.LogoRenderer2D;
import turtlepp.datatypes.Value;
import turtlepp.datatypes.Variable;
import turtlepp.exec.Executable.ReturnValue;

public class Subroutine extends Invokeable {
	private Block block;

	protected Subroutine() {
		super("");
	}

	public Subroutine(String subCode, int lineOffset) {
		super("");
		String first = Tokenizer.getLines(subCode)[0];
		String[] firstLine = first.split(" +");
		if (firstLine.length >= 2 && firstLine[0].equalsIgnoreCase("sub")) {
			name = firstLine[1].toLowerCase();
		} else {
			throw new InterpreterException(
					"Check your use of the \"Sub\" statement",
					new CodeLocationInfo(lineOffset));
		}

		arguments = new String[firstLine.length - 2];
		for (int i = 2; i < firstLine.length; i++) {
			arguments[i - 2] = firstLine[i];
		}

		String rest = subCode.substring(first.length());
		block = new Block(rest, null, lineOffset);
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

		oi.pushInvokable(this);
		ReturnValue cb = block.execute(renderer, oi);
		if (cb == ReturnValue.EXIT_REPEAT)
			renderer.printErrLine("Exit Repeat without repeat");
		oi.popInvokable();

		stack = tempStack;
		variableIndices = tempRef;
	}
	
	@Override
	public void optimize(SubroutineOptimizer so) {
		block = (Block) block.getOptimized(so);
	}
}
