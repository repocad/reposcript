//package com.repocad.reposcript.evaluating
//
//import com.repocad.remote.HttpClient
//import com.repocad.reposcript.lexing.TokenLexer$
//import com.repocad.reposcript.parsing._
//import com.repocad.reposcript.{Environment, Renderer}
//import org.scalamock.scalatest.MockFactory
//import org.scalatest.{FlatSpec, Matchers}
//
//class PrimitivesTest extends FlatSpec with MockFactory with Matchers {
//
//  val mockPrinter: Renderer = mock[Renderer]
//  val defaultEnv = Environment.evaluatorEnv
//  val mockParser = new Parser(mock[HttpClient], ParserEnv(), TokenLexer.lex)
//  val evaluator = new Evaluator(mockParser, defaultEnv)
//
//  def evalPrimitive[T](operand: String, arg1: Int, arg2: Int, output: AnyType, expected: T) = {
//    evaluator.eval(CallExpr(operand, output, Seq(NumberExpr(arg1), NumberExpr(arg2))), defaultEnv) should equal(
//      Right(defaultEnv -> expected))
//  }
//
//  "An evaluator for primitive expressions" should "evaluate a plus expression" in {
//    evalPrimitive("+", 2, 3, NumberType, 5)
//  }
//  it should "evaluate a minus expression" in {
//    evalPrimitive("-", 2, 3, NumberType, -1)
//  }
//  it should "evaluate a times expression" in {
//    evalPrimitive("*", 2, 3, NumberType, 6)
//  }
//  it should "evaluate a division expression" in {
//    evalPrimitive("/", 3, 2, NumberType, 1.5)
//  }
//  it should "evaluate a less-than expression" in {
//    evalPrimitive("<", 2, 2, BooleanType, false)
//  }
//  it should "evaluate a less-than-equals expression" in {
//    evalPrimitive("<=", 2, 2, BooleanType, true)
//  }
//  it should "evaluate a greater-than expression" in {
//    evalPrimitive(">", 2, 2, BooleanType, false)
//  }
//  it should "evaluate a greater-than-equals expression" in {
//    evalPrimitive(">=", 2, 2, BooleanType, true)
//  }
//}
