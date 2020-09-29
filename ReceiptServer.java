import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

public class ReceiptServer {

    private int portNumber = 1983;
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ReceiptServerEngine engine;

    public ReceiptServer() {
        init();

        try {
            startServer();
        } catch (IOException e) {
            System.err.println("Unrecoverable error whilst starting the server: " + e.getMessage());
        }
    }

    private void init() {
        engine = new ReceiptServerEngine();
    }

    private void startServer() throws IOException {
        serverSocket = new ServerSocket(portNumber);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        //we only make it this far if the code above doesn't throw an exception
        welcomeClient();

        //user interaction starts (and ends) here
        mainLoop();

        //shut down
        closeAll();
    }

    private void welcomeClient() {
        //welcome the client and show the available commands
        String commands = engine.getAvailableCommands();
        String ip = clientSocket.getInetAddress().getHostAddress();
        String welcomeMessage = String.format("Welcome IP address '%s' to the Invoice Server. Available commands:%n%s", ip, commands);
        sendOutputToClient(welcomeMessage);
    }

    //our shutdown method. close all open streams
    private void closeAll() {
        //out of the loop. all done, close the connection
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error while shutting down: " + e.getMessage());
        }
    }

    private void mainLoop() {
        //receive and respond to input
        boolean moreInput = true;
        while (moreInput) {
            try {
                String inputLine = null;
                while ((inputLine = in.readLine()) != null) {
                    String output = engine.parseCommand(inputLine.trim());
                    sendOutputToClient(output);
                }
                moreInput = false;
            } catch (IOException e) {
                System.err.println("Error reading from client: " + e.getMessage());
                moreInput = false;
            } catch (Exception e) {
                //engine has thrown some sort of hissy fit;
                sendOutputToClient("" + e);
            }
        }
    }

    private void sendOutputToClient(String s) {
        out.println(s);
        out.println(ServerUtils.getEOM()); //The client won't know it has reached the end of our message without this
    }

    public static void main(String[] args) throws IOException {
        Locale.setDefault(new Locale("English"));
        new ReceiptServer();
    }
}
