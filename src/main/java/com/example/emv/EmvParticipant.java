package com.example.emv;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.security.*;
import org.jpos.security.jceadapter.JCESecurityModule;
import org.jpos.tlv.TLVList;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.NameRegistrar;

import java.io.Serializable;

public class EmvParticipant implements TransactionParticipant, Configurable {

    private Configuration cfg;

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg  = (ISOMsg) ctx.get("REQUEST");

        // Field 55 is EMV data — skip if not present
        if (msg == null || !msg.hasField(55)) {
            return PREPARED;
        }

        try {
            // --- Step 1: Parse EMV TLV data from field 55 ---
            byte[] emvData = msg.getBytes(55);

            // TLVList is the correct jPOS class for parsing EMV TLV data
            TLVList tlvList = new TLVList();
            tlvList.unpack(emvData);   // single-arg unpack — no offset

            // Extract EMV tags using TLVList.getString(tag) or .find(tag).getValue()
            byte[] arqc   = tlvList.find(0x9F26) != null
                    ? tlvList.find(0x9F26).getValue() : null;
            byte[] atc    = tlvList.find(0x9F36) != null
                    ? tlvList.find(0x9F36).getValue() : null;
            byte[] tvr    = tlvList.find(0x95)   != null
                    ? tlvList.find(0x95).getValue()   : null;
            byte[] unpNum = tlvList.find(0x9F37) != null
                    ? tlvList.find(0x9F37).getValue() : null;

            if (arqc == null || atc == null) {
                ctx.put("RESULT", "EMV_MISSING_TAGS");
                return ABORTED;
            }

            // --- Step 2: Get ICC Master Key from context ---
            // This was loaded by a prior participant (key derivation step)
            SecureDESKey imkac = (SecureDESKey) ctx.get("IMK_AC");
            if (imkac == null) {
                ctx.put("RESULT", "IMK_AC_NOT_FOUND");
                return ABORTED;
            }

            String pan       = msg.getString(2);
            String panSeqNo  = msg.hasField(23) ? msg.getString(23) : "00";

            // --- Step 3: Verify ARQC using correct SMAdapter signature ---
            // verifyARQC(mkdm, skdm, imkac, accountNo, acctSeqNo,
            //            atc, arqc, data, tvr)
            JCESecurityModule sm =
                    (JCESecurityModule) NameRegistrar.get("s-m-adapter.default");

            boolean valid = sm.verifyARQC(
                    MKDMethod.OPTION_A,      // ICC master key derivation method
                    SKDMethod.EMV_CSKD,      // Session key derivation method
                    imkac,                   // ICC Master Key for AC
                    pan,                     // PAN (field 2)
                    panSeqNo,                // PAN sequence number (field 23)
                    atc,                     // Application Transaction Counter
                    arqc,                    // Cryptogram to verify
                    buildAuthData(msg),      // Data used to compute the ARQC
                    tvr                      // Terminal Verification Results
            );

            if (!valid) {
                ctx.put("RESULT", "ARQC_INVALID");
                return ABORTED;
            }

            // --- Step 4: Generate ARPC (issuer response back to card) ---
            // generateARPC(mkdm, skdm, imkac, pan, panSeqNo,
            //              atc, arqc, data, arpcMethod, arc, propAuthData)
            byte[] arc = new byte[]{0x00, 0x00}; // 0x0000 = approve
            byte[] arpc = sm.generateARPC(
                    MKDMethod.OPTION_A,
                    SKDMethod.EMV_CSKD,
                    imkac,
                    pan,
                    panSeqNo,
                    atc,
                    arqc,
                    buildAuthData(msg),
                    ARPCMethod.METHOD_1,
                    arc,
                    null          // no proprietary auth data
            );

            ctx.put("ARPC",  arpc);
            ctx.put("RESULT", "EMV_OK");
            return PREPARED;

        } catch (NameRegistrar.NotFoundException e) {
            ctx.put("RESULT", "HSM_NOT_FOUND");
            return ABORTED;
        } catch (SMException e) {
            ctx.put("RESULT", "EMV_ERROR: " + e.getMessage());
            return ABORTED;
        } catch (Exception e) {
            ctx.put("RESULT", "EMV_PARSE_ERROR: " + e.getMessage());
            return ABORTED;
        }
    }

    /**
     * Builds the 29-byte data string used in ARQC computation.
     * Concatenates: amount(6) + other amount(6) + terminal country(2)
     *             + TVR(5) + currency(2) + date(3) + type(1) + unpredictable(4)
     */
    private byte[] buildAuthData(ISOMsg msg) throws Exception {
        // In a real implementation concatenate the exact fields
        // the terminal used to compute the ARQC (per EMV Book 2).
        // For sandbox testing, return a zeroed block.
        return new byte[29];
    }

    @Override
    public void commit(long id, Serializable context) { }

    @Override
    public void abort(long id, Serializable context) { }
}