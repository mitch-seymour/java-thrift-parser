package com.mitchseymour.thrift.parser.ast;

import org.parboiled.support.ToStringFormatter;
import org.parboiled.trees.GraphUtils;
import org.parboiled.trees.ImmutableGraphNode;

import java.util.*;

public class Nodes {

    //================================================================================
    // Node classes
    //================================================================================

    public static class AstNode extends ImmutableGraphNode {

        public String toString() {
            return String.format("%s:", this.getClass().getSimpleName().replace("Node", ""));
        }

        @Override
        public java.util.List<AstNode> getChildren() {
            return new ArrayList<>();
        }
    }

    public static class VoidNode extends AstNode {}

    public static class CollectionNode extends NamedNode {

        public CollectionNode(FieldTypeNode fieldType) {
            super((IdentifierNode) fieldType.fieldType);
        }

    }

    public static class NamedNode extends AstNode {
        public IdentifierNode identifier;

        NamedNode(IdentifierNode identifier) {
            this.identifier = identifier;
        }

        public String getName() {
            return this.identifier.name;
        }
    }

    public static class BaseTypeNode extends AstNode {
        public String name;

        BaseTypeNode(String name) {
            this.name = name;
        }
    }

    public static class ConstListNode extends AstNode {
        public List<ConstListItemNode> values;

        public ConstListNode(List<ConstListItemNode> values) {
            this.values = values;
        }
    }

    public static class ConstListItemNode extends AstNode {
        AstNode value;

        public ConstListItemNode(AstNode value) {
            this.value = value;
        }
    }

    public static class ConstNode extends NamedNode {
        public FieldTypeNode constType;
        public ConstValueNode value;

        public String toString() {
            String name;
            if (constType.fieldType.getClass() == IdentifierNode.class) {
                name = ((IdentifierNode) constType.fieldType).name;
            } else {
                name = constType.fieldType.getClass().getSimpleName();
            }
            return String.format("Const: %s %s", name, identifier.name);
        }

        ConstNode(
                FieldTypeNode constType,
                IdentifierNode identifier,
                ConstValueNode value) {
            super(identifier);
            this.constType = constType;
            this.value = value;
        }
    }

    public static class ConstMapNode extends AstNode {
        public List<ConstMapEntryNode> mappings;

        ConstMapNode(List<ConstMapEntryNode> mappings) {
            this.mappings = mappings;
        }
    }

    public static class ConstMapEntryNode extends AstNode {
        public AstNode key;
        public AstNode value;

        public String toString() {
            return String.format("Const Map Entry: %s = %s", key, value);
        }

        ConstMapEntryNode(AstNode key, AstNode value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class ConstValueNode extends AstNode {
        public String constant;

        public String toString() {
            return String.format("%s", constant);
        }

        ConstValueNode(String constant) {
            this.constant = constant;
        }
    }

    public static class CppIncludeNode extends AstNode {
        public String name;

        CppIncludeNode(String name) {
            this.name = name;
        }
    }

    public static class CppTypeNode extends AstNode {
        public LiteralNode value;

        CppTypeNode(LiteralNode value) {
            this.value = value;
        }
    }

    public static class DefinitionNode extends AstNode {
        public NamedNode value;
        public Class type;

        @Override
        public java.util.List<AstNode> getChildren() {
            ArrayList<AstNode> children = new ArrayList<>();
            children.add(value);
            return children;
        }

        DefinitionNode(NamedNode value) {
            this.value = value;
            this.type = value.getClass();
        }
    }

    public static class DocumentNode extends AstNode {
        public List<HeaderNode> headers;
        public List<DefinitionNode> definitions;

        @Override
        public java.util.List<AstNode> getChildren() {
            ArrayList<AstNode> children = new ArrayList<>();
            children.addAll(headers);
            children.addAll(definitions);
            return children;
        }

        DocumentNode(List<HeaderNode> headers, List<DefinitionNode> definitions) {
            this.headers = headers;
            this.definitions = definitions;
            Collections.reverse(headers);
            Collections.reverse(definitions);
        }

        public void addDefinitions(List<DefinitionNode> definitions_) {
            if (definitions_.size() > 0) {
                this.definitions.addAll(definitions_);
            }
        }

        public void addHeaders(List<HeaderNode> headers_) {
            if (headers_.size() > 0) {
                this.headers.addAll(headers_);
            }
        }

        public List<FieldNode> getFieldsForStruct(Class cls) {
            return getFieldsForStruct(cls.getSimpleName());
        }

        public List<FieldNode> getFieldsForStruct(String findStruct) {
            for (DefinitionNode node : definitions) {
                if(!StructNode.class.isInstance(node.value)) {
                    continue;
                }
                StructNode struct = (StructNode) node.value;
                if (findStruct.equals(struct.identifier.name)) {
                    return struct.fields;
                }
            }
            throw new RuntimeException(String.format("Struct not found: %s", findStruct));
        }

        public List<String> getIncludeFiles() {
            List<String> includes = new ArrayList<>();
            for (HeaderNode header : headers) {
                if (IncludeNode.class.isInstance(header.value)) {
                    IncludeNode include = (IncludeNode) header.value;
                    includes.add(include.value);
                }
            }
            return includes;
        }

        public String printTree() {
            return GraphUtils.printTree(this, new ToStringFormatter<AstNode>());
        }
    }

    public static class DoubleConstNode extends AstNode {
        public Double value;

        DoubleConstNode(Double value) {
            this.value = value;
        }
    }

    public static class EnumNode extends NamedNode {
        public List<EnumValueNode> values;

        public String toString() {
            return String.format("Enum: %s", identifier.name);
        }

        @Override
        public java.util.List<AstNode> getChildren() {
            ArrayList<AstNode> children = new ArrayList<>();
            children.addAll(values);
            return children;
        }

        EnumNode(IdentifierNode identifier, List<EnumValueNode> values) {
            super(identifier);
            this.values = values;
        }
    }

    public static class EnumValueNode extends NamedNode {
        public Optional<IntConstNode> value;

        public String toString() {
            String val;
            if (value.isPresent()) {
                val = String.format(" (%d)", value.get().value);
            } else {
                val = "";
            }
            return String.format("Enum Value: %s%s", identifier.name, val);
        }

        EnumValueNode(IdentifierNode identifier, Optional<IntConstNode> value) {
            super(identifier);
            this.value = value;
        }
    }

    public static class ExceptionNode extends NamedNode {
        public List<FieldNode> fields;

        public String toString() {
            return String.format("Exception: %s", identifier.name);
        }

        ExceptionNode(IdentifierNode identifier, List<FieldNode> fields) {
            super(identifier);
            this.fields = fields;
        }
    }

    public static class FieldNode extends NamedNode {
        public Optional<IntConstNode> id;
        public FieldTypeNode fieldType;
        public Optional<ConstValueNode> value;
        public XsdFieldOptionsNode xsdFieldOptions;

        public String toString() {
            String field;
            String idStr;
            if (IdentifierNode.class.isInstance(fieldType.fieldType)) {
                field = ((IdentifierNode) fieldType.fieldType).name;
            } else {
                field = fieldType.fieldType.getClass().getSimpleName();
            }

            if (id.isPresent()) {
                idStr = String.format("%d: ", id.get().value);
            } else {
                idStr = "";
            }
            return String.format("Field: %s%s %s", idStr, field, identifier.name);
        }

        FieldNode(Optional<IntConstNode> id,
                  FieldTypeNode fieldType,
                  IdentifierNode identifier,
                  Optional<ConstValueNode> value,
                  XsdFieldOptionsNode xsdFieldOptions) {
            super(identifier);
            this.id = id;
            this.fieldType = fieldType;
            this.value = value;
            this.xsdFieldOptions = xsdFieldOptions;

        }
    }

    public static class FieldTypeNode extends AstNode {
        public AstNode fieldType;

        public String toString() {
            return String.format("Field Type: %s", fieldType.getClass().getSimpleName());
        }

        FieldTypeNode(AstNode fieldType) {
            this.fieldType = fieldType;
        }
    }

    public static class FunctionArgumentsNode extends AstNode {
        public List<FieldNode> arguments;

        public String toString() {
            return String.format("Arguments:");
        }

        @Override
        public java.util.List<AstNode> getChildren() {
            ArrayList<AstNode> children = new ArrayList<>();
            children.addAll(arguments);
            return children;
        }

        FunctionArgumentsNode(List<FieldNode> arguments) {
            this.arguments = arguments;
        }
    }

    public static class FunctionNode extends NamedNode {
        public FunctionTypeNode functionType;
        public List<FieldNode> arguments;
        public Optional<ThrowsNode> throws_;

        public String toString() {
            return String.format("Function: %s", identifier.name);
        }

        @Override
        public java.util.List<AstNode> getChildren() {
            ArrayList<AstNode> children = new ArrayList<>();
            AstNode args = new FunctionArgumentsNode(arguments);
            children.add(args);
            if (throws_.isPresent()) {
                children.add(throws_.get());
            }
            return children;
        }

        FunctionNode(FunctionTypeNode functionType,
                     IdentifierNode identifier,
                     List<FieldNode> arguments,
                     Optional<ThrowsNode> throws_) {
            super(identifier);
            this.functionType = functionType;
            this.arguments = arguments;
            this.throws_ = throws_;
            Collections.reverse(arguments);
        }
    }

    public static class FunctionTypeNode extends AstNode {
        public AstNode functionType;

        FunctionTypeNode(AstNode functionType) {
            this.functionType = functionType;
        }
    }

    public static class GeneralNamespaceNode extends NamedNode {
        public  String scope;

        public String toString() {
            return String.format("General Namespace: %s %s", scope, identifier.name);
        }

        GeneralNamespaceNode(String scope, IdentifierNode identifier) {
            super(identifier);
            this.scope = scope;
        }
    }

    public static class HeaderNode extends AstNode {
        public AstNode value;
        public Class type;

        @Override
        public java.util.List<AstNode> getChildren() {
            return Arrays.asList(value);
        }

        HeaderNode(AstNode value) {
            this.value = value;
            this.type = value.getClass();
        }
    }

    public static class IdentifierNode extends AstNode {
        public String name;

        public String toString() {
            return String.format("Identifier: %s", name);
        }

        public IdentifierNode(String name) {
            this.name = name.trim();
        }
    }

    public static class IncludeNode extends AstNode {
        public String value;

        public String toString() {
            return String.format("Include: %s", value);
        }

        IncludeNode(String value) {
            this.value = value;
        }
    }

    public static class IntConstNode extends AstNode {
        public Integer value;

        IntConstNode(Integer value) {
            this.value = value;
        }
    }

    public static class InterruptNode extends AstNode {}

    public static class ListTypeNode extends CollectionNode {
        public FieldTypeNode fieldType;
        public Optional<CppTypeNode> cppType;

        public String toString() {
            return String.format("List: %s", fieldType);
        }

        ListTypeNode(FieldTypeNode fieldType, Optional<CppTypeNode> cppType) {
            super(fieldType);
            this.fieldType = fieldType;
            this.cppType = cppType;
        }
    }

    public static class LiteralNode extends AstNode {
        public String value;

        public String toString() {
            return String.format("Literal: %s", value);
        }
        LiteralNode(String value) {
            this.value = value;
        }
    }

    public static class MapTypeNode extends CollectionNode {
        public Optional<CppTypeNode> cppType;
        public FieldTypeNode keyType;
        public FieldTypeNode valueType;

        MapTypeNode(FieldTypeNode keyType,
                    FieldTypeNode valueType,
                    Optional<CppTypeNode> cppType) {
            super(keyType);
            this.cppType = cppType;
            this.keyType = keyType;
            this.valueType = valueType;
        }
    }

    public static class PhpNamespaceNode extends NamedNode {

        PhpNamespaceNode(IdentifierNode identifier) {
            super(identifier);
        }
    }

    public static class SenumNode extends NamedNode {
        public List<LiteralNode> values;

        SenumNode(IdentifierNode identifier, List<LiteralNode> values) {
            super(identifier);
            this.values = values;
        }
    }

    public static class ServiceNode extends NamedNode {
        public Optional<IdentifierNode> parent;
        public List<FunctionNode> functions;

        public String toString() {
            return String.format("Service: %s", identifier.name);
        }

        @Override
        public java.util.List<AstNode> getChildren() {
            ArrayList<AstNode> children = new ArrayList<>();
            children.addAll(functions);
            return children;
        }

        ServiceNode(IdentifierNode identifier,
                    Optional<IdentifierNode> parent,
                    List<FunctionNode> functions) {
            super(identifier);
            this.parent = parent;
            this.functions = functions;
            Collections.reverse(functions);
        }
    }

    public static class SetTypeNode extends CollectionNode {
        public Optional<CppTypeNode> cppType;
        public FieldTypeNode fieldType;

        SetTypeNode(FieldTypeNode fieldType, Optional<CppTypeNode> cppType) {
            super(fieldType);
            this.cppType = cppType;
            this.fieldType = fieldType;
        }
    }

    public static class StCategoryNamespaceNode extends NamedNode {

        StCategoryNamespaceNode(IdentifierNode identifier) {
            super(identifier);
        }
    }

    public static class StPrefixNamespaceNode extends NamedNode {

        StPrefixNamespaceNode(IdentifierNode identifier) {
            super(identifier);
        }
    }

    public static class StructNode extends NamedNode {
        public List<FieldNode> fields;

        public String toString() {
            return String.format("Struct: %s", identifier.name);
        }

        @Override
        public java.util.List<AstNode> getChildren() {
            ArrayList<AstNode> children = new ArrayList<>();
            children.addAll(fields);
            return children;
        }

        StructNode(IdentifierNode identifier, List<FieldNode> fields) {
            super(identifier);
            this.fields = fields;
        }
    }

    public static class ThrowsNode extends AstNode {
        public List<FieldNode> fields;

        public String toString() {
            return "Throws: ";
        }

        @Override
        public java.util.List<AstNode> getChildren() {
            ArrayList<AstNode> children = new ArrayList<>();
            children.addAll(fields);
            return children;
        }

        ThrowsNode(List<FieldNode> fields) {
            this.fields = fields;
            Collections.reverse(fields);
        }
    }

    public static class TypedefNode extends NamedNode {
        public BaseTypeNode defType;

        public String toString() {
            return String.format("Type Def: %s %s", defType.name, identifier.name);
        }

        TypedefNode(BaseTypeNode defType, IdentifierNode identifier) {
            super(identifier);
            this.defType = defType;
        }
    }

    public static class UnionNode extends NamedNode {
        public List<FieldNode> fields;

        public String toString() {
            return String.format("Union: %s", identifier.name);
        }

        @Override
        public java.util.List<AstNode> getChildren() {
            ArrayList<AstNode> children = new ArrayList<>();
            children.addAll(fields);
            return children;
        }

        UnionNode(IdentifierNode identifier, List<FieldNode> fields) {
            super(identifier);
            this.fields = fields;
        }
    }

    public static class XsdNamespaceNode extends NamedNode {

        XsdNamespaceNode(IdentifierNode identifier) {
            super(identifier);
        }
    }

    public static class XsdFieldOptionsNode extends AstNode {
        public Optional<XsdAttrsNode> attrs;

        XsdFieldOptionsNode(Optional<XsdAttrsNode> attrs) {
            this.attrs = attrs;
        }
    }

    public static class XsdAttrsNode extends AstNode {
        public List<FieldNode> fields;

        XsdAttrsNode(List<FieldNode> fields) {
            this.fields = fields;
        }
    }
}
