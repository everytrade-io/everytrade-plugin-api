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
import static io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean.UNSUPPORTED_TRANSACTION_TYPE;
import static io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.HuobiBeanV1.UNSUPPORTED_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class HuobiBeanV1Test {
    public static final String HEADER_CORRECT
        = "\uFEFF\"Time\",\"Type\",\"Pair\",\"Side\",\"Price\",\"Amount\",\"Total\",\"Fee\"\n";


    @Test
    void testWrongHeader() {
        final String headerWrong = "\"Time\",\"Type\",\"Pair\",\"Price\",\"Amount\",\"Total\",\"Fee\"\n";
        try {
            ParserTestUtils.testParsing(headerWrong);
            fail("No expected exception has been thrown.");
        } catch (ParsingProcessException e) {
        }
    }

    @Test
    void testCorrectParsingRawTransactionBuy() {
        final String row = "2020-03-31 21:31:43,Exchange,LTC/BTC,Buy,0.006040,0.8940,0.0053,0.00178800LTC,\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            null,
            Instant.parse("2020-03-31T21:31:43Z"),
            Currency.LTC,
            Currency.BTC,
            TransactionType.BUY,
            new BigDecimal("0.89221200"),
            new BigDecimal("0.0059402922"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionBuyDiffFee() {
        final String row = "2020-03-31 21:31:43,Exchange,LTC/BTC,Buy,0.006040,0.8940,0.0053,0.00178800BTC,\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            null,
            Instant.parse("2020-03-31T21:31:43Z"),
            Currency.LTC,
            Currency.BTC,
            TransactionType.BUY,
            new BigDecimal("0.894"),
            new BigDecimal("0.0079284116"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }


    @Test
    void testCorrectParsingRawTransactionSell() {
        final String row = "2020-03-31 21:31:24,Exchange,LTC/BTC,Sell,0.006036,0.7362,0.0044,0.00000888BTC,\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            null,
            Instant.parse("2020-03-31T21:31:24Z"),
            Currency.LTC,
            Currency.BTC,
            TransactionType.SELL,
            new BigDecimal("0.73620000"),
            new BigDecimal("0.0059645748"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionSellDiffFee() {
        final String row = "2020-03-31 21:31:24,Exchange,LTC/BTC,Sell,0.006036,0.7362,0.0044,0.00000888LTC,\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            null,
            Instant.parse("2020-03-31T21:31:24Z"),
            Currency.LTC,
            Currency.BTC,
            TransactionType.SELL,
            new BigDecimal("0.73620888"),
            new BigDecimal("0.0059765647"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }


    @Test
    void testUnknownType() {
        final String row = "2020-03-31 21:31:24,YYY,LTC/BTC,Sell,0.006036,0.7362,0.0044,0.00000888LTC,\n";
        final RowError rowError = ParserTestUtils.getRowError(HEADER_CORRECT + row);
        assertNotNull(rowError);
        final String error = rowError.getMessage();
        assertTrue(error.contains(UNSUPPORTED_TYPE.concat("YYY")));
    }

    @Test
    void testUnknownTransactionType() {
        final String row = "2020-03-31 21:31:24,Exchange,LTC/BTC,Deposit,0.006036,0.7362,0.0044,0.00000888LTC,\n";
        final RowError rowError = ParserTestUtils.getRowError(HEADER_CORRECT + row);
        assertNotNull(rowError);
        final String error = rowError.getMessage();
        assertTrue(error.contains(UNSUPPORTED_TRANSACTION_TYPE.concat("Deposit")));
    }

    @Test
    void testWrongFeeCurrency() {
        final String row = "2020-03-31 21:31:43,Exchange,LTC/BTC,Buy,0.006040,0.8940,0.0053,0.00178800ETH,\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            null,
            Instant.parse("2020-03-31T21:31:43Z"),
            Currency.LTC,
            Currency.BTC,
            TransactionType.BUY,
            new BigDecimal("0.89400000"),
            new BigDecimal("0.0059284116"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testNotAllowedPair() {
        final String row = "2020-03-31 21:31:24,Exchange,BTC/LTC,Sell,0.006036,0.7362,0.0044,0.00000888BTC,\n";
        final RowError rowError = ParserTestUtils.getRowError(HEADER_CORRECT + row);
        assertNotNull(rowError);
        final String error = rowError.getMessage();
        assertTrue(error.contains(UNSUPPORTED_CURRENCY_PAIR.concat("BTC/LTC")));
    }

    @Test
    void testIgnoredFee() {
        final String row = "2020-03-31 21:31:24,Exchange,LTC/BTC,Sell,0.006036,0.7362,0.0044,0.00000888XXX,\n";
        final ConversionStatistic conversionStatistic =
            ParserTestUtils.getConversionStatistic(HEADER_CORRECT + row);
        assertNotNull(conversionStatistic);
        assertEquals(1, conversionStatistic.getIgnoredFeeTransactionCount());
    }

    @Test
    void testIgnoredTransactionType() {
        final String row = "2020-03-31 21:31:43,Exchange,LTC/BTC,Bought,0.006040,0.8940,0.0053,0.00178800LTC,\n";
        final ConversionStatistic conversionStatistic
            = ParserTestUtils.getConversionStatistic(HEADER_CORRECT + row);
        assertNotNull(conversionStatistic);
        assertEquals(1, conversionStatistic.getIgnoredRowsCount());
        assertTrue(
            conversionStatistic
                .getErrorRows()
                .get(0)
                .getMessage()
                .contains(ExchangeBean.UNSUPPORTED_TRANSACTION_TYPE.concat("Bought"))
        );
    }
}