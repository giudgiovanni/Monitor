package chat;



import java.io.Serializable;



/**
 * Implementazione di un generico messaggio scambiato fra due utenti:
 * prevede un mittente, un destinatario ed un <i>buffer</i> di byte
 * dimensionabile a piacere per contenere le informazioni da condividere
 * fra i due.
 * Da questa classe è possibile ottenere messaggi più specifici mediante
 * ereditarietà.
 * <p>
 *
 * @author    <em>Alessandro Gaspari</em>
 * @version   1.0
 */
public class MessaggioBase implements Serializable {
    
    /** L'identificativo del mittente del messaggio. */
    protected String da_chi;
    
    /** La stringa che rappresenta il destinatario. */
    protected String a_chi;
    
    /** Il <i>buffer</i> contenente le informazioni da scambiare. */
    protected byte[] che_cosa;
    
    
    /**
     * Costruttore. Inizializza i tre campi <code>protected</code>
     * dell'istanza mediante assegnazione, senza duplicare il
     * vettore <code>cosa</code> ricevuto come parametro.
     *
     * @param   da     il mittente del messaggio.
     * @param   a      il destinatario del suddetto.
     * @param   cosa   le informazioni da includere nella busta;
     *                 si assegna <code>che_cosa = cosa</code>.
     * @see     #che_cosa
     */
    public MessaggioBase(String da, String a, byte[] cosa) {
        da_chi   = da;
        a_chi    = a;
        che_cosa = cosa;
    }
    
    /**
     * <b>Selettore primitiva</b> per ottenere il nome del mittente
     * del messaggio rappresentato dall'istanza.
     *
     * @return   il valore del campo <code>protected da_chi</code>.
     * @see      #da_chi
     */
    public String daChi() {
        return da_chi;
    }
    
    /**
     * <b>Selettore primitiva</b> per leggere il destinatario di
     * questo messaggio.
     *
     * @return   il valore del campo <code>protected a_chi</code>.
     * @see      #a_chi
     */
    public String aChi() {
        return a_chi;
    }
    
    /**
     * <b>Selettore primitiva</b> per accedere al contenuto della busta.
     * <u>ATTENZIONE</u>: per questioni di efficienza non viene duplicato
     * il testo del <code>MessaggioBase</code>, quindi in seguito a
     * questa invocazione è possibile sovrascrivere il <i>buffer</i>
     * interno usato per contenere le informazioni.
     *
     * @return   il campo <code>protected che_cosa</code>.
     * @see      #che_cosa
     */
    public byte[] cheCosa() {
        return che_cosa;
    }
}