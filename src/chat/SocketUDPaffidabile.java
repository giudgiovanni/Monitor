package chat;



import java.io.InterruptedIOException;

import java.io.IOException;

import java.net.DatagramPacket;

import java.net.DatagramSocket;

import java.net.InetAddress;

import java.net.SocketException;

import java.util.Vector;



/**
 * Astrazione di un <i>socket</i> UDP che utilizza <i>Automatic
 * Repeat reQuest</i> (ARQ) per consegnare i datagrammi all'altro
 * capo della rete. Realizzato come <i>wrapper</i> attorno ad un
 * <code>java.net.DatagramSocket</code>.
 * <p>
 * Oltre all'aspetto <i>reliable</i> dell'invio di datagrammi, viene
 * realizzata anche una gestione dei <code>java.net.DatagramPacket</code>
 * per evitare che duplicazioni o forti ritardi nel transito in rete
 * possano riflettersi anche esternamente alla classe stessa. Questo
 * viene realizzato mediante <i>sequence number</i> progressivi
 * attribuiti ai messaggi scambiati in rete.
 * <p>
 * <u>ATTENZIONE</u>: per via del protocollo di comunicazione realizzato
 * questa classe non consente di ricevere meno di quattro byte: i
 * datagrammi contenenti quattro byte applicativi vengono interpretati
 * come <i>acknowledge</i>, mentre solo quelli aventi cinque o più
 * byte sono interpretati come unità informative significative. Per
 * lunghezze strettamente inferiori a quattro il <code>DatagramPacket</code>
 * viene semplicemente scartato.
 * <p>
 *
 * @author    <em>Alessandro Gaspari</em>
 * @version   1.0
 */
public abstract class SocketUDPaffidabile {
    
    /**
     * <i>Timeout</i> in millisecondi fra l'invio di un messaggio e
     * la ricezione del relativo <i>acknowledge</i>.
     */
    public static final int TIMEOUT = 10000;   // 10 secondi
    
    
    /**
     * Numero massimo di invii per ciascun datagramma nel caso non
     * sia ricevuta alcuna conferma (ACK) da parte dal destinatario.
     */
    public static final int TENTATIVI = 3;
    
    
    /** La socket UDP tramite la quale realizzare gli scambi affidabili di datagrammi. */
    private DatagramSocket sock;
    
    /** Archivio degli ultimi <i>sequence number</i> delle connessioni <b>virtuali</b>. */
    private Vector history = new Vector(16, 4);
    
    /** <i>Buffer</i> temporaneo nel quale inserire i datagrammi non ancora letti dall'utente. */
    private Vector cache = new Vector(64, 8);
    
    
    /**
     * Costruttore che crea il <code>java.net.DatagramSocket</code>
     * interno da impiegare per l'accesso alla rete. La porta UDP da
     * usare viene scelta dal Sistema Operativo sottostante.
     *
     * @exception   java.net.SocketException   se ottenuta creando il <i>socket</i>.
     */
    public SocketUDPaffidabile() throws SocketException {
        sock = new DatagramSocket();
        sock.setSoTimeout(1);   // Socket non bloccante
        
    }
    
    /**
     * Costruttore che crea il <code>java.net.DatagramSocket</code>
     * interno da impiegare per l'accesso alla rete, assegnandogli un
     * numero di porta UDP da impiegare.
     *
     * @param       porta   il numero di porta UDP da impiegare.
     * @exception   java.net.SocketException   se ottenuta creando il <i>socket</i>.
     */
    public SocketUDPaffidabile(int porta) throws SocketException {
        sock = new DatagramSocket(porta);
        sock.setSoTimeout(1);   // Socket non bloccante
        
    }
    
    /**
     * Metodo per inviare in modo affidabile un datagramma UDP.
     * <i>Wrapper</i> di <code>int invia(DatagramPacket[])</code>.
     *
     * @param       d             il datagramma UDP da recapitare.
     * @exception   IOException   se generato dal <i>socket</i> usato internamente.
     * @return      il numero di <code>DatagramPacket</code> per i quali
     *              è giunta la conferma della ricezione (ACK).
     * @see         #invia(DatagramPacket[])
     */
    public final synchronized int invia(DatagramPacket d) throws IOException {
        DatagramPacket[] vett = { d };
        return invia(vett);
    }
    
    /**
     * Metodo per inviare in modo affidabile dei datagrammi UDP.
     *
     * @param       d             i datagrammi UDP da recapitare.
     * @exception   IOException   se generato dal <i>socket</i> usato internamente.
     * @return      il numero di <code>DatagramPacket</code> per i quali
     *              è giunta la conferma della ricezione (ACK).
     */
    public final synchronized int invia(DatagramPacket[] d) throws IOException {
        int            i, j, seq_num, consegnati = 0;   // Il valore di ritorno
        
        byte[]         buf = new byte[0xFFFF - 8];      // Max. buffer UDP
        
        DatagramPacket risposta    = new DatagramPacket(buf, buf.length),
        acknowledge = new DatagramPacket(new byte[4], 4);
        
        int[] ack = new int[d.length];   // Gli ACK attesi
        
        for (i = 0; i < d.length; ++i)   // Calcolo degli acknowledge
            
            ack[i] = inserisciSeqNum(d[i]);
        
        flush();   // Flush dei datagrammi ricevuti sino ad ora
        
        
        for (i = 0; i < TENTATIVI && consegnati < d.length; ++i) {
            // Invio dei pacchetti != null...
            
            for (j = 0; j < d.length; ++j)
                if (d[j] != null)
                    sock.send(d[j]);
            
            // ...e attesa di altrettanti ACK entro TIMEOUT a partire da ora
            
            long timeout = System.currentTimeMillis() + TIMEOUT;
            do
                try {
                    risposta.setData(buf, 0, buf.length);
                    sock.receive(risposta);
                    if (risposta.getLength() < 4)   // BEST-EFFORT => scartati se meno di 4 byte
                        
                        continue;
                    if (risposta.getLength() == 4) {  // E' un acknowledge?
                        
                        seq_num = Utili.intFromArray(risposta.getData(), risposta.getOffset());
                        for (j = 0; j < d.length; ++j)
                            if (ack[j] == seq_num && d[j] != null)
                                if (risposta.getAddress().equals(d[j].getAddress()) && risposta.getPort() == d[j].getPort()) {
                                    d[j] = null;    // La consegna di d[j] è riuscita!
                                    
                                    ++consegnati;   // Una consegna effettuata in più
                                    
                                    break;
                                }
                    }
                    else {   // No, non è un acknowledge...
                        
                        seq_num = estraiSeqNum(risposta);
                        Utili.intToArray(seq_num, acknowledge.getData(), acknowledge.getOffset());
                        acknowledge.setAddress(risposta.getAddress());
                        acknowledge.setPort(risposta.getPort());
                        sock.send(acknowledge);
                        allaCache(risposta, seq_num);
                    }
                } catch (InterruptedIOException e) {}
            while (System.currentTimeMillis() <= timeout && consegnati < d.length);
        }
        return consegnati;
    }
    
    /**
     * Metodo per ottenere il numero di <code>DatagramPacket</code>
     * disponibili alla lettura.
     *
     * @exception   IOException   se generata dal <code>DatagramSocket</code> interno.
     * @return      il numero di datagrammi che possono essere letti senza
     *              bloccarsi in attesa sul <i>socket</i> interno.
     * @see         #sock
     */
    public final synchronized int disponibili() throws IOException {
        flush();
        return cache.size();
    }
    
    /**
     * Lettura, con eventuale attesa, del primo datagramma disponibile.
     *
     * @param       d             il <code>DatagramPacket</code> ricevuto.
     * @exception   IOException   se generata dal <code>DatagramSocket</code> interno.
     */
    public synchronized void ricevi(DatagramPacket d) throws IOException {
        while (cache.size() == 0)
            flush();
        DatagramPacket tmp = (DatagramPacket) cache.remove(0);
        System.arraycopy(tmp.getData(), tmp.getOffset(), d.getData(), d.getOffset(), tmp.getLength());
        d.setLength(tmp.getLength());
        d.setAddress(tmp.getAddress());
        d.setPort(tmp.getPort());
    }
    
    /**
     * Chiusura del <i>socket</i> UDP usato internamente. Invoca
     * semplicemente <code>sock.close()</code>.
     *
     * @see   #sock
     */
    public void chiudi() {
        sock.close();
    }
    
    /**
     * Metodo che processa i datagrammi prima di inviarli direttamente
     * in rete. Per ciascun messaggio UDP passato come parametro ad
     * <code>invia(...)</code> viene invocato <u>una ed una sola</u> volta.
     *
     * @param    d   il <code>DatagramPacket</code> da manipolare.
     * @return   il <i>sequence number</i> aggiunto a <code>d</code>.
     * @see      #invia(DatagramPacket)
     * @see      #invia(DatagramPacket[])
     */
    protected abstract int inserisciSeqNum(DatagramPacket d);
    
    /**
     * Metodo che processa i datagrammi (non ACK) ricevuti dalla rete.
     * Per ogni messaggio UDP pervenuto a <code>ricevi(...)</code>
     * questa procedura viene invocata <u>una ed una sola</u> volta.
     *
     * @param    d   il <code>DatagramPacket</code> da manipolare.
     * @return   il <i>sequence number</i> letto da <code>d</code>.
     * @see      #ricevi(DatagramPacket)
     */
    protected abstract int estraiSeqNum(DatagramPacket d);
    
    /**
     * Lettura di tutti i datagrammi, diversi dagli ACK, disponibili
     * da <code>sock</code>. Gli ACK vengono <u>scartati</u>, mentre
     * gli altri messaggi vengono messi nella <i>cache</i> interna
     * <code>cache</code> se non sono già stati ricevuti.
     *
     * @exception   IOException   se generata dal <code>DatagramSocket</code> interno.
     * @see         #sock
     * @see         #cache
     */
    private void flush() throws IOException {
        try {
            byte[]         buf = new byte[0xFFFF - 8];
            DatagramPacket d   = new DatagramPacket(buf, buf.length),
            ack = new DatagramPacket(new byte[4], 4);
            
            while (true) {
                d.setData(buf, 0, buf.length);
                sock.receive(d);
                if (d.getLength() <= 4)   // Scarto di ACK e datagrammi fasulli
                    
                    continue;
                int seq_num = estraiSeqNum(d);
                Utili.intToArray(seq_num, ack.getData(), ack.getOffset());
                ack.setAddress(d.getAddress());
                ack.setPort(d.getPort());
                sock.send(ack);
                allaCache(d, seq_num);
            }
        } catch (InterruptedIOException e) {}
    }
    
    /**
     * Accesso in scrittura alla <i>cache</i> dei <code>DatagramPacket</code>
     * estratti da <code>sock</code> ma non ancora letti dall'utente.
     * Prima di aggiungere effettivamente il datagramma a <code>cache</code>
     * viene controllato se il medesimo segmento informativo è già
     * stati ricevuto, ispezionando le <i>entry</i> di <code>history</code>;
     * questo evita duplicazioni per il <i>client</i> esterno di questo
     * <code>SocketUDPaffidabile</code>.
     *
     * @param   d         il datagramma UDP appena estratto dal <i>socket</i> interno.
     * @param   seq_num   il <i>sequence number</i> di <code>d</code>.
     * @see     #sock
     * @see     #cache
     * @see     #history
     */
    private void allaCache(DatagramPacket d, int seq_num) {
        for (int i = 0; i < history.size(); ++i) {
            HistoryEntry entry = (HistoryEntry) history.elementAt(i);
            if (d.getAddress().equals(entry.IP) && d.getPort() == entry.port)
                if (entry.last_seq_num > seq_num)   // Già ricevuto?
                    
                    return;
                else {
                    history.remove(i);
                    break;
                }
        }
        history.add(new HistoryEntry(d.getAddress(), d.getPort(), seq_num));
        byte[] buf = new byte[d.getLength()];
        System.arraycopy(d.getData(), d.getOffset(), buf, 0, buf.length);
        cache.add(new DatagramPacket(buf, buf.length, d.getAddress(), d.getPort()));
    }
}




/**
 * Entità che rappresenta un'<i>entry</i> dell'<i>history</i> interna
 * di <code>SocketUDPaffidabile</code>, usata per evitare duplicazioni
 * dei messaggi UDP verso l'utente.
 * <p>
 * In sintesi, per ogni <i>endpoint</i> viene conservato il numero
 * di sequenza più alto ricevuto.
 * <p>
 * Non sono inclusi dei <b>selettori</b> per accedere ai vari campi
 * interni per questioni di efficienza. L'accesso a tali variabili è
 * diretto per le classi appartenenti al <code>package chat</code>.
 * <p>
 *
 * @author    <em>Alessandro Gaspari</em>
 * @version   1.0
 */
class HistoryEntry {
    
    /** L'indirizzo IP di provenienza dei datagrammi. */
    InetAddress IP;
    
    /** Il numero di porta di provenienza dei datagrammi. */
    int port;
    
    /** Il <i>sequence number</i> più alto ricevuto da questo <i>endpoint</i>. */
    int last_seq_num;
    
    /**
     * Costruttore che inizializza tutti i campi dell'istanza.
     *
     * @param   IP             indirizzo IP del mittente.
     * @param   port           porta UDP di provenienza del datagramma.
     * @param   last_seq_num   il <i>sequence number</i> per questo <i>endpoint</i>.
     */
    HistoryEntry(InetAddress IP, int port, int last_seq_num) {
        this.IP           = IP;
        this.port         = port;
        this.last_seq_num = last_seq_num;
    }
}
