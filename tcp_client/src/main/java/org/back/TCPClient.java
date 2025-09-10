package org.back;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TCPClient {
    private final String serverAddress;
    private final int serverPort;

    public TCPClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    // Este método se encargará de la comunicación para una sola transacción
    private String sendAndReceive(String command, byte[] fileBytes) {
        // Usamos try-with-resources para asegurar que todo se cierre
        try (
                SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(serverAddress, serverPort);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())
        ) {
            System.out.println("Connected securely to server...");


            out.writeUTF(command);
            System.out.println("Sending command: " + command);

            // Si hay un archivo para enviar (fileBytes no es null), lo enviamos
            if (fileBytes != null) {
                out.write(fileBytes);
                System.out.println("Sending file data (" + fileBytes.length + " bytes)...");
            }

            // Recibir y devolver la respuesta
            String response = in.readUTF();
            System.out.println("Received response: " + response);
            return response;

        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR|Connection error: " + e.getMessage();
        }
    }

    // --- Métodos para cada acción del menú ---

    public String sendCreatePatientRequest(Patient patient, byte[] fastaBytes) throws NoSuchAlgorithmException {
        int fileSize = fastaBytes.length;
        String checksum = calculateChecksumMD5(fastaBytes);
        patient.setChecksum_fasta(checksum);
        patient.setFile_size_bytes(fileSize);

        String commandLine = String.join("|",
                "CREATE_PATIENT",
                patient.getFull_name(),
                patient.getDocument_id(),
                String.valueOf(patient.getAge()),
                String.valueOf(patient.getSex()),
                patient.getContact_email(),
                patient.getClinical_notes(),
                checksum,
                String.valueOf(fileSize)
        );
        return sendAndReceive(commandLine, fastaBytes);
    }

    public String sendGetPatientRequest(String patientId) {
        String commandLine = "GET_PATIENT|" + patientId;
        return sendAndReceive(commandLine, null);
    }

    public String sendUpdatePatientRequest(String patientId, String field, String value) {
        String commandLine = String.join("|", "UPDATE_PATIENT", patientId, field, value);
        return sendAndReceive(commandLine, null);
    }

    public String sendDeletePatientRequest(String patientId) {
        String commandLine = "DELETE_PATIENT|" + patientId;
        return sendAndReceive(commandLine, null);
    }

    // --- Metodo de utilidad para el Checksum ---
    private String calculateChecksumMD5(byte[] fileBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(fileBytes);
        BigInteger no = new BigInteger(1, digest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
}