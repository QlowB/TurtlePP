package turtlepp.exec;

import turtlepp.InterpreterException;
import turtlepp.LogoRenderer2D;
import turtlepp.datatypes.Value;
import turtlepp.exec.OrderInterpreter.InvokeableList;
import turtlepp.exec.Tokenizer.Token;

public class RepeatStatement extends Block {
	Value repeatArgument;
	
	public RepeatStatement(String code, InvokeableList subroutines, int lineNumber) {
		super(lineNumber);
		
		String first = Tokenizer.getLines(code)[0];
		Token[] tokens = Tokenizer.tokenize(first);
		if (tokens[0].isTextCommand("repeat")) {
			repeatArgument = tokens[1].createValue();
		}
		else {
			throw new InterpreterException("Check your use of the repeat statement.",
					new CodeLocationInfo(lineOffset));
		}
		
		String rest = code.substring(first.length());
		addToBlock(rest, subroutines);
	}
	
	@Override
	public ReturnValue execute(LogoRenderer2D renderer,
			OrderInterpreter oi) {
		//long repetitions = oi.calculateLongValue(repeatArgument);
		long repetitions = repeatArgument.getVarValue(renderer, oi, null).getLongValue();
		for (long i = 0; i < repetitions; i++) {
			ReturnValue cb = super.execute(renderer, oi);
			if (cb == ReturnValue.EXIT_REPEAT)
				break;
			else if (cb != ReturnValue.NOTHING && cb != ReturnValue.NEW_VARIABLE_CREATED)
				return cb;
		}
		return ReturnValue.NOTHING;
	}
}
