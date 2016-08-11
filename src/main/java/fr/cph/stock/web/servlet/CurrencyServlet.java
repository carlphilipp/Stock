/**
 * Copyright 2013 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.stock.web.servlet;

import fr.cph.stock.business.Business;
import fr.cph.stock.business.impl.BusinessImpl;
import fr.cph.stock.entities.Portfolio;
import fr.cph.stock.entities.User;
import fr.cph.stock.exception.YahooException;
import fr.cph.stock.language.LanguageFactory;
import fr.cph.stock.util.Info;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

import static fr.cph.stock.util.Constants.*;

/**
 * This servlet is called to access the currency page
 *
 * @author Carl-Philipp Harmant
 *
 */
@WebServlet(name = "CurrencyServlet", urlPatterns = { "/currencies" })
public class CurrencyServlet extends HttpServlet {

	private static final long serialVersionUID = 8821408830626147089L;
	private static final Logger LOG = Logger.getLogger(CurrencyServlet.class);
	private Business business;
	private LanguageFactory language;

	@Override
	public final void init() throws ServletException {
		this.business = BusinessImpl.getInstance();
		this.language = LanguageFactory.getInstance();
	}

	@Override
	protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		try {
			final HttpSession session = request.getSession(false);
			final User user = (User) session.getAttribute(USER);
			final String update = request.getParameter(UPDATE);
			if (update != null) {
				final Portfolio portfolio = business.getUserPortfolio(user.getId(), null, null);
				try {
					business.updateOneCurrency(portfolio.getCurrency());
					request.setAttribute(MESSAGE, "Done !");
				} catch (final YahooException e) {
					request.setAttribute(ERROR, e.getMessage());
				}
			}
			final Portfolio portfolio = business.getUserPortfolio(user.getId(), null, null);
			final Object[][] tab = business.getAllCurrencyData(portfolio.getCurrency());
			request.setAttribute(PORTFOLIO, portfolio);
			request.setAttribute(TAB, tab);
			final String lang = CookieManagement.getCookieLanguage(Arrays.asList(request.getCookies()));
			request.setAttribute(LANGUAGE, language.getLanguage(lang));
			request.setAttribute(APP_TITLE, Info.NAME + " &bull;   Currencies");
			request.getRequestDispatcher("jsp/currencies.jsp").forward(request, response);
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			throw new ServletException("Error: " + t.getMessage(), t);
		}
	}

	@Override
	protected final void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		doGet(request, response);
	}

}
