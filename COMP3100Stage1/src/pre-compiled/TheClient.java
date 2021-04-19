import java.net.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TheClient {

	// Create global variables for the socket, and the input and output, which we will need to write and read messages
	// between server and client
	private Socket socket = null;
	private BufferedReader in = null;
	private DataOutputStream out = null;
	private Server[] servers = new Server[1];
	private int largestServerIndex = 0;
	private String inputString;

	// Constructor for our client class: we connect the socket to the address 127.0.0.1 and to the port 50000, as
	// provided by the server, and we initialize the input variable (in) and the output (out)
	// in will be used for reading the messages sent by the server, while out will be used for writing messages 
	// to the server
	public TheClient() {
		try {
			socket = new Socket("localhost", 50000);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream()); 
		} catch (UnknownHostException i) {
			System.out.println("Error: " + i);
		} catch (IOException i) {
			System.out.println("Error: " + i);
		}
	}
    
	// We instantiate one client object in main, and we need some method that will start communication with the server,
	// which I called start().
	public static void main(String args[]) {
		TheClient client = new TheClient();
		client.start();
	}

	public void start() {
		// Start by sending HELO to the server
		write("HELO");
        //System.out.println("Sent HELLO");

		// Read the server reply
		inputString = read();
        //System.out.println("Received " + inputString);

		// Send message with AUTH and the client's username
		write("AUTH " + System.getProperty("user.name"));
        //System.out.println("Sent Auth " + System.getProperty("user.name"));

		// Read the server reply
		inputString = read();
        //System.out.println("Received " + inputString);

		// Read ds-system.xml, where we can get information about the server
		File file = new File("ds-system.xml");
		readFile(file);

		// Send message with REDY
		write("REDY");
		//System.out.println("Sent REDY");

		// Read the server reply
		inputString = read();
        //System.out.println("Received " + inputString);

		// allToLargest() algorithm starts here, where the client schedules all the jobs following the design
		// explained in the assignment document

		// After allToLargest() is done with scheduling, we start the quitting procedure
		try {
			write("QUIT");
            //System.out.println("Sent QUIT");

			// If the reply is QUIT, then we can close input, output and the socket
			inputString = read();
            //System.out.println("Received" + inputString);
			if (inputString.equals("QUIT")) {
				in.close();
				out.close();
				socket.close();
			}
		} catch (IOException i) {
			System.out.println("ERR: " + i);
		}
	}

	// This method parses through the XML file found at the path stated in the start() method. 
	// It iterates through the file looks for attributes found in the XML file.
	// It then stores those values in an array
	public void readFile(File file) {
		try {
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document systemDocument = builder.parse(file);
			systemDocument.getDocumentElement().normalize();
			
			NodeList serverNodeList = systemDocument.getElementsByTagName("server");
			servers = new Server[serverNodeList.getLength()];
			for (int i = 0; i < serverNodeList.getLength(); i++) {
				Element server = (Element) serverNodeList.item(i);
				String t = server.getAttribute("type");
				int c = Integer.parseInt(server.getAttribute("cores"));
				Server temp = new Server(i, t, c);
				servers[i] = temp;
			}
			largestServerIndex = findLargestServer();
		} catch (Exception i) {
			i.printStackTrace();
		}

	}

	// Returns the index of the largest server(CPU cores) in the array 
	// created by the readFile() method
	public int findLargestServer() {
		int largestServer = servers[0].id;
		for (int i = 0; i < servers.length; i++) {
			if (servers[i].cores > servers[largestServer].cores) {
				largestServer = servers[i].id;
			}
		}
		return largestServer;
	}

	public void write(String text) {
		try {
			out.write((text + "\n").getBytes());
			// System.out.print("SENT: " + text);
			out.flush();
		} catch (IOException i) {
			System.out.println("ERR: " + i);
		}
	}

	public String read() {
		String text = "";
		try {
            text = in.readLine();
			// System.out.print("RCVD: " + text);
			inputString = text;
		} catch (IOException i) {
			System.out.println("ERR: " + i);
		}
		return text;
	}
}