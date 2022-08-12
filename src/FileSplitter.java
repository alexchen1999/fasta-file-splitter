import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class FileSplitter {

	// HashMap maps header to the line number within the file at which it occurs.
	public static HashMap<String, Integer> headers = new HashMap<String, Integer>();

	// ArrayList stores each of the chr sequence lengths.
	public static ArrayList<Integer> sequenceLengths = new ArrayList<Integer>();

	// Static declaration for FileReader.
	public static FileReader in;

	// Static declaration for BufferedReader.
	public static BufferedReader r;

	// Static declaration for scanner to read line by line.
	public static Scanner scan;

    // Utility function to identify a line as a header by the '>' symbol.
    public static boolean isHeader(String line) {
        return line.charAt(0) == '>';
    }

	// Reads in FASTA file line by line and populates a HashMap that maps
	// the line number of the current header to the line number of the next header in the file.
	public static void populateHeaderMap(File file) {
		try {
			
			// Variables to store current line number and current chromosome sequence length.
			int currentLineNumber = 0, currentSeqLength = 0;

			// Readers and scanner
			in = new FileReader(file);
			r = new BufferedReader(in);
			scan = new Scanner(r);

			String line;
			while (scan.hasNextLine()) {
				line = scan.nextLine();
				if (isHeader(line)) {
					System.out.println("header: " + line + ", current line number: " + currentLineNumber);
					System.out.println("currentSeqLength: " + currentSeqLength);
					headers.put(line, currentLineNumber);
					if (currentSeqLength > 0) {
						sequenceLengths.add(currentSeqLength);
						currentSeqLength = 0;
					}
				} else {
					currentSeqLength += line.length();
				}
				currentLineNumber++;
			}
			// Boundary case: Add the last chromosome's sequence length
			sequenceLengths.add(currentSeqLength);
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Utility function to generate new header.
	public static String generateBisectedHeader(String oldHeader, boolean isFirstHalf) {
		StringBuilder newHeader = new StringBuilder();
		int counter = 0;
		for (int i = 0; i < oldHeader.length() - 1; i++) {
			char c = oldHeader.charAt(i);
			char next = oldHeader.charAt(i+1);
			newHeader.append(c);
			if (Character.isDigit(c) && Character.isLetter(next)) {
				newHeader.append(next);
				if (isFirstHalf) {
					newHeader.append("_1");
				} else {
					newHeader.append("_2");
				}
				i++;
				counter++;
			}
		}
		newHeader.append(oldHeader.substring(oldHeader.length() + 1 - counter));

		return newHeader.toString();
	}

	// Core function to bisect chromosome with header ChrX into ChrX_1 and ChrX_2
	public static void splitFile(File file, File outFile) {
		try {

			// Readers and scanner.
			in = new FileReader(file);
			r = new BufferedReader(in);
			scan = new Scanner(r);

			// BufferedWriter to write bisected FASTA file output.
			FileWriter out = new FileWriter(outFile);
			BufferedWriter w = new BufferedWriter(out);

			// Variables to store
			int splitPoint = 0, headerNumber = 0, currentSequenceLength = 0, offset = 0;

			// store current header, unprocessed
			String currentHeader = "";

			boolean secondHeaderCreated = false;
			
			String line;
			while (scan.hasNextLine()) {
				line = scan.nextLine();
				System.out.println("current line: " + line);
				if (headers.containsKey(line)) {
					currentHeader = line;
					splitPoint = sequenceLengths.get(headerNumber) / 2;
					headerNumber++;
					System.out.println("Split point: " + splitPoint);

					// Print ChrX_1 header on a new line aside from the first one
					if (headers.get(currentHeader) != 0) {
						w.newLine();
					}

					// Write ChrX_1 header.
					String newHeader = generateBisectedHeader(currentHeader, true);
					w.write(newHeader);
					w.newLine();

					// Reset current sequence length to 0 as we are evaluating a new chromosome.
					currentSequenceLength = 0;

					secondHeaderCreated = false;
				} else {
					currentSequenceLength += line.length();
					System.out.println("current sequence length: " + currentSequenceLength);
					int currentLineSplitPoint = currentSequenceLength - splitPoint;
					if (currentSequenceLength >= splitPoint && !secondHeaderCreated) {
						String firstPartOfCurrentLine = line.substring(0, line.length() - currentLineSplitPoint);
						String secondPartOfCurrentLine = line.substring(line.length() - currentLineSplitPoint);
						
						// Write first portion of current line's sequence as normal.
						w.write(firstPartOfCurrentLine);
						w.newLine();
						
						// Write ChrX_2 header.
						String newHeader = generateBisectedHeader(currentHeader, false);
						w.write(newHeader);
						w.newLine();

						// Write second portion of current line.
						offset = secondPartOfCurrentLine.length();
						if (offset > 0) {
							w.write(secondPartOfCurrentLine);
						}

						// Reset current sequence length -- we have passed the split point and are now writing the second half of the
						// original chromosome, which should be considered a "new" sequence.
						currentSequenceLength = 0;
						secondHeaderCreated = true;
					} else {
						if (secondHeaderCreated) {
							int lineLen = line.length();
							for (int i = 0; i < lineLen; i++) {
								if (i == lineLen - offset) {
									w.newLine();
								} else {
									w.write(line.charAt(i));
								}
							}
						} else {
							// Current line is not split -- write current line as normal.
							System.out.println("printed");
							w.write(line);
							w.newLine();
						}
					}
				}				
			}
			
			// Close all input/output writers/streams	
			scan.close();
			w.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static String getFileExtension(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename is invalid");
        }
        String extension = "";
        int index = filename.lastIndexOf('.');
        if (index > 0) {
            extension = filename.substring(index + 1);
        }
        return extension;
    }

    public static boolean isFasta(String filename) {
        String extension = getFileExtension(filename);
        return (extension.equals("fa") || extension.equals("fasta"));
    }
	
	public static void main(String[] args) {
		// Read in file path directory.
		String filename = args[0];

        // Get input file extension.
        String extension = getFileExtension(filename);
        if (!isFasta(filename)) {
            throw new IllegalArgumentException("File must be FASTA format");
        }

        // Print starting time.
		DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date startTime = new Date();
        System.out.println("FASTA File Splitter ran starting at: " + format.format(startTime));
		System.out.println("Input filename: " + filename);
		
		// Read in the file.
		File read = new File(filename);
		populateHeaderMap(read);

        // Print completion of LinkedHashMap pre-processing.
		Date populateHeaderMapCompletionTime = new Date();
        System.out.println("Line numbers corresponding to headers determined at: " + format.format(populateHeaderMapCompletionTime));

		// Create new output file with extension .split.fasta
		String outFile = filename.replace(extension, "split." + extension);
		System.out.println("Now writing to output...");
		System.out.println("Output filename: " + outFile);
		File write = new File(outFile);
		
		// Split the file's chromosomes and write to output
		splitFile(read, write);

        // Print time to complete
		Date finishTime = new Date();
		System.out.println("File has been split successfully at: " + format.format(finishTime));
	}
}