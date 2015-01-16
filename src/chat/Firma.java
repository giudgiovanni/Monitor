package chat;



import java.security.InvalidKeyException;

import java.security.NoSuchAlgorithmException;

import java.security.PrivateKey;

import java.security.PublicKey;

import java.security.Signature;

import java.security.SignatureException;



/**
 * Classe di utilità che semplifica l'uso della firma digitale
 * all'interno dei programmi Java.
 * <p>
 * E' sufficiente creare un'istanza di questa classe specificando
 * l'algoritmo crittografico desiderato, dopodiché <code>firma(...)</code>
 * e <code>firmaOk(...)</code> possono essere invocati in qualsiasi
 * successione ed un qualunque numero di volte per applicare l'operazione
 * voluta ai dati specificati di volta in volta.
 * <p>
 * Questa classe è idonea anche agli ambiti <i>multi-threading</i>.
 * <p>
 *
 * @author    <em>Marco Cimatti</em>
 * @version   1.0
 */
public class Firma {
    
    /** L'algoritmo del <code>java.security.Signature</code> da impiegare. */
    private String algoritmo;
    
    /**
     * Unico costruttore della classe.
     *
     * @param       algoritmo   il tipo di <code>Signature</code> da adottare internamente.
     * @exception   NoSuchAlgorithmException   se l'algoritmo specificato non è supportato.
     * @see         #algoritmo
     */
    public Firma(String algoritmo) throws NoSuchAlgorithmException {
        Signature.getInstance(this.algoritmo = algoritmo);
    }
    
    /**
     * Semplice <i>wrapper</i> che equivale a:
     * <p>
     * <code>firma(messaggio, 0, messaggio.length, chiave);</code>
     *
     * @see   #firma(byte[], int, int, java.security.PrivateKey)
     */
    public byte[] firma(byte[] messaggio, PrivateKey chiave) {
        return firma(messaggio, 0, messaggio.length, chiave);
    }
    
    /**
     * Creazione della firma di un messaggio.
     *
     * @param       messaggio          il testo da firmare.
     * @param       offset             l'offset del primo byte utile nel vettore <code>messaggio</code>.
     * @param       lun                la lunghezza del testo da firmare, considerato
     *                                 da <code>messaggio[offset]</code> (compreso) in poi.
     * @param       chiave             la chiave privata con cui firmare.
     * @exception   RuntimeException   se internamente viene generata una
     *                                 <code>NoSuchAlgorithmException</code>.
     * @return      la firma ottenuta dal (blocco appartenente a) <code>messaggio</code>;
     *              <code>null</code> in caso di <code>InvalidKeyException</code>
     *              oppure <code>SignatureException</code> interne.
     * @see         #firmaOk(byte[], byte[], java.security.PublicKey)
     * @see         #firmaOk(byte[], int, int, byte[], java.security.PublicKey)
     */
    public byte[] firma(byte[] messaggio, int offset, int lun, PrivateKey chiave) {
        try {
            Signature sign = Signature.getInstance(algoritmo);
            sign.initSign(chiave);
            sign.update(messaggio, offset, lun);
            return sign.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Ora l'algoritmo non è più supportato?");
        }
        catch (InvalidKeyException e) { e.printStackTrace(); }
        catch (SignatureException  e) { e.printStackTrace(); }
        return null;
    }
    
    /**
     * Semplice <i>wrapper</i> che equivale a:
     * <p>
     * <code>firmaOk(messaggio, 0, messaggio.length, firma, chiave);</code>
     *
     * @see   #firmaOk(byte[], int, int, byte[], java.security.PublicKey)
     */
    public boolean firmaOk(byte[] messaggio, byte[] firma, PublicKey chiave) {
        return firmaOk(messaggio, 0, messaggio.length, firma, chiave);
    }
    
    /**
     * Test di validità della firma di un messaggio.
     *
     * @param       messaggio          il testo da cui è stata ottenuta la firma.
     * @param       offset             l'offset del primo byte utile nel vettore <code>messaggio</code>.
     * @param       lun                la lunghezza del testo firmato, considerato
     *                                 da <code>messaggio[offset]</code> (compreso) in poi.
     * @param       firma              la firma associata al (blocco del) messaggio.
     * @param       chiave             la chiave pubblica con cui verifica la firma.
     * @exception   RuntimeException   se internamente viene generata una
     *                                 <code>NoSuchAlgorithmException</code>.
     * @return      <code>true</code> se la firma è valida, <code>false</code>
     *              altrimenti oppure in caso di <code>InvalidKeyException</code>
     *              o <code>SignatureException</code> interne.
     * @see         #firmaOk(byte[], byte[], java.security.PublicKey)
     */
    public boolean firmaOk(byte[] messaggio, int offset, int lun, byte[] firma, PublicKey chiave) {
        try {
            Signature sign = Signature.getInstance(algoritmo);
            sign.initVerify(chiave);
            sign.update(messaggio, offset, lun);
            return sign.verify(firma);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Ora l'algoritmo non è più supportato?");
        }
        catch (InvalidKeyException e) { e.printStackTrace(); }
        catch (SignatureException  e) { e.printStackTrace(); }
        return false;
    }
}
