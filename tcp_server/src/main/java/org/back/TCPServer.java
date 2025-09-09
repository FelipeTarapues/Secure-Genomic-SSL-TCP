package org.back;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/** * A simple TCP server that listens for incoming connections,
 * processes messages, and sends responses.
 */
public class TCPServer {
    private int serverPort;
    public TCPServer(int serverPort){
        this.serverPort = serverPort;
    }

    public void start(){
        try{
            // Creating a server socket to listen on the specified port
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("Server started on port: " + serverPort);
            while(true){
                Socket clientSocket = serverSocket.accept();
                DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                String message = dis.readUTF();
                String[] parts = message.split(":");
                System.out.println("Received message: " + message);
                String response = "Name "+parts[0]+" Last Name "+parts[1];
                out.writeUTF(response);
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }

    }
}
