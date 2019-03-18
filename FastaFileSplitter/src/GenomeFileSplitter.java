import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GenomeFileSplitter {
	
	public String ReadFile(String filename) {
		FileReader in;
		try {
			in = new FileReader(filename);
			BufferedReader r = new BufferedReader(in);
			Scanner scan = new Scanner(r);
			String contents = "";
			String line;
			while ((line = scan.nextLine()) != null) {
				contents += line + "\n";
			}
			scan.close();
			return contents;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;	
	}
	
}
