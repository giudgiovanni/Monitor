package chat;



import java.io.Serializable;

import java.security.Key;

import java.util.Vector;



/**
 * Classe atta a rappresentare un individuo, intendendo questi come
 * l'insieme dei seguenti attributi:
 * <ul>
 *   <li>un nome testuale, composto di un numero variabile da 1 a 32
 *       caratteri, ciascuno dei quali soddisfa il predicato di
 *       classe <code>carLecitoNome(char c)</code>;</li>
 *   <li>una chiave crittografica <code>java.security.Key</code>;</li>
 *   <li>una lista contenente uno o più <code>Endpoint</code>.</li>
 * </ul>
 * <p>
 *
 * @author    <em>Marco Cimatti</em>
 * @version   1.0
 * @see       Endpoint
 */
public class Contatto implements Serializable {
    
    /** Il <i>nickname</i> dell'individuo. */
    private String nome;
    
    /** La chiave crittografica. */
    private Key chiave;
    
    /* La lista degli <code>Endpoint</code> ove può trovarsi la persona. */
    private Vector dove;
    
    
    /**
     * Unico costruttore. Verifica la validità del <code>nome</code>
     * testandone la lunghezza (che deve appartenere all'intervallo [1..32])
     * ed il tipo dei caratteri, assicurandosi che ciascuno di essi
     * soddisfi il predicato di classe <code>carLecitoNome(char)</code>.
     *
     * @param       nome     il nome del <code>Contatto</code>.
     * @param       chiave   la chiave crittografica.
     * @param       dove     un <code>Endpoint</code> possibile per la persona rappresentata.
     * @exception   RuntimeException   se <code>nome</code> non è valido.
     * @see         #carLecitoNome(char)
     * @see         Endpoint
     */
    public Contatto(String nome, Key chiave, Endpoint dove) {
        if (nome.length() == 0 || nome.length() > 32)
            throw new RuntimeException("Lunghezza del nome illegale: " + nome.length());
        for (int i = 0; i < nome.length(); ++i)
            if (! carLecitoNome(nome.charAt(i)))
                throw new RuntimeException("Carattere '" + nome.charAt(i) + "' proibito per Contatto.");
        this.nome   = nome;
        this.chiave = chiave;
        this.dove   = new Vector();
        this.dove.add(dove);
    }
    
    /**
     * Verifica che il carattere <code>c</code> possa appartenere al
     * campo <code>nome</code> di un'istanza di <code>Contatto</code>.
     *
     * @param    c   il carattere da testare.
     * @return   <code>true</code> &lt;=&gt; <code>Character.isJavaIdentifierPart(c)</code>.
     */
    public static boolean carLecitoNome(char c) {
        return Character.isJavaIdentifierPart(c);
    }
    
    /**
     * Aggiunge un <code>Endpoint</code> nella lista di quelli ammessi
     * per l'istanza in questione. Non compie nulla se il parametro
     * indicato è già presente nella lista; questo evita duplicazioni
     * inutili. <b>Primitiva</b>.
     *
     * @param   e   l'<code>Endpoint</code> da aggiungere alla lista.
     * @see     #dove
     * @see     Endpoint
     */
    public void aggiungi(Endpoint e) {
        if (!dove.contains(e))
            dove.add(e);
    }
    
    /**
     * <b>Selettore</b> per ottenere il nome dell'istanza. <b>Primitiva</b>.
     *
     * @return   il valore della variabile privata <code>nome</code>.
     * @see      #nome
     */
    public String nome() {
        return nome;
    }
    
    /**
     * <b>Selettore</b> per accedere alla chiave crittografica. <b>Primitiva</b>.
     *
     * @return   il valore della variabile privata <code>chiave</code>.
     * @see      #chiave
     */
    public Key chiave() {
        return chiave;
    }
    
    /**
     * <b>Selettore</b> per ottenere la lista degli <code>Endpoint</code>
     * associati all'istanza. <b>Primitiva</b>.
     *
     * @return   un clone della variabile privata <code>dove</code>,
     *           per evitare che il chiamante possa modificarla a piacere.
     * @see      #dove
     */
    public Vector dove() {
        return (Vector) dove.clone();
    }
    
    /**
     * <b>Convertitore</b> per avere la rappresentazione testuale di
     * questa istanza. La <code>String</code> restituita è conforme
     * alla grammatica sulla quale sono improntati <code>Lexer</code>
     * e <code>Parser</code>. <b>Primitiva</b> per comodità, non è
     * obbligatorio realizzare questo metodo come tale.
     *
     * @return   un stringa rappresentante l'istanza di <code>Contatto</code>.
     * @see      Lexer
     * @see      Parser
     */
    public String toString() {
        String temp = nome + "\n/" + Utili.hexByteArrayToString(chiave.getEncoded());
        for (int i = 0; i < dove.size(); ++i)
            temp += "\n@" + (Endpoint) dove.get(i);
        return temp;
    }
}




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


