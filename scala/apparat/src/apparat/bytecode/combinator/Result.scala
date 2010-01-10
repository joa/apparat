package apparat.bytecode.combinator

import apparat.bytecode.operations.AbstractOp

sealed trait Result[+A]

case class Success[+A](value: A, remaining: Stream[AbstractOp]) extends Result[A]
case class Failure(msg: String) extends Result[Nothing]