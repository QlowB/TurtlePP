package turtlepp.exec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import turtlepp.InterpreterException;
import turtlepp.Language;
import turtlepp.datatypes.Array;
import turtlepp.datatypes.Bool;
import turtlepp.datatypes.Float64;
import turtlepp.datatypes.Integer64;
import turtlepp.datatypes.LogoChar;
import turtlepp.datatypes.LogoString;
import turtlepp.datatypes.Reference;
import turtlepp.datatypes.Value;
import turtlepp.datatypes.Variable;
import turtlepp.datatypes.Value.BoolTermValue;
import turtlepp.datatypes.Value.ComparisonValue;

/**
 * Exception thrown while parsing a piece of code.
 * 
 * @author Nicolas Winkler
 * 
 */
class ParserException extends InterpreterException {
	private static final long serialVersionUID = 632797542061918768L;

	public ParserException(String message) {
		super(message);
	}

	public ParserException(String message, CodeLocationInfo cli) {
		super(message, cli);
	}
}

public class Tokenizer {
	private static Pattern operatorMatch = null;

	public static boolean isOperator(String token) {
		if (operatorMatch == null)
			operatorMatch = Pattern.compile(
					"[\\+\\-\\*/\\^<>]|<=|>=|==|!=|and|or|xor",
					Pattern.CASE_INSENSITIVE);
		return operatorMatch.matcher(token).matches();
	}

	/**
	 * Base class for every type of token. (However not all token types
	 * represent one single tokens; some of them are function calls etc.)
	 * 
	 * @author nicolas.winkler
	 * 
	 */
	static public abstract class Token {
		public abstract String getString();

		abstract boolean isTermOperator();

		public abstract boolean matches(String regex);

		public abstract Value createValue();

		public boolean isTextCommand(String string) {
			return false;
		}

		public boolean isModifyingOperator() {
			return false;
		}

		@Override
		public String toString() {
			return getString();
		}

		/**
		 * @return <code>null</code>, if the token is not a TextToken, the
		 *         content of the TextToken otherwise.
		 */
		public String getStringToken() {
			return "";
		}

		public boolean canBeVariable() {
			return false;
		}
	}

	/**
	 * Token that stands for one single string token.
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	static public class TextToken extends Token {
		public String token;

		public TextToken(String token) {
			this.token = token;
		}

		public boolean isTextToken() {
			return !token.matches(".*(=|\\-=|\\+=|\\*=|/=|^=|[\\+\\-\\*/]).*");
		}

		@Override
		public boolean isTermOperator() {
			return isOperator(token);
		}

		@Override
		public String getString() {
			return "TextToken: " + token;
		}

		@Override
		public boolean matches(String regex) {
			return Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
					.matcher(token).matches();
		}

		@Override
		public boolean isTextCommand(String string) {
			return string.equalsIgnoreCase(token);
		}

		@Override
		public Value createValue() {
			if (Language.isNumeric(token))
				return new Value.ConstantValue(token);
			else if (token.matches("'.'"))
				return new Value.ConstantValue(token);
			else {
				if (token.startsWith("-"))
					return new Value.NegativeVarValue(token.substring(1));
				else
					return new Value.VariableValue(token);
			}
		}

		@Override
		public String getStringToken() {
			return token;
		}

		public boolean canBeVariable() {
			return true;
		}
	}

	static public class StringToken extends Token {
		public String token;

		public StringToken(String token) {
			this.token = token.substring(1, token.length() - 1);
			replace();
		}

		private void replace() {
			// token = token.replaceAll("\\\\\\\\", "\\\\");
			// token = token.replaceAll("\\\\\"", "\""); // replace
			// \" with "

			token = replaceBackslashes(token); // split at \\ and glue
												// together again
			// token.
		}

		private String replaceBackslashes(String text) {
			Pattern pattern = Pattern.compile("\\\\\\\\");
			Matcher m = pattern.matcher(text);
			String together = "";

			int lastIndex = 0;
			while (m.find()) {
				int start = m.start();
				int end = m.end();
				String last = text.substring(lastIndex, start);
				together += replaceChars(last) + "\\";
				lastIndex = end;
			}
			together += replaceChars(text.substring(lastIndex));
			return together;
		}

		private String replaceChars(String last) {
			last = last.replaceAll("\\\\n", "\n");
			last = last.replaceAll("\\\\r", "\r");
			last = last.replaceAll("\\\\t", "\t");
			last = last.replaceAll("\\\\\"", "\"");
			return last;
		}

		public boolean isTextToken() {
			return false;
		}

		@Override
		public boolean isTermOperator() {
			return false;
		}

		@Override
		public String getString() {
			return "StringToken: " + token;
		}

		@Override
		public boolean matches(String regex) {
			return false;
		}

		@Override
		public boolean isTextCommand(String string) {
			return false;
		}

		@Override
		public Value createValue() {
			return new Value.StringValue(token);
		}

		@Override
		public String getStringToken() {
			return "\"" + token + "\"";
		}
	}

	/**
	 * This type of token represents a whole term-inline function call.
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	static public class FncToken extends Token {
		Token[] tokens;

		FncToken(Token[] toks) {
			tokens = toks;
		}

		@Override
		public String getString() {
			String ret = "FncToken: [";
			for (int i = 0; i < tokens.length; i++) {
				ret += tokens[i].getString()
						+ ((i < tokens.length - 1) ? ", " : "");
			}
			ret += "]";
			return ret;
		}

		@Override
		boolean isTermOperator() {
			return false;
		}

		@Override
		public boolean matches(String regex) {
			return false;
		}

		@Override
		public Value createValue() {
			Value.FunctionValue fv = new Value.FunctionValue(
					tokens[0].getStringToken());
			Value[] args = new Value[tokens.length - 1];
			for (int i = 1; i < tokens.length; i++) {
				args[i - 1] = tokens[i].createValue();
			}
			fv.setArgs(args);
			return fv;
		}
	}

	static public class ArrayAccessToken extends Token {
		private Token varName;
		private Token[] arguments;

		public ArrayAccessToken(Token[] toks) {
			/*
			 * if (toks.length != 2) throw new
			 * ParserException("invalid array access.");
			 */
			varName = toks[0];
			arguments = new Token[toks.length - 1];
			for (int i = 0; i < arguments.length; i++)
				arguments[i] = toks[i + 1];
		}

		@Override
		public String toString() {
			return "ArrayAccessToken [varName=" + varName + ", arguments="
					+ Arrays.toString(arguments) + "]";
		}

		@Override
		boolean isTermOperator() {
			return false;
		}

		@Override
		public boolean matches(String regex) {
			return false;
		}

		@Override
		public Value createValue() {
			return new Value.ArrayAccessValue(this);
		}

		public Token getArrayVariable() {
			return varName;
		}

		public int getNIndices() {
			return arguments.length;
		}

		public Token getIndex(int i) {
			return arguments[i];
		}

		@Override
		public String getString() {
			return toString();
		}

		public boolean canBeVariable() {
			return true;
		}

		public Variable createVariableTemplate() {
			Variable template = null;
			if (varName instanceof TextToken) {
				String varType = ((TextToken) varName).token;
				if (varType.equalsIgnoreCase(Variable
						.getTypeName(Integer64.class)))
					template = new Integer64(null);
				else if (varType.equalsIgnoreCase(Variable
						.getTypeName(Float64.class)))
					template = new Float64(null);
				else if (varType.equalsIgnoreCase(Variable
						.getTypeName(LogoString.class)))
					template = new LogoString(null);
				else if (varType.equalsIgnoreCase(Variable
						.getTypeName(Bool.class)))
					template = new Bool(null);
				else if (varType.equalsIgnoreCase(Variable
						.getTypeName(Reference.class)))
					template = new Reference(null);
				else if (varType.equalsIgnoreCase(Variable
						.getTypeName(LogoChar.class)))
					template = new LogoChar(null);
			} else if (varName instanceof ArrayAccessToken) {
				template = ((ArrayAccessToken) varName)
						.createVariableTemplate();
				try {
					@SuppressWarnings("unchecked")
					Array a = new Array(null,
							(Class<? extends Variable[]>) Class.forName("[L"
									+ template.getClass().getName()));
					template = a;
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

			return template;
		}
	}

	static public class NegativeToken extends Token {
		Token token;

		public NegativeToken() {
			token = null;
		}

		public void setToken(Token t) {
			this.token = t;
		}

		@Override
		public String getString() {
			return "-" + ((token != null) ? token.toString() : "");
		}

		@Override
		boolean isTermOperator() {
			return false;
		}

		@Override
		public boolean matches(String regex) {
			return false;
		}

		@Override
		public Value createValue() {
			return new Value.NegativeVarValue(token.createValue());
		}

		public boolean valueExpected() {
			return token == null;
		}
	}

	/**
	 * Basically not one single token but multiple tokens together. They
	 * represent a term that can be evaluated to a single value.
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	static public class TermToken extends Token {
		private Token[] tokens;
		private boolean isParanthesisExpression;

		public TermToken(Token token) {
			if (token instanceof TermToken
					&& !((TermToken) token).isParanthesisExpression()) {
				TermToken tt = (TermToken) token;
				tokens = new Token[tt.tokens.length];
				for (int i = 0; i < tokens.length; i++) {
					tokens[i] = tt.tokens[i];
				}
				setIsParanthesisExpression(false);
			} else {
				tokens = new Token[1];
				tokens[0] = token;
			}

		}

		public TermToken(Token[] toks) {
			this.tokens = toks;
			setIsParanthesisExpression(false);
		}

		public TermToken() {
			tokens = new Token[0];
			setIsParanthesisExpression(false);
		}

		@Override
		public String getString() {
			String ret = "TermToken: [";
			for (int i = 0; i < tokens.length; i++) {
				ret += tokens[i].getString()
						+ ((i < tokens.length - 1) ? ", " : "");
			}
			ret += "]";
			return ret;
		}

		void addToken(Token token) {
			Token[] newTokens = new Token[tokens.length + 1];
			for (int i = 0; i < tokens.length; i++)
				newTokens[i] = tokens[i];
			newTokens[tokens.length] = token;
			tokens = newTokens;
		}

		private boolean isExpectingOperand() {
			if (tokens.length > 0) {
				if (tokens[tokens.length - 1].isTermOperator())
					return true;
				else
					return false;
			} else
				return true;
		}

		@Override
		boolean isTermOperator() {
			return false;
		}

		public Value getTermValue() {
			if (ComparisonValue.isComparison(this)) {
				return new ComparisonValue(this);
			} else {
				return new BoolTermValue(this).simplify();
			}
		}

		/**
		 * splits the token into shorter tokens
		 * 
		 * @param regex
		 *            the regular expression to determine which token is a
		 *            delimiter (with {@link Pattern#CASE_INSENSITIVE} flag)
		 */
		public TermToken[] split(String regex) {
			Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			ArrayList<TermToken> newTokens = new ArrayList<TermToken>();
			TermToken lastOperator = null;
			for (int i = 0; i < tokens.length; i++) {
				if (p.matcher(tokens[i].getStringToken()).matches()) {
					if (lastOperator != null)
						newTokens.add(lastOperator);
					lastOperator = new TermToken(tokens[i]);
				} else {
					if (lastOperator == null)
						lastOperator = new TermToken(tokens[i]);
					else
						lastOperator.addToken(tokens[i]);
				}
			}
			if (lastOperator != null)
				newTokens.add(lastOperator);
			return newTokens.toArray(new TermToken[0]);
		}

		public TermToken getNormalized() {
			if (tokens.length == 1 && tokens[0] instanceof TermToken) {
				return ((TermToken) tokens[0]).getNormalized();
			}
			return this;
		}

		public Token simplify() {
			if (tokens.length == 1) {
				return tokens[0];
			}
			return this;
		}

		@Override
		public boolean matches(String regex) {
			return false;
		}

		@Override
		public Value createValue() {
			return getNormalized().getTermValue();
		}

		public int getNTokens() {
			return tokens.length;
		}

		public Token getToken(int i) {
			return tokens[i];
		}

		public boolean containsOperator(String operator) {
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].matches(operator))
					return true;
			}
			return false;
		}

		public void setIsParanthesisExpression(boolean b) {
			this.isParanthesisExpression = b;
		}

		public boolean isParanthesisExpression() {
			return isParanthesisExpression;
		}

		public Token getLastToken() {
			return tokens[tokens.length - 1];
		}
	}

	/**
	 * parses a string of code and returns its representation as an array of
	 * {@link Token}s
	 * 
	 * @param cmd
	 *            the code
	 */
	public static Token[] tokenize(String cmd) {
		Lexer lexer = new Lexer(cmd);
		String[] tokens = lexer.getTokens();

		return createTokens(tokens, 0, tokens.length - 1, false);
	}

	/**
	 * parses raw string tokens to {@link Token}s
	 * 
	 * @param tokens
	 *            the raw strings
	 * @param startIndex
	 *            the beginning index
	 * @param endIndex
	 *            the end index
	 * @return the parsed {@link Token}s
	 */
	private static Token[] createTokens(String[] tokens, int startIndex,
			int endIndex, boolean inRoundParantheses) {
		ArrayList<Token> tokenBuff = new ArrayList<Token>();
		for (int i = startIndex; i <= endIndex; i++) {
			i = addToken(tokenBuff, tokens, i, inRoundParantheses);
		}

		if (tokenBuff.isEmpty())
			return new Token[] {};
		else
			return tokenBuff.toArray(new Token[0]);
	}

	/**
	 * expands the given list of tokens by some new ones
	 * 
	 * @param tokenBuff
	 *            the list of {@link Token}s to expand
	 * @param tokens
	 *            the raw tokens as {@link String}s
	 * @param i
	 *            the index of the token to add
	 * @param inRoundParantheses
	 *            if it is an expression "wrapped" in round parantheses (in
	 *            other words, a term expression)
	 * @return the index of the token last added
	 */
	private static int addToken(List<Token> tokenBuff, String[] tokens, int i,
			boolean inRoundParantheses) {
		String token = tokens[i];
		Token lastToken = tokenBuff.isEmpty() ? null : tokenBuff.get(tokenBuff
				.size() - 1);

		boolean operandExpected = lastToken instanceof TermToken
				&& ((TermToken) lastToken).isExpectingOperand();

		// value for negative expression expected
		boolean valExpected = (lastToken instanceof NegativeToken && ((NegativeToken) lastToken)
				.valueExpected())
				|| (lastToken instanceof TermToken
						&& ((TermToken) lastToken).getLastToken() instanceof NegativeToken && ((NegativeToken) ((TermToken) lastToken)
							.getLastToken()).valueExpected());
		NegativeToken valExpectator = null;
		if (valExpected) {
			if (lastToken instanceof NegativeToken) {
				valExpectator = (NegativeToken) lastToken;
			} else if (lastToken instanceof TermToken) {
				valExpectator = (NegativeToken) ((TermToken) lastToken)
						.getLastToken();
			}
		}

		// if a minus is expected to be an operator, not a sign of a number
		boolean minusAsOperator = (!operandExpected && lastToken instanceof TermToken)
				|| (tokenBuff.size() == 1 && inRoundParantheses)
				|| (tokenBuff.size() > 1 && lastToken instanceof TextToken
						&& ((TextToken) lastToken).isTextToken() || (tokenBuff
						.size() > 1 && lastToken instanceof NegativeToken));

		boolean newTerm = lastToken instanceof TermToken
				&& ((TermToken) lastToken).isParanthesisExpression();

		if (token.equals("[")) {
			int mpi = getMatchingParanthesisIndex(tokens, i);
			Token[] toks = createTokens(tokens, i + 1, mpi - 1, false);
			FncToken ft = new FncToken(toks);
			if (operandExpected) {
				TermToken tt = new TermToken(lastToken);
				tt.addToken(ft);
				tokenBuff.set(tokenBuff.size() - 1, tt);
			} else if (valExpected) {
				valExpectator.setToken(ft);
			} else
				tokenBuff.add(new TermToken(ft));
			i = mpi;
		} else if (token.equals("{")) {
			int mpi = getMatchingParanthesisIndex(tokens, i);
			Token[] toks = createTokens(tokens, i + 1, mpi - 1, false);
			ArrayAccessToken aat = new ArrayAccessToken(toks);
			if (operandExpected) {
				TermToken tt = new TermToken(lastToken);
				tt.addToken(aat);
				tokenBuff.set(tokenBuff.size() - 1, tt);
			} else if (valExpected) {
				valExpectator.setToken(aat);
			} else
				tokenBuff.add(new TermToken(aat));
			i = mpi;
		} else if (token.equals("(")) {
			int mpi = getMatchingParanthesisIndex(tokens, i);
			Token[] toks = createTokens(tokens, i + 1, mpi - 1, true);
			TermToken tt = new TermToken(toks[0]);
			tt.setIsParanthesisExpression(true);
			if (operandExpected) {
				TermToken last = new TermToken(lastToken);
				last.addToken(tt);
				tokenBuff.set(tokenBuff.size() - 1, last);
			} else if (valExpected) {
				valExpectator.setToken(tt);
			} else
				tokenBuff.add(new TermToken(new Token[] { tt }));
			i = mpi;
		} else {
			Token t;
			if (token.matches("[\\+\\-\\*/\\^=!<>]") && tokens.length > i + 1
					&& tokens[i + 1].equals("=")) { // +=, -=, *=, /=, ^=, ==,
													// !=, <=, >=
				t = new TextToken(token + tokens[i + 1]);
				i++;
			} else
				t = createToken(token);

			boolean minusHandled = false;
			if (t.isTextCommand("-") && !minusAsOperator
					&& i < tokens.length - 1) {
				t = new NegativeToken();
				minusHandled = true;
			}
			if (t.isTermOperator() && newTerm && !minusHandled) {
				if (tokenBuff.isEmpty())
					throw new ParserException("unexpected operator "
							+ t.getStringToken()
							+ " at the beginning of a line.");
				TermToken tt = new TermToken();
				tt.addToken(lastToken);
				tt.addToken(t);
				tokenBuff.set(tokenBuff.size() - 1, tt);
			} else if (t.isTermOperator() && !minusHandled) {
				if (tokenBuff.isEmpty())
					throw new ParserException("unexpected operator "
							+ t.getStringToken()
							+ " at the beginning of a line.");
				TermToken tt = new TermToken(lastToken);
				tt.addToken(t);
				tokenBuff.set(tokenBuff.size() - 1, tt);
			} else if (operandExpected) {
				((TermToken) tokenBuff.get(tokenBuff.size() - 1)).addToken(t);
			} else if (valExpected) {
				valExpectator.setToken(t);
			} else {
				tokenBuff.add(t);
			}
		}
		return i;
	}

	private static Token createToken(String token) {
		if (isStringToken(token))
			return new StringToken(token);
		else
			return new TextToken(token);
	}

	private static boolean isStringToken(String token) {
		return token.startsWith("\"") && token.endsWith("\"");
	}

	/**
	 * gets the index of a closing paranthesis to the matching '[', '{' or '('
	 * 
	 * @param toks
	 *            the tokens
	 * @param index
	 *            the index of the opening paranthesis
	 * @return the index of the closing paranthesis
	 */
	protected static int getMatchingParanthesisIndex(String[] toks, int index) {
		String opening = toks[index];
		String closing = null;
		if (opening.equals("["))
			closing = "]";
		else if (opening.equals("{"))
			closing = "}";
		else if (opening.equals("("))
			closing = ")";
		index++;
		int parantLevel = 1;
		while (true) {
			if (toks[index].equals(opening))
				parantLevel++;
			if (toks[index].equals(closing))
				parantLevel--;

			if (parantLevel <= 0)
				break;

			if (++index >= toks.length)
				throw new ParserException("'" + opening
						+ "' without matching '" + closing + "'");
		}
		return index;
	}

	/**
	 * Cuts the string into pieces, this is my last resort!
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class Lexer {
		private String code;
		private ArrayList<String> tokens;

		/**
		 * tokens that match these regular expressions get a separate token
		 */
		protected static final String[] delimitters = new String[] { "\\+",
				"\\-", "\\*", "=", "/", "\\^", "\\-\\[", "\\-\\(", "\\-\\{",
				"\\[", "\\]", "\\(", "\\)", "\\{", "\\}" };

		private static String regexDelim;
		static {
			regexDelim = "";
			for (int i = 0; i < delimitters.length; i++) {
				String delim = delimitters[i];
				regexDelim += delim + ".*"
						+ (i < delimitters.length - 1 ? "|" : "");
			}
		}

		/**
		 * creates a new Lexer that scans the given code
		 * 
		 * @param code
		 *            the text to scan
		 */
		public Lexer(String code) {
			this.code = code;
			this.lex();
		}

		/**
		 * tokenizes the input string.
		 */
		protected void lex() {
			tokens = new ArrayList<String>();

			String current = "";
			String left = code;
			for (int i = 0; i < code.length(); i++) {
				char chr = code.charAt(i);
				left = code.substring(i);
				if (isDelimitter(left)) {
					if (!current.isEmpty()) {
						tokens.add(current);
						current = "";
					}
					tokens.add(new String(new char[] { chr }));
				} else if (Character.isWhitespace(chr)) {
					if (!current.isEmpty()) {
						tokens.add(current);
						current = "";
					}
				} else if (chr == '\'') {
					int j = i + 2;
					if (j >= code.length()) {
						throw new ParserException("\' without matching \'");
					}
					String content = code.substring(i, i + 3);
					tokens.add(content);
					i = j + 1;
				} else if (chr == '"') {
					int j = i + 1;

					if (j >= code.length()) {
						throw new ParserException("\" without matching \"");
					}

					char chr2 = code.charAt(j);
					int backslashes = 0;
					while (chr2 != '"' || (backslashes & 0x01) != 0) {
						if (chr2 == '\\')
							backslashes++;
						else
							backslashes = 0;

						j++;
						if (j >= code.length()) {
							throw new ParserException("\" without matching \"");
						}
						chr2 = code.charAt(j);
					}
					j++;
					String content = code.substring(i, j);
					tokens.add(content);
					i = j - 1;
				} else {
					current += chr;
				}
			}
			if (!current.isEmpty()) {
				tokens.add(current);
			}
		}

		protected boolean isDelimitter(String left) {
			// return delimitters.indexOf(character) != -1;
			return left.matches(regexDelim);
		}

		/**
		 * @return an array containing the tokens
		 */
		public String[] getTokens() {
			return tokens.toArray(new String[] {});
		}
	}

	public static String[] getLines(String code) {
		code = code.replaceAll("\\s_\\n", " ");
		String[] splits = code.split("\\n");
		for (int i = 0; i < splits.length; i++)
			splits[i] = splits[i].trim();
		return splits;
	}
}
