package io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean;

import io.everytrade.server.model.Currency;
import io.everytrade.server.model.TransactionType;
import io.everytrade.server.plugin.api.parser.ConversionStatistic;
import io.everytrade.server.plugin.api.parser.ImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.RowError;
import io.everytrade.server.plugin.impl.everytrade.parser.exception.ParsingProcessException;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.PaxfulBeanV1.UNSUPPORTED_QUOTE_CURRENCY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PaxfulBeanV1Test {
    private static final String HEADER_CORRECT = "type,fiat_currency,amount_fiat,amount_btc,rate,fee_fiat," +
        "fee_btc," +
        "market_rate_usd,payment_method,partner,status,completed_at,trade_hash,offer_hash\n";


    @Test
    void testWrongHeader() {
        final String headerWrong = "type,fiat_currency,amountXXX,amount_btc,rate,fee_fiat,fee_btc,market_rate_usd," +
            "payment_method,partner,status,completed_at,trade_hash,offer_hash\n";
        try {
            ParserTestUtils.testParsing(headerWrong);
            fail("No expected exception has been thrown.");
        } catch (ParsingProcessException e) {
        }
    }

    @Test
    void testCorrectParsingRawTransactionBuy() {
        final String row = "SELL,CAD,300.00,0.03544758,8463.20,3,0.00035447,5413.23,\"Interac e-Transfer\",tkemper," +
            "successful,2020-03-17T16:31:07-04:00,D91wM5jDkoQ,J8w1dk35wmp\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            "D91wM5jDkoQ",
            Instant.parse("2020-03-17T16:31:07Z"),
            Currency.BTC,
            Currency.CAD,
            TransactionType.BUY,
            new BigDecimal("0.03544758"),
            new BigDecimal("8463.2011550577"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionSell() {
        final String row = "BUY,CAD,500.00,0.07126192,7016.37,-,-,5127.41,\"Interac e-Transfer\",userae5d05ca6," +
            "successful," +
            "2020-03-16T16:51:28-04:00,JKo3yjVg21j,7Kx1GjKB7mj\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            "JKo3yjVg21j",
            Instant.parse("2020-03-16T16:51:28Z"),
            Currency.BTC,
            Currency.CAD,
            TransactionType.SELL,
            new BigDecimal("0.07126192"),
            new BigDecimal("7016.3700332520"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testQuoteIsNotFee() {
        final String row = "BUY,LTC,500.00,0.07126192,7016.37,-,-,5127.41,\"Interac e-Transfer\",userae5d05ca6," +
            "successful," +
            "2020-03-16T16:51:28-04:00,JKo3yjVg21j,7Kx1GjKB7mj\n";
        final RowError rowError = ParserTestUtils.getRowError(HEADER_CORRECT + row);
        assertNotNull(rowError);
        final String error = rowError.getMessage();
        assertTrue(error.contains(UNSUPPORTED_QUOTE_CURRENCY.concat("LTC")));
    }

    @Test
    void testIgnoredTransactionType() {
        final String row = "SOLD,CAD,300.00,0.03544758,8463.20,3,0.00035447,5413.23,\"Interac e-Transfer\",tkemper," +
            "successful,2020-03-17T16:31:07-04:00,D91wM5jDkoQ,J8w1dk35wmp\n";
        final ConversionStatistic conversionStatistic
            = ParserTestUtils.getConversionStatistic(HEADER_CORRECT + row);
        assertNotNull(conversionStatistic);
        assertEquals(1, conversionStatistic.getIgnoredRowsCount());
        assertTrue(
            conversionStatistic
                .getErrorRows()
                .get(0)
                .getMessage()
                .contains(ExchangeBean.UNSUPPORTED_TRANSACTION_TYPE.concat("SOLD"))
        );

    }

    @Test
    void testIgnoredStatus() {
        final String row = "SELL,CAD,300.00,0.03544758,8463.20,3,0.00035447,5413.23,\"Interac e-Transfer\",tkemper," +
            "UNsuccessful,2020-03-17T16:31:07-04:00,D91wM5jDkoQ,J8w1dk35wmp\n";
        final ConversionStatistic conversionStatistic
            = ParserTestUtils.getConversionStatistic(HEADER_CORRECT + row);
        assertNotNull(conversionStatistic);
        assertEquals(1, conversionStatistic.getIgnoredRowsCount());
        assertTrue(
            conversionStatistic
                .getErrorRows()
                .get(0)
                .getMessage()
                .contains(ExchangeBean.UNSUPPORTED_STATUS_TYPE.concat("UNsuccessful"))
        );
    }
}