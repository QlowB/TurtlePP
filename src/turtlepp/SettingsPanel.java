package turtlepp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * 
 * @author Nicolas Winkler
 * 
 */
public class SettingsPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -3182492527819253735L;

	/**
	 * the
	 */
	private CommandDialog cd;

	private JButton chooseColor;
	private JComboBox<String> syntaxChoose;

	public SettingsPanel(CommandDialog cd) {
		super(new GridLayout(0, 1));
		this.cd = cd;

		TitledBorder lineBorder = BorderFactory
				.createTitledBorder("Syntax Coloring");
		this.setBorder(lineBorder);
		chooseColor = new JButton("Choose Color");
		syntaxChoose = new JComboBox<String>(new String[] { "Normal Text",
				"Keywords", "Numbers", "Constants", "Native Functions",
				"Strings", "Callback Functions", "Comments" });

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints layoutConstraints = new GridBagConstraints();
		this.setLayout(layout);

		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 0;
		layoutConstraints.gridwidth = 1;
		layoutConstraints.gridheight = 1;
		layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		layoutConstraints.insets = new Insets(5, 5, 5, 5);
		layoutConstraints.anchor = GridBagConstraints.NORTH;
		layoutConstraints.weightx = 1.0;
		layoutConstraints.weighty = 0.0;
		layout.setConstraints(syntaxChoose, layoutConstraints);
		this.add(syntaxChoose);

		layoutConstraints = new GridBagConstraints();
		layoutConstraints.gridx = 0;
		layoutConstraints.gridy = 1;
		layoutConstraints.gridwidth = 1;
		layoutConstraints.gridheight = 1;
		layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
		layoutConstraints.insets = new Insets(5, 5, 5, 5);
		layoutConstraints.anchor = GridBagConstraints.NORTH;
		layoutConstraints.weightx = 1.0;
		layoutConstraints.weighty = 0.0;
		layout.setConstraints(chooseColor, layoutConstraints);
		this.add(chooseColor);
		

		chooseColor.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.chooseColor) {
			SyntaxHighlighter synt = cd.getCodePane()
					.getSyntaxHighlighter();
			java.awt.Color before = null;

			if (syntaxChoose.getSelectedItem().equals("Normal Text")) {
				before = SyntaxHighlighter.getNormalTextColor();
			} else if (syntaxChoose.getSelectedItem().equals("Keywords")) {
				before = SyntaxHighlighter.getKeywordColor();
			} else if (syntaxChoose.getSelectedItem().equals("Numbers")) {
				before = SyntaxHighlighter.getNumbersColor();
			} else if (syntaxChoose.getSelectedItem().equals("Constants")) {
				before = SyntaxHighlighter.getConstantsColor();
			} else if (syntaxChoose.getSelectedItem()
					.equals("Native Functions")) {
				before = SyntaxHighlighter.getNativeFunctionsColor();
			} else if (syntaxChoose.getSelectedItem().equals("Strings")) {
				before = SyntaxHighlighter.getStringsColor();
			} else if (syntaxChoose.getSelectedItem().equals(
					"Callback Functions")) {
				before = SyntaxHighlighter.getCallbackFunctionsColor();
			} else if (syntaxChoose.getSelectedItem().equals("Comments")) {
				before = SyntaxHighlighter.getCommentsColor();
			}

			java.awt.Color c = JColorChooser.showDialog(this,
					"Pick a color", before);

			if (syntaxChoose.getSelectedItem().equals("Normal Text")) {
				SyntaxHighlighter.setNormalTextColor(c);
			} else if (syntaxChoose.getSelectedItem().equals("Keywords")) {
				SyntaxHighlighter.setKewordColor(c);
			} else if (syntaxChoose.getSelectedItem().equals("Numbers")) {
				SyntaxHighlighter.setNumbersColor(c);
			} else if (syntaxChoose.getSelectedItem().equals("Constants")) {
				SyntaxHighlighter.setConstantsColor(c);
			} else if (syntaxChoose.getSelectedItem()
					.equals("Native Functions")) {
				SyntaxHighlighter.setNativeFunctionsColor(c);
			} else if (syntaxChoose.getSelectedItem().equals("Strings")) {
				SyntaxHighlighter.setStringsColor(c);
			} else if (syntaxChoose.getSelectedItem().equals(
					"Callback Functions")) {
				SyntaxHighlighter.setCallbackFunctionsColor(c);
			} else if (syntaxChoose.getSelectedItem().equals("Comments")) {
				SyntaxHighlighter.setCommentsColor(c);
			}

			synt.reformat();
			cd.getExampleWindow().getCodeTextPane().getSyntaxHighlighter().reformat();
		}
	}
}
