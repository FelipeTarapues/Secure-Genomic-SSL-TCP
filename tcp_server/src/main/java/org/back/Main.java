package org.back;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    /** Main class to start the TCP server.
     * Configures SSL properties and initiates the server on the specified port.
     */
    public static void main(String[] args) {
        TCPServer server = new TCPServer(2020);
        server.start();
    }
}