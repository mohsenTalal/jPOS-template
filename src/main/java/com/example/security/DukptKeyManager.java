package com.example.security;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.security.*;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.NameRegistrar;

import java.io.Serializable;

public class DukptKeyManager implements TransactionParticipant, Configurable {

    private Configuration cfg;

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
    }

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg msg  = (ISOMsg) ctx.get("REQUEST");

        if (msg == null || !msg.hasField(52)) {
            // No PIN block — skip this participant (non-PIN transaction)
            return PREPARED;
        }

        try {
            // Correct way to get SMAdapter from NameRegistrar
            SMAdapter sm = BaseSMAdapter.getSMAdapter("default");

            byte[] pinBlock   = msg.getBytes(52);
            String pan        = msg.getString(2);

            // Build EncryptedPIN from field 52 (ISO FORMAT01 = ANSI PIN block)
            EncryptedPIN pinUnderKd1 = new EncryptedPIN(
                    pinBlock,
                    SMAdapter.FORMAT01,
                    pan
            );

            // Get the Zone PIN Key (ZPK) — loaded from cfg
            // In production this comes from your HSM key store
            SecureDESKey zpk = (SecureDESKey) ctx.get("ZPK");
            if (zpk == null) {
                ctx.put("RESULT", "ZPK_NOT_FOUND");
                return ABORTED;
            }

            // Translate PIN from terminal key (kd1=ZPK) to LMK for safe storage
            // Uses translatePIN(pinUnderKd1, kd1, kd2, destFormat)
            // kd2=null means re-encrypt under LMK
            EncryptedPIN pinUnderLMK = sm.translatePIN(
                    pinUnderKd1,
                    zpk,
                    null,              // null kd2 = store under LMK
                    SMAdapter.FORMAT01
            );

            ctx.put("PIN_UNDER_LMK", pinUnderLMK);
            return PREPARED;

        } catch (NameRegistrar.NotFoundException e) {
            ctx.put("RESULT", "HSM_NOT_FOUND");
            return ABORTED;
        } catch (SMException e) {
            ctx.put("RESULT", "PIN_TRANSLATE_FAILED: " + e.getMessage());
            return ABORTED;
        }
    }

    @Override
    public void commit(long id, Serializable context) { }

    @Override
    public void abort(long id, Serializable context) { }
}