
package serveur;

/**
 *
 * @author Franc's
 */
public class MonFichier {

    private int id;
    private String nom;
    private byte[] donne;
    private String extension;

    public MonFichier(int id, String nom, byte[] donne, String extension) {
        this.id = id;
        this.nom = nom;
        this.donne = donne;
        this.extension = extension;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public byte[] getDonne() {
        return donne;
    }

    public void setDonne(byte[] donne) {
        this.donne = donne;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }  
}
