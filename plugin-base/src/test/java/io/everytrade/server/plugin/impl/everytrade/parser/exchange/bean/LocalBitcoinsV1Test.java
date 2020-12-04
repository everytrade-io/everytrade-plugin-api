package io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean;

import io.everytrade.server.model.Currency;
import io.everytrade.server.model.TransactionType;
import io.everytrade.server.plugin.api.parser.ImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.RowError;
import io.everytrade.server.plugin.impl.everytrade.parser.exception.ParsingProcessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean.UNSUPPORTED_TRANSACTION_TYPE;
import static io.everytrade.server.plugin.impl.everytrade
    .parser.exchange.bean.LocalBitcoinsBeanV1.UNSUPPORTED_QOUTE_BTC;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class LocalBitcoinsV1Test {
    static final String HEADER_CORRECT = "id,created_at,buyer,seller,trade_type,btc_amount,btc_traded," +
        "fee_btc,btc_amount_less_fee,btc_final,fiat_amount,fiat_fee,fiat_per_btc,currency,exchange_rate," +
        "transaction_released_at,online_provider,reference\n";

    @Test
    void testWrongHeader() {
        final String headerWrong = "id,created_at,buyer,seller,trade_type,btc_amount,btc_traded,fee_btc,"
            + "btc_amount_less_fee,btX_final,fiat_amount,fiat_fee,fiat_per_btc,currency,exchange_rate,"
            + "transaction_released_at,online_provider,reference\n";
        try {
            ParserTestUtils.testParsing(headerWrong);
            fail("No expected exception has been thrown.");
        } catch (ParsingProcessException e) {
        }
    }

    @Test
    void testCorrectParsingRawTransactionBuy() {
        final String row = "57963022,2020-03-17 16:57:32+00:00,Frankmaione,cnjoku,ONLINE_BUY,0.6747611,0.6747611," +
            "0.00674761,0.66801349,0.66801349,5000,50,7484.88,CAD,7410.03,2020-03-17 17:39:50+00:00," +
            "INTERAC,L57963022BYICJY\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            "57963022",
            Instant.parse("2020-03-17T17:39:50Z"),
            Currency.BTC,
            Currency.CAD,
            TransactionType.BUY,
            new BigDecimal("0.66801349"),
            new BigDecimal("7484.8787859060"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionBuySwitchToSell() {
        final String row = "57963022,2020-03-17 16:57:32+00:00,Frankmaione,cnjoku,ONLINE_BUY,0.6747611,0.6747611,0" +
            ".00674761,0.66801349,0.66801349,5000,50,7484.88,XRP,7410.03,2020-03-17 17:39:50+00:00,INTERAC," +
            "L57963022BYICJY\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            "57963022",
            Instant.parse("2020-03-17T17:39:50Z"),
            Currency.XRP,
            Currency.BTC,
            TransactionType.SELL,
            new BigDecimal("5000"),
            new BigDecimal("0.0001336027"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionSell() {
        final String row = "57965754,2020-03-17 18:02:46+00:00,thesimpleman243,Frankmaione,ONLINE_SELL,0.01515238," +
            "0.01500236,0.00015002,0.01500236,0.01515238,125,1.24,8332.02,CAD,8332.02,2020-03-17 18:05:15+00:00," +
            "INTERAC,L57965754BYIENU\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            "57965754",
            Instant.parse("2020-03-17T18:05:15Z"),
            Currency.BTC,
            Currency.CAD,
            TransactionType.SELL,
            new BigDecimal("0.01515238"),
            new BigDecimal("8249.5291168780"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void unknownTransactionType() {
        final String row = "57965754,2020-03-17 18:02:46+00:00,thesimpleman243,Frankmaione,OFFLINE_SELL,0.01515238," +
            "0.01500236,0.00015002,0.01500236,0.01515238,125,1.24,8332.02,CAD,8332.02,2020-03-17 18:05:15+00:00," +
            "INTERAC,L57965754BYIENU\n";
        final RowError rowError = ParserTestUtils.getRowError(HEADER_CORRECT + row);
        assertNotNull(rowError);
        final String error = rowError.getMessage();
        assertTrue(error.contains(UNSUPPORTED_TRANSACTION_TYPE.concat("OFFLINE_SELL")));
    }

    @Test
    void unsupportedTransactionQuote() {
        final String row = "57965754,2020-03-17 18:02:46+00:00,thesimpleman243,Frankmaione,ONLINE_SELL,0.01515238,0" +
            ".01500236,0" +
            ".00015002,0.01500236,0.01515238,125,1.24,8332.02,BTC,8332.02,2020-03-17 18:05:15+00:00,INTERAC," +
            "L57965754BYIENU\n";
        final RowError rowError = ParserTestUtils.getRowError(HEADER_CORRECT + row);
        assertNotNull(rowError);
        final String error = rowError.getMessage();
        assertTrue(error.contains(UNSUPPORTED_QOUTE_BTC));
    }
}