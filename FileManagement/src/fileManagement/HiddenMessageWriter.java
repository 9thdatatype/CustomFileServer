package fileManagement;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

public class HiddenMessageWriter {

	public static final int RED_CHANNEL = 1;
	public static final int GREEN_CHANNEL = 2;
	public static final int BLUE_CHANNEL = 4;
	public static final int RGB_CHANNEL = RED_CHANNEL | GREEN_CHANNEL | BLUE_CHANNEL;
	public static String DEFAULT_TERMINATION_SEQUENCE = "^END^";

	public HiddenMessageWriter(File file){
		if(!setInternalFileReference(file))
			return;
	}

	public HiddenMessageWriter(File file, int channel){
		if(!setInternalFileReference(file))
			return;

		if(channel == 0){
			redChannel = true;
			greenChannel = false;
			blueChannel = false;
		}

		if((channel & RED_CHANNEL) != 0){
			redChannel = true;
		}

		if((channel & GREEN_CHANNEL) != 0){
			greenChannel = true;
		}

		if((channel & BLUE_CHANNEL) != 0){
			blueChannel = true;
		}
	}

	public void hideMessageInFile(String message, File destination){
		message += termSeq;

		_HideMessageInFile(message.getBytes(), destination);
	}

	public void hideMessageInFile(String message){
		message += termSeq;

		_HideMessageInFile(message.getBytes(), null);
	}

	public void writeToRedFilter(boolean red){redChannel = red;}
	public void writeToGreenFilter(boolean green){greenChannel = green;}
	public void writeToBlueFilter(boolean blue){blueChannel = blue;}
	public void changeTerminationSequence(String sequence){this.termSeq = sequence;}
	public void setSequentialMessageEncoding(boolean sequential){sequentialRGBValues = sequential;}

	/****************************************************************************************************************************
	 * 
	 * This is the start of the private portion of code
	 * 
	 ****************************************************************************************************************************/

	//The buffer size when reading files
	private static final int BUFFER_SIZE = 65536;

	//Internal reference to the file supplied to the constructor
	private File file;
	//Temporary file
	private File tempFile = new File("\\$tempFile");
	//Will the program write to the red filter
	private boolean redChannel = false;
	//Will the program write to the green filter
	private boolean greenChannel = false;
	//Will the program write to the blue filter
	private boolean blueChannel = false;
	//This is the character sequence that is the end of the message
	private String termSeq = DEFAULT_TERMINATION_SEQUENCE;
	//This tells the program whether the pixel rgb values are to be sequentially written or are all the same
	private boolean sequentialRGBValues = false;

	private boolean setInternalFileReference(File theFile){
		if(!theFile.exists()){
			if(!theFile.mkdir()){
				return false;
			}
		}else if(theFile.isDirectory()){
			return false;
		}else if(!theFile.canWrite()){
			return false;
		}

		this.file = theFile;
		return true;
	}

	private void _CreateTempFile(){
		try{
			tempFile = new File(tempFile.getAbsolutePath() + file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")));
			FileInputStream in = new FileInputStream(file);
			FileOutputStream out = new FileOutputStream(tempFile);

			//Used to store the information read in by the input stream
			byte[] buffer = new byte[BUFFER_SIZE];
			//Used to check the number of bytes read into buffer
			int length;
			//Reads through the file and saves it into the buffer then writes it into the zip file
			while ((length = in.read(buffer)) >= 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void deleteTempFile(){tempFile.delete();}

	private String bytesToBinaryString(byte bytes[]){
		String temp = "";

		for(byte i:bytes){
			for(int j = 7; j > -1; j--){
				if((i & (byte)(Math.pow(2, j))) == (byte)(Math.pow(2, j))){
					temp += "1";
				}else{
					temp += "0";
				}
			}
		}

		return temp;
	}
	
	private void _HideMessageInFile(byte array[], File destination){
		Color colour = null;
		boolean hasDestination = destination != null;

		if(!hasDestination)
			_CreateTempFile();

		try{
			BufferedImage img;
			if(!hasDestination)
				img = ImageIO.read(tempFile);
			else
				img = ImageIO.read(file);
			
			BufferedImage img2 = img;

			int counter = 0;
			String tempString = bytesToBinaryString(array);
			System.out.println(tempString);
			for(int i = 0; i < img.getHeight(); i++){
				for(int j = 0; j < img.getWidth(); j++){
					if(!sequentialRGBValues){
						colour = new Color(img.getRGB(j, i));

						int r = colour.getRed();
						int g = colour.getGreen();
						int b = colour.getBlue();

						int binaryDigit = 0;
						if(tempString.charAt(counter) == '1'){
							binaryDigit = 1;
						}
						if(redChannel){
							if(r % 2 == 0 && binaryDigit == 1){
								r++;
							}else if(r % 2 == 1 && binaryDigit == 0){
								r--;
							}
						}
						if(greenChannel){
							if(g % 2 == 0 && binaryDigit == 1){
								g++;
							}else if(g % 2 == 1 && binaryDigit == 0){
								g--;
							}
						}
						if(blueChannel){
							if(b % 2 == 0 && binaryDigit == 1){
								b++;
							}else if(b % 2 == 1 && binaryDigit == 0){
								b--;
							}
						}

						if(counter + 1 < tempString.length())
							counter++;

						colour = new Color(r, g, b);

						img2.setRGB(j, i, colour.getRGB());
					}else{
						for(int k = 1; k < 4; k++){
							colour = new Color(img.getRGB(j, i));

							int r = colour.getRed();
							int g = colour.getGreen();
							int b = colour.getBlue();

							int binaryDigit = 0;
							if(tempString.charAt(counter) == '1'){
								binaryDigit = 1;
							}
							if(redChannel && k == 1){
								if(r % 2 == 0 && binaryDigit == 1){
									r++;
								}else if(r % 2 == 1 && binaryDigit == 0){
									r--;
								}
								if(counter + 1 < tempString.length())
									counter++;
							}
							if(greenChannel && k == 2){
								if(g % 2 == 0 && binaryDigit == 1){
									g++;
								}else if(g % 2 == 1 && binaryDigit == 0){
									g--;
								}
								if(counter + 1 < tempString.length())
									counter++;
							}
							if(blueChannel && k == 3){
								if(b % 2 == 0 && binaryDigit == 1){
									b++;
								}else if(b % 2 == 1 && binaryDigit == 0){
									b--;
								}
								if(counter + 1 < tempString.length())
									counter++;
							}

							colour = new Color(r, g, b);

							img2.setRGB(j, i, colour.getRGB());
						}
					}
				}
			}

			if(hasDestination){
				if(!destination.exists()){
					ImageIO.write(img2, "png", ImageIO.createImageOutputStream(destination));
				}else{
					ImageIO.write(img2, "png", new FileOutputStream(destination));
				}
			}else{
				ImageIO.write(img2, "png", new FileOutputStream(file));
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		if(hasDestination)
			deleteTempFile();
	}
}
// end class