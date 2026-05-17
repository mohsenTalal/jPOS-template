package com.example;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.ISO87APackager;

public class JposIsoClient {
    public static void main(String[] args) throws Exception {
        ISO87APackager packager = new ISO87APackager();

        ASCIIChannel channel = new ASCIIChannel("localhost", 8000, packager);
        channel.connect();

        ISOMsg request = new ISOMsg();
        request.setMTI("0200");
        request.set(2, "4111111111111111");   // PAN
        request.set(3, "000000");             // Purchase
        request.set(4, "000000020000");       // Amount: 100.00
        request.set(7, "0517115500");         // Transmission datetime
        request.set(11, "123456");            // STAN
        request.set(22, "051");               // POS Entry Mode
        request.set(37, "123456789012");      // RRN
        request.set(41, "TERM001");           // Terminal ID
        request.set(42, "MERCHANT000001");    // Merchant ID
        request.set(49, "682");               // SAR


        System.out.println("*Mohsen* Sending ISO 8583 request:");
        request.dump(System.out, "");

        channel.send(request);

        ISOMsg response = channel.receive();

        System.out.println("Received ISO 8583 response:");
        response.dump(System.out, "");

        channel.disconnect();
    }
}