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

package fr.cph.stock.dao;

import fr.cph.stock.entities.User;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * This class implements IDAO functions and add some more. It access to the User in DB.
 * 
 * @author Carl-Philipp Harmant
 * 
 */
public class UserDAO extends AbstractDAO<User> {

	@Override
	public final void insert(final User user) {
		final SqlSession session = getSqlSessionFactory();
		try {
			session.insert("UserDao.insertOneUser", user);
			session.commit();
		} finally {
			session.close();
		}
	}

	@Override
	public final User select(final int id) {
		final SqlSession session = getSqlSessionFactory();
		try {
			return session.selectOne("UserDao.selectOneUser", id);
		} finally {
			session.close();
		}
	}

	@Override
	public final void update(final User user) {
		final SqlSession session = getSqlSessionFactory();
		try {
			session.update("UserDao.updateOneUser", user);
			session.commit();
		} finally {
			session.close();
		}
	}

	/**
	 * Update one user password
	 * 
	 * @param user
	 *            the user
	 */
	public final void updateOneUserPassword(final User user) {
		final SqlSession session = getSqlSessionFactory();
		try {
			session.update("UserDao.updateOneUserPassword", user);
			session.commit();
		} finally {
			session.close();
		}
	}

	@Override
	public final void delete(final User user) {
		final SqlSession session = getSqlSessionFactory();
		try {
			session.delete("UserDao.deleteOneUser", user);
			session.commit();
		} finally {
			session.close();
		}
	}

	/**
	 * Get a user with its login
	 * 
	 * @param login
	 *            the login
	 * @return a user
	 */
	public final User selectWithLogin(final String login) {
		final SqlSession session = getSqlSessionFactory();
		try {
			return session.selectOne("UserDao.selectOneUserWithLogin", login);
		} finally {
			session.close();
		}
	}

	/**
	 * Get a user with its email
	 * 
	 * @param email
	 *            the email
	 * @return a user
	 */
	public final User selectWithEmail(final String email) {
		final SqlSession session = getSqlSessionFactory();
		try {
			return session.selectOne("UserDao.selectOneUserWithEmail", email);
		} finally {
			session.close();
		}
	}

	/**
	 * Get all users
	 * 
	 * @return a list of user
	 */
	public final List<User> selectAllUsers() {
		final SqlSession session = getSqlSessionFactory();
		try {
			return session.selectList("UserDao.selectAllUsers");
		} finally {
			session.close();
		}
	}
}