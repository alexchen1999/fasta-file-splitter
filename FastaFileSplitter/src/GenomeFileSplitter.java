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
	public static ArrayList<String> readFile(String filename) {
		FileReader in;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			in = new FileReader(filename);
			BufferedReader r = new BufferedReader(in);
			Scanner scan = new Scanner(r);
			String line;
			while ((line = scan.nextLine()) != null) {
				lines.add(line);
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return lines;	
	}
	
	
	//Take in an ArrayList version of the fasta file (separated by line) and
	//split chromosome XX into XX1 and XX2 with appropriate headers and sequences.
	public static void splitFile(ArrayList<String> lines, String filename) {
		try {
			FileWriter out = new FileWriter(filename + ".split.fasta");
			BufferedWriter w = new BufferedWriter(out);
			String header = "";
			for (int i = 0; i < lines.size(); i++) {
				//distinguish between header and sequence with the '>' symbol
				if (lines.get(i).charAt(0) == '>') {
					header = lines.get(i);
					//Ex: Chromosome >1A becomes >1A1 signaling 1st half of
					//1A sequence
					out.write(header.substring(0, 3) + "1" + header.substring(3));
				} else {
					
					//split sequence in half
					String sequence = lines.get(i);
					String firstHalf = sequence.substring(0, sequence.length()/2);
					String secondHalf = sequence.substring(sequence.length()/2);
					out.write(firstHalf);
					//Ex: Chromosome >1A becomes >1A2 signaling 2nd half of
					//1A sequence
					out.write(header.substring(0, 3) + "2" + header.substring(3));
					out.write(secondHalf);					
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String filename = args[0];
		ArrayList<String> l = readFile(filename);
		splitFile(l, filename);
		System.out.println("File has been split successfully");
	}
}
