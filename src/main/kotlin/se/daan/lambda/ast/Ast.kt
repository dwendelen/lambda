package se.daan.lambda.ast

sealed interface Expression
data class NamedExpression(
    val name: String,
    val expression: Expression
): Expression
data class TypedExpression(
    val type: Type,
    val expression: Expression
) : Expression
data class Call(
    val called: Expression,
    val param: Expression
) : Expression
data class Lambda(
    val expression: Expression
) : Expression
data class ClosureRef(
    val index: Int
) : Expression

sealed class Type
class SelfType : Type()
data class LambdaType(
    val from: Type,
    val to: Type
) : Type()
data class ParameterType(
    val index: Int
) : Type()
class InternalType: Type()
data class TypeRef(
    val type: Type,
    val params: List<Type>,
    val name: String
) : Type()

