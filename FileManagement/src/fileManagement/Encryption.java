package fileManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/*
 * Improvements to be made are:
 * 		- More methods should be created to make this class better for reuse
 * 		- Add comments to this to explain what is going on
 * 		- Figure out why the encryptBytes method doesn't always work, I think it might be the fact that i was trying to encrypt compressed files
 * 		- Should the key be of dynamic size or absolute size
 * 				- Absolute can guarantee a minimum security level
 * 				- dynamic is a get what you put in kind of security level
 * 		- Test the encryption on files to make sure that you can't just get the password
 */

public class Encryption {

	/**
	 * Basic Constructor to set up the encryption algorithms
	 * @param eKey This is the encryption key to be used for the encrypting algorithm
	 */
	public Encryption(String eKey) {
		if (eKey == null || eKey.equals(""))
			return;

		key = eKey.toCharArray();
	}

	/**
	 * Encrypts a file based on the filePath
	 * @param filePath this is the whole file path to the file being encrypted
	 * @return returns true if successful
	 */
	public boolean encryptFile(String filePath) {
		return _EncryptFile(new File(filePath));
	}

	/**
	 * Encrypts a file based on the File object
	 * @param file this is the File object to the file being encrypted
	 * @return returns true if successful
	 */
	public boolean encryptFile(File file) {
		return _EncryptFile(file);
	}

	/**
	 * Encrypts an array of bytes
	 * @param bytesFromFile the array of bytes to be encrypted
	 * @return returns an array of encrypted bytes
	 */
	public byte[] encryptBytes(byte[] bytesFromFile) {
		// A temporary array to hold the encrypted values
		byte[] temp = new byte[bytesFromFile.length];

		// Used to increment the key index
		int j = 0;
		// Loops through all the bytesFromFile to encrypt it all
		for (int i = 0; i < BUFFER_SIZE; i++) {
			// Is the j index longer then the key size? If it is make it zero
			// again
			if (j >= key.length)
				j = 0;

			// Take the bytesFromFile at index i and Xor it with the key at
			// index j
			temp[i] = (byte) (bytesFromFile[i] ^ (byte) key[j]);
			// Increment j to the next index value
			j++;
		}

		// Return the temporary array
		return temp;
	}

	/********************************************************************************************************
	 * This marks the start of the private portion of the code
	 *********************************************************************************************************/

	// This is the buffersize
	private final int BUFFER_SIZE = 1024;
	// This holds the key as a char array
	private char key[];
	// Just to have a global reference to a SimplFileIO
	private GenericFileIO sfio;

	// Used to encrypt a file
	private boolean _EncryptFile(File file) {
		// A temporary byte array to hold the bytes being read in from the file
		byte temp[] = new byte[BUFFER_SIZE];
		// A temporary file to be created and then later destroyed. Holds the
		// file while it is encrypted.
		File tempFile = new File("\\Encrypt.tmp");

		try {
			// Used to read in the file
			FileInputStream in = new FileInputStream(file);
			// Used to output to the temp file
			FileOutputStream out = new FileOutputStream(tempFile);

			// nRead is used to tell the functions how many bytes have been read
			// in from the file
			int nRead = 0;
			while ((nRead = in.read(temp, 0, BUFFER_SIZE)) != -1) {
				// write the encrypted bytes from the temp array
				out.write(encryptBytes(temp), 0, nRead);
			}

			// Close the resources
			in.close();
			out.close();

			// Create an empty reference to the SimpleFileIO class
			sfio = new GenericFileIO();

			// Change the encrypted file to the file you just deleted
			sfio.changeFileDirDest(tempFile, file);

			// Make sure the temporary file is deleted
			tempFile.delete();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}// end class