package io.everytrade.server.plugin.impl.everytrade.parser;

import com.univocity.parsers.common.DataValidationException;
import io.everytrade.server.model.SupportedExchange;
import io.everytrade.server.plugin.api.IPlugin;
import io.everytrade.server.plugin.api.parser.ConversionStatistic;
import io.everytrade.server.plugin.api.parser.ICsvParser;
import io.everytrade.server.plugin.api.parser.ImportedTransactionBean;
import io.everytrade.server.plugin.api.parser.ParseResult;
import io.everytrade.server.plugin.api.parser.ParserDescriptor;
import io.everytrade.server.plugin.api.parser.RowError;
import io.everytrade.server.plugin.api.parser.RowErrorType;
import io.everytrade.server.plugin.impl.everytrade.EveryTradePlugin;
import io.everytrade.server.plugin.impl.everytrade.parser.exception.UnknownHeaderException;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.BitfinexExchangeSpecificParser;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.OkexExchangeSpecificParser;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.BinanceBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.BitflyerBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.BitmexBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.BitstampBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.BittrexBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.BittrexBeanV2;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.CoinbaseBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.CoinmateBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.CoinmateBeanV2;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.CoinsquareBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.CoinsquareBeanV2;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.EveryTradeBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.EveryTradeBeanV2;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.GeneralBytesBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.HitBtcBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.HuobiBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.KrakenBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.LocalBitcoinsBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.PaxfulBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.PoloniexBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.bean.ShakePayBeanV1;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.binance.v2.BinanceExchangeSpecificParser;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.DefaultUnivocityExchangeSpecificParser;
import io.everytrade.server.plugin.impl.everytrade.parser.exchange.IExchangeSpecificParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EverytradeCsvMultiParser implements ICsvParser {
    private static final String ID = EveryTradePlugin.ID + IPlugin.PLUGIN_PATH_SEPARATOR + "everytradeParser";
    private static final Map<String, ExchangeParseDetail> EXCHANGE_PARSE_DETAILS = new HashMap<>();
    private static final String DELIMITER_COMMA = ",";
    private static final String DELIMITER_SEMICOLON = ";";

    static {
        EXCHANGE_PARSE_DETAILS.put(
            "Date(UTC);Market;Type;Price;Amount;Total;Fee;Fee Coin",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(BinanceBeanV1.class),
                SupportedExchange.BINANCE,
                DELIMITER_SEMICOLON
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "Date(UTC);Pair;Type;Order Price;Order Amount;AvgTrading Price;Filled;Total;status",
            new ExchangeParseDetail(
                new BinanceExchangeSpecificParser(),
                SupportedExchange.BINANCE,
                DELIMITER_SEMICOLON
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "#,PAIR,AMOUNT,PRICE,FEE,FEE CURRENCY,DATE,ORDER ID",
            new ExchangeParseDetail(
                new BitfinexExchangeSpecificParser(),
                SupportedExchange.BITFINEX,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "Trade Date;Product;Trade Type;Traded Price;Currency 1;Amount (Currency 1);Fee;USD Rate (Currency);" +
                "Currency 2;Amount (Currency 2);Order ID;Details",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(BitflyerBeanV1.class),
                SupportedExchange.BITFLYER,
                DELIMITER_SEMICOLON
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "\uFEFF\"transactTime\",\"symbol\",\"execType\",\"side\",\"lastQty\",\"lastPx\",\"execCost\",\"commission\"," +
                "\"execComm\",\"ordType\",\"orderQty\",\"leavesQty\",\"price\",\"text\",\"orderID\"",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(BitmexBeanV1.class),
                SupportedExchange.BITMEX,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "Type,Datetime,Account,Amount,Value,Rate,Fee,Sub Type",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(BitstampBeanV1.class),
                SupportedExchange.BITSTAMP,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "OrderUuid,Exchange,Type,Quantity,Limit,CommissionPaid,Price,Opened,Closed",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(BittrexBeanV1.class),
                SupportedExchange.BITTREX,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "Uuid,Exchange,TimeStamp,OrderType,Limit,Quantity,QuantityRemaining,Commission,Price,PricePerUnit,"
                + "IsConditional,Condition,ConditionTarget,ImmediateOrCancel,Closed",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(BittrexBeanV2.class),
                SupportedExchange.BITTREX,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "portfolio,trade id,product,side,created at,size,size unit,price,fee,total,price/fee/total unit",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(CoinbaseBeanV1.class),
                SupportedExchange.COINBASE,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "ID;Date;Type;Amount;Amount Currency;Price;Price Currency;Fee;Fee Currency;Total;" +
                "Total Currency;Description;Status",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(CoinmateBeanV1.class),
                SupportedExchange.COINMATE,
                DELIMITER_SEMICOLON
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "?Transaction id;Date;Email;Name;Type;Type detail;Currency amount;Amount;Currency price;Price;" +
                "Currency fee;Fee;Currency total;Total;Description;Status;Currency first balance after;" +
                "First balance after;Currency second balance after;Second balance after",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(CoinmateBeanV2.class),
                SupportedExchange.COINMATE,
                DELIMITER_SEMICOLON
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "date;action;currency;base_currency;price;amount;base_amount",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(CoinsquareBeanV1.class),
                SupportedExchange.COINSQUARE,
                DELIMITER_SEMICOLON
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "date;from_currency;from_amount;to_currency;to_amount",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(CoinsquareBeanV2.class),
                SupportedExchange.COINSQUARE,
                DELIMITER_SEMICOLON
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "UID;DATE;SYMBOL;ACTION;QUANTY;PRICE;FEE",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(EveryTradeBeanV1.class),
                SupportedExchange.EVERYTRADE,
                DELIMITER_SEMICOLON
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "UID;DATE;SYMBOL;ACTION;QUANTY;VOLUME;FEE",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(EveryTradeBeanV2.class),
                SupportedExchange.EVERYTRADE,
                DELIMITER_SEMICOLON
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "Terminal SN;Server Time;Terminal Time;Local Transaction Id;Remote Transaction Id;Type;Cash Amount;" +
                "Cash Currency;Crypto Amount;Crypto Currency;Used Discount;Actual Discount (%);Destination address;" +
                "Related Remote Transaction Id;Identity;Status;Phone Number;Transaction Detail;",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(GeneralBytesBeanV1.class),
                SupportedExchange.GENERAL_BYTES,
                DELIMITER_SEMICOLON
            )
        );
        //
        EXCHANGE_PARSE_DETAILS.put(
            "\"Date (UTC)\",\"Instrument\",\"Trade ID\",\"Order ID\",\"Side\",\"Quantity\",\"Price\",\"Volume\"," +
                "\"Fee\",\"Rebate\",\"Total\"",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(HitBtcBeanV1.class),
                SupportedExchange.HITBTC,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "\"Date (+01)\",\"Instrument\",\"Trade ID\",\"Order ID\",\"Side\",\"Quantity\",\"Price\",\"Volume\"," +
                "\"Fee\",\"Rebate\",\"Total\"",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(HitBtcBeanV1.class),
                SupportedExchange.HITBTC,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "\uFEFF\"Time\",\"Type\",\"Pair\",\"Side\",\"Price\",\"Amount\",\"Total\",\"Fee\"",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(HuobiBeanV1.class),
                SupportedExchange.HUOBI,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "txid,ordertxid,pair,time,type,ordertype,price,cost,fee,vol,margin,misc,ledgers",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(KrakenBeanV1.class),
                SupportedExchange.KRAKEN,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "id,created_at,buyer,seller,trade_type,btc_amount,btc_traded,fee_btc,btc_amount_less_fee,btc_final," +
                "fiat_amount,fiat_fee,fiat_per_btc,currency,exchange_rate,transaction_released_at,online_provider," +
                "reference",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(LocalBitcoinsBeanV1.class),
                SupportedExchange.LOCALBITCOINS,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "\uFEFFOrder ID,\uFEFFTrade ID,\uFEFFTrade Time,\uFEFFPairs,\uFEFFAmount,\uFEFFPrice,\uFEFFTotal," +
                "\uFEFFtaker/maker,\uFEFFFee,\uFEFFunit",
            new ExchangeParseDetail(
                new OkexExchangeSpecificParser(),
                SupportedExchange.OKEX,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "type,fiat_currency,amount_fiat,amount_btc,rate,fee_fiat,fee_btc,market_rate_usd,payment_method,partner," +
                "status,completed_at,trade_hash,offer_hash",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(PaxfulBeanV1.class),
                SupportedExchange.PAXFUL,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "Date,Market,Category,Type,Price,Amount,Total,Fee,Order Number,Base Total Less Fee,Quote Total Less Fee",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(PoloniexBeanV1.class),
                SupportedExchange.POLONIEX,
                DELIMITER_COMMA
            )
        );
        EXCHANGE_PARSE_DETAILS.put(
            "Transaction Type,Date,Amount Debited,Debit Currency,Amount Credited,Credit Currency,Exchange Rate," +
                "Credit/Debit,Spot Rate",
            new ExchangeParseDetail(
                new DefaultUnivocityExchangeSpecificParser(ShakePayBeanV1.class),
                SupportedExchange.SHAKEPAY,
                DELIMITER_COMMA
            )
        );
    }

    public static final ParserDescriptor DESCRIPTOR = new ParserDescriptor(
        ID,
        EXCHANGE_PARSE_DETAILS.entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    v -> v.getValue().getSupportedExchange()
                )
            )
    );
    private final Logger log = LoggerFactory.getLogger(this.getClass());


    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ParseResult parse(File file, String header) {
        final ExchangeParseDetail exchangeParseDetail = EXCHANGE_PARSE_DETAILS.get(header);
        if (exchangeParseDetail == null) {
            throw new UnknownHeaderException(String.format("Unknown header: '%s'", header));
        }
        final List<RowError> rowErrors = new ArrayList<>();
        final IExchangeSpecificParser exchangeParser = exchangeParseDetail.getExchangeSpecificParser();
        List<? extends ExchangeBean> listBeans = exchangeParser.parse(
            file,
            exchangeParseDetail.getDelimiter(),
            rowErrors
        );

        int ignoredFeeCount = 0;
        List<ImportedTransactionBean> importedTransactionBeans = new ArrayList<>();
        for (ExchangeBean p : listBeans) {
            try {
                final ImportedTransactionBean importedTransactionBean = p.toImportedTransactionBean();
                importedTransactionBeans.add(importedTransactionBean);
                if (importedTransactionBean.getImportDetail().isIgnoredFee()) {
                    ignoredFeeCount++;
                }
            } catch (DataValidationException e) {
                rowErrors.add(new RowError(p.rowToString(), e.getMessage(), RowErrorType.FAILED));
            }
        }

        log.info("{} transaction(s) parsed successfully.", importedTransactionBeans.size());
        if (!rowErrors.isEmpty()) {
            log.warn("{} row(s) not parsed.", rowErrors.size());
        }

        return new ParseResult(importedTransactionBeans, new ConversionStatistic(rowErrors, ignoredFeeCount));
    }
}
