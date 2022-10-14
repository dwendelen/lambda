package se.daan.lambda.runner

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import se.daan.lambda.ast.*

internal class RunnerKtTest {
    @Test
    internal fun callId() {
        // id = a -> a
        val id = Lambda(ClosureRef(0))
        val actual = evaluate(id, 0)
        assertEquals(UserObjectResult(0, Nil()), actual)
    }

    @Test
    internal fun callSelfInvoking() {
        // id = a -> a.a
        val id = Lambda(Call(ClosureRef(0), ClosureRef(0)))
        val actual = evaluate(id, 0)
        assertEquals(UserObjectResult(0, Node(LazyResult(ClosureRef(0), Node(UserObject(0), Nil())), Nil())), actual)
    }

    @Test
    internal fun crash() {
        // id = a -> (b -> b).a.a
        // id = _ -> (_ -> $0).$0.$0
        val id = Lambda(Call(Call(Lambda(ClosureRef(0)), ClosureRef(0)), ClosureRef(0)))
        val actual = evaluate(id, 0)
        assertEquals(UserObjectResult(0, Node(LazyResult(ClosureRef(0), Node(UserObject(0), Nil())), Nil())), actual)
    }

    @Test
    internal fun optimise1() {
        testOptimise(
            "b -> (a -> a).b",
            "a -> a"
        )
    }

    @Test
    internal fun optimise2() {
        testOptimise(
            "b -> (a -> c -> a.c).b",
            "b -> c -> b.c"
        )
    }
    @Test
    internal fun optimise3() {
        testOptimise(
            "z -> b -> (a -> c -> a.c.z).b",
            "z -> b -> c -> b.c.z"
        )
    }

    private fun testOptimise(script: String, expected: String) {
        val parsedScript = parse<Any>("t : A = $script", "t")
        val parsedExpected = parse<Any>("t : A = $expected", "t")
        val cleanedExpected = cleanNameAndType(parsedExpected)

        val actual = optimise(parsedScript)
        println("Org " + script)
        println("Exp " + toCode(cleanedExpected))
        println("Act " + toCode(actual))
        assertEquals(cleanedExpected, actual)
    }

    private fun cleanNameAndType(expression: Expression): Expression {
        return when(expression) {
            is NamedExpression -> cleanNameAndType(expression.expression)
            is Call -> Call(cleanNameAndType(expression.called), cleanNameAndType(expression.param))
            is ClosureRef -> expression
            is Lambda -> Lambda(cleanNameAndType(expression.expression))
            is TypedExpression -> cleanNameAndType(expression.expression)
        }
    }
}