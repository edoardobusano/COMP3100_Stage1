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
		try {
			out.write(("HELO\n").getBytes());
			out.flush();
		} catch (IOException i) {
			System.out.println("ERR: " + i);
		}

		// Read the server reply
		String text = "";
		try {
            text = in.readLine();
			// System.out.print("RCVD: " + text);
		} catch (IOException i) {
			System.out.println("ERR: " + i);
		}
		System.out.println(text);

		// Send message with AUTH and the client's username
		try {
			out.write(("AUTH" + System.getProperty("user.name") + "\n").getBytes());
			out.flush();
		} catch (IOException i) {
			System.out.println("ERR: " + i);
		}

		// Read the server reply
		try {
            text = in.readLine();
			// System.out.print("RCVD: " + text);
		} catch (IOException i) {
			System.out.println("ERR: " + i);
		}
		System.out.println(text);

		// Read ds-system.xml, where we can get information about the server
		File file = new File("ds-system.xml");
		readFile(file);

		// Send message with REDY
		// Read the server reply
	}

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
		} catch (Exception i) {
			i.printStackTrace();
		}

	}
}