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
import java.util.Set;
import java.util.Stack;

public class Main {



	public static void runBFS(LinkedList<GameBoardState> sol) {
		LinkedList<GameBoardState> gameSequence = new LinkedList<GameBoardState>();
		ArrayList<Point> catMoves = new ArrayList<Point>();
		while (!sol.isEmpty()) {
			GameBoardState s = sol.pollLast();
			gameSequence.add(s);
			catMoves.add(s.cat);
		}
		Integer step = 0;
		System.out.println("Cat Moves sequence: " + catMoves);
		while (!gameSequence.isEmpty()) {
			GameBoardState s = gameSequence.poll();
			System.out.println("Step: " + step);
			s.display();
			step++;
		}
	}

	public static void runDFS(LinkedList<GameBoardState> sol) {
		Integer step = 0;
		while (!sol.isEmpty()) {
			GameBoardState s = sol.pollLast();
			System.out.println("Step: " + step);
			s.display();
			step++;
		}
	}
	
	public static void runAStar(LinkedList<GameBoardState> sol) {
		Integer step = 0;
		while (!sol.isEmpty()) {
			GameBoardState s = sol.pollLast();
			System.out.println("Step: " + step);
			s.display();
			step++;
		}
	}	
	
	public static void runSearch(LinkedList<GameBoardState> sol) {
		LinkedList<GameBoardState> gameSequence = new LinkedList<GameBoardState>();
		ArrayList<Point> catMoves = new ArrayList<Point>();
		while (!sol.isEmpty()) {
			GameBoardState s = sol.pollLast();
			gameSequence.add(s);
			catMoves.add(s.cat);
		}
		Integer step = 0;
		System.out.println("Cat Moves sequence: " + catMoves);
		while (!gameSequence.isEmpty()) {
			GameBoardState s = gameSequence.poll();
			System.out.println("Step: " + step);
			s.display();
			step++;
		}
	}
	
	public static void defaultBoardStateTest() {
		GameBoardState start = new GameBoardState();
		start.init(true, 12);
//		start.init(false, 30);
		System.out.println("START BOARD:");
		start.display();
		System.out.println("SEARCHING...");
		
		Agent agent = new Agent();
//		System.out.println("BFS Solution");
//		LinkedList<GameBoardState> solutionBFS = agent.BFS(start);
//		System.out.println("Total steps: " + (solutionBFS.size()-1));
//		runSearch(solutionBFS);
//		System.out.println("DFS Solution");
//		LinkedList<GameBoardState> solutionDFS = agent.DFS(start);
//		System.out.println("Total steps: " + (solutionDFS.size()-1));
//		runSearch(solutionDFS);
//		
		System.out.println("Euclidean Distance 1 Solution");
		LinkedList<GameBoardState> solution1 = agent.AStarSearch(start, "EuclideanDistance1");
		System.out.println("Number of Iterations: "+agent.getNumIterations()+ " Total steps: " + (solution1.size()-1));
		runSearch(solution1);
		
		System.out.println("Euclidean Distance 2 Solution");
		LinkedList<GameBoardState> solution2 = agent.AStarSearch(start, "EuclideanDistance2");
		System.out.println("Number of Iterations: "+agent.getNumIterations()+ " Total steps: " + (solution2.size()-1));
		runSearch(solution2);

		System.out.println("Average Euclidean Distance Solution");
		LinkedList<GameBoardState> solution3 = agent.AStarSearch(start, "EuclideanDistanceAverage");
		System.out.println("Number of Iterations: "+agent.getNumIterations()+ " Total steps: " + (solution3.size()-1));
		runSearch(solution3);		
	}
	
	public static Double average(int [] arr) {
		Double avg = 0.0;
		int length = arr.length;
		for(int x: arr) {
			avg += x;
		}
		return avg/length;
	}
	public static Double standardDeviation(int [] arr, double avg) {
		Double std = 0.0;
		int length = arr.length;
		for(int x: arr) {
			std += (x - avg)*(x - avg);
		}
		std  = std/length;
		return Math.sqrt(std);
	}

	public static void averageSearch(String searchType, List<GameBoardState> states) {
//		List<GameBoardState> states = GameBoardState.generateStates(numTrials, 12);
		int numTrials = states.size();
		int [] iterations = new int[numTrials];
		int [] moves = new int[numTrials];
		double avg = 0.0;
		double std = 0.0;

		Agent agent = new Agent();
		for (int i = 0 ; i < numTrials; i++) {
			LinkedList<GameBoardState> solution;
			if(searchType.equals("BFS")) {
				solution = agent.BFS(states.get(i));
				
			}else if(searchType.equals("DFS")) {
				solution = agent.DFS(states.get(i));
				
			}
			else {
				solution = agent.AStarSearch(states.get(i), searchType);
			}
			iterations[i] = agent.getNumIterations();
			moves[i] = solution.size();
		}
		
		
		System.out.println(searchType+ ": Average number of iterations - " + average(iterations) + " \tstd - " + standardDeviation(iterations, average(iterations)));
		System.out.println(searchType+ ": Average number of moves - " + average(moves) + " \tstd - " + standardDeviation(moves, average(moves)));
		
	}
	
	public static void avgBFSandDFS() {
		int numTrials = 5;
		int [] iterations = new int[numTrials];
		double avg = 0.0;
		double std = 0.0;

		Agent agent = new Agent();
		for (int i = 0 ; i < numTrials; i++) {
			GameBoardState  start = new GameBoardState();
//			start.init(true, 12);
			start.init(false, 12);
			LinkedList<GameBoardState> solution = agent.BFS(start);
			iterations[i] = agent.getNumIterations();
			runSearch(solution);
		}
		
		for (int x : iterations) {
			avg += x;
		}
		avg /= numTrials;
		for (int x : iterations) {
			std += Math.pow((x - avg), 2);
		}
		std = Math.sqrt(std/numTrials);
		
		System.out.println("Average number of iterations: " + avg + " std: " + std);
		
	}


	/**
	 * Questions
	 * Which search worked best? 
	 * The A star search worked best on the 12x12 default board.  Using the heuristic 1
	 * 
	 * Which heuristics did you use? 
	 * Euclidean Distance between curr c and m
	 * Euclidean Distance between curr c and predicted m
	 * Average of above
	 * 
	 * Why did you choose these heuristics?
	 * Because simple and intuitive
	 * 
	 * Does the combination of the two heuristics work better or worse than they do individually
	 * Worse. H1 worked best
	 * 
	 * How well do the searches work if you increase the size of the board to 30x30, 50x50
	 * The average heuristic seems to work best in terms of finding a better solution
	 * while h1 is fastest at finding a solution
	 * the DFS takes exponentially longer to find solutions
	 * and BFS is also extremely bad at finding the optimal solution
	 * 
	 * How many nodes are searched for each of hte searches on average with respective deviation
	 * For the default board size of 12 on 5 randomly generated boards we have
	 *  On average BFS took 333 std~533
	 *  			DFS took 3353 std~ 6000
	 *  			H1 took 195 std~  309
	 *  			H2 took 363  std~ 584
	 *  			H_avg took 251 std~ 374
	 * 
	 * What is the average number of moves required for each type of search with respeictve deviation?
	 * For the same board we found in the above question
	 * On average BFS took 5.0 moves ~ 1.414
	 * 			  DFS took 15.0 moves ~ 4.56
	 * 			  H1 took  5.4 moves ~ 1.62
	 * 			  H2 took 5.4 moves ~ 1.62
	 * 			
	 * Which search works best if you increase speed of mouse to two steps per turn? 3 steps?
	 * If the mouse moves 2 steps per turn, h2 works best.
	 * If the mouse moves 3 steps per turn, h2 is still the best. in terms of finding the quickest solution
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		defaultBoardStateTest();
		List<GameBoardState> states = GameBoardState.generateStates(5, 12);
		averageSearch("BFS", states);
		averageSearch("DFS", states);
		averageSearch("EuclideanDistance1", states);
		averageSearch("EuclideanDistance2", states);
		averageSearch("EuclideanDistanceAverage", states);
//		avgBFSandDFS();
	}

}
