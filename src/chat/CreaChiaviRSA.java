package chat;



import cryptix.provider.Cryptix;

import java.io.FileOutputStream;

import java.security.KeyPairGenerator;

import java.security.KeyPair;

import java.security.NoSuchAlgorithmException;

import java.security.SecureRandom;

import java.security.Security;



/**
 * Classe di utilità per creare le chiavi relative all'algoritmo RSA.
 * Redirigendo lo <i>standard output</i> su file è possibile salvare
 * in un vettore <code>byte[]</code> di Java le chiavi generate.
 * Vengono creati inoltre due file "Pubblica.bin" e "Segreta.bin" i
 * quali contengono, rispettivamente, la chiave pubblica e la chiave
 * privata appena generate.
 * Per riottenere le chiavi partendo dal vettore di byte è sufficiente:
 * <p>
 * <code><b>new</b> cryptix.provider.rsa.RawRSAPublicKey(<b>new</b> java.io.ByteArrayInputStream(<i>public_byte_array</i>));</code>
 * <p>
 * oppure:
 * <p>
 * <code><b>new</b> cryptix.provider.rsa.RawRSAPrivateKey(<b>new</b> java.io.ByteArrayInputStream(<i>private_byte_array</i>));</code>
 * <p>
 * Analoga cosa si può fare a partire dai file su disco, incapsulandoli
 * entro un <code>java.io.FileInputStream</code> piuttosto che in un
 * <code>java.io.ByteArrayInputStream</code>.
 * <p>
 *
 * @author    <em>Marco Cimatti</em>
 * @version   1.0
 */
public class CreaChiaviRSA {
    
    /**
     * L'<i>entry point</i> del programma.
     * Il primo parametro sulla linea di comando indica la lunghezza in
     * bit delle chiavi da generare. Ad esempio, per generare chiavi RSA
     * lunghe 512 bit:
     * <p>
     * <code><i>java</i> chat.CreaChiaviRSA 512</code>
     * <p>
     *
     * @param   args   gli argomenti della linea di comando.
     */
    public static void main(String[] args) {
        if (args.length != 1)
            System.out.println("Uso:   <java> CreaChiaviRSA [lunghezza delle chiavi (in bit)]");
        else
            try {
                Security.addProvider(new Cryptix());
                KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
                gen.initialize(Integer.parseInt(args[0]), new SecureRandom());
                Cronometro c = new Cronometro();
                c.avanza();
                KeyPair k = gen.generateKeyPair();
                c.ferma();
                System.out.println("Coppia di chiavi generata in " + c + " millisecondi.\n");
                System.out.println("\nChiave pubblica = { "
                                   + Utili.hexByteArrayToString(k.getPublic().getEncoded())
                                   + " }   // " + k.getPublic().getEncoded().length + " byte\n");
                System.out.println("\nChiave privata  = { "
                                   + Utili.hexByteArrayToString(k.getPrivate().getEncoded())
                                   + " }   // " + k.getPrivate().getEncoded().length + " byte");
                new FileOutputStream("Pubblica.bin").write(k.getPublic().getEncoded());
                new FileOutputStream("Segreta.bin").write(k.getPrivate().getEncoded());
            } catch (Exception e) { e.printStackTrace(); }
    }
}