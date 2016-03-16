package fileManagement;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;

public class Zipper {

	public void createZipFile(File destination, File fileToBeZipped) throws Exception{
		_CreateZipFile(destination, fileToBeZipped);
	}

	public void unzipFile(File fileToBeUnzipped, File destination) throws Exception{
		_UnzipFile(fileToBeUnzipped, destination);
	}

	/***********************************************************************************************************************************************************************
	 * This is the start of the private portion of the code
	 ***********************************************************************************************************************************************************************/

	private static final int BUFFER_SIZE = 65536;

	private void _CreateZipFile(File destination, File fileToBeZipped) throws Exception{
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destination), BUFFER_SIZE));

		//This holds a list of every file in the directory to be zipped
		ArrayList<String> source = new GenericFileIO(fileToBeZipped).listAllFilesAndSubfoldersInDirectory();

		//This for statement is used to add all of the files in the directory to the zip file
		for(int i = 0; i < source.size(); i++){
			//This is the file object of the source string array at index i
			File sourceFile = new File(source.get(i));

			//Checks whether the file is a directory
			if(!(new File(source.get(i)).isDirectory())){
				//This creates an input stream for reading in the current file
				FileInputStream in = new FileInputStream(sourceFile);

				//This is used to make the actual entry into the zip file
				ZipEntry zipEntry = new ZipEntry(source.get(i).substring(fileToBeZipped.getAbsolutePath().toCharArray().length + 1));
				//This is where the zip file is initially populated
				zos.putNextEntry(zipEntry);

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

		zos.close();
	}

	private void _UnzipFile(File fileToBeUnzipped, File destination) throws Exception{
		ZipInputStream zipIn = new ZipInputStream(new FileInputStream(fileToBeUnzipped));
		ZipEntry entry = zipIn.getNextEntry();

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
	}

}
// end class