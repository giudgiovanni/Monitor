package chat;



/**
 * Classe che identifica un messaggio scambiato fra utenti adatto
 * alle esigenze della <i>chat</i>: interpreta il contenuto del
 * <i>buffer</i> interno di <code>MessaggioBase</code> per ottenere
 * un <i>sequence number</i>, un <code>byte</code> che assegna il
 * "tipo" di segmento informativo, un'eventuale firma digitale del
 * testo vero e prorio incluso nella busta rappresentata.
 * <p>
 * Più precisamente, il <i>buffer</i> interno alla super-classe viene
 * così interpretato da <code>Messaggio</code>:
 * <ul>
 *   <li>i primi 4 byte contengono un <code>int</code> che viene
 *       riconosciuto come <i>sequence number</i> del messaggio;
 *       tale intero viene serializzato secondo la semantica di
 *       <code>Utili.intToArray(...)</code> e <code>Utili.intFromArray(...)</code>;</li>
 *   <li>ulteriori 4 byte contengono l'<code>int</code> che tiene
 *       traccia della lunghezza della firma inclusa; sia <i>n</i>
 *       tale valore, maggiore o uguale a zero;</li>
 *   <li>un byte che rappresenta il "tipo" di datagramma;</li>
 *   <li>di seguito c'é il vero contenuto testuale di questa busta,
 *       inteso come successione di zero o più <code>byte</code>;</li>
 *   <li>gli ultimi <i>n</i> byte del <i>buffer</i> vengono considerati
 *       come campo "firma digitale" del suddetto testo.</li>
 * </ul>
 * Globalmente la cosa può esser vista in questo modo: un <i>header</i>
 * iniziale composto da 4 + 4 + 1 = 9 byte, il testo vero e proprio,
 * quindi un <i>footer</i> costituito dalla firma digitale del testo.
 * La lunghezza del <i>footer</i> è maggiore o uguale a zero, e tale
 * valore è mantenuto nell'<i>header</i>, mentre la lunghezza del testo
 * viene calcolata per differenza fra la lunghezza dell'intero <i>buffer</i>
 * e quella di <i>header+footer</i>.
 * <p>
 *
 * @author    <em>Alessandro Gaspari</em>
 * @version   1.0
 * @see       MessaggioBase
 * @see       MessaggioBase#che_cosa
 * @see       Utili#intToArray(int, byte[], int)
 * @see       Utili#intFromArray(byte[], int)
 */
public class Messaggio extends MessaggioBase {
    
    /**
     * Costruttore che richiama quello della super-classe.
     *
     * @param   da     il mittente del messaggio.
     * @param   a      il destinatario del suddetto.
     * @param   cosa   le informazioni da inserire nella busta.
     * @see     MessaggioBase#MessaggioBase(String, String, byte[])
     */
    public Messaggio(String da, String a, byte[] cosa) {
        super(da, a, cosa);
    }
    
    /**
     * Costruttore che permette di assegnare un valore specifico a
     * tutti i campi dell'istanza di <code>Messaggio</code>.
     *
     * @param   da        il mittente del messaggio.
     * @param   a         il destinatario del suddetto.
     * @param   seq_num   il <i>sequence number</i> da assegnargli.
     * @param   tipo      il "tipo" del segmento informativo.
     * @param   testo     le informazioni testuali da inserire nella busta.
     * @param   firma     la firma digitale relativa a <code>testo</code>.
     */
    public Messaggio(String da, String a, int seq_num, byte tipo, byte[] testo, byte[] firma) {
        super(da, a, new byte[4 + 4 + 1 + (testo == null ? 0 : testo.length) + (firma == null ? 0 : firma.length)]);
        scriviSeqNum(seq_num);
        Utili.intToArray(firma == null ? 0 : firma.length, che_cosa, 4);
        che_cosa[8] = tipo;
        if (testo != null)
            System.arraycopy(testo, 0, che_cosa, 4 + 4 + 1, testo.length);
        if (firma != null)
            System.arraycopy(firma, 0, che_cosa, che_cosa.length - firma.length, firma.length);
    }
    
    /**
     * Costruttore più semplice che assegna un valore ai campi più
     * usati dell'istanza di <code>Messaggio</code>. Equivale a:
     * <p>
     * <code>Messaggio(da, a, 0, tipo, testo, null);</code>
     *
     * @param   da        il mittente del messaggio.
     * @param   a         il destinatario del suddetto.
     * @param   tipo      il "tipo" del segmento informativo.
     * @param   testo     le informazioni testuali da inserire nella busta.
     * @see     #Messaggio(String, String, int, byte, byte[], byte[])
     */
    public Messaggio(String da, String a, byte tipo, byte[] testo) {
        this(da, a, 0, tipo, testo, null);
    }
    
    /**
     * <b>Modificatore primitiva</b> che scrive il <i>sequence number</i>.
     *
     * @param   seq_num   il numero di sequenza da assegnare al messaggio.
     */
    public void scriviSeqNum(int seq_num) {
        Utili.intToArray(seq_num, che_cosa, 0);
    }
    
    /**
     * <b>Selettore primitiva</b> che legge il <i>sequence number</i>.
     *
     * @return   il numero di sequenza del messaggio.
     */
    public int seqNum() { return Utili.intFromArray(che_cosa, 0); }
    
    /**
     * <b>Selettore primitiva</b> che ritorna il "tipo" del messaggio.
     *
     * @return   il tipo assegnato al <code>Messaggio</code>.
     */
    public byte tipo() {
        return che_cosa[8];
    }
    
    /**
     * <b>Selettore primitiva</b> per accedere al testo del messaggio.
     * <u>ATTENZIONE</u>: il contenuto del messaggio viene estratto
     * per copia, quindi non è possibile invocare questo metodo per
     * poi accedere in scrittura al messaggio stesso.
     *
     * @return   il testo del messaggio; può essere un <code>byte[]</code>
     *           avente lunghezza nulla, ma non <code>null</code>.
     */
    public byte[] testo() {
        byte[] ret = new byte[che_cosa.length - 4 - 4 - 1 - Utili.intFromArray(che_cosa, 4)];
        System.arraycopy(che_cosa, 4 + 4 + 1, ret, 0, ret.length);
        return ret;
    }
    
    /**
     * <b>Selettore primitiva</b> per ottenere il campo "firma digitale".
     * <u>ATTENZIONE</u>: il contenuto della firma viene estratto per
     * copia, quindi non è possibile invocare questo metodo per poi
     * modificare la firma contenuta nel messaggio.
     *
     * @return   il campo "firma"; può essere un <code>byte[]</code>
     *           avente lunghezza nulla, ma non <code>null</code>.
     */
    public byte[] firma() {
        byte[] ret = new byte[Utili.intFromArray(che_cosa, 4)];
        System.arraycopy(che_cosa, che_cosa.length - ret.length, ret, 0, ret.length);
        return ret;
    }
}
