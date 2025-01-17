package com.dss.poc.metrics

import com.twitter.util.Await
import roc.postgresql.{Request, Result}
import roc.{Postgresql, postgresql}


case class PostgresAndQueryProcessing(id: String, client: postgresql.Client, request: Request, postProcessing: Result => Unit = _ => Unit) extends QueryAndPostProcessing {
  override def process: (String, Long) = {
    val startTime = System.currentTimeMillis()
    val result = Await.result(client.query(request))
    postProcessing(result)
    val totalTime = System.currentTimeMillis() - startTime
    (id, totalTime)
  }
}

object PostgresQueryHandler {

  def getQuery(name: String): QueryAndPostProcessing = {
    name match {
      case "asset_distribution_by_tag" => makeAssetDistributionByTagQuery
      case "sensitive_data_access" => makeSensitiveDataAccessQuery
      case "access_per_days" => accessPerDays
    }
  }

  val connectionString = "inet!172.22.65.107:5432"
  val database = "poc"

  val client: postgresql.Client = Postgresql.client
    .withUserAndPasswd("postgres", "")
    .withDatabase(database)
    .newRichClient(connectionString)


  def makeAssetDistributionByTagQuery: QueryAndPostProcessing = {
    val query = "select tag,count(distinct(tablename)) as " +
      "number_of_tables from sensitivity group " +
      "by tag order by number_of_tables limit 10;"
    PostgresAndQueryProcessing("asset_distribution_by_tag", client, Request(query))

  }

  def makeSensitiveDataAccessQuery: QueryAndPostProcessing = {
    val triplet = TestParamGenerator.getDateRangeAndCollection
    val query = "select sum(allowed) as allowed,sum(denied) as denied from\n" +
      "(select sensistive_tables.tablename as tablename from (\n" +
      s"(select tablename from collections where collection='${triplet._3}') " +
      "AS table_in_collection\nINNER JOIN " +
      "\n(select distinct tablename from sensitivity) AS sensistive_tables " +
      "\nON sensistive_tables.tablename=table_in_collection.tablename\n) \n)" +
      "AS sensitive_tables_in_collection \nINNER JOIN  " +
      s"\n(select *  from audit where date>='${triplet._1}' and date<='${triplet._2}') " +
      "\nAS audit_filtered " +
      "\nON audit_filtered.tablename=sensitive_tables_in_collection.tablename;"
    PostgresAndQueryProcessing("sensitive_data_access", client, Request(query))
  }


  def accessPerDays: QueryAndPostProcessing = {
    val range = TestParamGenerator.getDateRange
    val query = "select finaltable.date,sum(finaltable.allowed) as access " +
      "" +
      "from (\n(select distinct tablename from sensitivity) " +
      "\nAS sensitivity_tables\n " +
      "INNER JOIN \n " +
      "(select date,tablename,sum(allowed) as allowed from audit " +
      "where " +
      s"date>='${range._1}' and date<='${range._2}' " +
      "group by tablename,date)\n  " +
      "AS tableaccess ON tableaccess.tablename=sensitivity_tables.tablename\n  ) " +
      "AS finaltable group by finaltable.date order by access limit 10"

    PostgresAndQueryProcessing("access_per_days", client, Request(query))
  }

}
