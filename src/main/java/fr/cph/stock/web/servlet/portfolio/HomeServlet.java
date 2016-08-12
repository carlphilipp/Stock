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

package fr.cph.stock.web.servlet.portfolio;

import fr.cph.stock.business.Business;
import fr.cph.stock.business.impl.BusinessImpl;
import fr.cph.stock.cron.Job;
import fr.cph.stock.entities.Index;
import fr.cph.stock.entities.Portfolio;
import fr.cph.stock.entities.User;
import fr.cph.stock.enumtype.Currency;
import fr.cph.stock.exception.YahooException;
import fr.cph.stock.language.LanguageFactory;
import fr.cph.stock.util.Info;
import fr.cph.stock.web.servlet.CookieManagement;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static fr.cph.stock.util.Constants.*;

/**
 * Home servlet
 *
 * @author Carl-Philipp Harmant
 *
 */
@WebServlet(name = "HomeServlet", urlPatterns = { "/home" }, loadOnStartup = 1)
public class HomeServlet extends HttpServlet {

	private static final long serialVersionUID = 122322259823208331L;
	private static final Logger LOG = Logger.getLogger(HomeServlet.class);
	private Business business;
	private LanguageFactory language;

	@Override
	public final void init() throws ServletException {
		try {
			final InetAddress inetAddress = InetAddress.getLocalHost();
			final String hostName = inetAddress.getHostName();
			if (!hostName.equals("carl-Laptop")) {
				final Job job = new Job();
				job.run();
			}
		} catch (final UnknownHostException | SchedulerException e) {
			LOG.error(e.getMessage(), e);
		}

		this.business = BusinessImpl.INSTANCE;
		this.language = LanguageFactory.INSTANCE;
	}

	@Override
	protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		try {
			final HttpSession session = request.getSession(false);
			final User user = (User) session.getAttribute(USER);
			final String day = request.getParameter(DAYS);
			try {
				final Portfolio portfolio;
				if (day != null) {
					final int days = Integer.parseInt(day);
					final Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DATE, -days);
					portfolio = business.getUserPortfolio(user.getId(), cal.getTime(), null);
				} else {
					portfolio = business.getUserPortfolio(user.getId(), null, null);
				}
				if (portfolio.getShareValues().size() != 0) {
					final Date from = portfolio.getShareValues().get(portfolio.getShareValues().size() - 1).getDate();
					final List<Index> indexesCAC40 = business.getIndexes(Info.YAHOOID_CAC40, from, null);
					final List<Index> indexesSP500 = business.getIndexes(Info.YAHOOID_SP500, from, null);
					portfolio.addIndexes(indexesCAC40);
					portfolio.addIndexes(indexesSP500);
				}
				request.setAttribute(PORTFOLIO, portfolio);
			} catch (final YahooException e) {
				LOG.error("Error: " + e.getMessage(), e);
			}
			final String lang = CookieManagement.getCookieLanguage(Arrays.asList(request.getCookies()));
			request.setAttribute(LANGUAGE, language.getLanguage(lang));
			request.setAttribute(APP_TITLE, Info.NAME + " &bull; Portfolio");
			request.setAttribute(CURRENCIES, Currency.values());
			request.getRequestDispatcher("jsp/home.jsp").forward(request, response);
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
