package com.dss.poc.metrics

import java.util
import java.util.Optional

import org.apache.solr.client.solrj.impl.CloudSolrClient
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.{SolrClient, SolrQuery}


trait QueryAndPostProcessing {
  def process: (String, Long)
}


case class SolrQueryAndPostProcessing(id: String, collection: String, client: SolrClient, query: SolrQuery, postProcessing: QueryResponse => Unit) extends QueryAndPostProcessing {
  override def process: (String, Long) = {
    val startTime = System.currentTimeMillis()
    val response = client.query(collection, query)
    postProcessing(response)
    val totalTime = System.currentTimeMillis() - startTime
    (id, totalTime)
  }
}

case class MultiQuery(id: String, queries: List[QueryAndPostProcessing]) extends QueryAndPostProcessing {
  override def process: (String, Long) = {
    val startTime = System.currentTimeMillis()
    queries.foreach(_.process)
    val totalTime = System.currentTimeMillis() - startTime
    (id, totalTime)
  }
}

object SolrQueryHandler {

  val connectionString = "172.22.65.110:2181"


  val list = new util.ArrayList[String]()
  list.add(connectionString)

  val client: SolrClient = new CloudSolrClient.Builder(list, Optional.empty[String]).withSocketTimeout(30000).withConnectionTimeout(15000).build()


  def getQuery(name: String): QueryAndPostProcessing = {
    name match {
      case "asset_distribution_by_tag" => makeAssetDistributionByTagQuery
      case "sensitive_data_access" => makeSensitiveDataAccessQuery
      case "access_per_days" => accessPerDays
    }
  }


  def makeAssetDistributionByTagQuery: QueryAndPostProcessing = {

    val querypre = new SolrQuery
    querypre.setQuery("*:*")
    querypre.set("stats", true)
    querypre.set("stats.field", "{!tag=piv1%20max=true}profile_date")
    querypre.set("rows", 0)

    val query = new SolrQuery
    query.setQuery("*:*")
    query.set("fq", s"profile_date: ${TestParamGenerator.getDate}")
    query.setFacet(true)
    query.set("facet.pivot", "tags")
    query.set("rows", 0)
    query.setFacetLimit(10)
    MultiQuery("asset_distribution_by_tag",
      List(
        SolrQueryAndPostProcessing("", "assets", client, query, x => Unit),
        SolrQueryAndPostProcessing("", "assets", client, querypre, x => Unit)
      ))

  }

  def makeSensitiveDataAccessQuery: QueryAndPostProcessing = {
    val triplet = TestParamGenerator.getDateRangeAndCollection
    val query = new SolrQuery
    query.setQuery(s"audit_date:{${triplet._1} TO ${triplet._2}}")
    query.set("fq", s"collections: ${triplet._3}")
    query.set("stats", true)
    query.set("stats.field", "{!tag=piv1 sum=true}allowed")
    query.setFacet(true)
    query.set("facet.pivot", "{!stats=piv1}is_sensitive")
    query.set("rows", 0)
    SolrQueryAndPostProcessing("sensitive_data_access", "audit", client, query, x => {

    })
  }


  def accessPerDays: QueryAndPostProcessing = {
    val duet = TestParamGenerator.getDateRange
    val query = new SolrQuery
    query.setQuery(s"audit_date:{${duet._1} TO ${duet._2}}")
    query.set("fq", s"is_sensitive:true")
    query.set("stats", true)
    query.set("stats.field", "{!tag=piv1 sum=true}allowed")
    query.setFacet(true)
    query.set("facet.pivot", "{!stats=piv1}audit_date")
    query.set("rows", 0)
    SolrQueryAndPostProcessing("access_per_days", "audit", client, query, x => {
      Unit
    })
  }

}
