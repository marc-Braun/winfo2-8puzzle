package puzzleSolver;

class node
{
    puzzle puzzle;
    int manhatten_distance;
    int wrong_plates;
    node parent = null;
    boolean goal = false;

    node(puzzle puzzle)
    {
        this.puzzle = puzzle;
        this.wrong_plates = 0;
        puzzleSolver.calculate_manhatten_distance(this);
        puzzleSolver.calculate_misplace_plates(this);
    }

    node(puzzle puzzle, boolean goal)
    {
        this.puzzle = puzzle;
        this.wrong_plates = 0;
        this.goal = goal;
        if(!goal)puzzleSolver.calculate_manhatten_distance(this);
        if(!goal)puzzleSolver.calculate_misplace_plates(this);
    }
}
