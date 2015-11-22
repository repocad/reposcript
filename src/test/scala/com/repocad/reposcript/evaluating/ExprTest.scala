package com.repocad.reposcript.evaluating

import com.repocad.reposcript.{parsing, HttpClient}
import com.repocad.reposcript.parsing._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

/**
 * Tests that the evaluator can evaluate [[com.repocad.reposcript.parsing.Expr]]
 */
class ExprTest extends FlatSpec with MockFactory with Matchers {

  val emptyEnv : Env = Map[String, Any]()
  val mockParser = new Parser(mock[HttpClient], ParserEnv())
  val evaluator = new Evaluator(mockParser, Map())

  "An expression evaluator" should "evaluate an empty block expression" in {
    evaluator.eval(BlockExpr(Seq()), emptyEnv) should equal (Right(emptyEnv -> Unit))
  }
  it should "evaluate a non-empty block expression" in {
    evaluator.eval(BlockExpr(Seq(NumberExpr(1))), emptyEnv) should equal (Right(emptyEnv -> 1))
  }
  it should "evaluate a def expression" in {
    evaluator.eval(DefExpr("test", NumberExpr(1)), emptyEnv) should equal (Right(Map("test" -> 1) -> 1))
  }
  it should "evaluate a function expression" in {
    val fun = (env : Env, a : Int) => a
    val output = evaluator.eval(FunctionExpr("f", Seq(RefExpr("a", NumberType)), RefExpr("a", NumberType)), emptyEnv).right.get
    output._1.get("f") should equal(Some(output._2))
    output._2.asInstanceOf[Function2[Env, Int, Int]](emptyEnv, 2) should equal (2)
  }

  "An object evaluator" should "evaluate an object expression" in {
    evaluator.eval(ObjectType("object", Seq(), AnyType), emptyEnv) should equal(Right(Map("object" -> Seq()), Seq()))
  }
  it should "construct an object" in {
    val t = ObjectType("object", Seq(RefExpr("a", NumberType)), AnyType)
    val paramNames = t.params.map(_.name)
    evaluator.eval(CallExpr("object", t, Seq(NumberExpr(12))), Map("object" -> paramNames)) should equal(Right(Map("object" -> paramNames), Map("a" -> 12)))
  }
  it should "access an element in an object" in {
    val params = Map("a" -> 12)
    evaluator.eval(RefFieldExpr("object", "a", StringType), Map("object" -> params)) should equal(Right(Map("object" -> params), 12))
  }

  "A value evaluator" should "evaluate a boolean expression" in {
    evaluator.eval(BooleanExpr(false), emptyEnv) should equal(Right(emptyEnv -> false))
  }
  it should "evaluate a float expression" in {
    evaluator.eval(NumberExpr(2.2), emptyEnv) should equal(Right(emptyEnv -> 2.2))
  }
  it should "evaluate a int expression" in {
    evaluator.eval(NumberExpr(2), emptyEnv) should equal(Right(emptyEnv -> 2))
  }
  it should "evaluate a string expression" in {
    evaluator.eval(StringExpr("hi"), emptyEnv) should equal(Right(emptyEnv -> "hi"))
  }
  it should "evaluate a unit expression" in {
    evaluator.eval(UnitExpr, emptyEnv) should equal(Right(Map() -> Unit))
  }

}