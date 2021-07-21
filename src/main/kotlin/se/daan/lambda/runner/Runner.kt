package se.daan.lambda.runner

import se.daan.lambda.ast.*
import se.daan.lambda.parser.*
import java.lang.IllegalArgumentException
import kotlin.IllegalStateException

class Runner(
    private val initialSymbolTable: SymbolTable = mapOf("done" to ExpressionSymbol(Done)),
    private val initialTypeTable: TypeTable = mapOf("IO" to InternalTypeSymbol(IOType))
) {
    fun io(name: String, fn: () -> Unit): Runner {
        return Runner(initialSymbolTable + (name to ExpressionSymbol(IOExpression(fn))))
    }

    fun run(script: String, name: String) {
        val symbolTable = parse(script, initialSymbolTable, initialTypeTable)

        val symbol = symbolTable[name] ?: throw IllegalArgumentException("$name not found")
        val expressionSymbol = symbol as? ExpressionSymbol ?: throw IllegalAccessException("$name is not an expression")
        val expression = expressionSymbol.expression
        val evaluate = evaluate(expression, emptyList())
        if(evaluate !is DoneResult) {
            throw IllegalStateException("Expecting DoneResult, but got $evaluate")
        }
    }

    private fun evaluate(expression: Expression, closure: List<LazyExpression>): Result {
        return when (expression) {
            is TypedExpression -> evaluate(expression.expression, closure)
            is Call -> callWith(evaluate(expression.called, closure), LazyExpression(closure, expression.param))
            is Lambda -> LambdaResult(closure, expression.expression)
            is ClosureRef -> evaluate(closure[expression.index])
            is IOExpression -> IOResult(expression.fn)
            is Done -> DoneResult
        }
    }

    private fun callWith(result: Result, lazyExpression: LazyExpression): Result {
        return when(result) {
            is IOResult -> {
                result.fn()
                evaluate(lazyExpression)
            }
            is LambdaResult -> evaluate(result.expression, listOf(lazyExpression) + result.closure)
            DoneResult -> throw IllegalStateException("Can't call a done result")
        }
    }

    private fun evaluate(lazyExpression: LazyExpression): Result {
        return evaluate(lazyExpression.expression, lazyExpression.closure)
    }
}

data class LazyExpression(
    val closure: List<LazyExpression>,
    val expression: Expression
)

sealed class Result
data class LambdaResult(
    val closure: List<LazyExpression>,
    val expression: Expression
): Result()
data class IOResult(
    val fn: () -> Unit
): Result()
object DoneResult: Result()