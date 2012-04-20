package com.micronautics.zeromq

case object Tick

case class Heap(timestamp: Long, used: Long, max: Long)

case class Load(timestamp: Long, loadAverage: Double)
