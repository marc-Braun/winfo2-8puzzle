public class Solver {
    public static boolean is_solvable(int[][] p) {
        int[] x = new int[9];
        int counter = 0, inversionen = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                x[counter++] = p[i][j];
            }
        }

        for(int i = 0; i < 9; i++) {
            if(x[i]!=0) {
                for (int j = i+1; j < 9; j++) {
                    if(x[j]!=0 && x[j] < x[i]) inversionen++;
                }
            }
        }

        if(inversionen%2 == 0) return false;

        return true;
    }

    public static void main(String[] args) {
        int[][] test = {{},{},{}};

        System.out.println(is_solvable(test));
    }
}
