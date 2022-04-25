package P2P;
/*
 * author Franc's
 */
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import java.awt.*;
import java.io.*;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

public class fenetre implements ActionListener, WindowListener {

	private JFrame frame;
	private JFrame annonce;
	private JFrame barProgress;
	private JTextField FileName;
	private JButton add;
	private JButton btnEnvoyer;
	private JTextField IPclient;
	private JTextField Field_port;
	private JButton btnConnecter;
	private JLabel portlabel;
	private JLabel state;
	public JList<String> list;
	private JButton btnOk;
	private String select;
	private JLabel myip;
	private String host = " ";
	private JLabel lblIp;
	private File fichier = null;
	private JPanel panelBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					fenetre window = new fenetre();
					 
					window.frame.setVisible(true);
					window.annonce.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public fenetre() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Send File");
		frame.setBounds(100, 100, 673, 280);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		annonce = new JFrame();
		annonce.setTitle("Choix Ip");
		annonce.setBounds(798, 100, 300, 400);
		annonce.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		annonce.getContentPane().setLayout(null);
		 
		
		barProgress = new JFrame();
		barProgress.setTitle("Transfert");
		barProgress.setBounds(300, 100, 481, 216);
		barProgress.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		barProgress.getContentPane().setLayout(null);

		panelBar = new JPanel();
		panelBar.setBounds(10, 11, 445, 100);
		barProgress.getContentPane().add(panelBar);
		panelBar.setLayout(null);

		JButton btnAnnuler = new JButton("Annuler");
		btnAnnuler.setBounds(173, 121, 89, 23);
		panelBar.add(btnAnnuler);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBounds(0, 0, 231, 213);
		frame.getContentPane().add(panel);
		panel.setLayout(null);

		Field_port = new JTextField();
		Field_port.setBounds(35, 107, 126, 24);
		panel.add(Field_port);
		Field_port.setColumns(10);
		Field_port.disable();
		
		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(72, 79, 26, 24);
		panel.add(lblPort);

		myip = new JLabel("ip conteneur\r\n");
		myip.setFont(new Font("Tahoma", Font.BOLD, 14));
		myip.setBackground(Color.WHITE);
		myip.setBounds(10, 24, 211, 24);
		panel.add(myip);
		myip.setText("Mon ip : ");
		btnConnecter = new JButton("Connecter");
		btnConnecter.setBounds(35, 143, 126, 24);
		btnConnecter.disable();
		panel.add(btnConnecter);
		
		
		portlabel = new JLabel("Mon port :"+64000);
		portlabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		portlabel.setBackground(Color.WHITE);
		portlabel.setBounds(10, 54, 151, 24);
		panel.add(portlabel);

		state = new JLabel();
		state.setForeground(Color.RED);
		state.setFont(new Font("Tahoma", Font.BOLD, 14));
		state.setBackground(Color.WHITE);
		state.setBounds(35, 178, 110, 24);
		panel.add(state);
		state.setText("Non Connecter");

		btnConnecter.addActionListener(this);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setBounds(232, 0, 419, 213);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(null);

		FileName = new JTextField();
		FileName.setBounds(46, 100, 235, 32);
		panel_1.add(FileName);
		FileName.setColumns(10);

		add = new JButton("Ajouter");
		add.setBounds(318, 105, 89, 23);
		panel_1.add(add);
		add.addActionListener(this);

		btnEnvoyer = new JButton("Envoyer");
		btnEnvoyer.setBounds(117, 150, 102, 32);
		panel_1.add(btnEnvoyer);

		IPclient = new JTextField();
		IPclient.setBounds(117, 22, 164, 32);
		panel_1.add(IPclient);
		IPclient.setColumns(10);

		lblIp = new JLabel("IP");
		lblIp.setBounds(46, 22, 50, 32);
		panel_1.add(lblIp);

		JLabel lblSelectionneUnFichier = new JLabel("Selectionne un fichier");
		lblSelectionneUnFichier.setBounds(117, 62, 136, 32);
		panel_1.add(lblSelectionneUnFichier);
		btnEnvoyer.addActionListener(this);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("");
		menuBar.add(mnFile);

		JMenu mnSetting = new JMenu("");
		menuBar.add(mnSetting);

		JMenu mnHelp = new JMenu("");
		menuBar.add(mnHelp);
 
		annonce.getContentPane().setLayout(null);
		extractionIP();

		btnOk = new JButton("OK");
		btnOk.setBounds(79, 59, 92, 36);
		btnOk.addActionListener(this);
		annonce.getContentPane().add(btnOk);

		panel = new JPanel();
		panel.setBounds(10, 20, 229, 35);
		annonce.getContentPane().add(panel);
		panel.setLayout(null);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 229, 184);
		panel.add(scrollPane);
		 
		 list = new JList<String>(listExtend);
		scrollPane.setViewportView(list);
		list.setSelectedIndex(0);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		select = (String) list.getSelectedValue();
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				select = (String) list.getSelectedValue();
			}
		});
  
		annonce.setSize(252, 178);
		 

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == add) {
			JFileChooser dialogue = new JFileChooser(new File("."));
			if (dialogue.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				fichier = dialogue.getSelectedFile();
				FileName.setText(fichier.getName());
			}
		}
		if (e.getSource() == btnEnvoyer) {
			String host = IPclient.getText().trim();
			try{
			new ServeurProcessus(fichier.toString(), host);
			}catch (NullPointerException e1){
				JOptionPane.showMessageDialog(null, ("veuillez ecrire IP et choisir un fichier"));
			}
		}
		if (e.getSource() == btnOk) {
			System.out.println(select);
			StringTokenizer message = new StringTokenizer(select, ":");
			message.nextToken();
			host = message.nextToken();
			myip.setText("My ip : " + host);
			annonce.dispose();
			state.setForeground(Color.BLUE);
			state.setText("Connection");
				 
					try {
						new ClientProcessus(64000);//ouvertir de la reception
					} catch (BindException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		}

	}
//extration des adresse ip de lordinateur
	public static String[] extractionIP()  {
		String name = null;
		String concat = "";
		boolean interrupteur = false;
		 
		try {
			StringTokenizer	message = new StringTokenizer(getLocalAddress().toString(), ",");
		 
		ArrayList<String> newliste = new ArrayList<String>();
		while (message.hasMoreTokens()) {
			String c = message.nextToken();

			if (c.contains("%")) {
				StringTokenizer caract = new StringTokenizer(c, "%");
				caract.nextToken();
				name = caract.nextToken();
				if (interrupteur == true) {
					newliste.add(name + " :" + concat);
					interrupteur = false;
				}
			} else {
				if (!c.contains(":") && !c.contains("127.0.0.1")) {
					interrupteur = true;
					concat = c;
				}
			}

		}
		
		listExtend = new String[newliste.size()];

		System.out.println("----------------------liste des mes ip-------------------");
		for (int i = 0; i < newliste.size(); i++) {
			System.out.println(newliste.get(i));
			listExtend[i] = newliste.get(i);
		}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listExtend;
	}

	public static ArrayList<String> liste = new ArrayList<String>();
	private static String[] listExtend;
//recuperer les adresses ip local
	public static ArrayList<String> getLocalAddress() throws SocketException {

		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration<InetAddress> ee = n.getInetAddresses();

			while (ee.hasMoreElements()) {
				InetAddress i = ee.nextElement();
				liste.add(i.getHostAddress());

			}
		}

		return liste;
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	



	
}
