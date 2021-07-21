package se.daan.lambda.parser

import se.daan.lambda.ast.*

fun validateType(expression: Expression, type: Type) {
    //bind(evaluateType(expression), type, type, emptyList(), emptyMap())
}

fun evaluateType(expression: Expression, closure: List<Type>): Type {
    return when(expression) {
        is TypedExpression -> expression.type
        is Call -> evaluateType(expression.called, listOf(evaluateType(expression.param, closure)) + closure)
        is Lambda -> TODO()
        is ClosureRef -> closure[expression.index]
        is IOExpression -> LambdaType(IOType, IOType)
        is Done -> IOType
    }
}

private fun bind(type1: Type, type2: Type, self: Type, params: List<Type>, closure: List<Type>) {
    when(type1) {
        is TypeRef ->
            if(type2 is TypeRef && type1.type === type2.type) {
                type1.params
                    .zip(type2.params)
                    .forEach {(p1, p2) ->
                        bind(p1, p2, self, params, closure)
                    }
            } else {
                bind(type1.type, type2, self, type1.params, closure)
            }
        is SelfType ->
            if (type1 !== type2) {
                bind(self, type2, self, params, closure)
            }
        is ParameterType ->
            bind(params[type1.index], type2, self, params, closure)
        is ClosureType ->
            bind(closure[type1.index], type2, self, params, closure)
        is NewType -> {}
        is IOType ->
            when(type2) {
                is IOType -> {}
                else -> bind(type2, type1, self, params, closure)
            }
        is LambdaType ->
            when(type2) {
                is IOType -> throw IllegalStateException()
                is LambdaType -> {
                    bind(type1.from, type2.from, self, params, closure)
                    bind(type1.to, type2.to, self, params, closure) // TODO closure????
                }
                else -> bind(type2, type1, self, params, closure)
            }
    }
}
