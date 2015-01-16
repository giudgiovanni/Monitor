package chat;



import cryptix.provider.rsa.RawRSAPublicKey;

import java.io.ByteArrayInputStream;

import java.io.Reader;

import java.io.IOException;

import java.net.InetAddress;

import java.security.PublicKey;

import java.util.Vector;



/**
 * Il <i>parser</i> specifico per la seguente grammatica:
 * <p>
 * <i>scopo</i> ::= <i>nome</i> / <i>chiave</i> @ <i>endpoint</i><br>
 * <i>nome</i> ::= ( a |...| z | A |...| Z | 0 |...| 9 | _ | $ ) { a |...| z | A |...| Z | 0 |...| 9 | _ | $ }<sup>31</sup><br>
 * <i>chiave</i> ::= <i>numero</i> { , <i>numero</i> }<br>
 * <i>endpoint</i> ::= <i>ip</i> : <i>numero</i> { @ <i>ip</i> : <i>numero</i> }<br>
 * <i>ip</i> ::= <i>numero</i> . <i>numero</i> . <i>numero</i> . <i>numero</i><br>
 * <i>numero</i> ::= 0 [ x <i>cifra-hex</i> { <i>cifra-hex</i> } | { <i>cifra</i> } ] | <i>cifra-non-nulla</i> { <i>cifra</i> }<br>
 * <i>cifra</i> ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9<br>
 * <i>cifra-non-nulla</i> ::= 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9<br>
 * <i>cifra-hex</i> ::= 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | a | b | c | d | e | f | A | B | C | D | E | F
 * <p>
 * Sono riconosciuti i commenti in stile C e C++.
 * <p>
 *
 * @author    <em>Marco Cimatti</em>
 * @version   1.0
 */
class Parser {
    
    /**
     * Il lexer incapsulato, associato allo stream fisico.
     *
     * @see   Lexer
     */
    private Lexer lexer;
    
    
    /**
     * Unico costruttore.
     *
     * @param   stream   il <code>java.io.Reader</code> su cui far agire il lexer <code>lexer</code>.
     * @see     #lexer
     */
    Parser(Reader stream) {
        lexer = new Lexer(stream);
    }
    
    /**
     * <b>Procedura</b> associata alla regola di produzione "nome"
     * della grammatica.
     *
     * @exception   IOException        se generata dal <i>lexer</i>.
     * @exception   RuntimeException   in caso di errori sintattici.
     * @return      una stringa formata secondo la produzione "nome".
     */
    protected String nome() throws IOException {
        if (lexer.ttype == lexer.TT_WORD || lexer.ttype == lexer.TT_NUMBER) {
            String buf = lexer.sval;
            lexer.nextToken();
            return buf;
        }
        else
            throw new RuntimeException("Nome non valido alla linea " + lexer.lineno() + ".");
    }
    
    /**
     * <b>Procedura</b> associata alla regola di produzione "chiave"
     * della grammatica.
     *
     * @exception   IOException        se generata dal <i>lexer</i>.
     * @exception   RuntimeException   in caso di errori sintattici.
     * @return      la <code>cryptix.provider.rsa.RawRSAPublicKey</code>
     *              ottenuta mediante la sintassi espressa della
     *              riscrittura "chiave".
     */
    protected PublicKey chiave() throws IOException {
        Vector v = new Vector();
        
        while (true) {
            long l = numero();
            if ((l & 0xff) != l)
                throw new RuntimeException("Numero " + l + " non byte alla linea " + lexer.lineno() + ".");
            v.add(new Byte((byte) (l & 0xff)));
            if (lexer.ttype != ',')
                break;
            lexer.nextToken();
        }
        byte[] buf = new byte[v.size()];
        for (int i = 0; i < buf.length; ++i)
            buf[i] = ((Byte) v.get(i)).byteValue();
        return new RawRSAPublicKey(new ByteArrayInputStream(buf));
    }
    
    /**
     * <b>Procedura</b> associata alla regola di produzione "endpoint"
     * della grammatica.
     *
     * @exception   IOException        se generata dal <i>lexer</i>.
     * @exception   RuntimeException   in caso di errori sintattici.
     * @return      l'<code>Endpoint</code> secondo la sintassi della
     *              regola di riscrittura "endpoint" della grammatica.
     * @see         Endpoint
     */
    protected Endpoint endpoint() throws IOException {
        InetAddress IP = ip();
        if (lexer.ttype != ':')
            throw new RuntimeException("Atteso ':', trovato [" + tokenCorrente() + "] alla linea " + lexer.lineno() + ".");
        lexer.nextToken();
        long porta = numero();
        if (porta < 0 || 0xffff < porta)
            throw new RuntimeException("Numero di porta TCP/UDP " + porta + " illegale alla linea " + lexer.lineno() + ".");
        return new Endpoint(IP, (int) porta);
    }
    
    /**
     * <b>Procedura</b> associata alla regola di produzione "ip"
     * della grammatica.
     *
     * @exception   IOException        se generata dal <i>lexer</i> oppure
     *                                 se l'indirizzo Internet letto
     *                                 non rappresenta un IP valido.
     * @exception   RuntimeException   in caso di errori sintattici.
     * @return      il <code>java.net.InetAddress</code> in base alla
     *              produzione "ip" della grammatica.
     */
    protected InetAddress ip() throws IOException {
        String IP = "" + numero();
        for (int i = 1; i < 4; ++i) {
            if (lexer.ttype != '.')
                throw new RuntimeException("Atteso '.', trovato [" + tokenCorrente() + "] alla linea " + lexer.lineno() + ".");
            lexer.nextToken();
            IP += "." + numero();
        }
        return InetAddress.getByName(IP);   // IP illegale => IOException!
        
    }
    
    /**
     * <b>Procedura</b> associata alla regola di produzione "numero"
     * della grammatica.
     *
     * @exception   IOException        se generata dal <i>lexer</i>.
     * @exception   RuntimeException   in caso di errori sintattici.
     * @return      il <code>long</code> secondo la riscrittura "numero" della grammatica.
     */
    protected long numero() throws IOException {
        if (lexer.ttype != lexer.TT_NUMBER)
            throw new RuntimeException("Atteso un numero, trovato [" + tokenCorrente() + "] alla linea " + lexer.lineno() + ".");
        long l = (long) lexer.nval;
        lexer.nextToken();
        return l;
    }
    
    /**
     * <b>Procedura</b> per avanzare lungo il flusso di caratteri; da
     * invocare <u>OBBLIGATORIAMENTE</u> all'avvio per portare il
     * <i>lexer</i> interno sul primo <i>token</i> disponibile dallo
     * <i>stream</i> di lettura.
     *
     * @exception   IOException   se generata dal <i>lexer</i>.
     * @see         #lexer
     */
    void avanzaToken() throws IOException {
        lexer.nextToken();
    }
    
    /**
     * <b>Funzione</b> che restituisce il token disponibile
     * dal <code>Lexer lexer</code>, senza estrarne uno nuovo.
     *
     * @return   La stringa costituita dal token ultimo estratto.
     * @see      #lexer
     * @see      Lexer
     * @see      Lexer#tokenCorrente()
     */
    String tokenCorrente() {
        return lexer.tokenCorrente();
    }
    
    /**
     * <b>Procedura</b> che estrae la prima parola disponibile
     * dallo <i>stream</i> cui è associato il <code>Lexer lexer</code>.
     * Realizza lo scopo della grammatica:
     * <p>
     * <i>scopo</i> ::= <i>nome</i> / <i>chiave</i> @ <i>endpoint</i>
     *
     * @exception   IOException        se prodotta da <code>lexer</code>.
     * @exception   RuntimeException   in caso di errori sintattici o semantici.
     * @return      Il primo <code>Contatto</code> disponibile nel flusso
     *              di <code>char</code> dello <i>stream</i> di <code>lexer</code>.
     * @see         Contatto
     */
    Contatto prossimo() throws IOException {
        String nome = nome();
        if (lexer.ttype != '/')
            throw new RuntimeException("Atteso '/', trovato [" + tokenCorrente() + "] alla linea " + lexer.lineno() + ".");
        lexer.nextToken();
        PublicKey chiave = chiave();
        if (lexer.ttype != '@')
            throw new RuntimeException("Atteso '@', trovato [" + tokenCorrente() + "] alla linea " + lexer.lineno() + ".");
        lexer.nextToken();
        Contatto c = new Contatto(nome, chiave, endpoint());
        while (lexer.ttype == '@') {
            lexer.nextToken();
            c.aggiungi(endpoint());
        }
        return c;
    }
}