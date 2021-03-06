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

package fr.cph.stock.service;

import fr.cph.stock.entities.Company;
import fr.cph.stock.entities.Equity;
import fr.cph.stock.exception.EquityException;
import fr.cph.stock.exception.YahooException;

public interface EquityService {

	void createEquity(int userId, String ticker, Equity equity) throws EquityException, YahooException;

	void createManualEquity(int userId, Company company, Equity equity) throws EquityException;

	void updateEquity(int userId, String ticker, Equity equity) throws YahooException;

	void deleteEquity(Equity equity);
}
