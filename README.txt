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

COMMENTS ABOUT THE ASSIGNMENT:
I was not able to test any of my outputs since I do not have a linux install,
was unable to get ANY SAT solver I found working on windows, and I was unable to
make/install any SAT solver in the linux labs since they required administrative privileges.
Between my issues in this, the fact that I am working alone on a 3 person project, and the minor features
I implemented to the program, I'm hoping I can be awarded some marks beyond the basic task marks.

I attempted to use the two SAT solvers (miniSAT, libsolv) which are included in my project folder
The three methods I used to attempt to build them were:
mingw32-make, MSYS make, and CYGWIN
but encountered errors with all of them.