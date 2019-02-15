package com.dss.poc.metrics


object LoadJob extends App {

  val warmUpIterations = 5

  print("source:")
  val source: String = scala.io.StdIn.readLine()

  require(List("solr", "pg").contains(source))
  val quries: List[String] = List("asset_distribution_by_tag", "sensitive_data_access", "access_per_days")

  print("Test type: ")

  val testType: String = scala.io.StdIn.readLine()

  require(quries.contains(testType))

  print("Number of iterations: ")

  val numberOfIterations: Int = scala.io.StdIn.readInt()

  source match {

    case "solr" =>
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

      val percentiles: List[Long] = percentilesToCalculate.map(percentileCompute(latenciesSolr, _))

      println(s"Test:$testType,Source:Solr,iterations:$numberOfIterations")
      println(columns.mkString(","))
      println((List(avg) ::: percentiles).mkString(","))
      SolrQueryHandler.client.close()

    case "pg" =>
      val percentilesToCalculate: List[Double] = List(25, 50, 75, 90, 100)

      val columns = List("avg") ::: percentilesToCalculate.map(x => s"$x%")


      println("Warming up Postgres")

      (0 until warmUpIterations).foreach(
        _ => PostgresQueryHandler.getQuery(testType).process
      )
      println("Starting Postgres test")


      val latenciesSolr: List[Long] = (0 until numberOfIterations).map(
        _ => PostgresQueryHandler.getQuery(testType).process._2
      ).toList
      val avg = latenciesSolr.map(_.toDouble).sum / latenciesSolr.size

      val percentiles: List[Long] = percentilesToCalculate.map(percentileCompute(latenciesSolr, _))


      println(s"Test:$testType,Source:Postgres,iterations:$numberOfIterations")
      println(columns.mkString(","))
      println((List(avg) ::: percentiles).mkString(","))
      PostgresQueryHandler.client.close()
  }

  def percentileCompute(latencies: List[Long], percentile: Double) = {
    val sortedLatencies: List[Long] = latencies.sorted
    val Index = Math.ceil((percentile.toDouble / 100.toDouble) * sortedLatencies.size.toDouble).asInstanceOf[Int]
    sortedLatencies(Index - 1)
  }


}
