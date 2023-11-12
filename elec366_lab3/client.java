package elec366_lab3;

import java.io.*;
import java.net.*;

import javax.swing.*;

import java.awt.Color;
import java.awt.event.*;

public class client {

	static Socket clientSocket;
	static JTextArea receivedTextArea;
	// static JLabel countLabel;
	// static JLabel dateLabel;

	public static void main(String[] args) throws Exception {

		JFrame frame = new JFrame("Chatting Client");
		frame.setLayout(null);
		frame.setBounds(100, 100, 510, 580);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		JLabel statusLabel = new JLabel("Not Connected");
		statusLabel.setBounds(20, 40, 150, 30);
		statusLabel.setForeground(Color.RED);
		frame.getContentPane().add(statusLabel);

		JButton sendButton = new JButton("Send");
		sendButton.setBounds(400, 490, 75, 30);
		frame.getContentPane().add(sendButton);
		sendButton.setVisible(false);

		JButton connectButton = new JButton("Connect");
		connectButton.setBounds(300, 20, 100, 30);
		frame.getContentPane().add(connectButton);
		connectButton.setVisible(true);

		JLabel labelClientName = new JLabel("Client Name:");
		labelClientName.setBounds(20, 20, 300, 30);
		frame.getContentPane().add(labelClientName);
		labelClientName.setVisible(true);

		JLabel labelSend = new JLabel("Send to");
		labelSend.setBounds(20, 410, 300, 30);
		frame.getContentPane().add(labelSend);
		labelSend.setVisible(false);

		JTextField sendTextField = new JTextField();
		sendTextField.setBounds(20, 450, 360, 80);
		frame.getContentPane().add(sendTextField);
		sendTextField.setVisible(false);

		JTextField clientName = new JTextField("");
		clientName.setBounds(100, 20, 180, 30);
		frame.getContentPane().add(clientName);
		clientName.setVisible(true);

		receivedTextArea = new JTextArea();
		receivedTextArea.setBounds(20, 60, 450, 300);
		receivedTextArea.setEditable(true);
		frame.getContentPane().add(receivedTextArea);
		receivedTextArea.setVisible(false);

		JTextField privateMessage = new JTextField("");
		privateMessage.setBounds(100, 410, 180, 30);
		frame.getContentPane().add(privateMessage);
		privateMessage.setVisible(false);

		JTextArea clientMessages = new JTextArea("");
		clientMessages.setBounds(20, 420, 360, 80);
		clientMessages.setEditable(true);
		frame.getContentPane().add(clientMessages);
		clientMessages.setVisible(false);

		JScrollPane receivedTextAreaScroll = new JScrollPane(receivedTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		receivedTextAreaScroll.setBounds(20, 100, 460, 300);
		frame.getContentPane().add(receivedTextAreaScroll);
		receivedTextAreaScroll.setVisible(false);

		// Action listener when connect button is pressed
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					if (connectButton.getText().equals("Connect")) { // if pressed to connect

						// create a new socket to connect with the server application
						clientSocket = new Socket("localhost", 6789);

						// send the name

						// receive the reply (reject or accepted)

						// if (rejected)
						// {
						// reject, display a message rejected
						// }
						// else if connected {
						// call function StartThread
						StartThread();

						// make the GUI components visible, so the client can send and receive messages
						sendButton.setVisible(true);
						// labelSend.setVisible(true);
						sendTextField.setVisible(true);
						labelSend.setVisible(true);

						receivedTextArea.setVisible(true);
						receivedTextAreaScroll.setVisible(true);
						privateMessage.setVisible(true);
						// clientMessages.setVisible(true);

						statusLabel.setText("Connected");
						statusLabel.setForeground(Color.BLUE);

						// change the Connect button text to disconnect
						connectButton.setText("Disconnect");
						// }

					} else { // if pressed to disconnect

						// create an output stream and send a Remove message to disconnect from the
						// server
						DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
						outToServer.writeBytes("-Remove\n");

						// close the client's socket
						clientSocket.close();

						// make the GUI components invisible
						sendButton.setVisible(false);
						labelSend.setVisible(false);
						sendTextField.setVisible(false);
						receivedTextArea.setVisible(false);
						receivedTextAreaScroll.setVisible(false);
						privateMessage.setVisible(false);

						// change the Connect button text to connect
						connectButton.setText("Connect");
						statusLabel.setText("Not Connected");
						statusLabel.setForeground(Color.RED);

					}

				} catch (Exception ex) {
					System.out.println(ex.toString());
				}
			}
		});

		// Action listener when send button is pressed
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// create an output stream
					DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

					if (!sendTextField.getText().equals("")) { // if the send to textfield has a name then add "@sendTo
																// name:" to the beginning of the message and send it

						String sendingSentence = "-Compute," + sendTextField.getText() + "\n";
						outToServer.writeBytes(sendingSentence);

					}

				} catch (Exception ex) {
					System.out.println(ex.toString());
				}
			}
		});

		// Disconnect on close
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {

				try {

					// create an output stream and send a Remove message to disconnect from the
					// server
					DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
					outToServer.writeBytes("-Remove\n");

					// close the client's socket
					clientSocket.close();

					// make the GUI components invisible
					sendButton.setVisible(false);
					labelSend.setVisible(false);
					sendTextField.setVisible(false);
					receivedTextArea.setVisible(false);
					receivedTextAreaScroll.setVisible(false);

					// change the Connect button text to connect
					connectButton.setText("Connect");
					statusLabel.setText("Not Connected");
					statusLabel.setForeground(Color.RED);

					System.exit(0);

				} catch (Exception ex) {
					System.out.println(ex.toString());
				}

			}
		});

		frame.setVisible(true);

	}

	// Thread to always read messages from the server and print them in the textArea
	private static void StartThread() {

		new Thread(new Runnable() {
			@Override
			public void run() {

				try {

					// create a buffer reader and connect it to the socket's input stream
					BufferedReader inFromServer = new BufferedReader(
							new InputStreamReader(clientSocket.getInputStream()));

					String receivedSentence;

					// always read received messages and append them to the textArea
					while (true) {

						receivedSentence = inFromServer.readLine();
						// System.out.println(receivedSentence);

						if (receivedSentence.startsWith("-Date")) {

							// String[] strings = receivedSentence.split(";");
							// dateLabel.setText("Server's Date: " + strings[1]);

						} else if (receivedSentence.startsWith("-Results")) {

							String[] strings = receivedSentence.split(",");
							receivedTextArea.setText("Sum is: " + strings[1] + "\n");
							receivedTextArea.append("Average is: " + strings[2] + "\n");
							receivedTextArea.append("Minimum is: " + strings[3] + "\n");
							receivedTextArea.append("Maximum is: " + strings[4]);

						} else if (receivedSentence.startsWith("-Count")) {

							// String[] strings = receivedSentence.split(",");
							// countLabel.setText("Number of connected clients to the server: " + strings[1]
							// + "\n");

						}
					}

				} catch (Exception ex) {

				}

			}
		}).start();

	}

}