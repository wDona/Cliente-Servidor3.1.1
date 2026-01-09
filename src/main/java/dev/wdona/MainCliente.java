package dev.wdona;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class MainCliente {
    public static void main(String[] args) {
        // Argumentos
        if (args.length != 2) {
            System.out.println("Uso: <host> <puerto>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            // Socket del servidor
            InetSocketAddress serverAddr = new InetSocketAddress(InetAddress.getByName(host), port);
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(1200); // 1.2s

            // Iniciar hilo Cliente (Receiver)
            Thread rx = new Thread(new Cliente(socket), "udp-rx");
            rx.setDaemon(true);
            rx.start();

            System.out.println("Conectado a " + host + ":" + port);
            System.out.println("Escribe texto para enviar o '/salir' para terminar");

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;

            // Envio de menasjes periodicos
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("/salir")) break;

                byte[] data = line.getBytes(StandardCharsets.UTF_8);
                DatagramPacket p = new DatagramPacket(data, data.length, serverAddr);
                socket.send(p);
            }

            socket.close();
            System.out.println("Cliente cerrado");

        } catch (IOException e) {
            System.err.println("Error en el cliente: " + e.getMessage());
        }
    }
}