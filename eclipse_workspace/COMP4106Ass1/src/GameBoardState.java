import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GameBoardState {
	public static final Map<Integer, Integer[]> MOUSE_ANGLE_MOVESET_MAP;
	static {
		Map<Integer, Integer[]> m = new HashMap<>();
		m.put(-135, new Integer[] { -1, -1 });
		m.put(-90, new Integer[] { -1, 0 });
		m.put(-45, new Integer[] { -1, 1 });
		m.put(-180, new Integer[] { 0, -1 });
		m.put(180, new Integer[] { 0, -1 });
		m.put(135, new Integer[] { 1, -1 });
		m.put(90, new Integer[] { 1, 0 });
		m.put(45, new Integer[] { 1, 1 });
		m.put(0, new Integer[] { 0, 1 });
		MOUSE_ANGLE_MOVESET_MAP = Collections.unmodifiableMap(m);
	};
	public static final Double CAT_EUCLIDEAN_DISTANCE = Math.sqrt(5);
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
//	public BoardPiece[][] board;
	public Point cat;
	public Point mouse;
	public List<Point> cheese;
	public Boolean same = true;
	// The cost to reach the current state for the cat
	public Double cost;
	public final int MOUSE_STEPS = 3;

//	public GameBoardState(GameBoardState parent, int boardSize, BoardPiece[][] board, Point cat, Point mouse,
//			List<Point> cheese, Double cost) {
//		this.parent = parent;
//		this.boardSize = boardSize;
//		this.cat = cat;
//		this.mouse = mouse;
//		this.cheese = cheese;
//		this.board = board;
//		this.cost = cost;
//	}
	
	public GameBoardState(GameBoardState parent, int boardSize, Point cat, Point mouse,
			List<Point> cheese, Double cost) {
		this.parent = parent;
		this.boardSize = boardSize;
		this.cat = cat;
		this.mouse = mouse;
		this.cheese = cheese;
		this.cost = cost;
	}
	
	public GameBoardState(GameBoardState parent, int boardSize, Point cat, Point mouse,
			List<Point> cheese) {
		this.parent = parent;
		this.boardSize = boardSize;
		this.cat = cat;
		this.mouse = mouse;
		this.cheese = cheese;
	}
	
//	public GameBoardState(GameBoardState parent, int boardSize, BoardPiece[][] board, Point cat, Point mouse,
//			List<Point> cheese) {
//		this.parent = parent;
//		this.boardSize = boardSize;
//		this.cat = cat;
//		this.mouse = mouse;
//		this.cheese = cheese;
//		this.board = board;
//	}

	public GameBoardState() {
	}


	
	private Point generateRandomPoint(int boardSize, Random rand) {
		return new Point(rand.nextInt(boardSize),rand.nextInt(boardSize));
	}
	/*
	 * Initialize the board state
	 */
	
	public static List<GameBoardState> generateStates(int num, int boardSize){
		List<GameBoardState> states = new ArrayList<GameBoardState>();
		for(int i = 0 ; i < num ; i ++) {
			GameBoardState start = new GameBoardState();
			start.init(false, boardSize);
			states.add(start);
		}
		return states;
	}
	public void init(boolean def, Integer boardSize) {
		// if not random, we setup the board as
		// shown in the assignment
		parent = null;
		if (def) {
			this.boardSize = boardSize;
			this.cheese = new ArrayList<Point>();
			cheese.add(new Point(9, 1));
			cheese.add(new Point(9, 6));
			cheese.add(new Point(6, 10));
			mouse = new Point(7, 1);
			cat = new Point(2, 6);
			

//			this.boardSize = boardSize;
//			this.cheese = new ArrayList<Point>();
//			cheese.add(new Point(2,4));
//			cheese.add(new Point(3, 10));
//			cheese.add(new Point(2, 5));
//			mouse = new Point(5, 3);
//			cat = new Point(10, 10);
		}else {
			this.boardSize = boardSize;
			this.cheese = new ArrayList<Point>();
			
			Random rand = new Random();
			// add a random cat position
//			System.out.println(rand.nextInt(boardSize));
			cat = generateRandomPoint(boardSize, rand);
//			this.board[cat.y][cat.x] = BoardPiece.CAT;
			// add a random mouse position that is not the same as the cat
			do {
				mouse = generateRandomPoint(boardSize, rand);
			}while(mouse.equals(cat));
//			this.board[mouse.y][mouse.x] = BoardPiece.MOUSE;

			int numCheese = 3; 
			// create a random number of cheese from 2
			// make sure points are not equal to the cat and mouse position
			for(int i = 0 ; i < numCheese ; i++) {
				Point cheesePoint;
				do {
					cheesePoint = generateRandomPoint(boardSize, rand);
				}while(cheesePoint.equals(cat) || cheesePoint.equals(mouse));
				
//				this.board[cheesePoint.y][cheesePoint.x] = BoardPiece.CHEESE;
				cheese.add(cheesePoint);
			}
			System.out.println("Creating Random Board.");
			System.out.println("Initialized state");
			System.out.println(this.cat);
			System.out.println(this.mouse);
			System.out.println(this.cheese);
			this.display();
		}
	}
	public Double EuclideanDistance(Point p1, Point p2) {
		double dx = p2.x - p1.x;
		double dy = p2.y - p1.y;
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	public Point getClosestCheese(Point mouse, List<Point> cheese) {
		Point closest = cheese.get(0);
		Double curr = EuclideanDistance(mouse, cheese.get(0));
		for (Point c : cheese) {
			Double d = EuclideanDistance(mouse, c);
			if (d < curr) {
				closest = c;
			}
		}
		return closest;
	}
	
	public Point nextMouseMove(Point mouse, Point cheese) {
		Double dx = (double) (cheese.x - mouse.x);
		Double dy = (double) (cheese.y - mouse.y);
		Double angle = Math.atan2(dy, dx);
		angle = 180 / Math.PI * angle;
//	 round angle to nearest orientation
		Integer rounded_angle = (int) (Math.round(angle / 45) * 45);
		Integer[] mouseMove = MOUSE_ANGLE_MOVESET_MAP.get(rounded_angle);
		Point newPoint = new Point(mouse.x + mouseMove[1]*MOUSE_STEPS, mouse.y + mouseMove[0]*MOUSE_STEPS);
		return newPoint;
	}
	
	
	// Evaluates the estimated total cost of path through this state
	// to the goal. In this case, we are estimated the cost of 
	// the cat trapping the mouse
	public Double EvaluateEuclideanHeuristic() {
		Double h = EuclideanDistance(cat, mouse)/CAT_EUCLIDEAN_DISTANCE;
		return cost + h;
	}
	
	//
	public Double EvaluateEuclideanHeuristic2() {
		Point nextCheese1 = getClosestCheese(mouse, cheese);
		Point nextMouse1 = nextMouseMove(mouse, nextCheese1);
		// the mouse.
//		Double h = EuclideanDistance(cat, nextMouse1)/CAT_EUCLIDEAN_DISTANCE;
		Integer steps = (int)(EuclideanDistance(cat, mouse)/CAT_EUCLIDEAN_DISTANCE);
		for(int i = 0 ; i < steps-1; i++) {
			nextMouse1 = nextMouseMove(nextMouse1, nextCheese1);
		}
		Double h = EuclideanDistance(cat, nextMouse1)/CAT_EUCLIDEAN_DISTANCE;
		
		return cost + h;
	}
	// Take the average of the 2 heuristics from before
	public Double EvaluateEuclideanHeuristicAverage() {
		Double avg = (EvaluateEuclideanHeuristic() + EvaluateEuclideanHeuristic2())/2.0;
		return avg;
	}
	
	public void updateCat(Point newCatPos) {
//		this.board[cat.y][cat.x] = BoardPiece.BLANK;
//		this.board[newCatPos.y][newCatPos.x] = BoardPiece.CAT;
		this.cat = newCatPos;
	}

	public void updateMouse(Point newMousePos) {
		// remove cheese if new mouse position is on cheese
//		System.out.println(cheese);
		Iterator<Point> itr = cheese.iterator();
		while (itr.hasNext()) {
			Point c = itr.next();
			if (c.equals(newMousePos)) {
				cheese.remove(c);
				break;
			}
		}
		this.mouse = newMousePos;
	}

	public void display() {
		String r = "";
		for(int y = 0 ; y < this.boardSize; y++) {
			for(int x = 0 ; x < this.boardSize; x++) {
				Point p = new Point(x, y);
				if (p.equals(this.cat)) {
					r += "C ";
				}else if(p.equals(this.mouse)) {
					r += "M ";
				}else if(this.cheese.contains(p)) {
					r += "O ";
				}else {
					r += "_ ";
				}
			}
			r += "\n";
		}
		System.out.println(r);
	}


	@Override
	public boolean equals(Object o) {
		// If the object is compared with itself then return true
		if (o == this) {
			return true;
		}
		/*
		 * Check if o is an instance of Complex or not "null instanceof [type]" also
		 * returns false
		 */
		if (!(o instanceof GameBoardState)) {
			return false;
		}
		// typecast o to Complex so that we can compare data members
		GameBoardState gbs = (GameBoardState) o;
		return gbs.cat.equals(this.cat) && gbs.mouse.equals(this.mouse) && gbs.cheese.equals(this.cheese);
	}

}
