import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HiloCliente implements Runnable {

    private Socket socket;
    private Servidor servidor;
    private String nombreCliente;
    private DataInputStream in; 
    //private DataOutputStream out; 



    public HiloCliente(Servidor servidor, Socket socket) {
        this.servidor = servidor;
        this.socket = socket;
        try {
            // Crear flujos de entrada y salida
            this.in = new DataInputStream(socket.getInputStream());
            //this.out = new DataOutputStream(socket.getOutputStream());

            // Establecer el nombre del cliente
            this.nombreCliente = in.readUTF();
            System.out.println("SERVIDOR > Nuevo cliente: " + this.nombreCliente);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //getters y setters
    public String getNombreCliente() {
        return nombreCliente; //Permite a otras clases obtener el nombre del cliente desde una instancia de HiloCliente sin acceder directamente a la variable miembro.
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente; //TODO permitir el cambio de nombre
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    // Método básico para el envio general de mensajes
     public void enviarMensaje(String mensaje) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 



    // Método para desconectar el cliente y eliminarlo de la lista
    public void desconectarCliente() {
        try {
            in.close();
            socket.close();
            servidor.eliminarCliente(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            //DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (!socket.isClosed()) {
                try { 
                    if (in.available() > 0) {
                        String entrada = in.readUTF();
                        servidor.broadcastMessage(this, entrada); // test
 /*                        if (entrada.startsWith("/clientes")) {
                            // Llama al método en el Servidor para enviar la lista de clientes al cliente solicitante
                            servidor.enviarListaClientes(this); 
                        } else {
                            servidor.broadcastMessage(this, entrada);
                        } 
                        BLOQUE REDUNDANTE: CENTRALIZADO EN SERVIDOR*/
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
