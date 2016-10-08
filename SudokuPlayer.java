import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;

public class SudokuPlayer implements Runnable, ActionListener {

	// final values must be assigned in vals[][]
	int[][] vals = new int[9][9];
	Board board = null;

	Queue<Arc> globalQueue = new LinkedList<Arc>();
	static Cell[][] cells = new Cell[9][9];

	/*
	 * This method sets up the data structures and the initial global
	 * constraints (by calling allDiff()) and makes the initial call to
	 * backtrack().
	 */

	private final void init() {
		// Do NOT remove these 3 lines (required for the GUI)
		board.Clear();
		ops = 0;
		recursions = 0;

		// sets up all arcs, as well as cells, populating their neighbors
		allDiff();
		// replaces default domain with known constraints
		setupGlobalDomains();

		// Initial call to backtrack() on cell 0 (top left)
		boolean success = backtrack(0, 0, cells);

		// Prints evaluation of run
		Finished(success);

	}

	private final void allDiff() {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				allArcs(i, j);
				allNeighbors(i, j);
			}
		}
	}

	private final void setupGlobalDomains() {
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				if (vals[x][y] != 0) {
					cells[x][y].setDomain(vals[x][y]);
				}
			}
		}

	}

	private final void allNeighbors(int x, int y) {
		// set domain if preset
		int[] xValues = new int[20];
		int[] yValues = new int[20];
		int index = 0;

		for (int row = 0; row < 9; row++) {
			if (x != row) {
				xValues[index] = row;
				yValues[index] = y;
				index++;

			}
		}
		for (int column = 0; column < 9; column++) {
			if (y != column) {
				xValues[index] = x;
				yValues[index] = column;
				index++;
			}
		}

		int offsetX = (x / 3) * 3;
		int offsetY = (y / 3) * 3;

		for (int i = offsetX; i < offsetX + 3; i++) {
			for (int j = offsetY; j < offsetY + 3; j++) {
				if (x != i && y != j) {
					xValues[index] = i;
					yValues[index] = j;
					index++;

				}

			}
		}

		Cell temp = new Cell(xValues, yValues, x, y);
		cells[x][y] = temp;
	}

	// takes a x,y pair representing a cell, and finds all of its arcs
	private final void allArcs(int x, int y) {
		for (int row = 0; row < 9; row++) {
			if (x != row) {
				Arc temp = new Arc(x, y, row, y);
				globalQueue.add(temp);
			}
		}
		for (int column = 0; column < 9; column++) {
			if (y != column) {

				Arc temp = new Arc(x, y, x, column);
				globalQueue.add(temp);
			}

		}

		int offsetX = (x / 3) * 3;
		int offsetY = (y / 3) * 3;

		for (int i = offsetX; i < offsetX + 3; i++) {
			for (int j = offsetY; j < offsetY + 3; j++) {
				if (x != i && y != j) {

					Arc temp = new Arc(x, y, i, j);
					globalQueue.add(temp);
				}

			}
		}

	}

	// This is the Recursive AC3. ( You may change this method header )
	private final boolean backtrack(int cellx, int celly, Cell[][] cellsPass) {
		recursions += 1;

		// check ac3
		if (!AC3(cellsPass)) {
			return false;
		}

		// this is the last cell
		if (cellx == 8 && celly == 8) {

			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < 9; j++) {
					vals[i][j] = cellsPass[i][j].domain.get(0);
				}
			}
			return true;
		}

		else {
			// not last cell

			int nextCellX = cellx + 1;
			int nextCellY = celly;

			if (cellx == 8) {
				nextCellX = 0;
				nextCellY = celly + 1;
			}

			// cell is set by default, call on next
			if (vals[cellx][celly] != 0) {
				Cell currentCell = cellsPass[cellx][celly];
				currentCell.setDomain(vals[cellx][celly]);
				// next
				return backtrack(nextCellX, nextCellY, cellsPass);
			} 
			
			// no value set by default, set and call next
			else {
				//for value in domain, make a copy, set domain, and backtrack
				for (Integer value : cellsPass[cellx][celly].domain) {
					
					// make a copy of cells (equivalent to domains)
					Cell[][] cellsCopy = new Cell[9][9];
					for (int i = 0; i < 9; i++) {
						for (int j = 0; j < 9; j++) {
							cellsCopy[i][j] = cellsPass[i][j].clone();
						}
					}
					
					// set a domain value
					Cell currentCell = cellsCopy[cellx][celly];
					currentCell.setDomain(value);
					
					// call backtrack on next cell
					if (backtrack(nextCellX, nextCellY, cellsCopy)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private final boolean AC3(Cell[][] cellsCopy) {

		// create local queue of arcs
		Queue<Arc> localQueue = new LinkedList<Arc>(globalQueue);

		while (!localQueue.isEmpty()) {
			Arc current = localQueue.poll();

			if (Revise(current, cellsCopy)) {
				Cell x = cellsCopy[current.ix][current.iy];
				if (x.isEmpty()) {
					return false;
				} else {
					for (int i = 0; i < x.neighborsX.length; i++) {
						Arc newArc = new Arc(x.neighborsX[i], x.neighborsY[i], current.ix, current.iy);
						localQueue.add(newArc);
					}
				}
			}

		}
		return true;
	}

	// This is the Revise() method defined in the book
	// ( You may change this method header )

	private final boolean Revise(Arc current, Cell[][] cellsCopy) {
		ops += 1;
		Cell x = cellsCopy[current.ix][current.iy];
		Cell y = cellsCopy[current.jx][current.jy];

		for (Integer valueX : x.domain) {
			if (y.domain.size() == 1 && valueX == y.domain.get(0)) {
				x.domain.remove(valueX);
				return true;
			}
		}

		return false;
	}

	private final void customSolver() {

		// ’success’ should be set to true if a successful board
		// is found and false otherwise.
		boolean success = true;
		board.Clear();

		System.out.println("Running custom algorithm");

		// -- Your Code Here --

		Finished(success);

	}

	/// ---------- HELPER FUNCTIONS --------- ///
	/// ---- DO NOT EDIT REST OF FILE --- ///
	/// ---------- HELPER FUNCTIONS --------- ///
	/// ---- DO NOT EDIT REST OF FILE --- ///
	public final boolean valid(int x, int y, int val) {
		ops += 1;
		if (vals[x][y] == val)
			return true;
		if (rowContains(x, val))
			return false;
		if (colContains(y, val))
			return false;
		if (blockContains(x, y, val))
			return false;
		return true;
	}

	public final boolean blockContains(int x, int y, int val) {
		int block_x = x / 3;
		int block_y = y / 3;
		for (int r = (block_x) * 3; r < (block_x + 1) * 3; r++) {
			for (int c = (block_y) * 3; c < (block_y + 1) * 3; c++) {
				if (vals[r][c] == val)
					return true;
			}
		}
		return false;
	}

	public final boolean colContains(int c, int val) {
		for (int r = 0; r < 9; r++) {
			if (vals[r][c] == val)
				return true;
		}
		return false;
	}

	public final boolean rowContains(int r, int val) {
		for (int c = 0; c < 9; c++) {
			if (vals[r][c] == val)
				return true;
		}
		return false;
	}

	private void CheckSolution() {
		// If played by hand, need to grab vals
		board.updateVals(vals);

		/*
		 * for(int i=0; i<9; i++){ for(int j=0; j<9; j++)
		 * System.out.print(vals[i][j]+" "); System.out.println(); }
		 */

		for (int v = 1; v <= 9; v++) {
			// Every row is valid
			for (int r = 0; r < 9; r++) {
				if (!rowContains(r, v)) {
					board.showMessage("Value " + v + " missing from row: " + (r + 1));// +
																						// "
																						// val:
																						// "
																						// +
																						// v);
					return;
				}
			}
			// Every column is valid
			for (int c = 0; c < 9; c++) {
				if (!colContains(c, v)) {
					board.showMessage("Value " + v + " missing from column: " + (c + 1));// +
																							// "
																							// val:
																							// "
																							// +
																							// v);
					return;
				}
			}
			// Every block is valid
			for (int r = 0; r < 3; r++) {
				for (int c = 0; c < 3; c++) {
					if (!blockContains(r, c, v)) {
						return;
					}
				}
			}
		}
		board.showMessage("Success!");
	}

	/// ---- GUI + APP Code --- ////
	/// ---- DO NOT EDIT --- ////
	enum algorithm {
		AC3, Custom
	}

	// changed to take 4 values
	class Arc implements Comparable<Object> {
		int ix, iy, jx, jy;

		public Arc(int ix, int iy, int jx, int jy) {
			if (ix == jx && iy == jy) {
				try {
					throw new Exception("=");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			this.ix = ix;
			this.iy = iy;
			this.jx = jx;
			this.jy = jy;
		}

		public int compareTo(Object o) {
			return this.toString().compareTo(o.toString());
		}

		public String toString() {
			return "((" + ix + ", " + iy + ") , (" + jx + ", " + jy + "))";
		}
	}

	// our addition
	class Cell {
		int x;
		int y;
		int[] neighborsX = new int[20];
		int[] neighborsY = new int[20];
		ArrayList<Integer> domain = new ArrayList();

		public Cell(int[] nx, int[] ny, int cellX, int cellY) {
			this.neighborsX = nx.clone();
			this.neighborsY = ny.clone();
			this.x = cellX;
			this.y = cellY;
			domain = setDefaultDomain();
		}

		public Cell(int[] nx, int[] ny, int cellX, int cellY, ArrayList<Integer> sDomain) {
			this.neighborsX = nx.clone();
			this.neighborsY = ny.clone();
			this.x = cellX;
			this.y = cellY;
			for (Integer i : sDomain) {
				domain.add(i);
			}
		}

		public void removeFromDomain(Integer x) {
			domain.remove(x);
		}

		public void setDomain(Integer x) {
			domain.clear();
			domain.add(x);
		}

		public boolean isEmpty() {
			return domain.isEmpty();
		}

		public ArrayList setDefaultDomain() {
			ArrayList<Integer> temp = new ArrayList();

			for (int i = 1; i < 10; i++) {
				temp.add(i);
			}
			return temp;
		}

		public Cell clone() {
			Cell c = new Cell(this.neighborsX, this.neighborsY, this.x, this.y, this.domain);
			return c;

		}
	}

	enum difficulty {
		easy, medium, hard, random
	}

	public void actionPerformed(ActionEvent e) {
		String label = ((JButton) e.getSource()).getText();
		if (label.equals("AC-3"))
			init();
		else if (label.equals("Clear"))
			board.Clear();
		else if (label.equals("Check"))
			CheckSolution();
		// added
		else if (label.equals("Custom"))
			customSolver();
	}

	public void run() {
		board = new Board(gui, this);

		long start = 0, end = 0;

		while (!initialize())
			;
		if (gui)
			board.initVals(vals);
		else {
			board.writeVals();
			System.out.println("Algorithm: " + alg);
			switch (alg) {
			default:
			case AC3:
				start = System.currentTimeMillis();
				init();
				end = System.currentTimeMillis();
				break;
			case Custom: // added
				start = System.currentTimeMillis();
				customSolver();
				end = System.currentTimeMillis();
				break;
			}

			CheckSolution();

			if (!gui)
				System.out.println("time to run: " + (end - start));
		}
	}

	public final boolean initialize() {
		switch (level) {
		case easy:
			vals[0] = new int[] { 0, 0, 0, 1, 3, 0, 0, 0, 0 };
			vals[1] = new int[] { 7, 0, 0, 0, 4, 2, 0, 8, 3 };
			vals[2] = new int[] { 8, 0, 0, 0, 0, 0, 0, 4, 0 };
			vals[3] = new int[] { 0, 6, 0, 0, 8, 4, 0, 3, 9 };
			vals[4] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			vals[5] = new int[] { 9, 8, 0, 3, 6, 0, 0, 5, 0 };
			vals[6] = new int[] { 0, 1, 0, 0, 0, 0, 0, 0, 4 };
			vals[7] = new int[] { 3, 4, 0, 5, 2, 0, 0, 0, 8 };
			vals[8] = new int[] { 0, 0, 0, 0, 7, 3, 0, 0, 0 };
			break;
		case medium:
			vals[0] = new int[] { 0, 4, 0, 0, 9, 8, 0, 0, 5 };
			vals[1] = new int[] { 0, 0, 0, 4, 0, 0, 6, 0, 8 };
			vals[2] = new int[] { 0, 5, 0, 0, 0, 0, 0, 0, 0 };
			vals[3] = new int[] { 7, 0, 1, 0, 0, 9, 0, 2, 0 };
			vals[4] = new int[] { 0, 0, 0, 0, 8, 0, 0, 0, 0 };
			vals[5] = new int[] { 0, 9, 0, 6, 0, 0, 3, 0, 1 };
			vals[6] = new int[] { 0, 0, 0, 0, 0, 0, 0, 7, 0 };
			vals[7] = new int[] { 6, 0, 2, 0, 0, 7, 0, 0, 0 };
			vals[8] = new int[] { 3, 0, 0, 8, 4, 0, 0, 6, 0 };
			break;
		case hard:
			vals[0] = new int[] { 1, 2, 0, 4, 0, 0, 3, 0, 0 };
			vals[1] = new int[] { 3, 0, 0, 0, 1, 0, 0, 5, 0 };
			vals[2] = new int[] { 0, 0, 6, 0, 0, 0, 1, 0, 0 };
			vals[3] = new int[] { 7, 0, 0, 0, 9, 0, 0, 0, 0 };
			vals[4] = new int[] { 0, 4, 0, 6, 0, 3, 0, 0, 0 };
			vals[5] = new int[] { 0, 0, 3, 0, 0, 2, 0, 0, 0 };
			vals[6] = new int[] { 5, 0, 0, 0, 8, 0, 7, 0, 0 };
			vals[7] = new int[] { 0, 0, 7, 0, 0, 0, 0, 0, 5 };
			vals[8] = new int[] { 0, 0, 0, 0, 0, 0, 0, 9, 8 };
			break;
		case random:
		default:
			ArrayList<Integer> preset = new ArrayList<Integer>();
			while (preset.size() < numCells) {
				int r = rand.nextInt(81);
				if (!preset.contains(r)) {
					preset.add(r);
					int x = r / 9;
					int y = r % 9;
					if (!assignRandomValue(x, y))
						return false;
				}
			}
			break;
		}
		return true;
	}

	public final boolean assignRandomValue(int x, int y) {
		ArrayList<Integer> pval = new ArrayList<Integer>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

		while (!pval.isEmpty()) {
			int ind = rand.nextInt(pval.size());
			int i = pval.get(ind);
			if (valid(x, y, i)) {
				vals[x][y] = i;
				return true;
			} else
				pval.remove(ind);
		}
		System.err.println("No valid moves exist.  Recreating board.");
		for (int r = 0; r < 9; r++) {
			for (int c = 0; c < 9; c++) {
				vals[r][c] = 0;
			}
		}
		return false;
	}

	private void Finished(boolean success) {
		if (success) {
			board.writeVals();
			board.showMessage(
					"Solved in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
		} else {
			board.showMessage("No valid configuration found in " + myformat.format(ops) + " ops \t("
					+ myformat.format(recursions) + " recursive ops)");
		}
	}

	public static void main(String[] args) {

		Scanner scan = new Scanner(System.in);

		System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

		char c = '*';

		while (c != 'e' && c != 'm' && c != 'n' && c != 'h' && c != 'r') {
			c = scan.nextLine().charAt(0);

			if (c == 'e')
				level = difficulty.valueOf("easy");
			else if (c == 'm')
				level = difficulty.valueOf("medium");
			else if (c == 'h')
				level = difficulty.valueOf("hard");
			else if (c == 'r')
				level = difficulty.valueOf("random");
			else {
				System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
			}
			// System.out.println("2: "+c+" "+level);
		}

		System.out.println("Gui? y or n ");
		c = scan.nextLine().charAt(0);

		if (c == 'n')
			gui = false;
		else
			gui = true;

		// System.out.println("c: "+c+", Difficulty: " + level);

		// System.out.println("Difficulty: " + level);

		if (!gui) {
			System.out.println("Algorithm? AC3 (1) or Custom (2)");
			if (scan.nextInt() == 1)
				alg = algorithm.valueOf("AC3");
			else
				alg = algorithm.valueOf("Custom");
		}

		SudokuPlayer app = new SudokuPlayer();

		app.run();

	}

	class Board {
		GUI G = null;
		boolean gui = true;

		public Board(boolean X, SudokuPlayer s) {
			gui = X;
			if (gui)
				G = new GUI(s);
		}

		public void initVals(int[][] vals) {
			G.initVals(vals);
		}

		public void writeVals() {
			if (gui)
				G.writeVals();
			else {
				for (int r = 0; r < 9; r++) {
					if (r % 3 == 0)
						System.out.println(" ----------------------------");
					for (int c = 0; c < 9; c++) {
						if (c % 3 == 0)
							System.out.print(" | ");
						if (vals[r][c] != 0) {
							System.out.print(vals[r][c] + " ");
						} else {
							System.out.print("_ ");
						}
					}
					System.out.println(" | ");
				}
				System.out.println(" ----------------------------");
			}
		}

		public void Clear() {
			if (gui)
				G.clear();
		}

		public void showMessage(String msg) {
			if (gui)
				G.showMessage(msg);
			System.out.println(msg);
		}

		public void updateVals(int[][] vals) {
			if (gui)
				G.updateVals(vals);
		}

	}

	class GUI {
		// ---- Graphics ---- //
		int size = 40;
		JFrame mainFrame = null;
		JTextField[][] cells;
		JPanel[][] blocks;

		public void initVals(int[][] vals) {
			// Mark in gray as fixed
			for (int r = 0; r < 9; r++) {
				for (int c = 0; c < 9; c++) {
					if (vals[r][c] != 0) {
						cells[r][c].setText(vals[r][c] + "");
						cells[r][c].setEditable(false);
						cells[r][c].setBackground(Color.lightGray);
					}
				}
			}
		}

		public void showMessage(String msg) {
			JOptionPane.showMessageDialog(null, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
		}

		public void updateVals(int[][] vals) {

			// System.out.println("calling update");
			for (int r = 0; r < 9; r++) {
				for (int c = 0; c < 9; c++) {
					try {
						vals[r][c] = Integer.parseInt(cells[r][c].getText());
					} catch (java.lang.NumberFormatException e) {
						System.out.println("Invalid Board: row col: " + (r + 1) + " " + (c + 1));
						showMessage("Invalid Board: row col: " + (r + 1) + " " + (c + 1));
						return;
					}
				}
			}
		}

		public void clear() {
			for (int r = 0; r < 9; r++) {
				for (int c = 0; c < 9; c++) {
					if (cells[r][c].isEditable()) {
						cells[r][c].setText("");
						vals[r][c] = 0;
					} else {
						cells[r][c].setText("" + vals[r][c]);
					}
				}
			}
		}

		public void writeVals() {
			for (int r = 0; r < 9; r++) {
				for (int c = 0; c < 9; c++) {
					cells[r][c].setText(vals[r][c] + "");
				}
			}
		}

		public GUI(SudokuPlayer s) {

			mainFrame = new javax.swing.JFrame();
			mainFrame.setLayout(new BorderLayout());
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JPanel gamePanel = new javax.swing.JPanel();
			gamePanel.setBackground(Color.black);
			mainFrame.add(gamePanel, BorderLayout.NORTH);
			gamePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			gamePanel.setLayout(new GridLayout(3, 3, 3, 3));

			blocks = new JPanel[3][3];
			for (int i = 0; i < 3; i++) {
				for (int j = 2; j >= 0; j--) {
					blocks[i][j] = new JPanel();
					blocks[i][j].setLayout(new GridLayout(3, 3));
					gamePanel.add(blocks[i][j]);
				}
			}

			cells = new JTextField[9][9];
			for (int cell = 0; cell < 81; cell++) {
				int i = cell / 9;
				int j = cell % 9;
				cells[i][j] = new JTextField();
				cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
				cells[i][j].setHorizontalAlignment(JTextField.CENTER);
				cells[i][j].setSize(new java.awt.Dimension(size, size));
				cells[i][j].setPreferredSize(new java.awt.Dimension(size, size));
				cells[i][j].setMinimumSize(new java.awt.Dimension(size, size));
				blocks[i / 3][j / 3].add(cells[i][j]);
			}

			JPanel buttonPanel = new JPanel(new FlowLayout());
			mainFrame.add(buttonPanel, BorderLayout.SOUTH);
			// JButton DFS_Button = new JButton("DFS");
			// DFS_Button.addActionListener(s);
			JButton AC3_Button = new JButton("AC-3");
			AC3_Button.addActionListener(s);
			JButton Clear_Button = new JButton("Clear");
			Clear_Button.addActionListener(s);
			JButton Check_Button = new JButton("Check");
			Check_Button.addActionListener(s);
			// buttonPanel.add(DFS_Button);
			JButton Custom_Button = new JButton("Custom");
			Custom_Button.addActionListener(s);
			// added
			buttonPanel.add(AC3_Button);
			buttonPanel.add(Custom_Button);
			buttonPanel.add(Clear_Button);
			buttonPanel.add(Check_Button);

			mainFrame.pack();
			mainFrame.setVisible(true);

		}
	}

	Random rand = new Random();

	// ----- Helper ---- //
	static algorithm alg = algorithm.AC3;
	static difficulty level = difficulty.easy;
	static boolean gui = true;
	static int ops;
	static int recursions;
	static int numCells = 15;
	static DecimalFormat myformat = new DecimalFormat("###,###");
}

