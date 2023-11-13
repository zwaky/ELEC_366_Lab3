package elec366_lab3;

import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class ChatServer {

	public static ArrayList<ClientServiceThread> Clients = new ArrayList<ClientServiceThread>();
	static int clientCount = 0;
	private static JFrame frame;
	private static JLabel connectionStatusLabel;

	public static void main(String[] args) throws Exception {
		setupGUI();
		ServerSocket welcomeSocket = new ServerSocket(6789);

		new Thread(() -> acceptClients(welcomeSocket)).start();
		new Thread(() -> updateClientCountAndDate()).start();
	}

	private static void setupGUI() {
		frame = new JFrame("Chatting Server");
		frame.setLayout(null);
		frame.setBounds(100, 100, 300, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		connectionStatusLabel = new JLabel("No Clients Connected");
		connectionStatusLabel.setBounds(80, 30, 200, 30);
		connectionStatusLabel.setForeground(Color.red);
		frame.getContentPane().add(connectionStatusLabel);
		frame.setVisible(true);
	}

	private static void acceptClients(ServerSocket welcomeSocket) {
		while (!welcomeSocket.isClosed()) {
			try {
				Socket connectionSocket = welcomeSocket.accept();
				BufferedReader inFromClient = new BufferedReader(
						new InputStreamReader(connectionSocket.getInputStream()));
				String clientName = inFromClient.readLine(); // Assume the first message is the client's name

				synchronized (Clients) {
					if (isNameTaken(clientName)) {
						new DataOutputStream(connectionSocket.getOutputStream()).writeBytes("Name already taken.\n");
						connectionSocket.close();
					} else {
						ClientServiceThread newClient = new ClientServiceThread(clientCount, connectionSocket,
								clientName, Clients);
						Clients.add(newClient);
						newClient.start();
						Clients.notify(); // Notify the waiting threads of a new connection
					}
				}
			} catch (Exception ex) {
				System.out.println("Error accepting client connection: " + ex.getMessage());
			}
		}
	}

	private static boolean isNameTaken(String name) {
		return Clients.stream().anyMatch(client -> client.getClientName().equals(name));
	}

	public static void updateClientCountLabel() {
		SwingUtilities.invokeLater(() -> {
			clientCount = Clients.size();
			connectionStatusLabel.setText(clientCount + (clientCount <= 1 ? " Client" : " Clients") + " Connected");
			connectionStatusLabel.setForeground(clientCount > 0 ? Color.blue : Color.red);
		});
	}

	public static void sendDateAndCountToAllClients() throws IOException {
		for (ClientServiceThread client : Clients) {
			client.sendDateAndCount();
		}
	}

	private static void updateClientCountAndDate() {
		while (true) {
			try {
				synchronized (Clients) {
					try {
						sendDateAndCountToAllClients();
						updateClientCountLabel();
					} catch (IOException e) {
						System.out.println("Error sending date and count to client: " + e.getMessage());
						// Handle errors if a client has disconnected
					}

					Clients.wait();
				}
			} catch (InterruptedException ex) {
				System.out.println("Update client count and date thread interrupted: " + ex.getMessage());// Handle the
																											// interrupt
																											// appropriately
				Thread.currentThread().interrupt(); // Preserve interrupt status
				break;
			}
		}
	}

}
