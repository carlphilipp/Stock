/**
 * Copyright 2016 Carl-Philipp Harmant
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.stock.external.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.cph.stock.entities.Company;
import fr.cph.stock.entities.CurrencyData;
import fr.cph.stock.entities.Index;
import fr.cph.stock.enumtype.Currency;
import fr.cph.stock.enumtype.Market;
import fr.cph.stock.exception.YahooException;
import fr.cph.stock.exception.YahooUnknownTickerException;
import fr.cph.stock.external.ExternalDataAccess;
import fr.cph.stock.external.YahooGateway;
import fr.cph.stock.external.web.currency.BaseResult;
import fr.cph.stock.external.web.currency.Rate;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static fr.cph.stock.util.Constants.QUOTE;

/**
 * This class connect to yahooGatewayImpl api and convert the jsonObjects to java bean of the app
 *
 * @author Carl-Philipp Harmant
 */
@Log4j2
@Singleton
public class ExternalDataAccessImpl implements ExternalDataAccess {

	private static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private static final String CHANGE_IN_PERCENT = "ChangeinPercent";
	private static final String CATEGORY = "Category";
	private static final String CURRENCY = "Currency";
	private static final String CLOSE = "Close";
	private static final String DATE = "Date";
	private static final String DIVIDEND_YIELD = "DividendYield";
	private static final String DIAGNOSTICS = "diagnostics";
	private static final String DESCRIPTION = "description";
	private static final String ERROR = "error";
	private static final String FUND_FAMILY = "FundFamily";
	private static final String INDUSTRY = "Industry";
	private static final String JAVASCRIPT = "javascript";
	private static final String LAST_TRADE_PRICE_ONLY = "LastTradePriceOnly";
	private static final String PREVIOUS_CLOSE = "PreviousClose";
	private static final String QUERY = "query";
	private static final String MARKET_CAPITALIZATION = "MarketCapitalization";
	private static final String NET_ASSETS = "NetAssets";
	private static final String NULL = "null";
	private static final String NAME = "Name";
	private static final String RATE_UPPERCASE = "Rate";
	private static final String RATE_LOWERCASE = "rate";
	private static final String RESULTS = "results";
	private static final String STOCK_EXCHANGE = "StockExchange";
	private static final String SYMBOL = "symbol";
	private static final String SECTOR = "Sector";
	private static final String STOCK = "stock";
	private static final String YEAR_LOW = "YearLow";
	private static final String YEAR_HIGH = "YearHigh";

	@NonNull
	private YahooGateway yahooGateway;

	@Inject
	public ExternalDataAccessImpl(final YahooGateway yahooGateway) {
		this.yahooGateway = yahooGateway;
	}

	// TODO sounds like we need GSON or Jackson here.
	@Override
	public List<Company> getCompaniesData(final List<String> yahooIds) throws YahooException {
		final List<Company> companies = new ArrayList<>();

		final String requestQuotes = "select * from yahoo.finance.quotes where symbol in (" + getFormattedList(yahooIds) + ")";
		final JsonObject json = yahooGateway.getJSONObject(requestQuotes);
		final JsonArray jsonResults = getJSONArrayFromJSONObject(json);
		for (int j = 0; j < jsonResults.size(); j++) {
			final JsonObject jsonCompany = (JsonObject) jsonResults.get(j);
			Company company = Company.builder().yahooId(jsonCompany.get(SYMBOL).getAsString()).manual(false).build();
			if (jsonCompany.get(STOCK_EXCHANGE).getAsString().equals(NULL)) {
				company = getCompanyInfo(company);
				if (company.getSector() != null && company.getIndustry() != null && company.getMarketCapitalization() != null) {
					company.setRealTime(false);
					company.setMarket(guessMarket(company.getYahooId()));
					company.setCurrency(Market.getCurrency(company.getMarket()));
					final Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DATE, -5);
					try {
						final List<Company> temp = getCompanyDataHistory(company.getYahooId(), cal.getTime(), null);
						company.setQuote(temp.get(0).getQuote());
					} catch (final YahooException e) {
						log.error(e.getMessage(), e);
					}
					companies.add(company);
				} else {
					throw new YahooUnknownTickerException(jsonCompany.get(SYMBOL).getAsString() + YahooUnknownTickerException.TOKEN_UNKNOWN);
				}
			} else {
				company.setName(WordUtils.capitalizeFully(jsonCompany.get(NAME).getAsString()));
				final String stockExchange = jsonCompany.get(STOCK_EXCHANGE).getAsString().toUpperCase();
				company.setMarket(Market.getMarket(stockExchange));

				if (company.getMarket().equals(Market.UNKNOWN)) {
					final String currency = jsonCompany.get(CURRENCY).getAsString();
					if (StringUtils.isNotEmpty(currency) && !currency.equals(NULL)) {
						company.setCurrency(Currency.getEnum(currency));
					}
				} else {
					company.setCurrency(Market.getCurrency(company.getMarket()));
				}
				JsonElement jsonElement = jsonCompany.get(DIVIDEND_YIELD);
				if (!jsonElement.isJsonNull()) {
					company.setYield(jsonElement.getAsDouble());
				}
				final String lastTrade = jsonCompany.get(LAST_TRADE_PRICE_ONLY).getAsString();
				if (StringUtils.isNotEmpty(lastTrade) && !lastTrade.equals(NULL)) {
					company.setQuote(Double.valueOf(lastTrade));
				}
				final JsonElement marketCap = jsonCompany.get(MARKET_CAPITALIZATION);
				if (!marketCap.isJsonNull()) {
					company.setMarketCapitalization(marketCap.getAsString());
				}
				final Double previousClose = jsonCompany.get(PREVIOUS_CLOSE).getAsDouble();
				if (!previousClose.isNaN()) {
					company.setYesterdayClose(previousClose);
				} else {
					company.setYesterdayClose(0.0);
				}
				company.setChangeInPercent(jsonCompany.get(CHANGE_IN_PERCENT).getAsString());
				final JsonElement yearLowJsonElement = jsonCompany.get(YEAR_LOW);
				if (!yearLowJsonElement.isJsonNull()) {
					company.setYearLow(yearLowJsonElement.getAsDouble());
				}
				final JsonElement yearHighJsonElement = jsonCompany.get(YEAR_HIGH);
				if (!yearHighJsonElement.isJsonNull()) {
					company.setYearHigh(yearHighJsonElement.getAsDouble());
				}
				company.setRealTime(true);
				company.setFund(false);
				companies.add(company);
			}
		}
		return companies;
	}

	@Override
	public Company getCompanyInfo(final Company company) throws YahooException {
		final String requestStocks = "select * from yahoo.finance.stocks where symbol='" + company.getYahooId() + "'";
		JsonObject jsonCompanyInfo = yahooGateway.getJSONObject(requestStocks);
		try {
			jsonCompanyInfo = jsonCompanyInfo.getAsJsonObject(QUERY).getAsJsonObject(RESULTS).getAsJsonObject(STOCK);
		} catch (final Exception e) {
			throw new YahooException("Error while getting info from json : " + company.getYahooId() + " " + jsonCompanyInfo, e);
		}
		String sector = null;
		String industry = null;
		JsonElement sectorJsonElement = jsonCompanyInfo.get(SECTOR);
		JsonElement industryJsonElement = jsonCompanyInfo.get(INDUSTRY);
		if ((sectorJsonElement == null || sectorJsonElement.isJsonNull()) && (industryJsonElement == null || industryJsonElement.isJsonNull())) {
			if (jsonCompanyInfo.get(CATEGORY) != null) {
				sector = jsonCompanyInfo.get(CATEGORY).getAsString();
			}
			if (jsonCompanyInfo.get(FUND_FAMILY) != null) {
				final String fundFamily = jsonCompanyInfo.get(FUND_FAMILY).getAsString();
				company.setFund(true);
				industry = fundFamily;
			}
			final JsonElement marketCap = jsonCompanyInfo.get(NET_ASSETS);
			if (marketCap != null && StringUtils.isNotEmpty(marketCap.getAsString()) && !marketCap.getAsString().equals(NULL)) {
				company.setMarketCapitalization(marketCap.getAsString());
			}
		}
		if (StringUtils.isNotEmpty(industry)) {
			company.setIndustry(industry);
		}
		if (StringUtils.isNotEmpty(sector)) {
			company.setSector(sector);
		}
		return company;
	}

	@Override
	public final List<CurrencyData> getCurrencyData(final Currency currency) throws YahooException {
		final Currency[] currencies = Currency.values();
		final List<CurrencyData> currenciesData = new ArrayList<>();
		for (final Currency c : currencies) {
			if (c != currency) {
				final StringBuilder sb = new StringBuilder();
				sb.append("\"").append(currency.getCode()).append(c.getCode()).append("\",\"").append(c.getCode()).append(currency.getCode()).append("\"");
				final String request = "select * from yahoo.finance.xchange where pair in (" + sb + ")";
				final BaseResult baseResultDTO = (BaseResult) yahooGateway.getObject(request, BaseResult.class);
				if (baseResultDTO.getQuery().getResults().getRate() != null && baseResultDTO.getQuery().getResults().getRate().size() == 2) {
					final List<Rate> rates = baseResultDTO.getQuery().getResults().getRate();
					final CurrencyData currencyData = CurrencyData.builder()
						.currency1(currency)
						.currency2(c)
						.value(rates.get(0).getRate())
						.build();
					currenciesData.add(currencyData);
					final CurrencyData currencyData2 = CurrencyData.builder()
						.currency1(c)
						.currency2(currency)
						.value(rates.get(1).getRate())
						.build();
					currenciesData.add(currencyData2);
				} else {
					log.error("Could not find currency data: {}", baseResultDTO);
				}
			}
		}
		return currenciesData;
	}

	@Override
	public List<Company> getCompanyDataHistory(final String yahooId, final Date from, final Date to) throws YahooException {
		final String startDate = SIMPLE_DATE_FORMAT.format(from);
		final Calendar cal = Calendar.getInstance();
		final String endDate = to == null
			? SIMPLE_DATE_FORMAT.format(cal.getTime())
			: SIMPLE_DATE_FORMAT.format(to);
		final List<Company> companies = new ArrayList<>();
		final String request = "select * from yahoo.finance.historicaldata where symbol = \"" + yahooId + "\" and startDate = \"" + startDate + "\" and endDate = \"" + endDate + "\"";
		final JsonObject json = yahooGateway.getJSONObject(request);
		final JsonArray jsonResults = getJSONArrayFromJSONObject(json);
		for (int j = 0; j < jsonResults.size(); j++) {
			final JsonObject jsonIndex = (JsonObject) jsonResults.get(j);
			try {
				final Company company = Company.builder()
					.quote(jsonIndex.get(CLOSE).getAsDouble())
					.yahooId(yahooId)
					.build();
				companies.add(company);
			} catch (final Exception e) {
				log.warn("Error while trying to get double (Close) from json object: {}", jsonIndex);
			}
		}
		return companies;
	}

	@Override
	public final Index getIndexData(final String yahooId) throws YahooException {
		final String requestQuotes = "select * from yahoo.finance.quotes where symbol in ('" + yahooId + "')";

		final JsonObject json = yahooGateway.getJSONObject(requestQuotes);

		JsonObject jsonn;
		try {
			jsonn = json.getAsJsonObject(QUERY).getAsJsonObject(RESULTS).getAsJsonObject(QUOTE);
		} catch (final Exception e) {
			throw new YahooException(YahooException.ERROR, e);
		}

		return Index.builder()
			.yahooId(yahooId)
			.value(jsonn.get(LAST_TRADE_PRICE_ONLY).getAsDouble())
			.build();
	}

	/**
	 * Get a json array from a json object. Don't remember the goal of this function
	 *
	 * @param json the jsonObject
	 * @return a jsonArray
	 * @throws YahooException the yahooGatewayImpl exception
	 */
	private JsonArray getJSONArrayFromJSONObject(final JsonObject json) throws YahooException {
		final JsonObject jQuery = json.getAsJsonObject(QUERY);
		JsonArray quotes;
		if (jQuery != null) {
			final JsonObject jsonResults = jQuery.getAsJsonObject(RESULTS);
			if (jsonResults != null) {
				if (jsonResults.get(QUOTE).isJsonArray()) {
					quotes = jsonResults.getAsJsonArray(QUOTE);
				} else {
					final JsonObject quote = jsonResults.getAsJsonObject(QUOTE);
					if (quote == null) {
						final JsonObject error2 = json.getAsJsonObject(QUERY).getAsJsonObject(DIAGNOSTICS).getAsJsonObject(JAVASCRIPT);
						if (error2 == null) {
							throw new YahooException("Can't get the error message");
						} else {
							throw new YahooException(error2.getAsJsonObject("content").getAsString());
						}
					} else {
						quotes = new JsonArray();
						quotes.add(quote);
					}
				}
			} else {
				final JsonObject error = json.getAsJsonObject(QUERY).getAsJsonObject(DIAGNOSTICS).getAsJsonObject(JAVASCRIPT);
				if (error == null) {
					log.debug("JSONObject found: {}", json.getAsJsonObject(QUERY).getAsJsonObject(DIAGNOSTICS));
					throw new YahooException("The current table 'yahoo.finance.quotes' has probably been blocked.");
				} else {
					throw new YahooException(error.getAsJsonObject("content").getAsString());
				}
			}
		} else {
			throw new YahooException("Something went wrong. Query object null");
		}
		return quotes;
	}

	/**
	 * Format a list to json
	 *
	 * @param list a list
	 * @return a String json friendly
	 */
	private String getFormattedList(final List<String> list) {
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		for (final String str : list) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append("'").append(str).append("'");
			i++;
		}
		return sb.toString();
	}

	/**
	 * Guess the market from the id
	 *
	 * @param yahooId the yahooGatewayImpl id
	 * @return a Market
	 */
	private Market guessMarket(final String yahooId) {
		final String suffix = yahooId.substring(yahooId.indexOf('.') + 1, yahooId.length());
		return Market.getMarketFromSuffix(suffix);
	}
}
