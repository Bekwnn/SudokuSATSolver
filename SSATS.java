import java.io.*;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

public class SSATS {
	//total x^2 size of the puzzle
	private static final int SUDOKU_SIZE = 9;
	private static final int SUDOKU_SIZE_SQR = SUDOKU_SIZE*SUDOKU_SIZE;
	private static final int SUDOKU_SIZE_SQRT = (int)Math.sqrt(SUDOKU_SIZE);
	
	private static int clauseCount = 0;
	
	public static void main(String[] args) throws IOException {
		//need to return if no file specified
		if (args.length != 2) {
			System.out.println("Usage: \"java SSATS inputfilename outputfilename\" (no .txt; extension assumed)");
			return;
		}
		
		//using a hash map structure for storing the puzzle 
		HashMap<String,Integer> puzzleMap = new HashMap<String,Integer>();
		Scanner s = null;
		
		try {
			s = new Scanner(new BufferedReader(new FileReader(args[0]+".txt")));
			int puzzleCount = 0;
			while (s.hasNext())
			{
				clauseCount = 0;	//reset clause count
				
				String str = s.next(); 
				char[] inChar = str.toCharArray();
				
				//exit if file format isn't right
				if (inChar.length != SUDOKU_SIZE_SQR)
				{
					System.out.println("Error with file format");
					return;
				}
				
				PrintSudoku(inChar); //to view the map and debug
				
				//fill map from input file
				for (int x, i = 0; i < SUDOKU_SIZE; i++) {
					for (int j = 0; j < SUDOKU_SIZE; j++) {
						x = Character.getNumericValue(inChar[i*SUDOKU_SIZE+j]);
						if (x >= 1 && x <= SUDOKU_SIZE) {
							puzzleMap.put("("+(i+1)+","+(j+1)+")", x);
						} else {
							puzzleMap.put("("+(i+1)+","+(j+1)+")", 0);
						}
					}
				}
				
				//create multiple files if there are multiple puzzles to prevent overwriting
				String outFileName = args[1];
				if (puzzleCount > 0) outFileName += "("+puzzleCount+")";
				outFileName += ".txt";
				
				GenerateSATCNF(puzzleMap, outFileName);
				
				//now add the formatting to the start of the file
				FormatPostPass(outFileName);
				
				puzzleCount++;
			}
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}
	
	private static void GenerateSATCNF(HashMap<String,Integer> puzzleMap, String fileOutName)
	{
		try {
			PrintWriter writer = new PrintWriter(fileOutName, "UTF-8");
			
			//writer.println("Individual Cell Clauses:");
			IndividualCellClauses(writer);
			
			//writer.println("Row Clauses:");
			RowClauses(writer);
			
			//writer.println("Column Clauses:");
			ColumnClauses(writer);
			
			//writer.println("Block Clauses:");
			BlockClauses(writer);
			
			//writer.println("PreFilled Clauses:");
			PreFilledCells(writer, puzzleMap);
			
			//close the writer to save the file
			writer.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("writer threw FileNotFound");
		} catch (UnsupportedEncodingException e) {
			System.out.println("writer threw UnsupportedEncodingException (UTF-8)");
		}
	}
	
	private static void FormatPostPass(String fileOutName) throws IOException
	{
		/*
		TODO: intro SAT formatting
		p cnf <# variables> <# clauses>
		<list of clauses>
		ex:
			c A sample file
			p cnf 4 3
			1 3 4 0
			-1 2 0
			-3 -4 0
		*/
		
		//read the file back and store it in a StringBuilder
		Scanner s = null;
		StringBuilder rebuilder = new StringBuilder();
		try {
			s = new Scanner(new BufferedReader(new FileReader(fileOutName)));
			while (s.hasNextLine())
			{	
				rebuilder.append(s.nextLine());
				rebuilder.append(System.getProperty("line.separator"));
			}
		} finally {
			if (s != null) {
				s.close();
			}
		}
		
		//write the formatting info then append the rest
		PrintWriter postWriter = null;
		try {
			postWriter = new PrintWriter(fileOutName, "UTF-8");
			
			postWriter.println("c This SAT solution was produced by Sudoku SAT Solver (SSATS),");
			postWriter.println("c written by Evan Nickerson");
			postWriter.println("p cnf " + (SUDOKU_SIZE_SQR*SUDOKU_SIZE) + " " + clauseCount);
			
			postWriter.print(rebuilder.toString());
			
			postWriter.close();
		} catch (FileNotFoundException e) {
			System.out.println("postWriter threw FileNotFound");
		} catch (UnsupportedEncodingException e) {
			System.out.println("postWriter threw UnsupportedEncodingException (UTF-8)");
		}
	}
	
	private static void IndividualCellClauses(PrintWriter writer)
	{
		/*
		Clauses have to be included to indicate that a cell contains exactly one value in the range 1 to 9. Consider the cell <1,1>. The clause
		111 112 113 114 115 116 117 118 119 0
		indicates that it contains a least one such value (the 0 is a terminator). To show that it contains only one value needs a series of clauses of the form
		-111 -112 0 (can't be both 1 and 2)
		-111 -113 0 (can't be both 1 and 3)

		These have to be repeated for each of the 81 cells. 
		*/
		for (int i = 0; i < SUDOKU_SIZE; i++) {         //the row
			for (int j = 0; j < SUDOKU_SIZE; j++) {	    //the column
				for (int x = 0; x < SUDOKU_SIZE; x++) { //the value
					writer.print("" + (i+1) + (j+1) + (x+1) + " ");
				}	
				EndClause(writer);
			}
		}
	}
	
	private static void RowClauses(PrintWriter writer)
	{
		/*
		In the same manner, to show that row 1 contains a 1, we need
		111 121 131 141 151 161 171 181 191 0
		and to show that it contains a 2 we need
		112 122 132 142 152 162 172 182 192 0
		This is repeated for values 3 to 9. Then, to show that it does not contain more than one 1, we need
		-111 -121 0 (first two squares aren't both 1)
		-111 -131 0 (first and third aren't both 1)

		This is repeated for the values in the range 2 to 9. 
		*/
		for (int x = 0; x < SUDOKU_SIZE; x++) {         //the value
			for (int i = 0; i < SUDOKU_SIZE; i++) {	    //the row
				for (int j = 0; j < SUDOKU_SIZE; j++) { //the column
					writer.print("" + (i+1) + (j+1) + (x+1) + " ");
				}	
				EndClause(writer);
			}
		}
		
		//TODO: NEGATIVE ENCODINGS!!!
	}
	
	private static void ColumnClauses(PrintWriter writer)
	{
		/*
		In the same manner, to show that column 1 contains a 1, we need
		111 211 311 411 511 611 711 811 911 0
		and to show that it contains a 2 we need
		112 212 312 412 512 612 712 812 912 0
		This is repeated for values 3 to 9. Then, to show that it does not contain more than one 1, we need
		-111 -211 0 (first two squares aren't both 1)
		-111 -311 0 (first and third aren't both 1)

		This is repeated for the values in the range 2 to 9. 
		*/
		for (int x = 0; x < SUDOKU_SIZE; x++) {         //the value
			for (int i = 0; i < SUDOKU_SIZE; i++) {	    //the column
				for (int j = 0; j < SUDOKU_SIZE; j++) { //the row
					writer.print("" + (j+1) + (i+1) + (x+1) + " ");
				}	
				EndClause(writer);
			}
		}
		
		//TODO: NEGATIVE ENCODINGS!!!
	}
	
	private static void BlockClauses(PrintWriter writer)
	{
		/*
		Again, these are similar. For example to show that the top left block contains a 1, we need
		111 121 131 211 221 231 311 321 331 0 
		*/
		for (int x = 0; x < SUDOKU_SIZE; x++) {  //the value
			for (int blocki = 0; blocki < SUDOKU_SIZE_SQRT; blocki++) {
				for (int blockj = 0; blockj < SUDOKU_SIZE_SQRT; blockj++) {
					for (int i = 0; i < SUDOKU_SIZE_SQRT; i++) {
						for (int j = 0; j < SUDOKU_SIZE_SQRT; j++) {
							writer.print("" + (blocki*SUDOKU_SIZE_SQRT + i+1) + (blockj*SUDOKU_SIZE_SQRT + j+1) + (x+1) + " ");
						}
					}
					EndClause(writer);
				}
			}
		}
		
		//TODO: NEGATIVE ENCODINGS!!!
	}
	
	private static void PreFilledCells(PrintWriter writer, HashMap<String,Integer> puzzleMap)
	{
		/*
		This completes the clauses which are the same for every puzzle because they reflect the basic rules 
		of the game. We finally need to add the clauses which specify the initial cells.
		For example if cell <2,3> has to contain 4, we add the clause
		234 0
		*/
		for (Map.Entry<String,Integer> entry : puzzleMap.entrySet())
		{
			if (entry.getValue() != 0)
			{
				String[] entryLocation = entry.getKey().substring(1,entry.getKey().length()-1).split(",");	//trims the brackets and splits at comma
				String line = entryLocation[0]+entryLocation[1]+entry.getValue()+" ";
				writer.print(line);
				EndClause(writer);
			}
		}
	}
	
	private static void EndClause(PrintWriter writer)
	{
		writer.println("0");
		clauseCount++;
	}
	
	//prints a char array as a formatted sudoku table
	private static void PrintSudoku(char[] charArray)
	{
		if (charArray.length != SUDOKU_SIZE_SQR) return;
		for (int i = 0; i < SUDOKU_SIZE; i++) {
			
			//draw horizontal line
			if (i % SUDOKU_SIZE_SQRT == 0 && i != 0) {
				for (int j = 0; j < SUDOKU_SIZE + SUDOKU_SIZE_SQRT-1; j++) {
					System.out.print("-");
				}
				System.out.println();
			}
			
			//fill normally
			for (int j = 0; j < SUDOKU_SIZE; j++) {
				if (j % SUDOKU_SIZE_SQRT == 0 && j != 0) System.out.print("|");
				System.out.print(charArray[i*SUDOKU_SIZE+j]);
			}
			System.out.println();
		}
		System.out.println();
	}
}