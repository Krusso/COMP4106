import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameBoardState {
	private static final Map<BoardPiece, Character> DISPLAY_MAP;
	static {
		Map<BoardPiece, Character> m = new HashMap<>();
		m.put(BoardPiece.BLANK, '_');
		m.put(BoardPiece.CAT, 'C');
		m.put(BoardPiece.MOUSE, 'M');
		m.put(BoardPiece.CHEESE, 'O');
		DISPLAY_MAP = Collections.unmodifiableMap(m);
	}
	public GameBoardState parent;
	public Integer boardSize;
	public BoardPiece[][] board;
	public Point cat;
	public Point mouse;
	public List<Point> cheese;
	public Boolean same = true;

	public GameBoardState(GameBoardState parent, int boardSize, BoardPiece[][] board, Point cat, Point mouse, List<Point> cheese) {
		this.parent = parent;
		this.boardSize = boardSize;
		this.cat = cat;
		this.mouse = mouse;
		this.cheese = cheese;
		this.board = board;
	}

	public GameBoardState() {
	}

	/*
	 * Initialize the board state
	 */
	public void init() {
		// if not random, we setup the board as
		// shown in the assignment
		parent = null;
		if (same) {
			this.boardSize = 12;
			this.cheese = new ArrayList<Point>();
			this.board = new BoardPiece[boardSize][boardSize];
			for (int i = 0; i < boardSize; i++) {
				for (int j = 0; j < boardSize; j++) {
					this.board[i][j] = BoardPiece.BLANK;
				}
			}
			this.board[1][7] = BoardPiece.MOUSE;
			this.board[6][2] = BoardPiece.CAT;
			this.board[1][8] = BoardPiece.CHEESE;
			this.board[6][8] = BoardPiece.CHEESE;
			this.board[10][6] = BoardPiece.CHEESE;
			cheese.add(new Point(8, 1));
			cheese.add(new Point(8, 6));
			cheese.add(new Point(6, 10));
			mouse = new Point(7, 1);
			cat = new Point(2, 6);
		}
	}
	
	public void updateCat(Point newCatPos) {
		this.board[cat.y][cat.x] = BoardPiece.BLANK;
		this.board[newCatPos.y][newCatPos.x] = BoardPiece.CAT;
		this.cat = newCatPos;
	}
	
	public void updateMouse(Point newMousePos) {
		// remove cheese if new mouse position is on cheese
//		System.out.println(cheese);
		Iterator<Point> itr = cheese.iterator();
		while(itr.hasNext()) {
			Point c = itr.next();
			if(c.equals(newMousePos)) {
				cheese.remove(c);
				break;
			}
		}
		
		this.board[mouse.y][mouse.x] = BoardPiece.BLANK;
		this.board[newMousePos.y][newMousePos.x] = BoardPiece.MOUSE;
		this.mouse = newMousePos;
	}

	public void display() {
		String r = "";
		for(BoardPiece[] row: this.board) {
			for(BoardPiece bp: row) {
				r = r + DISPLAY_MAP.get(bp) + " ";
			}
			r = r + "\n";
		}
//		for(int i = 0 ; i < this.boardSize ; i++) {
//			for(int j = 0 ; j < this.boardSize ; j++) {
//				if(i==this.cat.y && j==this.cat.x) {
//					r += "C ";
//				}else
//				if(i == this.mouse.y && j == this.mouse.x) {
//					r += "M ";
//				}else if (i == )
//			}
//			
//		}
		System.out.println(r);
	}
	
	public static GameBoardState deepCopy(GameBoardState g) {
		BoardPiece [][] board = new BoardPiece[g.boardSize][g.boardSize];
		for(int i = 0 ; i < g.boardSize ; i++) {
			for(int j = 0 ; j < g.boardSize; j++) {
				board[i][j] = g.board[i][j];
			}
		}
		Point cat = (Point) g.cat.clone();
		Point mouse = (Point) g.mouse.clone();
		List<Point> cheese = new ArrayList<Point>();
		for(Point c : g.cheese) {
			cheese.add(c);
		}
		return new GameBoardState(g.parent, g.boardSize, board, cat, mouse, cheese);
	}

}
