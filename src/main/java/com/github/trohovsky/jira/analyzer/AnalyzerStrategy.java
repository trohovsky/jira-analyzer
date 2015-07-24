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

import java.util.List;

/**
 * A strategy that is used for querying and reporting of the results.
 *
 * @author Tomas Rohovsky
 */
public interface AnalyzerStrategy {

	/**
	 * Queries a JIRA server and reports the results.
	 * @param jqlQueryTemplate the JQL query template
	 * @param queryParameters the query parameters
	 */
	void analyze(String jqlQueryTemplate, List<String> queryParameters);
}
