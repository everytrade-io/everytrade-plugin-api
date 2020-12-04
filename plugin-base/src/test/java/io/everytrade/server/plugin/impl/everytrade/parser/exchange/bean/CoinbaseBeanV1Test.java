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

import static io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean.UNSUPPORTED_CURRENCY_PAIR;
import static io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.CoinbaseBeanV1.BASE_DIFFERS_FROM_UNIT_SIZE;
import static io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.CoinbaseBeanV1.QUOTE_DIFFERS_FROM_PRICE_FEE_TOTAL_UNIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CoinbaseBeanV1Test {
    private static final String HEADER_CORRECT = "portfolio,trade id,product,side,created at,size,size unit,price," +
        "fee,total,price/fee/total unit\n";

    @Test
    void testCorrectHeader() {
        try {
            ParserTestUtils.testParsing(HEADER_CORRECT);
        } catch (ParsingProcessException e) {
            fail("Unexpected exception has been thrown.");
        }
    }
    @Test
    void testWrongHeader() {
        final String headerWrong = "portfolio,tradeX,product,side,created at,size,size unit,price," +
            "fee,total,price/fee/total unit\n";
        try {
            ParserTestUtils.testParsing(headerWrong);
            fail("No expected exception has been thrown.");
        } catch (ParsingProcessException e) {
        }
    }

    @Test
    void testCorrectParsingRawTransactionBuy() {
        final String row = "default,6101747,LTC-EUR,BUY,2020-05-18T20:08:39.184Z,2.96875093,LTC,41.45,0.6152736302425,-123" +
            ".6699996787425,EUR\n";
        final ImportedTransactionBean txBeanParsed  = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            "6101747",
            Instant.parse("2020-05-18T20:08:39.184Z"),
            Currency.LTC,
            Currency.EUR,
            TransactionType.BUY,
            new BigDecimal("2.96875093"),
            new BigDecimal("41.45"),
            new BigDecimal("0.6152736302")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionSell() {
        final String row = "default,6101759,LTC-EUR,SELL,2020-05-18T20:10:26.735Z,2.81680093,LTC,41.35,0" +
            ".5823735922775,115" +
            ".8923448632225,EUR\n";
        final ImportedTransactionBean txBeanParsed  = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            "6101759",
            Instant.parse("2020-05-18T20:10:26.735Z"),
            Currency.LTC,
            Currency.EUR,
            TransactionType.SELL,
            new BigDecimal("2.81680093"),
            new BigDecimal("41.35"),
            new BigDecimal("0.5823735923")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testUnknownPair() {
        final String row = "default,6101759,BTC-ETH,SELL,2020-05-18T20:10:26.735Z,2.81680093,BTC,41.35,0" +
            ".5823735922775," +
            "115" +
            ".8923448632225,ETH\n";
        final RowError rowError = ParserTestUtils.getRowError(HEADER_CORRECT + row);
        assertNotNull(rowError);
        final String error = rowError.getMessage();
        assertTrue(error.contains(UNSUPPORTED_CURRENCY_PAIR.concat("BTC/ETH")));
    }

    @Test
    void testDiffFeeCurrency() {
        final String row = "default,6101759,LTC-EUR,SELL,2020-05-18T20:10:26.735Z,2.81680093,LTC,41.35,0" +
            ".5823735922775," +
            "115" +
            ".8923448632225,BTC\n";
        final RowError rowError = ParserTestUtils.getRowError(HEADER_CORRECT + row);
        assertNotNull(rowError);
        final String error = rowError.getMessage();
        assertTrue(error.contains(String.format(QUOTE_DIFFERS_FROM_PRICE_FEE_TOTAL_UNIT, "EUR", "BTC")));
    }

    @Test
    void testDiffSizeCurrency() {
        final String row = "default,6101759,LTC-EUR,SELL,2020-05-18T20:10:26.735Z,2.81680093,ETH,41.35,0" +
            ".5823735922775,115" +
            ".8923448632225,BTC\n";
        final RowError rowError = ParserTestUtils.getRowError(HEADER_CORRECT + row);
        assertNotNull(rowError);
        final String error = rowError.getMessage();
        assertTrue(error.contains(String.format(BASE_DIFFERS_FROM_UNIT_SIZE, "LTC", "ETH")));
    }

    @Test
    void testIgnoredTransactionType() {
        final String row = "default,6101759,LTC-EUR,DEPOSIT,2020-05-18T20:10:26.735Z,2.81680093,LTC,41.35,0" +
            ".5823735922775," +
            "115.8923448632225,BTC\n";
        final ConversionStatistic conversionStatistic
            = ParserTestUtils.getConversionStatistic(HEADER_CORRECT + row);
        assertNotNull(conversionStatistic);
        assertEquals(1, conversionStatistic.getIgnoredRowsCount());
        assertTrue(
            conversionStatistic
                .getErrorRows()
                .get(0)
                .getMessage()
                .contains(ExchangeBean.UNSUPPORTED_TRANSACTION_TYPE.concat("DEPOSIT"))
        );
    }
}