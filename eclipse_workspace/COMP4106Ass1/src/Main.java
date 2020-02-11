import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class Main {
	// Movesets for a mouse
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

	public static Double EuclideanDistance(Point p1, Point p2) {
//		System.out.println("point1: "+p1);
//		System.out.println("point2: " +p2);
		double dx = p2.x - p1.x;
		double dy = p2.y - p1.y;

//		System.out.println("dx: "+dx+ " dy " + dy);
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static Point getClosestCheese(Point mouse, List<Point> cheese) {
		Point closest = cheese.get(0);
		Double curr = EuclideanDistance(mouse, cheese.get(0));
		for (Point c : cheese) {
//			System.out.println("Mouse " + mouse);
//			System.out.println("Cheese " + c);
			Double d = EuclideanDistance(mouse, c);
//			System.out.println(d);
			if (d < curr) {
				closest = c;
			}
			
		}
//		System.out.println("closest " + closest);
		
		return closest;
	}

	public static Point nextMouseMove(Point mouse, Point cheese) {
		Double dx = (double) (cheese.x - mouse.x);
		Double dy = (double) (cheese.y - mouse.y);
		Double angle = Math.atan2(dy, dx);
		angle = 180/Math.PI * angle;
//		 round angle to nearest orientation
		Integer rounded_angle = (int) (Math.round(angle/45) * 45);
		Integer[] mouseMove = MOUSE_ANGLE_MOVESET_MAP.get(rounded_angle);
		Point newPoint= new Point(mouse.x + mouseMove[1], mouse.y + mouseMove[0]);
//		Point newPoint = null;
		
//		if (dx == 0) {
//			if (dy > 0) {
//				newPoint = new Point(mouse.x, mouse.y + 1);
//			} else if (dy < 0) {
//				newPoint = new Point(mouse.x, mouse.y - 1);
//			}
//		} else if (dy == 0) {
//			if (dx > 0) {
//				newPoint = new Point(mouse.x + 1, mouse.y);
//			} else if (dx < 0) {
//				newPoint = new Point(mouse.x - 1, mouse.y);
//			}
//		} else if (dy / dx > 0) {
//			if (dy < 0 && dx < 0) {
//				newPoint = new Point(mouse.x - 1, mouse.y - 1);
//			} else if (dy > 0 && dx > 0){
//				newPoint = new Point(mouse.x + 1, mouse.y + 1);
//			}
//		}else if (dy / dx < 0) {
//			if (dy < 0) {
//				newPoint = new Point(mouse.x + 1, mouse.y - 1);
//			} else if (dx < 0){
//				newPoint = new Point(mouse.x - 1, mouse.y + 1);
//			}
//		}

		return newPoint;
	}

	public static Boolean isValidMove(Point cat, Integer[] move, Integer boardSize) {
		Integer y = cat.y+move[0];
		Integer x = cat.x+move[1];
//		System.out.println("isValue " + y + " " + x );
		if ((y < 0) || (y > boardSize-1) || (x < 0) || (x > boardSize-1)) {
			return false;
		}
		return true;
	}

	public static LinkedList<GameBoardState> BFS(GameBoardState start) {
		
		Queue<GameBoardState> q = new LinkedList<>();
		q.add(start);
		Integer iteration = 0;
		GameBoardState solution = null;
		LinkedList<GameBoardState> moves = new LinkedList<>();
		while (solution == null) {
//			System.out.println(String.format("Iteration %d", iteration));
			GameBoardState currentState = q.poll();
			
			if (currentState == null) {
				System.out.println("Finished.");
				solution = currentState;
				break;
			}
			
//			currentState.display();
			Point closestCheese = getClosestCheese(currentState.mouse, currentState.cheese);
//			System.out.println(closestCheese);
			Point newMousePosition = nextMouseMove(currentState.mouse, closestCheese);
			// Add all possible moves that the cat can perform and populate the queue with
			// the new states
			for (Integer[] move : CAT_MOVESET) {
				if (isValidMove(currentState.cat, move, currentState.boardSize)) {
					
//					GameBoardState nextState = GameBoardState.deepCopy(currentState);
					//Create the new instance state
					BoardPiece[][] b = new BoardPiece[currentState.boardSize][currentState.boardSize];
					for(int i = 0 ; i < currentState.boardSize ; i++) {
						for(int j = 0 ; j < currentState.boardSize; j++) {
							b[i][j] = currentState.board[i][j];
						}
					}
					Point cat = (Point) currentState.cat.clone();
					Point mouse = (Point) currentState.mouse.clone();
					List<Point> cheese = new ArrayList<Point>();
					for(Point c : currentState.cheese) {
						cheese.add(c);
					}
					GameBoardState nextState = new GameBoardState(currentState, currentState.boardSize, b,cat,mouse,cheese);
					
					Point catPos = new Point(currentState.cat.x + move[1], currentState.cat.y + move[0]);
					//If the cat position ans the mouse position are equal, we have found the solution

//					System.out.println(catPos.y  + " " + catPos.x);
					nextState.updateMouse(newMousePosition);
					nextState.updateCat(catPos);
					if(catPos.equals(newMousePosition)) {
						solution = nextState;
						break;
					}
				
					q.add(nextState);
				}
			}
			iteration++;
//			currentState.display();
		}
		System.out.println("Number of iterations/expansions " + iteration);
		
		
		// Add the solution moves for the cat into a queue.
		while(solution!=null) {
			moves.add(solution);
			solution = solution.parent;
//			System.out.println(solution.parent);
		}
		
		return moves;
	}
	
	public static void runBFS(LinkedList<GameBoardState> sol) {
		Integer step  = 1;
		while(!sol.isEmpty()) {
			GameBoardState s = sol.pollLast();
			System.out.println("Step: " + step);
			s.display();
			step++;
		}
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GameBoardState start = new GameBoardState();
		start.init();
//		start.display();
//		System.out.println(start.cheese);
//		Point closestCheese = getClosestCheese(start.mouse, start.cheese);

//		System.out.println(closestCheese);
//		Point newMouse = nextMouseMove(start.mouse, closestCheese);

//		System.out.println(String.format("Current Mouse: %s, Next Position Mouse: %s", start.mouse, newMouse));
//		start.board[newMouse.y][newMouse.x] = BoardPiece.MOUSE;
//		start.display();
		LinkedList<GameBoardState> solution = BFS(start);
		runBFS(solution);
	}

}
