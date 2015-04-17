import java.net.*;
import java.util.Arrays;
import java.io.*;

//Copyright 2014, Karan Chadha and Avi Dubey, All rights reserved.

public class CHAT extends RSA {
	static ServerSocket serverSocket;
	static Socket aliceSocket;
	static Socket bobSocket;
	static BufferedReader aliceInput;
	static BufferedReader bobInput;
	static PrintWriter aliceOutput;
	static PrintWriter bobOutput;
	static BufferedReader alice_keyboardInput;
	static BufferedReader bob_keyboardInput;
	static boolean bob_connection = false;
	static boolean alice_connection = false;

	boolean chat_done = false;
	static long DES_key = 0;
	static String hybrid_key = null;

	public static void main(String[] args) {

		String address = "", key_file = "", port = "", nick_name = "";
		if (args.length == 0) {
			System.out.println("no command line options were provided");
			System.out.println("Retry with one of the following options");
			System.out
					.println("-<alice/bob> -e <key file>.public -p <port> -a <address>");
		}

		if (args.length > 0) {

			for (int i = 0; i < args.length; i++) {

				if (args[i].equals("-alice") || args[i].equals("-bob")) {
					for (int j = 0; j < args.length; j++) {
						if (args[j].equals("-a")) { // input filename
							address = args[j + 1];
						}
						if (args[j].equals("-e")) { // input filename
							key_file = args[j + 1];
						}
						if (args[j].equals("-p")) { // output filename
							port = args[j + 1];
						}
						if (args[j].equals("-alice") || args[j].equals("-bob")) { // key
																					// filename
							nick_name = args[j].substring(1);
						}

					}
				}
			}

			System.out.println("Configuration for chat recived:");
			System.out.println("Nick:" + nick_name + "\nKey File:" + key_file
					+ "\nport:" + port + "\naddress:" + address);

			try {
				chat_protocol(nick_name, key_file, port, address);
			} catch (Exception e) {
				System.out.println("Error");
			}
		}
	}

	private static void chat_protocol(String nick_name, String key_file,
			String port, String address) throws IOException {

		// ALICE
		if (nick_name.toLowerCase().equals("alice")) {

			System.out.println("\nInnitiating connection for alice");
			alice_connection(Integer.parseInt(port), address); // setting up
																// variables

			boolean received_key = false;
			while (alice_connection != false) {
				System.out.println("Receiving key from bob");
				received_key = receive_key();
				break;
			}
			boolean sent_ack = false;
			while (received_key != false) {
				System.out.println("Sending acknowledgment to Bob");
				System.out.println("*********************");
				sent_ack = send_ack();
				break;
			}
			System.out.println("Encrypted Chat Initiated");
			System.out.println("*********************");
			boolean msg_rcv = true;
			boolean msg_send = false;
			boolean chat_done = false;
			while (sent_ack != false) {
				// alice_send();
				// starts with receiving from bob
				// starting chat
				while (chat_done != true) {
					if (msg_rcv == true && msg_send == false) {
						alice_receive();
						msg_rcv = false;
						msg_send = true;
					} else if (msg_send == true && msg_rcv == false) {
						alice_send();
						msg_rcv = true;
						msg_send = false;
					}
					chat_done = false;
				}

			}

			// BOB
		} else if (nick_name.toLowerCase().equals("bob")) {

			bob_connection(key_file, Integer.parseInt(port), address); // setting
																		// up
																		// variables

			boolean sent_key = false;
			while (bob_connection != false) {
				System.out.println("Hybrid key process started");
				sent_key = send_key();
				break;
			}
			boolean received_ack = false;
			while (sent_key != false) {
				System.out.println("Receiving acknowledgment from Alice");
				System.out.println("*******************");
				received_ack = receive_ack();
				break;
			}
			System.out.println("Chat Initiated");
			System.out.println("*********************");
			boolean msg_rcv = false;
			boolean msg_send = true;
			boolean chat_done = false;
			while (received_ack != false) {
				// alice_send();
				// starts with receiving from bob
				// starting chat
				while (chat_done != true) {
					if (msg_send == true && msg_rcv == false) {
						bob_send();
						msg_rcv = true;
						msg_send = false;
					} else if (msg_rcv == true && msg_send == false) {
						bob_receive();
						msg_rcv = false;
						msg_send = true;
					}
					chat_done = false;
				}
			}

		} else {
			System.err.println("Error - Bad nickname, "
					+ "please run program with nick as \"alice\" or \"bob\"");
			closeall();
			System.exit(1);
		}

	}

	private static void alice_receive() {
		try {
			// Receiving message from client
			String inputMessage = aliceInput.readLine();
			while (inputMessage != null && inputMessage.length() != 0) {
				// DES Decrypt message and display
				Decrypt("temp2.txt", DES_key, "temp1.txt");
				inputMessage = ReadFile("temp1.txt");
				System.out.println("Bob : " + inputMessage);
				return;
			}

		} catch (SocketException e) {
			System.out.println("Error(alice_receive) : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error(alice_receive) : " + e.getMessage());
			e.printStackTrace();
		}

	}

	private static void bob_receive() {
		try {
			// Receiving message from server
			String inputMessage = bobInput.readLine();
			while (inputMessage != null && inputMessage.length() != 0) {
				// Displaying message on output screen of client
				// DES Decrypt message and display
				Decrypt("temp2.txt", DES_key, "temp1.txt");
				inputMessage = ReadFile("temp1.txt");
				System.out.println("Alice : " + inputMessage);
				return;
			}
		} catch (SocketException e) {
			System.out.println("Error(bob_send) : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error(bob_send) : " + e.getMessage());
			e.printStackTrace();
		}

	}

	private static void alice_send() {
		// String chattemp_plain = "temp1.txt";
		// String chattemp_encrypt = "temp2.txt";
		try {
			System.out.print("Alice : ");
			// Receiving Input from keyboard
			String outputMessage = null;
			while ((outputMessage = alice_keyboardInput.readLine()) != null) {
				// Sending the input received from keyboard to client
				// Encrypt message and send to BOB
				chat_temp("temp1.txt", outputMessage);
				Encrypt("temp1.txt", DES_key, "temp2.txt");
				outputMessage = ReadFile("temp2.txt");
				aliceOutput.println(outputMessage);
				return;
			}
		} catch (SocketException e) {
			System.out.println("Error(alice_send) : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error(alice_send) : " + e.getMessage());
			e.printStackTrace();
		}

	}

	private static void chat_temp(String chattemp_plain, String outputMessage) {
		try {
			FileWriter fstream = new FileWriter(chattemp_plain);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(outputMessage);
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("File write Error: " + e.getMessage());
		}
		return;
	}

	private static void bob_send() {
		String outputMessage = null;
		// String chattemp_plain = "temp1.txt";
		// String chattemp_encrypt = "temp2.txt";
		try {
			System.out.print("Bob : ");
			while ((outputMessage = bob_keyboardInput.readLine()) != null) {
				// encrypt message and send to alice
				chat_temp("temp1.txt", outputMessage);
				Encrypt("temp1.txt", DES_key, "temp2.txt");
				outputMessage = ReadFile("temp2.txt");
				bobOutput.println(outputMessage);
				return;

			}
		} catch (SocketException e) {
			System.out.println("Error(bob_send) : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error(bob_send) : " + e.getMessage());
			e.printStackTrace();
		}

	}

	private static void closeall() {
		try {
			System.out.println("Closing all I/O devices");
			aliceInput.close();
			aliceOutput.close();
			bobInput.close();
			serverSocket.close();
			aliceSocket.close();
			bobSocket.close();
			bob_connection = false;
			alice_connection = false;
		} catch (SocketException e) {
			System.out.println("Something went wrong : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Something went wrong : " + e.getMessage());
			e.printStackTrace();
		}

	}

	private static boolean receive_ack() throws IOException {
		String inputMessage = bobInput.readLine();
		if (inputMessage != null && inputMessage.length() != 0) {
			// Displaying message on output screen of server
		}
		// creating temp file containing encrypted acknowledgment
		chat_temp("temp1.txt", inputMessage);

		// Decryting the acknowledgment with DES
		Decrypt("temp1.txt", DES_key, "temp2.txt");
		String ack_cont = ReadFile("temp2.txt");
		System.out.println("Ack : " + ack_cont);
		if (ack_cont.contains(("OK"))) {
			System.out.println("Acknowledgment correctly recieved");
		} else {
			System.out.println("Acknowledgment incorrectly recieved\nExiting");
			closeall();
			System.exit(1);
		}
		return true;
	}

	private static boolean send_ack() {

		String ack = "OK";
		// creating file for "OK" message
		chat_temp("temp1.txt", ack);

		// Encrypting with DES
		Encrypt("temp1.txt", DES_key, "temp2.txt");
		String content = ReadFile("temp2.txt");

		// sending the contents of the file
		aliceOutput.println(content);
		return true;

	}

	private static boolean send_key() throws IOException {

		// create DES key
		DES_key = generateKey();
		String DESkey_file = "DES_key.txt";
		OutputStream fin = new FileOutputStream(DESkey_file);
		DataOutputStream din = new DataOutputStream(fin);
		din.writeLong(DES_key);

		// Create RSA key
		keygen("alice", 1024);

		// Encrypt DES key with RSA key to produce Hybrid key
		encrypt(DESkey_file, "hybridkey_e.txt", "alice.public");

		// Sending the key byte wise to alice
		File infile = new File("hybridkey_e.txt");
		FileInputStream finn = null;
		byte DESEncrypted[] = new byte[128];
		try {
			finn = new FileInputStream(infile);
			Arrays.fill(DESEncrypted, (byte) 0);
			finn.read(DESEncrypted);

		} catch (Exception e) {
			System.out.println("Error - Sending key");
		}
		String to_send = Arrays.toString(DESEncrypted);
		System.out.println("hybrid key generated");
		bobOutput.println(to_send);
		System.out.println("Hybrid key sent to Alice");

		return true;
	}

	private static boolean receive_key() throws IOException {
		String inputMessage = aliceInput.readLine();
		if (inputMessage != null && inputMessage.length() != 0) {

			// Displaying message on output screen of server
			// DES_temp("hybridkey_e2",inputMessage);
			// Decrypt the hybrid key by reading from keyfile

			FileOutputStream outfile = null;
			try {
				outfile = new FileOutputStream("hybridkey_e2.txt");
				String[] bytevalues = inputMessage.substring(1,
						inputMessage.length() - 1).split(",");
				byte[] bytes = new byte[bytevalues.length];
				for (int i = 0, len = bytes.length; i < len; i++) {
					bytes[i] = Byte.parseByte(bytevalues[i].trim());
				}
				outfile.write(bytes);
			} catch (Exception e) {// Catch exception if any
				System.err.println("Error - Receive key");
			}

			decrypt("hybridkey_e2.txt", "hybridkey_d.txt", "alice.private");
			String DESkey_file = "hybridkey_d.txt";
			InputStream fin = new FileInputStream(DESkey_file);
			DataInputStream din = new DataInputStream(fin);
			DES_key = din.readLong();
			System.out.println("DES Key (Hybrid key decrypted) : "
					+ Long.toHexString(DES_key).toUpperCase());
			return true;
		}
		return true;
	}

	private static void bob_connection(String key_file, int port, String address) {
		try {
			// Creating socket for communicating with server
			bobSocket = new Socket(address, port);
			// Obtaining the input stream of the client to clientInput object
			bobInput = new BufferedReader(new InputStreamReader(
					bobSocket.getInputStream()));
			// Obtaining the output stream of the client to clientOutput object
			bobOutput = new PrintWriter(bobSocket.getOutputStream(), true);
			// Obtaining the input stream of keyboard
			bob_keyboardInput = new BufferedReader(new InputStreamReader(
					System.in));
			bob_connection = true;
			System.out.println("*******************");
			System.out.println("Connected to alice");
			System.out.println("*******************");

		} catch (SocketException e) {
			System.out.println("Error(socket) : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error(IOException) : " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void alice_connection(int port, String address) { // alice
		try {
			// Initializing Server Socket for obtaining client requests
			serverSocket = new ServerSocket(port);
			System.out
					.println("Alice started and is ready to accept Bob connection");
			// Accepting client connection request and obtaining the socket for
			// communicating with client
			aliceSocket = serverSocket.accept();
			System.out.println("Bob connection accepted");
			// Obtaining the input stream of the server to serverInput object
			aliceInput = new BufferedReader(new InputStreamReader(
					aliceSocket.getInputStream()));
			// Obtaining the output stream of the server to serverOutput object
			aliceOutput = new PrintWriter(aliceSocket.getOutputStream(), true);
			// Obtaining the input stream of keyboard
			alice_keyboardInput = new BufferedReader(new InputStreamReader(
					System.in));
			alice_connection = true;
			// aliceOutput.println("Alice is ready");
		} catch (SocketException e) {
			System.out.println("Error(socket) : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error(IOException) : " + e.getMessage());
			e.printStackTrace();
		}

	}
}
