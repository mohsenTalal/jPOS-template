package com.example.api;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;

public class ResponseBuilderParticipant implements TransactionParticipant {

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;

        try {
            ISOMsg req = (ISOMsg) ctx.get("REQUEST");
            ISOSource source = (ISOSource) ctx.get("DESTINATION");

            if (req == null) {
                System.out.println("[ResponseBuilder] REQUEST is null");
                return ABORTED;
            }

            if (source == null) {
                System.out.println("[ResponseBuilder] DESTINATION is null");
                return ABORTED;
            }

            ISOMsg response = (ISOMsg) req.clone();
            response.setResponseMTI(); // 0200 -> 0210

            String rc = ctx.getString("RESPONSE_CODE");
            if (rc == null || rc.isBlank()) {
                rc = "00";
            }

            response.set(39, rc);

            if ("00".equals(rc)) {
                String transId = ctx.getString("CS_TRANS_ID");
                if (transId != null && transId.length() >= 6) {
                    response.set(38, transId.substring(0, 6));
                } else {
                    response.set(38, "123456");
                }
            }

            ctx.put("RESPONSE", response);

            System.out.println("[ResponseBuilder] Sending ISO response:");
            response.dump(System.out, "");

            source.send(response);

            return PREPARED | NO_JOIN | READONLY;

        } catch (Exception e) {
            e.printStackTrace();
            return ABORTED;
        }
    }

    @Override
    public void commit(long id, Serializable context) {
    }

    @Override
    public void abort(long id, Serializable context) {
    }
}