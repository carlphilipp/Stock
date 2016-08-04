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

package fr.cph.stock.web.servlet.share;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;

import fr.cph.stock.business.Business;
import fr.cph.stock.business.IBusiness;
import fr.cph.stock.csv.Csv;
import fr.cph.stock.entities.Account;
import fr.cph.stock.entities.Portfolio;
import fr.cph.stock.entities.ShareValue;
import fr.cph.stock.entities.User;

/**
 * Creat history from CSV file
 * 
 * @author Carl-Philipp Harmant
 * 
 */
@WebServlet(name = "CreateHistoryServlet", urlPatterns = { "/createhistory" })
@MultipartConfig
public class CreateHistoryServlet extends HttpServlet {

	/** Serialization **/
	private static final long serialVersionUID = -2999218921595727810L;
	/** Logger **/
	private static final Logger LOG = Logger.getLogger(CreateHistoryServlet.class);
	/** Business **/
	private IBusiness business;

	@Override
	public final void init() {
		business = Business.getInstance();
	}

	@Override
	protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		try {
			HttpSession session = request.getSession(false);
			User user = (User) session.getAttribute("user");
			String liquidity = request.getParameter("liquidity");
			String acc = request.getParameter("account");

			Portfolio portfolio = business.getUserPortfolio(user.getId(), null, null);
			Account account = portfolio.getAccount(acc);

			Part p1 = request.getPart("file");
			InputStream is = p1.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			Csv csv = new Csv(br, user, acc);
			List<ShareValue> shareValues = csv.getShareValueList();
			for (ShareValue sv : shareValues) {
				business.addShareValue(sv);
			}
			if (!liquidity.equals("")) {
				business.updateLiquidity(account, Double.parseDouble(liquidity));
			}
			request.getRequestDispatcher("sharevalue?page=1").forward(request, response);
		} catch (Throwable t) {
			LOG.error(t.getMessage(), t);
			throw new ServletException("Error: " + t.getMessage(), t);
		}
	}

	@Override
	protected final void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		doGet(request, response);
	}
}