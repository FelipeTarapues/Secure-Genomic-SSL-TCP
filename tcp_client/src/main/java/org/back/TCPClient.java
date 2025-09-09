package org.back;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.List;

/**
 * Cliente SSL/TCP que implementa CREATE, RETRIEVE, UPDATE
 * y DELETE. Lee/Escribe con DataInputStream/DataOutputStream (readUTF/writeUTF).
 */
public class TCPClient {
    private final String host;
    private final int port;

    // Ajusta si tu truststore tiene otro nombre/clave
    private static final String TRUSTSTORE_PATH = "clientTruststore.jks";
    private static final String TRUSTSTORE_PASS = "123456";

    public TCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // --- Util: checksum SHA-256 hex ---
    private String sha256Hex(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] h = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : h) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // --- Configura truststore y crea SSLSocket ---
    private SSLSocket createSSLSocket() throws IOException {
        // establece truststore para que el cliente confíe en el certificado del servidor
        System.setProperty("javax.net.ssl.trustStore", TRUSTSTORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASS);
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        return (SSLSocket) factory.createSocket(host, port);
    }

    // Lee respuestas del servidor en un loop: puede enviar ALERT ... múltiples veces y finalmente OK / ERROR
    private void readResponses(DataInputStream in, alertas_loggin alertsLogger) throws IOException {
        while (true) {
            String resp;
            try {
                resp = in.readUTF();
            } catch (EOFException eof) {
                // conexión cerrada por el servidor
                break;
            }
            if (resp == null) break;
            if (resp.startsWith("ALERT")) {
                System.out.println("[ALERT] " + resp.substring(6)); // mensaje más legible
                if (alertsLogger != null) alertsLogger.append(resp);
                // seguir leyendo (podría venir más ALERTs o un OK)
            } else if (resp.startsWith("OK")) {
                System.out.println("[SERVER] " + resp);
                break;
            } else if (resp.startsWith("ERROR")) {
                System.out.println("[ERROR] " + resp);
                break;
            } else {
                // Texto libre (por ejemplo RETRIEVE devuelve un bloque con saltos)
                System.out.println("[SERVER] " + resp);
                // seguir leyendo hasta OK/ERROR o EOF
            }
        }
    }

    /**
     * CREATE: valida localmente, calcula checksum y file size, envía metadata y FASTA línea por línea.
     */
    public void createPatient(Paciente p, alertas_loggin alertsLogger) {
        try (SSLSocket socket = createSSLSocket();
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            // leer FASTA y calcular checksum/size
            Path fastaPath = Paths.get(p.getFastaPath());
            byte[] raw = Files.readAllBytes(fastaPath);
            String checksum = sha256Hex(raw);
            long size = Files.size(fastaPath);

            // metadata
            out.writeUTF("COMMAND: CREATE");
            out.writeUTF("full_name: " + p.getFullName());
            out.writeUTF("document_id: " + p.getDocumentId());
            out.writeUTF("age: " + p.getAge());
            out.writeUTF("sex: " + p.getSex());
            out.writeUTF("contact_email: " + p.getContactEmail());
            out.writeUTF("clinical_notes: " + p.getClinicalNotes());
            out.writeUTF("checksum_fasta: " + checksum);
            out.writeUTF("file_size_bytes: " + size);
            out.writeUTF("END_METADATA");

            // FASTA
            out.writeUTF("FASTA_BEGIN");
            List<String> lines = Files.readAllLines(fastaPath);
            for (String line : lines) out.writeUTF(line);
            out.writeUTF("FASTA_END");
            out.flush();

            // leer respuestas (ALERTs + OK/ERROR)
            readResponses(in, alertsLogger);

        } catch (Exception e) {
            System.out.println("Error en createPatient: " + e.getMessage());
        }
    }

    /**
     * RETRIEVE: pide datos del paciente por patient_id.
     */
    public void getPatient(String patientId) {
        try (SSLSocket socket = createSSLSocket();
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            out.writeUTF("COMMAND: RETRIEVE");
            out.writeUTF("patient_id: " + patientId);
            out.flush();

            // servidor normalmente devuelve un único bloque con info; readResponses imprimirá y terminará con OK/ERROR
            // pero en retrieve el servidor responde con una sola línea que contiene todos los campos (ya usamos readUTF)
            readResponses(in, null);

        } catch (Exception e) {
            System.out.println("Error en getPatient: " + e.getMessage());
        }
    }

    /**
     * UPDATE: actualiza notas y opcionalmente reemplaza FASTA. Si newFastaPath==null no envía FASTA.
     * out: "COMMAND: UPDATE", "patient_id: ...", "clinical_notes: ...", (si hay FASTA -> "checksum_fasta: ...", "file_size_bytes: ..."), "END_METADATA", ["FASTA_BEGIN", lines..., "FASTA_END"]
     */
    public void updatePatient(String patientId, String newNotes, String newFastaPath, alertas_loggin alertsLogger) {
        try (SSLSocket socket = createSSLSocket();
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            out.writeUTF("COMMAND: UPDATE");
            out.writeUTF("patient_id: " + patientId);
            out.writeUTF("clinical_notes: " + newNotes);

            if (newFastaPath != null && !newFastaPath.isBlank()) {
                Path fpath = Paths.get(newFastaPath);
                byte[] raw = Files.readAllBytes(fpath);
                String checksum = sha256Hex(raw);
                long size = Files.size(fpath);
                out.writeUTF("checksum_fasta: " + checksum);
                out.writeUTF("file_size_bytes: " + size);
                out.writeUTF("END_METADATA");
                // enviar FASTA
                out.writeUTF("FASTA_BEGIN");
                List<String> lines = Files.readAllLines(fpath);
                for (String line : lines) out.writeUTF(line);
                out.writeUTF("FASTA_END");
            } else {
                out.writeUTF("END_METADATA");
            }
            out.flush();

            // leer respuesta(s)
            readResponses(in, alertsLogger);

        } catch (Exception e) {
            System.out.println("Error en updatePatient: " + e.getMessage());
        }
    }

    /**
     * DELETE: borrado lógico en servidor.
     */
    public void deletePatient(String patientId) {
        try (SSLSocket socket = createSSLSocket();
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            out.writeUTF("COMMAND: DELETE");
            out.writeUTF("patient_id: " + patientId);
            out.flush();

            readResponses(in, null);

        } catch (Exception e) {
            System.out.println("Error en deletePatient: " + e.getMessage());
        }
    }
}
