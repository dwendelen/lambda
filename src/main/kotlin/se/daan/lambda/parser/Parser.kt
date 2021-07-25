package se.daan.lambda.parser

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import se.daan.lambda.ast.*
import java.lang.Exception
import java.lang.RuntimeException

fun parse(script: String, symbolTable: SymbolTable, typeTable: TypeTable): SymbolTable {
    val lexer = LambdaLexer(ANTLRInputStream(script))
    val parser = LambdaParser(CommonTokenStream(lexer))
    val program = parser.program()

    return program.accept(ProgramVisitor(symbolTable, typeTable)).first
}

typealias SymbolTable = Map<String, Symbol>
typealias TypeTable = Map<String, TypeSymbol>

sealed class Symbol
data class ExpressionSymbol(val expression: Expression) : Symbol()
data class VariableSymbol(val depth: Int) : Symbol()

sealed class TypeSymbol
data class ExternalTypeSymbol(val type: Type, val nbParams: Int) : TypeSymbol()
data class InternalTypeSymbol(val type: Type) : TypeSymbol()

// TODO add lines and char in line to exception

class ProgramVisitor(
    private val symbolTable: SymbolTable,
    private val typeTable: TypeTable
) : LambdaBaseVisitor<Pair<SymbolTable, TypeTable>>() {
    override fun visitProgram(ctx: LambdaParser.ProgramContext): Pair<SymbolTable, TypeTable> {
        return ctx.programItem()
            .fold(Pair(symbolTable, typeTable)) { (expr, type), item ->
                item.accept(ProgramItemVisitor(expr, type))
            }
    }
}

class ProgramItemVisitor(
    private val table: SymbolTable,
    private val typeTable: TypeTable
) : LambdaBaseVisitor<Pair<SymbolTable, TypeTable>>() {
    override fun visitTypeDef(ctx: LambdaParser.TypeDefContext): Pair<SymbolTable, TypeTable> {
        val ids = ctx.typeIdentifier().IDENTIFIER().map { it.text }
        val name = ids.first()
        // TODO Self
        val tableWithParams = ids
            .drop(1)
            .foldIndexed(typeTable) { i, tab, id ->
                tab + (id to InternalTypeSymbol(ParameterType(i)))
            }

        val typeTableWithSelf = tableWithParams + ("Self" to ExternalTypeSymbol(SelfType(), ids.size - 1))
        val (type, _) = ctx.type().accept(TypeVisitor(typeTableWithSelf, 0))

        return Pair(table, typeTable + (name to ExternalTypeSymbol(type, ids.size - 1)))
//        return Pair(table, typeTable)
    }

    override fun visitAssignment(ctx: LambdaParser.AssignmentContext): Pair<SymbolTable, TypeTable> {
        val newExpr = ctx.expression().accept(ExpressionVisitor(table, 0))
        val (type, _) = ctx.type().accept(TypeVisitor(typeTable + ("Self" to ExternalTypeSymbol(SelfType(), 0)), 0))
        val context = TypeContext(
            type,
            emptyList(),
            emptyList(),
            emptyMap()
        )
        try {
            //validateType(newExpr, type, context)
        } catch (e: Exception) {
            throw RuntimeException("Line ${ctx.start.line}", e)
        }
        return Pair(table + (ctx.IDENTIFIER().text to ExpressionSymbol(newExpr)), typeTable)
    }
}

class TypeVisitor(
    private val typeTable: TypeTable,
    private val depth: Int
    ) : LambdaBaseVisitor<Pair<Type, TypeTable>>() {
    override fun visitTypeRef(ctx: LambdaParser.TypeRefContext): Pair<Type, TypeTable> {
        val name = ctx.IDENTIFIER().text
        val paramNames = ctx.type()

        val foundType = typeTable[name]
        val (type, table1) = if(foundType == null) {
            val typ = InternalTypeSymbol(InternalType())
            Pair(typ, typeTable + (name to typ))
//            throw IllegalStateException("$name not found")
        } else {
            Pair(foundType, typeTable)
        }

        return when (type) {
            is InternalTypeSymbol -> if (paramNames.isNotEmpty()) {
                throw IllegalStateException("There should be no parameters")
            } else {
                Pair(type.type, table1)
            }
            is ExternalTypeSymbol -> {
                if (type.nbParams != paramNames.size) {
                    throw IllegalStateException("Expecting ${type.nbParams} parameters, but got ${ctx.text}")
                }
                val params = paramNames
                    .fold(emptyList<Type>()) { params, p ->
                        val (typ, _) = p.accept(this)
                        params + typ
                    }
                Pair(TypeRef(type.type, params, name), table1)
            }
        }
    }

    override fun visitLambdaType(ctx: LambdaParser.LambdaTypeContext): Pair<Type, TypeTable> {
        val (variable, table1) = ctx.lambdaVarType().accept(this)
        val (to, table2) = ctx.type().accept(TypeVisitor(table1, depth + 1))
        return Pair(LambdaType(variable, to), table2)
    }

    override fun visitLambdaVarTypeByParentes(ctx: LambdaParser.LambdaVarTypeByParentesContext): Pair<Type, TypeTable> {
        return ctx.type().accept(this)
    }
}

class ExpressionVisitor(
    private val symbolTable: SymbolTable,
    private val depth: Int
) : LambdaBaseVisitor<Expression>() {
    override fun visitSimpleCallItem(ctx: LambdaParser.SimpleCallItemContext): Expression {
        val symbolName = ctx.IDENTIFIER().text
        val symbol: Symbol = symbolTable[symbolName] ?: throw IllegalStateException("$symbolName not found")
        return when (symbol) {
            is ExpressionSymbol -> symbol.expression
            is VariableSymbol -> ClosureRef(depth - symbol.depth - 1)
        }
    }

    override fun visitParentes(ctx: LambdaParser.ParentesContext): Expression {
        return ctx.expression().accept(this)
    }

    override fun visitCallChain(ctx: LambdaParser.CallChainContext): Expression {
        return ctx.callItem()
            .map { it.accept(this) }
            .reduce { l, r -> Call(l, r) }
    }

    override fun visitLambda(ctx: LambdaParser.LambdaContext): Expression {
        val variable = ctx.IDENTIFIER().text
        val newSymbol = symbolTable + (variable to VariableSymbol(depth))
        val expr = ctx.expression().accept(ExpressionVisitor(newSymbol, depth + 1))
        return Lambda(expr)
    }
}