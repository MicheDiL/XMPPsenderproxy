package com.arbitcds.sender.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Libreria complete per generare e spedire una transazione VOID
import com.arbitsecurity.atg.libjvoidproto.MetadataInStreamWrapper; //import com.arbitcds.sender.MetadataInStreamWrapper;
import com.arbitsecurity.atg.libjvoidproto.TransferResult;          //import com.arbitcds.sender.TransferResult;
//import com.arbitsecurity.atg.libjvoidproto.VoidTrans;               //import com.arbitcds.sender.VoidTrans;
import com.arbitsecurity.atg.libjvoidproto.VoidTrans;
// Nuove librerie per firmare la transazione VOID con chiave GPG v4 (OpenPGP)
import com.arbitsecurity.atg.libjvoidproto.ArbitSignVoid;
import com.arbitsecurity.atg.libjvoidproto.ISignCallback;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class Main {

    private static final Log LOGGER = LogFactory.getLog(Main.class);

    public static void main(String[] args) {

        FileInputStream secretKeyStream = null;
        FileInputStream certificateStream = null;
        char[] password = null;

        if (args.length != 4) {
            usage();
            System.exit(1);
        }
        String metadataFileName = args[0];
        String fileName = args[1];
        String host = args[2];
        int port = Integer.parseInt(args[3]);

        System.out.println("Metadata FileName: " + metadataFileName + ", filename: " + fileName + ", host: " + host + ", port: " + port);
        
        VoidTrans vt = new VoidTrans(host, port, true);
        vt.setPassphrase(null);

        try {

            secretKeyStream = new FileInputStream("./keys/adg@GWHtoL.secret");
            certificateStream = new FileInputStream("./keys/adg@GWHtoL.certificate");
            password = "738p0v6iqg0vcr0mb0ubc328ff".toCharArray();

            ISignCallback signCallback = new ArbitSignVoid(certificateStream, secretKeyStream, password);
        
            FileInputStream finpstr = new FileInputStream(fileName);
            byte[] metadata = Files.readAllBytes(Paths.get(metadataFileName));

            TransferResult tr = vt.transfer(new MetadataInStreamWrapper(0xbabe, metadata, finpstr), 0, 0, signCallback);

            System.out.println("Return code: " + tr.rc + " Return Message: " + tr.reason);

        } catch (Exception e) { // <- Cattura qualsiasi Exception (incluse le sottoclassi come IOException)
            
            System.err.println("[Main] Errore: " + e.getMessage());
            e.printStackTrace(); // stampa del dettaglio tecnico dell'errore
            System.exit(1);

        } finally {
            // TODO Auto-generated catch block
            if (secretKeyStream != null) {
                try {
                    secretKeyStream.close();
                } catch (IOException e) {
                    System.err.println("[Main] Errore chiusura secretKeyStream: " + e.getMessage());
                    LOGGER.error("Error closing secretKeyStream", e);
                }
            }

            if (certificateStream != null) {
                try {
                    certificateStream.close();
                } catch (IOException e) {
                    System.err.println("[Main] Errore chiusura certificateStream: " + e.getMessage());
                    LOGGER.error("Error closing certificateStream", e);
                }
            }
        }
    }

    private static void usage() {
        System.out.println("Usage: testtool <metadatafilename> <filename> <host> <port>");
    }
}