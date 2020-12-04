package io.everytrade.server.plugin.impl.everytrade;

import io.everytrade.server.plugin.api.IPlugin;
import io.everytrade.server.plugin.api.connector.ConnectorDescriptor;
import io.everytrade.server.plugin.api.connector.IConnector;
import io.everytrade.server.plugin.api.parser.ICsvParser;
import io.everytrade.server.plugin.api.parser.ParserDescriptor;
import io.everytrade.server.plugin.impl.everytrade.parser.EverytradeCsvParser;
import org.pf4j.Extension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Extension
public class EveryTradePlugin implements IPlugin {
    public static final String ID = "everytrade";

    private static final Map<String, ConnectorDescriptor> CONNECTORS_BY_ID = Set.of(
        EveryTradeConnector.DESCRIPTOR,
        KrakenConnector.DESCRIPTOR,
        BitstampConnector.DESCRIPTOR,
        CoinmateConnector.DESCRIPTOR,
        BitfinexConnector.DESCRIPTOR,
        BinanceConnector.DESCRIPTOR,
        BittrexConnector.DESCRIPTOR,
        CoinbaseProConnector.DESCRIPTOR,
        BitmexConnector.DESCRIPTOR,
        OkexConnector.DESCRIPTOR,
        HuobiConnector.DESCRIPTOR
    ).stream().collect(Collectors.toMap(ConnectorDescriptor::getId, it -> it));

    private static final List<ParserDescriptor> PARSER_DESCRIPTORS = List.of(
        EverytradeCsvParser.DESCRIPTOR
    );

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<ConnectorDescriptor> allConnectorDescriptors() {
        return List.copyOf(CONNECTORS_BY_ID.values());
    }

    @Override
    public ConnectorDescriptor connectorDescriptor(String connectorId) {
        return CONNECTORS_BY_ID.get(connectorId);
    }

    @Override
    public IConnector createConnectorInstance(String connectorId, Map<String, String> parameters) {
        if (connectorId.equals(EveryTradeConnector.DESCRIPTOR.getId())) {
            return new EveryTradeConnector(parameters);
        }
        if (connectorId.equals(KrakenConnector.DESCRIPTOR.getId())) {
            return new KrakenConnector(parameters);
        }
        if (connectorId.equals(BitstampConnector.DESCRIPTOR.getId())) {
            return new BitstampConnector(parameters);
        }
        if (connectorId.equals(CoinmateConnector.DESCRIPTOR.getId())) {
            return new CoinmateConnector(parameters);
        }
        if (connectorId.equals(BitfinexConnector.DESCRIPTOR.getId())) {
            return new BitfinexConnector(parameters);
        }
        if (connectorId.equals(BinanceConnector.DESCRIPTOR.getId())) {
            return new BinanceConnector(parameters);
        }
        if (connectorId.equals(BittrexConnector.DESCRIPTOR.getId())) {
            return new BittrexConnector(parameters);
        }
        if (connectorId.equals(CoinbaseProConnector.DESCRIPTOR.getId())) {
            return new CoinbaseProConnector(parameters);
        }
        if (connectorId.equals(BitmexConnector.DESCRIPTOR.getId())) {
            return new BitmexConnector(parameters);
        }
        if (connectorId.equals(OkexConnector.DESCRIPTOR.getId())) {
            return new OkexConnector(parameters);
        }
        if (connectorId.equals(HuobiConnector.DESCRIPTOR.getId())) {
            return new HuobiConnector(parameters);
        }
        return null;
    }

    @Override
    public List<ParserDescriptor> allParserDescriptors() {
        return List.copyOf(PARSER_DESCRIPTORS);
    }

    @Override
    public ICsvParser createParserInstance(String parserId) {
        if (parserId.equals(EverytradeCsvParser.DESCRIPTOR.getId())) {
            return new EverytradeCsvParser();
        }
        return null;
    }
}
