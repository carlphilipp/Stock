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

package fr.cph.stock.cron;

import fr.cph.stock.business.impl.BusinessImpl;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import fr.cph.stock.business.Business;

/**
 * Job that update companies that don't have any real time data.
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class CompanyNotRealTimeJob implements Job {

	/** Logger **/
	private static final Logger LOG = Logger.getLogger(CompanyNotRealTimeJob.class);
	/** **/
	private Business business;

	/** Constructor **/
	public CompanyNotRealTimeJob() {
		business = BusinessImpl.getInstance();
	}

	@Override
	public final void execute(final JobExecutionContext context) {
		try {
			business.updateCompaniesNotRealTime();
		} catch (Throwable t) {
			LOG.error("Error while executing CompanyNotRealTimeJob: " + t.getMessage(), t);
		}
	}

}
