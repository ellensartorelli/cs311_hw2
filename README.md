# cs311_hw2
<<<<<<< HEAD
# ellensartorelli.github.io
=======

- Joy Wood, Ellen Sartorelli
- All group members were present while working on this project.
- We have neither given nor recieved unathorized aid on this assignment.

1. The only file required to run this program is SudokuPlayer.Java.

2. There are no known bugs.

Our custom solver combines the optimizing techniques of backtracking and the minimum remaining value heuristic. Instead of selecting the "next" cell on the board when picking a cell to assign a value, we select the cell with the shortest domain. By selecting the cell with the least possible amount of potential values, we are forcing the algorithim to "fail first" in an attempt to reduce the number of branches at each level. The custom solver ran quicker and more efficiently on the easy, hard and a large selection of random puzzles. However, it was unable to solve the medium puzzle as efficiently as the AC3 solution that only utilized backtracking. We believe this is due to the algorithms failure to preform optimally under certain edge cases where a board is configured such that it appears to have more than one solution, and fails late in the backtracking step.  Our custom solver outpreformed the AC3 algorithm in terms of efficiency and speed when tested randomly on 10 solvable boards. However, as the random board generator can generate unsolvable board configurations, it was difficult to ensure the accuracy of our testing method. Forward checking would be a optimization in terms of recursions needed, but would come at the price of storage space. It would help prevent the algorithm from following false solutions that fail late. 
>>>>>>> 6b250c69103733d41d98e76f42dcaa20d44f5cce
# ellensartorelli.github.io
