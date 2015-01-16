package chat;



import cryptix.provider.Cryptix;

import cryptix.provider.rsa.RawRSAPrivateKey;

import cryptix.provider.rsa.RawRSAPublicKey;

import java.io.BufferedReader;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;

import java.io.FileInputStream;

import java.io.FileReader;

import java.io.IOException;

import java.io.ObjectInputStream;

import java.io.ObjectOutputStream;

import java.net.DatagramPacket;

import java.net.SocketException;

import java.security.Key;

import java.security.KeyException;

import java.security.PrivateKey;

import java.security.PublicKey;

import java.security.SecureRandom;

import java.security.Security;

import java.util.Vector;

import xjava.security.KeyGenerator;



/**
 * Classe che realizza il monitor di output della <i>chat</i>; in
 * questo contesto il termine "monitor" è da indendersi come "video",
 * "area di visualizzazione".
 * <p>
 *
 * @author    <em>Alessandro Gaspari</em>
 * @version   1.0
 */
public class Monitor extends SocketUDPaffidabile {
    
    /** Porta UDP di default in assenza di direttive in fase di costruzione. */
    public static final int PORTA_DEFAULT = 2001;
    
    /**
     * Nome del file di scambio dei messaggi con <code>Console</code>.
     *
     * @see   Console
     */
    public static final String FILE_SWAP = "swap.txt";
    
    /**
     * Nome del file ASCII contenente la rubrica delle conoscenze.
     *
     * @see   Database
     */
    public static final String FILE_DATABASE = "Database.txt";
    
    /** Parametro di configurazione per usare o meno la crittografia. */
    private static final boolean ACCLUDI_FIRMA_DIGITALE = true;
    
    /** Parametro di configurazione per impiegare o meno la firma digitale. */
    private static final boolean ADOTTA_CRITTOGRAFIA = true;
    
    
    /**
     * Un <i>wrapper</i> attorno al file coi messaggi provenienti dalla
     * <code>Console</code>.
     *
     * @see   Console
     */
    private BufferedReader swap;
    
    /** La rubrica con tutte le conoscenze. */
    private Database rubrica;
    
    /** L'identità da adottare durante la <i>chat</i>. */
    private String nickname;
    
    /** La chiave crittografica pubblica dell'utente. */
    private PublicKey c_pubblica;
    
    /** La chiave crittografica privata dell'utente. */
    private PrivateKey c_privata;
    
    /** Oggetto per generare e verificare le firme digitali. */
    private Firma md5rsa;
    
    /** Oggetto per applicare l'algoritmo crittografico RSA ai dati. */
    private Crittografia rsa;
    
    /** Il generatore delle chiavi segrete di sessione per IDEA. */
    private KeyGenerator generatore_c;
    
    /** La chiave segreta di sessione per IDEA. */
    private Key c_sessione;
    
    /** Oggetto per applicare l'algoritmo crittografico IDEA ai dati. */
    private Crittografia idea;
    
    /**
     * Lista degli utenti a cui sono stati iniviati uno o più
     * datagrammi di tipo SYN. Usato per decidere se accettare o meno
     * un OK ricevuto.
     *
     * @see   Protocollo#SYN
     * @see   Protocollo#OK
     */
    private Vector SYNinviati = new Vector();
    
    /**
     * Archivio degli utenti che ci hanno inviato un SYN ed ai quali
     * non abbiamo ancora inviato il corrispondente OK. Rappresenta
     * la lista dei SYN in attesa di risposta.
     *
     * @see   Protocollo#SYN
     * @see   Protocollo#OK
     */
    private Database SYNricevuti = new Database();
    
    /** Archivio con i <code>Contatto</code> dell'attuale gruppo di discussione. */
    private Database interlocutori = new Database();
    
    /** Un generatore di "casualità". */
    private SecureRandom entropia = new SecureRandom();
    
    /** Numero di sequenza dei datagrammi UDP inviati. */
    private int seq_num = Integer.MIN_VALUE + entropia.nextInt(0xffff);
    
    
    /**
     * Costruttore che avvia il <code>Monitor</code> sulla porta UDP
     * di default <code>PORTA_DEFAULT</code>. In caso di differenti
     * eccezioni termina il programma mediante <code>System.exit(0)</code>.
     *
     * @exception   SocketException   se generata dal costruttore della super-classe.
     * @see         #PORTA_DEFAULT
     * @see         SocketUDPaffidabile
     * @see         SocketUDPaffidabile#SocketUDPaffidabile(int)
     */
    Monitor() throws SocketException {
        this(PORTA_DEFAULT);
    }
    
    /**
     * Costruttore che avvia il <code>Monitor</code> sulla porta UDP
     * indicata. In caso di differenti eccezioni termina il programma
     * mediante <code>System.exit(0)</code>.
     *
     * @param       porta             la porta UDP da usare per il <i>socket reliable</i>.
     * @exception   SocketException   se generata dal costruttore della super-classe.
     * @see         SocketUDPaffidabile
     * @see         SocketUDPaffidabile#SocketUDPaffidabile(int)
     */
    Monitor(int porta) throws SocketException {
        super(porta);
        try {
            System.out.println("Monitor sulla porta UDP " + porta + ".");
            swap    = new BufferedReader(new FileReader(FILE_SWAP));
            rubrica = new Database(FILE_DATABASE);
            System.out.print("Lettura del nickname in corso...");
            nickname = swap.readLine();
            System.out.println("fatto.\nBenvenuto \"" + nickname + "\"!");
            c_pubblica   = new RawRSAPublicKey(new FileInputStream(nickname + ".pub"));
            c_privata    = new RawRSAPrivateKey(new FileInputStream(nickname + ".pri"));
            md5rsa       = new Firma("MD5/RSA");
            rsa          = new Crittografia("RSA/ECB/PKCS#7");
            generatore_c = KeyGenerator.getInstance("IDEA");
            generatore_c.initialize(entropia);
            idea         = new Crittografia("IDEA/ECB/PKCS#5");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        rubrica.cancella(nickname);   // User non nella rubrica!
        
    }
    
    /**
     * <b>Procedura</b> che numera i datagrammi UDP prima di inviarli
     * in rete; utilizza <code>seq_num</code> incrementandolo di una
     * quantità casuale ogni volta, lo firma se la costante di classe
     * <code>ACCLUDI_FIRMA_DIGITALE</code> vale <code>true</code>,
     * infine lo cifra se </code>ADOTTA_CRITTOGRAFIA == true</code>.
     *
     * @param    d   il <code>DatagramPacket</code> da numerare.
     * @return   il <i>sequence number</i> aggiunto a <code>d</code>.
     * @see      #seq_num
     * @see      #ACCLUDI_FIRMA_DIGITALE
     * @see      #ADOTTA_CRITTOGRAFIA
     * @see      SocketUDPaffidabile#inserisciSeqNum(DatagramPacket)
     */
    protected int inserisciSeqNum(DatagramPacket d) {
        Messaggio m = messaggioFromDatagram(d);
        seq_num += entropia.nextInt(0xffff) + 1;
        if (ACCLUDI_FIRMA_DIGITALE)
            m = new Messaggio(m.daChi(), m.aChi(), seq_num, m.tipo(), m.testo(), md5rsa.firma(m.testo(), c_privata));
        else
            m.scriviSeqNum(seq_num);
        if (ADOTTA_CRITTOGRAFIA)
            try {
                m = new Messaggio(m.daChi(), m.aChi(),
                                  interlocutori.contiene(m.aChi()) ? idea.cifra(m.cheCosa(), c_sessione)
                                  : rsa.cifra(m.cheCosa(), rubrica.seleziona(m.aChi()).chiave()));
                
            } catch (KeyException e) {
                e.printStackTrace();
            }
        messaggioToDatagram(m, d);
        return seq_num;
    }
    
    /**
     * <b>Procedura</b> che ricava il numero di sequenza dei datagrammi
     * UDP appenda ricevuti via <i>socket</i>.
     * Decifra il <code>DatagramPacket</code> se la costante di classe
     * </code>ADOTTA_CRITTOGRAFIA</code> vale <code>true</code>,
     * scegliendo la chiave e l'algoritmo di decifratura sulla base
     * del mittente del datagramma e sulla lista degli interlocutori.
     *
     * @param    d   il <code>DatagramPacket</code> da manipolare.
     * @return   il <i>sequence number</i> letto da <code>d</code>.
     * @see      #ADOTTA_CRITTOGRAFIA
     * @see      #interlocutori
     * @see      SocketUDPaffidabile#estraiSeqNum(DatagramPacket)
     */
    protected int estraiSeqNum(DatagramPacket d) {
        Messaggio m = messaggioFromDatagram(d);
        if (ADOTTA_CRITTOGRAFIA)
            try {
                m = new Messaggio(m.daChi(), m.aChi(),
                                  interlocutori.contiene(m.daChi()) ? idea.decifra(m.cheCosa(), c_sessione)
                                  : rsa.decifra(m.cheCosa(), c_privata));
                messaggioToDatagram(m, d);
            } catch (KeyException e) {
                e.printStackTrace();
            }
        return m.seqNum();
    }
    
    /**
     * Metodo che serializza un <code>Messaggio</code> riversandone
     * il contenuto entro un <code>DatagramPacket</code>.
     *
     * @param   m   il messaggio da serializzare.
     * @param   d   il datagramma UDP entro cui scrivere la versione
     *              serializzata di <code>m</code>.
     * @see     Messaggio
     */
    void messaggioToDatagram(Messaggio m, DatagramPacket d) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream    oos  = new ObjectOutputStream(baos);
            oos.writeObject(m);
            oos.flush();
            byte[] buf = baos.toByteArray();
            oos.close();
            d.setData(buf, 0, buf.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Metodo che estrae un <code>Messaggio</code> serializzato da un
     * <code>DatagramPacket</code>. Compie l'azione complementare di
     * <code>messaggioToDatagram(Messaggio, DatagramPacket)</code>.
     *
     * @param    d   il datagramma da cui leggere la versione
     *               serializzata di un <code>Messaggio</code>.
     * @return   il messaggio estratto da <code>d</code>, oppure
     *           <code>null</code> in caso di errore.
     * @see      #messaggioToDatagram(Messaggio, DatagramPacket)
     * @see      Messaggio
     */
    Messaggio messaggioFromDatagram(DatagramPacket d) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(d.getData(), d.getOffset(), d.getLength()));
            Messaggio m = (Messaggio) ois.readObject();
            ois.close();
            return m;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * <i>Main-loop</i> del programma: legge il contenuto del file di
     * scambio con la <code>Console</code> e processa il comando,
     * quindi esamina anche il <i>socket</i> affidabile per verificare
     * i <code>Messaggio</code> ricevuti.
     *
     * @see   #swap
     * @see   Console
     */
    void esegui() {
        byte[]         buf = new byte[0xFFFF - 8];
        DatagramPacket d   = new DatagramPacket(buf, buf.length);
        
        Vector cmd_uscita = new Vector();   // I 4 comandi per terminare
        
        cmd_uscita.add("bye");
        cmd_uscita.add("exit");
        cmd_uscita.add("quit");
        cmd_uscita.add("stop");
        
        while (true)   // Main-loop
            
            try {
                if (swap.ready()) {   // Input disponibile dal file di scambio?
                    
                    String cmd = swap.readLine().trim();
                    if (cmd_uscita.contains(cmd))
                        break;
                    processaComando(cmd);
                }
                if (disponibili() == 0)   // Ci sono datagrammi disponibili?
                    
                    continue;
                d.setData(buf, 0, buf.length);
                ricevi(d);
                Messaggio m = messaggioFromDatagram(d);
                if (!m.aChi().equals(nickname))   // E' per noi?
                    
                    continue;
                Endpoint da_dove = new Endpoint(d.getAddress(), d.getPort());
                if (ACCLUDI_FIRMA_DIGITALE)   // Verifica della firma!
                    
                    if (!md5rsa.firmaOk(m.testo(), m.firma(), (PublicKey) rubrica.seleziona(m.daChi()).chiave())) {
                        System.out.println(" !!!FIRMA NON VALIDA DA " + m.daChi() + "@" + da_dove + "!!!");
                        continue;
                    }
                switch (m.tipo()) {
                    case Protocollo.SYN:  System.out.println(" <<<SYN DA " + m.daChi() + "@" + da_dove + ">>>");
                        SYNricevuti.modifica(new Contatto(m.daChi(), rubrica.seleziona(m.daChi()).chiave(), da_dove));
                        break;
                    case Protocollo.OK:   if (SYNinviati.contains(m.daChi())) {
                        System.out.println(" <<<OK DA " + m.daChi() + "@" + da_dove + ">>>");
                        if (interlocutori.quanti() > 0)
                            System.out.println(" Impossibile accettare \"" + m.daChi() + "\" a dialogo già in corso!");
                        else {
                            SYNinviati.remove(m.daChi());
                            interlocutori.modifica(new Contatto(m.daChi(), rubrica.seleziona(m.daChi()).chiave(), da_dove));
                            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(m.testo()));
                            c_sessione = (Key) ois.readObject();
                            while (ois.available() > 0)
                                interlocutori.modifica((Contatto) ois.readObject());
                            ois.close();
                            visualizzaInterlocutori();
                        }
                    }
                        break;
                    case Protocollo.ADD:  if (interlocutori.contiene(m.daChi())) {
                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(m.testo()));
                        while (ois.available() > 0)
                            interlocutori.modifica((Contatto) ois.readObject());
                        ois.close();
                        visualizzaInterlocutori();
                    }
                        break;
                    case Protocollo.TEXT: if (interlocutori.contiene(m.daChi()))
                        System.out.println("[" + m.daChi() + "] " + new String(m.testo()));
                        break;
                    case Protocollo.FIN:  System.out.println(" <<<FIN DA " + m.daChi() + "@" + da_dove + ">>>");
                        interlocutori.cancella(m.daChi());
                        break;
                    default:              System.out.println(" ???MESSAGGIO ILLEGALE DI TIPO 0x" + Utili.hexByte(m.tipo()) + " DA " + m.daChi() + "@" + da_dove + "???");
                }   // switch (m.tipo())
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        try {       // Chiusura del file di scambio con la Console
            
            swap.close();
        } catch (IOException e) {}
        try {       // Chiusura del dialogo in corso, avvisando il gruppo
            
            agliInterlocutori(Protocollo.FIN, null);
        } catch (IOException e) {}
        chiudi();   // Chiusura del socket UDP affidabile
        
    }
    
    /**
     * Interpreta un comando letto dal file di scambio <code>swap</code>
     * con la <code>Console</code>. Non termina il programma perché non
     * riconosce i comandi di uscita.
     *
     * @param       cmd           la linea letta da <code>swap</code>.
     * @exception   IOException   causa le azioni che hanno a che
     *                            vedere con il <i>socket</i> interno.
     * @see         Console
     */
    void processaComando(String cmd) throws IOException {
        cmd = cmd.trim();
        if (cmd.length() == 0)
            return;
        if (cmd.equals("."))   // Chiudere il dialogo in corso
            
            if (interlocutori.quanti() == 0)
                System.out.println(" Nessun dialogo in corso.");
            else
                try {
                    System.out.println(" Chiusi regolarmente " + agliInterlocutori(Protocollo.FIN, null) + " colloqui su " + interlocutori.quanti() + ".");
                    interlocutori.cancella();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            else   // cmd != "."
                
                if (cmd.startsWith(">") || cmd.startsWith("<")) {
                    String chi = cmd.substring(1).trim();
                    if (chi.length() == 0)
                        visualizzaInterlocutori();
                    else   // cmd != "." && chi != ""
                        
                        if (interlocutori.contiene(chi))
                            System.out.println(" \"" + chi + "\" è già un interlocutore.");
                        else
                            if (cmd.startsWith("<"))
                                if (SYNricevuti.contiene(chi)) {   // Inviargli un OK
                                    
                                    if (interlocutori.quanti() == 0)
                                        c_sessione = generatore_c.generateKey();
                                    
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    ObjectOutputStream    oos  = new ObjectOutputStream(baos);
                                    oos.writeObject(c_sessione);
                                    Contatto[] v = interlocutori.tutti();
                                    for (int i = 0; i < v.length; ++i)
                                        oos.writeObject(v[i]);
                                    oos.flush();
                                    byte[] buf = baos.toByteArray();
                                    oos.close();
                                    Contatto c = SYNricevuti.seleziona(chi);
                                    DatagramPacket d = new DatagramPacket(new byte[0], 0, ((Endpoint) c.dove().get(0)).IP(), ((Endpoint) c.dove().get(0)).porta());
                                    messaggioToDatagram(new Messaggio(nickname, chi, Protocollo.OK, buf), d);
                                    if (invia(d) == 1) {
                                        System.out.println(" OK consegnato a \"" + chi + "\".");
                                        if (interlocutori.quanti() > 0) {   // Avvisare il gruppo del nuovo ingresso
                                            
                                            baos = new ByteArrayOutputStream();
                                            oos  = new ObjectOutputStream(baos);
                                            oos.writeObject(c);
                                            oos.flush();
                                            buf = baos.toByteArray();
                                            oos.close();
                                            System.out.println(" Avvisati " + agliInterlocutori(Protocollo.ADD, buf) + " interlocutori su " + interlocutori.quanti() + ".");
                                        }
                                        interlocutori.modifica(c);
                                        SYNricevuti.cancella(chi);
                                    }
                                    else
                                        System.out.println(" Impossibile consegnare l'OK a \"" + chi + "\".");
                                }
                                else
                                    System.out.println(" Nessun SYN ricevuto da \"" + chi + "\".");
                                else   // cmd != "." && chi != "" && chi.startsWith(">")
                                    
                                    if (rubrica.contiene(chi))   // Consegnargli un SYN
                                        
                                        try {
                                            Vector           dove = rubrica.seleziona(chi).dove();
                                            DatagramPacket[] d    = new DatagramPacket[dove.size()];
                                            
                                            for (int i = 0; i < d.length; ++i)
                                                messaggioToDatagram(new Messaggio(nickname, chi, Protocollo.SYN, null), d[i] = new DatagramPacket(new byte[0], 0, ((Endpoint) dove.get(i)).IP(), ((Endpoint) dove.get(i)).porta()));
                                            System.out.println(" Recapitati " + invia(d) + " SYN su " + d.length + " per \"" + chi + "\".");
                                            if (!SYNinviati.contains(chi))
                                                SYNinviati.add(chi);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    else
                                        System.out.println(" \"" + chi + "\" sconosciuto.");
                }
                else   // cmd != "." && cmd.charAt(0) != '>' && cmd.charAt(0) != '<'
                    
                    if (interlocutori.quanti() == 0)
                        System.out.println(" Nessun dialogo in corso.");
                    else
                        try {
                            System.out.println("[" + nickname + "] " + cmd + " {" + agliInterlocutori(Protocollo.TEXT, cmd.getBytes()) + "/" + interlocutori.quanti() + "}");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
    }
    
    /**
     * <b>Funzione</b> che visualizza sullo <i>standard output</i>
     * i nomi degli interlocutori attuali.
     *
     * @see   #interlocutori
     */
    void visualizzaInterlocutori() {
        System.out.println(" Interlocutori:\n ==============\n\n");
        Contatto[] v = interlocutori.tutti();
        for (int i = 0; i < v.length; ++i)
            System.out.println(" " + v[i].nome());
    }
    
    /**
     * Metodo che invia il messaggio specificato a tutti gli utenti
     * dell'attuale gruppo di discussione.
     *
     * @param       tipo   il "tipo" di <code>Messaggio</code> da inviare.
     * @param       cosa   il testo da includere nel <code>Messaggio</code>.
     * @exception   se generata inviando il/i datagramma/i in rete.
     * @return      il numero di consegne confermate tramite ACK.
     * @see         Protocollo
     */
    int agliInterlocutori(byte tipo, byte[] cosa) throws IOException {
        if (interlocutori.quanti() == 0)
            return 0;
        DatagramPacket d[]   = new DatagramPacket[interlocutori.quanti()];
        Contatto[]     a_chi = interlocutori.tutti();
        for (int i = 0; i < d.length; ++i)
            messaggioToDatagram(new Messaggio(nickname, a_chi[i].nome(), tipo, cosa), d[i] = new DatagramPacket(new byte[0], 0, ((Endpoint) a_chi[i].dove().get(0)).IP(), ((Endpoint) a_chi[i].dove().get(0)).porta()));
        return invia(d);
    }
    
    
    /**
     * L'<i>entry-point</i> del programma. Installa dinamicamente la
     * libreria <code>Cryptix</code> ed avvia un'istanza di
     * <code>Monitor</code>, assegnandole la porta UDP eventualmente
     * specificata tramite la linea di comando.
     *
     * @param   args   gli argomenti della linea di comando.
     */
    public static void main(String[] args) {
        Security.addProvider(new Cryptix());   // Installa dinamicamente Cryptix
        
        try {
            switch (args.length) {
                case 0:  new Monitor().esegui();
                    break;
                case 1:  new Monitor(Integer.parseInt(args[0])).esegui();
                    break;
                default: System.out.println("Sintassi d'uso:   java chat.Monitor [porta UDP]");
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}