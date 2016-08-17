package fr.cph.stock.business.impl;

import fr.cph.stock.business.CurrencyBusiness;
import fr.cph.stock.dao.CurrencyDAO;
import fr.cph.stock.entities.CurrencyData;
import fr.cph.stock.enumtype.Currency;
import fr.cph.stock.exception.YahooException;
import fr.cph.stock.external.IExternalDataAccess;
import fr.cph.stock.external.YahooExternalDataAccess;
import fr.cph.stock.util.Util;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public enum CurrencyBusinessImpl implements CurrencyBusiness {

	INSTANCE;

	private static final Logger LOG = Logger.getLogger(CurrencyBusinessImpl.class);
	private static final int PAUSE = 1000;

	private IExternalDataAccess yahoo;
	private CurrencyDAO currencyDAO;

	CurrencyBusinessImpl() {
		yahoo = new YahooExternalDataAccess();
		currencyDAO = new CurrencyDAO();
	}

	@Override
	public final Currency loadCurrencyData(final Currency currency) throws YahooException {
		List<CurrencyData> currencyDataList = currencyDAO.selectListCurrency(currency.getCode());
		if (currencyDataList.size() == 0) {
			List<CurrencyData> currenciesData = yahoo.getCurrencyData(currency);
			for (final CurrencyData currencyData : currenciesData) {
				CurrencyData c = currencyDAO.selectOneCurrencyDataWithParam(currencyData);
				if (c == null) {
					currencyDAO.insert(currencyData);
				} else {
					currencyData.setId(c.getId());
					currencyDAO.update(currencyData);
				}
			}
			currencyDataList = currencyDAO.selectListCurrency(currency.getCode());
		}
		currency.setCurrencyData(currencyDataList);
		return currency;
	}

	@Override
	public final void updateAllCurrencies() throws YahooException {
		List<Currency> currencyDone = new ArrayList<>();
		for (Currency currency : Currency.values()) {
			List<CurrencyData> currenciesData = yahoo.getCurrencyData(currency);
			Util.makeAPause(PAUSE);
			if ((Currency.values().length - 1) * 2 == currenciesData.size()) {
				for (final CurrencyData currencyData : currenciesData) {
					if (!(currencyDone.contains(currencyData.getCurrency1()) || currencyDone.contains(currencyData.getCurrency2()))) {
						CurrencyData c = currencyDAO.selectOneCurrencyDataWithParam(currencyData);
						if (c == null) {
							currencyDAO.insert(currencyData);
						} else {
							currencyData.setId(c.getId());
							currencyDAO.update(currencyData);
						}
					}
				}
				currencyDone.add(currency);
			} else {
				LOG.warn("Impossible to update this currency: " + currency.getCode());
			}
		}
	}

	@Override
	public final void updateOneCurrency(final Currency currency) throws YahooException {
		List<CurrencyData> currenciesData = yahoo.getCurrencyData(currency);
		if ((Currency.values().length - 1) * 2 == currenciesData.size()) {
			for (final CurrencyData currencyData : currenciesData) {
				CurrencyData c = currencyDAO.selectOneCurrencyDataWithParam(currencyData);
				if (c == null) {
					currencyDAO.insert(currencyData);
				} else {
					currencyData.setId(c.getId());
					currencyDAO.update(currencyData);
				}
			}
		} else {
			throw new YahooException(
				"The current table 'yahoo.finance.xchange' has been blocked. It exceeded the allotted quotas of either time or instructions");
		}
	}

	@Override
	public final Object[][] getAllCurrencyData(final Currency currency) {
		final List<CurrencyData> currencies = currencyDAO.selectListAllCurrency();
		final Currency[] currencyTab = Currency.values();
		final Object[][] res = new Object[currencyTab.length - 1][6];
		int i = 0;
		for (final Currency c : currencyTab) {
			if (c != currency) {
				res[i][0] = c.toString();
				res[i][1] = c.getName();
				for (final CurrencyData currencyData : currencies) {
					if (c == currencyData.getCurrency1() && currency == currencyData.getCurrency2()) {
						res[i][3] = currencyData.getValue().toString();
						res[i][4] = currencyData.getLastUpdate();
					}
					if (currency == currencyData.getCurrency1() && c == currencyData.getCurrency2()) {
						res[i][2] = currencyData.getValue().toString();
					}
				}
				i++;
			}
		}
		return res;
	}
}
