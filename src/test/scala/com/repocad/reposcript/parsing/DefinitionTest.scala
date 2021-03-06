package com.repocad.reposcript.parsing

import com.repocad.reposcript.Compiler
import com.repocad.reposcript.lexing.Position

class DefinitionTest extends ParsingTest {

  /* Values */
  "A parser for definitions" should "parse a definition" in {
    testEquals(DefExpr("a", NumberExpr(10)), "def a = 10")
  }
  it should "parse a definition with type information" in {
    testEquals(DefExpr("a", NumberExpr(10)), "def a as Number = 10")
  }
  it should "set the type to a supertype if requested" in {
    testEquals(DefExpr("a", NumberExpr(10)), "def a as Number = 10")
  }
  it should "store a value in the value environment" in {
    parseString("def a = 10", ParserEnv(), spillEnvironment = true).right.get.env should equal(ParserEnv("a" -> NumberExpr(10)))
  }
  it should "fail when wrong type is specified" in {
    parseString("def a as Unit = 1").isLeft should equal(true)
  }
  it should "recognise future expressions as part of the definition" in {
    parseString("def a = 10 * 20").right.get.expr should equal(DefExpr("a", CallExpr("*", NumberType, Seq(NumberExpr(10), NumberExpr(20)))))
  }
  it should "recognise future expressions as part of the definition after function calls" in {
    val env = ParserEnv("f" -> FunctionType("f", Seq(), NumberExpr(2)), "*" -> FunctionType("*", Seq(RefExpr("a", NumberType), RefExpr("b", NumberType)), NumberExpr(2)))
    parseString("def a = f() * 20", env).right.get.expr should equal(
      DefExpr("a", CallExpr("*", NumberType, Seq(CallExpr("f", NumberType, Seq()), NumberExpr(20)))))
  }

  /* Functions */
  "A parser for functions" should "parse a function without parameters and body" in {
    testEquals(FunctionType("a", Seq(), UnitExpr), "def a() = ")
  }
  it should "parse a function with one parameter and no body" in {
    testEquals(FunctionType("a", Seq(RefExpr("b", NumberType)), UnitExpr), "def a(b as Number) = ")
  }
  it should "parse a function without a parameter but with a body" in {
    testEquals(FunctionType("a", Seq(), DefExpr("b", NumberExpr(10.2))), "def a() = { def b = 10.2 }")
  }
  it should "parse a function with two parameters and no body" in {
    testEquals(FunctionType("a", Seq(RefExpr("b", NumberType), RefExpr("c", NumberType)), UnitExpr), "def a(b as Number c as Number) = ")
  }
  it should "parse a function with three parameters and no body" in {
    testEquals(FunctionType("a", Seq(RefExpr("b", NumberType), RefExpr("c", NumberType), RefExpr("d", StringType)), UnitExpr), "def a(b as Number c as Number d as String) = ")
  }
  it should "parse a function with four parameters and no body" in {
    testEquals(FunctionType("a", Seq(RefExpr("b", NumberType), RefExpr("c", NumberType), RefExpr("d", StringType), RefExpr("e", BooleanType)), UnitExpr), "def a(b as Number c as Number d as String e as Boolean) = ")
  }
  it should "parse a function with a prepended parameter" in {
    testEquals(FunctionType("a", Seq(RefExpr("b", NumberType)), UnitExpr), "def (b as Number)a = ")
  }
  it should "store a function in the value environment" in {
    val function = FunctionType("a", Seq(), UnitExpr)
    parseString("def a() = ", ParserEnv(), spillEnvironment = true).right.get.env should equal(ParserEnv("a" -> function))
  }
  it should "accept references to existing parameters in the function body" in {
    val function = FunctionType("a", Seq(RefExpr("b", NumberType)), RefExpr("b", NumberType))
    testEquals(function, "def a(b as Number) = b")
  }
  it should "call a function with no parameters" in {
    val function = FunctionType("a", Seq(), NumberExpr(8))
    testEquals(CallExpr("a", NumberType, Seq()), "a()", ParserEnv("a" -> function))
  }
  it should "refer to a Number parameter as a Number in an assignment" in {
    val function = FunctionType("a", Seq(RefExpr("b", NumberType)), DefExpr("c", RefExpr("b", NumberType)))
    testEquals(function, "def a(b as Number) = def c as Number = b")
  }
  it should "refer to an object parameter" in {
    val obj = ObjectType("o", Seq(), AnyType)
    val function = FunctionType("a", Seq(RefExpr("x", obj)), RefExpr("x", obj))
    testEquals(function, "def a(x as o) = x", ParserEnv("o" -> obj))
  }
  it should "refer to an object element in a function" in {
    val obj = ObjectType("o", Seq(RefExpr("a", NumberType)), AnyType)
    val function = FunctionType("a", Seq(RefExpr("x", obj)), RefFieldExpr(RefExpr("x", obj), "a", NumberType))
    testEquals(function, "def a(x as o) = x.a", ParserEnv("o" -> obj))
  }

  /* Objects */
  "An object parser" should "create an object and a type" in {
    parseString("def object(a as Any)").right.get.expr should equal(
      ObjectType("object", Seq(RefExpr("a", AnyType)), AnyType))
  }
  it should "store an object in the environment" in {
    parseString("def object(a as Any)", ParserEnv("any" -> AnyType), true).right.get.env should equal(
      ParserEnv("object" -> ObjectType("object", Seq(RefExpr("a", AnyType)), AnyType), "any" -> AnyType))
  }
  it should "fail when calling an object with the wrong parameter type" in {
    parseString("object(\"string\")", ParserEnv("object" -> ObjectType("object", Seq(RefExpr("a", NumberType)), AnyType))).isLeft should equal(true)
  }
  it should "fail when calling an object with the wrong parameter list length" in {
    parseString("object(12, \"string\")", ParserEnv("object" -> ObjectType("object", Seq(RefExpr("a", NumberType)), AnyType))).isLeft should equal(true)
  }
  it should "refer to a field in an object" in {
    val obj = ObjectType("o", Seq(RefExpr("x", NumberType)), AnyType)
    parseString("o(10).x", ParserEnv("o" -> obj)).right.get.expr should equal(RefFieldExpr(CallExpr("o", obj, Seq(NumberExpr(10))), "x", NumberType))
  }
  it should "refer to a field in a referenced object" in {
    val obj = ObjectType("o", Seq(RefExpr("x", NumberType)), AnyType)
    val instance = CallExpr("o", obj, Seq(NumberExpr(10)))
    val expr = parseStringAll("def o(x as number) \n def i = o(10) \n i.x", ParserEnv("number" -> NumberType))
    expr.right.get.expr should equal(
      BlockExpr(Seq(obj, DefExpr("i", instance), RefFieldExpr(RefExpr("i", obj), "x", NumberType))))
  }
  it should "reference a field in an object" in {
    val value = "hello"
    val t = ObjectType("object", Seq(RefExpr("name", StringType)), AnyType)
    parseString("instance.name", ParserEnv("object" -> t, "instance" -> CallExpr("object", t, Seq(StringExpr(value))))).right.get.expr should equal(
      RefFieldExpr(RefExpr("instance", t), "name", StringType)
    )
  }
  it should "fail to access a field that does not exist in an object" in {
    val value = "hello"
    val t = ObjectType("object", Seq(RefExpr("name", StringType)), AnyType)
    parseString("instance.noField", ParserEnv("object" -> t, "instance" -> CallExpr("object", t, Seq(StringExpr(value))))).left.get should equal(
      ParserError.OBJECT_UNKNOWN_PARAMETER_NAME("object", "nofield")(Position.start))
  }
  it should "reference another object" in {
    val o1 = ObjectType("o1", Seq(), AnyType)
    parseString("def o2(o as o1)", ParserEnv("o1" -> o1)).right.get.expr should equal(ObjectType("o2", Seq(RefExpr("o", o1)), AnyType))
  }
  it should "define objects as a subtype of another object" in {
    val parent = ObjectType("o", Seq(RefExpr("a", NumberType)), AnyType)
    parseString("def child(a as Number) extends o", ParserEnv("o" -> parent, "number" -> NumberType)).right.get.expr should equal(
      ObjectType("child", Seq(RefExpr("a", NumberType)), parent)
    )
  }
  it should "fail when subtypes forget parent parameters" in {
    val parent = ObjectType("o", Seq(RefExpr("a", NumberType)), AnyType)
    parseString("def child() extends o", ParserEnv("o" -> parent)).isLeft should equal(true)
  }
  it should "give default arguments to a supertype" in {
    val parent = ObjectType("o", Seq(RefExpr("a", NumberType)), AnyType)
    parseString("def child() extends o(7)", ParserEnv("o" -> parent)).right.get.expr should equal(
      ObjectType("child", Seq(), parent, Map("a" -> NumberExpr(7)))
    )
  }
  it should "allow to instantiate supertypes with zero parameters" in {
    val parent = ObjectType("o", Seq(), AnyType)
    parseString("def child() extends o()", ParserEnv("o" -> parent)).right.get.expr should equal(
      ObjectType("child", Seq(), parent)
    )
  }
  it should "fail when giving a parameter with a wrong type to a supertype" in {
    val parent = ObjectType("o", Seq(RefExpr("a", NumberType)), AnyType)
    parseString("def child() extends o(\"text\")", ParserEnv("o" -> parent)).isLeft should equal(true)
  }
  it should "refer to an external value inside a nested object call" in {
    val obj = ObjectType("o", Seq(RefExpr("x", NumberType)), AnyType)
    val function = FunctionType("f", Seq(RefExpr("o", obj)), RefFieldExpr(RefExpr("o", obj), "x", NumberType))
    parseStringAll("def v = 20 f(o(v))", ParserEnv("o" -> obj, "f" -> function)).right.get.expr should equal(
      BlockExpr(Seq(DefExpr("v", NumberExpr(20)), CallExpr("f", NumberType, Seq(
        CallExpr("o", obj, Seq(RefExpr("v", NumberType)))))))
    )
  }
  it should "perform calculations within references" in {
    val obj = ObjectType("f", Seq(RefExpr("x", NumberType)), AnyType)
    parseStringAll("def a = f(10) f(a.x - 11)", Compiler.defaultEnv ++ ParserEnv("f" -> obj)).right.get.expr should equal(
      BlockExpr(Seq(DefExpr("a", CallExpr("f", obj, Seq(NumberExpr(10)))),
        CallExpr("f", obj, Seq(CallExpr("-", NumberType, Seq(RefFieldExpr(RefExpr("a", obj), "x", NumberType), NumberExpr(11)))))))
    )
  }

}
