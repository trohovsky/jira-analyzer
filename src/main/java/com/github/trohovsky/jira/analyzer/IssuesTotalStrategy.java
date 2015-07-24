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

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;

import java.util.List;

/**
 * It queries a JIRA and reports the results. The results are in this format:
 * query_parameter+ number_of_issues
 *
 * @author Tomas Rohovsky
 */
public class IssuesTotalStrategy implements AnalyzerStrategy {

	private SearchRestClient searchRestClient;

	public IssuesTotalStrategy(SearchRestClient searchRestClient) {
		this.searchRestClient = searchRestClient;
	}

	@Override
	public void analyze(String jqlQueryTemplate, List<String> queryParameters) {
		final String jqlQuery = String.format(jqlQueryTemplate, queryParameters.toArray());
		final SearchResult searchResult = searchRestClient.searchJql(jqlQuery, 0, 0).claim();

		System.out.println(String.format("%s %s", String.join(" ", queryParameters), searchResult.getTotal()));
	}
}
