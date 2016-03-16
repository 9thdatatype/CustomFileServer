package fileManagement;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;

/*
 * Improvements to be made are:
 * 		- Finish adding comments to clarify things
 */

//This class has a bunch of methods that do common things that I require for file input and output
public class GenericFileIO {

	//This creates a file object based on a string
	public GenericFileIO(String FilePath){file = new File(FilePath);}
	//This creates a reference to the file object supplied
	public GenericFileIO(File theFile){file = theFile;}
	//Creates a null object
	public GenericFileIO(){}

	//Writes a single line to the file
	public boolean writeLine(String s){
		try{
			//This creates the print writer object
			pWriter = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));

			//This actually writes to the file
			pWriter.println(s);

			//Close the resource
			pWriter.close();
			return true;
		}catch(Exception e){
			return false;
		}
	}

	//Writes a single character to the file
	public boolean writeCharToFile(char c){
		try{
			//This creates the print writer object
			pWriter = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));

			//This actually writes a character to the file
			pWriter.print(c);

			//Close the resource
			pWriter.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	//Reads a line in from the file and returns a string
	public String readFirstLine(){
		//Temporarily holds the string to be returned
		String temp = null;
		try{
			//Create the object
			bReader = new BufferedReader(new FileReader(file));

			//Read a line in and store it temporarily
			temp = bReader.readLine();

			//Close the resource
			bReader.close();
			//Return the line read in from file 
			return temp;
		}catch(Exception e){
			e.printStackTrace();
			return temp;
		}
	}

	//reads an entire file into an array
	public String[] readEntireFile(){
		int size = numberOfLinesInFile();
		//Used to hold the array of lines from the file
		String temp[] = new String[size];
		try{
			//Creates a BufferedReader to read a file
			BufferedReader reader = new BufferedReader(new FileReader(file));

			//Reads the file into RAM
			for(int i = 0; i < size; i++)
				temp[i] = reader.readLine();

			//Closes the reader
			reader.close();

			return temp;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	//Counts the number of lines a file has
	private int numberOfLinesInFile(){
		//Used as a counter
		int size = 0;

		try {
			//Creates a BufferedReader to read a file
			BufferedReader reader = new BufferedReader(new FileReader(file));

			//Counts the number of lines in a file
			while(reader.readLine() != null)
				size++;

			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return size;
	}

	//This function will change the directory of the file destructively
	public boolean changeFileDirDest(String filePathOne, String filePathTwo){
		File fileOne = new File(filePathOne);
		File fileTwo = new File(filePathTwo);

		return _TransferFile(fileOne, fileTwo);
	}

	//This function will change the directory of the file destructively
	public boolean changeFileDirDest(File fileOne, File fileTwo){return _TransferFile(fileOne, fileTwo);}

	//This starts to create the list of files in a directory
	public ArrayList<String> listAllFilesAndSubfoldersInDirectory(){
		//This is a file object of the file to be zipped
		File input = file;
		//This is a holder for the absolute path
		String absPath = input.getAbsolutePath() + "\\";

		//Temporarily holds the list, this gets returned
		ArrayList<String> list = new ArrayList<String>(1);

		if(input.list() == null){
			list.add(file.getAbsolutePath());
			return list;
		}

		//Creates a temporary array for holding the next files in the directory
		String temp[] = new String[input.list().length];

		//This is used to loop through the initial directory and place the file paths in the list
		//This is used mostly for the first layer
		for(int i = 0; i < temp.length; i++){
			//Adds the file name from location i to the absolute path and puts it in temp at i
			temp[i] = absPath + input.list()[i];
			//Checks if the file being checked is a directory
			//		- if not a directory it adds the file path to the list
			//		- if it is it will add the directory and then call the recursive function _Lister
			if(!(new File(temp[i]).isDirectory())){
				list.add(temp[i]);
			}else{
				list.add(temp[i] + "/");
				_Lister(temp[i], list);
			}
		}

		return list;
	}

	//This starts to create the list of files in a directory
	public ArrayList<String> listAllFilesAndSubfoldersInDirectory(File theFile){
		this.file = theFile;

		//This is a file object of the file to be zipped
		File input = theFile;
		//This is a holder for the absolute path
		String absPath = input.getAbsolutePath() + "\\";

		//Temporarily holds the list, this gets returned
		ArrayList<String> list = new ArrayList<String>(1);

		if(input.list() == null){
			list.add(input.getAbsolutePath());
			return list;
		}

		//Creates a temporary array for holding the next files in the directory
		String temp[] = new String[input.list().length];

		//This is used to loop through the initial directory and place the file paths in the list
		//This is used mostly for the first layer
		for(int i = 0; i < temp.length; i++){
			//Adds the file name from location i to the absolute path and puts it in temp at i
			temp[i] = absPath + input.list()[i];
			//Checks if the file being checked is a directory
			//		- if not a directory it adds the file path to the list
			//		- if it is it will add the directory and then call the recursive function _Lister
			if(!(new File(temp[i]).isDirectory())){
				list.add(temp[i]);
			}else{
				list.add(temp[i] + "/");
				_Lister(temp[i], list);
			}
		}

		return list;
	}

	public boolean createBlankFile(String filePath){
		Path path = Paths.get(filePath);

		try{
			//Files.createDirectories(path.getParent());
			Files.createFile(path);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	/*********************************************************************************************************************************
	 * 								This is the start of the private portion of the code
	 * Everything past this point is inaccessible from anywhere else
	 *********************************************************************************************************************************/

	//This is a reference to the file
	private File file;
	//This is for writing to the file
	private PrintWriter pWriter;
	//This is for reading the file
	private static BufferedReader bReader;

	//This is a recursive function to continue going through heavily nested file structures
	private void _Lister(String path, ArrayList<String> list){
		//Temporary file object
		File file = new File(path);

		if(!file.isHidden()){
			//Temporary array of files for the current folder
			String temp[] = new String[file.list().length];
			//Holds the absolute path to the current file
			String absPath = file.getAbsolutePath() + "\\";

			//This for loop is used to add all the files from the current directory to the list
			for(int i = 0; i < temp.length; i++){
				temp[i] = absPath + file.list()[i];
				if(!(new File(temp[i]).isDirectory())){
					list.add(temp[i]);
				}else{
					list.add(temp[i] + "/");
					_Lister(temp[i], list);
				}
			}
		}
	}

	//Transfers the files
	private boolean _TransferFile(File fileOne, File fileTwo){
		boolean temp = false;

		//Deletes the the file to be transfered to
		fileTwo.delete();

		//Rename the first file to the second one
		temp = fileOne.renameTo(fileTwo);

		//Now delete the first one so there is no duplicates
		fileOne.delete();

		return temp;
	}
}