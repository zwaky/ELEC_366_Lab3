package elec366_lab3;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClientServiceThread extends Thread {
	private String clientName; // Client's name
	private Socket connectionSocket; // Client's connection socket
	private ArrayList<ClientServiceThread> clients; // List of all clients connected to the server
	private DataOutputStream outToClient;

	public ClientServiceThread(int number, Socket connectionSocket, String clientName,
			ArrayList<ClientServiceThread> clients) {
		super("ClientServiceThread-" + clientName);
		this.clientName = clientName;
		this.connectionSocket = connectionSocket;
		this.clients = clients;
		try {
			this.outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			broadcastMessage(this.clientName + " connected");

		} catch (IOException e) {
			System.out.println("Error getting output stream: " + e.getMessage());
		}
	}

	public void run() {
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

			String clientSentence;

			while (true) {
				clientSentence = inFromClient.readLine();
				if (clientSentence != null) {
					// If it is a private message, the format should be "@[name] message"
					if (clientSentence.startsWith("@")) {
						sendPrivateMessage(clientSentence);
					} else {
						// Broadcast message to all clients
						broadcastMessage(clientSentence);
					}
				} else {
					// Client disconnected
					this.disconnectClient();
					break;
				}
			}

		} catch (IOException ex) {
			System.out.println(this.clientName + " disconnected.");
			this.disconnectClient();
		}
	}

	private void sendPrivateMessage(String message) throws IOException {
		// if the client uses the format is "@[name] message"
		int spaceIndex = message.indexOf(" ");
		if (spaceIndex != -1) {
			String toClientName = message.substring(1, spaceIndex);
			String messageContent = message.substring(spaceIndex + 1);

			synchronized (clients) {
				for (ClientServiceThread client : clients) {
					if (client.getClientName().equals(toClientName)) {
						client.outToClient.writeBytes("-Message," + this.clientName + ": " + messageContent + "\n");
						break;
					}
				}
			}
		}
	}

	private void broadcastMessage(String message) throws IOException {
		synchronized (clients) {
			for (ClientServiceThread client : clients) {
				// if (!client.getClientName().equals(this.clientName)) { // Do not send the
				// message to the sender only the
				// // other cluents
				// }
				client.outToClient.writeBytes("-Message," + this.clientName + ": " + message + "\n");
			}
		}
	}

	public void disconnectClient() {
		try {
			connectionSocket.close();
		} catch (IOException e) {
			System.out.println("Error closing the connection socket for client " + clientName);
		}
		synchronized (clients) {
			clients.remove(this);

			// Notify other clients
			if (clients.size() != 0) {
				clients.notify();

				// Notify the server if this was the last client in the list
			} else {
				ChatServer.updateClientCountLabel();
			}
			try {
				broadcastMessage(this.clientName + " disconnected");
			} catch (IOException e) {
				System.out.println("Error sending disconnect message: " + e.getMessage());
			}
		}
	}

	public String getClientName() {
		return this.clientName;
	}

	public void sendDateAndCount() throws IOException {
		Date now = new Date();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE, MMMM d, yyyy  h:m:s a z");// added a date and time
																								// feature but its
																								// causing problems in
																								// the Chatsrver
		outToClient.writeBytes("-Date;" + dateFormatter.format(now) + "\n");
		outToClient.writeBytes("-Count," + clients.size() + "\n");

		String names = new String("");

		for (ClientServiceThread client : clients) {
			names = names + client.clientName + ",";
		}
		outToClient.writeBytes("-Names," + names + "\n");

	}

}
