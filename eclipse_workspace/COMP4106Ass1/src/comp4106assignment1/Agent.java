package comp4106assignment1;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

public class Agent {
	// Movesets for a mouse
	private Integer numIterations;
	public static final Integer[][] MOUSE_MOVESET = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 },
			{ 1, 0 }, { 1, 1 } };
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
	public static final Integer[][] CAT_MOVESET = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 },
			{ 2, -1 }, { 2, 1 } };

	public static final Double CAT_EUCLIDEAN_DISTANCE = Math.sqrt(5);
	public Agent() {
		numIterations = 0;

	}

	public static Double EuclideanDistance(Point p1, Point p2) {
		double dx = p2.x - p1.x;
		double dy = p2.y - p1.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static Point getClosestCheese(Point mouse, List<Point> cheese) {
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

	public static Point nextMouseMove(Point mouse, Point cheese) {
		Double dx = (double) (cheese.x - mouse.x);
		Double dy = (double) (cheese.y - mouse.y);
		Double angle = Math.atan2(dy, dx);
		angle = 180 / Math.PI * angle;
//	 round angle to nearest orientation
		Integer rounded_angle = (int) (Math.round(angle / 45) * 45);
		Integer[] mouseMove = MOUSE_ANGLE_MOVESET_MAP.get(rounded_angle);
		Point newPoint = new Point(mouse.x + mouseMove[1], mouse.y + mouseMove[0]);
//	Point newPoint = null;

//	if (dx == 0) {
//		if (dy > 0) {
//			newPoint = new Point(mouse.x, mouse.y + 1);
//		} else if (dy < 0) {
//			newPoint = new Point(mouse.x, mouse.y - 1);
//		}
//	} else if (dy == 0) {
//		if (dx > 0) {
//			newPoint = new Point(mouse.x + 1, mouse.y);
//		} else if (dx < 0) {
//			newPoint = new Point(mouse.x - 1, mouse.y);
//		}
//	} else if (dy / dx > 0) {
//		if (dy < 0 && dx < 0) {
//			newPoint = new Point(mouse.x - 1, mouse.y - 1);
//		} else if (dy > 0 && dx > 0){
//			newPoint = new Point(mouse.x + 1, mouse.y + 1);
//		}
//	}else if (dy / dx < 0) {
//		if (dy < 0) {
//			newPoint = new Point(mouse.x + 1, mouse.y - 1);
//		} else if (dx < 0){
//			newPoint = new Point(mouse.x - 1, mouse.y + 1);
//		}
//	}

		return newPoint;
	}

	public Boolean isValidMove(Point cat, Integer[] move, Integer boardSize) {
		Integer y = cat.y + move[0];
		Integer x = cat.x + move[1];
		if ((y < 0) || (y > boardSize - 1) || (x < 0) || (x > boardSize - 1)) {
			return false;
		}
		return true;
	}
	
	public void incrementIteration() {
		numIterations++;
	}
	
	public Integer getNumIterations() {
		return numIterations;
	}
	
	public void resetNumIterations() {
		this.numIterations = 0;
	}

	public LinkedList<GameBoardState> BFS(GameBoardState start) {
		Queue<GameBoardState> q = new LinkedList<>();
		q.add(start);
		resetNumIterations();
		GameBoardState solution = null;
		LinkedList<GameBoardState> moves = new LinkedList<>();
		while (solution == null) {
			GameBoardState currentState = q.poll();
			
			incrementIteration();
			if (currentState == null) {
				System.out.println("Finished.");
				solution = currentState;
				break;
			}
			if (currentState.cheese.isEmpty()) {
				continue;
			}
			Point closestCheese = getClosestCheese(currentState.mouse, currentState.cheese);
			Point newMousePosition = nextMouseMove(currentState.mouse, closestCheese);
			// Add all possible moves that the cat can perform and populate the queue with
			// the new states
			for (Integer[] move : CAT_MOVESET) {
				if (isValidMove(currentState.cat, move, currentState.boardSize)) {
					// Create the new instance state
//					BoardPiece[][] b = new BoardPiece[currentState.boardSize][currentState.boardSize];
//					for (int i = 0; i < currentState.boardSize; i++) {
//						for (int j = 0; j < currentState.boardSize; j++) {
//							b[i][j] = currentState.board[i][j];
//						}
//					}
					Point cat = (Point) currentState.cat.clone();
					Point mouse = (Point) currentState.mouse.clone();
					List<Point> cheese = new ArrayList<Point>();
					for (Point c : currentState.cheese) {
						cheese.add(c);
					}
					GameBoardState nextState = new GameBoardState(currentState, currentState.boardSize, cat, mouse,
							cheese);

					Point catPos = new Point(currentState.cat.x + move[1], currentState.cat.y + move[0]);
					// If the cat position ans the mouse position are equal, we have found the
					// solution
					nextState.updateMouse(newMousePosition);
					nextState.updateCat(catPos);
					if (catPos.equals(newMousePosition)) {
						solution = nextState;
						break;
					}

					q.add(nextState);
				}
			}
		}
		System.out.println("Number of iterations/expansions " + numIterations);

		// Add the solution moves for the cat into a queue.
		while (solution != null) {
			moves.add(solution);
			solution = solution.parent;
//			System.out.println(solution.parent);
		}

		return moves;
	}

	public LinkedList<GameBoardState> DFS(GameBoardState start) {
		Stack<GameBoardState> q = new Stack<>();
		q.add(start);
		resetNumIterations();
		GameBoardState solution = null;
		LinkedList<GameBoardState> moves = new LinkedList<>();
		while (solution == null) {

			incrementIteration();
			GameBoardState currentState = q.pop();
			if (currentState == null) {
				System.out.println("Finished.");
				solution = currentState;
				break;
			}
			if ((currentState.cheese.size()) == 0) {
				continue;
			}
			Point closestCheese = getClosestCheese(currentState.mouse, currentState.cheese);
			Point newMousePosition = nextMouseMove(currentState.mouse, closestCheese);
			// Add all possible moves that the cat can perform and populate the queue with
			// the new states
			for (Integer[] move : CAT_MOVESET) {
				if (isValidMove(currentState.cat, move, currentState.boardSize)) {
					// Create the new instance state
//					BoardPiece[][] b = new BoardPiece[currentState.boardSize][currentState.boardSize];
//					for (int i = 0; i < currentState.boardSize; i++) {
//						for (int j = 0; j < currentState.boardSize; j++) {
//							b[i][j] = currentState.board[i][j];
//						}
//					}
					Point cat = (Point) currentState.cat.clone();
					Point mouse = (Point) currentState.mouse.clone();
					List<Point> cheese = new ArrayList<Point>();
					for (Point c : currentState.cheese) {
						cheese.add(c);
					}
					GameBoardState nextState = new GameBoardState(currentState, currentState.boardSize, cat, mouse,
							cheese);

					Point catPos = new Point(currentState.cat.x + move[1], currentState.cat.y + move[0]);
					// If the cat position ans the mouse position are equal, we have found the
					// solution
					nextState.updateMouse(newMousePosition);
					nextState.updateCat(catPos);
					if (catPos.equals(newMousePosition)) {
						solution = nextState;
						break;
					}
					q.push(nextState);
				}
			}
		}
		System.out.println("Number of iterations/expansions " + numIterations);
		while (solution != null) {
			moves.add(solution);
			solution = solution.parent;
		}

		return moves;
	}

	public LinkedList<GameBoardState> AStarSearch(GameBoardState start, String heuristicType) {
		resetNumIterations();
		start.cost = 0d;
		PriorityQueue<GameBoardState> open = null; 
		if (heuristicType.equals("EuclideanDistance1")){
			open = new PriorityQueue<>(8, new EuclideanDistance1Comparator());
		}else if (heuristicType.equals("EuclideanDistance2")) {
			open = new PriorityQueue<>(8, new EuclideanDistance2Comparator());
		}else if(heuristicType.equals("EuclideanDistanceAverage")) {
			open = new PriorityQueue<>(8, new EuclideanHeuristicAverageComparator());
		}
		open.add(start);
		
		HashSet<GameBoardState> closed = new HashSet<>();
		GameBoardState solution = null;
		LinkedList<GameBoardState> moves = new LinkedList<>();

		int count = 0;
//		int iterations = 0;
		while (!open.isEmpty() && solution  == null) {
			GameBoardState currentState = open.poll(); // get the next best node
			incrementIteration();
			closed.add(currentState); //Add the state to closed (visited now)
			if (currentState == null) {
				System.out.println("Finished.");
				solution = currentState;
				break;
			}
			if (currentState.cheese.isEmpty()) {
				continue;
			}
			Point closestCheese = getClosestCheese(currentState.mouse, currentState.cheese);
			Point newMousePosition = nextMouseMove(currentState.mouse, closestCheese);
			// Generate the 8 successors
			for (Integer[] move : CAT_MOVESET) {
				if (isValidMove(currentState.cat, move, currentState.boardSize)) {
					// Create the new instance state
//					BoardPiece[][] b = new BoardPiece[currentState.boardSize][currentState.boardSize];
//					for (int i = 0; i < currentState.boardSize; i++) {
//						for (int j = 0; j < currentState.boardSize; j++) {
//							b[i][j] = currentState.board[i][j];
//						}
//					}
					Point cat = (Point) currentState.cat.clone();
					Point mouse = (Point) currentState.mouse.clone();
					List<Point> cheese = new ArrayList<Point>();
					for (Point c : currentState.cheese) {
						cheese.add(c);
					}
					// The cost is the # of steps/iteration to reach this state
					// So the nextState is just the currentState.cost + 1
					GameBoardState nextState = new GameBoardState(currentState, currentState.boardSize, cat, mouse,
							cheese, currentState.cost + 1);

					Point catPos = new Point(currentState.cat.x + move[1], currentState.cat.y + move[0]);
					// If the cat position and the mouse position are equal, we have found the
					// solution
					nextState.updateMouse(newMousePosition);
					nextState.updateCat(catPos);

					// If the mouse has eaten all the cheese, this
					// state is  incorrect.
					if(nextState.cheese.isEmpty()) {
						continue;
					}
					// We have found the solution if cat position == mouse position
					if (catPos.equals(newMousePosition)) {
						solution = nextState;
						break;
					}
					
					if(!closed.contains(nextState)) {
						open.add(nextState);
					}

					open.add(nextState);
					count++;
					if (count == 4) {
						break;
					}
				}
			}
		}
		while (solution != null) {
			moves.add(solution);
			solution = solution.parent;
		}
//		solution.display();
		System.out.println("iterations: " + numIterations);
		return moves;

	}
	
	public Double EvaluateEuclideanHeuristic(GameBoardState g) {
		// f(n) = g(n) + h(n)
		// g(n) - cost so far to reach n
		// h(n) - estimated cost from n to goal
		
		// Calculate the euclidean distance from the current mouse position and
		// the cat.
		Point nextCheese1 = Agent.getClosestCheese(g.mouse, g.cheese);
		Point nextMouse1 = Agent.nextMouseMove(g.mouse, nextCheese1);
		
		// The estimated cost to reach from the current cat pos 
		// to the mouse is the EuclideanDistance between them divided by 
		// the distance a cat can travel. (this will get us the # of steps it should take to reach
		// the mouse.
		Double h = EuclideanDistance(g.cat, nextMouse1)/CAT_EUCLIDEAN_DISTANCE;
		return g.cost + h;
	}
	

	class EuclideanDistance1Comparator implements Comparator<GameBoardState> {

		@Override
		public int compare(GameBoardState o1, GameBoardState o2) {
			Double d1 = o1.EvaluateEuclideanHeuristic();
			Double d2 = o2.EvaluateEuclideanHeuristic();
//			System.out.println(d1 + " " + d2);
			if (d1 > d2) {
				return 1;
			} else if (d2 < d1) {
				return -1;
			} else {
				return 0;
			}
		}

	}
	
	class EuclideanDistance2Comparator implements Comparator<GameBoardState> {

		@Override
		public int compare(GameBoardState o1, GameBoardState o2) {
			Double d1 = o1.EvaluateEuclideanHeuristic2();
			Double d2 = o2.EvaluateEuclideanHeuristic2();
//			System.out.println(d1 + " " + d2);
			if (d1 > d2) {
				return 1;
			} else if (d2 < d1) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	class EuclideanHeuristicAverageComparator implements Comparator<GameBoardState> {

		@Override
		public int compare(GameBoardState o1, GameBoardState o2) {
			Double d1 = o1.EvaluateEuclideanHeuristicAverage();
			Double d2 = o2.EvaluateEuclideanHeuristicAverage();
//			System.out.println(d1 + " " + d2);
			if (d1 > d2) {
				return 1;
			} else if (d2 < d1) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
