package io.everytrade.server.plugin.impl.everytrade.parser.postprocessor;

import io.everytrade.server.plugin.impl.everytrade.parser.exchange.ExchangeBean;

import java.util.List;

public class DefaultPostProcessor implements IPostProcessor {
    @Override
    public ConversionParams evalConversionParams(List<? extends ExchangeBean> exchangeBeans) {
        return null;
    }
}