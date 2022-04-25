package P2P;
/*
 * author Cornelus Madjri et Josaphat Mayuba
 */
import java.awt.FlowLayout;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class ServeurProcessus extends Thread {
	String file = null, host = null;

	private static final int defaut = 64000; 
	
	public ServeurProcessus(String file, String host) {
		this.file = file;
		this.host = host;
		start();
	}
 
	public void run() {
		Transfert(file, host);
		yield();
		stop();

	}
//tranferer le fichier 
	public static boolean Transfert(String file, String host) {

		File fichier = null;
		fichier = new File(file);
		Socket s = null;
		OutputStream out = null;
		BufferedInputStream response = null;
		try { 
			s = new Socket(host, defaut);
			response = new BufferedInputStream(s.getInputStream());
			out = s.getOutputStream();
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(null, "Destination non connecté");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long sizef = 0;
		sizef = fichier.length();

		String Conten = null;
		 
			Conten = fichier.getName() + "|" + sizef;

		System.out.println("Fichier à envoyer " + Conten);
		byte nameFile[] = Conten.getBytes();// recuperation du nom du fichier

		if (s.isConnected())
			try {
				out.write(nameFile, 0, nameFile.length);//emvoi le nom de fichier
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		try {
			response.read();//lecture de la reponse du recepteur
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte donnee[] = new byte[defaut];
		long compteurcourant = 0;

		int size;

		InputStream in = null;
		try {
			in = fichier.toURL().openStream();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JFrame frame = new JFrame("Envoi en cours");
		JProgressBar pb = new JProgressBar();
		JLabel  fname = new JLabel();
		pb.setValue(0);
		pb.setStringPainted(true);

		// add progress bar
		frame.setLayout(new FlowLayout());
		frame.getContentPane().add(pb);
		frame.getContentPane().add(fname);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 200);
		frame.setVisible(true);
		fname.setText(fichier.getName());
		try {
			while ((size = in.read(donnee)) != -1) 
			{
				compteurcourant += size;
				if (s.isConnected())
					out.write(donnee, 0, size);
				pb.setValue((int) ((100 * compteurcourant) / sizef));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.dispose();
		try {
			out.flush();
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;//pour verifier la fin de transfert

	}
 

}
