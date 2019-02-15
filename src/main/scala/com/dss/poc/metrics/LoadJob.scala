package com.dss.poc.metrics

import scala.collection.immutable


object LoadJob extends App {

  val warmUpIterations = 5

  val quries: List[String] = List("asset_distribution_by_tag", "sensitive_data_access", "access_per_days")

  print("Test type: ")

  val testType: String = scala.io.StdIn.readLine()

  require(quries.contains(testType))

  print("Number of iterations: ")

  val numberOfIterations: Int = scala.io.StdIn.readInt()


  val percentilesToCalculate: List[Double] = List(25, 50, 75, 90, 100)

  val columns = List("avg") ::: percentilesToCalculate.map(x => s"$x%")


  println("Warming up Solr")

  (0 until warmUpIterations).foreach(
    _ => SolrQueryHandler.getQuery(testType).process
  )
  println("Starting Solr test")


  val latenciesSolr: List[Long] = (0 until numberOfIterations).map(
    _ => SolrQueryHandler.getQuery(testType).process._2
  ).toList
  val avg = latenciesSolr.map(_.toDouble).sum / latenciesSolr.size

  val percentiles: List[Long] = percentilesToCalculate.map(percentile(latenciesSolr, _))


  def percentile(latencies: List[Long], percentile: Double) = {
    val sortedLatencies: List[Long] = latencies.sortBy(_ => _)
    val Index = Math.ceil((percentile.toDouble / 100.toDouble) * sortedLatencies.size.toDouble).asInstanceOf[Int]
    latencies(Index - 1)
  }


}
