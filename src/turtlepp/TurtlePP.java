package turtlepp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import turtlepp.reference.ReferenceWindow;

public class TurtlePP {
	/**
	 * the "code window"
	 */
	private CommandDialog commandDialog;

	/**
	 * debug use only
	 */
	public static final boolean printLogs = true;

	/**
	 * instantiates a new instance of TurtlePP
	 */
	public TurtlePP() {
		// setBounds(0, 0, 700, 700);

		setCommandDialog(new CommandDialog());
		// logoDrawArea = new LogoDrawArea(commandDialog.getCodePane());
		// logoDrawArea.getRenderer().setOutputPanel(commandDialog.getConsole());
		// this.setContentPane(logoDrawArea);

		// commandDialog.setHandler(logoDrawArea);

		// this.setResizable(false);

		// setLocationRelativeTo(getParent());
		// setVisible(true);

		// logoDrawArea.initialize();

		// this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * well... main method
	 * 
	 * @param args
	 *            yes... arguments
	 */
	public static void main(String args[]) {

		// System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name",
				"Turtle++");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		new ReferenceWindow().setVisible(true);
		TurtlePP tpp = new TurtlePP();
		tpp.getCommandDialog().getFrame().setSize(100, 100);
	}

	/**
	 * reads a string out of the jar-file
	 * 
	 * @param path
	 *            the path of the file
	 * @return the contents of the file as a string
	 */
	public static String getResourceAsString(String path) {
		try {
			InputStreamReader isr = new InputStreamReader(
					TurtlePP.class.getResourceAsStream(path));
			BufferedReader br = new BufferedReader(isr);
			StringBuilder builder = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				builder.append(line);
				builder.append(System.lineSeparator());
				line = br.readLine();
			}
			String a = builder.toString();
			return a;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	/**
	 * reads an xml document out of the jar-file
	 * 
	 * @param path
	 *            the path of the file
	 * @return the generated xml document
	 */
	public static Document getXmlSource(String path) {
		String str = getResourceAsString(path);
		DocumentBuilder db = null;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(str));
		try {
			return db.parse(is);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public CommandDialog getCommandDialog() {
		return commandDialog;
	}

	public void setCommandDialog(CommandDialog commandDialog) {
		this.commandDialog = commandDialog;
	}
}
