import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultHighlighter;

// 
// Decompiled by Procyon v0.5.30
// 

public class GUI implements ActionListener, TableModelListener {
	private final Machine mac;
	public static String LOOKANDFEEL;
	private final JFrame frame;
	private final JFileChooser fileChooser;
	private final JMenuBar menuBar;
	private final JMenu fileMenu;
	private final JMenu aboutMenu;
	private final JMenuItem openItem;
	private final JMenuItem quitItem;
	private final JMenuItem commandItem;
	private final JMenuItem versionItem;
	private final String openActionCommand = "Open";
	private final String quitActionCommand = "Quit";
	private final String openCOWActionCommand = "OutputWindow";
	private final String versionActionCommand = "Version";
	private final JPanel leftPanel;
	private final JPanel controlPanel;
	private final JButton nextButton;
	private final String nextButtonCommand = "Next";
	private final JButton stepButton;
	private final String stepButtonCommand = "Step";
	private final JButton continueButton;
	private final String continueButtonCommand = "Continue";
	private final JButton finishButton;
	private final String finishButtonCommand = "Finish";
	private final JButton stopButton;
	private final String stopButtonCommand = "Stop";
	private final String statusLabelRunning = "    Running ";
	private final String statusLabelSuspended = "Suspended ";
	private final String statusLabelHalted = "       Halted ";
	private final JLabel statusLabel;
	private final Color runningColor;
	private final Color suspendedColor;
	private final Color haltedColor;
	private final JTable regTable;
	private final CommandLinePanel commandPanel;
	private final CommandOutputWindow commandOutputWindow;

	private final JPanel memoryDumpAndGoToPanel;
	private JTextField gotoDumpText;
	private final JButton gotoDumpButton;

	private final JPanel memoryAndGoToPanel;
	private JTextField gotoText;
	private final JButton gotoButton;

	private final JPanel dumpAndBreakpointsPanel;
	private final JPanel breakpointsPanel;
	private final JPanel memoryDumpPanel;
	private final JPanel stackPanel;
	private final JPanel breakpointsAndStackPanel;

	private final JPanel memoryPanel;
	private final JTable memTable;
	private final JScrollPane memScrollPane;
	public static final Color BreakPointColor;
	public static final Color PCColor;
	private final JPanel devicePanel;
	private final JPanel registerPanel;
	private final TextConsolePanel ioPanel;
	private final VideoConsole video;
	private final JPanel srcPanel;
	private final JTextArea srcText;
	private final JScrollPane srcScrollPane;
	private final DataPath dataPath;

	private final JPanel dataPathPanel;
	private final JFrame dataPathFrame;
	private final JButton dataPathButton;
	public ControlSignals signals;

	private JTextArea breakpointsText;
	private JTextArea stackText;
	private JTable memoryDumpTable;
	private JScrollPane memoryDumpScrollPane;

	private final JToggleButton toggleUpdateButton;
	private final JToggleButton toggleColorButton;

	private final JFrame opcodeFrame;
	private final JPanel opcodePanel;
	private JTextField opcodeText;
	private boolean addedOpcodeListener = false;

	private JButton aboutButton;
	
	private LC4Graph lc4Graph;
	private final JButton lc4Button;

	private void setupDataPathPanel() {
		this.dataPathPanel.setLayout(new BoxLayout(this.dataPathPanel, 2));
		this.dataPathPanel.add(this.dataPath);
		this.dataPathPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Datapath"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		this.dataPathPanel.setVisible(true);
	}
	
	private void generateLc4Graph() {
		lc4Graph = new LC4Graph();
		lc4Graph.init(this.mac);
        JFrame frame = new JFrame();
        frame.getContentPane().add(lc4Graph);
        frame.setTitle("JGraphT Adapter to JGraph Demo");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
	}

	public void loadSourceFile(final String s) {
		this.srcText.setText(null);
		try {
			String line;
			while ((line = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(s))))
					.readLine()) != null) {
				this.srcText.append(line + "\n");
			}
		} catch (Exception ex) {
			System.err.println("Error reading source file '" + s + "'");
		}
	}

	public void gotoSourceLine(final int n, final int n2) {
		final DefaultHighlighter highlighter = new DefaultHighlighter();
		this.srcText.setHighlighter(highlighter);
		if (n != -1) {
			try {
				highlighter.addHighlight(this.srcText.getLineStartOffset(n - 1), this.srcText.getLineEndOffset(n - 1),
						new DefaultHighlighter.DefaultHighlightPainter(Color.WHITE));
			} catch (Exception ex) {
				System.out.println("Bad line " + n);
				ex.printStackTrace();
			}
		}
		try {
			highlighter.addHighlight(this.srcText.getLineStartOffset(n2 - 1), this.srcText.getLineEndOffset(n2 - 1),
					new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
			final JScrollBar verticalScrollBar = this.srcScrollPane.getVerticalScrollBar();
			verticalScrollBar.setValue((n2 - 1) * (verticalScrollBar.getMaximum() / this.srcText.getLineCount()));
		} catch (Exception ex2) {
			System.out.println("Bad line " + n);
			ex2.printStackTrace();
		}
	}

	private void setupSrcPanel() {
		this.srcPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Source"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		this.srcPanel.add(this.srcScrollPane);
		this.srcScrollPane.setVerticalScrollBarPolicy(22);
		this.srcScrollPane.setHorizontalScrollBarPolicy(32);
		this.srcScrollPane.setPreferredSize(new Dimension(400, 480));
		this.srcText.setEditable(false);
		this.srcPanel.setVisible(true);
	}

	private void setupMemoryPanel() {
		JLabel myLabel = new JLabel("Line Number:");

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(myLabel, BorderLayout.WEST);
		panel.add(gotoText, BorderLayout.CENTER);
		panel.add(gotoButton, BorderLayout.EAST);
		this.gotoButton.setActionCommand("goto");
		this.gotoButton.addActionListener(this);
		panel.setPreferredSize(new Dimension(220, 30));
		this.memoryAndGoToPanel.add(panel, BorderLayout.NORTH);
		this.memoryAndGoToPanel.add(memoryPanel, BorderLayout.CENTER);
		this.memoryPanel.add(this.memScrollPane);
		this.memoryPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Memory"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		this.memTable.getModel().addTableModelListener(this);
		this.memTable.getModel().addTableModelListener((TableModelListener) this.memScrollPane.getVerticalScrollBar());
		this.memTable.setPreferredScrollableViewportSize(new Dimension(220, 480));
		if (!PennSim.isDoubleBufferedVideo()) {
			this.memTable.getModel().addTableModelListener(this.video);
		}
	}

	public JPanel getControlPanel() {
		return this.controlPanel;
	}

	private void setupControlPanel() {
		this.controlPanel.setLayout(new BoxLayout(this.controlPanel, 1));
		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 7));
		this.nextButton.setActionCommand("Next");
		this.nextButton.addActionListener(this);
		panel.add(this.nextButton);
		this.stepButton.setActionCommand("Step");
		this.stepButton.addActionListener(this);
		panel.add(this.stepButton);
		this.continueButton.setActionCommand("Continue");
		this.continueButton.addActionListener(this);
		panel.add(this.continueButton);
		this.finishButton.setActionCommand("Finish");
		this.finishButton.addActionListener(this);
		panel.add(this.finishButton);
		this.stopButton.setActionCommand("Stop");
		this.stopButton.addActionListener(this);
		panel.add(this.stopButton);
		this.setStatusLabelSuspended();

		this.dataPathButton.setActionCommand("DataPath");
		this.dataPathButton.addActionListener(this);
		panel.add(this.dataPathButton);

		aboutButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				JOptionPane.showMessageDialog(frame,
						"PennSim edited by Henry Zhu ~ 10/31/16. \nIf any issues come up, feel free to post on piazza or email henryzhu@seas.upenn.edu :)",
						"About | Report", JOptionPane.INFORMATION_MESSAGE, null);
				;
			}
		});

		panel.add(this.aboutButton);

		panel.add(this.statusLabel);

		this.controlPanel.add(panel);
		this.controlPanel.add(this.commandPanel);
		this.controlPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Controls"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		this.controlPanel.setVisible(true);
	}

	private void setupRegisterPanel() {
		this.registerPanel.add(this.regTable);
		this.registerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Registers"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		this.registerPanel.setVisible(true);
	}

	private void setupDevicePanel() {
		this.devicePanel.setLayout(new BoxLayout(this.devicePanel, 1));
		this.devicePanel.add(this.video);
		this.devicePanel.add(this.ioPanel);
		this.devicePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Devices"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		this.devicePanel.setVisible(true);
	}

	private void setupInfoPanel() {
		this.toggleUpdateButton.setActionCommand("toggle update");
		this.toggleUpdateButton.addActionListener(this);
		this.toggleUpdateButton.setSelected(false);
		JPanel togglePanel = new JPanel(new BorderLayout());
		togglePanel.add(toggleUpdateButton, "North");

		this.toggleColorButton.setActionCommand("toggle color");
		this.toggleColorButton.addActionListener(this);
		this.toggleColorButton.setSelected(false);
		togglePanel.add(toggleColorButton, "South");
		this.breakpointsAndStackPanel.add(togglePanel, "South");

		final JSplitPane splitPane = new JSplitPane(1, true, this.dumpAndBreakpointsPanel, this.stackPanel);
		this.breakpointsAndStackPanel.add(splitPane, "Center");

		this.dumpAndBreakpointsPanel.add(this.breakpointsPanel, "North");
		this.dumpAndBreakpointsPanel.add(this.memoryDumpAndGoToPanel, "South");
		this.dumpAndBreakpointsPanel.setPreferredSize(new Dimension(300, 550));
		
		JLabel myLabel = new JLabel("Line Number:");

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(myLabel, BorderLayout.WEST);
		panel.add(gotoDumpText, BorderLayout.CENTER);
		panel.add(gotoDumpButton, BorderLayout.EAST);
		this.gotoDumpButton.setActionCommand("gotodump");
		this.gotoDumpButton.addActionListener(this);
		
		panel.setPreferredSize(new Dimension(220, 30));
		this.memoryDumpAndGoToPanel.add(panel, BorderLayout.NORTH);
		this.memoryDumpAndGoToPanel.add(memoryDumpPanel, BorderLayout.CENTER);

		this.memoryDumpPanel.add(this.memoryDumpScrollPane);
		this.memoryDumpPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Memory Dump (For looking at data)"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		this.memoryDumpTable.getModel().addTableModelListener(this);
		this.memoryDumpTable.getModel()
				.addTableModelListener((TableModelListener) this.memoryDumpScrollPane.getVerticalScrollBar());
		this.memoryDumpTable.setPreferredScrollableViewportSize(new Dimension(220, 200));

		this.memoryDumpTable.scrollRectToVisible(this.memoryDumpTable.getCellRect(16384, 0, true));

		this.breakpointsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Breakpoints"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		JScrollPane breakpointsScrollPane = new JScrollPane(this.breakpointsText);
		this.breakpointsPanel.add(breakpointsScrollPane);
		breakpointsScrollPane.setVerticalScrollBarPolicy(22);
		breakpointsScrollPane.setHorizontalScrollBarPolicy(32);
		breakpointsScrollPane.setPreferredSize(new Dimension(225, 225));
		this.breakpointsText.setEditable(false);
		breakpointsScrollPane.setVisible(true);

		this.stackPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Stack"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		JScrollPane stackScrollPane = new JScrollPane(this.stackText);
		this.stackPanel.add(stackScrollPane, "North");
		stackScrollPane.setVerticalScrollBarPolicy(22);
		stackScrollPane.setHorizontalScrollBarPolicy(32);
		stackScrollPane.setPreferredSize(new Dimension(225, 480));
		this.stackText.setEditable(false);
		stackScrollPane.setVisible(true);
		
		stackPanel.add(lc4Button, "South");
		this.lc4Button.setActionCommand("generate graph");
		this.lc4Button.addActionListener(this);
		
	}

	public GUI(final Machine mac, final CommandLine commandLine) {
		this.frame = new JFrame("PennSim - " + PennSim.version + " - " + PennSim.getISA() + " | edited by Henry Zhu");
		this.dataPathFrame = new JFrame("PennSim - " + PennSim.version + " - " + PennSim.getISA());
		this.fileChooser = new JFileChooser(".");
		this.menuBar = new JMenuBar();
		this.fileMenu = new JMenu("File");
		this.aboutMenu = new JMenu("About");
		this.openItem = new JMenuItem("Open .obj File");
		this.quitItem = new JMenuItem("Quit");
		this.commandItem = new JMenuItem("Open Command Output Window");
		this.versionItem = new JMenuItem("Simulator Version");
		this.leftPanel = new JPanel();
		this.controlPanel = new JPanel();
		this.nextButton = new JButton("Next");
		this.stepButton = new JButton("Step");
		this.continueButton = new JButton("Continue");
		this.finishButton = new JButton("Finish");
		this.stopButton = new JButton("Stop");
		
		this.memoryAndGoToPanel = new JPanel(new BorderLayout());
		this.gotoText = new JTextField();
		this.gotoButton = new JButton("Go to line");

		this.memoryDumpAndGoToPanel = new JPanel(new BorderLayout());
		this.gotoDumpText = new JTextField();
		this.gotoDumpButton = new JButton("Go to line");

		this.dataPathButton = new JButton("Open DataPath Chart");

		this.lc4Button = new JButton("Generate Flow Chart/Graph/How your code looks like");
		
		this.aboutButton = new JButton("About/Report Issue");

		this.breakpointsAndStackPanel = new JPanel(new BorderLayout());
		this.dumpAndBreakpointsPanel = new JPanel(new BorderLayout());
		this.memoryDumpPanel = new JPanel(new BorderLayout());
		this.breakpointsPanel = new JPanel(new BorderLayout());
		this.stackPanel = new JPanel(new BorderLayout());
		this.breakpointsText = new JTextArea();
		this.stackText = new JTextArea();
		this.toggleUpdateButton = new JToggleButton("Additional Information Output Disabled");
		this.toggleColorButton = new JToggleButton("Christmas Syntax Highlighting Disabled");

		this.opcodeText = new JTextField();

		this.opcodePanel = new JPanel(new BorderLayout());
		this.opcodePanel.add(opcodeText);

		this.opcodeFrame = new JFrame() {
			public @Override void toFront() {
				int sta = super.getExtendedState() & ~JFrame.ICONIFIED & JFrame.NORMAL;

				super.setExtendedState(sta);
				super.setAlwaysOnTop(true);
				super.toFront();
				super.requestFocus();
				super.setAlwaysOnTop(false);
			}
		};
		this.opcodeFrame.getContentPane().setLayout(new BorderLayout());
		this.opcodeFrame.getContentPane().add(this.opcodePanel, "Center");
		this.opcodeFrame.setSize(new Dimension(400, 50));
		this.opcodeFrame.setDefaultCloseOperation(3);
		this.opcodeFrame.setVisible(false);
		this.opcodeFrame.setResizable(true);
		this.opcodeFrame.setLocationRelativeTo(this.frame);
		this.opcodeFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		this.statusLabel = new JLabel("");
		this.runningColor = new Color(43, 129, 51);
		this.suspendedColor = new Color(209, 205, 93);
		this.haltedColor = new Color(161, 37, 40);
		this.memoryPanel = new JPanel(new BorderLayout());
		this.devicePanel = new JPanel();
		this.registerPanel = new JPanel();
		this.dataPathPanel = new JPanel();
		this.srcPanel = new JPanel(new BorderLayout());
		this.srcText = new JTextArea();
		this.srcScrollPane = new JScrollPane(this.srcText);
		this.mac = mac;
		this.regTable = new JTable(mac.getRegisterFile());
		final TableColumn column = this.regTable.getColumnModel().getColumn(0);
		column.setMaxWidth(30);
		column.setMinWidth(30);
		final TableColumn column2 = this.regTable.getColumnModel().getColumn(2);
		column2.setMaxWidth(30);
		column2.setMinWidth(30);
		this.memTable = new JTable(mac.getMemory()) {
			@Override
			public Component prepareRenderer(final TableCellRenderer tableCellRenderer, final int n, final int n2) {
				if (!addedOpcodeListener) {
					this.addMouseListener(new MouseAdapter() {
						public void mouseEntered(MouseEvent evt) {
							if (!opcodeFrame.isVisible()) {
								opcodeFrame.setVisible(true);
								opcodeFrame.setLocation(evt.getLocationOnScreen());
								opcodeFrame.toFront();
								opcodeFrame.requestFocus();
								opcodeFrame.repaint();
								Word word = null;
								if(memTable.rowAtPoint(evt.getPoint()) != -1)
									if(GUI.this.mac.getMemory().getWord(memTable.rowAtPoint(evt.getPoint())) != null)
										word = GUI.this.mac.getMemory().getWord(memTable.rowAtPoint(evt.getPoint()));
								if (word != null)
									if (ISA.getInstruction(word) != null)
										opcodeText.setText("Instruction: " + ISA.disassemble(word, 0, mac)
												+ " | Bits " + word.toBinary());
							}
						}

						public void mouseExited(MouseEvent evt) {
							if (opcodeFrame.isVisible())
								opcodeFrame.setVisible(false);
						}

						public void mousePressed(MouseEvent evt) {
							// System.out.println("PRESSED");
						}

						public void mouseReleased(MouseEvent evt) {
							// System.out.println("RELEASED");
						}
					});
					addedOpcodeListener = true;
				}
				final Component prepareRenderer = super.prepareRenderer(tableCellRenderer, n, n2);
				if (n2 == 0) {
					final JCheckBox checkBox = new JCheckBox();
					if (n < 65024) {
						if (GUI.this.mac.getMemory().isBreakPointSet(n)) {
							checkBox.setSelected(true);
							checkBox.setBackground(GUI.BreakPointColor);
							checkBox.setForeground(GUI.BreakPointColor);
						} else {
							checkBox.setSelected(false);
							checkBox.setBackground(this.getBackground());
						}
					} else {
						checkBox.setEnabled(false);
						checkBox.setBackground(Color.lightGray);
					}
					return checkBox;
				}
				if (n == GUI.this.mac.getRegisterFile().getPC()) {
					prepareRenderer.setBackground(GUI.PCColor);
				} else if (GUI.this.mac.getMemory().isBreakPointSet(n)) {
					prepareRenderer.setBackground(GUI.BreakPointColor);
				} else {
					prepareRenderer.setBackground(this.getBackground());
					Word word = GUI.this.mac.getMemory().getWord(n);
					if (word != null && toggleColorButton.isSelected()) {
						try {
							if (ISA.getInstruction(word).isPseudo()) {
								prepareRenderer.setBackground(Color.GRAY);
							} else if (ISA.getInstruction(word).isData()) {
								prepareRenderer.setBackground(Color.GREEN);
							} else if (ISA.getInstruction(word).isDirective()) {
								prepareRenderer.setBackground(Color.CYAN);
							} else if (ISA.getInstruction(word).isCall()) {
								prepareRenderer.setBackground(Color.CYAN);
							} else if (ISA.getInstruction(word).isBranch()) {
								prepareRenderer.setBackground(new Color(255, 255, 140, 155));
							} else if (ISA.getInstruction(word).isLoad()) {
								prepareRenderer.setBackground(new Color(102, 255, 102, 155));
							} else if (ISA.getInstruction(word).isStore()) {
								prepareRenderer.setBackground(new Color(178, 141, 255, 255));
							}
						} catch (Exception ex) {
							// nothing
						}
					}
				}
				return prepareRenderer;
			}

			@Override
			public void tableChanged(final TableModelEvent tableModelEvent) {
				if (mac != null) {
					super.tableChanged(tableModelEvent);
				}
			}
		};

		// dump stuff
		memoryDumpTable = new JTable(mac.getMemory()) {
			@Override
			public Component prepareRenderer(final TableCellRenderer tableCellRenderer, final int n, final int n2) {
				final Component prepareRenderer = super.prepareRenderer(tableCellRenderer, n, n2);
				if (n2 == 0) {
					final JCheckBox checkBox = new JCheckBox();
					if (n < 65024) {
						if (GUI.this.mac.getMemory().isBreakPointSet(n)) {
							checkBox.setSelected(true);
							checkBox.setBackground(GUI.BreakPointColor);
							checkBox.setForeground(GUI.BreakPointColor);
						} else {
							checkBox.setSelected(false);
							checkBox.setBackground(this.getBackground());
						}
					} else {
						checkBox.setEnabled(false);
						checkBox.setBackground(Color.lightGray);
					}
					return checkBox;
				}
				if (n == GUI.this.mac.getRegisterFile().getPC()) {
					prepareRenderer.setBackground(GUI.PCColor);
				} else if (GUI.this.mac.getMemory().isBreakPointSet(n)) {
					prepareRenderer.setBackground(GUI.BreakPointColor);
				} else {
					prepareRenderer.setBackground(this.getBackground());
					// Word word = GUI.this.mac.getMemory().getWord(n);
					// if(word != null && toggleColorButton.isSelected()) {
					// try {
					// if(ISA.getInstruction(word).isPseudo()) {
					// prepareRenderer.setBackground(Color.GRAY);
					// } else if(ISA.getInstruction(word).isData()) {
					// prepareRenderer.setBackground(Color.GREEN);
					// } else if(ISA.getInstruction(word).isDirective()) {
					// prepareRenderer.setBackground(Color.CYAN);
					// } else if(ISA.getInstruction(word).isCall()) {
					// prepareRenderer.setBackground(Color.CYAN);
					// } else if(ISA.getInstruction(word).isBranch()) {
					// prepareRenderer.setBackground(Color.YELLOW);
					// } else if(ISA.getInstruction(word).isLoad()) {
					// prepareRenderer.setBackground(new Color(102, 255, 102,
					// 155));
					// } else if(ISA.getInstruction(word).isStore()) {
					// prepareRenderer.setBackground(Color.MAGENTA);
					// }
					// } catch (Exception ex) {
					// //nothing
					// }
					// }
				}
				return prepareRenderer;
			}

			@Override
			public Object getValueAt(final int n, final int n2) {
				Object o = null;
				switch (n2) {
				case 0: {
					o = new Boolean(mac.getMemory().isBreakPointSet(n));
					break;
				}
				case 1: {
					final String lookupAddr = mac.getSymTable().lookupAddr(n);
					o = ((lookupAddr != null) ? lookupAddr : Word.toHex(n));
					break;
				}
				case 2: {
					if (n < 65024) {
						o = mac.getMemory().getWord(n).toHex();
						break;
					}
					o = "???";
					break;
				}
				}
				return o;
			}

			@Override
			public void tableChanged(final TableModelEvent tableModelEvent) {
				if (mac != null) {
					super.tableChanged(tableModelEvent);
				}
			}

			@Override
			public boolean isCellEditable(final int n, final int n2) {
				return (n2 == 2);
			}

		};

		this.memoryDumpScrollPane = new JScrollPane(this.memoryDumpTable) {
			@Override
			public JScrollBar createVerticalScrollBar() {
				return new HighlightScrollBar(mac);
			}
		};

		this.memoryDumpScrollPane.getVerticalScrollBar()
				.setBlockIncrement(this.memoryDumpTable.getModel().getRowCount() / 512);
		this.memoryDumpScrollPane.getVerticalScrollBar().setUnitIncrement(1);

		this.memScrollPane = new JScrollPane(this.memTable) {
			@Override
			public JScrollBar createVerticalScrollBar() {
				return new HighlightScrollBar(mac);
			}
		};
		this.memScrollPane.getVerticalScrollBar().setBlockIncrement(this.memTable.getModel().getRowCount() / 512);
		this.memScrollPane.getVerticalScrollBar().setUnitIncrement(1);
		final TableColumn column3 = this.memTable.getColumnModel().getColumn(0);
		column3.setMaxWidth(20);
		column3.setMinWidth(20);
		column3.setCellEditor(new DefaultCellEditor(new JCheckBox()));
		final TableColumn column4 = this.memTable.getColumnModel().getColumn(1);
		column4.setMinWidth(100);
		column4.setPreferredWidth(100);
		column4.setMaxWidth(1000);
		final TableColumn column5 = this.memTable.getColumnModel().getColumn(2);
		column5.setMinWidth(100);
		column5.setPreferredWidth(100);
		column5.setMaxWidth(1000);
		this.commandPanel = new CommandLinePanel(mac, commandLine);
		(this.commandOutputWindow = new CommandOutputWindow("Command Output")).addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(final WindowEvent windowEvent) {
			}

			@Override
			public void windowClosed(final WindowEvent windowEvent) {
			}

			@Override
			public void windowClosing(final WindowEvent windowEvent) {
				GUI.this.commandOutputWindow.setVisible(false);
			}

			@Override
			public void windowDeactivated(final WindowEvent windowEvent) {
			}

			@Override
			public void windowDeiconified(final WindowEvent windowEvent) {
			}

			@Override
			public void windowIconified(final WindowEvent windowEvent) {
			}

			@Override
			public void windowOpened(final WindowEvent windowEvent) {
			}
		});
		this.commandOutputWindow.setSize(700, 600);
		Console.registerConsole(this.commandPanel);
		Console.registerConsole(this.commandOutputWindow);
		(this.ioPanel = new TextConsolePanel(mac.getMemory().getKeyBoardDevice(), mac.getMemory().getMonitorDevice()))
				.setMinimumSize(new Dimension(256, 85));
		this.video = new VideoConsole(mac);
		this.dataPath = new DataPath(mac, dataPathFrame);
		this.commandPanel.setGUI(this);
	}

	public void setUpGUI() {
		initLookAndFeel();
		JFrame.setDefaultLookAndFeelDecorated(true);
		this.mac.setStoppedListener(this.commandPanel);
		this.fileChooser.setFileSelectionMode(2);
		this.fileChooser.addChoosableFileFilter(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				if (file.isDirectory()) {
					return true;
				}
				final String name = file.getName();
				return name != null && name.toLowerCase().endsWith(".obj");
			}

			@Override
			public String getDescription() {
				return "*.obj";
			}
		});
		this.openItem.setActionCommand("Open");
		this.openItem.addActionListener(this);
		this.fileMenu.add(this.openItem);
		this.commandItem.setActionCommand("OutputWindow");
		this.commandItem.addActionListener(this);
		this.fileMenu.add(this.commandItem);
		this.fileMenu.addSeparator();
		this.quitItem.setActionCommand("Quit");
		this.quitItem.addActionListener(this);
		this.fileMenu.add(this.quitItem);
		this.versionItem.setActionCommand("Version");
		this.versionItem.addActionListener(this);
		this.aboutMenu.add(this.versionItem);
		this.menuBar.add(this.fileMenu);
		this.menuBar.add(this.aboutMenu);
		this.frame.setJMenuBar(this.menuBar);
		this.setupControlPanel();
		this.setupDevicePanel();
		this.setupDataPathPanel();
		this.setupMemoryPanel();
		this.setupRegisterPanel();

		this.setupInfoPanel();
		// this.setupSrcPanel();

		this.regTable.getModel().addTableModelListener(this);
		this.frame.getContentPane().setLayout(new BorderLayout());
		this.frame.getContentPane().add(this.controlPanel, "North");
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		this.frame.getContentPane().add(panel, "Center");
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new BoxLayout(panel2, 1));
		panel2.add(this.registerPanel);
		panel2.add(this.devicePanel);
		this.dataPathFrame.getContentPane().setLayout(new BorderLayout());
		this.dataPathFrame.getContentPane().add(this.dataPathPanel, "Center");
		this.dataPathFrame.setSize(new Dimension(700, 725));
		this.dataPathFrame.setDefaultCloseOperation(3);
		this.dataPathFrame.pack();
		this.dataPathFrame.setVisible(true);
		this.dataPathFrame.setResizable(true);
		this.dataPathFrame.setLocationRelativeTo(this.frame);
		this.dataPathFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.dataPathFrame.setVisible(false);
		panel.add(panel2, "West");
		final JSplitPane splitPane = new JSplitPane(1, true, this.memoryAndGoToPanel, this.breakpointsAndStackPanel);
		splitPane.resetToPreferredSizes();
		panel.add(splitPane, "Center");
		this.frame.setSize(new Dimension(700, 725));
		this.frame.setDefaultCloseOperation(3);
		this.frame.pack();
		this.frame.setVisible(true);
		this.scrollToPC();
		this.commandPanel.actionPerformed(null);
	}

	public void scrollToIndex(final int n) {
		this.memTable.scrollRectToVisible(this.memTable.getCellRect(n, 0, true));
	}

	public void scrollToPC() {
		this.scrollToPC(0);
	}

	public void scrollToPC(final int n) {
		this.memTable.scrollRectToVisible(this.memTable.getCellRect(this.mac.getRegisterFile().getPC() + n, 0, true));
	}

	@Override
	public void tableChanged(final TableModelEvent tableModelEvent) {
		if (!this.mac.isContinueMode()) {
		}
	}

	public void confirmExit() {
		final Object[] array = { "Yes", "No" };
		if (JOptionPane.showOptionDialog(this.frame, "Are you sure you want to quit?", "Quit verification", 0, 3, null,
				array, array[1]) == 0) {
			this.mac.cleanup();
			System.exit(0);
		}
	}

	@Override
	public void actionPerformed(final ActionEvent actionEvent) {
		try {
			try {
				this.scrollToIndex(Integer.parseInt(actionEvent.getActionCommand()));
			} catch (NumberFormatException ex2) {
				if ("Next".equals(actionEvent.getActionCommand())) {
					this.mac.executeNext();
				} else if ("Step".equals(actionEvent.getActionCommand())) {
					this.mac.executeStep();
				} else if ("Continue".equals(actionEvent.getActionCommand())) {
					this.mac.executeMany();
				} else if ("Finish".equals(actionEvent.getActionCommand())) {
					this.mac.executeFin();
				} else if ("Quit".equals(actionEvent.getActionCommand())) {
					this.confirmExit();
				} else if ("Stop".equals(actionEvent.getActionCommand())) {
					Console.println(this.mac.stopExecution(true));
				} else if ("OutputWindow".equals(actionEvent.getActionCommand())) {
					this.commandOutputWindow.setVisible(true);
				} else if ("Version".equals(actionEvent.getActionCommand())) {
					JOptionPane.showMessageDialog(this.frame, PennSim.getVersion(), "Version", 1);
				} else if ("DataPath".equals(actionEvent.getActionCommand())) {
					if (!this.dataPathFrame.isVisible())
						this.dataPathFrame.setVisible(true);
					else
						this.dataPathFrame.setVisible(false);
				} else if ("goto".equals(actionEvent.getActionCommand())) {
					this.scrollToIndex(Word.parseNum(gotoText.getText()));
				} else if ("gotodump".equals(actionEvent.getActionCommand())) {
					this.memoryDumpTable.scrollRectToVisible(
							this.memoryDumpTable.getCellRect(Word.parseNum(gotoDumpText.getText()), 0, true));
				} else if ("toggle update".equals(actionEvent.getActionCommand())) {
					if (toggleUpdateButton.isSelected()) {
						toggleUpdateButton.setText("Additional Information Output Enabled");
					} else {
						toggleUpdateButton.setText("Additional Information Output Disabled");
					}
				} else if ("toggle color".equals(actionEvent.getActionCommand())) {
					if (toggleColorButton.isSelected()) {
						toggleColorButton.setText("Christmas Syntax Highlighting Enabled");
					} else {
						toggleColorButton.setText("Christmas Syntax Highlighting Disabled");
					}
				} else if ("generate graph".equals(actionEvent.getActionCommand())) {
					this.generateLc4Graph();
				}
			}
		} catch (ExceptionException ex) {
			ex.showMessageDialog(this.frame);
		}
	}

	public static void initLookAndFeel() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		if (GUI.LOOKANDFEEL != null) {
			String lookAndFeel;
			if (GUI.LOOKANDFEEL.equals("Metal")) {
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
			} else if (GUI.LOOKANDFEEL.equals("System")) {
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			} else if (GUI.LOOKANDFEEL.equals("Motif")) {
				lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
			} else if (GUI.LOOKANDFEEL.equals("GTK+")) {
				lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			} else {
				ErrorLog.logError("Unexpected value of LOOKANDFEEL specified: " + GUI.LOOKANDFEEL);
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
			}
			try {
				UIManager.setLookAndFeel(lookAndFeel);
			} catch (ClassNotFoundException ex2) {
				ErrorLog.logError("Couldn't find class for specified look and feel:" + lookAndFeel);
				ErrorLog.logError("Did you include the L&F library in the class path?");
				ErrorLog.logError("Using the default look and feel.");
			} catch (UnsupportedLookAndFeelException ex3) {
				ErrorLog.logError("Can't use the specified look and feel (" + lookAndFeel + ") on this platform.");
				ErrorLog.logError("Using the default look and feel.");
			} catch (Exception ex) {
				ErrorLog.logError("Couldn't get specified look and feel (" + lookAndFeel + "), for some reason.");
				ErrorLog.logError("Using the default look and feel.");
				ErrorLog.logError(ex);
			}
		}
	}

	public JFrame getFrame() {
		return this.frame;
	}

	public void setStatusLabelRunning() {
		this.statusLabel.setText("    Running ");
		this.statusLabel.setForeground(this.runningColor);
	}

	public void setStatusLabelSuspended() {
		this.statusLabel.setText("Suspended ");
		this.statusLabel.setForeground(this.suspendedColor);
	}

	public void setStatusLabelHalted() {
		this.statusLabel.setText("       Halted ");
		this.statusLabel.setForeground(this.haltedColor);
	}

	public void setStatusLabel(final boolean b) {
		if (b) {
			this.setStatusLabelSuspended();
		} else {
			this.setStatusLabelHalted();
		}
	}

	public void setTextConsoleEnabled(final boolean enabled) {
		this.ioPanel.setEnabled(enabled);
	}

	public VideoConsole getVideoConsole() {
		return this.video;
	}

	public void update(ControlSignals signals) {
		if (toggleUpdateButton.isSelected()) {
			dataPath.UpdateSignals(signals);
			updateStack();
			updateBreakPoints();
		}
	}

	void updateStack() {
		Stack callStack = this.mac.getCallStack();
		Iterator stackIterator = callStack.iterator();
		stackText.setText("");
		while (stackIterator.hasNext()) {
			int address = (Integer) stackIterator.next();
			stackText.append("Function called: " + this.mac.getMemory().getValueAt(address - 1, 2) + "\n"
					+ "Return Address: " + Word.toHex(address, true) + "\n");
		}
	}

	void updateBreakPoints() {
		int currentBreakPoint = 1;
		Memory memory = this.mac.getMemory();
		breakpointsText.setText("");
		for (int i = 0; i < 65536; i++) {
			if (memory.isBreakPointSet(i)) {
				breakpointsText.append(
						"Breakpoint: " + currentBreakPoint++ + " | " + "Address: " + Word.toHex(i, true) + "\n");
			}
		}
	}

	public void reset() {
		this.setTextConsoleEnabled(true);
		this.commandPanel.reset();
		this.video.reset();
		this.scrollToPC();
	}

	static {
		GUI.LOOKANDFEEL = "Metal";
		BreakPointColor = new Color(241, 103, 103);
		PCColor = Color.YELLOW;
	}
}
