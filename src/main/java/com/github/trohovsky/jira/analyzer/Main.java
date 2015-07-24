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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * jira-analyzer main class.
 *
 * @author Tomas Rohovsky
 */
public final class Main {

	private static final String HELP_CMDLINE = "jira-analyzer [OPTION]... JIRA_SERVER JQL_QUERY_TEMPLATE"
			+ " PATH_TO_PARAMETER_FILE";
	private static final String HELP_HEADER = "Repeatitively queries a JIRA server with parametrized queries."
			+ " The parameters are stored in a CSV file.";
	private static final String CSV_DELIMITER = " ";

	private static JiraRestClient restClient;

	private Main() {
	}

	public static void main(String[] args) throws Exception {

		final Options options = new Options();
		options.addOption("u", true, "username");
		options.addOption("p", true, "password (optional, if not provided, the password is prompted)");
		options.addOption("h", false, "show this help");
		options.addOption("s", true,
				"use the strategy for querying and output, the strategy can be either 'issues_toatal' (default) or"
						+ " 'per_month'");
		options.addOption("d", true, "CSV delimiter");

		// parsing of the command line arguments
		final CommandLineParser parser = new DefaultParser();
		CommandLine cmdLine = null;
		try {
			cmdLine = parser.parse(options, args);
			if (cmdLine.hasOption('h') || cmdLine.getArgs().length == 0) {
				final HelpFormatter formatter = new HelpFormatter();
				formatter.setOptionComparator(null);
				formatter.printHelp(HELP_CMDLINE, HELP_HEADER, options, null);
				return;
			}
			if (cmdLine.getArgs().length != 3) {
				throw new ParseException(
						"You should specify exactly three arguments JIRA_SERVER JQL_QUERY_TEMPLATE"
								+ " PATH_TO_PARAMETER_FILE");
			}
		} catch (ParseException e) {
			System.err.println("Error parsing command line: " + e.getMessage());
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(HELP_CMDLINE, HELP_HEADER, options, null);
			return;
		}
		final String csvDelimiter = (String) (cmdLine.getOptionValue('d') != null ? cmdLine.getOptionObject('d')
				: CSV_DELIMITER);

		final URI jiraServerUri = URI.create(cmdLine.getArgs()[0]);
		final String jqlQueryTemplate = cmdLine.getArgs()[1];
		final List<List<String>> queryParametersData = readCSVFile(cmdLine.getArgs()[2], csvDelimiter);
		final String username = cmdLine.getOptionValue("u");
		String password = cmdLine.getOptionValue("p");
		final String strategy = cmdLine.getOptionValue("s");

		try {
			// initialization of the REST client
			final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
			if (username != null) {
				if (password == null) {
					final Console console = System.console();
					final char[] passwordCharacters = console.readPassword("Password: ");
					password = new String(passwordCharacters);
				}
				restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, username, password);
			} else {
				restClient = factory.create(jiraServerUri, new AnonymousAuthenticationHandler());
			}
			final SearchRestClient searchRestClient = restClient.getSearchClient();

			// choosing of an analyzer strategy
			AnalyzerStrategy analyzer = null;
			if (strategy != null) {
				switch (strategy) {
					case "issues_total":
						analyzer = new IssuesTotalStrategy(searchRestClient);
						break;
					case "issues_per_month":
						analyzer = new IssuesPerMonthStrategy(searchRestClient);
						break;
					default:
						System.err.println("The strategy does not exist");
						return;
				}
			} else {
				analyzer = new IssuesTotalStrategy(searchRestClient);
			}

			// analyzing
			for (List<String> queryParameters : queryParametersData) {
				analyzer.analyze(jqlQueryTemplate, queryParameters);
			}
		} finally {
			// destroy the REST client, otherwise it stucks
			restClient.destroy();
		}
	}

	private static List<List<String>> readCSVFile(String filepath, String delimiter) {
		final List<List<String>> data = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				data.add(Arrays.asList(line.split(delimiter)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
}
