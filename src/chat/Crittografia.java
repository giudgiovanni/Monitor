package chat;



import java.security.Key;

import java.security.KeyException;

import java.security.NoSuchAlgorithmException;

import xjava.security.Cipher;



/**
 * Classe di utilità che semplifica l'uso della crittografia
 * all'interno dei programmi Java.
 * <p>
 * E' sufficiente creare un'istanza di questa classe specificando
 * l'algoritmo crittografico desiderato, dopodiché <code>cifra(...)</code>
 * e <code>decifra(...)</code> possono essere invocati in qualsiasi
 * successione ed un qualunque numero di volte per applicare l'operazione
 * crittografica voluta ai dati specificati di volta in volta.
 * <p>
 * Questa classe è idonea anche agli ambiti <i>multi-threading</i>.
 * <p>
 *
 * @author    <em>Marco Cimatti</em>
 * @version   1.0
 */
public class Crittografia {
    
    /** L'algoritmo da usare nelle operazioni di cifratura e decifratura. */
    private String algoritmo;
    
    
    /**
     * Unico costruttore della classe.
     *
     * @param       algoritmo   la stringa che identifica il tipo di
     *                          <code>Cipher</code> da adottare.
     *                          Si possono specificare anche la modalità di
     *                          lavoro a blocchi (ECB, CBC, CFB, OFB...) ed il
     *                          tipo di <i>padding</i> (PKCS#5, PKCS#7...),
     *                          impiegando il carattere <code>'/'</code>
     *                          per separare i vari campi entro la stringa.
     * @exception   NoSuchAlgorithmException   se l'algoritmo specificato non è supportato.
     * @see         #algoritmo
     */
    public Crittografia(String algoritmo) throws NoSuchAlgorithmException {
        Cipher.getInstance(this.algoritmo = algoritmo);
    }
    
    /**
     * Produce la versione cifrata del testo <code>dati</code>.
     * Semplice <i>wrapper</i> che equivale a:
     * <p>
     * <code>cifra(dati, 0, dati.length, chiave);</code>
     *
     * @param       dati           il <i>plaintext</i> da cifrare.
     * @param       chiave         la chiave di cifratura da impiegare.
     * @exception   KeyException   se generata dal <code>Cipher</code> interno.
     * @return      il <i>ciphertext</i> ottenuto da <code>dati</code>.
     * @see         #cifra(byte[], int, int, java.security.Key)
     */
    public byte[] cifra(byte[] dati, Key chiave) throws KeyException {
        return cifra(dati, 0, dati.length, chiave);
    }
    
    /**
     * Operazione di cifratura. Si può usare <code>decifra(...)</code>
     * come azione complementare per risalire al messaggio in chiaro.
     *
     * @param       dati               il <i>plaintext</i> da cifrare.
     * @param       offset             l'offset del primo byte utile nel vettore <code>dati</code>.
     * @param       lun                la lunghezza del testo da cifrare, considerato
     *                                 da <code>dati[offset]</code> (compreso) in poi.
     * @param       chiave             la chiave di cifratura da impiegare.
     * @exception   KeyException       se generata dal <code>Cipher</code> interno.
     * @exception   RuntimeException   se internamente viene generata una
     *                                 <code>NoSuchAlgorithmException</code>.
     * @return      il <i>ciphertext</i> ottenuto da <code>dati</code>.
     * @see         #decifra(byte[], java.security.Key)
     * @see         #decifra(byte[], int, int, java.security.Key)
     */
    public byte[] cifra(byte[] dati, int offset, int lun, Key chiave) throws KeyException  {
        try {
            Cipher c = Cipher.getInstance(algoritmo);
            c.initEncrypt(chiave);
            return c.doFinal(dati, offset, lun);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Ora l'algoritmo non è più supportato?");
        }
    }
    
    /**
     * Produce la versione decifrata del testo <code>dati</code>.
     * Semplice <i>wrapper</i> che equivale a:
     * <p>
     * <code>decifra(dati, 0, dati.length, chiave);</code>
     *
     * @param       dati           il <i>ciphertext</i> da decifrare.
     * @param       chiave         la chiave di decifratura da impiegare.
     * @exception   KeyException   se generata dal <code>Cipher</code> interno.
     * @return      il <i>plaintext</i> ottenuto da <code>dati</code>.
     * @see         #decifra(byte[], int, int, java.security.Key)
     */
    public byte[] decifra(byte[] dati, Key chiave) throws KeyException {
        return decifra(dati, 0, dati.length, chiave);
    }
    
    /**
     * Operazione di decifratura. Si può usare <code>cifra(...)</code>
     * come azione eseguita in precedenza per creare il messaggio cifrato.
     *
     * @param       dati               il <i>ciphertext</i> da decifrare.
     * @param       offset             l'offset del primo byte utile nel vettore <code>dati</code>.
     * @param       lun                la lunghezza del testo da decifrare, considerato
     *                                 da <code>dati[offset]</code> (compreso) in poi.
     * @param       chiave             la chiave di decifratura da impiegare.
     * @exception   KeyException       se generata dal <code>Cipher</code> interno.
     * @exception   RuntimeException   se internamente viene generata una
     *                                 <code>NoSuchAlgorithmException</code>.
     * @return      il <i>plaintext</i> ottenuto da <code>dati</code>.
     * @see         #cifra(byte[], java.security.Key)
     * @see         #cifra(byte[], int, int, java.security.Key)
     */
    public byte[] decifra(byte[] dati, int offset, int lun, Key chiave) throws KeyException {
        try {
            Cipher c = Cipher.getInstance(algoritmo);
            c.initDecrypt(chiave);
            return c.doFinal(dati, offset, lun);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Ora l'algoritmo non è più supportato?");
        }
    }
}