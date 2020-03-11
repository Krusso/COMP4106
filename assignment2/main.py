
from typing import Dict, Tuple, Sequence
from enum import Enum, auto
import random
from colorama import init, Fore, Style, Back
import copy
import math
import numpy as np

init(autoreset=True)

class HOLETYPE(Enum):
    HOLE = auto()
    MANCALA = auto()

class PLAYER(Enum):
    ONE = auto()
    TWO = auto()

class GAMETYPE(Enum):
    PVC = auto()
    CVC = auto()

class HEURISTIC(Enum):
    TOTAL_PIECES_IN_MANCALA = auto()
    TOTAL_PIECES_ON_MY_SIDE = auto()

def other(player:PLAYER):
    return PLAYER.ONE if player == PLAYER.TWO else PLAYER.TWO

# Global declarations
# BOARD_PLAYER = [1,1,1,2,2,2,2,2,2,1,1,1,1,1,1,2,2,2,2,2,2,1,1,1]
BOARD_PLAYER = [PLAYER.ONE, PLAYER.ONE, PLAYER.ONE, 
                PLAYER.TWO, PLAYER.TWO, PLAYER.TWO, PLAYER.TWO, PLAYER.TWO, PLAYER.TWO,
                PLAYER.ONE, PLAYER.ONE, PLAYER.ONE, PLAYER.ONE, PLAYER.ONE, PLAYER.ONE, 
                PLAYER.TWO, PLAYER.TWO, PLAYER.TWO, PLAYER.TWO, PLAYER.TWO, PLAYER.TWO,
                PLAYER.ONE, PLAYER.ONE, PLAYER.ONE]

BOARD  = np.array(BOARD_PLAYER)
PLAYER_ONE_INDEX  = np.where(BOARD ==  PLAYER.ONE)
PLAYER_TWO_INDEX  = np.where(BOARD ==  PLAYER.TWO)


PLAYER_MANCALA = {PLAYER.ONE: [0, 12],
                    PLAYER.TWO: [6, 18]}

# Indices of the board (which is a list of size 24) that are valid moves for player1 and player2
PLAYER_MOVESET =  {PLAYER.ONE :  {1, 2, 9, 10, 11, 13, 14, 21, 22, 23} ,
                    PLAYER.TWO : {3, 4, 5, 7, 8, 15, 16, 17, 19, 20}}



HOLE_NEIGHBOUR = [[1, 23], [2, 23], [1, 3, 22], [2, 4, 9,21], [3,5,8], [4,7], [5,7], [5,8], [4,7,9],[3,8,10,15],[9,11,14],[10,13],[11,13],[11,14],[10,13,15],[9,14,16,21],[15,17,20],[16,19],[17,19],[17,20],[16,19,21],[3,15,20,22],[2,21,23],[1,22]]

# Indices of the board used to display the board for each row
ROWS = [[0], [1, 23], [2, 22], [5, 4, 3, 21, 20, 19], [6, 18] ,[7, 8, 9, 15,16,17], [10, 14], [11,13], [12]]

# Cache the states that have been seen.
STATE_CACHE = {}

class BoardHole:
    def __init__(self, holetype=None, stones=None):
        self.type = holetype
        self.stones = stones

    def display(self):
        print("{} {}".format(self.type, self.stones))

class GameState:
    def __init__(self):
        # The board is a list of BoardHole
        self.board = None


    def generate_board(self, stones=4, default=False):
        BOARD_SIZE = 24
        self.board = []
        if default:
            self.board = [8,3,2,0,1,0,
                          11,0,0,6,5,5,
                          0,5,5,5,5,5,
                          4,7,7,7,7,2]
                          
            
            # self.board = [0,0,2,1,0,0,
            #               0,0,0,0,0,0,
            #               0,0,0,0,0,0,
            #               0,0,1,1,0,0]
        else:
            for i in range(BOARD_SIZE):
                self.board.append(stones if i%6 != 0 else 0)

    def is_game_over(self):
        numStones = sum([self.board[i] for i in PLAYER_MOVESET[PLAYER.ONE]])
        # print(numStones)
        if numStones != 0:
            return False
        numStones = sum([self.board[i] for i in PLAYER_MOVESET[PLAYER.TWO]])
        if numStones != 0:
            return False
        return True

    def display(self):
        # s = ""
        spacing = 3
        # self.board[6].stones = 20
        for level, r in enumerate(ROWS):
            s = ""
            spaces = int((6 - len(r))/2) * spacing
            for c in r:
                b = self.board[c]
                if c % 6 == 0:
                    s = s + Back.WHITE
                s = s + Fore.BLUE + str(b) if BOARD_PLAYER[c] == PLAYER.ONE else s+ Fore.RED +str(b)
                s = s + Style.RESET_ALL + "  "
                if level == 4:
                    num_space = (4 * spacing + 1) - len(str(self.board[c]))
                    # print(len(''.join([str(self.board[n].stones) for n in r])))
                    # print([str(self.board[n].stones) for n in r])
                    s = s + ''.join([" " for _ in range(num_space)])
            if level != 4 :
                s = ''.join([" " for _ in range(spaces)]) + s
            print(s)
        print()

class Node:
    def __init__(self, gamestate: GameState, depth, parent=None):
        self.depth = depth
        self.parent = parent
        self.state = gamestate
        self.moveset = []

    def display(self):
        self.state.display()

    def __eq__(self, other):
        return (
            self.__class__ == other.__class__ and
            self.depth == other.depth and
            self.state.board == other.state.board and
            self.parent == other.parent
        )

    def __hash__(self):
        return 0

    def __lt__(self, other):
        return (
            self.depth < other.depth
        )

class GameController:
    def __init__(self, gtype,stones=4, default=False):
        self._start = GameState()
        self._start.generate_board(stones=stones,default=default)
        self.current_state = Node(self._start, depth = 0)
        self.gametype = gtype
        self.current_player = PLAYER.ONE
    
    def calculate_score(self):
        p1 = 0
        p2 = 0
        for i, p in enumerate(BOARD_PLAYER):
            if p == PLAYER.ONE:
                p1 += self.current_state.state.board[i]
            else:
                p2 += self.current_state.state.board[i]
        return p1, p2

    def is_valid_move(self, player: PLAYER, move: int):
        if player == PLAYER.ONE:
            return move in PLAYER_MOVESET[PLAYER.ONE]
        else:
            return move in PLAYER_MOVESET[PLAYER.TWO]

    def is_game_over(self):
        numStones = sum([self.current_state.state.board[i] for i in PLAYER_MOVESET[PLAYER.ONE]])
        # print(numStones)
        if numStones == 0:
            return True
        numStones = sum([self.current_state.state.board[i] for i in PLAYER_MOVESET[PLAYER.TWO]])
        if numStones == 0:
            return True
        return False

    def switch(self):
        if self.current_player == PLAYER.ONE:
            self.current_player = PLAYER.TWO
        else:
            self.current_player = PLAYER.ONE

    def play(self):
        self.display()
        print("Start Game {}".format(self.gametype))
        turn = 0
        if self.gametype == GAMETYPE.PVC:
            player_input = ""
            while not self.is_game_over():
                print("{} turn".format(self.current_player))
                if self.current_player == PLAYER.ONE:
                    pos = int(input("Input hole position {} {}: ".format(self.current_player, PLAYER_MOVESET[self.current_player])))
                    counter_clockwise = input("Counter-clockwise? [y/n]: ")
                    counter_clockwise = True if counter_clockwise == 'y' else False
                    player = PLAYER.ONE
                    # play given player input.
                    newState = copy.deepcopy(self.current_state)
                    newState.parent = self.current_state
                    newState.depth += 1
                    hand = newState.state.board[pos]
                    newState.state.board[pos] = 0
                    itr = 0
                    curr_pos = None
                    while hand > 0:
                        # The current hole position for which we will be dropping a stone into
                        curr_pos = (pos + itr + 1) % 24 if counter_clockwise else (pos - itr - 1) % 24
                        itr += 1
                        # check if we are over a mancala (a position that is divisible by 6)
                        # if it's the opponent's mancala, then we have 2 options, skip or take 2
                        if BOARD_PLAYER[curr_pos] != player and curr_pos % 6 == 0:
                            skip = input("Skip mancala? [y/n]: ")
                            skip = True if skip == 'y' else False
                            if skip:
                                continue
                            else:
                                newState.state.board[curr_pos] += 1  
                                # now we take at most 2 stones from opponent's mancala (in this position)
                                # and we place those stones into our previous mancala
                                taken = 0
                                if newState.state.board[curr_pos] > 1:
                                    newState.state.board[curr_pos] -= 2
                                    newState.state.board[(curr_pos-6)%24] += 2
                                else:
                                    newState.state.board[curr_pos] -= 1
                                    newState.state.board[(curr_pos-6)%24] += 1
                        else:
                            newState.state.board[curr_pos] += 1  
                        hand -= 1  

                    newState.display()
                    # if previous hole was empty, then we can choose the neighbouring pieces to be added into our mancala
                    if newState.parent.state.board[curr_pos] == 0 and self.current_player == BOARD_PLAYER[curr_pos]:
                        neighbour = HOLE_NEIGHBOUR[curr_pos]
                        p_select = int(input("Select a neighbour to be added into your mancala {}: ".format(neighbour)))
                        mancala = int(input("Select which mancala to add too {}: ".format(PLAYER_MANCALA[self.current_player])))
                        newState.state.board[mancala] += newState.state.board[p_select]
                        newState.state.board[p_select] = 0
                        newState.display()

                    self.current_state = newState

                    # switch player turn if the last piece did not land in his own mancala
                    if curr_pos not in PLAYER_MANCALA[self.current_player]:
                        self.switch()

                else:
                    h = HEURISTIC.TOTAL_PIECES_IN_MANCALA if self.current_player == PLAYER.ONE else HEURISTIC.TOTAL_PIECES_ON_MY_SIDE
                    p1_score, p2_score = self.calculate_score()
                    score, nextState, nc = minimax(self.current_state, 2, True, self.current_player, ALPHA, BETA, 0, heuristic=h)
                    print("{} turn {}, Nodes searched: {}, P1: {}, P2: {}, Move: {}".format(self.current_player, turn, nc, p1_score, p2_score, nextState.moveset))
                    del STATE_CACHE[self.current_state]
                    self.current_state = nextState
                    self.display()
                    self.switch()
            
        else:
            depth = 4
            # self.display()

            while not self.is_game_over():
                self.display()
                nodeCount = 0
                # score, nextState, nc = minimax(self.current_state, 2, True, self.current_player, None, None, nodeCount)
                h = HEURISTIC.TOTAL_PIECES_IN_MANCALA if self.current_player == PLAYER.ONE else HEURISTIC.TOTAL_PIECES_ON_MY_SIDE
                # h = HEURISTIC.TOTAL_PIECES_ON_MY_SIDE
                score, nextState, nc = minimax(self.current_state, DEPTH, True, self.current_player, ALPHA, BETA, nodeCount, heuristic=h)
                p1_score, p2_score = self.calculate_score()
                print("{} turn {}, Nodes searched: {}, P1: {}, P2: {}, Move: {}".format(self.current_player, turn, nc, p1_score, p2_score, nextState.moveset))
                turn += 1
                self.current_player = other(self.current_player)
                del STATE_CACHE[self.current_state]
                self.current_state = nextState
                # print(self.is_game_over())

            self.display()
                
    def display(self):
        self.current_state.display()


PLAYER_MANCALA_POSITION = {PLAYER.ONE: [0, 12, 24],
                           PLAYER.TWO: [6, 18]}

def closer_mancala(player, position):
    p = PLAYER_MANCALA_POSITION[player]
    x = [(abs(i-position), i) for i in p]
    closer = min(x)
    return closer[1] if closer[1] != 24 else 0

# generate all possible moves for a given state
def production_system(node: Node, player: PLAYER, real_parent: Node = None):
    arr = set()
    valid_moves = [m for m in PLAYER_MOVESET[player] if node.state.board[m] != 0]
    # direction we go in (either counter clockwise or clockwise)
    directions = [1, -1]
    # whether we are skipping or not if a we are over an opponent's mancala
    skips = [True, False]
    for skip in skips:
        for direction in directions:
            for m in valid_moves:
                moveset = [skip, direction, m]
                new_node = copy.deepcopy(node)
                if real_parent == None:
                    new_node.parent = node
                    new_node.moveset = []
                    new_node.moveset.append(moveset)
                else:
                    new_node.parent = real_parent
                    new_node.moveset.append(moveset)
                new_node.depth = new_node.parent.depth + 1

                hand = new_node.state.board[m]
                new_node.state.board[m] = 0 # we remove all the stones at this position
                itr = 0
                # print("skip: {} direction: {} | move: {} hand: {}".format(skip, direction, m, hand))
                curr_pos = None
                while hand > 0:
                    # The current hole position for which we will be dropping a stone into
                    curr_pos = (m + itr*direction + direction) % 24
                    new_node.state.board[curr_pos] += 1 # drop a stone into this hole
                    hand -= 1
                    itr += 1
                    # if player reaches an opponent's mancala
                    if player != BOARD_PLAYER[curr_pos] and curr_pos % 6 == 0:
                        # if we skip the current mancala, we must subtract the stone we just placed into it
                        if skip:
                            new_node.state.board[curr_pos] -= 1
                            hand += 1
                        else:
                            # if we are not skipping, take at most 2 stones from this mancala and add it to our previous mancala
                            # there will always be at least 1 because we jsut droppped on into the hole
                            stones_to_add = 2 if new_node.state.board[curr_pos] >= 2 else 1
                            new_node.state.board[curr_pos] -= stones_to_add
                            new_node.state.board[(curr_pos - 6)%24] += stones_to_add

                # new_node.display()
                nodes = set() # keep track of all the split possibilities that can arise from the rules
                # if the last position (curr_pos) was previously empty, the player can choose to take one of its adjacent holes
                if node.state.board[curr_pos] == 0 and player == BOARD_PLAYER[curr_pos]:
                    # take the adjacent neighbours
                    for nbr in HOLE_NEIGHBOUR[curr_pos]:
                        # we only care about this neighbour if it contains at least 1 stone:
                        if new_node.state.board[nbr] > 0:
                            node_split = copy.deepcopy(new_node)
                            node_split.state.board[closer_mancala(player, curr_pos)] += node_split.state.board[nbr]
                            node_split.state.board[nbr] = 0
                            # nodes.add((m, node_split))
                            nodes.add(node_split)
                else:
                    # nodes.add((m, new_node))
                    nodes.add(new_node)

                # if the last placed piece was on his own mancala, we can play again.
                if curr_pos in PLAYER_MANCALA[player]:
                    my_nodes = set()
                    for n in nodes:
                        if n.state.is_game_over():
                            my_nodes.add(n)
                        else:
                            my_nodes.update(production_system(n, player, node))
                    nodes = my_nodes
                arr.update(nodes)
    return arr

def get_children(node, player):
    if node in STATE_CACHE:
        return STATE_CACHE[node]
    children = production_system(node, player)
    STATE_CACHE[node] = children
    return STATE_CACHE[node]


# a simple scoring method that returns the amount of stones in this player's mancalas
def score(node: Node, player: PLAYER, heuristic: HEURISTIC):
    s = 0
    if heuristic == HEURISTIC.TOTAL_PIECES_IN_MANCALA:
        for mancala in PLAYER_MANCALA[player]:
            s += node.state.board[mancala]
    elif heuristic == HEURISTIC.TOTAL_PIECES_ON_MY_SIDE:
        b = np.array(node.state.board)
        s = b[PLAYER_ONE_INDEX].sum() if player == PLAYER.ONE else b[PLAYER_TWO_INDEX].sum()
    return s


def minimax(node, depth, maximizingPlayer: bool, player:PLAYER, alpha, beta, nodeCount, heuristic = HEURISTIC.TOTAL_PIECES_IN_MANCALA):
    if depth == 0:
        return score(node, player, heuristic), node, nodeCount + 1
    if maximizingPlayer:
        maxEval = -math.inf
        maxState = None
        children = list(get_children(node, player))
        # print(len(children))
        # edge case for if this is the leaf and the max depth has not been reached yet
        if len(children) == 0:
            return score(node, player, heuristic), node, nodeCount
        np.random.shuffle(children)
        for n in children:
            evaluation, _, nodeCount = minimax(n, depth - 1, False, player, alpha, beta, nodeCount)
            if evaluation > maxEval:
                maxEval = evaluation
                maxState = n
            if alpha != None:
                if alpha < maxEval:
                    alpha = maxEval
                if beta <= alpha:
                    break
        return maxEval, maxState, nodeCount
    else:
        minEval = math.inf
        minState = None
        children = list(get_children(node, other(player)))
        # edge case for if this is the leaf and the max depth has not been reached yet
        if len(children) == 0:
            return score(node, player, heuristic), node, nodeCount

        np.random.shuffle(children)
        for n in children:
            evaluation, _, nodeCount = minimax(n, depth - 1, True, player, alpha, beta, nodeCount)
            if evaluation < minEval:
                minEval = evaluation
                minState = n
            if alpha != None:
                if beta > minEval:
                    beta = minEval
                if beta <= alpha:
                    break

        return minEval, minState, nodeCount


# Heuristic Used:
# 1. Number of stones in player X's Mancala
# 2. Total number of stones on a player X's side of the board

# In each of the games, does one palyer always win?
# Not always. 

DEPTH = 2
ALPHA = -math.inf
#Uncomment to do not do alpha-beta pruning
# ALPHA = None 
BETA = math.inf
def main():
    # game = GameController(GAMETYPE.CVC, stones=2, default=False)
    game = GameController(GAMETYPE.CVC, stones=4, default=False)
    # game = GameController(GAMETYPE.PVC, stones=4, default=False)
    # node = game.current_state
    # node.display()
    # print(node.depth)
    # alpha, beta = -math.inf, math.inf
    # score, state, nodes = minimax(node, 2, True, PLAYER.ONE, alpha, beta, 0)
    # print(score, nodes)
    # state.display()
    # print(state.moveset)
    # print(state.depth)
    # r = np.array(node.state.board)
    # print(r[PLAYER_ONE_INDEX].sum())
    # print(r[PLAYER_TWO_INDEX].sum())


    game.play()
    p1_score, p2_score = game.calculate_score()
    print("Player 1: {}, Player 2: {}".format(p1_score, p2_score))
    if p1_score > p2_score:
        print("Player 1 wins!")
    elif p1_score < p2_score:
        print("Player 2 wins!")
    else:
        print("Tie")
if __name__=='__main__':
    main()