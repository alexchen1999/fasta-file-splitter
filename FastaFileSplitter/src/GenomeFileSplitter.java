import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class GenomeFileSplitter {

	//maps a current header to the next occurrence of header
	public static HashMap<Integer, Integer> headers = new HashMap<Integer, Integer>();
	public static ArrayList<Integer> sequenceLengths = new ArrayList<Integer>();
	static int numLines = 0;
	
	//reads in FASTA file line by line and generates a HashMap that maps
	//the line number of a header with the line number of the next header in the file
	public static void readFile(File file) {
		FileReader in;
		System.out.println("Currently reading in file...");
		try {
			in = new FileReader(file);
			BufferedReader r = new BufferedReader(in);
			Scanner scan = new Scanner(r);
			String line;
			int currSeqLength = 0;
			int countLineNo = 0;
			int previousHeader = 0;
			while (scan.hasNextLine()) {
				line = scan.nextLine();
				//distinguish between header and sequence with the '>' symbol
				if (line.charAt(0) == '>') {
					headers.put(previousHeader, countLineNo);
					previousHeader = countLineNo;
					if (currSeqLength != 0) {
						sequenceLengths.add(currSeqLength);
						currSeqLength = 0;
					}
				} else {
					currSeqLength += line.length();
				}
				countLineNo++;
			}
			//put in the last header; split halfway from that line to EOF
			headers.put(previousHeader, countLineNo - 1);
			sequenceLengths.add(currSeqLength);
			numLines = countLineNo;
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println("Successfully read all lines of file at " + dateFormat.format(date));
		System.out.println("Number of lines read in file: " + numLines);
		System.out.println("Number of headers (>) found in file: " + headers.size()); 
		System.out.println("Number of sequences found in file: " + sequenceLengths.size());
	}
	
	//Split chromosome XX into XX1 and XX2 with appropriate headers and sequences.
	public static void splitFile(File originalFile, File writeFile) {
		FileReader in;
		try {
			System.out.println("Now splitting file and writing to output...");
			
			//Readers
			in = new FileReader(originalFile);
			BufferedReader r = new BufferedReader(in);
			Scanner scan = new Scanner(r);
			
			//only 1 reference per line; storing every single line
			//consumes too much memory if file contains a large number of lines
			String line;
			
			//BufferedWriter to write split file
			FileWriter out = new FileWriter(writeFile);
			BufferedWriter w = new BufferedWriter(out);
			String header = "";
			
			//Want to also output a file containing a list of line numbers
			//where we split the original at
			File splitLines = new File(originalFile.getAbsolutePath().replace(
					".fasta", ".splitLocations.txt"));
			FileWriter loc = new FileWriter(splitLines);
			BufferedWriter writeSplitLinePositions = new BufferedWriter(loc);
			
			int whereToSplit = 0;
			int currentArrayIndex = 0;
			int currNumBases = 0;
			int currentLine = 0;
			
			while (scan.hasNextLine()) {
				line = scan.nextLine();
				if (currentLine == numLines / 2) {
					DateFormat dateFormatHalfway = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date dateHalfway = new Date();
					System.out.println("Halfway done splitting the file at: " + 
							dateFormatHalfway.format(dateHalfway));
				}
				
				if (headers.containsKey(currentLine)) {
					//calculate which line to split at by taking the average line numbers 
					//of the current header and next header
					header = line;
					System.out.println("Found header: " + header);
					whereToSplit = sequenceLengths.get(currentArrayIndex) / 2;
					
					//marker for XX1 header
					String[] contents = header.split(":");
					String newFirst = contents[0].substring(0, 3) + "1" + contents[0].substring(3);
					String newFourth = contents[3] + "1";
					String newSixth = "" + Integer.parseInt(contents[5]) / 2;
					String newHeader = newFirst + ":" + contents[1] + ":" + contents[2] +
							":" + newFourth + ":" + contents[4] + ":" + newSixth + ":" +
							contents[6];
					System.out.println("Created new header: " + newHeader);
					w.write(newHeader);
					w.newLine();
					currNumBases = 0;
					if (currentLine != 0) {
						currentArrayIndex++;
					}
				} else {
					if (currNumBases >= whereToSplit && currNumBases - whereToSplit <= 60) {
						String firstHalf = line.substring(0, (currNumBases - whereToSplit));
						String secondHalf = line.substring((currNumBases - whereToSplit));
						
						//write portion of sequence on line as normal
						w.write(firstHalf);
						w.newLine();
						
						//marker for XX2 header
						String[] contents = header.split(":");
						String newFirst = contents[0].substring(0, 3) + "2" + contents[0].substring(3);
						String newFourth = contents[3] + "2";
						String newSixth = "" + Integer.parseInt(contents[5]) / 2;
						String newHeader = newFirst + ":" + contents[1] + ":" + contents[2] +
								":" + newFourth + ":" + contents[4] + ":" + newSixth + ":" +
								contents[6];
						w.write(newHeader);
						System.out.println("Created new header: " + newHeader);
						DateFormat dateFormatSplitHeader = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						Date dateSplitHeader = new Date();
						System.out.println("We have split chromosome " + header.substring(1, 3) + 
								" at index " + whereToSplit + " at: " + 
								dateFormatSplitHeader.format(dateSplitHeader));	
						w.newLine();
						w.write(secondHalf);
						w.newLine();
						
						//write position of split to split lines file
						writeSplitLinePositions.write("" + whereToSplit);
						writeSplitLinePositions.newLine();
					} else {
						//write portion of sequence on line as normal
						w.write(line);
						w.newLine();
					}
					currNumBases += line.length();
				}				
				currentLine++;
			}
			
			//close all input/output writers/streams	
			scan.close();
			w.close();
			out.close();
			writeSplitLinePositions.close();
			loc.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//read in file path directory
		String filename = args[0];
		
		DateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date1 = new Date();
		System.out.println("Genome File Splitter ran starting at: " + dateFormat1.format(date1));
		
		System.out.println("Input: " + filename);
		
		//must be .fasta
		if (!filename.contains(".fasta")) {
			throw new IllegalArgumentException("only FASTA (.fasta) inputs are accepted");
		}
		
		//create new output file with extension .split.fasta
		String outputFilename = filename.replace(".fasta", "") + ".split.fasta";
		
		System.out.println("Expected Output: " + outputFilename);
		File write = new File(outputFilename);
		
		//read in the file
		File read = new File(filename);
		readFile(read);	
		
		//split the file's chromosomes and write to output
		splitFile(read, write);
		DateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date2 = new Date();
		System.out.println("File has been split successfully at: " + dateFormat2.format(date2));
	}
}
