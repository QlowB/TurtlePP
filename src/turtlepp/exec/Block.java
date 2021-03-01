package turtlepp.exec;

import java.util.ArrayList;

import turtlepp.InterpreterException;
import turtlepp.LogoRenderer2D;
import turtlepp.exec.OrderInterpreter.InvokeableList;
import turtlepp.exec.Tokenizer.Token;

/**
 * Consists of several instructions that can be run
 * 
 * @author Nicolas Winkler
 * 
 */
public class Block extends Executable {

	/**
	 * the list of instructions
	 */
	Executable[] executables;

	/**
	 * used for creating error messages; the position of this block in the
	 * source code
	 */
	protected int lineOffset;

	/**
	 * <code>true</code> if the global variable stack should be cleared after
	 * the block has finished running.
	 */
	protected boolean removeGlobals;

	protected Block(int lineOffset) {
		this.lineOffset = lineOffset;
		executables = new Executable[0];
		removeGlobals = true;
	}

	public Block(String code, InvokeableList subroutines, int lineOffset) {
		this(lineOffset);
		addToBlock(code, subroutines);
	}

	public Block(String code, InvokeableList subroutines, int lineOffset,
			boolean removeGlobals) {
		this(code, subroutines, lineOffset);
		this.removeGlobals = removeGlobals;
	}

	/**
	 * always returns an instance of block, just an optimized version
	 */
	@Override
	public Executable getOptimized(OrderOptimizer oo) {
		oo.pushState();
		for (int i = 0; i < executables.length; i++) {
			executables[i] = executables[i].getOptimized(oo);
		}
		oo.popState();

		return this;
	}

	/**
	 * adds a piece of code to the block
	 * 
	 * @warning repetitive code
	 * @param code
	 *            the code to add
	 * @param subroutines
	 *            this function adds subroutines that are defined inside of this
	 *            block to this list.
	 */
	protected void addToBlock(String code, InvokeableList subroutines) {
		String[] lines = Tokenizer.getLines(code);

		ArrayList<Executable> executables = new ArrayList<Executable>();

		for (int i = 0; i < lines.length; i++) {
			if (lines[i].equals(""))
				continue;

			Token[] line = Tokenizer.tokenize(lines[i]);
			if (line.length >= 2 && line[0].isTextCommand("repeat")) {
				int repeatLevel = 1;
				int endRepeat = -1;

				// find matching "end repeat"
				for (int j = i + 1; j < lines.length; j++) {
					Token[] tline = Tokenizer.tokenize(lines[j]);
					if (tline.length >= 1
							&& tline[0].isTextCommand("repeat"))
						repeatLevel++;
					else if (tline.length >= 2
							&& tline[0].isTextCommand("end")
							&& tline[1].isTextCommand("repeat")) {
						repeatLevel--;
					}

					if (repeatLevel <= 0) {
						endRepeat = j;
						break;
					}
				}
				if (endRepeat == -1) {
					throw new InterpreterException(
							"Syntax error: \"Repeat\" without matching \"End Repeat\"",
							new CodeLocationInfo(i + lineOffset));
				}

				String repeatCode = "";
				for (int j = i; j <= endRepeat; j++) {
					repeatCode += lines[j] + "\n";
				}
				executables.add(new RepeatStatement(repeatCode, subroutines, i
						+ lineOffset));
				i = endRepeat;
			} else if (line.length >= 2 && line[0].isTextCommand("if")) {
				int ifLevel = 1;
				int endIf = -1;

				// find matching "end if"
				for (int j = i + 1; j < lines.length; j++) {
					Token[] tline = Tokenizer.tokenize(lines[j]);
					if (tline.length >= 1 && tline[0].isTextCommand("if"))
						ifLevel++;
					else if (tline.length >= 2
							&& tline[0].isTextCommand("end")
							&& tline[1].isTextCommand("if")) {
						ifLevel--;
					}

					if (ifLevel <= 0) {
						endIf = j;
						break;
					}
				}
				if (endIf == -1) {
					throw new InterpreterException(
							"Syntax error: \"If\" without matching \"End If\"",
							new CodeLocationInfo(i + lineOffset));
				}

				String ifCode = "";
				for (int j = i; j <= endIf; j++) {
					ifCode += lines[j] + "\n";
				}
				executables.add(new IfStatement(ifCode, subroutines, i
						+ lineOffset));
				i = endIf;
			} else if (line.length >= 2 && line[0].isTextCommand("while")) {
				int whileLevel = 1;
				int endWhile = -1;

				// find matching "end while"
				for (int j = i + 1; j < lines.length; j++) {
					Token[] tline = Tokenizer.tokenize(lines[j]);
					if (tline.length >= 1 && tline[0].isTextCommand("while"))
						whileLevel++;
					else if (tline.length >= 2
							&& tline[0].isTextCommand("end")
							&& tline[1].isTextCommand("while")) {
						whileLevel--;
					}

					if (whileLevel <= 0) {
						endWhile = j;
						break;
					}
				}
				if (endWhile == -1) {
					throw new InterpreterException(
							"Syntax error: \"While\" without matching \"End While\"",
							new CodeLocationInfo(i + lineOffset));
				}

				String whileCode = "";
				for (int j = i; j <= endWhile; j++) {
					whileCode += lines[j] + "\n";
				}
				executables.add(new WhileStatement(whileCode, subroutines, i
						+ lineOffset));
				i = endWhile;
			} else if (line.length >= 2 && line[0].isTextCommand("sub")) {
				int endSub = -1;
				for (int j = i + 1; j < lines.length; j++) {
					Token[] tline = Tokenizer.tokenize(lines[j]);
					if (tline.length >= 2 && tline[0].isTextCommand("end")
							&& tline[1].isTextCommand("sub")) {
						endSub = j;
						break;
					} else if (tline.length >= 1
							&& tline[0].isTextCommand("sub")) {
						throw new InterpreterException(
								"Syntax error: definition of subroutine inside of another subroutine",
								new CodeLocationInfo(i + lineOffset));
					}
				}
				if (endSub == -1) {
					throw new InterpreterException(
							"Syntax error: \"Sub\" without matching \"End Sub\"",
							new CodeLocationInfo(i + lineOffset));
				}

				String subCode = "";
				for (int j = i; j <= endSub; j++) {
					subCode += lines[j] + "\n";
				}
				if (subroutines == null)
					throw new InterpreterException(
							"Invalid definition of subroutine " + line[1],
							new CodeLocationInfo(i + lineOffset));
				subroutines.add(new Subroutine(subCode, i + lineOffset));
				i = endSub;
			} else if (line.length >= 2 && line[0].isTextCommand("function")) {
				int endFunction = -1;
				for (int j = i + 1; j < lines.length; j++) {
					Token[] tline = Tokenizer.tokenize(lines[j]);
					if (tline.length >= 2 && tline[0].isTextCommand("end")
							&& tline[1].isTextCommand("function")) {
						endFunction = j;
						break;
					} else if (tline.length >= 1
							&& tline[0].isTextCommand("function")) {
						throw new InterpreterException(
								"Syntax error: definition of subroutine inside of another subroutine",
								new CodeLocationInfo(i + lineOffset));
					}
				}
				if (endFunction == -1) {
					throw new InterpreterException(
							"Syntax error: \"Function\" without matching \"End Function\"",
							new CodeLocationInfo(i + lineOffset));
				}

				String funcCode = "";
				for (int j = i; j <= endFunction; j++) {
					funcCode += lines[j] + "\n";
				}
				if (subroutines == null)
					throw new InterpreterException(
							"Invalid definition of subroutine " + line[1],
							new CodeLocationInfo(i + lineOffset));
				subroutines.add(new Function(funcCode, i + lineOffset));
				i = endFunction;
			} else if (line.length >= 1 && line[0].isTextCommand("else")) {
				String[] remainingCode = new String[lines.length - i];
				for (int j = 0; j < remainingCode.length; j++) {
					remainingCode[j] = lines[j + i];
				}
				foundElse(i, remainingCode);
				break;
			} else {
				if (line.length > 0 && !line[0].isTextCommand("end")) {
					CodeLocationInfo cli = new CodeLocationInfo(i + lineOffset);
					executables.add(Compiler.compile(lines[i], cli));
				}
			}
		}

		Executable[] newArray = new Executable[executables.size()
				+ this.executables.length];
		for (int i = 0; i < this.executables.length; i++) {
			newArray[i] = this.executables[i];
		}
		for (int i = this.executables.length; i < newArray.length; i++) {
			newArray[i] = executables.get(i - this.executables.length);
		}

		this.executables = newArray;
	}

	/*private String[] splitLine(String line) {
		ArrayList<String> tokens = new ArrayList<>();
		String token = "";
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			String cStr = new String(new char[] { c });
			if (Character.isWhitespace(c)) {
				if (!token.isEmpty()) {
					tokens.add(token);
					token = "";
				}
			} else if (Tokenizer.isOperator(cStr)
					|| cStr.matches("[\\(\\)\\{\\}\\[\\]]")) {
				if (!token.isEmpty()) {
					tokens.add(token);
					token = "";
				}
				tokens.add(cStr);
			} else {
				token += line.charAt(i);
			}
		}
		if (!token.isEmpty()) {
			tokens.add(token);
		}

		String[] arr = tokens.toArray(new String[] {});
		System.out.println(Arrays.toString(arr));
		return arr;
	}*/

	/**
	 * invoked, when an else is found in {@link addToBlock}
	 * 
	 * @param i
	 *            the line number of the else
	 * @param remainingCode
	 */
	protected void foundElse(int i, String[] remainingCode) {
		throw new InterpreterException("Else without If", new CodeLocationInfo(
				lineOffset + i + 1));
	}

	@Override
	public ReturnValue execute(LogoRenderer2D renderer, OrderInterpreter oi) {
		int variables = 0;
		ReturnValue ret = ReturnValue.NOTHING;
		for (int i = 0; i < executables.length; i++) {
			ReturnValue cb = executables[i].execute(renderer, oi);
			if (cb == ReturnValue.NEW_VARIABLE_CREATED)
				variables++;
			else if (cb != ReturnValue.NOTHING) {
				ret = cb;
				break;
			}
		}

		if (removeGlobals)
			for (int i = 0; i < variables; i++)
				oi.popVariable();

		return ret;
	}
}
