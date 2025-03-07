import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor implements Runnable {
    public static final int PUERTO = 5050; //PUERTO DISPONIBLE 
    private ServerSocket serverSocket;
    private List<HiloCliente> clientes;

    // Iniciando servidor en el método main
    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciarServidor();
    }

    // 
    public Servidor() {
        clientes = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(PUERTO);
            System.out.println("SERVIDOR ENCENDIDO");
            System.out.println("SERVIDOR > Esperando conexiones...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<HiloCliente> getClientes() {
        return clientes;
    }

    private void iniciarServidor() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("SERVIDOR > Nueva conexión: " + socket.getRemoteSocketAddress());



                // Crear hilo cliente y establecer nombre
                HiloCliente cliente = new HiloCliente(this, socket);
                clientes.add(cliente);
                Thread hilo = new Thread(cliente);
                hilo.start();


                // Enviar mensaje de bienvenida a todos los clientes
                cliente.enviarMensaje("SERVIDOR > ¡Bienvenido al JavaChat, " + cliente.getNombreCliente() + "!");
                broadcastMessage(cliente, "SERVIDOR > " + cliente.getNombreCliente() + " ha ingresado JavaChat. \nSERVIDOR > Escribe @"+cliente.getNombreCliente()+" para enviarle un PM");

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("SERVIDOR > Error al aceptar la conexión");
            }
        }
    }

    // Método para eliminar un cliente desconectado de la lista
    public synchronized void eliminarCliente(HiloCliente cliente) {
        clientes.remove(cliente);
        broadcastMessage(null, "SERVIDOR > " + cliente.getNombreCliente() + " se ha desconectado.");
    }

    

    @Override
    public void run() {
        // Metodo abstracto run
    }

    // Método con el que el servidor se comunica con los clientes
    public synchronized void broadcastMessage(HiloCliente remitente, String mensaje) {
        if (mensaje.contains("/clientes")) {
            // Lista de clientes conectados
            enviarListaClientes(remitente);  
        } else if (mensaje.startsWith("@")) {
            // Mensaje privado PM
           manejarMensajePrivado(remitente, mensaje);
        } else if (mensaje.equals("/salir")) {
            // Salir o CerrarGUI
            remitente.desconectarCliente();
        } else {
            // Mensaje público
            for (HiloCliente cliente : clientes) {
                if (cliente != remitente) {
                    cliente.enviarMensaje(mensaje);
                }
            }
        }
    }
    

    // Método para enviar la lista de clientes a un cliente específico
    public void enviarListaClientes(HiloCliente clienteSolicitante) {
        StringBuilder listaClientes = new StringBuilder("SERVIDOR > Clientes Conectados: ");
        for (HiloCliente cliente : clientes) {
            listaClientes.append(cliente.getNombreCliente()).append(", ");
        }
        // Eliminar la coma adicional al final
        listaClientes.setLength(listaClientes.length() - 2);

        // Enviar la lista actualizada solo al cliente solicitante
        clienteSolicitante.enviarMensaje(listaClientes.toString());
    }

    // Formato de mensajes privados
    private void manejarMensajePrivado(HiloCliente remitente, String mensaje) {
        // Extraer el nombre de usuario del mensaje privado
        String[] partes = mensaje.split(" ");
        if (partes.length >= 2) {
            String nombreDestinatario = partes[0].substring(1); // Eliminar el "@" del nombre
            String mensajePrivado = mensaje.substring(partes[0].length() + 1);

            // Buscar al destinatario en la lista de clientes (IgnoreCase)
            for (HiloCliente cliente : clientes) {
                if (cliente.getNombreCliente().equalsIgnoreCase(nombreDestinatario)) {
                    // Enviar el mensaje privado al destinatario
                    cliente.enviarMensaje("(PM de " + remitente.getNombreCliente() + "): " + mensajePrivado);
                    // También enviar una copia al remitente
                    remitente.enviarMensaje("(Para " + nombreDestinatario + "): " + mensajePrivado);
                    return; // Salir del bucle una vez que se haya enviado el mensaje privado
                }
            }

            // Notificar al remitente si el destinatario no fue encontrado
            remitente.enviarMensaje("SERVIDOR > Usuario '" + nombreDestinatario + "' no encontrado. ");
        }
    }


}