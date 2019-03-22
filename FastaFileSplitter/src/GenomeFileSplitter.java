import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GenomeFileSplitter {
	
	//reads in FASTA file line by line and generates an ArrayList with
	//each line as an element of the list.
	public static ArrayList<String> readFile(File file) {
		FileReader in;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			in = new FileReader(file);
			BufferedReader r = new BufferedReader(in);
			Scanner scan = new Scanner(r);
			String line;
			while (scan.hasNextLine()) {
				line = scan.nextLine();
				lines.add(line);
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		System.out.println("Successfully read all lines of file");
		System.out.println("Number of lines read in file: " + lines.size());
		return lines;	
	}
	
	
	//Take in an ArrayList version of the fasta file (separated by line) and
	//split chromosome XX into XX1 and XX2 with appropriate headers and sequences.
	public static void splitFile(ArrayList<String> lines, File file) {
		try {
			System.out.println("Now splitting file and writing to output");
			FileWriter out = new FileWriter(file);
			BufferedWriter w = new BufferedWriter(out);
			String header = "";
			for (int i = 0; i < lines.size(); i++) {
				//distinguish between header and sequence with the '>' symbol
				if (lines.get(i).charAt(0) == '>') {
					header = lines.get(i);
					//Ex: Chromosome >1A becomes >1A1 signaling 1st half of
					//1A sequence
					w.write(header.substring(0, 3) + "1" + header.substring(3) + "\n");
				} else {
					//split sequence in half
					String sequence = lines.get(i);
					String firstHalf = sequence.substring(0, sequence.length()/2);
					String secondHalf = sequence.substring(sequence.length()/2);
					w.write(firstHalf + "\n");
					//Ex: Chromosome >1A becomes >1A2 signaling 2nd half of
					//1A sequence
					w.write(header.substring(0, 3) + "2" + header.substring(3) +"\n");
					w.write(secondHalf + "\n");		
				}
			}
			w.close();
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//read in file path directory
		String filename = args[0];
		
		System.out.println("Input: " + filename);
		
		//must be .fa or .fasta
		if (!filename.contains(".fasta") || !filename.contains(".fa")) {
			throw new IllegalArgumentException("only FASTA (.fa or .fasta) inputs are accepted");
		}
		
		//create new output file with extension .split.fasta
		String outputFilename;
		if (filename.contains("fasta")) {
			outputFilename = filename.replace(".fasta", "") + ".split.fasta";
		} else {
			outputFilename = filename.replace(".fa", "") + ".split.fasta";
		}
		
		System.out.println("Expected Output: " + outputFilename);
		File write = new File(outputFilename);
		
		//read in the file
		File read = new File(filename);
		ArrayList<String> l = readFile(read);	
		
		//split the file's chromosomes and write to output
		splitFile(l, write);
		System.out.println("File has been split successfully");
	}
}
