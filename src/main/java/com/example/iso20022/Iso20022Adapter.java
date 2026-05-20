package com.example.iso20022;

import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Iso20022Adapter implements TransactionParticipant {

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx  = (Context) context;
        ISOMsg msg   = (ISOMsg) ctx.get("REQUEST");

        // Build pacs.008 XML (Credit Transfer for Visa settlement)
        String pacs008 = """
            <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.09">
              <FIToFICstmrCdtTrf>
                <GrpHdr>
                  <MsgId>%s</MsgId>
                  <CreDtTm>%s</CreDtTm>
                  <NbOfTxs>1</NbOfTxs>
                  <SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>
                </GrpHdr>
                <CdtTrfTxInf>
                  <Amt><InstdAmt Ccy="SAR">%s</InstdAmt></Amt>
                  <Dbtr><Nm>%s</Nm></Dbtr>
                </CdtTrfTxInf>
              </FIToFICstmrCdtTrf>
            </Document>
            """.formatted(
                msg.getString(37),   // retrieval ref number as msg ID
                LocalDateTime.now(),
                new BigDecimal(msg.getString(4)).movePointLeft(2),
                msg.getString(43)    // card acceptor name
        );

        ctx.put("ISO20022_MSG", pacs008);
        return PREPARED;
    }
}
