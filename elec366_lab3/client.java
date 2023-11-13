package elec366_lab3;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.Color;

public class Client {

	static Socket clientSocket;
	static DataOutputStream outToServer;
	static BufferedReader inFromServer;
	static JTextArea receivedTextArea;
	static JTextArea connectedClientsTextArea;
	static JLabel statusLabel;
	static JTextField clientNameField;
	static JTextField privateMessage;
	static JTextArea clientMessages;
	static JButton sendButton;
	static JButton connectButton;
	static JLabel labelSend;
	static JScrollPane receivedTextAreaScroll;
	static JScrollPane connectedClientsAreaScroll;
	static JLabel connectedClientsLabel;

	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame("Chatting Client");
		frame.setLayout(null);
		frame.setBounds(100, 100, 500, 550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		statusLabel = new JLabel("Not Connected");
		statusLabel.setBounds(20, 40, 150, 30);
		statusLabel.setForeground(Color.RED);
		frame.getContentPane().add(statusLabel);

		connectButton = new JButton("Connect"); // Initialize connectButton here
		connectButton.setBounds(300, 20, 100, 30);
		frame.getContentPane().add(connectButton);

		labelSend = new JLabel("Send to");
		labelSend.setBounds(20, 370, 300, 30);
		frame.getContentPane().add(labelSend);
		labelSend.setVisible(false);

		sendButton = new JButton("Send");
		sendButton.setBounds(400, 450, 75, 30);
		sendButton.setVisible(false);
		frame.getContentPane().add(sendButton);
		sendButton.addActionListener(e -> sendButtonAction());

		connectButton.addActionListener(e -> connectButtonAction());

		JLabel labelClientName = new JLabel("Client Name:");
		labelClientName.setBounds(20, 20, 300, 30);
		frame.getContentPane().add(labelClientName);

		clientNameField = new JTextField("");
		clientNameField.setBounds(100, 20, 180, 30);
		frame.getContentPane().add(clientNameField);

		receivedTextArea = new JTextArea();
		receivedTextArea.setBounds(20, 60, 320, 300);
		receivedTextArea.setEditable(false);
		frame.getContentPane().add(receivedTextArea);

		receivedTextAreaScroll = new JScrollPane(receivedTextArea);
		receivedTextAreaScroll.setBounds(20, 60, 320, 300);
		receivedTextAreaScroll.setVisible(true);
		frame.getContentPane().add(receivedTextAreaScroll);

		connectedClientsTextArea = new JTextArea();
		connectedClientsTextArea.setBounds(350, 100, 120, 260);
		connectedClientsTextArea.setEditable(false);
		connectedClientsTextArea.setVisible(false);
		frame.getContentPane().add(connectedClientsTextArea);

		connectedClientsAreaScroll = new JScrollPane(connectedClientsTextArea);
		connectedClientsAreaScroll.setBounds(350, 100, 120, 260);
		connectedClientsAreaScroll.setVisible(false);
		frame.getContentPane().add(connectedClientsAreaScroll);

		connectedClientsLabel = new JLabel("Connected Clients: ");
		connectedClientsLabel.setBounds(350, 60, 120, 30);
		frame.getContentPane().add(connectedClientsLabel);
		connectedClientsLabel.setVisible(false);

		privateMessage = new JTextField("");
		privateMessage.setBounds(100, 370, 180, 30);
		privateMessage.setVisible(false);
		frame.getContentPane().add(privateMessage);

		clientMessages = new JTextArea("");
		clientMessages.setBounds(20, 420, 360, 80);
		clientMessages.setVisible(false);
		frame.getContentPane().add(clientMessages);

		frame.setVisible(true);
	}

	private static void connectButtonAction() {
		try {
			if (connectButton.getText().equals("Connect")) {
				clientSocket = new Socket("localhost", 6789);
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				String clientName = clientNameField.getText();
				outToServer.writeBytes(clientName + "\n");

				new Thread(() -> listenForServerMessages()).start();

				statusLabel.setText("Connected");
				statusLabel.setForeground(Color.BLUE);
				connectButton.setText("Disconnect");

				clientNameField.setEditable(false);

				labelSend.setVisible(true);
				privateMessage.setVisible(true);
				sendButton.setVisible(true);
				clientMessages.setVisible(true);
				connectedClientsTextArea.setVisible(true);
				connectedClientsAreaScroll.setVisible(true);
				connectedClientsLabel.setVisible(true);

			} else {
				// outToServer.writeBytes("-Remove\n");
				clientSocket.close();

				statusLabel.setText("Not Connected");
				statusLabel.setForeground(Color.RED);
				connectButton.setText("Connect");

				clientNameField.setEditable(true);

				labelSend.setVisible(false);
				privateMessage.setVisible(false);
				sendButton.setVisible(false);
				clientMessages.setVisible(false);
				connectedClientsTextArea.setVisible(false);
				connectedClientsAreaScroll.setVisible(false);
				connectedClientsLabel.setVisible(false);

			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void sendButtonAction() {
		try {
			String message = clientMessages.getText();
			if (!privateMessage.getText().isEmpty()) {
				// Cannot sent private message to yourself
				if (privateMessage.getText().equals(clientNameField.getText()))
					return;

				message = "@" + privateMessage.getText() + " " + message;
			}
			outToServer.writeBytes(message + "\n");
			clientMessages.setText("");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void listenForServerMessages() {
		try {
			String messageFromServer;
			while ((messageFromServer = inFromServer.readLine()) != null) {
				final String originalMessage = messageFromServer;

				if (originalMessage.startsWith("-Date;")) {
					String finalMessage = originalMessage.substring("-Date;".length());
					SwingUtilities.invokeLater(() -> receivedTextArea.append(finalMessage + "\n"));

				} else if (originalMessage.startsWith("-Names,")) {
					String[] names = originalMessage.split(",");

					StringBuilder namesText = new StringBuilder();
					for (int i = 1; i < names.length; i++) {
						namesText.append(names[i]).append("\n");
					}

					SwingUtilities.invokeLater(() -> connectedClientsTextArea.setText(namesText.toString()));

				} else if (originalMessage.startsWith("-Count,")) {
					String finalMessage = originalMessage.substring("-Count,".length());
					SwingUtilities
							.invokeLater(() -> connectedClientsLabel.setText("Connected Clients: " + finalMessage));

				} else if (originalMessage.startsWith("-Message,")) {
					String finalMessage = originalMessage.substring("-Message,".length());
					SwingUtilities.invokeLater(() -> receivedTextArea.append(finalMessage + "\n"));

				} else {
					SwingUtilities.invokeLater(() -> receivedTextArea.append(originalMessage + "\n"));
				}

			}
		} catch (IOException e) {
			if (e instanceof SocketException && e.getMessage().equals("Socket closed")) {
				// Handle if this was the last connected client

			} else {
				e.printStackTrace();
			}
		}
	}

}
