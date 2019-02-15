package com.dss.poc.metrics

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

import scala.util.Random

object TestParamGenerator {
  val collections: List[String] = List("e1cbj6wiy0", "ik6bkv0xeo", "wp0lv4i1zg",
    "fce4ntbqey", "jg1tgbohfp", "z3ue78uexq", "bdvbo0mg0g",
    "enrsea1rop", "ea6k2gu5ae", "fc7fd5ls37", "vzb5xhtbeb",
    "uheq5qumnb", "ejuk518nqg", "v3yi168ztv", "kgm4vlk448",
    "xc5ias6frs", "drzsvfu0fp", "k3zabumqcw", "bx0o3elv5i",
    "dfhoja5z43", "oqbkofuf8l", "xkctszabwv", "wc1stsdl4r",
    "zq6yy1wxw9", "mzcsas9t8q", "ofvmnvu00e", "jmv5rjpu94",
    "ervpty1eoy", "bpqc1zfvt9", "gygva6wlrw", "hjbfb68np0",
    "urrxz0z2ji", "ppxunoezgy", "bye4i7tird", "ms515udbme",
    "y9thaxyk8h", "r7lyy3ar3k", "dbuv5vui4j", "bqijc0wh4p",
    "utp5egcecm", "olssjduhhw", "e8odks2hwg", "tsxdwe7jtx",
    "j2yyif32vm", "i2do3j964b", "xk2znjevks", "ehd9wcp3pm",
    "rvc8kit56u", "qtg010028l", "l11g30whux", "s4wq2g0bgy")

  val random = new Random(System.nanoTime)


  def randomDate(from: LocalDate = LocalDate.of(2018, 1, 2),
                 to: LocalDate = LocalDate.of(2018, 12, 30)): LocalDate = {
    val diff = DAYS.between(from, to)
    from.plusDays(random.nextInt(diff.toInt))
  }

  def getDate: String = {
    randomDate().toString
  }

  def getDateRange: (String, String) = {
    val startDate = randomDate(LocalDate.of(2018, 1, 2),
      LocalDate.of(2018, 6, 15))

    val endDate = startDate.plusDays(random.nextInt(100) + 20)
    (startDate.toString, endDate.toString)
  }


  def getDateRangeAndCollection: (String, String, String) = {
    val range = getDateRange
    (range._1, range._2, collections(random.nextInt(collections.length)))
  }
}
