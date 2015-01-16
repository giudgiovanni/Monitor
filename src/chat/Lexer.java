package chat;



import java.io.IOException;

import java.io.Reader;

import java.io.StreamTokenizer;



/**
 * Implementazione del <i>lexer</i> specifico per la seguente grammatica:
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
class Lexer extends StreamTokenizer {
    
    /**
     * Unico costruttore che inizializza il <i>lexer</i> con i
     * caratteri separatori specificati dalla grammatica.
     *
     * @param   r   il <code>java.io.Reader</code> su cui
     *              poggiare questa specializzazione di
     *              <code>java.io.StreamTokenizer</code>.
     */
    Lexer(Reader r) {
        super(r);
        resetSyntax();
        wordChars('a', 'z');
        wordChars('A', 'Z');
        wordChars('0', '9');
        wordChars('_', '_');
        wordChars('$', '$');
        commentChar(';');                   // Carattere di inizio dei commenti sino a fine linea
        
        slashStarComments(true);            // Riconosciuti i commenti con sintassi C...
        
        slashSlashComments(true);           // ...e C++
        
        whitespaceChars('\t', '\t');        // I separatori fra le parole
        
        whitespaceChars('\r', '\r');
        whitespaceChars(' ', ' ');
        eolIsSignificant(false);            // 'End Of Line' come un blank
        
        ordinaryChar('/');                  // 4 tipi di token specifici
        
        ordinaryChar(',');
        ordinaryChar('@');
        ordinaryChar(':');
        ordinaryChar('.');
    }
    
    /**
     * Specializzazione di <code>java.io.StreamTokenizer.nextToken()</code>
     * per il corretto parsing dei numeri (naturali in notazione decimale ed
     * esadecimale Java-<i>like</i>, prefissati cioè da <code>0x</code>).
     *
     * @return      Il valore di <code>java.io.StreamTokenizer.ttype</code>, cioè il tipo del token letto.
     * @exception   java.io.IOException   se generata da <code>java.io.StreamTokenizer.nextToken()</code>.
     */
    public int nextToken() throws IOException {
        if (super.nextToken() == TT_WORD)
            try {
                nval  = sval.startsWith("0x") ? Long.parseLong(sval.substring(2), 16) : Long.parseLong(sval);
                ttype = TT_NUMBER;
            } catch (NumberFormatException e) {}
        return ttype;
    }
    
    /**
     * <b>Funzione</b> che ritorna una rappresentazione testuale
     * del <i>token</i> corrente ultimo estratto dallo <i>stream</i>.
     *
     * @return   La versione stringa del <i>token</i> in esame;
     *           <code>""</code> in caso di <i>End Of File</i>.
     */
    String tokenCorrente() {
        switch (ttype) {
            case TT_NUMBER: return "" + (long) nval;
            case TT_WORD:   return sval;
            case TT_EOL:    return "<EOL>";
            case TT_EOF:    return "";
            default:        return "" + (char) ttype;
        }
        // Qui non si arriva mai...
        
    }
}

