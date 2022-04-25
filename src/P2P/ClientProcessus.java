package P2P;
/*
 * author Cornelus Madjri et Josaphat Mayuba
 */
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class ClientProcessus extends Thread {

	Vector<String> listereception = new Vector<String>();
	Vector<JProgressBar> listeProsgresbar = new Vector<JProgressBar>();

	public ClientProcessus(int port) throws BindException {
		this.port = port;
		socket = null;
		try {
			socket = new ServerSocket(this.port);
			System.out.println(socket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, ("l'application est deja ouverte"));
		}

		start();
	}

	public void run() {
		do
			try {
				java.net.Socket soc = socket.accept();
				new Reception(soc);
			} catch (IOException e) {

			}
		while (true);
	}

	private ServerSocket socket;
	private int port;// port d'écoute
}
