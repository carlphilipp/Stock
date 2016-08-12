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

package fr.cph.stock.web.servlet.user;

import fr.cph.stock.business.Business;
import fr.cph.stock.business.impl.BusinessImpl;
import fr.cph.stock.entities.User;
import fr.cph.stock.security.Security;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static fr.cph.stock.util.Constants.CHECK;
import static fr.cph.stock.util.Constants.ERROR;
import static fr.cph.stock.util.Constants.LOGIN;

/**
 * This servlet is called when a new password is asked
 *
 * @author Carl-Philipp Harmant
 *
 */
@WebServlet(name = "NewPasswordServlet", urlPatterns = { "/newpassword" })
public class NewPasswordServlet extends HttpServlet {

	private static final long serialVersionUID = -4548932564405559822L;
	private static final Logger LOG = Logger.getLogger(NewPasswordServlet.class);
	private Business business;

	@Override
	public final void init() {
		this.business = BusinessImpl.INSTANCE;
	}

	@Override
	protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		try {
			final String login = request.getParameter(LOGIN);
			final String check = request.getParameter(CHECK);
			final User user = business.getUser(login);
			if (user != null) {
				final String checkServer = Security.encodeToSha256(user.getLogin() + user.getPassword() + user.getEmail());
				if (check.equals(checkServer)) {
					request.setAttribute(LOGIN, user.getLogin());
				} else {
					request.setAttribute(ERROR, "Error while checking id");
				}
			} else {
				request.setAttribute(ERROR, "Error while checking id");
			}
			request.getRequestDispatcher("jsp/newpassword.jsp").forward(request, response);
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
