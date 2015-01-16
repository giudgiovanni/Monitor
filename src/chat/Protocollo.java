package chat;



/**
 * Classe che racchiude le costanti definite dal protocollo di
 * dialogo utilizzato dalla <i>chat</i>. Contiene solo 4 costanti.
 *
 * @author    <em>Alessandro Gaspari</em>
 * @version   1.0
 */
public class Protocollo {
    
    /**
     * Identifica i messaggi che richiedono un dialogo fra due utenti;
     * equivale al <i>flag</i> SYN dei segmenti TCP.
     */
    public static final byte SYN  = (byte) 0x80;
    
    /**
     * Marca i datagrammi di conferma per instaurare una conversazione;
     * semanticamente rappresenta l'ACK di un segmento SYN adottato da TCP.
     */
    public static final byte OK   = (byte) 0x40;
    
    /** Tipo di datagramma che avvisa di un nuovo ingresso nella discussione. */
    public static final byte ADD  = (byte) 0x20;
    
    /** Caratterizza i messaggi testuali facenti parte del colloquio. */
    public static final byte TEXT = (byte) 0x10;
    
    /**
     * Tipo di messaggio per terminare una <i>chat</i>; ha lo stesso
     * significato dei segmenti TCP aventi il <i>flag</i> FIN attivo.
     */
    public static final byte FIN  = (byte) 0x08;
}
