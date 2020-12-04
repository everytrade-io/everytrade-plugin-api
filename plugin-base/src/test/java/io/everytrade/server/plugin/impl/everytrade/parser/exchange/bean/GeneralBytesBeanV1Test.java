package io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean;

import io.everytrade.server.model.Currency;
import io.everytrade.server.model.TransactionType;
import io.everytrade.server.plugin.api.parser.ConversionStatistic;
import io.everytrade.server.plugin.api.parser.ImportedTransactionBean;
import io.everytrade.server.plugin.impl.everytrade.parser.exception.ParsingProcessException;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class GeneralBytesBeanV1Test {
    private static final String HEADER_CORRECT = "Terminal SN;Server Time;Terminal Time;Local Transaction Id;" +
        "Remote Transaction Id;Type;Cash Amount;Cash Currency;Crypto Amount;Crypto Currency;Used Discount;" +
        "Actual Discount (%);Destination address;Related Remote Transaction Id;Identity;Status;Phone Number;" +
        "Transaction Detail;\n";

    @Test
    void testCorrectHeader() {
        try {
            ParserTestUtils.testParsing(HEADER_CORRECT);
        } catch (ParsingProcessException e) {
            fail("Unexpected exception has been thrown." + e.getMessage());
        }
    }

    @Test
    void testWrongHeader() {
        final String headerWrong = "Terminal SN;Server Time;Terminal Time;Local TransactioX Id;Remote Transaction Id;" +
            "Type;Cash Amount;Cash Currency;Crypto Amount;Crypto Currency;Used Discount;Actual Discount (%);" +
            "Destination address;Related Remote Transaction Id;Identity;Status;Phone Number;Transaction Detail;\n";
        try {
            ParserTestUtils.testParsing(headerWrong);
            fail("No expected exception has been thrown.");
        } catch (ParsingProcessException e) {
        }
    }


    @Test
    void testCorrectParsingRawTransaction() {
        final String row
            = "BT300132;2018-08-06 06:38:43.0;2018-08-06 12:38:43.0;LAMCAC;R3IKYZ;SELL;8000;CZK;0.052674;BTC;;0.00;" +
            "19Tg7X8FrhkdCLP2dWhiDZ7WdNe1VnDuN9;;I2TZ4MYIUHAYRB3V;PAYMENT ARRIVED;;" +
            "0fa8a3aa9fa680433ac0cd671b7dc23dbd3bd679a1e055b8cee2b4de536308a5;\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            "LAMCAC-R3IKYZ",
            Instant.parse("2018-08-06T06:38:43Z"),
            Currency.BTC,
            Currency.CZK,
            TransactionType.BUY,
            new BigDecimal("0.05267400"),
            new BigDecimal("151877.5866651479"),
            BigDecimal.ZERO
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionDate_d_m_yy() {
        final String row = "BT300132;8/6/18 06:38:43.0;2018-08-06 12:38:43.0;" +
            "LAMCAC;R3IKYZ;SELL;8000;CZK;0.052674;BTC;;0.00;" +
            "19Tg7X8FrhkdCLP2dWhiDZ7WdNe1VnDuN9;;I2TZ4MYIUHAYRB3V;PAYMENT ARRIVED;;" +
            "0fa8a3aa9fa680433ac0cd671b7dc23dbd3bd679a1e055b8cee2b4de536308a5;\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            "LAMCAC-R3IKYZ",
            Instant.parse("2018-08-06T06:38:43Z"),
            Currency.BTC,
            Currency.CZK,
            TransactionType.BUY,
            new BigDecimal("0.05267400"),
            new BigDecimal("151877.5866651479"),
            BigDecimal.ZERO
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testCorrectParsingRawTransactionDate_d_m_yyTime_h_mm() {
        final String row = "BT300132;8/6/18 6:38:00 AM;2018-08-06 12:38:43.0;" +
            "LAMCAC;R3IKYZ;SELL;8000;CZK;0.052674;BTC;;0.00;" +
            "19Tg7X8FrhkdCLP2dWhiDZ7WdNe1VnDuN9;;I2TZ4MYIUHAYRB3V;PAYMENT ARRIVED;;" +
            "0fa8a3aa9fa680433ac0cd671b7dc23dbd3bd679a1e055b8cee2b4de536308a5;\n";
        final ImportedTransactionBean txBeanParsed = ParserTestUtils.getTransactionBean(HEADER_CORRECT + row);
        final ImportedTransactionBean txBeanCorrect = new ImportedTransactionBean(
            "LAMCAC-R3IKYZ",
            Instant.parse("2018-08-06T06:38:00Z"),
            Currency.BTC,
            Currency.CZK,
            TransactionType.BUY,
            new BigDecimal("0.05267400"),
            new BigDecimal("151877.5866651479"),
            BigDecimal.ZERO
        );
        ParserTestUtils.checkEqual(txBeanParsed, txBeanCorrect);
    }

    @Test
    void testUnknonwStatus() {
        final String row = "BT300132;2018-08-06 06:38:43.0;2018-08-06 12:38:43.0;LAMCAC;R3IKYZ;SELL;8000;CZK" +
            ";0.052674;BTC;;0.00;19Tg7X8FrhkdCLP2dWhiDZ7WdNe1VnDuN9;;I2TZ4MYIUHAYRB3V;UNKNOWN;;" +
            "0fa8a3aa9fa680433ac0cd671b7dc23dbd3bd679a1e055b8cee2b4de536308a5;\n";
        final ConversionStatistic conversionStatistic
            = ParserTestUtils.getConversionStatistic(HEADER_CORRECT + row);
        assertNotNull(conversionStatistic);
        assertEquals(1, conversionStatistic.getIgnoredRowsCount());
        assertTrue(
            conversionStatistic
                .getErrorRows()
                .get(0)
                .getMessage()
                .contains(ExchangeBean.UNSUPPORTED_STATUS_TYPE.concat("UNKNOWN"))
        );
    }

    @Test
    void testIgnoredTransactionType() {
        final String row = "BT300132;2018-08-06 06:38:43.0;2018-08-06 12:38:43.0;LAMCAC;R3IKYZ;SOLD;8000;CZK;" +
            "0.052674;BTC;;0.00;19Tg7X8FrhkdCLP2dWhiDZ7WdNe1VnDuN9;;I2TZ4MYIUHAYRB3V;COMPLETED (1);;" +
            "0fa8a3aa9fa680433ac0cd671b7dc23dbd3bd679a1e055b8cee2b4de536308a5;\n";
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
}