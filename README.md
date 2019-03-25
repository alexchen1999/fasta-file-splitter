# fasta-file-splitter

PROGRAM/PROBLEM DESCRIPTION
---------------------------
This is a program for making two halves for each chromosome in a FASTA text file in order to cut contig size in half.
The project was designed specifically for large genomes--the wheat (Triticum aestivum) genome is extremely large, with each 
chromosome containing ~800 mB. 

Because of the size of the chromosomes, the wheat genome is normally incompatible with picard tools,
which only accepts genome FASTA files with contig sizes of <512 mB. This application thus artificially "doubles" the number of
chromosomes present in the FASTA file by identifying headers and splitting the sequence halfway in between. The contig size of each
half is thus approximately half of the original contig size. 

For example, if the 1A wheat chromosome is normally ~800 mB long, this application would split the 1A chromosome into 1A1 and 1A2 halves,
each having contig size ~400 mB, which should be compatible with picard tools.

ALGORITHM DESCRIPTION
---------------------
There are three methods defined in this program: Main, readFile, and splitFile.

1) Main - Main takes in the user-specified FASTA filename/filepath to be split via command line and applies readFile and splitFile to it.

2) readFile - readFile takes in the file to be split and uses FileReader and FileWriter objects to read the file, line by line, and:
      a) store each line in an ArrayList<String>, and
      b) for every header encountered (denoted by a > as the first symbol on a line representing a header), associate its line number
         with the position of the next header's line position, using a HashMap<Integer, Integer>.
  
3) writeFile - using the information in the ArrayList<String> and HashMap<Integer, Integer>, writeFile will output each line of
   the original FASTA file to a split version of the FASTA file, [filename].split.fasta, and will also find the line position
   where the current chromosome should be split. This is accomplished by taking the average of the line position of the current header
   and the line position of the next header found (these are associated by the HashMap). The logic behind this computation is that
   a FASTA file is formatted such that the header, denoted by a > as the first character, contains information about a
   sequence of DNA, which starts from the very next line. The DNA sequence ends right before the next header, which denotes the next DNA
   sequence. We therefore can determine where to split the DNA sequence (in our case a chromosome) by taking the average of the header
   line positions, since we know that the entire DNA sequence of interest must be contained within those line positions, and the halfway
   point is the average.
 
RUNTIME COMPLEXITY ANALYSIS
---------------------------
Given that the number of lines in our FASTA file is n, we loop through the entirety of lines in our FASTA file and add each line to
our ArrayList, which takes amortized O(1) time per line added to the end of the ArrayList. While doing so, if we find a header, we associate the previously found header position (initialized to 0) to the currently found header's position (recorded/updated by an int count variable). Adding to a HashMap takes expected O(1) time per header found. If there are n lines, and not too many headers, this should take roughly O(n) time. In the worst case, where every line is a header (which would not be too realistic because then you wouldn't have any DNA sequence data), this would take O(n) + O(n) = O(2n) = O(n) time.

Then, we loop through our newly created ArrayList, which contains every line of our input file, and writing each line one by one to
the output file. If we encounter a header, we write a >XX1 header, indicating that we are beginning to write the first half of the DNA sequence. The time complexity of write is O(s) where s is the number of characters in the line the program is currently writing,
but since all the lines in a FASTA file are <80 characters we can treat O(s) as constant. 

Additionally, we also check whether we need to split at a given line by checking if the current line position is found in the HashMap; if so we split according to the average calculation detailed above and write a >XX2 header after the line position where we split, and 
continue iterating and writing lines to the output file. This involves lookup in the map and writing a header, which both take O(1) time.

Given the constant number of operations on n lines, writing to the output file should also take roughly O(n) time.

Thus, in total, the program on a FASTA file of size n should take O(n) + O(n) = O(2n) = O(n) time. This means that the runtime of
this program will scale linearly with the size of your input file.
  
