package io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean;

import io.everytrade.server.plugin.api.parser.ConversionStatistic;
import io.everytrade.server.plugin.api.parser.ImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.ParseResult;
import io.everytrade.server.plugin.api.parser.RowError;
import io.everytrade.server.plugin.impl.everytrade.parser.EverytradeCsvMultiParser;
import io.everytrade.server.plugin.impl.everytrade.parser.exception.ParsingProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;


public class ParserTestUtils {
    private static final EverytradeCsvMultiParser CSV_PARSER = new EverytradeCsvMultiParser();
    private static final Logger LOG = LoggerFactory.getLogger(ParserTestUtils.class);

    private static File createTestFile(String rows) {
        try {
            File file = File.createTempFile("parsertest", "csv");
            new FileWriter(file)
                    .append(rows)
                    .close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void checkEqual(ImportedTransactionBean txCorrect, ImportedTransactionBean txExpected) {
        assertNotNull(txExpected);
        assertNotNull(txCorrect);
        assertEquals(txExpected.getUid(), txCorrect.getUid());
        assertEquals(0, txExpected.getExecuted().compareTo(txCorrect.getExecuted()));
        assertEquals(txExpected.getBase(), txCorrect.getBase());
        assertEquals(txExpected.getQuote(), txCorrect.getQuote());
        assertEquals(txExpected.getAction(), txCorrect.getAction());
        assertEquals(0, txExpected.getBaseQuantity().compareTo(txCorrect.getBaseQuantity()));
        assertEquals(0, txExpected.getUnitPrice().compareTo(txCorrect.getUnitPrice()));
        assertEquals(0, txExpected.getFeeQuote().compareTo(txCorrect.getFeeQuote()));
    }

    public static ImportedTransactionBean getTransactionBean(String rows) {
        try {
            final ParseResult result = CSV_PARSER.parse(ParserTestUtils.createTestFile(rows), getHeader(rows));
            if (!result.getConversionStatistic().isErrorRowsEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                result.getConversionStatistic().getErrorRows().forEach(p->stringBuilder.append(p).append("\n"));
                LOG.error("getRawTransaction(): NOT PARSED ROWS: {}", stringBuilder.toString());
            }
            List<ImportedTransactionBean> list = result.getImportedTransactionBeans();

            if (list.isEmpty()) {
                return null;
            }
            return list.get(0);
        } catch (ParsingProcessException e) {
            LOG.error("getRawTransaction(): ", e);
            return null;
        }
    }

    public static void testParsing(String rows)  {
        CSV_PARSER.parse(ParserTestUtils.createTestFile(rows), getHeader(rows));
    }

    public static RowError getRowError(String rows) {
        try {
           final  ParseResult result = CSV_PARSER.parse(ParserTestUtils.createTestFile(rows), getHeader(rows));
            List<RowError> list = result.getConversionStatistic().getErrorRows();
            if (list.size() < 1) {
                return null;
            }
            return list.get(0);
        } catch (ParsingProcessException e) {
            fail(e.getMessage());
            return null;
        }
    }

    public static ConversionStatistic getConversionStatistic(String rows) {
        try {
            final ParseResult result = CSV_PARSER.parse(ParserTestUtils.createTestFile(rows), getHeader(rows));
            return result.getConversionStatistic();
        } catch (ParsingProcessException e) {
            fail(e.getMessage());
            return null;
        }
    }

    private static String getHeader(String rows) {
        int lineSeparator = rows.indexOf("\r\n");
        if (lineSeparator < 0) {
            lineSeparator = rows.indexOf("\n");
        }
        if (lineSeparator < 0) {
            return null;
        }
        return rows.substring(0, lineSeparator);
    }
}
