package org.back;
//clave keystore=SHA384withRSA-123456
public class Main {

    public static void main(String[] args) {
        String keyStorePath = "C:\\Users\\thoma\\Desktop\\Universidad\\2025-03\\Backend\\Proyecto1\\Secure-Genomic-SSL-TCP\\Secure-Genomic-SSL-TCP\\tcp_server\\certs\\keystore.jks";
        String keyStorePassword = "123456";
        System.setProperty("javax.net.ssl.keyStore", keyStorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        TCPServer server = new TCPServer(2020);
        System.out.println("Starting secure server...");
        server.start();
    }
}