package com.repocad.reposcript

import com.repocad.reposcript.evaluating.Value

/**
 * The evaluating package contains code to execute the Abstract Syntax Tree (AST) from the 
 * [[com.repocad.reposcript.parsing.Parser]], on the medium given to the [[com.repocad.reposcript.evaluating.Evaluator]].
 */
package object evaluating {

  type Env = Map[String, Any]

  type Value = Either[String, (Env, Any)]

  object Error {
    def OPERATOR_NOT_FOUND(x: String): String = s"Failed to find operator '$x'. Has it been defined?"

    def OBJECT_PARAM_EVAL_ERROR(name: String, lefts: Seq[Value]): String =
      s"Failed to evaluate ${lefts.size} parameters when creating object '$name': $lefts"

    def OBJECT_PARAM_SIZE_NOT_EQUAL(objectName : String, expectedParams : Int, actualParams : Int) = 
      s"Object '$objectName' requires $expectedParams parameters, but was given $actualParams"

    def TYPE_MISMATCH(expected : String, actual : String) = s"Expected type $expected but found $actual"

  }
  
}
