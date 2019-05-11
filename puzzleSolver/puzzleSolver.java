package puzzleSolver;

import java.util.LinkedList;
import java.util.StringTokenizer;

public class puzzleSolver
{
    //saves goal state of puzzle
    private static node goal_state;
    //saves current state of puzzle
    private node current_state;
    //search queue contains remaining nodes to search
    //solution path contains the solved puzzle path
    private LinkedList<node> search_queue, solution_path;
    private LinkedList<String> path_queue;
    private boolean debug = false, A_manhattan = false, A_wrong_plates = false, greedy = false;
    private int steps;

    private puzzleSolver(String[] input)
    {
        steps = 0;
        search_queue = new LinkedList<>();
        path_queue = new LinkedList<>();
        solution_path = new LinkedList<>();
        switch (input[1])
        {
            case "1": greedy = true; break;
            case "2": A_wrong_plates = true; break;
            case "3": A_manhattan = true; break;
        }
        if ("1".equals(input[2])) debug = true;
        this.create_goal_state();
        this.create_initial_state(input[0]);
    }

    public static void main(String[] args)
    {
        //args[0]: Hier soll das Puzzle, mit Komma als Trennzeichen oder direkt als int[][]-Array, Ÿbergeben werden kšnnen.
        //			( z.B.: 1,2,3,4,5,6,7,8,0 )
        //args[1]: Dieser Parameter soll den Suchalgorithmus variieren.
        //			(1: greedy, 2: A* mit h(n) = falsch platzierten Kacheln, 3: A*  mit h(n) = Manhattan-Distanzen)
        //args[2]: †ber einen Debugging-Parameter soll gewŠhlt werden kšnnen, ob eine vollstŠndige Ausgabe der Suchschritte stattfindet.
        //			(0: nein, 1: ja)

        String[] input = {"3,1,2,4,0,8,5,6,7","2","0" };
        puzzleSolver puzzleSolver = new puzzleSolver(input);
        puzzleSolver.search();
    }

    /**
     * Creates a goal state node
     */
    private void create_goal_state()
    {
        int[][] goal = {{1,2,3},{8,0,4},{7,6,5}};
        goal_state = new node(new puzzle(goal, 1, 1,-1),true);
    }

    /**
     * Takes a string and tries to convert it
     * into a puzzle. Prints error if String is in wrong format.
     * Afterwards it adds the puzzle to the search queue.
     * @param state String representation of a puzzle state
     */
    private void create_initial_state(String state)
    {
        int x0 = -1, y0 = -1;
        try {
            StringTokenizer tk = new StringTokenizer(state, ",");
            int[][] initial = new int[3][3];
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    initial[i][j] = Integer.parseInt(tk.nextToken());
                    if (initial[i][j] == 0) { x0 = i; y0 = j; }
                }
            }
            this.current_state = new node(new puzzle(initial, x0, y0, -1));
            this.current_state.puzzle.depth = 0;
            this.search_queue.add(this.current_state);
        } catch (Exception e) {
            System.out.println("-> Wrong Format");
        }
    }

    /**
     * Goes through the nodes puzzle array and calculates
     * the total amount of wrong plates
     * @param n node to calculate wrong plates of
     */
    static void calculate_misplace_plates(node n)
    {
        int wrong_plates = 0;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if(n.puzzle.puzzle[i][j] != goal_state.puzzle.puzzle[i][j]) wrong_plates++;
        n.wrong_plates = wrong_plates;
    }

    /**
     * Calculates the manhatten distance of the nodes puzzle.
     * @param n node to calculate manhatten distance of
     */
    static void calculate_manhatten_distance(node n)
    {
        int man_sum = 0;
        int[][] tmp = n.puzzle.puzzle;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                switch (tmp[i][j]) {
                    case 0: man_sum += Math.abs(i + 1 - 2) + Math.abs(j + 1 - 2); break;
                    case 1: man_sum += Math.abs(i + 1 - 1) + Math.abs(j + 1 - 1); break;
                    case 2: man_sum += Math.abs(i + 1 - 1) + Math.abs(j + 1 - 2); break;
                    case 3: man_sum += Math.abs(i + 1 - 1) + Math.abs(j + 1 - 3); break;
                    case 4: man_sum += Math.abs(i + 1 - 2) + Math.abs(j + 1 - 3); break;
                    case 5: man_sum += Math.abs(i + 1 - 3) + Math.abs(j + 1 - 3); break;
                    case 6: man_sum += Math.abs(i + 1 - 3) + Math.abs(j + 1 - 2); break;
                    case 7: man_sum += Math.abs(i + 1 - 3) + Math.abs(j + 1 - 1); break;
                    case 8: man_sum += Math.abs(i + 1 - 2) + Math.abs(j + 1 - 1); break;
                }
        n.manhatten_distance = man_sum;
    }

    /**
     * Starts the search for the correct path to solve the puzzle.
     * If the puzzle was solved successfully search() terminates.
     * Prints only the solution path if the debug parameter is false.
     * Prints the full search tree if debug parameter is true.
     */
    private void search()
    {
        while(true)
        {
            //add one step
            this.steps++;
            if (compare_current_and_goal())
            {
                System.out.println("Gelöst nach " + steps + " Schritten.");
                save_solution();
                return;
            }
            //check if debug param is set
            if(debug) this.current_state.puzzle.printPuzzle();                                                          // Debug-Parameter
            expand_node(this.current_state);
        }
    }

    /**
     * Compares the current state with the goal state.
     * @return Returns true if current state is goal state, false otherwise
     */
    private boolean compare_current_and_goal()
    {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (!(this.current_state.puzzle.puzzle[i][j] == puzzleSolver.goal_state.puzzle.puzzle[i][j])) return false;
        return true;
    }

    /**
     * Converts a node into a String
     * @param n node to convert into String
     * @return String representation of node n
     */
    private String nodeString(node n)
    {
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                tmp.append(n.puzzle.puzzle[i][j]);
        return tmp.toString();
    }

    /**
     * Checks if node is already in a path
     * of the search tree. If node is already in
     * search tree the method returns, otherwise it
     * adds node to path_queue
     * @param n node to check
     */
    private void compare_path(node n)
    {
        String tmp = nodeString(n);
        for(String node:path_queue)
            if(tmp.equals(node)) return;
        path_queue.add(tmp);
    }

    /**
     * Removes the first node from the search queue.
     * Checks if node is already in path_queue.
     * According to the position of the zero in the puzzle
     * the method creates new nodes where the zero is moved
     * one step, either up, down, left or right.
     * @param n current node
     */
    private void expand_node(node n)
    {
        //TODO , dont know what the purpose of this is
        search_queue.removeFirst();

        compare_path(n);
        switch ("" + n.puzzle.x0 + n.puzzle.y0)
        {
            case "00":
                move_down(n, n.puzzle.x0, n.puzzle.y0);
                move_right(n, n.puzzle.x0, n.puzzle.y0);
                break;
            case "01":
                move_left(n, n.puzzle.x0, n.puzzle.y0);
                move_right(n, n.puzzle.x0, n.puzzle.y0);
                move_down(n, n.puzzle.x0, n.puzzle.y0);
                break;
            case "02":
                move_left(n, n.puzzle.x0, n.puzzle.y0);
                move_down(n, n.puzzle.x0, n.puzzle.y0);
                break;
            case "10":
                move_right(n, n.puzzle.x0, n.puzzle.y0);
                move_down(n, n.puzzle.x0, n.puzzle.y0);
                move_up(n, n.puzzle.x0, n.puzzle.y0);
                break;
            case "11":
                move_up(n, n.puzzle.x0, n.puzzle.y0);
                move_down(n, n.puzzle.x0, n.puzzle.y0);
                move_right(n, n.puzzle.x0, n.puzzle.y0);
                move_left(n, n.puzzle.x0, n.puzzle.y0);
                break;
            case "12":
                move_up(n, n.puzzle.x0, n.puzzle.y0);
                move_down(n, n.puzzle.x0, n.puzzle.y0);
                move_left(n, n.puzzle.x0, n.puzzle.y0);
                break;
            case "20":
                move_up(n, n.puzzle.x0, n.puzzle.y0);
                move_right(n, n.puzzle.x0, n.puzzle.y0);
                break;
            case "21":
                move_up(n, n.puzzle.x0, n.puzzle.y0);
                move_left(n, n.puzzle.x0, n.puzzle.y0);
                move_right(n, n.puzzle.x0, n.puzzle.y0);
                break;
            case "22":
                move_left(n, n.puzzle.x0, n.puzzle.y0);
                move_up(n, n.puzzle.x0, n.puzzle.y0);
                break;
        }
        this.current_state = this.search_queue.getFirst();
    }

    /**
     * Retrieves the array of the node and returns a copy of the array.
     * @param n node to copy array of
     * @return copy of node n's array
     */
    private int[][] copyArray(node n)
    {
        int[][] tmp = new int[3][3];
        for (int i = 0; i < 3; i++)
            System.arraycopy(n.puzzle.puzzle[i], 0, tmp[i], 0, 3);
        return tmp;
    }

    /**
     * Swaps position of zero leftwards.
     * Creates a new node containing the swapped array
     * and sets the parent node of the new node to n.
     * Increases the nodes depth and tries to add it to
     * the search queue.
     * @param n parent node, node to create new puzzle state from
     * @param x x coordinate of zero in nodes puzzle field
     * @param y y coordinate of zero
     */
    private void move_left(node n, int x, int y)
    {
        int[][] tmp = copyArray(n);
        int temp = tmp[x][y - 1];
        tmp[x][y - 1] = 0;
        tmp[x][y] = temp;
        node node = new node(new puzzle(tmp, x, y - 1,temp));
        node.parent = n;
        node.puzzle.depth = n.puzzle.depth + 1;
        add_node_to_queue(node);
    }

    /**
     * Swaps position of zero rightwards.
     * Creates a new node containing the swapped array
     * and sets the parent node of the new node to n.
     * Increases the nodes depth and tries to add it to
     * the search queue.
     * @param n parent node, node to create new puzzle state from
     * @param x x coordinate of zero in nodes puzzle field
     * @param y y coordinate of zero
     */
    private void move_right(node n, int x, int y)
    {
        int[][] tmp = copyArray(n);

        int temp = tmp[x][y + 1];
        tmp[x][y + 1] = 0;
        tmp[x][y] = temp;
        node node = new node(new puzzle(tmp, x, y + 1,temp));
        node.parent = n;
        node.puzzle.depth = n.puzzle.depth + 1;
        add_node_to_queue(node);
    }

    /**
     * Swaps position of zero upwards.
     * Creates a new node containing the swapped array
     * and sets the parent node of the new node to n.
     * Increases the nodes depth and tries to add it to
     * the search queue.
     * @param n parent node, node to create new puzzle state from
     * @param x x coordinate of zero in nodes puzzle field
     * @param y y coordinate of zero
     */
    private void move_up(node n, int x, int y)
    {
        int[][] tmp = copyArray(n);
        int temp = tmp[x - 1][y];
        tmp[x - 1][y] = 0;
        tmp[x][y] = temp;
        node node = new node(new puzzle(tmp, x - 1, y,temp));
        node.parent = n;
        node.puzzle.depth = n.puzzle.depth + 1;
        add_node_to_queue(node);
    }

    /**
     * Swaps position of zero downwards.
     * Creates a new node containing the swapped array
     * and sets the parent node of the new node to n.
     * Increases the nodes depth and tries to add it to
     * the search queue.
     * @param n parent node, node to create new puzzle state from
     * @param x x coordinate of zero in nodes puzzle field
     * @param y y coordinate of zero
     */
    private void move_down(node n, int x, int y)
    {
        int[][] tmp = copyArray(n);
        int temp = tmp[x + 1][y];
        tmp[x + 1][y] = 0;
        tmp[x][y] = temp;
        node node = new node(new puzzle(tmp, x + 1, y,temp));
        node.parent = n;
        node.puzzle.depth = n.puzzle.depth + 1;
        add_node_to_queue(node);
    }

    /**
     * Adds node to search_queue
     * TODO: elaborate
     * @param nn node to add
     */
    private void add_node_to_queue(node nn)
    {
        if (compare_nodes_in_queue(nn)) return;
        int counter = 0;
        for (node node : search_queue)
        {
           /*if((A_manhattan && (node.manhatten_distance+node.puzzle.depth) == (nn.manhatten_distance+nn.puzzle.depth)))
                if(node.puzzle.lastmove>nn.puzzle.lastmove) break;
            if((A_wrong_plates && ((node.wrong_plates+node.puzzle.depth) == (nn.wrong_plates+nn.puzzle.depth))))
                if(node.puzzle.lastmove>nn.puzzle.lastmove) break;
            if(greedy && ((node.wrong_plates) == (nn.wrong_plates)))
                if(node.puzzle.lastmove>nn.puzzle.lastmove) break;*/
            if(A_manhattan && ((node.manhatten_distance+node.puzzle.depth) > (nn.manhatten_distance+nn.puzzle.depth))) break;            // A* mit Manhattendistanz
            if(A_wrong_plates && ((node.wrong_plates+node.puzzle.depth) > (nn.wrong_plates+nn.puzzle.depth))) break;                     // A* mit Anzahl falscher Platten
            if(greedy && ((node.wrong_plates) > (nn.wrong_plates))) break;                                                               // Greedy-Search
            counter++;
        }
        search_queue.add(counter, nn);
    }

    /**
     * Checks if node is already in path_queue.
     * Returns true if node is in path_queue, otherwise returns false.
     * @param n node to check
     * @return true if node in path_queue, false otherwise
     */
    private boolean compare_nodes_in_queue(node n)
    {
        String tmp = nodeString(n);
        for (String node : path_queue)
            if (node.equals(tmp)) return true;
        return false;
    }

    /**
     * Traverses the search tree upwards, saves each node of the
     * correct search path and prints out the solution path.
     */
    private void save_solution()
    {
        System.out.println("\nSolution depth: " + this.current_state.puzzle.depth);
        node tmp = this.current_state;
        while (tmp != null)
        {
            solution_path.add(tmp);
            tmp = tmp.parent;
        }
        solution_path = reverse(solution_path);
        System.out.println("Solution-Path: ");
        for (node n : solution_path)
            n.puzzle.printPuzzle();
    }

    /**
     * Reverses the order of a linked node list
     * @param list list to reverse
     * @return Reversed linked node list
     */
    private static LinkedList<node> reverse(LinkedList<node> list)
    {
        LinkedList<node> reverse = new LinkedList<>();
        for (int i = list.size() - 1; i >= 0; i--)
            reverse.add(list.get(i));
        return reverse;
    }
}