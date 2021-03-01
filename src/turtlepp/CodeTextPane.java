package turtlepp;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter.HighlightPainter;

/**
 * A text pane with syntax highlighting and font used for coding in MyLogo
 * 
 * @author Nicolas Winkler
 * 
 */
public class CodeTextPane extends JTextPane implements DocumentListener {
	private static final long serialVersionUID = 4642343024790200430L;

	/**
	 * Threads that are currently waiting to erase their highlighting. These are
	 * used to highlight an exception or an error and will remove the
	 * highlighting after a specific amount of time.
	 */
	private ArrayList<HighlightThread> highlightThreads;

	/**
	 * formats the text
	 */
	private SyntaxHighlighter syntaxHighlighter;

	/**
	 * creates and initializes the pane
	 */
	public CodeTextPane() {
		syntaxHighlighter = new SyntaxHighlighter(this);

		this.getDocument().addDocumentListener(this);
		setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
		highlightThreads = new ArrayList<HighlightThread>();
	}

	/**
	 * highligts one line of the code for a specific time
	 * 
	 * @param lineNumber
	 *            the index of the line to highlight
	 * @param millis
	 *            the amount of milliseconds after which the highligting is
	 *            removed. If millis is a negative number, the highlighting is
	 *            never removed.
	 */
	public void highlightLine(int lineNumber, int millis) {
		String text = super.getText();
		String[] lines = text.split("\\n");
		if (lineNumber >= lines.length)
			return;

		int lineCount = 0;
		for (int i = 0; i < getDocument().getDefaultRootElement()
				.getElementCount() - 1; i++) {
			String line = getLine(i);

			/*
			 * TODO bug fix: comment line ending with " _" must be ignored
			 */
			boolean twoeeew = !line.matches("(.|\\s)*//(.|\\s|\\n)*");
			// boolean twoeeew2 = "//ff".matches("//(.|\\s|\\n)*");
			if (!line.matches(".*[\\t\\ ]_\\s*") && twoeeew)
				lineCount++;
			if (lineCount == lineNumber)
				lineNumber = i + 1;
		}

		Element e = this.getDocument().getDefaultRootElement()
				.getElement(lineNumber);

		int startIndex = e.getStartOffset();
		int endIndex = e.getEndOffset();
		/*
		 * for (int i = 0; i < lineNumber; i++) startIndex += lines[i].length()
		 * + System.lineSeparator().length();
		 * 
		 * endIndex = startIndex + lines[lineNumber].length();
		 */

		try {
			HighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(
					new Color(255, 255, 100));
			Object highlight = super.getHighlighter().addHighlight(startIndex,
					endIndex, p);

			HighlightThread t = new HighlightThread(highlight, millis);
			highlightThreads.add(t);
			t.start();
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
	}

	public SyntaxHighlighter getSyntaxHighlighter() {
		return syntaxHighlighter;
	}

	public String getLine(int i) {
		Element e = this.getDocument().getDefaultRootElement().getElement(i);

		int startIndex = e.getStartOffset();
		int endIndex = e.getEndOffset();

		return getText().substring(startIndex, endIndex);
	}

	/**
	 * invoked, when the content of the pane has changed
	 */
	private void contentChanged() {
		for (int i = 0; i < highlightThreads.size(); i++) {
			highlightThreads.get(i).removeHighlighting();
			highlightThreads.remove(i);
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		contentChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		contentChanged();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		contentChanged();
	}

	/**
	 * Removes the highlighting of a code part after a specific amount of time
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	private class HighlightThread extends Thread {
		private Object highlight;
		private int millis;
		private boolean removed;

		/**
		 * @param highlight
		 *            the highlighted part to remove
		 * @param millis
		 *            the amount of time in milliseconds, when to erase the
		 *            highlighting
		 */
		public HighlightThread(Object highlight, int millis) {
			this.highlight = highlight;
			this.millis = millis;
			removed = false;
		}

		/**
		 * removes the highlighting and invalidates the thread
		 */
		public synchronized void removeHighlighting() {
			if (!removed)
				getHighlighter().removeHighlight(highlight);
			removed = true;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				// should not happen; otherwise too bad, remove highlighting
				// and exit thread
			}
			removeHighlighting();
		}
	}
}
