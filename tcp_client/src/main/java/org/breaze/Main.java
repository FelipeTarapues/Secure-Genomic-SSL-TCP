package org.breaze;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        TCPClient client = new TCPClient("127.0.0.1", 2020);
        client.sendMessage("Andres", "Montoya");
    }
}