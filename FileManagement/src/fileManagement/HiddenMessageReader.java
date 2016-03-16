package fileManagement;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class HiddenMessageReader {

	public static final int RED_CHANNEL = 1;
	public static final int GREEN_CHANNEL = 2;
	public static final int BLUE_CHANNEL = 4;
	public static final int RGB_CHANNEL = RED_CHANNEL | GREEN_CHANNEL | BLUE_CHANNEL;
	public static String DEFAULT_TERMINATION_SEQUENCE = "^END^";

	public HiddenMessageReader(File file){
		if(!setInternalFileReference(file))
			return;
	}

	public HiddenMessageReader(File file, int channel){
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

	public String readMessageFromFile(){
		return _ReadMessageFromFile();
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

	//Internal Reference to the file
	private File file;
	//Should this class try to read from the red filter
	private boolean redChannel = false;
	//Should this class try to read from the green filter
	private boolean greenChannel = false;
	//Should this class try to read from the blue filter
	private boolean blueChannel = false;
	//This is the character sequence that is the end of the message
	private String termSeq = DEFAULT_TERMINATION_SEQUENCE;
	//This tells the program whether the pixel rgb values are to be sequentially read or are all the same
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

	private String _ReadMessageFromFile(){
		String returnString = "";
		Color colour;
		String binaryString = "";

		try{
			BufferedImage img = ImageIO.read(file);

			for(int i = 0; i < img.getHeight(); i++){
				for(int j = 0; j < img.getWidth(); j++){
					if(!sequentialRGBValues){
						colour = new Color(img.getRGB(j, i));

						if(redChannel){
							if((colour.getRed() % 2) == 0){
								binaryString += "0";
							}else{
								binaryString += "1";
							}
						}

						if(greenChannel){
							if((colour.getGreen() % 2) == 0){
								binaryString += "0";
							}else{
								binaryString += "1";
							}
						}

						if(blueChannel){
							if((colour.getBlue() % 2) == 0){
								binaryString += "0";
							}else{
								binaryString += "1";
							}
						}
						if(binaryString.length() == 8){
							returnString += (char)Byte.parseByte(binaryString, 2);
							binaryString = "";
						}


						if(returnString.contains(termSeq)){
							returnString = returnString.substring(0, returnString.indexOf(termSeq));
							i = img.getHeight();
							j = img.getWidth();
						}
					}else{
						for(int k = 1; k < 4; k++){
							colour = new Color(img.getRGB(j, i));

							if(redChannel && k == 1){
								if((colour.getRed() % 2) == 0){
									binaryString += "0";
								}else{
									binaryString += "1";
								}
								if(binaryString.length() == 8){
									returnString += (char)Byte.parseByte(binaryString, 2);
									binaryString = "";
								}
							}

							if(greenChannel && k == 2){
								if((colour.getGreen() % 2) == 0){
									binaryString += "0";
								}else{
									binaryString += "1";
								}
								if(binaryString.length() == 8){
									returnString += (char)Byte.parseByte(binaryString, 2);
									binaryString = "";
								}
							}

							if(blueChannel && k == 3){
								if((colour.getBlue() % 2) == 0){
									binaryString += "0";
								}else{
									binaryString += "1";
								}
								if(binaryString.length() == 8){
									returnString += (char)Byte.parseByte(binaryString, 2);
									binaryString = "";
								}
							}
						}
						if(returnString.contains(termSeq)){
							returnString = returnString.substring(0, returnString.indexOf(termSeq));
							i = img.getHeight();
							j = img.getWidth();
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		return returnString;
	}
}
// end class