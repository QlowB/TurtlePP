package turtlepp;

import java.awt.Color;
import java.util.HashSet;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import turtlepp.exec.Tokenizer;

public class SyntaxHighlighter extends DefaultStyledDocument {
	private static final long serialVersionUID = -1367638907386801131L;

	private Element rootElement;
	private static MutableAttributeSet normal;
	private static MutableAttributeSet keyword;
	private static MutableAttributeSet number;
	private static MutableAttributeSet comment;
	private static MutableAttributeSet nativeFunction;
	private static MutableAttributeSet mathConstant;
	private static MutableAttributeSet invokedFunction;
	private static MutableAttributeSet strings;
	private HashSet<String> keywords;
	private HashSet<String> nativeFunctions;
	private HashSet<String> mathConstants;
	private HashSet<String> invokedFunctions;
	JTextPane textPane;

	static {
		normal = new SimpleAttributeSet();
		StyleConstants.setForeground(normal, Color.black);
		keyword = new SimpleAttributeSet();
		StyleConstants.setForeground(keyword, Color.blue);
		number = new SimpleAttributeSet();
		StyleConstants.setForeground(number, Color.red);
		comment = new SimpleAttributeSet();
		StyleConstants.setForeground(comment, Color.green.darker().darker()
				.darker());
		nativeFunction = new SimpleAttributeSet();
		StyleConstants.setForeground(nativeFunction, Color.darkGray);
		mathConstant = new SimpleAttributeSet();
		StyleConstants.setForeground(mathConstant, Color.red.darker().darker());
		invokedFunction = new SimpleAttributeSet();
		StyleConstants.setForeground(invokedFunction, Color.cyan.darker()
				.darker());
		strings = new SimpleAttributeSet();
		StyleConstants.setForeground(strings, Color.blue.darker());
	}

	public SyntaxHighlighter() {
		rootElement = this.getDefaultRootElement();

		keywords = new HashSet<String>();
		nativeFunctions = new HashSet<String>();
		mathConstants = new HashSet<String>();
		invokedFunctions = new HashSet<String>();

		String[] kw = Language.getKeywords();
		for (int i = 0; i < kw.length; i++)
			keywords.add(kw[i].toLowerCase());

		String[] nf = Language.getNativeFunctions();
		for (int i = 0; i < nf.length; i++)
			nativeFunctions.add(nf[i].toLowerCase());

		String[] mc = Language.getMathConstants();
		for (int i = 0; i < mc.length; i++) {
			mathConstants.add(mc[i].toLowerCase());
			mathConstants.add("-" + mc[i].toLowerCase());
		}

		String[] inf = Language.getInvokedFunctions();
		for (int i = 0; i < inf.length; i++)
			invokedFunctions.add(inf[i].toLowerCase());
	}

	public SyntaxHighlighter(JTextPane input) {
		this();
		input.setDocument(this);
		textPane = input;
	}

	public static void setNormalTextColor(Color c) {
		StyleConstants.setForeground(normal, c);
	}

	public static void setKewordColor(Color c) {
		StyleConstants.setForeground(keyword, c);
	}

	public static void setNumbersColor(Color c) {
		StyleConstants.setForeground(number, c);
	}

	public static void setConstantsColor(Color c) {
		StyleConstants.setForeground(mathConstant, c);
	}

	public static void setNativeFunctionsColor(Color c) {
		StyleConstants.setForeground(nativeFunction, c);
	}

	public static void setStringsColor(Color c) {
		StyleConstants.setForeground(strings, c);
	}

	public static void setCallbackFunctionsColor(Color c) {
		StyleConstants.setForeground(invokedFunction, c);
	}

	public static void setCommentsColor(Color c) {
		StyleConstants.setForeground(comment, c);
	}

	public static Color getNormalTextColor() {
		return StyleConstants.getForeground(normal);
	}

	public static Color getKeywordColor() {
		return StyleConstants.getForeground(keyword);
	}

	public static Color getNumbersColor() {
		return StyleConstants.getForeground(number);
	}

	public static Color getConstantsColor() {
		return StyleConstants.getForeground(mathConstant);
	}

	public static Color getNativeFunctionsColor() {
		return StyleConstants.getForeground(nativeFunction);
	}

	public static Color getStringsColor() {
		return StyleConstants.getForeground(strings);
	}

	public static Color getCallbackFunctionsColor() {
		return StyleConstants.getForeground(invokedFunction);
	}

	public static Color getCommentsColor() {
		return StyleConstants.getForeground(comment);
	}

	public void reformat() {
		try {
			processChangedLines(0, this.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Override to apply keyword highlighting after the document has been
	 * updated
	 */
	@Override
	public void insertString(int offset, String str, AttributeSet a)
			throws BadLocationException {
		str = str.replaceAll("\\t", "  "); // replace tabulators with two space
											// characters
		super.insertString(offset, str, a);

		processChangedLines(offset, str.length());
		if (str.equals(System.lineSeparator()) || str.equals("\n")
				|| str.equals("\r\n")) {
			processNewBreak(offset, str.length());
		}

		if (str.equals("d") || str.equals("e"))
			processEndInserted(offset, str.length());
	}

	/*
	 * Override to apply keyword highlighting after the document has been
	 * updated
	 */
	@Override
	public void remove(int offset, int length) throws BadLocationException {
		super.remove(offset, length);
		processChangedLines(offset, 0);
	}

	public void processNewBreak(int offset, int length)
			throws BadLocationException {
		String[] inruck = new String[] { "if", "sub", "repeat", "while",
				"function", "else" };
		int line = rootElement.getElementIndex(offset);
		int offsetStart = rootElement.getElement(line).getStartOffset();
		int offsetEnd = rootElement.getElement(line).getEndOffset();
		String content = this.getText(0, this.getLength());
		String linetext = content.substring(offsetStart, offsetEnd);

		String ruck = "";
		for (int i = 0; i < linetext.length(); i++) {
			if (linetext.charAt(i) == ' ')
				ruck += " ";
			else
				break;
		}

		this.insertString(offset + 1, ruck, null);

		for (int i = 0; i < inruck.length; i++) {
			if (linetext.toLowerCase().trim().startsWith(inruck[i])
					&& linetext.length() > inruck[i].length() + 1
					&& isDelimiter(linetext.substring(inruck[i].length(),
							inruck[i].length() + 1))) {

				this.insertString(offset + 1, "  ", null);
				// System.out.println(content.charAt(offsetStart));
			}
		}
		if (linetext.toLowerCase().trim().matches(".*\\s_")) {
			this.insertString(offset + 1, "  ", null);
			// System.out.println(content.charAt(offsetStart));
		}
	}

	public void processEndInserted(int offset, int length)
			throws BadLocationException {
		int line = rootElement.getElementIndex(offset);
		int offsetStart = rootElement.getElement(line).getStartOffset();
		int offsetEnd = rootElement.getElement(line).getEndOffset();
		// String content = this.getText(0, this.getLength());
		String linetext = this.getText(offsetStart, offsetEnd - offsetStart);

		if (line > 0
				&& (linetext.trim().equalsIgnoreCase("end") || linetext.trim()
						.equalsIgnoreCase("else"))) {
			int offsetLastLine = rootElement.getElement(line).getStartOffset();
			int ollLength = rootElement.getElement(line).getEndOffset()
					- offsetLastLine;
			String llinetext = this.getText(offsetLastLine, ollLength);
			int spacesLast = 0;
			int spaces = 0;
			for (; llinetext.charAt(spacesLast) == ' '; spacesLast++)
				;
			for (; linetext.charAt(spaces) == ' '; spaces++)
				;

			if (spaces == spacesLast && spaces >= 2) {
				new Thread(new Remover(offsetStart, 2)).start();
				// System.out.println(content.charAt(offsetStart));
			}
		}
	}

	/**
	 * A task which removes part of the document. This is normally invoked from
	 * a new thread, to go around the thread lock on the document.
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	protected class Remover implements Runnable {
		int offset;
		int length;

		Remover(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}

		public void run() {
			try {
				textPane.setCaretPosition(textPane.getCaretPosition() - 2);
				SyntaxHighlighter.this.remove(offset, length);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Determine how many lines have been changed, then apply highlighting to
	 * each line
	 */
	public void processChangedLines(int offset, int length)
			throws BadLocationException {
		String content = this.getText(0, this.getLength());
		
		int startLine = rootElement.getElementIndex(offset);
		int endDoc = rootElement.getElementIndex(offset + length); // rootElement.getElementCount()
																	// - 1;
		
		for (int i = startLine; i <= /* endLine */endDoc; i++) {
			applyHighlighting(content, i);
			applyStringHighlighting(content, i);
			applyCommentHighlighting(i);
		}
	}

	private void applyStringHighlighting(String content, int line) {
		int startOffset = rootElement.getElement(line).getStartOffset();
		int endOffset = rootElement.getElement(line).getEndOffset() - 1;
		int startString = -1;

		int backslashes = 0;
		while (startOffset < endOffset) {
			if (content.charAt(startOffset) == '"' && (backslashes & 0x01) == 0) {
				if (startString != -1) {
					this.setCharacterAttributes(startString, startOffset
							- startString + 1, strings, false);
					startString = -1;
				} else
					startString = startOffset;
			}

			if (content.charAt(startOffset) == '\'') {

				if (content.length() > startOffset + 2
						&& content.charAt(startOffset + 2) == '\'') {
					this.setCharacterAttributes(startOffset, 3, strings, false);
					startOffset += 2;
				}
			}

			if (startString != -1 && content.charAt(startOffset) == '\\') {
				backslashes++;
			} else
				backslashes = 0;

			startOffset++;
		}
		if (startString != -1) {
			this.setCharacterAttributes(startString, startOffset - startString,
					strings, false);
		}
	}

	private void applyCommentHighlighting(int line) {
		Element e = getDefaultRootElement().getElement(line);
		int startIndex = e.getStartOffset();
		int endIndex = e.getEndOffset();
		int commentStart = -1;
		String text = "";
		try {
			text = getText(startIndex, endIndex - startIndex);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		for (int i = 0; i < text.length(); i++) {
			if (i > 0 && text.charAt(i - 1) == '/' && text.charAt(i) == '/') {
				commentStart = i - 1;
				break;
			}
		}
		if (commentStart > -1)
			this.setCharacterAttributes(startIndex + commentStart,
					text.length() - commentStart, comment, false);
	}

	/*
	 * Parse the line to determine the appropriate highlighting
	 */
	private void applyHighlighting(String content, int line)
			throws BadLocationException {
		int startOffset = rootElement.getElement(line).getStartOffset();
		int endOffset = rootElement.getElement(line).getEndOffset() - 1;
		int lineLength = endOffset - startOffset;
		int contentLength = content.length();
		if (endOffset >= contentLength) {
			endOffset = contentLength - 1;
		}
		// set normal attributes for the line
		this.setCharacterAttributes(startOffset, lineLength, normal, true);
		// check for tokens
		while (startOffset <= endOffset) {
			// skip the delimiters to find the start of a new token
			while (isDelimiter(content.substring(startOffset, startOffset + 1))) {
				if (startOffset < endOffset) {
					startOffset++;
				} else {
					return;
				}
			}
			// Extract and process the entire token
			startOffset = getToken(content, startOffset, endOffset);
		}
	}

	private int getToken(String content, int startOffset, int endOffset) {
		int endOfToken = startOffset + 1;
		while (endOfToken <= endOffset) {
			if (isDelimiter(content.substring(endOfToken, endOfToken + 1))) {
				break;
			}
			endOfToken++;
		}
		String token = content.substring(startOffset, endOfToken);
		if (isNativeFunction(token)) {
			this.setCharacterAttributes(startOffset, endOfToken - startOffset,
					nativeFunction, false);
		}
		if (isNumber(token)) {
			this.setCharacterAttributes(startOffset, endOfToken - startOffset,
					number, false);
		}
		if (isMathConstant(token)) {
			this.setCharacterAttributes(startOffset, endOfToken - startOffset,
					mathConstant, false);
		}
		if (isInvokedFunction(token)) {
			this.setCharacterAttributes(startOffset, endOfToken - startOffset,
					invokedFunction, false);
		}
		if (isKeyword(token)) {
			this.setCharacterAttributes(startOffset, endOfToken - startOffset,
					keyword, false);
		}
		return endOfToken + 1;
	}

	protected boolean isDelimiter(String character) {
		/*
		 * String operands = ";:{}()[]+-/%<=>!&|^~*"; return
		 * Character.isWhitespace(character.charAt(0)) ||
		 * operands.indexOf(character) != -1;
		 */

		char c = character.charAt(0);

		return Character.isWhitespace(c) || c == '[' || c == ']' || c == '('
				|| c == ')' || c == '{' || c == '}'
				|| Tokenizer.isOperator(character) || c == '=';
	}

	protected boolean isKeyword(String token) {
		return keywords.contains(token.toLowerCase());
	}

	protected boolean isNativeFunction(String token) {
		return nativeFunctions.contains(token.toLowerCase());
	}

	protected boolean isMathConstant(String token) {
		return mathConstants.contains(token.toLowerCase());
	}

	private boolean isInvokedFunction(String token) {
		return invokedFunctions.contains(token.toLowerCase());
	}

	protected boolean isNumber(String token) {
		return Language.isNumeric(token);
	}

}
