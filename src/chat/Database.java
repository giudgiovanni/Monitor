package chat;



import java.io.FileInputStream;

import java.io.InputStreamReader;

import java.io.FileNotFoundException;

import java.io.IOException;

import java.util.Collections;

import java.util.Comparator;

import java.util.SortedSet;

import java.util.TreeSet;



/**
 * Classe per costruire un archivio di entità <code>Contatto</code>
 * ordinate in modo crescente sulla base del campo <code>nome</code>.
 * Realizza un semplice <i>database</i> che usa il nome come campo
 * chiave di ricerca.
 * <p>
 * Idonea anche a situazioni <i>multi-thread</i>.
 * <p>
 *
 * @author    <em>Marco Cimatti</em>
 * @version   1.0
 * @see       Contatto
 * @see       Contatto#nome()
 */
public class Database {
    
    /** La tabella ad albero binario ordinata che contiene i singoli <i>record</i>. */
    private SortedSet s = Collections.synchronizedSortedSet(new TreeSet(new ComparaContatto()));
    
    
    /** Costruttore che istanzia un <i>database</i> vuoto. */
    public Database() {}
    
    /**
     * Costruttore che legge il contenuto del <i>database</i> da un
     * file. Adotta <code>Parser</code> per processare il file ASCII
     * il cui nome è passato come parametro.
     *
     * @param       nome_file               il nome del file ASCII da leggere.
     * @exception   FileNotFoundException   se il file indicato manca.
     * @exception   IOException             in caso di errori accedendo in lettura al file.
     * @see         Parser
     */
    public Database(String nome_file) throws FileNotFoundException, IOException {
        InputStreamReader is = new InputStreamReader(new FileInputStream(nome_file));
        try {
            Parser p = new Parser(is);
            p.avanzaToken();
            while (p.tokenCorrente().length() > 0)
                modifica(p.prossimo());
        } finally { is.close(); }
    }
    
    /**
     * <b>Modificatore primitiva</b> per aggiungere un <i>record</i>
     * all'archivio; se è già presente una <i>entry</i> avente lo
     * stesso nome di <code>c</code> allora essa viene sovrascritta.
     * Questo evita di avere più elementi con lo stesso nome.
     *
     * @param   c   il <code>Contatto</code> da modificare/aggiungere.
     * @see     Contatto#nome()
     */
    public synchronized void modifica(Contatto c) {
        s.remove(c);
        s.add(c);
    }
    
    /**
     * <b>Predicato primitiva</b> per verificare l'appartenenza di
     * un certo nominativo entro l'archivio.
     *
     * @param    chi   il nome del <code>Contatto</code> da cercare.
     * @return   <code>true</code> se e solo se è presente un
     *           <i>record</i> avente <code>nome() == chi</code>;
     *           <code>false</code> altrimenti.
     */
    public synchronized boolean contiene(String chi) {
        return s.contains(new Contatto(chi, null, null));
    }
    
    /**
     * <b>Selettore primitiva</b> per ottenere un certo elemento
     * dall'archivio.
     *
     * @param    chi   il nome del <code>Contatto</code> da selezionare.
     * @return   <code>null</code> se e solo se non è presente un
     *           <i>record</i> avente <code>nome() == chi</code>;
     *           il <code>Contatto</code> richiesto altrimenti.
     */
    public synchronized Contatto seleziona(String chi) {
        return contiene(chi) ? (Contatto) s.tailSet(new Contatto(chi, null, null)).first()
        : null;
    }
    
    /**
     * <b>Modificatore primitiva</b> per eliminare un elemento.
     *
     * @param   chi   il nome del <code>Contatto</code> da cancellare.
     */
    public synchronized void cancella(String chi) {
        s.remove(new Contatto(chi, null, null));
    }
    
    /**
     * <b>Modificatore</b> per azzerare l'archivio; <b>primitiva</b>
     * solo per questione di comodità ed efficienza.
     */
    public synchronized void cancella() {
        s.clear();
    }
    
    /**
     * <b>Accesso</b> per ottenere il numero di elementi contenuti;
     * <b>primitiva</b> solo per questioni di comodità ed efficienza.
     *
     * @return   il numero di <i>record</i> presenti nell'archivio.
     */
    public synchronized int quanti() {
        return s.size();
    }
    
    /**
     * <b>Accesso primitiva</b> per ottenere tutti gli elementi.
     *
     * @return   tutti i <i>record</i> presenti nell'archivio;
     *           l'<i>array</i> restituito può avere lunghezza nulla
     *           qualora il <i>database</i> sia vuoto.
     */
    public synchronized Contatto[] tutti() {
        return (Contatto[]) s.toArray(new Contatto[s.size()]);
    }
    
    /**
     * <b>Convertitore</b> per ottenere la rappresentazione testuale
     * del contenuto dell'archivio, conforme alla grammatica definita
     * da <code>Parser</code> per esprimere un <code>Contatto</code>.
     * non è una <b>primitiva</b>.
     *
     * @return   una stringa rappresentante l'intero <i>database</i>.
     * @see      Parser
     * @see      Contatto
     */
    public synchronized String toString() {
        String     ret = "";
        Contatto[] v   = tutti();
        
        for (int i = 0; i < v.length; ++i)
            ret += v[i] + "\n";
        return ret;
    }
}




/**
 * Classe di utilità per ordinare gli elementi all'interno di un
 * <code>Database</code>; contiene solo un metodo che effettua
 * il confronto fra due oggetti di tipo <code>Contatto</code>,
 * ordinandoli in senso crescente sulla base del <code>nome()</code>.
 * <p>
 *
 * @author    <em>Marco Cimatti</em>
 * @version   1.0
 * @see       Database
 * @see       Contatto
 * @see       Contatto#nome()
 */
class ComparaContatto implements Comparator {
    
    /**
     * Realizza il confronto fra due <code>Contatto</code>.
     *
     * @return   un valore negativo, nullo o positivo a sconda che il
     *           <code>Contatto o1</code> sia minore, uguale o maggiore
     *           del <code>Contatto o2</code>, valutandolo sulla base
     *           dell'attributo <code>nome()</code>.
     * @see      Contatto
     * @see      Contatto#nome()
     */
    public int compare(Object o1, Object o2) {
        return ((Contatto) o1).nome().compareTo(((Contatto) o2).nome());
    }
}
