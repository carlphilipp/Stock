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

import fr.cph.stock.entities.Account;
import fr.cph.stock.entities.Portfolio;
import fr.cph.stock.entities.User;
import fr.cph.stock.exception.LoginException;
import fr.cph.stock.exception.YahooException;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Optional;

public interface UserService {

	void createUser(String login, String md5Password, String email) throws NoSuchAlgorithmException, UnsupportedEncodingException, LoginException;

	Optional<User> getUser(String login);

	Optional<User> getUserWithEmail(String email);

	Optional<User> checkUser(String login, String md5Password) throws LoginException;

	Optional<Portfolio> getUserPortfolio(int userId) throws YahooException;

	Optional<Portfolio> getUserPortfolio(int userId, Date from) throws YahooException;

	Optional<Portfolio> getUserPortfolio(int userId, Date from, Date to) throws YahooException;

	void validateUser(String login);

	void updateUser(User user);

	void updateOneUserPassword(User user);

	void updatePortfolio(Portfolio portfolio);

	void updateLiquidity(Account account, double liquidity);
}
