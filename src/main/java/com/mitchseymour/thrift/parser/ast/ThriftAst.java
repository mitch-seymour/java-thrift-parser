package com.mitchseymour.thrift.parser.ast;

import java.util.*;

import com.mitchseymour.thrift.parser.ast.Nodes.*;
import com.mitchseymour.thrift.parser.Preprocessor;
import org.parboiled.*;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.StringVar;
import org.parboiled.support.ParsingResult;

@SuppressWarnings({"InfiniteRecursion"})
@BuildParseTree
public class ThriftAst extends BaseParser<Object> {

    ParserActions actions = new ParserActions();

    @Override
    protected Rule fromStringLiteral(String string) {
        if (string.endsWith(" ")) {
            return Sequence(
                    fromCharArray(string.trim().toCharArray()),
                    WhiteSpace());
        }
        return fromCharArray(string.toCharArray());
    }

    //================================================================================
    // Thrift IDL
    //================================================================================

    /**
     * [1] Document ::=  Header* Definition*
     */
    Rule Document() {
        // Push 1 DocumentNode onto the value stack
        return Sequence(
                WhiteSpace(),
                ZeroOrMore(Header()),
                ZeroOrMore(Definition()),
                EOI,
                actions.pushDocumentNode());
    }

    /**
     * [2] Header ::=  Include | CppInclude | Namespace
     */
    Rule Header() {
        // Push 1 HeaderNode onto the value stack
        return Sequence(
                    FirstOf(
                        Include(),
                        CppInclude(),
                        Namespace()),
                    actions.pushHeaderNode());
    }

    /**
     * [3] Include ::=  'include' Literal
     */
    Rule Include() {
        // Push 1 IncludeNode onto the value stack
        return Sequence(
                "include",
                WhiteSpace(),
                Literal(),
                actions.pushIncludeNode(),
                WhiteSpace());
    }

    /**
     * [4] CppInclude ::=  'cpp_include' Literal
     */
    Rule CppInclude() {
        // Push 1 CppIncludeNode onto the value stack
        return Sequence(
                "cpp_include",
                WhiteSpace(),
                Literal(),
                actions.pushCppIncludeNode(),
                WhiteSpace());
    }

    /**
     * [5] Namespace ::= ( 'namespace' ( NamespaceScope Identifier ) |
     * ( 'smalltalk.category' STIdentifier ) |
     * ( 'smalltalk.prefix' Identifier ) ) |
     * ( 'php_namespace' Literal ) |
     * ( 'xsd_namespace' Literal )
     */
    Rule Namespace() {
        // No direct effect on value stack
        return FirstOf(
                ScopedNamespace(),
                PhpNamespace(),
                XsdNamespace());
    }

    Rule PhpNamespace() {
        // Push 1 PhpNamespaceNode onto the value stack
        return Sequence(
                "php_namespace",
                Literal(),
                actions.pushPhpNamespaceNode());
    }

    Rule XsdNamespace() {
        // Push 1 XsdNamespaceNode onto the value stack
        return Sequence(
                "xsd_namespace",
                Literal(),
                actions.pushXsdNamespaceNode());
    }

    Rule ScopedNamespace() {
        // No direct effect on value stack
        return Sequence(
                "namespace ",
                ScopeAndId());
    }

    Rule ScopeAndId() {
        // No direct effect on value stack
        return FirstOf(
                GeneralScopeAndId(),
                StCategoryScopeAndId(),
                StPrefixScopeAndId()
        );
    }

    Rule GeneralScopeAndId() {
        // Push 1 GeneralNamespaceNode onto the value stack
        return Sequence(
                NamespaceScope(),
                Identifier(),
                actions.pushGeneralNamespaceNode());
    }

    Rule StCategoryScopeAndId() {
        // Push 1 StCategoryNamespaceNode onto the value stack
        return Sequence(
                "smalltalk.category",
                STIdentifier(),
                actions.pushStCategoryNamespaceNode());
    }

    Rule StPrefixScopeAndId() {
        // Push 1 StPrefixNamespaceNode onto the value stack
        return Sequence(
                "smalltalk.prefix",
                Identifier(),
                actions.pushStPrefixNamespaceNode());
    }

    /**
     * [6] NamespaceScope ::=  '*' | 'cpp' | 'java' | 'py' | 'perl' | 'rb' | 'cocoa' | 'csharp'
     */
    Rule NamespaceScope() {
        // Push 1 LiteralNode onto the value stack
        return Sequence(
                FirstOf(
                        "* ",
                        "cpp ",
                        "java ",
                        "py ",
                        "perl ",
                        "php ",
                        "rb ",
                        "cocoa ",
                        "csharp "),
                actions.pushLiteralNode());
    }

    /**
     * [7] Definition ::=  Const | Typedef | Enum | Senum | Struct | Union | Exception | Service
     */
    Rule Definition() {
        // Push 1 DefinitionNode onto the stack
        return Sequence(
                    FirstOf(
                        Const(),
                        Typedef(),
                        Enum(),
                        Senum(),
                        Struct(),
                        Union(),
                        Exception(),
                        Service()),
                    actions.pushDefinitionNode());
    }

    /**
     * [8] Const ::=  'const' FieldType Identifier '=' ConstValue ListSeparator?
     */
    Rule Const() {
        // Push 1 ConstNode onto the value stack
        StringVar constName = new StringVar("");
        return Sequence(
                "const ",
                FieldType(),
                Identifier(),
                actions.pop(),
                ACTION(constName.set(match())),
                "= ",
                ConstValue(),
                Optional(ListSeparator()),
                WhiteSpace(),
                push(new IdentifierNode(constName.get())),
                actions.pushConstNode());
    }

    /**
     * [9] Typedef ::=  'typedef' DefinitionType Identifier
     */
    Rule Typedef() {
        // Push 1 TypedefNode onto the value stack
        return Sequence(
                "typedef ",
                DefinitionType(),
                Identifier(),
                actions.pushTypedefNode());
    }

    /**
     * [10] Enum ::=  'enum' Identifier '{' (Identifier ('=' IntConstant)? ListSeparator?)* '}'
     */
    Rule Enum() {
        // Push 1 EnumNode onto the value stack
        StringVar enumName = new StringVar("");
        return Sequence(
                "enum ",
                Identifier(),
                actions.pop(),
                ACTION(enumName.set(match())),
                "{ ",
                ZeroOrMore(EnumValue()),
                "} ",
                push(new IdentifierNode(enumName.get())),
                actions.pushEnumNode());
    }

    Rule EnumValue() {
        // Push 1 EnumValue node onto the value stack
        return Sequence(
                Identifier(),
                Optional(EnumConst()),
                Optional(ListSeparator()),
                WhiteSpace(),
                actions.pushEnumValueNode());
    }

    Rule EnumConst() {
        // No direct effect on value stack
        return Sequence(
                "= ",
                IntConstant(),
                WhiteSpace());
    }

    /**
     * [11] Senum ::=  'senum' Identifier '{' (Literal ListSeparator?)* '}'
     */
    Rule Senum() {
        // Push 1 SenumNode onto the value stack
        return Sequence(
                "senum ",
                Identifier(),
                "{ ",
                ZeroOrMore(Sequence(Literal(), Optional(ListSeparator()))),
                "} ",
                actions.pushSenumNode());
    }

    /**
     * [12] Struct ::=  'struct' Identifier 'xsd_all'? '{' Field* '}'
     */
    Rule Struct() {
        // Push 1 StructNode onto the value stack
        StringVar structName = new StringVar("");
        return Sequence(
                "struct ",
                Identifier(),
                actions.pop(),
                ACTION(structName.set(match())),
                Optional("xsd_all "),
                "{ ",
                ZeroOrMore(Field()),
                "} ",
                push(new IdentifierNode(structName.get())),
                actions.pushStructNode());
    }

    /**
     * [13] Union ::=  'union' Identifier 'xsd_all'? '{' Field* '}'
     */
    Rule Union() {
        // Push 1 UnionNode onto the value stack
        StringVar unionName = new StringVar("");
        return Sequence(
                "union ",
                Identifier(),
                actions.pop(),
                ACTION(unionName.set(match())),
                Optional("xsd_all "),
                "{ ",
                ZeroOrMore(Field()),
                "} ",
                push(new IdentifierNode(unionName.get())),
                actions.pushUnionNode());
    }

    /**
     * [14] Exception ::=  'exception' Identifier '{' Field* '}'
     */
    Rule Exception() {
        // Push 1 ExceptionNode onto the value stack
        StringVar exceptionName = new StringVar("");
        return Sequence(
                "exception ",
                Identifier(),
                actions.pop(),
                ACTION(exceptionName.set(match())),
                "{ ",
                ZeroOrMore(Field()),
                "} ",
                push(new IdentifierNode(exceptionName.get())),
                actions.pushExceptionNode());
    }

    /**
     * [15] Service ::=  'service' Identifier ( 'extends' Identifier )? '{' Function* '}'
     */
    Rule Service() {
        // Push 1 ServiceNode onto the value stack
        StringVar serviceName = new StringVar("");
        return Sequence(
                "service ",
                Identifier(),
                actions.pop(),
                ACTION(serviceName.set(match())),
                Optional(Inheritance()),
                "{ ",
                ZeroOrMore(Function()),
                "} ",
                push(new IdentifierNode(serviceName.get())),
                actions.pushServiceNode());
    }

    Rule Inheritance() {
        // No direct effect on value stack
        return Sequence("extends ", Identifier());
    }

    /**
     * [16] Field ::=  FieldID? FieldReq? FieldType Identifier ('= ConstValue)? XsdFieldOptions ListSeparator?
     */
    Rule Field() {
        // Push 1 FieldNode onto the value stack
        return Sequence(
                Optional(FieldID()),
                Optional(FieldReq()),
                FieldType(),  // pushes FieldType onto the value stack
                Identifier(), // pushes Identifier onto the value stack
                Optional(Sequence("= ", ConstValue())),
                //XsdFieldOptions(),
                Optional(ListSeparator()),
                WhiteSpace(),
                actions.pushFieldNode());
    }

    /**
     * [17] FieldID ::=  IntConstant ':'
     */
    Rule FieldID() {
        // No direct effect on value stack
        return Sequence(IntConstant(), WhiteSpace(), ": ");
    }

    //================================================================================
    // Field Requiredness
    //================================================================================

    /**
     * [18] FieldReq ::=  'required' | 'optional'
     */
    Rule FieldReq() {
        // No effect on the value stack
        return FirstOf("required ", "optional ");
    }

    //================================================================================
    // XSD Options
    //================================================================================

    /**
     * [19] XsdFieldOptions ::=  'xsd_optional'? 'xsd_nillable'? XsdAttrs?
     */
    Rule XsdFieldOptions() {
        // Push 1 XsdFieldOptionsNode onto the value stack
        return Sequence(
                Optional("xsd_optional "),
                Optional("xsd_nillable "),
                Optional(XsdAttrs()),
                actions.pushXsdFieldOptionsNode());
    }

    /**
     * [20] XsdAttrs ::=  'xsd_attrs' '{' Field* '}'
     */
    Rule XsdAttrs() {
        // Push 1 XsdAttrsNode onto the value stack
        return Sequence(
                "xsd_attrs ",
                "{ ",
                ZeroOrMore(Field()),
                "} ",
                actions.pushXsdAttrsNode());
    }

    //================================================================================
    // Functions
    //================================================================================

    /**
     * [21] Function ::=  'oneway'? FunctionType Identifier '(' Field* ')' Throws? ListSeparator?
     */
    Rule Function() {
        // Push 1 FunctionNode onto the value stack
        StringVar functionName = new StringVar("");
        return Sequence(
                Optional("oneway "),
                FunctionType(),
                Identifier(),
                actions.pop(),
                ACTION(functionName.set(match())),
                "( ",
                ZeroOrMore(Field()),
                ") ",
                Optional(Throws()),
                Optional(ListSeparator()),
                push(new IdentifierNode(functionName.get())),
                actions.pushFunctionNode());
    }

    /**
     * [22] FunctionType ::=  FieldType | 'void'
     */
    Rule FunctionType() {
        // Push 1 FunctionType node onto the value stack
        return Sequence(
                FirstOf(
                        FieldType(),
                        Sequence("void ", push(new VoidNode()))),
                actions.pushFunctionTypeNode());
    }

    /**
     * [23] Throws ::=  'throws' '(' Field* ')'
     */
    Rule Throws() {
        // Push 1 ThrowsNode onto the value stack
        return Sequence(
                "throws ",
                "( ",
                actions.pushInterrupt(),
                ZeroOrMore(Field()),
                ") ",
                actions.pushThrowsNode());
    }


    //================================================================================
    // Types
    //================================================================================

    /**
     * [24] FieldType ::=  Identifier | BaseType | ContainerType
     */
    Rule FieldType() {
        // Push 1 FieldTypeNode onto the value stack
        return Sequence(
                FirstOf(
                        ContainerType(),
                        Identifier(),
                        BaseType()),
                actions.pushFieldTypeNode());
    }

    /**
     * [25] DefinitionType ::=  BaseType | ContainerType
     */
    Rule DefinitionType() {
        // No direct effect on value stack
        return FirstOf(BaseType(), ContainerType());
    }

    /**
     * [26] BaseType ::=  'bool' | 'byte' | 'i8' | 'i16' | 'i32' | 'i64' | 'double' | 'string' | 'binary' | 'slist'
     */
    Rule BaseType() {
        // Push 1 BaseTypeNode onto the value stack
        return Sequence(
                FirstOf(
                        "bool ",
                        "byte ",
                        "i8 ",
                        "i16 ",
                        "i32 ",
                        "i64 ",
                        "double ",
                        "string ",
                        "binary ",
                        "slist "),
                actions.pushBaseTypeNode());
    }

    /**
     * [27] ContainerType ::=  MapType | SetType | ListType
     */
    Rule ContainerType() {
        // No direct effect on value stack
        return FirstOf(
                MapType(),
                SetType(),
                ListType());
    }

    /**
     * [28] MapType ::=  'map' CppType? '<' FieldType ',' FieldType '>'
     */
    Rule MapType() {
        // Push 1 MapTypeNode onto the value stack
        return Sequence(
                "map ",
                Optional(CppType()),
                "<",
                FieldType(),
                ", ",
                FieldType(),
                "> ",
                actions.pushMapTypeNode());
    }

    /**
     * [29] SetType ::=  'set' CppType? '<' FieldType '>'
     */
    Rule SetType() {
        // Push 1 SetTypeNode onto the value stack
        return Sequence(
                "set ",
                Optional(CppType()),
                "<",
                FieldType(),
                "> ",
                actions.pushSetTypeNode());
    }

    /**
     * [30] ListType ::=  'list' '<' FieldType '>' CppType?
     */
    Rule ListType() {
        // Push 1 ListTypeNode onto the value stack
        return Sequence(
                "list ",
                "<",
                FieldType(),
                "> ",
                Optional(CppType()),
                actions.pushListTypeNode());
    }

    /**
     * [31] CppType ::=  'cpp_type' Literal
     */
    Rule CppType() {
        // Push 1 CppTypeNode onto the value stack
        return Sequence(
                "cpp_type ",
                Literal(),
                actions.pushCppTypeNode());
    }

    //================================================================================
    // Constant Values
    //================================================================================

    /**
     * [32] ConstValue ::=  IntConstant | DoubleConstant | Literal | Identifier | ConstList | ConstMap
     */
    Rule ConstValue() {
        // Push 1 ConstValue node onto the value stack
        return Sequence(
                FirstOf(
                        IntConstant(),
                        DoubleConstant(),
                        Literal(),
                        Identifier(),
                        ConstList(),
                        ConstMap()),
                actions.pushConstValueNode());
    }

    /**
     * [33] IntConstant ::=  ('+' | '-')? Digit+
     */
    Rule IntConstant() {
        // Push 1 IntConstNode onto the value stack
        return Sequence(
                Sequence(
                        Optional(NumericSign()),
                        OneOrMore(Digit())),
                actions.pushIntConstNode());
    }

    /**
     * [34] DoubleConstant ::=  ('+' | '-')? Digit* ('.' Digit+)? ( ('E' | 'e') IntConstant )?
     */
    Rule DoubleConstant() {
        // Push 1 DoubleConstNode onto the value stack
        return Sequence(
                Sequence(
                        Optional(NumericSign()),
                        ZeroOrMore(Digit()),
                        Sequence('.', OneOrMore(Digit())),
                        MaybeScientific()),
                actions.pushDoubleConstNode());
    }

    Rule MaybeScientific() {
        // To do!!
        return Optional(Sequence(FirstOf('E', 'e'), IntConstant()));
    }

    Rule NumericSign() {
        return FirstOf('+', '-');
    }

    /**
     * [35] ConstList ::=  '[' (ConstValue ListSeparator?)* ']'
     */
    Rule ConstList() {
        return Sequence(
                "[ ",
                // We can't use the ConstValue definition here because that definition
                // uses a FirstOf expression (which is the proper method), but we cannot
                // pass Rules with potentially empty values (like FirstOf...) to the
                // ZeroOrMore matcher
                Sequence(
                        ZeroOrMore(Sequence(IntConstant(), Optional(ListSeparator()),
                                actions.pushConstListItemNode(IntConstNode.class))),
                        ZeroOrMore(Sequence(DoubleConstant(), Optional(ListSeparator()),
                                actions.pushConstListItemNode(DoubleConstNode.class))),
                        ZeroOrMore(Sequence(Literal(), Optional(ListSeparator()),
                                actions.pushConstListItemNode(LiteralNode.class))),
                        ZeroOrMore(Sequence(Identifier(), Optional(ListSeparator()),
                                actions.pushConstListItemNode(IdentifierNode.class))),
                        ZeroOrMore(Sequence(ConstList(), Optional(ListSeparator()),
                                actions.pushConstListItemNode(ConstListNode.class))),
                        ZeroOrMore(Sequence(ConstMap(), Optional(ListSeparator()),
                                actions.pushConstListItemNode(ConstMapNode.class)))
                        //ZeroOrMore(Sequence(DoubleConstant(), Optional(ListSeparator()))),
                ),
                "] ",
                actions.pushConstListNode());
    }

    Rule ConstListItem() {
        // No effect on value stack
        return Sequence(
                ConstValue(),
                Optional(ListSeparator()));
    }

    /**
     * [36] ConstMap ::=  '{' (ConstValue ':' ConstValue ListSeparator?)* '}'
     */
    Rule ConstMap() {
        // Push 1 ConstMapNode onto the value stack
        return Sequence(
                "{ ",
                ZeroOrMore(ConstMapEntry()),
                "} ",
                actions.pushConstMapNode());
    }

    Rule ConstMapEntry() {
        // Push 1 ConstMapEntryNode onto the value stack
        return Sequence(
                ConstValue(),
                WhiteSpace(),
                ":",
                WhiteSpace(),
                ConstValue(),
                Optional(ListSeparator()),
                actions.pushConstMapEntryNode());
    }

    //================================================================================
    // Basic Definitions
    //================================================================================

    /**
     * [37] Literal ::=  ('"' [^"]* '"') | ("'" [^']* "'")
     */
    Rule Literal() {
        // Push 1 LiteralNode onto the value stack
        return Sequence(
                FirstOf(
                        SingleQuotedLiteral(),
                        DoubleQuotedLiteral()),
                actions.pushLiteralNode());
    }

    Rule DoubleQuotedLiteral() {
        // No effect on value stack
        return Sequence(
                "\"",
                ZeroOrMore(NoneOf("\"")),
                "\"");
    }

    Rule SingleQuotedLiteral() {
        // No effect on value stack
        return Sequence(
                "'",
                ZeroOrMore(NoneOf("'")),
                "'");
    }

    /**
     * [38] Identifier ::=  ( Letter | '_' ) ( Letter | Digit | '.' | '_' )*
     */
    Rule Identifier() {
        // Push 1 IdentifierNode onto the value stack
        return Sequence(
                Sequence(
                        FirstOf(Letter(), "_"),
                        ZeroOrMore(IndentChar())),
                actions.pushIdentifierNode(),
                WhiteSpace());
    }

    /**
     * [39] STIdentifier ::=  ( Letter | '_' ) ( Letter | Digit | '.' | '_' | '-' )*
     */
    Rule STIdentifier() {
        // Push 1 IdentifierNode onto value stack
        return Sequence(
                Sequence(
                        FirstOf(Letter(), "_"),
                        ZeroOrMore(FirstOf(IndentChar(), "-"))),
                actions.pushIdentifierNode(),
                WhiteSpace());
    }

    Rule IndentChar() {
        // No effect on value stack
        return FirstOf(Letter(), Digit(), ".", "_");
    }

    /**
     * [40] ListSeparator ::=  ',' | ';'
     */
    Rule ListSeparator() {
        // No effect on value stack
        return Sequence(AnyOf(",;"), WhiteSpace());
    }

    /**
     * [41] Letter ::=  ['A'-'Z'] | ['a'-'z']
     */
    Rule Letter() {
        // No effect on value stack
        return FirstOf(
                CharRange('a', 'z'),
                CharRange('A', 'Z'));
    }

    /**
     * [42] Digit ::=  ['0'-'9']
     */
    Rule Digit() {
        // No effect on value stack
        return CharRange('0', '9');
    }

    Rule WhiteSpace() {
        // No effect on value stack
        return ZeroOrMore(AnyOf(" \n\r\t\f"));
    }

    public Optional<DocumentNode> parseThriftIdl(String input) {
        ThriftAst parser = Parboiled.createParser(ThriftAst.class);
        input = Preprocessor.stripComments(input);
        ParsingResult<?> result = new ReportingParseRunner(parser.Document()).run(input);
        // See if the last node is a DocumentNode
        Optional<DocumentNode> document;
        if (result.valueStack.size() > 0) {
            AstNode node = (AstNode) result.valueStack.pop();
            if (DocumentNode.class.isInstance(node)) {
                document = Optional.of((DocumentNode) node);
            } else {
                document = Optional.empty();
            }
        } else {
            document = Optional.empty();
        }
        return document;
    }
}
