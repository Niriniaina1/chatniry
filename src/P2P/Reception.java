package P2P;
/*
 * author Cornelus Madjri et Josaphat Mayuba
 */
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileSystemView;

public class Reception extends Thread {
	private Socket soc;
	private OutputStream write;
	private BufferedInputStream read;
	private String copy;
	private String fileName;
	long sizef = 0;

	public Reception(Socket soc) {
		this.soc = soc;
		try {
			read = new BufferedInputStream(this.soc.getInputStream());

			write = soc.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block

			JOptionPane.showMessageDialog(null, ("Reception erreur"));
		}
		start();

	}
//le processus de reception
	public void run() {
		try {
			byte b[] = new byte[64000];
			// exception
			read.read(b);
			// selection du dossier d'enregistrement
			copy = (new String(b, 0, b.length)).trim();// on renvoie une copie
														// au propre

			StringTokenizer token = new StringTokenizer(copy, "|");

			JFrame frame = new JFrame("En train de reception");
			JProgressBar pb = new JProgressBar();
			JLabel fname = new JLabel();
			pb.setValue(0);
			pb.setStringPainted(true);
			pb.setBounds(72, 79, 26, 24);

			frame.setBounds(10, 10, 10, 10);
			// add progress bar
			frame.setLayout(new FlowLayout());
			frame.getContentPane().add(pb);
			frame.getContentPane().add(fname);
			// add progress bar
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(300, 200);
			frame.setVisible(true);

			FileSystemView fsv = FileSystemView.getFileSystemView();
			JFileChooser fileChooserSaveImage = new JFileChooser(fsv.getRoots()[0]);
			fileChooserSaveImage.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int retour = fileChooserSaveImage.showSaveDialog(null);
			if (retour == JFileChooser.APPROVE_OPTION) {
				File url = fileChooserSaveImage.getSelectedFile();
				if (url == null)
					try {
						soc.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				String f = token.nextToken();

				fileName = url.getAbsolutePath() + File.separator+f;
				 
				fname.setText(f);
				sizef = Integer.parseInt(token.nextToken());

			}

			try {
				FileOutputStream out = null;

				out = new FileOutputStream(fileName);

				byte data[] = new byte[64000];
				write.write(1); //pour signaler la reception
				long sizef_r = 0;
				int sizeString = 0;
//on fait l'envoi du fichier
				while (((sizeString = read.read(data)) != -1)) {
					sizef_r += sizeString;
					out.write(data, 0, sizeString);
					pb.setValue((int) ((100 * sizef_r) / ((sizef != 0) ? sizef : 1)));
				}

				out.close();

			} catch (SocketException e) {
				JOptionPane.showMessageDialog(null, ("Communication interronpu"));
				new File(fileName).delete();
				try {
					this.soc.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			frame.dispose();
			JOptionPane.showMessageDialog(null, (new StringBuilder("reception termin\351e "))
					.append((new File(fileName)).getAbsolutePath()).toString());

			int y = JOptionPane.showConfirmDialog(null, (new StringBuilder("voulez vous ouvrir le fichier:"))
					.append((new File(fileName)).getName()).append("?").toString());
			if (y == 0 && Desktop.isDesktopSupported()
					&& Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN))
				try {
					Desktop.getDesktop().open(new File(fileName));// pour
																	// l'ouvrir
				} catch (IOException ioexception) {
				}
			yield();
		} catch (Exception e) {
			try {
				this.soc.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
