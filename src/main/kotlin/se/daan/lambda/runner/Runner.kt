package se.daan.lambda.runner

import se.daan.lambda.ast.*
import se.daan.lambda.parser.*
import java.lang.IllegalArgumentException
import java.util.NoSuchElementException

sealed interface LList<T> {
    fun prepend(e: T): LList<T> = Node(e, this)
    operator fun get(index: Int): T
    val size: Int
    val head: T
    val tail: LList<T>
}
class Nil<T>: LList<T> {
    override fun get(index: Int): T {
        throw NoSuchElementException()
    }
    override val size = 0

    override val head: T
        get() = throw NoSuchElementException()
    override val tail: LList<T>
        get() = throw NoSuchElementException()

    override fun hashCode(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        return other is Nil<*>
    }

    override fun toString(): String {
        return "Nil"
    }
}
data class Node<T>(
    override val head: T,
    override val tail: LList<T>
    ): LList<T> {
    override fun get(index: Int): T {
        if(index < 0) {
            throw NoSuchElementException()
        }
        return if(index == 0) {
            head
        } else {
            tail[index - 1]
        }
    }
    override val size get() = tail.size + 1
}
private fun <T> Iterable<T>.asLList(): LList<T> {
    return this.reversed().fold(Nil<T>() as LList<T>) {acc, e -> Node(e, acc)}
}


fun <T> parse(script: String, name: String): Expression {
    val symbolTable = parse(script)
    val symbol = symbolTable[name] ?: throw IllegalArgumentException("$name not found")
    val expressionSymbol = symbol as? ExpressionSymbol ?: throw IllegalAccessException("$name is not an expression")
    return expressionSymbol.expression
}

fun optimise(expression: Expression): Expression {
    return when(expression) {
        is NamedExpression -> optimise(expression.expression)
        is TypedExpression -> optimise(expression.expression)
        is Call -> {
            val optimizedCalled = optimise(expression.called)
            val optimizedParam = optimise(expression.param)
            if(optimizedCalled is Lambda) {
                resolveParam(optimizedCalled.expression, 0, optimizedParam)
            } else {
                Call(optimizedCalled, optimizedParam)
            }
        }
        is ClosureRef -> expression
        is Lambda -> Lambda(optimise(expression.expression))
    }
}

private fun resolveParam(expression: Expression, paramIdx: Int, optimizedCalled: Expression): Expression {
    return when(expression) {
        is Call -> Call(resolveParam(expression.called, paramIdx, optimizedCalled), resolveParam(expression.param, paramIdx, optimizedCalled))
        is ClosureRef ->
            if(expression.index == paramIdx) {
                optimizedCalled
            } else if(expression.index > paramIdx) {
                ClosureRef(expression.index - 1)
            } else {
                expression
            }
        is Lambda -> resolveParam(expression.expression, paramIdx + 1, optimizedCalled)
        is NamedExpression -> throw IllegalArgumentException()
        is TypedExpression -> throw IllegalArgumentException()
    }
}

fun <T> evaluate(expression: Expression, vararg params: T): UserObjectResult<T> {
    val initial = LazyResult<T>(expression, Nil())
    return evaluate(initial, params.map { UserObject(it) }.asLList())
}

tailrec fun <T> evaluate(lazyResult: LazyResult<T>, params: LList<Stackable<T>>): UserObjectResult<T> {
    val expression = lazyResult.expression
    val stack = lazyResult.stack
    return when (expression) {
        is TypedExpression -> evaluate(lazyResult.copy(expression = expression.expression), params)
        is NamedExpression ->
            evaluate(lazyResult.copy(expression = expression.expression), params)
        is Call -> evaluate(LazyResult(expression.called, stack), params.prepend(LazyResult(expression.param, stack)))
        is Lambda -> evaluate(LazyResult(expression.expression, stack.prepend(params.head)), params.tail)
        is ClosureRef -> when(val item = stack[expression.index]) {
            is UserObject -> UserObjectResult(item.userObject, params)
            is LazyResult -> evaluate(item, params)
        }
    }
}

fun toCode(expression: Expression, depth: Int = 0): String {
    return when(expression) {
        is Call -> "${toCode_called(expression.called, depth)}.${toCode_param(expression.param, depth)}"
        is ClosureRef -> "p${depth - expression.index - 1}"
        is Lambda -> "p$depth -> ${toCode(expression.expression, depth + 1)}"
        is NamedExpression -> toCode(expression.expression, depth)
        is TypedExpression -> toCode(expression.expression, depth)
    }
}

private fun toCode_called(called: Expression, depth: Int): String {
    return when(called) {
        is Call -> toCode(called, depth)
        is ClosureRef -> toCode(called, depth)
        is Lambda -> "(${toCode(called, depth)})"
        is NamedExpression -> toCode_called(called.expression, depth)
        is TypedExpression -> toCode_called(called.expression, depth)
    }
}

private fun toCode_param(param: Expression, depth: Int): String {
    return when(param) {
        is Call -> "(${toCode(param, depth)})"
        is ClosureRef -> toCode(param, depth)
        is Lambda -> "(${toCode(param, depth)})"
        is NamedExpression -> toCode_param(param.expression, depth)
        is TypedExpression -> toCode_param(param.expression, depth)
    }
}

sealed interface Stackable<T>

data class LazyResult<T>(
    val expression: Expression,
    val stack: LList<Stackable<T>>
): Stackable<T>
data class UserObject<T>(
    val userObject: T,
): Stackable<T>
data class UserObjectResult<T>(
    val userObject: T,
    val params: LList<Stackable<T>>
)
