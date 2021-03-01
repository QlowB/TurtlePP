package turtlepp;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Window to browse examples
 * 
 * @author Nicolas Winkler
 * 
 */
public class ExampleWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 2799634127050096393L;

	/**
	 * the combo box to chose an example
	 */
	private JComboBox<String> jc;

	/**
	 * list containing the examples
	 */
	private ArrayList<Example> examples;

	/**
	 * the formatted text pane to show the code
	 */
	private CodeTextPane ctp;

	/**
	 * initializes the window, but lets it visibility to <code>false</code>
	 */
	public ExampleWindow(SyntaxHighlighter syntaxHighlihter) {
		UtilityPanel main = new UtilityPanel();
		examples = initExamples();
		String[] strings = new String[examples.size()];
		for (int i = 0; i < examples.size(); i++)
			strings[i] = examples.get(i).getName();

		jc = new JComboBox<String>(strings);
		jc.setSelectedIndex(0);
		ctp = new CodeTextPane();
		ctp.setEditable(false);

		jc.addActionListener(this);
		JScrollPane sp = new JScrollPane(ctp);

		jc.setSelectedIndex(0);

		main.getTop().setLayout(new BorderLayout());
		main.getBottom().setLayout(new BorderLayout());
		
		main.getTop().add(sp);
		main.getBottom().add(jc);

		this.setContentPane(main);
		this.setBounds(200, 240, 500, 400);
		this.setType(Type.UTILITY);
	}

	/**
	 * reads and initializes the examples
	 */
	public static ArrayList<Example> initExamples() {
		String pack = "/" + ExampleWindow.class.getPackage().getName() + "/";
		Document d = TurtlePP.getXmlSource(pack + "examples.xml");
		d.getDocumentElement().normalize();
		NodeList nodeExamples = d.getElementsByTagName("example");

		ArrayList<Example> examples = new ArrayList<Example>();

		for (int i = 0; i < nodeExamples.getLength(); i++) {
			try {
				Node example = nodeExamples.item(i);
				String name = example.getAttributes().getNamedItem("name")
						.getNodeValue();
				String code;
				if (example.getAttributes().getNamedItem("path") != null) {
					String path = pack
							+ example.getAttributes().getNamedItem("path")
									.getNodeValue();
					code = TurtlePP.getResourceAsString(path).trim();
				} else {
					code = example.getTextContent().trim();
				}

				Example ex = new Example(name, code);
				examples.add(ex);
			} catch (RuntimeException re) {
				// oops, too bad, next example
			}
		}
		return examples;
	}

	/**
	 * Contains code and information about an example
	 * 
	 * @author Nicolas Winkler
	 * 
	 */
	public static class Example {
		/**
		 * name of the example
		 */
		private String name;
		/**
		 * source code
		 */
		private String code;

		/**
		 * initializes the example
		 * 
		 * @param name
		 *            name of the example
		 * @param code
		 *            source code
		 */
		public Example(String name, String code) {
			this.name = name;
			this.code = code;
		}

		/**
		 * gets the name of this example
		 * 
		 * @return the name of this example
		 */
		public String getName() {
			return name;
		}

		/**
		 * gets the code of this example
		 * 
		 * @return the code of this example
		 */
		public String getCode() {
			return code;
		}

		@Override
		public String toString() {
			return "Example [name=" + name + ", code=" + code + "]";
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jc) {
			int index = jc.getSelectedIndex();
			if (index >= 0 && index < examples.size())
				ctp.setText(examples.get(index).getCode());
			else
				ctp.setText("");
		}
	}

	public CodeTextPane getCodeTextPane() {
		return ctp;
	}
}
