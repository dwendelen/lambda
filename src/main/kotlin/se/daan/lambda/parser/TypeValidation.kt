package se.daan.lambda.parser

import se.daan.lambda.ast.*

fun validateType(
    expression: Expression,
    type: Type,
    context: TypeContext
): TypeContext {
    return when (expression) {
        is TypedExpression -> {
            bind(expression.type, type, context)
        }
        is Lambda -> {
            val internal = InternalType()
            val ctx = context.copy(
                closure = listOf(internal) + context.closure
            )
            validateType(expression.expression, type, ctx)
        }
        is Call -> {
            val internal = InternalType()
            val ctx1 = validateType(expression.param, internal, context)
            validateType(expression.called, LambdaType(internal, type), ctx1)
        }
        is ClosureRef -> {
            bind(context.closure[expression.index], type, context)
        }
        is IOExpression -> {
            bind(LambdaType(IOType, IOType), type, context) //TODO check
        }
        Done -> {
            bind(IOType, type, context)
        }
    }
}

private fun replaceWith(type: Type, from: Type, to: Type): Type {
    return when (type) {
        is TypeRef -> type.copy(params = type.params.map {
            replaceWith(it, from, to)
        })
        is LambdaType -> LambdaType(
            replaceWith(type.from, from, to),
            replaceWith(type.to, from, to)
        )
        else -> if(type == from) {
            return to
        } else {
            type
        }
    }
}

private fun bind(type1: Type, type2: Type, context: TypeContext): TypeContext {
    return when (type1) {
        is TypeRef ->
            if (type2 is TypeRef && type1.type === type2.type) {
                type1.params
                    .zip(type2.params)
                    .fold(context) { ctx, (p1, p2) ->
                        bind(p1, p2, ctx)
                    }
            } else {
                val ctx = context.copy(params = type1.params)
                bind(type1.type, type2, ctx)
            }
        is SelfType ->
            if (type1 !== type2) {
                bind(context.self, type2, context)
            } else {
                context
            }
        is ParameterType ->
            bind(context.params[type1.index], type2, context)
        is IOType ->
            when (type2) {
                is IOType -> context
                else -> bind(type2, type1, context)
            }
        is LambdaType ->
            when (type2) {
                is IOType -> throw IllegalStateException()
                is LambdaType -> {
                    val ctx = bind(type1.from, type2.from, context)
                    bind(type1.to, type2.to, ctx)
                }
                else -> bind(type2, type1, context)
            }
        is InternalType ->
            if (type1 === type2) {
                context
            } else {
                val found = context.bindings[type1]
                if (found == null) {
                    context.copy(bindings = context.bindings + (type1 to type2))
                } else {
                    bind(found, type2, context)
                }
            }
    }
}

data class TypeContext(
    val self: Type,
    val params: List<Type>,
    val closure: List<Type>,
    val bindings: Map<InternalType, Type>
)