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

package fr.cph.stock.web.servlet.performance;

import fr.cph.stock.business.IndexBusiness;
import fr.cph.stock.business.UserBusiness;
import fr.cph.stock.entities.Index;
import fr.cph.stock.entities.Portfolio;
import fr.cph.stock.entities.User;
import fr.cph.stock.entities.chart.PieChart;
import fr.cph.stock.entities.chart.TimeChart;
import fr.cph.stock.exception.NotFoundException;
import fr.cph.stock.exception.YahooException;
import fr.cph.stock.guice.GuiceInjector;
import fr.cph.stock.language.LanguageFactory;
import fr.cph.stock.report.PdfReport;
import fr.cph.stock.util.Info;
import fr.cph.stock.util.Util;
import fr.cph.stock.web.servlet.CookieManagement;
import lombok.extern.log4j.Log4j2;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static fr.cph.stock.util.Constants.*;

/**
 * This servlet is called when the user want to access to the performance page
 *
 * @author Carl-Philipp Harmant
 */
@Log4j2
@WebServlet(name = "PerformanceServlet", urlPatterns = {"/performance"})
public class PerformanceServlet extends HttpServlet {

	private static final long serialVersionUID = 2435465891228710040L;
	private UserBusiness userBusiness;
	private IndexBusiness indexBusiness;
	private LanguageFactory language;

	@Override
	public final void init() throws ServletException {
		indexBusiness = GuiceInjector.INSTANCE.getIndexBusiness();
		userBusiness = GuiceInjector.INSTANCE.getUserBusiness();
		language = LanguageFactory.INSTANCE;
	}

	@Override
	protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		final HttpSession session = request.getSession(false);
		final User user = (User) session.getAttribute(USER);
		Portfolio portfolio = null;
		try {
			final String createPdf = request.getParameter(PDF);
			try {
				final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				final String fromParameter = request.getParameter(FROM);
				final String toParameter = request.getParameter(TO);

				final Date fromDate = StringUtils.isNotEmpty(fromParameter) ? formatter.parse(fromParameter) : null;
				final Date toDate = StringUtils.isNotEmpty(toParameter) ? formatter.parse(toParameter) : null;

				portfolio = userBusiness.getUserPortfolio(user.getId(), fromDate, toDate).orElseThrow(() -> new NotFoundException(user.getId()));
				if (portfolio.getShareValues().size() != 0) {
					Date from = portfolio.getShareValues().get(portfolio.getShareValues().size() - 1).getDate();
					// Reset time to 17:00PM to get also the cac40 into the day selected (or it would not select it
					from = Util.resetHourMinSecMill(from);
					// Put 17:00PM to the first sharevalue, to make it nice in graphic
					portfolio.getShareValues().get(portfolio.getShareValues().size() - 1).setDate(from);

					final List<Index> indexesCAC40 = indexBusiness.getIndexes(Info.YAHOO_ID_CAC40, from, toDate);
					final List<Index> indexesSP500 = indexBusiness.getIndexes(Info.YAHOO_ID_SP500, from, toDate);
					portfolio.addIndexes(indexesCAC40);
					portfolio.addIndexes(indexesSP500);
					portfolio.compute();

					Date fro = from;
					if (indexesCAC40.size() > 0) {
						final Date date = indexesCAC40.get(0).getDate();
						if (date.before(fro)) {
							fro = date;
						}
					}
					if (indexesSP500.size() > 0) {
						final Date date2 = indexesSP500.get(0).getDate();
						if (date2.before(fro)) {
							fro = date2;
						}
					}

					Date t = portfolio.getShareValues().get(0).getDate();
					if (indexesCAC40.size() > 1) {
						final Date date3 = indexesCAC40.get(indexesCAC40.size() - 1).getDate();
						if (date3.after(t)) {
							t = date3;
						}
					}
					if (indexesSP500.size() > 1) {
						final Date date = indexesSP500.get(indexesSP500.size() - 1).getDate();
						if (date.after(t)) {
							t = date;
						}
					}
					request.setAttribute(FROM_UNDERSCORE, fro);
					request.setAttribute(TO_UNDERSCORE, t);
				}

				request.setAttribute(PORTFOLIO, portfolio);
			} catch (final YahooException e) {
				log.error("Error: {}", e.getMessage(), e);
			}
			if (createPdf != null && createPdf.equals("pdf")) {
				final Image sectorChart = PdfReport.createPieChart((PieChart) portfolio.getPieChartSector(), "Sector Chart");
				final Image capChart = PdfReport.createPieChart((PieChart) portfolio.getPieChartCap(), "Cap Chart");
				final Image timeChart = PdfReport.createTimeChart((TimeChart) portfolio.getTimeChart(), "Share value");
				final PdfReport pdf = new PdfReport(Info.REPORT);
				pdf.addParam(PORTFOLIO, portfolio);
				pdf.addParam(EQUITIES, portfolio.getEquities());
				pdf.addParam(USER, user);
				pdf.addParam(SECTOR_PIE, sectorChart);
				pdf.addParam(CAP_PIE, capChart);
				pdf.addParam(SHARE_VALUE_PIE, timeChart);
				response.setContentType("application/pdf");
				final DateFormat df = new SimpleDateFormat("dd-MM-yy");
				final String formattedDate = df.format(new Date());
				response.addHeader("Content-Disposition", "attachment; filename=" + user.getLogin() + formattedDate + ".pdf");
				try (final OutputStream responseOutputStream = response.getOutputStream()) {
					JasperExportManager.exportReportToPdfStream(pdf.getReport(), responseOutputStream);
				} catch (final JRException e) {
					throw new ServletException("Error: " + e.getMessage(), e);
				}
			} else {
				final String lang = CookieManagement.getCookieLanguage(Arrays.asList(request.getCookies()));
				request.setAttribute(LANGUAGE, language.getLanguage(lang));
				request.setAttribute(APP_TITLE, Info.NAME + " &bull;   Performance");
				request.getRequestDispatcher("jsp/performance.jsp").forward(request, response);
			}
		} catch (final Throwable t) {
			log.error(t.getMessage(), t);
			throw new ServletException("Error: " + t.getMessage(), t);
		}
	}

	@Override
	protected final void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		doGet(request, response);
	}
}
