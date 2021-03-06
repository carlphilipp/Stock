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

package fr.cph.stock.repository;

import fr.cph.stock.entities.Equity;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This class implements DAO functions and add some more. It access to the Equity in DB.
 *
 * @author Carl-Philipp Harmant
 */
@RequiredArgsConstructor
@Component
public class EquityRepository implements DAO<Equity> {

	private static final String INSERT = "fr.cph.stock.repository.EquityRepository.insertOneEquity";
	private static final String SELECT = "fr.cph.stock.repository.EquityRepository.selectOneEquity";
	private static final String UPDATE = "fr.cph.stock.repository.EquityRepository.updateOneEquity";
	private static final String DELETE = "fr.cph.stock.repository.EquityRepository.deleteOneEquity";

	@NonNull
	private final SqlSession session;

	@Override
	public final void insert(final Equity equity) {
		session.insert(INSERT, equity);
	}

	@Override
	public final Optional<Equity> select(final int id) {
		return Optional.ofNullable(session.selectOne(SELECT, id));
	}

	@Override
	public final void update(final Equity equity) {
		session.update(UPDATE, equity);
	}

	@Override
	public final void delete(final Equity equity) {
		session.delete(DELETE, equity);
	}
}
