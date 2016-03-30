/**
 * 
 */
package Communication;

/**
 * @author Daniel Thertell
 * @version 0.1
 * @since Mar 30, 2016
 */
public class CommTester {

	/**
	 * 
	 *@author Daniel Thertell
	 *@since Mar 30, 2016
	 *@param 
	 */
	public static void main(String[] args) {
		CommunicationServer server = new CommunicationServer();
		System.out.println("---STOP TEST---");
		server.stopServer();
		//System.out.println("I should be done...");
	}

}
