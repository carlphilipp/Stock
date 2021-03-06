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

package fr.cph.stock.service.impl;

import fr.cph.stock.service.AccountService;
import fr.cph.stock.repository.AccountRepository;
import fr.cph.stock.entities.Account;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * AccountServiceImpl class that access database and process data
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class AccountServiceImpl implements AccountService {

	@NonNull
	private final AccountRepository accountRepository;

	@Override
	public Optional<Account> getAccount(final int id) {
		return accountRepository.select(id);
	}

	@Override
	public void addAccount(final Account account) {
		accountRepository.insert(account);
	}

	@Override
	public void updateAccount(final Account account) {
		accountRepository.update(account);
	}

	@Override
	public void deleteAccount(final Account account) {
		accountRepository.delete(account);
	}
}
