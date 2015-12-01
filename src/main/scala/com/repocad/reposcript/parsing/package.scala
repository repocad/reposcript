package com.repocad.reposcript

import com.repocad.reposcript.lexing.{Token, LiveStream}

/**
 * The parsing package contains code for converting [[com.repocad.reposcript.lexing.Token]]s into an Abstract Syntax Tree
 * (AST), which is a tree structure with an [[com.repocad.reposcript.parsing.Expr]] as the only root.
 */
package object parsing {

  type Value = Either[String, ParserState]

  type FailureCont = String => Value
  type SuccessCont = ParserState => Value

  case class ParserState(expr : Expr, env : ParserEnv, tokens : LiveStream[Token])
  object ParserState {
    private val emptyStream = LiveStream[Token](Iterable())
    def apply(expr : Expr, env : ParserEnv) : ParserState = ParserState(expr, env, emptyStream)
  }

  lazy val stringTypeMap : Map[String, AnyType] = Map(
    "Boolean" -> BooleanType,
    "Number" -> NumberType,
    "String" -> StringType,
    "Unit" -> UnitType
  )

  object Error {

    def EXPECTED_FUNCTION_PARAMETERS(name : String, expected : String, actual : String) = s"Expected parameters for function $name like $expected, but got $actual"
    def EXPECTED_OBJECT_ACCESS(actual: String): String = s"Expected access to object, but tried to access the expression $actual"
    def EXPECTED_OBJECT_PARAMETERS(name : String, expected : String, actual : String) = s"Expected call for the object $name like $expected, but got $actual"
    def EXPECTED_PARAMETERS(actual : String) : String = s"Expected parameter list when creating a function or object, but received '$actual'"
    def EXPECTED_PARAMETER_NUMBER(functionName : String, expected : Int, actual : Int) : String = s"Function '$functionName' requires $expected parameters, but $actual was given"
    def EXPECTED_TYPE_PARAMETERS(name : String) : String = s"No type information for variable $name; please specify its type using '$name as [Type]'"

    def FUNCTION_NOT_FOUND(functionName: String): String = s"Function '$functionName' not found"

    def OBJECT_INSTANCE_NOT_FOUND(callName: String): String = s"Could not find object instance by the name of $callName"
    def OBJECT_MISSING_PARAMETERS(name : String) = s"Object '$name' must have at least one parameter"
    def OBJECT_NOT_FOUND(name: String): String = s"Could not find object of name '$name'"
    def OBJECT_UNKNOWN_PARAMETER_NAME(objectName : String, accessor: String): String = s"No field in object $objectName by the name of $accessor"

    def REFERENCE_NOT_FOUND(reference : String) = s"Could not find object '$reference'. Has it been defined?"

    def SYNTAX_ERROR(expected : String, actual : String) = s"Syntax error: Expected '$expected', but found '$actual'"

    def TYPE_MISMATCH(expected : String, actual : String, when : String = "") =
      s"Type mismatch ${if (when.isEmpty) "" else "when " + when}: Expected $expected, but got $actual"

    def TYPE_NOT_FOUND(typeName : String) : String = s"Type '$typeName' not found in scope. Is it defined above?"

    def TWO(error1 : String, error2 : String) = s"Two errors: $error1 and $error2"
  }

}
