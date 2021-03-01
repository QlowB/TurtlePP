package turtlepp;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;

public class CommandDialog implements ActionListener, WindowListener {

	private JFrame frmTurtlePPIde;
	JButton btnRun;
	JButton btnStop;
	CodeTextPane textPane;
	// BrainfuckInterpreter brainfuckInterpreter;
	// PipedOutputStream programInput;
	final JFileChooser fc = new JFileChooser();
	final UndoManager undoManager;

	/**
	 * this thread is either the process of running of code or <code>null</code>
	 * , if there is no execution at the time
	 */
	private Thread program;

	// DebugWindow debugWindow;
	// CodeGenerator codeGenerator;

	File currentlyOpened;

	/**
	 * the settings panel
	 */
	private SettingsPanel settings;

	private JMenu mnRun;
	private JMenu mnExamples;
	private JMenu mnEdit;

	private JMenuItem mntmLoad;
	private JMenuItem mntmSave;
	private JMenuItem mntmSaveAs;
	private JMenuItem mntmRun;
	private JMenuItem mntmUndo;
	private JMenuItem mntmRedo;
	private JMenuItem mntmStopExecution;
	private JMenuItem mntmShowDrawArea;

	private JFrame drawWindow;
	private LogoDrawArea logoDrawArea;
	private ConsolePanel console;
	private InfoWindow infoWindow;

	/**
	 * Create the application.
	 */
	public CommandDialog() {
		// brainfuckInterpreter = null;
		undoManager = new UndoManager();
		initialize();

		drawWindow = new JFrame("Turtle++ - Drawing area");
		logoDrawArea = new LogoDrawArea(textPane);
		logoDrawArea.getRenderer().setOutputPanel(getConsole());
		drawWindow.setContentPane(logoDrawArea);
		logoDrawArea.setPreferredSize(new Dimension(600, 600));
		logoDrawArea.initialize();
		drawWindow.pack();

		console.setLogoDrawArea(logoDrawArea);
		logoDrawArea.setConsole(console);

		frmTurtlePPIde.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		setFrame(new JFrame());
		getFrame().setBounds(100, 100, 800, 550);
		getFrame().addWindowListener(this);
		getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.8);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getFrame().getContentPane().add(splitPane, BorderLayout.CENTER);

		textPane = new CodeTextPane();
		textPane.setText("");

		textPane.getDocument().addUndoableEditListener(
				new UndoableEditListener() {
					UndoableEdit lastStableEdit;

					@Override
					public void undoableEditHappened(UndoableEditEvent e) {
						UndoableEdit ue = e.getEdit();
						if (ue.getPresentationName().equals("style change")) {
							if (lastStableEdit != null)
								lastStableEdit.addEdit(ue);
						} else {
							undoManager.addEdit(ue);
							// System.out.println(ue.getPresentationName());
							lastStableEdit = ue;
						}
					}
				});

		JScrollPane textScrollBar = new JScrollPane(textPane);
		splitPane.setLeftComponent(textScrollBar);

		JMenuBar menuBar = new JMenuBar();
		textScrollBar.setColumnHeaderView(menuBar);

		mnHelp = new JMenu("Turtle++");
		menuBar.add(mnHelp);

		mntmInfo = new JMenuItem("Info & Settings");
		mntmInfo.setAccelerator(KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_COMMA, getEventMask()));
		mnHelp.add(mntmInfo);
		mntmInfo.addActionListener(this);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmLoad = new JMenuItem("Open");
		mntmLoad.setAccelerator(KeyStroke.getKeyStroke(
				java.awt.event.KeyEvent.VK_O, getEventMask()));

		mntmLoad.addActionListener(this);

		mntmSave = new JMenuItem("Save");
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				getEventMask()));
		mntmSave.addActionListener(this);

		mntmSaveAs = new JMenuItem("Save As");
		mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				getEventMask() | Event.SHIFT_MASK));
		mntmSaveAs.addActionListener(this);

		mnFile.add(mntmLoad);
		mnFile.add(mntmSave);
		mnFile.add(mntmSaveAs);

		initializeExamples();
		mnFile.add(mnExamples);

		mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		mntmUndo = new JMenuItem("Undo");
		mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				getEventMask()));
		mntmUndo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (undoManager.canUndo())
						undoManager.undo();
				} catch (CannotUndoException cue) {
					cue.printStackTrace();
				}
			}
		});
		mnEdit.add(mntmUndo);

		mntmRedo = new JMenuItem("Redo");
		mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				getEventMask()));
		mntmRedo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (undoManager.canRedo())
						undoManager.redo();
				} catch (CannotRedoException cue) {
					cue.printStackTrace();
				}
			}
		});
		mnEdit.add(mntmRedo);

		mnRun = new JMenu("Run");
		menuBar.add(mnRun);

		mntmRun = new JMenuItem("Run");
		mntmRun.addActionListener(this);
		mntmRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				getEventMask()));
		mnRun.add(mntmRun);

		mntmStopExecution = new JMenuItem("Stop Execution");
		mntmStopExecution.addActionListener(this);
		mnRun.add(mntmStopExecution);

		mnWindow = new JMenu("Window");
		mntmShowDrawArea = new JMenuItem("Show Draw Window");
		mntmShowDrawArea.addActionListener(this);
		mnWindow.add(mntmShowDrawArea);
		menuBar.add(mnWindow);

		console = new ConsolePanel();
		JPanel panel = new JPanel();

		splitPane.setRightComponent(panel);
		// splitPane_1.setLeftComponent(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(console);

		JToolBar toolBar = new JToolBar();
		panel.add(toolBar);

		btnRun = new JButton("Run");
		toolBar.add(btnRun);

		btnStop = new JButton("Stop Execution");
		toolBar.add(btnStop);
		btnStop.addActionListener(this);

		btnRun.addActionListener(this);

		// output = new JTextPane();
		// output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
		// JScrollPane outputScrollPane = new JScrollPane(output);
		// new JPanel();
		// consoleArea.setLayout(new BorderLayout(0, 0));
		// consoleArea.add(outputScrollPane);
	}

	void initializeExamples() {
		mnExamples = new JMenu("Examples");
		ArrayList<ExampleWindow.Example> examples = ExampleWindow
				.initExamples();
		for (int i = 0; i < examples.size(); i++) {
			JMenuItem jmi = new JMenuItem(examples.get(i).getName());
			jmi.addActionListener(new ExampleSetter(examples.get(i)));
			mnExamples.add(jmi);
		}
	}

	class ExampleSetter implements ActionListener {
		ExampleWindow.Example example;

		ExampleSetter(ExampleWindow.Example ex) {
			this.example = ex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			currentlyOpened = null;
			textPane.setText(example.getCode());
			undoManager.die();
		}
	}

	public ConsolePanel getConsole() {
		return console;
	}

	private int getEventMask() {
		if (isMac()) {
			return Event.META_MASK;
		} else {
			return Event.CTRL_MASK;
		}
	}

	public JFrame getFrame() {
		return frmTurtlePPIde;
	}

	public void setFrame(JFrame frame) {
		this.frmTurtlePPIde = frame;
		frmTurtlePPIde.setTitle("Turtle++");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.btnRun || e.getSource() == this.mntmRun) {
			if (logoDrawArea != null) {
				if (logoDrawArea.isRunning()) {
					logoDrawArea.stopExecution();
				}
				if (program == null || !program.isAlive()) {
					logoDrawArea.setErrorHighlighting(true);
					if (!drawWindow.isVisible())
						drawWindow.setVisible(true);
					logoDrawArea.run(textPane.getText(), new Runnable() {
						@Override
						public void run() {
							// execute.setText("Run");
						}
					});
				}
			}
		} else if (e.getSource() == this.btnStop
				|| e.getSource() == mntmStopExecution) {
			logoDrawArea.stopExecution();
		} else if (e.getSource() == this.mntmLoad) {
			open();
		} else if (e.getSource() == this.mntmSave) {
			save(false);
		} else if (e.getSource() == this.mntmSaveAs) {
			save(true);
		} else if (e.getSource() == mntmInfo) {
			if (infoWindow == null) {
				infoWindow = new InfoWindow(this);
			}
			infoWindow.setVisible(true);
		} else if (e.getSource() == mntmShowDrawArea) {
			if (!drawWindow.isVisible())
				drawWindow.setVisible(true);
		}
	}

	private void open() {
		int returnVal = fc.showOpenDialog(getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			byte[] encoded = new byte[0];
			try {
				encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
				String content = new String(encoded, Charset.defaultCharset());
				textPane.setText(content);
				undoManager.die();
				currentlyOpened = file;
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(getFrame(),
						"Error loading file!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void save(boolean showDialogAnyway) {
		if (currentlyOpened == null || showDialogAnyway) {
			currentlyOpened = saveFileDialog();
		}

		if (currentlyOpened != null) {
			try {
				FileOutputStream fos = new FileOutputStream(currentlyOpened);
				String text = textPane.getText();
				fos.write(text.getBytes(Charset.defaultCharset()));
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private File saveFileDialog() {
		int returnVal = fc.showSaveDialog(getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file;
		}
		return null;
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		try {

		} catch (Exception ex) {
		}
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	public String getCode() {
		return textPane.getText();
	}

	public CodeTextPane getTextPane() {
		return (CodeTextPane) textPane;
	}

	private static String OS = System.getProperty("os.name").toLowerCase();
	private JMenu mnHelp;
	private JMenuItem mntmInfo;
	private JMenu mnWindow;

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS
				.indexOf("aix") > 0);
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}

	public CodeTextPane getCodePane() {
		return textPane;
	}

	public ExampleWindow getExampleWindow() {
		// TODO Auto-generated method stub
		return null;
	}
}
