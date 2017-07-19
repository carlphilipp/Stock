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

package fr.cph.stock.web.servlet;

import fr.cph.stock.entities.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static fr.cph.stock.util.Constants.USER;

/**
 * This classes is called each time the user try to access a page that needs to be logged in (basicely, every single page) It
 * checks if the session is valid or not
 *
 * @author Carl-Philipp Harmant
 */
public class SessionFilter implements Filter {

	@Override
	public final void init(final FilterConfig config) {
	}

	@Override
	public final void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;
		final HttpSession session = request.getSession(false);

		if (session == null) {
			response.sendRedirect("timeout");
		} else {
			final User user = (User) session.getAttribute(USER);
			if (user == null) {
				response.sendRedirect("timeout");
			} else {
				chain.doFilter(req, res);
			}
		}
	}

	@Override
	public void destroy() {
	}
}
