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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CoinsquareBeanV1Test {
    private static final String HEADER_CORRECT = "date;action;currency;base_currency;price;amount;base_amount\n";

    @Test
    void testWrongHeader() {
        final String headerWrong = "date;action;currency;base;price;amount;base_amount\n";
        try {
            ParserTestUtils.testParsing(headerWrong);
            fail("No expected exception has been thrown.");
        } catch (ParsingProcessException e) {
        }
    }

    @Test
    void testCorrectParsingRawTransactionBuy() {
        final String row = "23-02-20;buy;CAD;BTC;0,00007663;1,674.86;0.12823291\n";
        final ImportedTransactionBean txBeanParsed  = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            null,
            Instant.parse("2020-02-23T00:00:00Z"),
            Currency.BTC,
            Currency.CAD,
            TransactionType.BUY,
            new BigDecimal("0.12823291"),
            new BigDecimal("13061.0776905866"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionBuyWithIgnoredChars() {
        final String row = "23-02-20;buy;CAD;BTC;0,00007663;1 674.86$;0.128,232,91\n";
        final ImportedTransactionBean txBeanParsed  = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            null,
            Instant.parse("2020-02-23T00:00:00Z"),
            Currency.BTC,
            Currency.CAD,
            TransactionType.BUY,
            new BigDecimal("0.12823291"),
            new BigDecimal("13061.0776905866"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionSell() {
        final String row = "27-12-19;sell;CAD;BTC;0,00010604;2,271.66;0.24064597\n";
        final ImportedTransactionBean txBeanParsed  = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            null,
            Instant.parse("2019-12-27T00:00:00Z"),
            Currency.BTC,
            Currency.CAD,
            TransactionType.SELL,
            new BigDecimal("0.24064597"),
            new BigDecimal("9439.8422712003"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionSellNegativeAmount() {
        final String row = "27-12-19;sell;CAD;BTC;0,00010604;2,271.66;-0.24064597\n";
        final ImportedTransactionBean txBeanParsed  = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            null,
            Instant.parse("2019-12-27T00:00:00Z"),
            Currency.BTC,
            Currency.CAD,
            TransactionType.SELL,
            new BigDecimal("0.24064597"),
            new BigDecimal("9439.8422712003"),
            new BigDecimal("0")
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testNotAllowedPair() {
        final String row = "27-12-19;sell;DASH;ETH;0,00010604;2,271.66;-0.24064597\n";
        RowError rowError = ParserTestUtils.getRowError(HEADER_CORRECT + row);
        assertNotNull(rowError);
        final String error = rowError.getMessage();
        assertTrue(error.contains(UNSUPPORTED_CURRENCY_PAIR.concat("ETH/DASH")));
    }

    @Test
    void testIgnoredTransactionType() {
        final String row = "23-02-20;bought;CAD;BTC;0,00007663;1,674.86;0.12823291\n";
        final ConversionStatistic conversionStatistic
            = ParserTestUtils.getConversionStatistic(HEADER_CORRECT + row);
        assertNotNull(conversionStatistic);
        assertEquals(1, conversionStatistic.getIgnoredRowsCount());
        assertTrue(
            conversionStatistic
                .getErrorRows()
                .get(0)
                .getMessage()
                .contains(ExchangeBean.UNSUPPORTED_TRANSACTION_TYPE.concat("bought"))
        );
    }
}