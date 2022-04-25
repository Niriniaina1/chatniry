package serveur;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.DefaultListModel;

/**
 *
 * @author Franc's
 */
public class Serveur extends javax.swing.JFrame {

    // 
    
    // conserver tous les noms d'utilisateurs utilisés et leurs connexions socket
    private static Map<String, Socket> allUsersList = new ConcurrentHashMap<>();
    // tous les utilisateurs actifs
    private static Set<String> activeUserSet = new HashSet<>();
    private static int port = 2022;
    private ServerSocket serverSocket;
    // conserve la liste des utilisateurs actifs pour l'affichage sur l'interface utilisateur
    private DefaultListModel<String> activeDlm = new DefaultListModel<String>();

    // conserve la liste de tous les utilisateurs pour l'affichage sur l'interface utilisateur
    private DefaultListModel<String> allDlm = new DefaultListModel<String>();

    /**
     * Creates new form Serveur
     */
    public Serveur() {
        initComponents();

        try {
            // creation socket pour le serveur
            serverSocket = new ServerSocket(port);
            affichage.append("Le serveur demmare sur le port: " + port + "\n");
            affichage.append("J'attend les clients...\n");
            new ClientAccept().start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientAccept extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    // creation de socket pour le client
                    Socket clientSocket = serverSocket.accept();
                    // cela recevra le nom d'utilisateur envoyé depuis la vue du registre du client
                    String uName = new DataInputStream(clientSocket.getInputStream()).readUTF();
                    // créer un flux de sortie pour le client
                    DataOutputStream cOutStream = new DataOutputStream(clientSocket.getOutputStream());
                    // si le nom d'utilisateur est utilisé, nous devons inviter l'utilisateur à entrer un nouveau nom
                    if (activeUserSet != null && activeUserSet.contains(uName)) {
                        cOutStream.writeUTF("userExist");
                    } else {
                        // Ajouter le nouveau client à la liste de tout les utilisateur et à la liste de utilisateur en ligne
                        allUsersList.put(uName, clientSocket);
                        activeUserSet.add(uName);
                        cOutStream.writeUTF("");
                        // ajouter cet utilisateur à l'utilisateur actif JList
                        activeDlm.addElement(uName);
                        /**
                         * si le nom d'utilisateur a été pris précédemment, ne
                         * l'ajoutez pas à allUser JList, sinon ajoutez-le
                         */
                        if (!allDlm.contains(uName)) {
                            allDlm.addElement(uName);
                        }
                        // afficher la liste active et allUser à l'application swing dans JList
                        UtilActif.setModel(activeDlm);
                        util.setModel(allDlm);
                        // afficher un message sur le serveur indiquant que le nouveau client a été connecté.
                        affichage.append(uName + " Connecte...\n");
                        // créer thread pour lire les messages
                        new ReadMessage(clientSocket, uName).start();
                        //créer thread pour mettre à jour tous les clients actifs
                        new PrepareCLientList().start();
                    }
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * cette classe lit les messages provenant du client et prend les mesures
     * appropriées
     */
    class ReadMessage extends Thread {

        Socket s;
        String Id;

        // socket et nom d'utilisateur seront fournis par le client
        public ReadMessage(Socket s, String uname) {
            this.s = s;
            this.Id = uname;
        }

        @Override
        public void run() {
            // si allUserList n'est pas vide, continuez
            while (util != null && !allUsersList.isEmpty()) {

                try {
                    // lire le message du client
                    String message = new DataInputStream(s.getInputStream()).readUTF();
                    //System.out.println("message read ==> " + message); 
                    // J'ai utilisé mon propre identifiant pour identifier l'action à entreprendre sur le message reçu du client
                    String[] msgList = message.split(":");
                    // si l'action est multicast, envoyer des messages aux utilisateurs actifs sélectionnés
                    if (msgList[0].equalsIgnoreCase("multicast")) {
                        // cette variable contient la liste des clients qui recevront le message
                        String[] sendToList = msgList[1].split(",");
                        // pour chaque utilisateur envoyer un message
                        for (String usr : sendToList) {
                            try {
                                // vérifiez à nouveau si l'utilisateur est actif puis envoyez le message
                                if (activeUserSet.contains(usr)) {
                                    new DataOutputStream(((Socket) allUsersList.get(usr)).getOutputStream())
                                            .writeUTF("< " + Id + " >" + msgList[2]);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } // s'il est diffusé, envoyez un message à tous les clients actifs
                    else if (msgList[0].equalsIgnoreCase("broadcast")) {

                        //itérer sur tous les utilisateurs
                        Iterator<String> itr1 = allUsersList.keySet().iterator(); 
                        while (itr1.hasNext()) {
                            // c'est le nom d'utilisateur
                            String usrName = (String) itr1.next(); 
                            // nous n'avons pas besoin de nous envoyer de message, nous vérifions donc notre identifiant
                            if (!usrName.equalsIgnoreCase(Id)) { 
                                try {
                                    // si le client est actif, envoyez un message via le flux de sortie
                                    if (activeUserSet.contains(usrName)) { 
                                        new DataOutputStream(((Socket) allUsersList.get(usrName)).getOutputStream())
                                                .writeUTF("< " + Id + " >" + msgList[1]);
                                    } else {
                                        //si l'utilisateur n'est pas actif, informez l'expéditeur du client déconnecté
                                        new DataOutputStream(s.getOutputStream())
                                                .writeUTF("Le message n'a pas pu être remis à l'utilisateur " + usrName + " car il est déconnecté.\n");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } // si le processus d'un client est tué, notifier les autres clients
                    else if (msgList[0].equalsIgnoreCase("exit")) { 
                        // supprimer ce client de l'ensemble d'utilisateurs actif
                        activeUserSet.remove(Id); 
                        // afficher le message sur le babillard du serveur
                        affichage.append(Id + " disconnected....\n"); 

                        // mettre à jour la liste des utilisateurs actifs et tous sur l'interface utilisateur
                        new PrepareCLientList().start(); 

                        // itérer sur d'autres utilisateurs actifs
                        Iterator<String> itr = activeUserSet.iterator(); 
                        while (itr.hasNext()) {
                            String usrName2 = (String) itr.next();
                            // nous n'avons pas besoin de nous envoyer ce message
                            if (!usrName2.equalsIgnoreCase(Id)) { 
                                try {
                                    // notifier à tous les autres utilisateurs actifs la déconnexion d'un utilisateur
                                    new DataOutputStream(((Socket) allUsersList.get(usrName2)).getOutputStream())
                                            .writeUTF(Id + " disconnected..."); 
                                } catch (Exception e) { 
                                    e.printStackTrace();
                                }
                                // mettre à jour la liste des utilisateurs actifs pour chaque client après la déconnexion d'un utilisateur
                                new PrepareCLientList().start(); 
                            }
                        }
                        // supprimer le client de Jlist pour le serveur
                        activeDlm.removeElement(Id); 
                        // mettre à jour la liste des utilisateurs actifs
                        UtilActif.setModel(activeDlm); 
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // préparer la liste des utilisateurs actifs à afficher sur l'IU

    class PrepareCLientList extends Thread { 

        @Override
        public void run() {
            try {
                String ids = "";
                // itérer sur tous les utilisateurs actifs
                Iterator itr = activeUserSet.iterator(); 
                // préparer la chaîne de tous les utilisateurs
                while (itr.hasNext()) { 
                    String key = (String) itr.next();
                    ids += key + ",";
                }
                // juste couper la liste pour plus de sécurité.
                if (ids.length() != 0) { 
                    ids = ids.substring(0, ids.length() - 1);
                }
                itr = activeUserSet.iterator();
                // itérer sur tous les utilisateurs actifs
                while (itr.hasNext()) { 
                    String key = (String) itr.next();
                    try {
                        // définir le flux de sortie et envoyer la liste des utilisateurs actifs avec le préfixe d'identifiant :;.,/=
                        new DataOutputStream(((Socket) allUsersList.get(key)).getOutputStream())
                                .writeUTF(":;.,/=" + ids); 
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        affichage = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        UtilActif = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        util = new javax.swing.JList<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        affichage.setColumns(20);
        affichage.setRows(5);
        jScrollPane3.setViewportView(affichage);

        jScrollPane1.setViewportView(UtilActif);

        jScrollPane2.setViewportView(util);

        jPanel2.setBackground(new java.awt.Color(204, 204, 255));
        jPanel2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jPanel2MouseDragged(evt);
            }
        });
        jPanel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jPanel2MousePressed(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/cli/icon/minimize_window_16px.png"))); // NOI18N
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/io/cli/icon/delete_16px.png"))); // NOI18N
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)))
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 204, 255));
        jLabel1.setText("SERVEUR D'APPLICATION");

        jLabel4.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(204, 204, 255));
        jLabel4.setText("actifs");

        jLabel5.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(204, 204, 255));
        jLabel5.setText("utilisateurs");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(87, 87, 87)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 454, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(1, 1, 1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(34, 34, 34))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private int pX, pY;
    private void jPanel2MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2MouseDragged
        // TODO add your handling code here:
        this.setLocation(this.getLocation().x + evt.getX() - pX, this.getLocation().y + evt.getY() - pY);

    }//GEN-LAST:event_jPanel2MouseDragged

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        // TODO add your handling code here:

        this.setState(Serveur.ICONIFIED);

    }//GEN-LAST:event_jLabel2MouseClicked

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jLabel3MouseClicked

    private void jPanel2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2MousePressed
        // TODO add your handling code here:
        pX = evt.getX();
        pY = evt.getY();
    }//GEN-LAST:event_jPanel2MousePressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Serveur.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Serveur.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Serveur.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Serveur.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Serveur().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> UtilActif;
    private javax.swing.JTextArea affichage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<String> util;
    // End of variables declaration//GEN-END:variables
}
