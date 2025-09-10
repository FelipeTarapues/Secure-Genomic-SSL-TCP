package org.back;

import org.back.model.Disease;
import java.util.Map;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.Socket;

public class TCPServer {
    private final int serverPort;
    //Variable para almacenar la base de datos de enfermedades en memoria
    private Map<String, Disease> diseaseDb;

    public TCPServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public void start() {
        ServerLogger.log("Servidor SSL concurrente iniciando...");
        try {
            // Lógica para cargar la base de datos al iniciar el servidor
            DiseaseDatabase dbLoader = new DiseaseDatabase();
            dbLoader.load();
            this.diseaseDb = dbLoader.getDiseases();
            // Añadimos un log para confirmar la carga
            ServerLogger.log(this.diseaseDb.size() + " enfermedades cargadas exitosamente en la base de datos.");

            SSLServerSocketFactory serverSocketFactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket =
                    (SSLServerSocket) serverSocketFactory.createServerSocket(serverPort);


            ServerLogger.log("Servidor seguro iniciado en el puerto: " + serverPort + ". Esperando conexiones...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                ServerLogger.log("Conexión aceptada desde: " + clientSocket.getInetAddress());

                // Pasamos la base de datos cargada a cada nuevo hilo de ClientHandler
                ClientHandler clientHandler = new ClientHandler(clientSocket, this.diseaseDb);

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            ServerLogger.log("ERROR CRÍTICO del servidor: " + e.getMessage());
            e.printStackTrace(); // Mantenemos esto para ver el detalle completo en la consola durante el desarrollo
        }
    }
}