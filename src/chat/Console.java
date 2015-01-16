package chat;



import java.io.BufferedReader;

import java.io.File;

import java.io.FileWriter;

import java.io.InputStreamReader;

import java.io.IOException;

import java.util.Vector;



/**
 * Classe che rappresenta la finestra di <i>input</i> della <i>chat</i>;
 * possiede un unico metodo: il <code>main()</code>. Gli argomenti
 * della linea di comando vengono completamente ignorati.
 * <p>
 * L'applicazione elabora le linee testuali introdotte dallo <i>standard
 * input</i>, dopodiché invia quelle ritenute valide al file che viene
 * usato come <i>input</i> dalla classe <code>chat.Monitor</code>.
 * <p>
 * Architetturalmente costituisce il lato <i>client</i> del sistema
 * software complessivo.
 * <p>
 *
 * @author    <em>Marco Cimatti</em>
 * @version   1.0
 * @see       Monitor
 * @see       Monitor#FILE_SWAP
 */
public class Console {
    
    /**
     * L'<i>entry-point</i> del programma.
     *
     * @param   args   gli argomenti della linea di comando; vengono ignorati.
     */
    public static void main(String[] args) {
        final String HELP = "\n ? , help           - Visualizza questo help." +
        "\n *                  - Mostra l'archivio delle conoscenze." +
        "\n > , <              - Elenca gli interlocutori." +
        "\n >pippo , > pippo   - Cerca di contattare [pippo]." +
        "\n <pluto , < pluto   - Accetta di dialogare con [pluto]." +
        "\n .                  - Termina il dialogo in corso." +
        "\n bye,exit,quit,stop - Uscita.\n";
        
        BufferedReader stdin  = new BufferedReader(new InputStreamReader(System.in));
        String         prompt = "";
        do {
            System.out.print("Inserisci il nickname da usare: ");
            try {
                prompt = stdin.readLine().trim();
            } catch (IOException e) {}
        } while (prompt.length() == 0);
        
        File f = new File(Monitor.FILE_SWAP);
        if (f.exists() && !f.delete()) {
            System.out.println("Impossibile cancellare " + f.getName() + ".");
            System.exit(1);
        }
        FileWriter swap = null;
        Database   db   = null;
        try {
            swap = new FileWriter(f);
            swap.write(prompt + "\n");   // Scrittura del proprio nickname
            
            swap.flush();
            db = new Database(Monitor.FILE_DATABASE);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
        db.cancella(prompt);   // Eliminazione dello user dalla rubrica
        
        
        Vector cmd_uscita = new Vector();
        cmd_uscita.add("bye");
        cmd_uscita.add("exit");
        cmd_uscita.add("quit");
        cmd_uscita.add("stop");
        
        prompt = "[" + prompt + "]$ ";   // Rifinitura del prompt
        
        String cmd = "";
        do   // Main-loop
            
            try {
                System.out.print(prompt);
                if ((cmd = stdin.readLine().trim()).length() == 0)
                    continue;
                if (cmd.equals("?") || cmd.equals("help"))
                    System.out.println(HELP);
                else
                    if (cmd.equals("*")) {   // Visualizzazione del database
                        
                        Contatto[] vett = db.tutti();
                        for (int i = 0; i < vett.length; System.out.println(" " + vett[i++].nome())) ;
                    }
                    else {
                        if (cmd.startsWith(">") && cmd.length() > 1)
                            if (!db.contiene(cmd.substring(1).trim())) {
                                System.out.println("\"" + cmd.substring(1).trim() + "\" sconosciuto.");
                                continue;
                            }
                        swap.write(cmd + "\n");
                        swap.flush();
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }
        while (!cmd_uscita.contains(cmd));
        try {
            swap.close();
        } catch (IOException e) {}
    }
}
