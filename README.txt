Created by Evan Nickerson as a project for UVic CSC320, summer 2015
Repository at https://github.com/Bekwnn/SudokuSATSolver

Sample usage with SamplePuzzle.txt:
java SSATS SamplePuzzle OutFile
or
java SSATS SamplePuzzle OutFile -GSAT

Puzzle.txt formatting:
- Each puzzle must be an unbroken token. A file can have multiple puzzles, each in
separate tokens, in which case the program will generate multiple output files, such as
OutFile.txt, OutFile(1).txt, OutFile(2).txt, etc
- Blanks may be encoded as '*', '0', or '.'
- 9x9 puzzles only

comments of clause constructors taken and modified from
http://www.cs.qub.ac.uk/~I.Spence/SuDoku/SuDoku.html