/*
 * Copyright 2015 Tomas Rohovsky
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
package com.github.trohovsky.jira.analyzer;

import org.joda.time.DateTime;
import org.joda.time.Months;

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * It queries a JIRA and reports the results. The results are in this format:
 * query_parameter+ number_of_issues creation_date_of_first_issue issues_per_month
 *
 * issues_per_month is computed from creation_date_of_first_issue
 *
 * @author Tomas Rohovsky
 */
public class IssuesPerMonthStrategy implements AnalyzerStrategy {

	private SearchRestClient searchRestClient;

	public IssuesPerMonthStrategy(SearchRestClient searchRestClient) {
		this.searchRestClient = searchRestClient;
	}

	@Override
	public void analyze(String jqlQueryTemplate, List<String> queryParameters) {
		String jqlQuery = String.format(jqlQueryTemplate, queryParameters.toArray());
		// adjust the query to correctly order the results, it's required due to creation date
		int indexOfOrderBy = jqlQuery.indexOf("ORDER BY");
		if (indexOfOrderBy != -1) {
			jqlQuery = jqlQuery.substring(0, indexOfOrderBy - 1);
		}
		jqlQuery += " ORDER BY created ASC";

		// TODO JRJC-205 Problem when searching issues with 'fields' parameter set
		final SearchResult searchResult = searchRestClient.searchJql(jqlQuery, 1, 0, null).claim();

		DateTime firstIssueCreationDate = null;
		if (searchResult.getIssues().iterator().hasNext()) {
			final Issue firstIssue = searchResult.getIssues().iterator().next();
			firstIssueCreationDate = firstIssue.getCreationDate();
		}
		int monthDiff = 0;
		if (firstIssueCreationDate != null) {
			final DateTime today = new DateTime();
			final Months mt = Months.monthsBetween(firstIssueCreationDate, today);
			monthDiff = mt.getMonths();
		}
		final float issuesPerMonth = (float) searchResult.getTotal() / monthDiff;
		final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		String firstIssueCreationDateString = null;
		if (firstIssueCreationDate != null) {
			firstIssueCreationDateString = formatter.format(firstIssueCreationDate.toDate());
		} else {
			firstIssueCreationDateString = "None";
		}
		System.out.println(String.format("%s %s %s %s", String.join(" ", queryParameters), searchResult.getTotal(),
				firstIssueCreationDateString, issuesPerMonth));
	}
}
