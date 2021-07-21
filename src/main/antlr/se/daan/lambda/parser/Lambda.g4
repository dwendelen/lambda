grammar Lambda;

program             : programItem*;

programItem         : assignment
                    | typeDef;

typeDef             : typeIdentifier ':' type WS*;

type                : typeRef
                    | lambdaType
                    ;

lambdaType          : lambdaVarType '->' type;

lambdaVarType       : typeRef        #lambdaVarTypeById
                    | '(' type ')'          #lambdaVarTypeByParentes
                    ;

typeRef             : IDENTIFIER (('.') type)*;

typeIdentifier      : IDENTIFIER (('.') IDENTIFIER)*;

assignment          : IDENTIFIER ':' type '=' expression WS*;

expression          : callChain
                    | lambda
                    ;

lambda              : IDENTIFIER '->' expression;

callChain           : callItem ('.' callItem)*
                    ;

callItem            : '(' expression ')'    # parentes
                    | IDENTIFIER            # simpleCallItem
                    ;

IDENTIFIER  : [a-zA-Z0-9_-]+;
WS          : [ \t\n\r]+ -> skip;