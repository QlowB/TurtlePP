package turtlepp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Window that shows some sort of documentation of this program
 * 
 * @author Nicolas Winkler
 * 
 */
public class InfoWindow extends JFrame implements ActionListener {
	private static final long serialVersionUID = 4796069111772295406L;

	/**
	 * the package in which this class and the info file are found
	 */
	private final static String pack = "/"
			+ InfoWindow.class.getPackage().getName() + "/";

	/**
	 * the path of the file containig the html-documentation
	 */
	private static final String path = pack + "info.html";

	/**
	 * the html pane that contains the documentation
	 */
	private JTextPane info;

	/**
	 * the settings panel
	 */
	private SettingsPanel settings;

	/**
	 * initializes the window but lets it still be invisible
	 */
	public InfoWindow(CommandDialog cd) {
		super("Settings & Info");
		setBounds(200, 300, 850, 400);

		

		info = new JTextPane();
		info.setContentType("text/html");
		info.setEditable(false);
		info.setText(getHtmlText());
		// System.out.println(getHtmlText());
		info.setCaretPosition(0);

		settings = new SettingsPanel(cd);

		JScrollPane scrollInfo = new JScrollPane(info);
		
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints layoutConstraints = new GridBagConstraints();
		this.getContentPane().setLayout(layout);

		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridwidth = 1;
		layoutConstraints.gridheight = 1;
		layoutConstraints.fill = GridBagConstraints.BOTH;
		layoutConstraints.insets = new Insets(5, 5, 5, 5);
		layoutConstraints.anchor = GridBagConstraints.CENTER;
		layoutConstraints.weightx = 1.0;
		layoutConstraints.weighty = 1.0;
		layout.setConstraints(scrollInfo, layoutConstraints);
		this.getContentPane().add(scrollInfo);

		layoutConstraints = new GridBagConstraints();
		layoutConstraints.gridx = 1;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridwidth = 200;
		layoutConstraints.gridheight = 1;
		layoutConstraints.fill = GridBagConstraints.BOTH;
		layoutConstraints.insets = new Insets(5, 5, 5, 5);
		layoutConstraints.anchor = GridBagConstraints.CENTER;
		layoutConstraints.weightx = 0.0;
		layoutConstraints.weighty = 1.0;
		layout.setConstraints(settings, layoutConstraints);
		this.getContentPane().add(settings);
		// this.add(new JScrollPane(info));
	}

	/**
	 * gets the html code for the info field
	 * 
	 * @return the html code for the info field
	 */
	private static String getHtmlText() {
		return TurtlePP.getResourceAsString(path);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}
}
