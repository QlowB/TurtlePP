package turtlepp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

import turtlepp.exec.CodeLocationInfo;
import turtlepp.exec.Compiler;
import turtlepp.exec.Executable;
import turtlepp.exec.OrderInterpreter;

/**
 * some sort of a console; it can display text.
 * 
 * @author Nicolas Winkler
 * 
 */
public class ConsolePanel extends UtilityPanel {
	private static final long serialVersionUID = -8471078754490688745L;
	private static final String helpText = "commands:\n"
			+ "\t\\clear, \\cls: clears the console\n"
			+ "\t\\stop: stops the current execution";

	/**
	 * One entry in the console.
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	private static class ConsoleEntry {
		public ConsoleEntry(Type type, String str) {
			this.type = type;
			this.text = str;
		}

		private Type type;
		private String text;

		public enum Type {
			OUTPUT, INPUT, ERROR, COMMAND, CONSOLE_COMMAND
		}

		public Color getColor() {
			switch (type) {
			case INPUT:
				return Color.cyan.darker().darker();
			case OUTPUT:
				return Color.black;
			case ERROR:
				return Color.red;
			case COMMAND:
				return Color.red.darker().darker();
			case CONSOLE_COMMAND:
				return Color.green.darker().darker();
			default:
				break;
			}
			return null;
		}

		public Type getType() {
			return type;
		}
	}

	private class InputPane extends JTextPane implements DocumentListener,
			KeyListener {
		private static final long serialVersionUID = 6191848355720030193L;

		public InputPane() {
			getDocument().addDocumentListener(this);
			setForeground(Color.black);
			addKeyListener(this);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			update();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			update();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			update();
		}

		private void update() {
			Document d = getDocument();
			try {
				String content = d.getText(0, 2);
				if (content.matches("/[^/]?"))
					setForeground(Color.red.darker().darker());
				else if (content.matches("\\\\[^\\\\]?"))
					setForeground(Color.green.darker().darker());
				else
					setForeground(Color.cyan.darker().darker());
			} catch (BadLocationException e) {
				setForeground(Color.cyan.darker().darker());
			}

			if (consoleHistoryIndex == consoleHistory.size())
				nowTyping = getText();
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				if (consoleHistoryIndex > 0)
					consoleHistoryIndex--;
				setText(consoleHistory.get(consoleHistoryIndex));
				setCaretPosition(getDocument().getLength());
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				if (consoleHistoryIndex < consoleHistory.size() - 1) {
					consoleHistoryIndex++;
					setText(consoleHistory.get(consoleHistoryIndex));
					setCaretPosition(getDocument().getLength());
				} else if (nowTyping != null) {
					consoleHistoryIndex = consoleHistory.size();
					setText(nowTyping);
					setCaretPosition(getDocument().getLength());
				}
			} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				if (!e.isShiftDown()) {
					if (getText().length() > 0) {
						printInputLine(getText());
						consoleHistory.add(getText());
						consoleHistoryIndex = consoleHistory.size();
						setText("");
					}
					e.consume();
				} else {
					try {
						getDocument().insertString(getCaretPosition(), "\n",
								null);
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
				}
			} else {
				consoleHistoryIndex = consoleHistory.size();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}

	private ArrayList<ConsoleEntry> consoleEntries;
	private ArrayList<String> consoleHistory;
	private String nowTyping;
	private int consoleHistoryIndex;

	/**
	 * the text pane to display the console output
	 */
	private JTextPane output;

	/**
	 * the text pane to insert input
	 */
	private InputPane input;

	private String inputString;

	private LogoDrawArea lda;

	/**
	 * initializes the window, but lets it be invisible
	 */
	public ConsolePanel() {
		output = new JTextPane();
		input = new InputPane();
		output.setEditable(false);
		output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
		input.setEditable(true);
		input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
		inputString = "";
		input.setPreferredSize(new Dimension(100, 40));
		JScrollPane sp = new JScrollPane(output);
		JScrollPane sp2 = new JScrollPane(input);

		consoleEntries = new ArrayList<ConsoleEntry>();
		consoleHistory = new ArrayList<String>();
		consoleHistoryIndex = 1;
		this.lda = null;

		this.getTop().setLayout(new BorderLayout());
		this.getTop().add(sp);
		this.getBottom().setLayout(new BorderLayout());
		// System.out.println(main.getBottom().getLayout());
		this.getBottom().add(sp2, BorderLayout.CENTER);

		// this.setContentPane(this);
		// this.setType(Type.UTILITY);
		// this.setBounds(580, 300, 250, 360);
	}

	/**
	 * prints a line of text out into the text field
	 * 
	 * @param str
	 *            the line of text
	 */
	public void printLine(String str) {
		addConsoleEntry(new ConsoleEntry(ConsoleEntry.Type.OUTPUT, str));
		if (!isVisible())
			setVisible(true);
	}

	public void print(String str) {
		printLine(str);

		// unused
		if ("A".equals("B")) {
			ConsoleEntry last = null;
			if (!consoleEntries.isEmpty())
				last = consoleEntries.get(consoleEntries.size() - 1);
			if (last != null && last.getType() == ConsoleEntry.Type.OUTPUT) {
				last.text += str;
			} else {
				printLine(str);
			}
		}
	}

	public LogoDrawArea getLogoDrawArea() {
		return lda;
	}

	public void setLogoDrawArea(LogoDrawArea lda) {
		this.lda = lda;
	}

	/**
	 * prints an error message out into the text field
	 * 
	 * @param errStr
	 *            the error message
	 */
	public void printErrLine(String errStr) {
		addConsoleEntry(new ConsoleEntry(ConsoleEntry.Type.ERROR, errStr));
		if (!isVisible())
			setVisible(true);
		setVisible(true);
	}

	private synchronized void addConsoleEntry(ConsoleEntry ce) {
		consoleEntries.add(ce);

		try {
			MutableAttributeSet as = new SimpleAttributeSet();
			StyleConstants.setForeground(as, ce.getColor());
			output.getDocument().insertString(output.getDocument().getLength(),
					ce.text + "\n", as);
			output.setCaretPosition(output.getDocument().getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void printInputLine(String str) {
		String command = getCommand(str);
		if (str.startsWith("//") || str.startsWith("\\\\"))
			str = str.substring(1);

		if (command == null) {
			addConsoleEntry(new ConsoleEntry(ConsoleEntry.Type.INPUT, str));
			inputString += str + "\n";
		} else if (command.startsWith("\\")) {
			addConsoleEntry(new ConsoleEntry(ConsoleEntry.Type.CONSOLE_COMMAND,
					command.substring(1)));
			executeConsoleCommand(command);
		} else {
			addConsoleEntry(new ConsoleEntry(ConsoleEntry.Type.COMMAND, command));
			executeCommand(command);
		}
		setVisible(true);
	}

	public void executeConsoleCommand(String command) {
		String[] cmd = command.split("\\s");
		if (cmd.length == 1
				&& (cmd[0].equalsIgnoreCase("\\clear") || cmd[0]
						.equalsIgnoreCase("\\cls"))) {
			clear();
		} else if (cmd.length == 1 && cmd[0].equalsIgnoreCase("\\stop")) {
			if (lda != null) {
				lda.stopExecution();
				lda.checkForFinish();
			}
		} else if (cmd.length == 1 && cmd[0].equalsIgnoreCase("\\execute")) {
			printErrLine("Command not yet implemented.");
		} else if (cmd.length == 1 && cmd[0].equalsIgnoreCase("\\help")) {
			printLine(helpText);
		} else {
			printErrLine("Invalid console command: \"" + cmd[0] + "\"");
		}
	}

	public void executeCommand(String command) {
		String[] cmd = command.split("\\s");
		if (cmd.length > 0) {
			boolean invokeTry = tryToInvoke(command);
			if (!invokeTry)
				printErrLine("No instance of program running; invalid command: \""
						+ cmd[0] + "\"");
		} else {
			printErrLine("Invalid console command.");
		}
	}

	private boolean tryToInvoke(final String command) {
		if (lda.getOrderInterpreter() != null) {
			Executable cmd = Compiler.compile(command, new CodeLocationInfo(
					CodeLocationInfo.Domain.CONSOLE));
			try {
				cmd.execute(lda.getRenderer(), lda.getOrderInterpreter());
				lda.repaint();
			} catch (InterpreterException ie) {
				printErrLine(ie.getMessage());
			}
			return true;
		}

		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					OrderInterpreter oi = new OrderInterpreter(lda
							.getCodeSource().getText());
					Executable cmd = Compiler.compile(command, null);
					cmd.execute(lda.getRenderer(), oi);
					lda.repaint();
				} catch (InterpreterException ie) {
					printErrLine(ie.getMessage());
				}
			}
		};

		new Thread(r).start();

		return true;
	}

	private String getCommand(String str) {
		if (str.startsWith("/") && !str.startsWith("//")) {
			String command = str.substring(1);
			return command;
		}
		if (str.startsWith("\\") && !str.startsWith("\\\\")) {
			return str;
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public String popInputString() {
		while (inputString.isEmpty()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		String temp = inputString.trim();
		inputString = "";
		return temp;
	}

	public void clear() {
		consoleEntries.clear();
		output.setText("");
	}
}
