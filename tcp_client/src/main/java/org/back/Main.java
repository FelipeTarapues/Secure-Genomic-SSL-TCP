package org.back;

public class Main {
    public static void main(String[] args) {
        // host y puerto: ajusta si tu servidor corre en otro host/puerto
        String host = "localhost";
        int port = 5000;

        Menu menu = new Menu(host, port);
        menu.show();
    }
}
