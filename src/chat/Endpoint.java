package chat;



import java.io.Serializable;

import java.net.InetAddress;



/**
 * Classe che astrae l'<i>endpoint</i> di una connessione di rete.
 * Il concetto di <i>endpoint</i> é lo stesso adottato da TCP per
 * individuare una "connessione", definita appunto come coppia di
 * <i>endpoint</i>.
 * <p>
 * L'oggetto rappresentato da <code>Endpoint</code> è dunque una
 * coppia (<b>indirizzo IP</b>, <b>porta</b>), dove la seconda
 * può essere sia un numero di porta TCP, sia un numero di porta UDP.
 * <p>
 *
 * @author    <em>Marco Cimatti</em>
 * @version   1.0
 */
public class Endpoint implements Serializable {
    
    /** L'indirizzo IP. */
    private InetAddress IP;
    
    /** Il numero di porta TCP/UDP. */
    private int porta;
    
    /**
     * Costruttore. Inizializza le variabili d'istanza <code>IP</code>
     * e <code>porta</code>, verificando il range di appartenenza della
     * seconda: da 0 a 65535 (<code>0xFFFF</code>) estremi compresi.
     *
     * @param       IP          indirizzo IP da inglobare nell'<i>endpoint</i>.
     * @param       porta       il numero di porta TCP/UDP nell'<i>endpoint</i>.
     * @exception   IllegalArgumentException   se e solo se il numero di porta
     *                                         specificato non appartiene
     *                                         all'intervallo [0..65535].
     * @see         #IP
     * @see         #porta
     */
    public Endpoint(InetAddress IP, int porta) {
        if (porta < 0 || 0xffff < porta)
            throw new IllegalArgumentException("Porta fuori range: " + porta);
        this.IP    = IP;
        this.porta = porta;
    }
    
    /**
     * <b>Selettore</b> per ottenere l'IP incapsulato nell'istanza.
     * <b>Primitiva</b>.
     *
     * @return   la variabile d'istanza <code>IP</code>.
     * @see      #IP
     */
    public InetAddress IP() {
        return IP;
    }
    
    /**
     * <b>Selettore</b> per leggere il numero di porta incapsulato nell'istanza.
     * <b>Primitiva</b>.
     *
     * @return   la variabile d'istanza <code>porta</code>.
     * @see      #porta
     */
    public int porta() {
        return porta;
    }
    
    /**
     * Metodo per verificare l'uguaglianza fra due <code>Endpoint</code>.
     * In questa realizzazione è una <b>primitiva</b> solo per questioni
     * di efficienza, ma non è obbligatorio.
     *
     * @param    obj   l'istanza di <code>Endpoint</code> con la quale
     *                 effettuare il confronto.
     * @return   <code>true</code> se e solo se <code>obj</code> rappresenta
     *           il medesimo <i>endpoint</i> astratto da questa istanza,
     *           ovvero se sono uguali sia l'indirizzo IP, sia il numero
     *           di porta TCP/UDP. Se <code>obj == null</code> riporta <code>false</code>.
     */
    public boolean equals(Object obj) {
        return obj == null ? false
        : IP.equals(((Endpoint) obj).IP) && porta == ((Endpoint) obj).porta;
    }
    
    /**
     * <b>Convertitore</b>; in questa realizzazione è una <b>primitiva</b>
     * solo per questioni di efficienza.
     *
     * @return   una stringa rappresentante l'<i>endpoint</i> del tipo
     *           <code>"X.X.X.X:porta"</code>. Ad esempio, il <i>web server</i>
     *           locale risiede tipicamente a <code>"127.0.0.1:80"</code>.
     */
    public String toString() {
        return IP.getHostAddress() + ':' + porta;
    }
}
