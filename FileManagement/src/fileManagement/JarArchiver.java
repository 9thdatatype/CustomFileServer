package fileManagement;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarArchiver {

	public void createArchive(File destinationJar, File tobeJared) throws Exception{
		_CreateArchive(destinationJar, tobeJared, null);
	}
	
	public void createArchive(File destinationJar, File tobeJared, FileInputStream manifest) throws Exception{
		_CreateArchive(destinationJar, tobeJared, manifest);
	}

	public void openArchive(File tobeUnJared, File destination) throws Exception{
		_OpenArchive(tobeUnJared, destination);
	}

	/************************************************************************************************************************************
	 * This is the start of the private portion of the code
	 ************************************************************************************************************************************/

	private static final int BUFFER_SIZE = 65536;

	private void _CreateArchive(File destinationJar, File tobeJared, FileInputStream manifest) throws Exception{
		JarOutputStream jos;
		if(manifest != null){
			jos = new JarOutputStream(new FileOutputStream(destinationJar), new Manifest(manifest));
		}else{
			jos = new JarOutputStream(new FileOutputStream(destinationJar));
		}
		ArrayList<String> source = new GenericFileIO(tobeJared).listAllFilesAndSubfoldersInDirectory();

		//This for statement is used to add all of the files in the directory to the zip file
		for(int i = 0; i < source.size(); i++){
			//This is the file object of the source string array at index i
			File sourceFile = new File(source.get(i));

			//Checks whether the file is a directory
			if(!(new File(source.get(i)).isDirectory())){
				//This creates an input stream for reading in the current file
				FileInputStream in = new FileInputStream(sourceFile);

				//This is used to make the actual entry into the zip file
				JarEntry jarEntry = new JarEntry(source.get(i).substring(tobeJared.getAbsolutePath().toCharArray().length + 1));
				//This is where the zip file is initially populated
				jos.putNextEntry(jarEntry);

				//Used to store the information read in by the input stream
				byte[] buffer = new byte[BUFFER_SIZE];
				//Used to check the number of bytes read into buffer
				int length;
				//Reads through the file and saves it into the buffer then writes it into the zip file
				while ((length = in.read(buffer)) >= 0) {
					jos.write(buffer, 0, length);
				}
				in.close();
			}

			//Close the entry
			jos.closeEntry();
		}

		jos.close();
	}

	private void _OpenArchive(File tobeUnJared, File destination) throws Exception{
		JarInputStream jis = new JarInputStream(new FileInputStream(tobeUnJared));
		JarEntry entry = jis.getNextJarEntry();

		while (entry != null) {
			String filePath = destination.getAbsolutePath() + File.separator + entry.getName();
			File theFile = (new File(filePath)).getParentFile();

			//You need to make the directories in order for the program to make the new files
			theFile.mkdirs();

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
	}

}
// end class