/**
 * Copyright 2017 Carl-Philipp Harmant
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

package fr.cph.stock.entities.chart;

import fr.cph.stock.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class represents an time chart
 *
 * @author Carl-Philipp Harmant
 *
 */
public class TimeValueChart extends AChart {

	/** Starting date **/
	private final Date date;
	/** Portfolio value **/
	private final Map<Date, Double> portfolioValue;
	/** Liqudity **/
	private final Map<Date, Double> liquidity;

	/**
	 * Constructor
	 *
	 * @param portfolioValue
	 *            the portfolio value
	 * @param liquidity
	 *            the liquidity
	 * @param date
	 *            the starting date
	 */
	public TimeValueChart(final Map<Date, Double> portfolioValue, final Map<Date, Double> liquidity, final Date date) {
		this.portfolioValue = portfolioValue;
		this.liquidity = liquidity;
		this.date = (Date) date.clone();
	}

	@Override
	public final void generate() {
		StringBuilder dataTemp = new StringBuilder();
		StringBuilder drawTemp = new StringBuilder();
		dataTemp.append("d1 = [");
		drawTemp.append("[{data: d1, label: 'My portfolio value'}");
		int i = 1;
		for (Entry<Date, Double> e : portfolioValue.entrySet()) {
			if (i != 1) {
				dataTemp.append(",");
			}
			dataTemp.append("[").append(e.getKey().getTime()).append(",").append(e.getValue()).append("]");
			i++;
		}
		dataTemp.append("]");
		dataTemp.append(",");
		dataTemp.append("\nd2 = [");
		int j = 1;
		for (Entry<Date, Double> e : liquidity.entrySet()) {
			if (j != 1) {
				dataTemp.append(",");
			}
			dataTemp.append("[").append(e.getKey().getTime()).append(",").append(e.getValue()).append("]");
			j++;
		}
		dataTemp.append("]");
		drawTemp.append(",");
		drawTemp.append("{data: d2, label: 'My liquidities'}");
		drawTemp.append("]");
		setData(dataTemp.toString());
		setDraw(drawTemp.toString());
		generateColors();
	}

	/**
	 * Generate colors
	 */
	private void generateColors() {
		final List<String> colorsList = new ArrayList<>();
		colorsList.add("#3e933d");
		colorsList.add("#190525");
		colorsList.add("#6a0efc");
		colorsList.add("#FF0000");
		colorsList.add("#FFCC00");
		final List<String> colorsListRes = Util.getRandomColors(2, colorsList);
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String s : colorsListRes) {
			sb.append("'").append(s).append("'");
			if (i != colorsList.size()) {
				sb.append(",");
			}
		}
		setColors(sb.toString());
	}

	/**
	 * Get date
	 *
	 * @return a date
	 */
	public final Date getDate() {
		if (date != null) {
			return (Date) date.clone();
		} else {
			return null;
		}
	}

}
