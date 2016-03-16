package fileManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import java.util.ArrayList;
import java.util.Stack;

import java.util.jar.JarOutputStream;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Improvements to be made are:
 * 		- Make the methods here infallible
 */
@SuppressWarnings("unused")
public class EnhancedJarArchiver {

	public void createArchive(String destinationJar, String tobeJared, FileInputStream manifest, boolean overwritesOK, Encryption enc){
		if(tobeJared.split(";").length > 1){
			String filesAsString[] = tobeJared.split(";");
			ArrayList<File> files = new ArrayList<File>();
			for(String string:filesAsString){
				files.add(new File(string));
			}
			_CreateArchiveIntermediate(new File(destinationJar), files, manifest, overwritesOK, enc);
		}else{
			ArrayList<File> file = new ArrayList<File>();
			file.add(new File(tobeJared));
			_CreateArchiveIntermediate(new File(destinationJar), file, manifest, overwritesOK, enc);
		}
	}

	public void createArchive(File destinationJar, File tobeJared, FileInputStream manifest, boolean overwritesOK, Encryption enc){
		ArrayList<File> temp = new ArrayList<File>(1);
		temp.add(tobeJared);
		_CreateArchiveIntermediate(destinationJar, temp, manifest, overwritesOK, enc);
	}

	public void createArchive(File destinationJar, File tobeJared[], FileInputStream manifest, boolean overwritesOK, Encryption enc){
		ArrayList<File> temp = new ArrayList<File>();
		for(File file:tobeJared){
			temp.add(file);
		}
		_CreateArchiveIntermediate(destinationJar, temp, manifest, overwritesOK, enc);
	}

	public void openArchive(File tobeUnJared, File destination, boolean overwritesOK, Encryption enc){_OpenArchiveIntermediate(tobeUnJared, destination, overwritesOK, enc);}

	public boolean hasError(){return !errors.isEmpty();}

	public String getNextError(){return errors.poll();}

	public void setThreading(boolean threading){this.threading = threading;}
	
	public void setArchiveWholeFile(boolean wholeFile){this.wholeFile = wholeFile;}

	public boolean isComplete(){return !thread.isAlive();}

	/***************************************************************************************************************************************
	 * This is the start of the private portion of the code
	 ***************************************************************************************************************************************/

	private static final int BUFFER_SIZE = 65536;

	private Thread thread;

	private boolean threading = false;

	private ConcurrentLinkedQueue<String> currentFile = new ConcurrentLinkedQueue<String>();
	private ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<String>();
	private boolean wholeFile = true;

	private void _CreateArchiveIntermediate(File destinationJar, ArrayList<File> tobeJared, FileInputStream manifest, boolean overwritesOK, Encryption enc){
		if(threading){
			thread = new Thread(){
				public void run(){
					_CreateArchive(destinationJar, tobeJared, manifest, overwritesOK, enc);
				}
			};
			thread.start();
		}else{
			_CreateArchive(destinationJar, tobeJared, manifest, overwritesOK, enc);
		}
	}

	private void _OpenArchiveIntermediate(File tobeUnJared, File destination, boolean overwritesOK, Encryption enc){
		if(threading){
			thread = new Thread(){
				public void run(){
					_OpenArchive(tobeUnJared, destination, overwritesOK, enc);
				}
			};
			thread.start();
		}else{
			_OpenArchive(tobeUnJared, destination, overwritesOK, enc);
		}
	}

	//This is the function that adds files to a jar file recursively
	private void _CreateArchive(File destinationJar, ArrayList<File> tobeJared, FileInputStream manifest, boolean overwritesOK, Encryption enc){
		//This is a list of all the input directories that have been supplied
		ArrayList<String> directoriesList = new ArrayList<String>();

		//Does input validation on all of the elements of the tobeJared arraylist
		for(File fileToBeArchived:tobeJared){
			//Does the fileToBeZipped having a name at all
			if(fileToBeArchived.getName().equals("")){
				errors.add("Invalid input path.");
				return;
			}

			//If the file exists and is a directory then add it to the directories list
			if(fileToBeArchived.exists() && fileToBeArchived.isDirectory()){
				directoriesList.add(fileToBeArchived.getAbsolutePath());
			}

			//Does the fileToBeZipped exist and is it a directory
			if(!fileToBeArchived.exists()){
				errors.add("Please enter a valid input file path.");
				return;
			}
		}

		//Does the destination equal nothing
		if(destinationJar.equals("")){
			errors.add("Please enter a valid output file path.");
			return;
		}

		//Does the destination have a .zip extension
		if(!destinationJar.getName().endsWith(".jar")){
			destinationJar = new File(destinationJar.getAbsolutePath() + ".jar");
		}

		//Does the destination exist and are overwrites ok
		if(destinationJar.exists() && !overwritesOK){
			errors.add("Output File Already Exists");
			return;
		}

		//Used to check for duplicate input files this could probably be easier/more efficient
		for(int i = 0; i < tobeJared.size(); i++){
			for(int j = 0; j < tobeJared.size(); j++){
				if(j == i)
					continue;

				//Makes sure that there is no duplicate directories
				if(tobeJared.get(i).getAbsolutePath().equals(tobeJared.get(j).getAbsolutePath())){
					errors.add("Duplicate Input Files, Skipping second occurence of " + tobeJared.get(j).getAbsolutePath());
					tobeJared.remove(j);
					continue;
				}
			}
		}

		try{
			JarOutputStream jos;
			if(manifest != null){
				jos = new JarOutputStream(new FileOutputStream(destinationJar), new Manifest(manifest));
			}else{
				jos = new JarOutputStream(new FileOutputStream(destinationJar));
			}
			//Create an object of an arraylist
			ArrayList<String> source = new ArrayList<String>();
			
			//Add all of the subfolders of all the input files into the sources
			for(File file:tobeJared){
				source.addAll(new GenericFileIO().listAllFilesAndSubfoldersInDirectory(file));
			}

			//Is there more then one file to be added
			if(source.size() > 1){
				//This for statement is used to add all of the files in the directory to the jar file
				for(int i = 0; i < source.size(); i++){
					//This is the file object of the source string array at index i
					File sourceFile = new File(source.get(i));
					
					System.out.println(source.get(i));

					//Checks whether the file is a directory
					if(!(new File(source.get(i)).isDirectory())){
						//This creates an input stream for reading in the current file
						FileInputStream in = new FileInputStream(sourceFile);

						//Used to tell the program whether or not the directory for a file has been found
						boolean dirFound = false;
						//The zipEntry
						JarEntry jarEntry = null;
						//Try to find the directory for the file
						for(String string:directoriesList){
							//	If the source file path contains the directory else if the source file path exists and isn't a directory
							//make the zipEntry and tell the computer a directory was found
							if(source.get(i).contains(string)){
								//Is the whole file required to be archived or just the contents
								if(wholeFile){
									jarEntry = new JarEntry(source.get(i).substring(new File(string).getParent().length()));
								}else{
									jarEntry = new JarEntry(source.get(i).substring(string.length() + 1));
								}
								dirFound = true;
								break;
							}
							//TODO: Make sure this code is not needed in any circumstance
//							else if(new File(source.get(i)).exists()){
//								jarEntry = new JarEntry(new File(source.get(i)).getName());
//								dirFound = true;
//								break;
//							}
						}
						//	If the directory could not be found and the sources is not zero length or null 
						//and the file path represents an actual file that exists then make that the zipEntry
						if(source != null && !dirFound && source.size() > 0 && new File(source.get(i)).exists()){
							jarEntry = new JarEntry(new File(source.get(i)).getName());
							dirFound = true;
						}

						//If no directory could be found the zipper crashes
						if(!dirFound){
							errors.add("Invalid directory");
							in.close();
							return;
						}

						//Tries to put the next entry into the jar output stream
						try{
							jos.putNextEntry(jarEntry);
						}catch(JarException e){
							//If putting the next entry has failed check if its because of a duplicate entry
							if(e.getLocalizedMessage().contains("duplicate entry")){
								errors.add("Skipping file: " + jarEntry.getName() + " due to duplicate entry.");
								continue;
							}else{
								errors.add(e.toString());
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
						//Reads through the file and saves it into the buffer then writes it into the jar file
						while ((length = in.read(buffer)) >= 0) {
							jos.write(buffer, 0, length);
						}
						in.close();
					}

					//Close the entry
					jos.closeEntry();
				}
			}else{
				//This is the file object of the source string array at index i
				File sourceFile = new File(source.get(0));

				//This creates an input stream for reading in the current file
				FileInputStream in = new FileInputStream(sourceFile);

				//This is used to make the actual entry into the jar file
				JarEntry jarEntry = new JarEntry(tobeJared.get(0).getName());
				//This is where the jar file is initially populated
				jos.putNextEntry(jarEntry);

				//Make the currentFile the same as the current source file
				currentFile.add(source.get(0));

				//Used to store the information read in by the input stream
				byte[] buffer = new byte[BUFFER_SIZE];
				//Used to check the number of bytes read into buffer
				int length;
				//Reads through the file and saves it into the buffer then writes it into the jar file
				while ((length = in.read(buffer)) >= 0) {
					jos.write(buffer, 0, length);
				}
				in.close();

				//Close the entry
				jos.closeEntry();
			}

			jos.close();

			if(enc != null)
				enc.encryptFile(destinationJar);

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void _OpenArchive(File tobeUnJared, File destination, boolean overwritesOK, Encryption enc) {
		//For the file to be unzipped is it nothing, does it exist and does it end with a .zip file extension
		if(tobeUnJared.getAbsolutePath().equals("") || !tobeUnJared.exists() || !tobeUnJared.getAbsolutePath().endsWith(".jar")){
			errors.add("Please enter a valid jar file to the input.");
			return;
		}

		//Does the destination end with a .zip
		if(destination.getAbsolutePath().endsWith(".jar")){
			errors.add("You can not unpack a jar file into a jar file.");
			return;
		}

		//If the destination exists and overwrites are not ok then exit
		if(destination.exists() && !overwritesOK){
			errors.add("Output File Already Exists");
			return;
		}

		try{
			//If the file was encrypted you need to un-encrypt the file to read it
			if(enc != null)
				enc.encryptFile(tobeUnJared);

			JarInputStream jis = new JarInputStream(new FileInputStream(tobeUnJared));
			JarEntry entry = jis.getNextJarEntry();

			//If the entry is null before even trying to open anything then there is a problem
			if(entry == null){
				errors.add("Invalid Encryption Key");
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
					while ((read = jis.read(bytesIn)) != -1) {
						bos.write(bytesIn, 0, read);
					}

					bos.close();
				}

				//Close the now complete entry
				jis.closeEntry();
				//Get the next entry
				entry = jis.getNextJarEntry();
			}
			jis.close();

			//Re-encrypt the jar file so no one can take it
			if(enc != null)
				enc.encryptFile(tobeUnJared);

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
// end class