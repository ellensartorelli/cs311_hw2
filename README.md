# cs311_hw2

- Joy Wood, Ellen Sartorelli
- All group members were present while working on this project.
- We have neither given nor recieved unathorized aid on this assignment.

1. The only file required to run this program is SudokuPlayer.Java.

2. There are no known bugs.

Our custom solver combines the optimizing techniques of backtracking and the minimun remaining value heuristic. Instead of selecting the "next" cell on the board when picking a cell to assign a value, we select the cell with the shortest domain. By selecting the cell with the least possible amount of potential values, we are forcing the algorithim to "fail first' in an attempt to reduce the number of branches at each level. The custom solver ran quicker and more efficiently on the easy, hard and a large selection of random puzzles. However, it was unable to solve the medium puzzle as efficiently as the AC3 solution that only utilized backtracking.

