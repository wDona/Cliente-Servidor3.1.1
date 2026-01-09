package dev.wdona;

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

                // Prefijo PUB
                if (received.startsWith("PUB ")) {
                    System.out.println(received.substring(4));
                } else {
                    System.out.println(received);
                }
                
            // Timeout alcanzado, el bucle continua y verifica si el socket cerro
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    // System.err.println("Nada recibido: " + e.getMessage());
                }
            }
            
        }
    }
}
