package com.dss.poc.models

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

import scala.util.Random

object TestParamGenerator {
  val collections: List[String] = Nil

  val random = new Random(System.nanoTime)


  def randomDate(from: LocalDate = LocalDate.of(2018, 1, 1),
                 to: LocalDate = LocalDate.of(2018, 12, 31)): LocalDate = {
    val diff = DAYS.between(from, to)
    from.plusDays(random.nextInt(diff.toInt))
  }

  def getDate: String = {
    randomDate().toString
  }

  def getDateRange: (String, String) = {
    val startDate = randomDate(LocalDate.of(2018, 1, 1),
      LocalDate.of(2018, 6, 15))

    val endDate = startDate.plusDays(random.nextInt(100) + 10)
    (startDate.toString, endDate.toString)
  }


  def getDateRangeAndCollection: (String, String, String) = {
    val range = getDateRange
    (range._1, range._2, collections(random.nextInt(collections.length)))
  }
}
