package dev.wdona;

import javax.sound.midi.Receiver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Cliente implements Runnable {
    private static final int BUF_SIZE = 1024;

    private final DatagramSocket socket;

    public Cliente(DatagramSocket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        byte[] buf = new byte[BUF_SIZE];
        DatagramPacket p = new DatagramPacket(buf, buf.length);

        while (!socket.isClosed()) {
            try {
                socket.receive(p);
                String received = new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8);

                // Lógica de limpieza del prefijo PUB
                if (received.startsWith("PUB ")) {
                    System.out.println(received.substring(4)); // FIXME: ???
                } else {
                    System.out.println(received);
                }
            } catch (SocketTimeoutException e) {
                // Timeout alcanzado, el bucle continúa y verifica si el socket cerró
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    System.err.println("Error de E/S en recepción: " + e.getMessage());
                }
            }
        }
    }

    // --- Lógica Principal ---
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java UDPClient <host> <puerto>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            InetSocketAddress serverAddr = new InetSocketAddress(InetAddress.getByName(host), port);
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(1200); // Timeout de 1.2s

            // Iniciar hilo receptor
            Thread rx = new Thread(new Cliente(socket), "udp-rx");
            rx.setDaemon(true);
            rx.start();

            System.out.println("Conectado a " + host + ":" + port);
            System.out.println("Escribe texto para enviar o '/salir' para terminar.");

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;

            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("/salir")) break;

                byte[] data = line.getBytes(StandardCharsets.UTF_8);
                DatagramPacket p = new DatagramPacket(data, data.length, serverAddr);
                socket.send(p);
            }

            socket.close();
            System.out.println("Cliente cerrado.");

        } catch (IOException e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }
}
