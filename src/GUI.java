import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultHighlighter;

// 
// Decompiled by Procyon v0.5.30
// 

public class GUI implements ActionListener, TableModelListener {
	
	/**
	 * Pointers to essential systems
	 */
	
	private Machine mac;
	private CommandLine commandLine;
	public static String LOOKANDFEEL;
	
	
	/**
	 * STUFF I NEED TO CHANGE
	 */
	
	
	/**
	 * Handles to menu options
	 */
	
	private JFrame frame;
	private JFileChooser fileChooser;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenu aboutMenu;
	private JMenuItem openItem;
	private JMenuItem quitItem;
	private JMenuItem commandItem;
	private JMenuItem versionItem;
	
	/**
	 * Handles to panels of the main menu
	 */
	
	private ControlPanel controlPanel;
	private InfoPanel infoPanel;
	
	/**
	 * Memory dump
	 */

	private JTextField gotoDumpTexts;
	private int numDumps = 4;
		
	/**
	 * Memory Panel
	 */
	
	public final MemoryPanel memoryPanel;
	public static final Color BreakPointColor;
	public static final Color PCColor;
	
	/**
	 * Registers
	 */
	
	private JPanel registerPanel;
	
	/**
	 * Devices
	 */
	
	private DevicePanel devicePanel;
	private JPanel srcPanel;
	private JTextArea srcText;
	private JScrollPane srcScrollPane;
	
	/**
	 * Datapath button
	 */
	
	private DataPath dataPath;
	private JPanel dataPathPanel;
	private JFrame dataPathFrame;
	private JButton dataPathButton;
	public ControlSignals signals;
	
	private final JMenu resourceMenu;
	private final JMenuItem datapathItem;
	private final JMenuItem instructionsItem;
	private final JMenuItem controlSignals1Item;
	private final JMenuItem controlSignals2Item;
	

	/**
	 * Pop up frame that displays the opcodes for instructions
	 */
	
	private JFrame opcodeFrame;
	private JPanel opcodePanel;
	private JTextField opcodeText;
	private boolean addedOpcodeListener = false;
	
	/**
	 * Simple graph
	 */
	
	private LC4Graph lc4Graph;
	private JButton lc4Button;

	/**
	 * Disassembler/Grapher
	 */

	private LC4Decompiler lc4Diassembler;
	
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
	}
	
	private void generateLC4Diassembly()
	{
		lc4Diassembler = new LC4Decompiler(this.mac);

	}

	/**
	 * Loads obj file?
	 * @param s
	 */
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

	/**
	 * Goes to line num
	 * @param n
	 * @param n2
	 */
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

	/**
	 * ?
	 */
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

	/*********************************************************************************************
	 * MAIN MEMORY CENTER
	 *********************************************************************************************/
	
	/**
	 * Panel containing line panel and memory panel
	 * Setups (MAIN CENTER) Memory panel (NOT A DUMP)	
	 */
	
	/*
	 * Components
	 * memoryPanel
	 * 	- lineGoToPanel (North)
	 * 		- lineNumberLabel (West), gotoText (Center), gotoButton (East)
	 * 	- memoryPanel (South)
	 * 		- memTable (West), memScrollPane(East)
	 */	
	class MemoryPanel extends JPanel
	{
		public LineGoToPanel lineGoToPanel;
		public MemoryTablePanel memoryTablePanel;
		
		MemoryPanel(GUI gui)
		{
			//Setup layout
			this.setLayout(new BorderLayout());
			this.setCursor(Cursor.getDefaultCursor());
			
			//Init panels
			lineGoToPanel = new LineGoToPanel(gui, "Line Number:", "Go to line", "goto");
			memoryTablePanel = new MemoryTablePanel(gui);
			
			//Add the two panels
			this.add(lineGoToPanel, BorderLayout.NORTH);
			this.add(memoryTablePanel, BorderLayout.CENTER);
		}
	}
	
	/**
	 * Creates a Line GUI panel consisting of 
	 * panel
	 * 	- label text
	 * 	- button text
	 * 	- goto button
	 * @param labelText 
	 * label of the left side
	 * @param buttonText 
	 * text the button should hold
	 * @param String buttonActionCommand
	 * the command callback of the button
	 */
	class LineGoToPanel extends JPanel
	{
		private JLabel lineNumberLabel;
		private JTextField gotoText;
		private JButton gotoButton;
		
		LineGoToPanel(GUI gui, String labelText, String buttonText, String buttonActionCommand)
		{	
			//Setup layout
			this.setLayout(new BorderLayout());
			this.setCursor(Cursor.getDefaultCursor());
			
			//Init label, text and button
			lineNumberLabel = new JLabel(labelText);
			gotoText = new JTextField();
			gotoButton = new JButton(buttonText);
			
			//Add actionListener to the gotoButton
			gotoButton.setActionCommand(buttonActionCommand);
			gotoButton.addActionListener(gui);
			
			//Add the components
			this.add(lineNumberLabel, BorderLayout.WEST);
			this.add(gotoText, BorderLayout.CENTER);
			this.add(gotoButton, BorderLayout.EAST);
	
			//Format the panel
			this.setPreferredSize(new Dimension(220, 30));
		}
		
		public JTextField GetGoToText()
		{
			return this.gotoText;
		}
	}
	
	/**
	 * Creates a memory panel consisting of 
	 * panel
	 * 	- memory table
	 * 		- memory scroll pane
	 */
	
	class MemoryTablePanel extends JPanel
	{
		JTable memTable;
		JScrollPane memScrollPane;
		MemoryTablePanel(GUI gui)
		{
			//Setup layout
			this.setLayout(new BorderLayout());
			this.setCursor(Cursor.getDefaultCursor());
			
			//Init memtable
			memTable = new JTable(mac.getMemory()) {
				@Override
				public Component prepareRenderer(final TableCellRenderer tableCellRenderer, final int row, final int column) {
					if (!addedOpcodeListener) {
						this.addMouseListener(new MouseAdapter() {
							public void mouseEntered(MouseEvent evt) {
//								if (!opcodeFrame.isVisible()) {
//									opcodeFrame.setVisible(true);
//									opcodeFrame.setLocation(evt.getLocationOnScreen());
//									opcodeFrame.toFront();
//									opcodeFrame.requestFocus();
//									opcodeFrame.repaint();
//									Word word = null;
//									if(memTable.rowAtPoint(evt.getPoint()) != -1)
//										if(GUI.this.mac.getMemory().getWord(memTable.rowAtPoint(evt.getPoint())) != null)
//											word = GUI.this.mac.getMemory().getWord(memTable.rowAtPoint(evt.getPoint()));
//									if (word != null)
//										if (ISA.getInstruction(word) != null)
//											opcodeText.setText("Instruction: " + ISA.disassemble(word, 0, mac)
//													+ " | Bits " + word.toBinary());
//								}
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
					final Component prepareRenderer = super.prepareRenderer(tableCellRenderer, row, column);
					if (column == 0) {
						final JCheckBox checkBox = new JCheckBox();
						if (row < 65024) {
							if (GUI.this.mac.getMemory().isBreakPointSet(row)) {
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
					if (row == GUI.this.mac.getRegisterFile().getPC()) {
						prepareRenderer.setBackground(GUI.PCColor);
					} else if (GUI.this.mac.getMemory().isBreakPointSet(row)) {
						prepareRenderer.setBackground(GUI.BreakPointColor);
					} else {
						prepareRenderer.setBackground(this.getBackground());
						Word word = GUI.this.mac.getMemory().getWord(row);
						if (word != null && infoPanel.toggleColorButton.isSelected()) {
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
				
				@Override
				public boolean isCellEditable(int row, int col) {
					return col == 0;
				}
			};
			
			//Init memScrollPane
			this.memScrollPane = new JScrollPane(this.memTable) {
				@Override
				public JScrollBar createVerticalScrollBar() {
					return new HighlightScrollBar(mac);
				}
			};
			
			memTable.getModel().addTableModelListener(gui);
			memTable.getModel().addTableModelListener((TableModelListener) memScrollPane.getVerticalScrollBar());
			memTable.setPreferredScrollableViewportSize(new Dimension(220, 430));
			if (!PennSim.isDoubleBufferedVideo()) {
				memTable.getModel().addTableModelListener(gui.devicePanel.GetVideo());
			}
			
			//Edit columns of table
			this.memScrollPane.getVerticalScrollBar().setBlockIncrement(this.memTable.getModel().getRowCount() / 512);
			this.memScrollPane.getVerticalScrollBar().setUnitIncrement(16);
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
			
			//add the pane to panel
			this.add(memScrollPane);
			this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Memory"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			
		}
		
		public JTable GetMemTable()
		{
			return memTable;
		}
	}
	
	//==========================================================================================

	/**
	 * 
	 * Control Panel consisting of the command line
	 *
	 */
	
	class ControlPanel extends JPanel
	{
		/**
		 * Buttons
		 */
		private JButton nextButton;
		private JButton stepButton;
		private JButton continueButton;
		private JButton finishButton;
		private JButton stopButton;
		private JButton aboutButton;
		private JButton dataPathButton;

		/**
		 * Command line
		 */
		
		private JLabel statusLabel;
		private Color runningColor;
		private Color suspendedColor;
		private Color haltedColor;
		private CommandLinePanel commandPanel;
		private CommandOutputWindow commandOutputWindow;
		
		/**
		 * Const strings (mostly for callback commands)
		 */
		public String openActionCommand = "Open";
		public String quitActionCommand = "Quit";
		public String outputWindowActionCommand = "OutputWindow";
		public String versionActionCommand = "Version";
		public String nextButtonCommand = "Next";
		public String stepButtonCommand = "Step";
		public String continueButtonCommand = "Continue";
		public String finishButtonCommand = "Finish";
		public String stopButtonCommand = "Stop";
		public String statusLabelRunning = "    Running ";
		public String statusLabelSuspended = "Suspended ";
		public String statusLabelHalted = "       Halted ";
		
		
		ControlPanel(GUI gui)
		{
			//Setup layout
			this.setLayout(new BoxLayout(this, 1));
			
			//Init buttons
			final JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(1, 7));
			buttonPanel.setPreferredSize(new Dimension(1000, 25));

			//next
			this.nextButton = new JButton(nextButtonCommand);
			this.nextButton.setActionCommand(nextButtonCommand);
			this.nextButton.addActionListener(gui);
			buttonPanel.add(this.nextButton);
			
			//step
			this.stepButton = new JButton(stepButtonCommand);
			this.stepButton.setActionCommand(stepButtonCommand);
			this.stepButton.addActionListener(gui);
			buttonPanel.add(this.stepButton);
			
			//continue
			this.continueButton = new JButton(continueButtonCommand);
			this.continueButton.setActionCommand(continueButtonCommand);
			this.continueButton.addActionListener(gui);
			buttonPanel.add(this.continueButton);
			
			//finish
			this.finishButton = new JButton(finishButtonCommand);
			this.finishButton.setActionCommand(finishButtonCommand);
			this.finishButton.addActionListener(gui);
			buttonPanel.add(this.finishButton);
			
			//stop
			this.stopButton = new JButton(stopButtonCommand);
			this.stopButton.setActionCommand(stopButtonCommand);
			this.stopButton.addActionListener(gui);
			buttonPanel.add(this.stopButton);
			
			//about
			this.aboutButton = new JButton("About/Report Issue");
			this.aboutButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					JOptionPane.showMessageDialog(frame,
							"PennSim v2.2. \nIf any issues come up, please reach out to the TAs on Piazza",
							"About | Report", JOptionPane.INFORMATION_MESSAGE, null);
					;
				}
			});
			
			//datapath
			this.dataPathButton = new JButton("Open DataPath Chart");
			this.dataPathButton.setActionCommand("DataPath");
			this.dataPathButton.addActionListener(gui);
			buttonPanel.add(this.dataPathButton);

			buttonPanel.add(this.aboutButton);

			//Init status label
			this.statusLabel = new JLabel("");
			this.runningColor = new Color(43, 129, 51);
			this.suspendedColor = new Color(209, 205, 93);
			this.haltedColor = new Color(161, 37, 40);
			this.SetStatusLabelSuspended();	
			buttonPanel.add(this.statusLabel);
			
			//Init command panel and output window
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
					commandOutputWindow.setVisible(false);
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
			this.commandPanel.setGUI(gui);
			
			this.add(buttonPanel);
			this.add(this.commandPanel);
			this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Controls"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			this.setVisible(true);
		}
		
		public void SetStatusLabelRunning() {
			this.statusLabel.setText(statusLabelRunning);
			this.statusLabel.setForeground(this.runningColor);
		}

		public void SetStatusLabelSuspended() {
			this.statusLabel.setText(statusLabelSuspended);
			this.statusLabel.setForeground(this.suspendedColor);
		}

		public void SetStatusLabelHalted() {
			this.statusLabel.setText(statusLabelHalted);
			this.statusLabel.setForeground(this.haltedColor);
		}
		
		public CommandLinePanel GetCommandPanel()
		{
			return commandPanel;
		}
	}
	
	public JPanel getControlPanel() {
		return this.controlPanel;
	}

	//==========================================================================================
	
	/**
	 * 
	 * Register panel consisting of all the important registers R0, R1, ...
	 *
	 */
	
	class RegisterPanel extends JPanel
	{
		private JTable regTable;
		
		RegisterPanel(GUI gui)
		{
			this.regTable = new JTable(gui.mac.getRegisterFile());
			final TableColumn column = this.regTable.getColumnModel().getColumn(0);
			column.setMaxWidth(30);
			column.setMinWidth(30);
			final TableColumn column2 = this.regTable.getColumnModel().getColumn(2);
			column2.setMaxWidth(30);
			column2.setMinWidth(30);
			
			this.regTable.getModel().addTableModelListener(gui);

			this.add(this.regTable);
			this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Registers"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			this.setVisible(true);
		}
	}
	
	//==========================================================================================

	/**
	 * 
	 * Device panel consisting of video console and io text panel
	 *
	 */
	
	class DevicePanel extends JPanel
	{
		private TextConsolePanel ioPanel;
		private VideoConsole video;
		
		DevicePanel(GUI gui)
		{
			//Init video
			this.video = new VideoConsole(gui.mac);
			
			//Init IoPanel
			(this.ioPanel = new TextConsolePanel(gui.mac.getMemory().getKeyBoardDevice(), gui.mac.getMemory().getMonitorDevice()))
			.setMinimumSize(new Dimension(256, 85));
			
			//Format and add video/io panel
			this.setLayout(new BoxLayout(this, 1));
			this.add(this.video);
			this.add(this.ioPanel);
			this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Devices"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			this.setVisible(true);
		}
		
		public VideoConsole GetVideo()
		{
			return this.video;
		}
		
		public TextConsolePanel GetIoPanel()
		{
			return this.ioPanel;
		}
	}
	
	public void setTextConsoleEnabled(final boolean enabled) {
		this.devicePanel.GetIoPanel().setEnabled(enabled);
	}

	public VideoConsole getVideoConsole() {
		return this.devicePanel.GetVideo();
	}
	
	//==========================================================================================
	
	/**
	 * 
	 * Info Panel layout
	 * InfoPanel
	 * 	- dumpAndBreakpoints
	 * 	- togglePanel (South) | toggle buttons
	 *	- 
	 */
	
	class InfoPanel extends JPanel
	{
		//Toggles
		private JToggleButton toggleUpdateButton;
		private JToggleButton toggleColorButton;
		
		public String toggleUpdateString = "toggle update";
		public String toggleColorString = "toggle color";
		
		//Dump panels
		public ArrayList<DumpPanel> dumpPanels;
		public JTabbedPane dumpTabbedPanel;

		//Breakpoint panel
		public JPanel breakpointsPanel;
		public JTextArea breakpointsText;
		
		//Dump and Breakpoints panel
		public JPanel dumpAndBreakpointsPanel;
		
		//Stack panel
		public JPanel stackPanel;
		public JTextArea stackText;
		
		//Buttons
		public JButton lc4Button;
		public JButton lc4DisassemblerButton;		
		
		public String lc4ButtonCommand = "generate graph";
		public String lc4DisassemblerButtonCommand = "disassemble lc4";
		InfoPanel(GUI gui)
		{
			//Setup layout
			this.setLayout(new BorderLayout());
			
			//Init toggle buttons
			this.toggleUpdateButton = new JToggleButton("Additional Information Output Disabled");
			this.toggleColorButton = new JToggleButton("Christmas Syntax Highlighting Disabled");
			
			//Format toggle buttons
			this.toggleUpdateButton.setActionCommand(toggleUpdateString);
			this.toggleUpdateButton.addActionListener(gui);
			this.toggleUpdateButton.setSelected(false);

			this.toggleColorButton.setActionCommand(toggleColorString);
			this.toggleColorButton.addActionListener(gui);
			this.toggleColorButton.setSelected(false);
			
			//Add the buttons to a panel
			JPanel togglePanel = new JPanel(new BorderLayout());
			togglePanel.add(toggleUpdateButton, BorderLayout.NORTH);
			togglePanel.add(toggleColorButton, BorderLayout.SOUTH);
			
			//Setup dump panels
			this.dumpPanels = new ArrayList<DumpPanel>();
			this.dumpPanels.add(new DumpPanel(gui, "gotodump1"));
			this.dumpPanels.add(new DumpPanel(gui, "gotodump2"));
			this.dumpPanels.add(new DumpPanel(gui, "gotodump3"));
			this.dumpPanels.add(new DumpPanel(gui, "gotodump4"));
			
			this.dumpTabbedPanel = new JTabbedPane();
			for(int i = 0; i < dumpPanels.size(); i++)
			{
				this.dumpTabbedPanel.add(this.dumpPanels.get(i), "Dump" + i);
			}
			
			this.breakpointsPanel = new BreakpointTablePanel(gui);
			
			//Setup Dump and breakpoints panel
			dumpAndBreakpointsPanel = new JPanel(new BorderLayout());
			this.dumpAndBreakpointsPanel.add(this.breakpointsPanel, BorderLayout.NORTH);
			this.dumpAndBreakpointsPanel.add(this.dumpTabbedPanel, BorderLayout.SOUTH);
			
			//Setup stack
			this.stackPanel = new JPanel(new BorderLayout());
			this.stackText = new JTextArea();
			
			this.stackPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Stack"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			JScrollPane stackScrollPane = new JScrollPane(new StackTablePanel(gui));
			this.stackPanel.add(stackScrollPane, "North");
			stackScrollPane.setVerticalScrollBarPolicy(22);
			stackScrollPane.setHorizontalScrollBarPolicy(32);
			stackScrollPane.setPreferredSize(new Dimension(225, 480));
			this.stackText.setEditable(false);
			stackScrollPane.setVisible(true);
			
			//Setup the buttons
			this.lc4Button = new JButton("Generate Flow Chart/Graph/How your code looks like");
			this.stackPanel.add(lc4Button, "South");
			this.lc4Button.setActionCommand(lc4ButtonCommand);
			this.lc4Button.addActionListener(gui);
			
			this.lc4DisassemblerButton = new JButton("Diassemble LC4");
			this.stackPanel.add(lc4DisassemblerButton);
			this.lc4DisassemblerButton.setActionCommand(lc4DisassemblerButtonCommand);
			this.lc4DisassemblerButton.addActionListener(gui);
			
			final JSplitPane splitPane = new JSplitPane(1, true, this.dumpAndBreakpointsPanel, this.stackPanel);
			
			//Add toggle button to the south
			this.add(togglePanel, BorderLayout.CENTER);
			this.add(splitPane, BorderLayout.NORTH);
			this.setPreferredSize(this.getPreferredSize());

		}
		
		public JToggleButton GetToggleUpdateButton()
		{
			return this.toggleUpdateButton;
		}
		
		public JToggleButton GetToggleColorButton()
		{
			return this.toggleColorButton;
		}
	}
	
	class MemoryDumpTablePanel extends JPanel
	{
		private JTable memDumpTable;
		private JScrollPane memScrollPane;
		
		MemoryDumpTablePanel(GUI gui)
		{
			//Setup layout
			this.setLayout(new BorderLayout());
			this.setCursor(Cursor.getDefaultCursor());
			//Init memDumpTable
			this.memDumpTable = new JTable(mac.getMemory()) {
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
					return (n2 == 0);
				}
				
			};
			
			this.memScrollPane = new JScrollPane(this.memDumpTable) {
				@Override
				public JScrollBar createVerticalScrollBar() {
					return new HighlightScrollBar(mac);
				}
			};

			this.memScrollPane.getVerticalScrollBar()
					.setBlockIncrement(this.memDumpTable.getModel().getRowCount() / 512);
			this.memScrollPane.getVerticalScrollBar().setUnitIncrement(16);
			
			this.memDumpTable.getModel().addTableModelListener(gui);
			this.memDumpTable.getModel().addTableModelListener((TableModelListener) this.memScrollPane.getVerticalScrollBar());
			this.memDumpTable.setPreferredScrollableViewportSize(new Dimension(220, 200));
			if (!PennSim.isDoubleBufferedVideo()) {
				memDumpTable.getModel().addTableModelListener(gui.devicePanel.GetVideo());
			}
			
			final TableColumn column3 = this.memDumpTable.getColumnModel().getColumn(0);
			column3.setMaxWidth(20);
			column3.setMinWidth(20);
			column3.setCellEditor(new DefaultCellEditor(new JCheckBox()));
			final TableColumn column4 = this.memDumpTable.getColumnModel().getColumn(1);
			column4.setMinWidth(100);
			column4.setPreferredWidth(100);
			column4.setMaxWidth(1000);
			final TableColumn column5 = this.memDumpTable.getColumnModel().getColumn(2);
			column5.setMinWidth(100);
			column5.setPreferredWidth(100);
			column5.setMaxWidth(1000);
			
			this.memDumpTable.scrollRectToVisible(this.memDumpTable.getCellRect(16384, 0, true));
			
			//add the pane to panel
			this.add(memScrollPane);
			this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Memory Dump (For looking at data)"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		}
		
		public JTable GetMemDumpTable()
		{
			return this.memDumpTable;
		}
	}
	
	/**
	 * 
	 * Dump panel consisting of memory table and goto panel
	 *
	 */
	
	class DumpPanel extends JPanel
	{
		public LineGoToPanel lineGoToPanel;
		public MemoryDumpTablePanel memoryTablePanel;
		
		public String commandText;
		DumpPanel(GUI gui, String commandText)
		{
			this.commandText = commandText;
			this.setCursor(Cursor.getDefaultCursor());
			
			//Setup layout
			this.setLayout(new BorderLayout());
			
			//Init panels
			lineGoToPanel = new LineGoToPanel(gui, "Line Number:", "Go to line", commandText);
			memoryTablePanel = new MemoryDumpTablePanel(gui);
			
			//Add the two panels
			this.add(lineGoToPanel, BorderLayout.NORTH);
			this.add(memoryTablePanel, BorderLayout.CENTER);
		}
	}
	
	class BreakpointTablePanel extends JPanel
	{
		private JTable memDumpTable;
		private JScrollPane memScrollPane;
		
		BreakpointTablePanel(GUI gui)
		{
			//Setup layout
			this.setLayout(new BorderLayout());
			this.setCursor(Cursor.getDefaultCursor());
			
			DefaultTableModel breakPointTable = new DefaultTableModel();
			breakPointTable.addColumn("Address");
			breakPointTable.addColumn("Value");

			//Add to memory
			mac.getMemory().breakPointTable = breakPointTable;
			
			//Init memDumpTable
			this.memDumpTable = new JTable(breakPointTable);
			this.memDumpTable.setBackground(new Color(255, 153, 153));
			
			this.memScrollPane = new JScrollPane(this.memDumpTable) {
				@Override
				public JScrollBar createVerticalScrollBar() {
					return new JScrollBar();
				}
			};

			this.memScrollPane.getVerticalScrollBar()
					.setBlockIncrement(this.memDumpTable.getModel().getRowCount() / 512);
			this.memScrollPane.getVerticalScrollBar().setUnitIncrement(16);
			this.memDumpTable.getModel().addTableModelListener(gui);
			//this.memDumpTable.getModel().addTableModelListener((TableModelListener) this.memScrollPane.getVerticalScrollBar());
			this.memDumpTable.setPreferredScrollableViewportSize(new Dimension(220, 100));
			if (!PennSim.isDoubleBufferedVideo()) {
				memDumpTable.getModel().addTableModelListener(gui.devicePanel.GetVideo());
			}
			
			final TableColumn column3 = this.memDumpTable.getColumnModel().getColumn(0);
			column3.setMaxWidth(1000);
			column3.setMinWidth(100);
			column3.setCellEditor(new DefaultCellEditor(new JCheckBox()));
			final TableColumn column4 = this.memDumpTable.getColumnModel().getColumn(1);
			column4.setMinWidth(100);
			column4.setPreferredWidth(100);
			column4.setMaxWidth(1000);
			
			this.memDumpTable.scrollRectToVisible(this.memDumpTable.getCellRect(16384, 0, true));
			
			//add the pane to panel
			this.add(memScrollPane);
			this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Breakpoints"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		}
		
		public JTable GetMemDumpTable()
		{
			return this.memDumpTable;
		}
	}
	
	class StackTablePanel extends JPanel
	{
		private JTable memDumpTable;
		private JScrollPane memScrollPane;
		
		StackTablePanel(GUI gui)
		{
			//Setup layout
			this.setLayout(new BorderLayout());
			this.setCursor(Cursor.getDefaultCursor());

			//Init memDumpTable
			this.memDumpTable = new JTable(mac.getMemory()) {
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
						
						//put stack at register 6
						Stack callStack = gui.mac.getCallStack();
						if(mac != null && mac.getRegisterFile() != null && !callStack.empty())
							memDumpTable.scrollRectToVisible(memDumpTable.getCellRect(mac.getRegisterFile().getRegister(6), 0, true));
					}
				}
			};
			
			this.memScrollPane = new JScrollPane(this.memDumpTable) {
				@Override
				public JScrollBar createVerticalScrollBar() {
					return new HighlightScrollBar(mac);
				}
			};

			this.memScrollPane.getVerticalScrollBar()
					.setBlockIncrement(this.memDumpTable.getModel().getRowCount() / 512);
			this.memScrollPane.getVerticalScrollBar().setUnitIncrement(16);
			
			this.memDumpTable.getModel().addTableModelListener(gui);
			this.memDumpTable.getModel().addTableModelListener((TableModelListener) this.memScrollPane.getVerticalScrollBar());
			this.memDumpTable.setPreferredScrollableViewportSize(new Dimension(220, 200));
			if (!PennSim.isDoubleBufferedVideo()) {
				memDumpTable.getModel().addTableModelListener(gui.devicePanel.GetVideo());
			}
			
			final TableColumn column3 = this.memDumpTable.getColumnModel().getColumn(0);
			column3.setMaxWidth(20);
			column3.setMinWidth(20);
			column3.setCellEditor(new DefaultCellEditor(new JCheckBox()));
			final TableColumn column4 = this.memDumpTable.getColumnModel().getColumn(1);
			column4.setMinWidth(100);
			column4.setPreferredWidth(100);
			column4.setMaxWidth(1000);
			final TableColumn column5 = this.memDumpTable.getColumnModel().getColumn(2);
			column5.setMinWidth(100);
			column5.setPreferredWidth(100);
			column5.setMaxWidth(1000);
			
			this.memDumpTable.scrollRectToVisible(this.memDumpTable.getCellRect(16384, 0, true));
			
			//add the pane to panel
			this.add(memScrollPane);
		}
		
		public JTable GetMemDumpTable()
		{
			return this.memDumpTable;
		}
	}
	
	
	//==========================================================================================

	public GUI(final Machine mac, final CommandLine commandLine) {
		this.frame = new JFrame("PennSim Debugger - " + PennSim.version + " - " + PennSim.getISA());
		this.dataPathFrame = new JFrame("PennSim - " + PennSim.version + " - " + PennSim.getISA());
		this.fileChooser = new JFileChooser(".");
		this.menuBar = new JMenuBar();
		this.fileMenu = new JMenu("File");
		this.aboutMenu = new JMenu("About");
		this.resourceMenu = new JMenu("Resources");
		
		this.openItem = new JMenuItem("Open .obj File");
		this.quitItem = new JMenuItem("Quit");
		this.commandItem = new JMenuItem("Open Command Output Window");
		this.versionItem = new JMenuItem("Simulator Version");
		this.datapathItem = new JMenuItem("Datapath");
		this.instructionsItem = new JMenuItem("LC4 Instructions");
		this.controlSignals1Item = new JMenuItem("Control Signals (Page 1)");
		this.controlSignals2Item = new JMenuItem("Control Signals (Page 2)");
		
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

		this.registerPanel = new JPanel();
		this.dataPathPanel = new JPanel();
		this.srcPanel = new JPanel(new BorderLayout());
		this.srcText = new JTextArea();
		this.srcScrollPane = new JScrollPane(this.srcText);
		this.srcScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		this.mac = mac;
		this.commandLine = commandLine;
		
		
		
		/**
		 * SETUP
		 */
		this.devicePanel = new DevicePanel(this);
		this.memoryPanel = new MemoryPanel(this);
		this.controlPanel = new ControlPanel(this);
		this.infoPanel = new InfoPanel(this);
		
		
		
		// dump stuff
		


		datapathItem.setActionCommand("Datapath");
		datapathItem.addActionListener(this);
		instructionsItem.setActionCommand("Instructions");
		instructionsItem.addActionListener(this);
		controlSignals1Item.setActionCommand("Control Signals 1");
		controlSignals1Item.addActionListener(this);
		controlSignals2Item.setActionCommand("Control Signals 2");
		controlSignals2Item.addActionListener(this);
		
		this.resourceMenu.add(datapathItem);
		this.resourceMenu.add(instructionsItem);
		this.resourceMenu.add(controlSignals1Item);
		this.resourceMenu.add(controlSignals2Item);
		
		//setup data path
		this.dataPath = new DataPath(mac, dataPathFrame);
	}

	public void setUpGUI() {
		initLookAndFeel();
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		this.mac.setStoppedListener(this.controlPanel.GetCommandPanel());
		
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
		this.openItem.setActionCommand(this.controlPanel.openActionCommand);
		this.openItem.addActionListener(this);
		this.fileMenu.add(this.openItem);
		this.commandItem.setActionCommand(this.controlPanel.outputWindowActionCommand);
		this.commandItem.addActionListener(this);
		this.fileMenu.add(this.commandItem);
		this.fileMenu.addSeparator();
		this.quitItem.setActionCommand(this.controlPanel.quitActionCommand);
		this.quitItem.addActionListener(this);
		this.fileMenu.add(this.quitItem);
		this.versionItem.setActionCommand(this.controlPanel.versionActionCommand);
		this.versionItem.addActionListener(this);
		this.aboutMenu.add(this.versionItem);
		this.menuBar.add(this.fileMenu);
		this.menuBar.add(this.aboutMenu);
		this.menuBar.add(this.resourceMenu);
		this.frame.setJMenuBar(this.menuBar);
		this.setupDataPathPanel();

		/**
		 * INIT STUFF
		 */
		this.registerPanel = new RegisterPanel(this);

		// this.setupSrcPanel();

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
		final JSplitPane splitPane = new JSplitPane(1, true, this.memoryPanel, this.infoPanel);
		splitPane.resetToPreferredSizes();
		panel.add(splitPane, "Center");
		this.frame.setSize(new Dimension(700, 725));
		this.frame.setDefaultCloseOperation(3);
		this.frame.pack();
		this.frame.setVisible(true);
		this.scrollToPC();
		
		this.controlPanel.GetCommandPanel().actionPerformed(null);
	}

	public void scrollToIndex(final int n) {
		JTable memTable = this.memoryPanel.memoryTablePanel.GetMemTable();
		memTable.scrollRectToVisible(memTable.getCellRect(n, 0, true));
	}

	public void scrollToPC() {
		this.scrollToPC(0);
	}

	public void scrollToPC(final int n) {
		JTable memTable = this.memoryPanel.memoryTablePanel.GetMemTable();
		memTable.scrollRectToVisible(memTable.getCellRect(this.mac.getRegisterFile().getPC() + n, 0, true));
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
				if (this.controlPanel.nextButtonCommand.equals(actionEvent.getActionCommand())) {
					this.mac.executeNext();
				} else if (this.controlPanel.stepButtonCommand.equals(actionEvent.getActionCommand())) {
					this.mac.executeStep();
				} else if (this.controlPanel.continueButtonCommand.equals(actionEvent.getActionCommand())) {
					this.mac.executeMany();
				} else if (this.controlPanel.finishButtonCommand.equals(actionEvent.getActionCommand())) {
					this.mac.executeFin();
				} else if (this.controlPanel.quitActionCommand.equals(actionEvent.getActionCommand())) {
					this.confirmExit();
				} else if (this.controlPanel.stopButtonCommand.equals(actionEvent.getActionCommand())) {
					Console.println(this.mac.stopExecution(true));
				} else if (this.controlPanel.outputWindowActionCommand.equals(actionEvent.getActionCommand())) {
					this.controlPanel.commandOutputWindow.setVisible(true);
				} else if (this.controlPanel.versionActionCommand.equals(actionEvent.getActionCommand())) {
					JOptionPane.showMessageDialog(this.frame, PennSim.getVersion(), this.controlPanel.versionActionCommand, 1);
				} else if ("DataPath".equals(actionEvent.getActionCommand())) {
					if (!this.dataPathFrame.isVisible())
						this.dataPathFrame.setVisible(true);
					else
						this.dataPathFrame.setVisible(false);
				} else if ("goto".equals(actionEvent.getActionCommand())) {
					JTextField gotoTextField = this.memoryPanel.lineGoToPanel.GetGoToText();
					this.scrollToIndex(Word.parseNum(gotoTextField.getText()));
				} else if (this.infoPanel.toggleUpdateString.equals(actionEvent.getActionCommand())) {
					if (this.infoPanel.toggleUpdateButton.isSelected()) {
						this.infoPanel.toggleUpdateButton.setText("Additional Information Output Enabled");
					} else {
						this.infoPanel.toggleUpdateButton.setText("Additional Information Output Disabled");
					}
				} else if (this.infoPanel.toggleColorString.equals(actionEvent.getActionCommand())) {
					if (this.infoPanel.toggleColorButton.isSelected()) {
						this.infoPanel.toggleColorButton.setText("Christmas Syntax Highlighting Enabled");
					} else {
						this.infoPanel.toggleColorButton.setText("Christmas Syntax Highlighting Disabled");
					}
				} else if ("generate graph".equals(actionEvent.getActionCommand())) {
					this.generateLc4Graph();
				} else if ("disassemble lc4".equals(actionEvent.getActionCommand())) {
					this.generateLC4Diassembly();
				} else if ("Instructions".equals(actionEvent.getActionCommand())) {
					this.generateJPG("/resources/LC4_Instructions.jpg");
				} else if ("Datapath".equals(actionEvent.getActionCommand())) {
					this.generateJPG("/resources/LC4_DataPath.jpg");
				} else if ("Control Signals 1".equals(actionEvent.getActionCommand())) {
					this.generateJPG("/resources/LC4_Control_Signals_1.jpg");
				} else if ("Control Signals 2".equals(actionEvent.getActionCommand())) {
					this.generateJPG("/resources/LC4_Control_Signals_2.jpg");
				}
					
				//check if dump was called
				for(int i = 0; i < this.infoPanel.dumpPanels.size(); i++)
				{
					if (this.infoPanel.dumpPanels.get(i).commandText.equals(actionEvent.getActionCommand())) {
						//attempt to parse the input as a number
						int parsedValue = Word.parseNum(this.infoPanel.dumpPanels.get(i).lineGoToPanel.gotoText.getText());
						if(parsedValue != Integer.MAX_VALUE && parsedValue <= 0xFFFF)
						{
					this.infoPanel.dumpPanels.get(i).memoryTablePanel.GetMemDumpTable().scrollRectToVisible(
							this.infoPanel.dumpPanels.get(i).memoryTablePanel.GetMemDumpTable().getCellRect(parsedValue, 0, true));
						}
					}
				}
			}
		} catch (ExceptionException ex) {
			ex.showMessageDialog(this.frame);
		}
	}

	public void generateJPG(String path) {
		BufferedImage img = null;
		try {
		    img = ImageIO.read(PennSim.class.getResource(path));
		} catch (IOException e) {
		    e.printStackTrace();
		}
		Image scaledImg = img.getScaledInstance(img.getWidth() * 2 / 5, 
				img.getHeight() * 2 / 5, Image.SCALE_SMOOTH);
		ImageIcon icon = new ImageIcon(scaledImg);
		JDialog dialog = new JDialog();
		JLabel label = new JLabel(icon);
		dialog.add(label);
		label.setPreferredSize(dialog.getPreferredSize());
		dialog.pack();
		dialog.setVisible(true);
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
		this.controlPanel.SetStatusLabelRunning();
	}

	public void setStatusLabelSuspended() {
		this.controlPanel.SetStatusLabelSuspended();
	}

	public void setStatusLabelHalted() {
		this.controlPanel.SetStatusLabelHalted();
	}

	public void setStatusLabel(final boolean b) {
		if (b) {
			this.setStatusLabelSuspended();
		} else {
			this.setStatusLabelHalted();
		}
	}
	
	public void update(ControlSignals signals) {
		if (this.infoPanel.toggleUpdateButton.isSelected()) {
			dataPath.UpdateSignals(signals);
			updateStack();
		}
	}

	void updateStack() {
		Stack callStack = this.mac.getCallStack();
		Iterator stackIterator = callStack.iterator();
		/*
		stackText.setText("");
		while (stackIterator.hasNext()) {
			int address = (Integer) stackIterator.next();
			stackText.append("Function called: " + this.mac.getMemory().getValueAt(address - 1, 2) + "\n"
					+ "Return Address: " + Word.toHex(address, true) + "\n");
		}
		*/
	}

	public void reset() {
		this.setTextConsoleEnabled(true);
		this.controlPanel.GetCommandPanel().reset();
		this.devicePanel.GetVideo().reset();
		this.scrollToPC();
	}

	static {
		GUI.LOOKANDFEEL = "Metal";
		BreakPointColor = new Color(241, 103, 103);
		PCColor = Color.YELLOW;
	}
}
