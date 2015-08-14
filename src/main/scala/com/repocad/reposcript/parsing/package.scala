package com.repocad.reposcript

import com.repocad.reposcript.lexing.{Token, LiveStream}
import com.repocad.reposcript.util.DirectedGraph

/**
 * The parsing package contains code for converting [[com.repocad.reposcript.lexing.Token]]s into an Abstract Syntax Tree
 * (AST), which is a tree structure with an [[com.repocad.reposcript.parsing.Expr]] as the only root.
 */
package object parsing {

  type TypeEnv = DirectedGraph[AnyType]
  type ValueEnv = Map[String, Expr]

  type Value = Either[String, (Expr, ValueEnv, TypeEnv)]

  type FailureCont = String => Value
  type SuccessCont = (Expr, ValueEnv, TypeEnv, LiveStream[Token]) => Value

  val defaultTypeEnv : TypeEnv = DirectedGraph[AnyType](AnyType)
    /* Primitives */
    .union(AnyType, BooleanType)
    .union(AnyType, StringType)
    .union(AnyType, UnitType)
    .union(AnyType, NumberType)
    /* Functions */
    .union(AnyType, FunctionType)
    .union(FunctionType, Function1Type)
    .union(FunctionType, Function2Type)
    .union(FunctionType, Function3Type)
    .union(FunctionType, Function4Type)

  val emptyTypeEnv : TypeEnv = new DirectedGraph(Map(), AnyType)

  lazy val stringTypeMap : Map[String, AnyType] = Map(
    "Boolean" -> BooleanType,
    "Number" -> NumberType,
    "String" -> StringType,
    "Unit" -> UnitType
  )

  object Error {
    def EXPECTED_PARAMETERS(actual : String) : String = s"Expected parameter list when creating a function or object, but received '$actual'"
    def EXPECTED_PARAMETER_NUMBER(functionName : String, expected : Int, actual : Int) : String = s"Function '$functionName' requires $expected parameters, but $actual was given"
    def EXPECTED_TYPE_PARAMETERS(name : String) : String = s"No type information for variable $name; please specify its type using '$name as [Type]'"

    def OBJECT_MISSING_PARAMETERS(name : String) = s"Object '$name' must have at least one parameter"

    def FUNCTION_NOT_FOUND(functionName: String): String = s"Function '$functionName' not found"

    def REFERENCE_NOT_FOUND(reference : String) = s"Could not find object '$reference'. Has it been defined?"

    def SYNTAX_ERROR(expected : String, actual : String) = s"Syntax error: Expected '$expected', but found '$actual'"

    def TYPE_MISMATCH(expected : String, actual : String, when : String = "") =
      s"Type mismatch ${if (when.isEmpty) "" else "when " + when}: Expected $expected, but got $actual"

    def TYPE_NOT_FOUND(typeName : String) : String = s"Type '$typeName' not found in scope. Is it defined above?"

    def TWO(error1 : String, error2 : String) = s"Two errors: $error1 and $error2"
  }

}
