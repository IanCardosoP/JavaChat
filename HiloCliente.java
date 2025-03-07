import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HiloCliente implements Runnable {

    private Socket socket;
    private Servidor servidor;
    private String nombreCliente;
    private DataInputStream in;
    private DataOutputStream out;

    public HiloCliente(Servidor servidor, Socket socket) {
        this.servidor = servidor;
        this.socket = socket;
        try {
            // Crear flujos de entrada y salida
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            // Establecer el nombre del cliente
            this.nombreCliente = in.readUTF();
            System.out.println("SERVIDOR > Nuevo cliente: " + this.nombreCliente);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters y setters
    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    // Método básico para el envío general de mensajes
    public void enviarMensaje(String mensaje) {
        try {
            out.writeUTF(mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para desconectar el cliente y eliminarlo de la lista
    public void desconectarCliente() {
        try {
            in.close();
            out.close();
            socket.close();
            servidor.eliminarCliente(this);
            // Interrumpir el hilo actual
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Establecer un tiempo de espera en el socket para evitar el uso excesivo de CPU
            socket.setSoTimeout(5000);

            while (!socket.isClosed()) {
                try {
                    if (in.available() > 0) {
                        String entrada = in.readUTF();
                        servidor.broadcastMessage(this, entrada);
                    }
                } catch (IOException e) {
                    // Manejar la desconexión del cliente
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
