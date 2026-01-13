package dev.wdona;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static final int BUF_SIZE = 1024;
    public static void main(String[] args) {
        final int puerto = 8080;
        
        Set<SocketAddress> clientes = new HashSet<>();

        try (DatagramSocket socket = new DatagramSocket(puerto)) {
            System.out.println("Servidor UDP iniciado en el puerto " + puerto);
            byte[] buf = new byte[BUF_SIZE];

            while (true) {
                // Declarar "sobre" de recepcion
                DatagramPacket p = new DatagramPacket(buf, buf.length);

                // Recibir paquete
                socket.receive(p);

                // Obtener paquete del cliente y anadir el cliente al Set
                SocketAddress socketAddressCliente = p.getSocketAddress();
                clientes.add(socketAddressCliente);

                // Convertir a texto y validar
                String mensaje = new String(p.getData(), 0, p.getLength(), StandardCharsets.UTF_8).trim();
                if (mensaje.isBlank()) continue;

                // Crear mensaje de broadcast
                InetSocketAddress socketAddress = (InetSocketAddress) socketAddressCliente;
                String sender = "[" + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort() + "]";
                String mensajeBroadcast = "PUB " + sender + " " + mensaje;

                System.out.println(mensajeBroadcast);

                // Reenviar a todos
                byte[] datosAEnviar = mensajeBroadcast.getBytes(StandardCharsets.UTF_8);
                for (SocketAddress cliente : clientes) {
                    DatagramPacket paqueteEnviado = new DatagramPacket(
                            datosAEnviar, datosAEnviar.length, cliente
                    );
                    socket.send(paqueteEnviado);
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}
