package com.micronautics.util

object ByteFormatter {
  val units = List("B", "KB", "MB", "GB", "TB", "PB", "EB")
  
  def format(v: Double, precision: Int=2): String = {
    def formatR(n: Double, list: List[String]): String = {
      if (n < 1024.0 || list.size == 1) {
        val s = n.toString
        val dot = s.indexOf(".")
        val t = s.substring(0, math.min(dot + precision + 1, s.length))
        t + list.head
      } else {
        formatR(n/1024.0, list.tail)
      }
    }
    formatR(v, units)
  }

  def apply(l: Long): String = format(l.asInstanceOf[Double])
}
