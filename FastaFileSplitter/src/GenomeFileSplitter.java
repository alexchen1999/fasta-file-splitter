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
import java.util.List;
import java.util.Scanner;

public class GenomeFileSplitter {

	//maps a current header to the next occurrence of header
	public static HashMap<Integer, Integer> headers = new HashMap<Integer, Integer>();
	
	//reads in FASTA file line by line and generates an ArrayList with
	//each line as an element of the list.
	public static ArrayList<String> readFile(File file) {
		FileReader in;
		ArrayList<String> lines = new ArrayList<String>();
		System.out.println("Currently reading in file...");
		try {
			in = new FileReader(file);
			BufferedReader r = new BufferedReader(in);
			Scanner scan = new Scanner(r);
			String line;
			int countLineNo = 0;
			int previousHeader = 0;
			while (scan.hasNextLine()) {
				line = scan.nextLine();
				lines.add(line);
				
				//distinguish between header and sequence with the '>' symbol
				if (line.charAt(0) == '>') {
					headers.put(previousHeader, countLineNo);
					previousHeader = countLineNo;
				}
				countLineNo++;
			}
			//put in the last header; split halfway from that line to EOF
			headers.put(previousHeader, lines.size() - 1);
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println("Successfully read all lines of file at " + dateFormat.format(date));
		System.out.println("Number of lines read in file: " + lines.size());
		System.out.println("Number of headers (>) found in file: " + headers.size()); 
		return lines;	
	}
	
	
	//Take in an ArrayList version of the fasta file (separated by line) and
	//split chromosome XX into XX1 and XX2 with appropriate headers and sequences.
	public static void splitFile(ArrayList<String> lines, File file) {
		try {
			System.out.println("Now splitting file and writing to output...");
			
			//BufferedWriter to write split file
			FileWriter out = new FileWriter(file);
			BufferedWriter w = new BufferedWriter(out);
			String header = lines.get(0);
			
			//Want to also output a fil containing a list of line numbers
			//where we split the original at
			File splitLines = new File(file.getAbsolutePath().replace(".fasta", ".splitLocations.txt"));
			FileWriter loc = new FileWriter(splitLines);
			BufferedWriter writeSplitLinePositions = new BufferedWriter(loc);
			
			int nextHeaderPosition = 0;
			int whereToSplit = 0;
			
			//loop through and find the next header 
			for (int i = 0; i < lines.size(); i++) {
				if (i == lines.size() / 2) {
					DateFormat dateFormatHalfway = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date dateHalfway = new Date();
					System.out.println("Halfway done splitting the file at: " + 
							dateFormatHalfway.format(dateHalfway));
				}
				if (headers.containsKey(i)) {
					//calculate which line to split at by taking the average line numbers 
					//of the current header and next header
					header = lines.get(i);
					nextHeaderPosition = headers.get(i);
					whereToSplit = (nextHeaderPosition + i) / 2;
					
					//marker for XX1 header
					w.write(header.substring(0, 3) + "1" + header.substring(3));
					w.newLine();
				} else {
					if (i == whereToSplit) {
						
						//write portion of sequence on line as normal
						w.write(lines.get(i));
						w.newLine();
						
						//marker for XX2 header
						w.write(header.substring(0, 3) + "2" + header.substring(3));
						DateFormat dateFormatSplitHeader = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						Date dateSplitHeader = new Date();
						System.out.println("We have split chromosome " + header.substring(1, 3) + 
								" at line " + whereToSplit + " at: " + 
								dateFormatSplitHeader.format(dateSplitHeader));						
						w.newLine();
						
						//write position of split to split lines file
						writeSplitLinePositions.write("" + whereToSplit);
						writeSplitLinePositions.newLine();
						
					} else {
						
						//write portion of sequence on line as normal
						w.write(lines.get(i));
						w.newLine();
					}
				}
			}
			
			//close all output writers/streams
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
		ArrayList<String> l = readFile(read);	
		
		//split the file's chromosomes and write to output
		splitFile(l, write);
		DateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date2 = new Date();
		System.out.println("File has been split successfully at: " + dateFormat2.format(date2));
	}
}
