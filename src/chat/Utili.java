package chat;



import java.net.InetAddress;

import java.net.UnknownHostException;



/**
 * Classe contenente <u>solo ed esclusivamente</u> metodi di tipo
 * <code>public static</code> che realizzano funzioni di utilità.
 * <p>
 *
 * @author    <em>Alessandro Gaspari</em>
 * @version   1.0
 */
public class Utili {
    
    /**
     * <b>Funzione</b> per ottenere la rappresentazione esadecimale di un byte.
     *
     * @param    b   il byte da convertire.
     * @return   la stringa esadecimale per <code>b</code>, composta <u>sempre da due caratteri</u>.
     */
    public static String hexByte(byte b) {
        String hex = Long.toHexString(b & 0xff);
        return hex.length() < 2 ? "0" + hex : hex;
    }
    
    /**
     * <b>Funzione</b> per convertire in stringa il vettore di
     * <code>byte</code> ricevuto come parametro, adottando
     * una sintassi esadecimale Java-<i>like</i> per esprimerlo.
     * <p>
     * Ad esempio, il vettore <code>byte[] v = { 10, 20, 30 };</code>
     * produce la stringa <code>"0x0a,0x14,0x1e"</code>.
     *
     * @param   v   il <code>byte[]</code> da rappresentare.
     * @return  la stringa associata all'<i>array</i> in esadecimale;
     *          <code>""</code> per vettori aventi lunghezza nulla.
     * @see     #hexByte(byte)
     */
    public static String hexByteArrayToString(byte[] v) {
        String ret = "";
        for (int i = 0; i < v.length; ++i)
            ret += i < v.length-1 ? "0x" + hexByte(v[i])+"," : "0x" + hexByte(v[i]);
        return ret;
    }
    
    /**
     * <b>Procedura</b> che serializza un <code>int</code> in una serie
     * di quattro byte contigui entro un <code>byte[]</code>. Adotta
     * un ordinamento <i>big-endian</i>: il byte più significativo alla
     * posizione con indice più basso nell'<i>array</i>, quindi gli
     * altri a seguire.
     *
     * @param   i       l'intero da serializzare.
     * @param   buf     il vettore ove scrivere i quattro byte.
     * @param   offset  il primo indice da occupare entro <code>buf</code>;
     *                  verranno sovrascritti <code>buf[offset], ..., buf[offset+3]</code>.
     * @see     #intFromArray(byte[], int)
     */
    public static void intToArray(int i, byte[] buf, int offset) {
        buf[offset  ] = (byte)((i >> 24) & 0xFF);
        buf[offset+1] = (byte)((i >> 16) & 0xFF);
        buf[offset+2] = (byte)((i >>  8) & 0xFF);
        buf[offset+3] = (byte)(i & 0xFF);
    }
    
    /**
     * <b>Funzione</b> che ottiene un <code>int</code> partendo dalla
     * sua versione serializzata di quattro byte. Presuppone un
     * ordinamento <i>big-endian</i> entro l'<i>array</i> di lettura.
     *
     * @param    buf     il vettore da cui leggere quattro byte.
     * @param    offset  il primo indice da utilizzare entro <code>buf</code>;
     *                   verranno letti <code>buf[offset], ..., buf[offset+3]</code>.
     * @return   l'intero ottenuto come OR dei quattro byte letti opportunamente <i>shiftati</i>.
     * @see      #intToArray(int, byte[], int)
     */
    public static int intFromArray(byte[] buf, int offset) {
        return ((buf[offset  ] & 0xFF) << 24) |
        ((buf[offset+1] & 0xFF) << 16) |
        ((buf[offset+2] & 0xFF) <<  8) |
        ((buf[offset+3] & 0xFF));
    }
    
    /**
     * <b>Funzione</b> che calcola l'indirizzo IP di tutti gli <i>host</i>
     * appartenenti ad una rete. L'operazione viene effettuata sulla
     * base della classe dell'IP specificato; indirizzi di classi D
     * oppure E vengono riportati in uscita immutatti.
     * <p>
     * Setta ad 1 tutti i bit relativi all'<i>host-id</i> dell'indirizzo
     * IP ricevuto come parametro, lasciando immutati quelli che
     * costituiscono la parte di <i>net-id</i>.
     *
     * @param    IP   l'indirizzo IP di cui calcolare il <i>directed broadcast</i>.
     * @return   l'IP associato a tutti gli <i>host</i> della rete
     *           rappresentata da <code>IP</code>; <code>null</code> in
     *           caso di errore (molto raro...).
     */
    public static InetAddress directedBroadcast(InetAddress IP) {
        byte[] v = IP.getAddress();
        switch (v[0] & 0xe0) {
            case 0xe0: return IP;                         // IP di classe D o E
                
            case 0xc0: v[3] = (byte)0xff;                 // IP di classe C
                
                break;
            case 0xa0:
            case 0x80: v[2] = v[3] = (byte)0xff;          // IP di classe B
                
                break;
            default:   v[1] = v[2] = v[3] = (byte)0xff;   // IP di classe A
                
        }
        try {
            return InetAddress.getByName((v[0] & 0xff) + "." +
                                         (v[1] & 0xff) + "." +
                                         (v[2] & 0xff) + "." +
                                         (v[3] & 0xff));
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
