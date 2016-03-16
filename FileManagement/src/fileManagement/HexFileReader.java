package fileManagement;

import java.io.File;
import java.io.FileInputStream;


public class HexFileReader {

	public HexFileReader(){}

	public static String[] ReadFileInAsHex(String theFilePath){return _ReadFileInAsString(new File(theFilePath));}

	public static String[] ReadFileInAsHex(File theFile){return _ReadFileInAsString(theFile);}

	/********************************************************************************************************************
	 * 	The is the start of the private portion of the code
	 ********************************************************************************************************************/

	private static final int BUFFER_SIZE = 65536;

	private static byte buffer[] = new byte[BUFFER_SIZE];

	private static String[] _ReadFileInAsString(File theFile){
		String[] tempArr = null;
		String temp = "";

		try{
			FileInputStream input  = new FileInputStream(theFile);

			int readIn = 0;
			while((readIn = input.read(buffer)) >= 0){
				for(int i = 0; i < readIn; i++){
					temp += (char)buffer[i];
				}
			}

			temp = formatString(temp);

			tempArr = temp.split("\n");

			input.close();
			return tempArr;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	private static String formatString(String string){
		int count = 0;
		String temp = "";

		for(int i = 0; i < string.length(); i++){
			if(i % 128 == 0){
				temp += "\n" + count + ":";
				count += 128;
			}
			
			if(i < string.length() - 1){
				if(i % 2 == 0)
					temp += "\t";
				temp += Integer.toHexString(string.charAt(i+1));
			}else{
				if(i % 2 == 0)
					temp += "\t";
				temp += Integer.toHexString(string.charAt(i));
			}
		}

		return temp;
	}
}
// end class