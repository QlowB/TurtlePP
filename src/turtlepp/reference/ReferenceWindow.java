package turtlepp.reference;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextPane;
import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.JTextField;

import java.awt.SystemColor;

import javax.swing.DropMode;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.MatteBorder;

import java.awt.Color;

import javax.swing.border.LineBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import turtlepp.TurtlePP;

public class ReferenceWindow extends JFrame implements HyperlinkListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3957102617372364734L;

	private JPanel contentPane;
	private JTextPane txtpnBlaayooblaa;

	/**
	 * the package in which this class and the info file are found
	 */
	private final static String pack = "/"
			+ ReferenceWindow.class.getPackage().getName().replace('.', '/')
			+ "/";

	/**
	 * the path of the file containig the html-documentation
	 */
	private static final String path = pack + "reference.html";

	/**
	 * Create the frame.
	 */
	public ReferenceWindow() {
		setTitle("Turtle++ Reference");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		txtpnBlaayooblaa = new JTextPane();

		JScrollPane jsp = new JScrollPane(txtpnBlaayooblaa);
		txtpnBlaayooblaa.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
				null));
		txtpnBlaayooblaa.setContentType("text/html");
		contentPane.add(jsp, BorderLayout.CENTER);
		txtpnBlaayooblaa.setEditable(false);
		txtpnBlaayooblaa.setText(getHtmlText());
		txtpnBlaayooblaa.addHyperlinkListener(this);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JTextArea lblThisContainsA = new JTextArea(
				"This contains a documentation about the features provided by the Turtle++ standard library.");
		lblThisContainsA.setLineWrap(true);
		lblThisContainsA.setEditable(false);
		lblThisContainsA.setBackground(SystemColor.window);
		lblThisContainsA.setBorder(null);
		panel.add(lblThisContainsA);
	}

	/**
	 * gets the html code for the info field
	 * 
	 * @return the html code for the info field
	 */
	private static String getHtmlText() {
		return TurtlePP.getResourceAsString(path);
	}

	private static String getReferenceText(String file) {
		return TurtlePP.getResourceAsString(pack + file);
	}

	public void hyperlinkUpdate(HyperlinkEvent he) {
		if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String link = he.getDescription();
			System.out.println(link);
			if (link.startsWith("datatype:")) {
				link = link.substring("datatype:".length());

				String wrapper = getReferenceText("datatypeWrapper.html");
				String text = getReferenceText(link + ".txt");
				
				wrapper = wrapper.replaceAll("<typeName/>", link);
				wrapper = wrapper.replaceAll("<typeDesc/>", text);
				
				System.out.println(wrapper);
				txtpnBlaayooblaa.setText(wrapper);
			}
		}
	}
}
