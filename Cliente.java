import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    private static Cliente instancia;
    // Que solo haya una única instancia de la clase Cliente durante la ejecución del programa.

    public static Cliente getInstancia() {
        return instancia;
    }// Que solo haya una única instancia de la clase Cliente durante la ejecución del programa.

    private String nombreUsuario;
    private JTextArea areaChat;
    private JTextField campoEntrada;

    int PORT = Servidor.PUERTO;

    public Cliente(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        instancia = this;  
    }

    public void crearCliente() {
        try {
            //localHost para entorno local
            //PUERTO DEFINIDO EN LA CLASE SERVIDOR
            Socket socket = new Socket("localhost", PORT);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Envía el nombre de usuario al servidor
            out.writeUTF(nombreUsuario);

            instancia = this;

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("Cliente de Chat - " + nombreUsuario);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(400, 400);

                areaChat = new JTextArea();
                areaChat.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(areaChat);
                frame.add(scrollPane, BorderLayout.CENTER);

                campoEntrada = new JTextField();
                JButton botonEnviar = new JButton("Enviar");
                JPanel panelEntrada = new JPanel(new BorderLayout());
                panelEntrada.add(campoEntrada, BorderLayout.CENTER);
                panelEntrada.add(botonEnviar, BorderLayout.EAST);
                frame.add(panelEntrada, BorderLayout.SOUTH);

                // Accion del boton enviar
                botonEnviar.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String mensaje = campoEntrada.getText();
                        if (!mensaje.isEmpty()) {
                            //enviarMensaje(out, nombreUsuario + ": " + mensaje);
                            // Un mensaje iniciado con @ se envía tal cual, para ser manejado por
                            // el método Servidor.broadcastMessage()
                            if (mensaje.startsWith("@")) {
                                // Mensaje privado plano para Servidor.broadcastMessage()
                                enviarMensaje(out, mensaje);
                            } else {
                                // Mensaje Global formateado con remitente
                                enviarMensaje(out, nombreUsuario + ": " + mensaje);
                            }
                            campoEntrada.setText(""); //Limpia el campo de entrada al finalizar if
                        }
                    }
                });

                // Enter para enviar
                campoEntrada.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                            String mensaje = campoEntrada.getText();
                            if (!mensaje.isEmpty()) {
                            //enviarMensaje(out, nombreUsuario + ": " + mensaje);
                            // Un mensaje iniciado con @ se envía tal cual, para ser manejado por
                            // el método Servidor.broadcastMessage()
                            if (mensaje.startsWith("@")) {
                                // Mensaje privado plano para Servidor.broadcastMessage()
                                enviarMensaje(out, mensaje);
                            } else {
                                // Mensaje Global formateado con remitente
                                enviarMensaje(out, nombreUsuario + ": " + mensaje);
                            }
                            campoEntrada.setText(""); //Limpia el campo de entrada al finalizar if
                            }
                        }
                    }
                });

                // Hilo para recibir mensajes del servidor
                new Thread(() -> {
                    while (!socket.isClosed()) {
                        try {
                            if (in.available() > 0) {
                                String entrada = in.readUTF();
                                actualizarAreaChat(entrada);
                            }
                        } catch (IOException e) {
                            // Manejar la excepción
                            e.printStackTrace();
                            // Cerrar la conexión en caso de error
                            cerrarConexion(socket, in, out);
                        }
                    }
                }).start();

                // Cerrar la aplicación cuando se cierre la ventana
                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        cerrarConexion(socket, in, out); // Y desconectar cliente
                        
                    }
                });

                frame.setVisible(true);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para desconectar
    private void cerrarConexion(Socket socket, DataInputStream in, DataOutputStream out) {
        try {
            // Enviar mensaje de desconexión global al servidor, para ser 
            // gestionado por Servidor.broadcastMessage()
            enviarMensaje(out, "/salir");
            // Cerrar flujos de entrada y salida
            in.close();
            out.close();
            // Cerrar el socket
            socket.close();

            System.out.println("Conexión cerrada.");
            System.exit(0);  
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(DataOutputStream out, String mensaje) {
        try {
            out.writeUTF(mensaje);
            // Muestra el mensaje en la interfaz de usuario (areaChat) desde el hilo de eventos de Swing
            SwingUtilities.invokeLater(() -> {
                Cliente.getInstancia().actualizarAreaChat(mensaje);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actualizarAreaChat(String mensaje) {
        SwingUtilities.invokeLater(() -> areaChat.append(mensaje + "\n"));
    }

    // Main
    public static void main(String[] args) {

        System.out.println("*****************************************************\n" + //
                "*****************************************************\n" + //
                "**          5. 4 Actividad integradora.            **\n" + //
                "**       Programación concurrente y en red         **\n" + //
                "*****************************************************\n" + //
                "**          @author: IAN CARDOSO PILLADO           **\n" + //
                "**         @tutor: JOSE LUIS GARCIA CERPAS         **\n" + //
                "*****************************************************\n" + //
                "**                 Instrucciones:                  **\n" + //
                "**    + Asegure iniciar primero Serveridor.java    **\n" + //
                "**    + Ingrese su usuario cuando se le requiera   **\n" + //
                "**    + Inicie tantos clientes como guste!         **\n" + //
                "**      (Mas de tres sugeridos para probar PMs)    **\n" + //
                "**                                                 **\n" + //
                "**          Comandos dentro del JavaChat           **\n" + //
                "**   `/clientes` para ver la lista conectados      **\n" + //
                "** `@otro_cliente` para enviar PM (PrivateMessage) **\n" + //
                "**   `/salir` o cerrar ventana para desconectar    **\n" + //
                "*****************************************************");
        // Pide al usuario que ingrese su nombre
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nIngresa tu usuario: ");
        String nombreUsuario = scanner.nextLine();

        // Crea el cliente con el nombre proporcionado
        Cliente cliente = new Cliente(nombreUsuario);
        cliente.crearCliente();

        scanner.close();
    }
}
