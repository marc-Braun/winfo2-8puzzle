package puzzleSolver;

class puzzle
{

    int [][] puzzle;
    int x0;
    int y0;
    int depth;
    int lastmove;

    puzzle(int[][] puzzle, int x0, int y0, int last)
    {
        this.x0 = x0;
        this.y0 = y0;
        this.puzzle = puzzle;
        this.lastmove = last;
    }

    void printPuzzle()
    {
        for (int[] row : this.puzzle) {
            for (int column : row)
                System.out.print(column + " ");
            System.out.println();
        }
        System.out.println("-----");
    }
}