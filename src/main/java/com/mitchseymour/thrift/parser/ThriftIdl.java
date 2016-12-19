package com.mitchseymour.thrift.parser;

import org.parboiled.annotations.BuildParseTree;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.StringVar;

@SuppressWarnings({"InfiniteRecursion"})
@BuildParseTree
public class ThriftIdl extends BaseParser<Object> {

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
        return Sequence(
                WhiteSpace(),
                ZeroOrMore(Header()),
                ZeroOrMore(Definition()),
                EOI);
    }

    /**
     * [2] Header ::=  Include | CppInclude | Namespace
     */
    Rule Header() {
        return FirstOf(
                Include(),
                CppInclude(),
                Namespace());
    }

    /**
     * [3] Include ::=  'include' Literal
     */
    Rule Include() {
        return Sequence(
                "include",
                WhiteSpace(),
                Literal(),
                WhiteSpace());
    }

    /**
     * [4] CppInclude ::=  'cpp_include' Literal
     */
    Rule CppInclude() {
        return Sequence(
                "cpp_include",
                WhiteSpace(),
                Literal(),
                WhiteSpace());
    }

    /**
     * [5] Namespace ::= ( 'namespace' ( NamespaceScope Identifier ) |
     *                                 ( 'smalltalk.category' STIdentifier ) |
     *                                 ( 'smalltalk.prefix' Identifier ) ) |
     *                   ( 'php_namespace' Literal ) |
     *                   ( 'xsd_namespace' Literal )
     */
    Rule Namespace() {
        return FirstOf(
                ScopedNamespace(),
                Sequence("php_namespace", Literal()),
                Sequence("xsd_namespace", Literal()));
    }

    Rule ScopedNamespace() {
        return Sequence(
                "namespace ",
                ScopeAndId());
    }

    Rule ScopeAndId() {
        return FirstOf(
                Sequence(NamespaceScope(), Identifier()),
                Sequence("smalltalk.category", STIdentifier()),
                Sequence("smalltalk.prefix", Identifier())
        );
    }

    /**
     * [6] NamespaceScope ::=  '*' | 'cpp' | 'java' | 'py' | 'perl' | 'rb' | 'cocoa' | 'csharp'
     */
    Rule NamespaceScope() {
        return FirstOf(
                "* ",
                "cpp ",
                "java ",
                "py ",
                "perl ",
                "php ",
                "rb ",
                "cocoa ",
                "csharp ");
    }

    /**
     * [7] Definition ::=  Const | Typedef | Enum | Senum | Struct | Union | Exception | Service
     */
    Rule Definition() {
        return FirstOf(
                Const(),
                Typedef(),
                Enum(),
                Senum(),
                Struct(),
                Union(),
                Exception(),
                Service());
    }

    /**
     * [8] Const ::=  'const' FieldType Identifier '=' ConstValue ListSeparator?
     */
    Rule Const() {
        return Sequence(
                "const ",
                FieldType(),
                Identifier(),
                "= ",
                ConstValue(),
                Optional(ListSeparator()),
                WhiteSpace());
    }

    /**
     * [9] Typedef ::=  'typedef' DefinitionType Identifier
     */
    Rule Typedef() {
        return Sequence(
                "typedef ",
                DefinitionType(),
                Identifier());
    }

    /**
     * [10] Enum ::=  'enum' Identifier '{' (Identifier ('=' IntConstant)? ListSeparator?)* '}'
     */
    Rule Enum() {
        return Sequence(
                "enum ",
                Identifier(),
                "{ ",
                ZeroOrMore(EnumValue()),
                "} ");
    }

    Rule EnumValue() {
        return Sequence(
                Identifier(),
                Optional(EnumConst()),
                Optional(ListSeparator()),
                WhiteSpace());
    }

    Rule EnumConst() {
        return Sequence(
                "= ",
                IntConstant(),
                WhiteSpace());
    }

    /**
     * [11] Senum ::=  'senum' Identifier '{' (Literal ListSeparator?)* '}'
     */
    Rule Senum() {
        return Sequence(
                "senum ",
                Identifier(),
                "{ ",
                ZeroOrMore(Sequence(Literal(), Optional(ListSeparator()))),
                "} ");
    }

    /**
     * [12] Struct ::=  'struct' Identifier 'xsd_all'? '{' Field* '}'
     */
    Rule Struct() {
        StringVar theval = new StringVar(" ");
        return Sequence(
                "struct ",
                Identifier(),
                Optional("xsd_all "),
                "{ ",
                ZeroOrMore(Field()),
                "} ");
    }

    /**
     * [13] Union ::=  'union' Identifier 'xsd_all'? '{' Field* '}'
     */
    Rule Union() {
        return Sequence(
                "union ",
                Identifier(),
                Optional("xsd_all "),
                "{ ",
                ZeroOrMore(Field()),
                "} "
        );
    }

    /**
     * [14] Exception ::=  'exception' Identifier '{' Field* '}'
     */
    Rule Exception() {
        return Sequence(
                "exception ",
                Identifier(),
                "{ ",
                ZeroOrMore(Field()),
                "} ");
    }

    /**
     * [15] Service ::=  'service' Identifier ( 'extends' Identifier )? '{' Function* '}'
     */
    Rule Service() {
        return Sequence(
                "service ",
                Identifier(),
                Optional(Inheritance()),
                "{ ",
                ZeroOrMore(Function()),
                "} ");
    }

    Rule Inheritance() {
        return Sequence("extends ", Identifier());
    }

    /**
     * [16] Field ::=  FieldID? FieldReq? FieldType Identifier ('= ConstValue)? XsdFieldOptions ListSeparator?
     */
    Rule Field() {
        return Sequence(
                Optional(FieldID()),
                Optional(FieldReq()),
                FieldType(),
                Identifier(),
                Optional(Sequence("= ", ConstValue())),
                XsdFieldOptions(),
                Optional(ListSeparator()),
                WhiteSpace());
    }

    /**
     * [17] FieldID ::=  IntConstant ':'
     */
    Rule FieldID() {
        return Sequence(IntConstant(), ": ");
    }

    //================================================================================
    // Field Requiredness
    //================================================================================

    /**
     * [18] FieldReq ::=  'required' | 'optional'
     */
    Rule FieldReq() {
        return FirstOf("required ", "optional ");
    }

    //================================================================================
    // XSD Options
    //================================================================================
    /**
     * [19] XsdFieldOptions ::=  'xsd_optional'? 'xsd_nillable'? XsdAttrs?
     */
    Rule XsdFieldOptions() {
        return Sequence(
                Optional("xsd_optional "),
                Optional("xsd_nillable "),
                Optional(XsdAttrs()));
    }

    /**
     * [20] XsdAttrs ::=  'xsd_attrs' '{' Field* '}'
     */
    Rule XsdAttrs() {
        return Sequence(
                "xsd_attrs ",
                "{ ",
                ZeroOrMore(Field()),
                "} ");
    }

    //================================================================================
    // Functions
    //================================================================================

    /**
     * [21] Function ::=  'oneway'? FunctionType Identifier '(' Field* ')' Throws? ListSeparator?
     */
    Rule Function() {
        return Sequence(
                Optional("oneway "),
                FunctionType(),
                Identifier(),
                "( ",
                ZeroOrMore(Field()),
                ") ",
                Optional(Throws()),
                Optional(ListSeparator()));
    }

    /**
     * [22] FunctionType ::=  FieldType | 'void'
     */
    Rule FunctionType() {
        return FirstOf(FieldType(), "void ");
    }

    /**
     * [23] Throws ::=  'throws' '(' Field* ')'
     */
    Rule Throws() {
        return Sequence(
                "throws ",
                "( ",
                ZeroOrMore(Field()),
                ") ");
    }


    //================================================================================
    // Types
    //================================================================================

    /**
     * [24] FieldType ::=  Identifier | BaseType | ContainerType
     */
    Rule FieldType() {
        return FirstOf(
                ContainerType(),
                Identifier(),
                BaseType());
    }

    /**
     * [25] DefinitionType ::=  BaseType | ContainerType
     */
    Rule DefinitionType() {
        return FirstOf(BaseType(), ContainerType());
    }

    /**
     * [26] BaseType ::=  'bool' | 'byte' | 'i8' | 'i16' | 'i32' | 'i64' | 'double' | 'string' | 'binary' | 'slist'
     */
    Rule BaseType() {
        return FirstOf(
                "bool ",
                "byte ",
                "i8 ",
                "i16 ",
                "i32 ",
                "i64 ",
                "double ",
                "string ",
                "binary ",
                "slist ");
    }

    /**
     * [27] ContainerType ::=  MapType | SetType | ListType
     */
    Rule ContainerType() {
        return FirstOf(
                MapType(),
                SetType(),
                ListType());
    }

    /**
     * [28] MapType ::=  'map' CppType? '<' FieldType ',' FieldType '>'
     */
    Rule MapType() {
        return Sequence(
                "map ",
                Optional(CppType()),
                "<",
                FieldType(),
                ", ",
                FieldType(),
                "> ");
    }

    /**
     * [29] SetType ::=  'set' CppType? '<' FieldType '>'
     */
    Rule SetType() {
        return Sequence(
                "set ",
                Optional(CppType()),
                "<",
                FieldType(),
                "> ");
    }

    /**
     * [30] ListType ::=  'list' '<' FieldType '>' CppType?
     */
    Rule ListType() {
        return Sequence(
                "list ",
                "<",
                FieldType(),
                "> ",
                Optional(CppType()));
    }

    /**
     * [31] CppType ::=  'cpp_type' Literal
     */
    Rule CppType() {
        return Sequence("cpp_type ", Literal());
    }

    //================================================================================
    // Constant Values
    //================================================================================

    /**
     * [32] ConstValue ::=  IntConstant | DoubleConstant | Literal | Identifier | ConstList | ConstMap
     */
    Rule ConstValue() {
        return FirstOf(
                IntConstant(),
                Literal(),
                Identifier(),
                ConstList(),
                ConstMap(),
                DoubleConstant());
    }

    /**
     * [33] IntConstant ::=  ('+' | '-')? Digit+
     */
    Rule IntConstant() {
        return Sequence(
                Optional(NumericSign()),
                OneOrMore(Digit()));
    }

    /**
     * [34] DoubleConstant ::=  ('+' | '-')? Digit* ('.' Digit+)? ( ('E' | 'e') IntConstant )?
     */
    Rule DoubleConstant() {
        return Sequence(
                Optional(NumericSign()),
                ZeroOrMore(Digit()),
                Optional(Sequence('.', OneOrMore(Digit()))),
                Optional(Sequence(FirstOf('E', 'e'), IntConstant()))
        );
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
                        ZeroOrMore(Sequence(IntConstant(), Optional(ListSeparator()))),
                        ZeroOrMore(Sequence(Literal(), Optional(ListSeparator()))),
                        ZeroOrMore(Sequence(Identifier(), Optional(ListSeparator()))),
                        ZeroOrMore(Sequence(ConstList(), Optional(ListSeparator()))),
                        ZeroOrMore(Sequence(ConstMap(), Optional(ListSeparator())))
                        //ZeroOrMore(Sequence(DoubleConstant(), Optional(ListSeparator())))
                        ),
                "] ");
    }

    /**
     * [36] ConstMap ::=  '{' (ConstValue ':' ConstValue ListSeparator?)* '}'
     */
    Rule ConstMap() {
        return Sequence(
                "{ ",
                ZeroOrMore(ConstMapEntry()),
                WhiteSpace(),
                "} ");
    }

    Rule ConstMapEntry() {
        return Sequence(
                ConstValue(),
                WhiteSpace(),
                ":",
                WhiteSpace(),
                ConstValue(),
                Optional(ListSeparator()));
    }

    //================================================================================
    // Basic Definitions
    //================================================================================

    /**
     * [37] Literal ::=  ('"' [^"]* '"') | ("'" [^']* "'")
     */
    Rule Literal() {
        return FirstOf(
                SingleQuotedLiteral(),
                DoubleQuotedLiteral());
    }

    Rule DoubleQuotedLiteral() {
        return Sequence(
                "\"",
                ZeroOrMore(NoneOf("\"")),
                "\"");
    }

    Rule SingleQuotedLiteral() {
        return Sequence(
                "'",
                ZeroOrMore(NoneOf("'")),
                "'");
    }

    /**
     * [38] Identifier ::=  ( Letter | '_' ) ( Letter | Digit | '.' | '_' )*
     */
    Rule Identifier() {
        return Sequence(
                FirstOf(Letter(), "_"),
                ZeroOrMore(IndentChar()),
                WhiteSpace());
    }

    /**
     * [39] STIdentifier ::=  ( Letter | '_' ) ( Letter | Digit | '.' | '_' | '-' )*
     */
    Rule STIdentifier() {
        return Sequence(
                FirstOf(Letter(), "_"),
                ZeroOrMore(FirstOf(IndentChar(), "-")),
                WhiteSpace());
    }

    Rule IndentChar() {
        return FirstOf(Letter(), Digit(), ".", "_");
    }

    /**
     *[40] ListSeparator ::=  ',' | ';'
     */
    Rule ListSeparator() {
        return Sequence(AnyOf(",;"), WhiteSpace());
    }

    /**
     * [41] Letter ::=  ['A'-'Z'] | ['a'-'z']
     */
    Rule Letter() {
        return FirstOf(
                CharRange('a', 'z'),
                CharRange('A', 'Z'));
    }

    /**
     * [42] Digit ::=  ['0'-'9']
     */
    Rule Digit() {
        return CharRange('0', '9');
    }

    Rule WhiteSpace() {
        return ZeroOrMore(AnyOf(" \n\r\t\f"));
    }

}
