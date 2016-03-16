package fileManagement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;

import java.util.ArrayList;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Improvements to be made are:
 *		- Add comments to everything
 *		- Test the error handling more thoroughly
 *		- Make sure that this class has the infallible methods for creating and unzipping zip files
 */

public class EnhancedZipper{

	//Creates a zip file based on the file paths as strings. Capable of taking multiple file paths for input separated by semicolons
	public void createZipFile(String destination, String fileToBeZipped, boolean overwritesOK, Encryption enc){
		if(fileToBeZipped.split(";").length > 1){
			String filesAsString[] = fileToBeZipped.split(";");
			ArrayList<File> files = new ArrayList<File>();
			for(String string:filesAsString){
				files.add(new File(string));
			}
			_CreateZipFileIntermediate(new File(destination), files, overwritesOK, enc);
		}else{
			ArrayList<File> file = new ArrayList<File>();
			file.add(new File(fileToBeZipped));
			_CreateZipFileIntermediate(new File(destination), file, overwritesOK, enc);
		}
	}

	//Creates a zip file from the fileToBeZipped and puts it in the destination
	public void createZipFile(File destination, File fileToBeZipped, boolean overwritesOK, Encryption enc){
		ArrayList<File> temp = new ArrayList<File>(1);
		temp.add(fileToBeZipped);
		_CreateZipFileIntermediate(destination, temp, overwritesOK, enc);
	}

	//Creates a zip file from the array of files 
	public void createZipFile(File destination, File filesToBeZipped[], boolean overwritesOK, Encryption enc){
		ArrayList<File> temp = new ArrayList<File>();
		for(File file:filesToBeZipped){
			temp.add(file);
		}
		_CreateZipFileIntermediate(destination, temp, overwritesOK, enc);
	}

	//Unzips a zip file from the fileToBeUnzipped and puts it in the destination, using encryption
	public void unzipFile(File fileToBeUnzipped, File destination, boolean overwritesOK, Encryption enc){_UnzipFileIntermediate(fileToBeUnzipped, destination, overwritesOK, enc);}

	//getter to get the current file being worked on
	//Returns null when there is no file left
	public String getCurrentFile(boolean fullPath){
		if(currentFile == null)
			return null;

		if(currentFile.peek() == null)
			return null;

		String temp = currentFile.poll();

		if(fullPath){
			temp = new File(temp).getAbsolutePath();
		}else{
			temp = new File(temp).getName();
		}

		return temp;
	}

	//Sets whether or not threading should be enabled
	public void setThreading(boolean threading){this.threading = threading;}
	
	//Sets whether the program should zip the whole file or not
	public void setZipWholeFile(boolean wholeFile){this.wholeFile = wholeFile;}

	//Gets whether or not the zipper has encountered an error
	public boolean hasError(){return !error.isEmpty();}

	//Gets the next error from the queue
	public String getNextError(){return error.poll();}

	//Gets whether or not the thread has completed
	public boolean isComplete(){return !thread.isAlive();}

	/*********************************************************************************************************************************
	 * 					This marks the start of the private portion of the code
	 *********************************************************************************************************************************/

	//This is the buffer size for reading in the files
	private final int BUFFER_SIZE = 65536;
	//A reference to the thread used to do the processing
	private Thread thread = new Thread();
	//Tells the computer whether threading is allowed or not
	private boolean threading = false;
	//A Queue to hold the current file being put into or taken out of a zip file
	private ConcurrentLinkedQueue<String> currentFile = new ConcurrentLinkedQueue<String>();
	//A queue to hold any errors that have been encountered
	private ConcurrentLinkedQueue<String> error = new ConcurrentLinkedQueue<String>();
	//Do you want the whole file to be zipped or just the contents of the selected file
	private boolean wholeFile = true;

	//Used as an intermediate step between the public methods and the private _CreateZipFile method
	private void _CreateZipFileIntermediate(File destination, ArrayList<File> filesToBeZipped, boolean overwritesOK, Encryption enc){
		//If threading is set to true then create a new thread and run the _CreateZipFile method else just run it and block the thread
		if(threading){
			//This creates an inline object of type Thread and stores it into thread, telling it to start creating the zip
			thread = new Thread(){
				public void run(){
					_CreateZipFile(destination, filesToBeZipped, overwritesOK, enc);
				}
			};
			//Start the thread
			thread.start();
		}else{
			_CreateZipFile(destination, filesToBeZipped, overwritesOK, enc);
		}
	}

	//Used as an intermediate step between the public methods and the private _UnzipZipFile method
	private void _UnzipFileIntermediate(File fileToBeUnzipped, File destination, boolean overwritesOK, Encryption enc){
		//If threading is set to true then create a new thread and run the _UnzipFile else just run the method and block the thread
		if(threading){
			thread = new Thread(){
				public void run(){
					_UnzipFile(fileToBeUnzipped, destination, overwritesOK, enc);
				}
			};
			thread.start();
		}else{
			_UnzipFile(fileToBeUnzipped, destination, overwritesOK, enc);
		}
	}

	//This is the method that actually starts the creation of the zip file
	private void _CreateZipFile(File destination, ArrayList<File> filesToBeZipped, boolean overwritesOK, Encryption enc){
		//This is a list of all the input directories that have been supplied
		ArrayList<String> directoriesList = new ArrayList<String>();

		//Does input validation on all of the elements of the filesToBeZipped arraylist
		for(File fileToBeZipped:filesToBeZipped){
			//Does the fileToBeZipped having a name at all
			if(fileToBeZipped.getName().equals("") && !(fileToBeZipped.exists() && fileToBeZipped.isDirectory())){
				error.add("Invalid input path.");
				return;
			}
			
			//If the file exists and is a directory then add it to the directories list
			if(fileToBeZipped.exists() && fileToBeZipped.isDirectory()){
				directoriesList.add(fileToBeZipped.getAbsolutePath());
			}

			//Does the fileToBeZipped exist and is it a directory
			if(!fileToBeZipped.exists()){
				error.add("Invalid input file path.");
				return;
			}
		}

		//Used to check for duplicate input files this could probably be easier/more efficient
		for(int i = 0; i < filesToBeZipped.size(); i++){
			for(int j = 0; j < filesToBeZipped.size(); j++){
				if(j == i)
					continue;

				//Makes sure that there is no duplicate directories
				if(filesToBeZipped.get(i).getAbsolutePath().equals(filesToBeZipped.get(j).getAbsolutePath())){
					error.add("Duplicate Input Files, Skipping second occurence of " + filesToBeZipped.get(j).getAbsolutePath());
					filesToBeZipped.remove(j);
					continue;
				}
			}
		}

		//Does the destination equal nothing
		if(destination.equals("")){
			error.add("Invalid output file path.");
			return;
		}

		//Does the destination have a .zip extension
		if(!destination.getName().endsWith(".zip")){
			destination = new File(destination.getAbsolutePath() + ".zip");
		}

		//Does the destination exist and are overwrites ok
		if(destination.exists() && !overwritesOK){
			error.add("Output File Already Exists");
			return;
		}

		//Tries to create a ZipOutputStream
		try{
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destination), BUFFER_SIZE));
			//Create an object of an arraylist
			ArrayList<String> source = new ArrayList<String>();

			//Add all of the subfolders of all the input files into the sources
			for(File file:filesToBeZipped){
				source.addAll(new GenericFileIO().listAllFilesAndSubfoldersInDirectory(file));
			}
			
			//Is there more then one file to be added
			if(source.size() > 1){
				//This for statement is used to add all of the files in the directory to the zip file
				for(int i = 0; i < source.size(); i++){
					//This is the file object of the source string array at index i
					File sourceFile = new File(source.get(i));
					
					//Checks whether the file is a directory
					if(!(new File(source.get(i)).isDirectory())){
						//This creates an input stream for reading in the current file
						FileInputStream in = new FileInputStream(sourceFile);
						
						//Used to tell the program whether or not the directory for a file has been found
						boolean dirFound = false;
						//The zipEntry
						ZipEntry zipEntry = null;
						//Try to find the directory for the file
						for(String string:directoriesList){
							//	If the source file path contains the directory
							//make the zipEntry and tell the computer a directory was found
							if(source.get(i).contains(string)){
								//Is the whole file required to be zipped or just the contents
								if(wholeFile){
									//Make sure the file has a parent before trying to create it with one
									if(new File(string).getParent() != null){
										zipEntry = new ZipEntry(source.get(i).substring(new File(string).getParent().length()));
									}else{
										zipEntry = new ZipEntry(source.get(i).substring(string.length() + 1));
									}
								}else{
									zipEntry = new ZipEntry(source.get(i).substring(string.length() + 1));
								}
								dirFound = true;
								break;
							}
							//This is probably not needed in any way shape or form
//							else if(new File(source.get(i)).exists()){
//								zipEntry = new ZipEntry(new File(source.get(i)).getName());
//								dirFound = true;
//								break;
//							}
						}
						//	If the directory could not be found and the sources is not zero length or null 
						//and the file path represents an actual file that exists then make that the zipEntry
						if(source != null && !dirFound && source.size() > 0 && new File(source.get(i)).exists()){
							zipEntry = new ZipEntry(new File(source.get(i)).getName());
							dirFound = true;
						}
						
						//If no directory could be found the zipper crashes
						if(!dirFound){
							error.add("Invalid directory");
							in.close();
							return;
						}

						//Tries to put the next entry into the zip output stream
						try{
							zos.putNextEntry(zipEntry);
						}catch(ZipException e){
							//If putting the next entry has failed check if its because of a duplicate entry
							if(e.getLocalizedMessage().contains("duplicate entry")){
								error.add("Skipping file: " + zipEntry.getName() + " due to duplicate entry.");
								continue;
							}else{
								error.add(e.toString());
								in.close();
								return;
							}
						}

						//Make the currentFile the same as the current source file
						currentFile.add(source.get(i));

						//Used to store the information read in by the input stream
						byte[] buffer = new byte[BUFFER_SIZE];
						//Used to check the number of bytes read into buffer
						int length;
						//Reads through the file and saves it into the buffer then writes it into the zip file
						while ((length = in.read(buffer)) >= 0) {
							zos.write(buffer, 0, length);
						}
						in.close();
					}

					//Close the entry
					zos.closeEntry();
				}
			}else{
				//This is the file object of the source string array at index i
				File sourceFile = new File(source.get(0));

				//This creates an input stream for reading in the current file
				FileInputStream in = new FileInputStream(sourceFile);

				//This is used to make the actual entry into the zip file
				ZipEntry zipEntry = new ZipEntry(filesToBeZipped.get(0).getName());
				//This is where the zip file is initially populated
				zos.putNextEntry(zipEntry);

				//Make the currentFile the same as the current source file
				currentFile.add(source.get(0));

				//Used to store the information read in by the input stream
				byte[] buffer = new byte[BUFFER_SIZE];
				//Used to check the number of bytes read into buffer
				int length;
				//Reads through the file and saves it into the buffer then writes it into the zip file
				while ((length = in.read(buffer)) >= 0) {
					zos.write(buffer, 0, length);
				}
				in.close();

				//Close the entry
				zos.closeEntry();
			}

			zos.close();

			//If the encryption reference is not null then encrypt the file
			if(enc != null)
				enc.encryptFile(destination);

		}catch(Exception e){
			e.printStackTrace();
			//An unknown error has occured
			error.add("Unknown error");
		}
	}

	//This is the method to actually unzip a file
	private void _UnzipFile(File fileToBeUnzipped, File destination, boolean overwritesOK, Encryption enc){
		//For the file to be unzipped is it nothing, does it exist and does it end with a .zip file extension
		if(fileToBeUnzipped.getAbsolutePath().equals("") || !fileToBeUnzipped.exists() || !fileToBeUnzipped.getAbsolutePath().endsWith(".zip")){
			error.add("Please enter a valid zip file to the input.");
			return;
		}

		//Does the destination end with a .zip
		if(destination.getAbsolutePath().endsWith(".zip")){
			error.add("You can not unpack a zip file into a zip file.");
			return;
		}

		//If the destination exists and overwrites are not ok then exit
		if(destination.exists() && !overwritesOK){
			error.add("Output File Already Exists");
			return;
		}

		try{
			//You need to un-encrypt the file to read it
			if(enc != null)
				enc.encryptFile(fileToBeUnzipped);

			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(fileToBeUnzipped));
			ZipEntry entry = zipIn.getNextEntry();

			//If the entry is null before even trying to open anything then there is a problem
			if(entry == null){
				error.add("Invalid Encryption Key");
			}

			while (entry != null) {
				String filePath = destination.getAbsolutePath() + File.separator + entry.getName();
				File theFile = (new File(filePath)).getParentFile();

				//You need to make the directories in order for the program to make the new files
				theFile.mkdirs();

				//Used to tell the user the current file being worked on
				currentFile.add(filePath);

				//Used more for redundancy then anything and will help to make the program not crash
				if(!entry.isDirectory()){
					//Create the actual file to be written too
					(new File(filePath)).createNewFile();
					//Create the output stream
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));

					//A temporary variable to hold the bytes read in from the file
					byte[] bytesIn = new byte[BUFFER_SIZE];
					//Holds how many bytes were read in from the file
					int read = 0;
					//Performs the reading and writing of the new file
					while ((read = zipIn.read(bytesIn)) != -1) {
						bos.write(bytesIn, 0, read);
					}

					bos.close();
				}

				//Close the now complete entry
				zipIn.closeEntry();
				//Get the next entry
				entry = zipIn.getNextEntry();
			}
			zipIn.close();

			//Re-encrypt the zip file so no one can take it
			if(enc != null)
				enc.encryptFile(fileToBeUnzipped);
			
		}catch(Exception e){
			e.printStackTrace();

			//Unknown error has occured
			error.add("Unknown error");
		}
	}
}