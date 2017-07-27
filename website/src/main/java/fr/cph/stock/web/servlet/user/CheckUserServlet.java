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

package fr.cph.stock.web.servlet.user;

import fr.cph.stock.service.UserService;
import fr.cph.stock.entities.User;
import fr.cph.stock.security.SecurityService;
import lombok.extern.log4j.Log4j2;
import fr.cph.stock.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This servlet is called when the user is registering
 *
 * @author Carl-Philipp Harmant
 */
// FIXME to delete
@Log4j2
//@WebServlet(name = "CheckUserServlet", urlPatterns = {"/check"})
public class CheckUserServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private UserService userService;
	private SecurityService securityService;

	@Override
	public final void init() {
	}

	@Override
	protected final void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
		try {
			final String login = request.getParameter(Constants.LOGIN);
			final String check = request.getParameter(Constants.CHECK);
			final Optional<User> userOptional = userService.getUser(login);
			if (userOptional.isPresent()) {
				final User user = userOptional.get();
				final String serverCheck = securityService.encodeToSha256(user.getLogin() + user.getPassword() + user.getEmail());
				if (check.equals(serverCheck)) {
					userService.validateUser(login);
					request.setAttribute(Constants.MESSAGE, "It worked!<br>You can now <a href='index.jsp'>login</a>");
				} else {
					request.setAttribute(Constants.MESSAGE, "Sorry, it did not work");
				}
				request.setAttribute(Constants.USER, userOptional);
			} else {
				request.setAttribute(Constants.MESSAGE, "Sorry, it did not work");
			}
			request.getRequestDispatcher("/jsp/check.jsp").forward(request, response);
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