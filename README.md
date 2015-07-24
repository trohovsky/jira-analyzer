jira-analyzer
=============
This tool enables you to query a JIRA server with a query you want. It queries the server repeatetively with parametrized queries. The parameters are stored in a CVS file.

Usage
-----

1. Create a CVS file containing the parameters you will use for querying. Some examples how to create it:

```
wget https://camel.apache.org/components.html; grep -o '\camel-[a-z0-9-]*<' components.html | tr -d '<' | sort | uniq > components.txt
ls -1 camel/components > modules.txt
```
<br/>
2. Run jira-analyzer with these arguments:

```
JIRA_SERVER JQL_QUERY_TEMPLATE PATH_TO_PARAMETER_FILE.
```

For instance:

```
java -jar jira-analyzer-[VERSION]-jar-with-dependencies.jar https://issues.apache.org/jira 'project=CAMEL AND text ~ "%s" ORDER BY created ASC' $SOME_PATH/modules.txt -s issues_per_month
```

Results
-------

Format of the results depends on a querying/reporting strategy you choose. For instance, when using 'isseus_per_month' strategy you will get:

```
camel-ahc 25 04-01-2010 0.37878788
camel-ahc-ws 4 20-03-2014 0.25
camel-amqp 14 05-10-2007 0.15053764
```

where the columns are:

```
QUERY_PARAMETER+ NUMBER_OF_ISSUES CREATION_DATE_OF_FIRST_ISSUE ISSUES_PER_MONTH
```

Requirements
------------
- Maven 3.x
- Java 8
