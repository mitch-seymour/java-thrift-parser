package com.mitchseymour.thrift.parser.ast;

import com.mitchseymour.thrift.parser.ast.Nodes.*;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.support.ValueStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Actions for manipulating the Parboiled value stack.
 */
class ParserActions {

    Action pop() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                valueStack.pop();
                return true;
            }
        };
    }

    // This is a helper node. It exists for the sole purpose of separating the argument fields
    // of a ThrowsNode from the preceding function arguments
    Action pushInterrupt() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                valueStack.push(new InterruptNode());
                return true;
            }
        };
    }

    Action pushDocumentNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                List<HeaderNode> headers = new ArrayList<>();
                List<DefinitionNode> definitions = new ArrayList<>();
                ValueStack valueStack = context.getValueStack();
                while (valueStack.size() > 0) {
                    AstNode value = (AstNode) valueStack.pop();
                    if (HeaderNode.class.isInstance(value)) {
                        headers.add((HeaderNode) value);
                    } else if (DefinitionNode.class.isInstance(value)) {
                        definitions.add((DefinitionNode) value);
                    } else {
                        valueStack.push(value);
                        break;
                    }
                }
                DocumentNode node = new DocumentNode(headers, definitions);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushHeaderNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                AstNode value = (AstNode) valueStack.pop();
                HeaderNode node = new HeaderNode(value);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushDefinitionNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                NamedNode definition = (NamedNode) valueStack.pop();
                DefinitionNode node = new DefinitionNode(definition);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushIncludeNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                IncludeNode node = new IncludeNode(context.getMatch().replaceAll("^\"|\"$", ""));
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushCppIncludeNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                String name = (String) valueStack.pop();
                CppIncludeNode node = new CppIncludeNode(name);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushGeneralNamespaceNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                LiteralNode scope = (LiteralNode) valueStack.pop();
                GeneralNamespaceNode node = new GeneralNamespaceNode(scope.value, identifier);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushPhpNamespaceNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                PhpNamespaceNode node = new PhpNamespaceNode(identifier);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushXsdNamespaceNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                XsdNamespaceNode node = new XsdNamespaceNode(identifier);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushStCategoryNamespaceNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                StCategoryNamespaceNode node = new StCategoryNamespaceNode(identifier);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushStPrefixNamespaceNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                StPrefixNamespaceNode node = new StPrefixNamespaceNode(identifier);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushConstNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                ConstValueNode value = (ConstValueNode) valueStack.pop();
                FieldTypeNode constType = (FieldTypeNode) valueStack.pop();
                ConstNode node = new ConstNode(constType, identifier, value);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushTypedefNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                BaseTypeNode defType = (BaseTypeNode) valueStack.pop();
                TypedefNode node = new TypedefNode(defType, identifier);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushEnumNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                List<EnumValueNode> values = new ArrayList<>();
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                while (valueStack.size() > 0) {
                    AstNode value = (AstNode) valueStack.pop();
                    if (EnumValueNode.class.isInstance(value)) {
                        values.add((EnumValueNode) value);
                    } else {
                        valueStack.push(value);
                        break;
                    }
                }
                EnumNode node = new EnumNode(identifier, values);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushEnumValueNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();

                Optional<IntConstNode> value;

                if (IntConstNode.class.isInstance(valueStack.peek())) {
                    value = Optional.of((IntConstNode) valueStack.pop());
                } else {
                    value = Optional.empty();
                }

                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                EnumValueNode node = new EnumValueNode(identifier, value);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushSenumNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                List<LiteralNode> values = (List<LiteralNode>) valueStack.pop();
                SenumNode node = new SenumNode(identifier, values);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushStructNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                List<FieldNode> fields = new ArrayList<>();
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                while (valueStack.size() > 0) {
                    AstNode value = (AstNode) valueStack.pop();
                    if (FieldNode.class.isInstance(value)) {
                        fields.add((FieldNode) value);
                    } else {
                        valueStack.push(value);
                        break;
                    }
                }
                Collections.reverse(fields);
                StructNode node = new StructNode(identifier, fields);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushUnionNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                List<FieldNode> fields = new ArrayList<>();
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                while (valueStack.size() > 0) {
                    AstNode value = (AstNode) valueStack.pop();
                    if (FieldNode.class.isInstance(value)) {
                        fields.add((FieldNode) value);
                    } else {
                        valueStack.push(value);
                        break;
                    }
                }
                UnionNode node = new UnionNode(identifier, fields);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushExceptionNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                List<FieldNode> fields = new ArrayList<>();
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                while (valueStack.size() > 0) {
                    AstNode value = (AstNode) valueStack.pop();
                    if (FieldNode.class.isInstance(value)) {
                        fields.add((FieldNode) value);
                    } else {
                        valueStack.push(value);
                        break;
                    }
                }
                ExceptionNode node = new ExceptionNode(identifier, fields);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushServiceNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                List<FunctionNode> functions = new ArrayList<>();
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                Optional<IdentifierNode> parent;

                if (IntConstNode.class.isInstance(valueStack.peek())) {
                    parent = Optional.of((IdentifierNode) valueStack.pop());
                } else {
                    parent = Optional.empty();
                }

                while (valueStack.size() > 0) {
                    AstNode value = (AstNode) valueStack.pop();
                    if (FunctionNode.class.isInstance(value)) {
                        functions.add((FunctionNode) value);
                    } else {
                        valueStack.push(value);
                        break;
                    }
                }

                ServiceNode node = new ServiceNode(identifier, parent, functions);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushFieldNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();
                FieldTypeNode fieldType = (FieldTypeNode) valueStack.pop();

                Optional<IntConstNode> id;
                Optional<ConstValueNode> value;

                if (IntConstNode.class.isInstance(valueStack.peek())) {
                    id = Optional.of((IntConstNode) valueStack.pop());
                } else {
                    id = Optional.empty();
                }

                if (ConstValueNode.class.isInstance(valueStack.peek())) {
                    value = Optional.of((ConstValueNode) valueStack.pop());
                } else {
                    value = Optional.empty();
                }

                XsdFieldOptionsNode xsdFieldOptions = null; // temporarily disabled
                //XsdFieldOptionsNode xsdFieldOptions = (XsdFieldOptionsNode) valueStack.pop();
                FieldNode node = new FieldNode(id, fieldType, identifier, value, xsdFieldOptions);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushXsdFieldOptionsNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                Optional<XsdAttrsNode> attrs = (Optional<XsdAttrsNode>) valueStack.pop();
                XsdFieldOptionsNode node = new XsdFieldOptionsNode(attrs);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushXsdAttrsNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                List<FieldNode> fields = (List<FieldNode>) valueStack.pop();
                XsdAttrsNode node = new XsdAttrsNode(fields);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushFunctionNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                List<FieldNode> arguments = new ArrayList<>();
                ValueStack valueStack = context.getValueStack();
                IdentifierNode identifier = (IdentifierNode) valueStack.pop();

                Optional<ThrowsNode> throws_;

                if (ThrowsNode.class.isInstance(valueStack.peek())) {
                    throws_ = Optional.of((ThrowsNode) valueStack.pop());
                } else {
                    throws_ = Optional.empty();
                }

                while (valueStack.size() > 0) {
                    AstNode value = (AstNode) valueStack.pop();
                    if (FieldNode.class.isInstance(value)) {
                        arguments.add((FieldNode) value);
                    } else {
                        valueStack.push(value);
                        break;
                    }
                }
                FunctionTypeNode functionType = (FunctionTypeNode) valueStack.pop();
                FunctionNode node = new FunctionNode(functionType, identifier, arguments, throws_);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushFunctionTypeNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                AstNode functionType = (AstNode) valueStack.pop();
                FunctionTypeNode node = new FunctionTypeNode(functionType);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushThrowsNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                List<FieldNode> fields = new ArrayList<>();
                ValueStack valueStack = context.getValueStack();
                while (valueStack.size() > 0) {
                    AstNode value = (AstNode) valueStack.pop();
                    if (FieldNode.class.isInstance(value)) {
                        fields.add((FieldNode) value);
                    } else {
                        // This should be the interrupt node
                        break;
                    }
                }
                ThrowsNode node = new ThrowsNode(fields);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushFieldTypeNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                AstNode fieldType = (AstNode) valueStack.pop();
                FieldTypeNode node = new FieldTypeNode(fieldType);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushBaseTypeNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                BaseTypeNode node = new BaseTypeNode(context.getMatch());
                context.getValueStack().push(node);
                return true;
            }
        };
    }

    Action pushMapTypeNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                Optional<CppTypeNode> cppType;
                if (CppTypeNode.class.isInstance(valueStack.peek())) {
                    cppType = Optional.of((CppTypeNode) valueStack.pop());
                } else {
                    cppType = Optional.empty();
                }
                FieldTypeNode keyType = (FieldTypeNode) valueStack.pop();
                FieldTypeNode valueType = (FieldTypeNode) valueStack.pop();
                MapTypeNode node = new MapTypeNode(keyType, valueType, cppType);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushSetTypeNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                FieldTypeNode fieldType = (FieldTypeNode) valueStack.pop();

                Optional<CppTypeNode> cppType;

                if (CppTypeNode.class.isInstance(valueStack.peek())) {
                    cppType = Optional.of((CppTypeNode) valueStack.pop());
                } else {
                    cppType = Optional.empty();
                }

                SetTypeNode node = new SetTypeNode(fieldType, cppType);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushListTypeNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                FieldTypeNode fieldType = (FieldTypeNode) valueStack.pop();

                Optional<CppTypeNode> cppType;

                if (CppTypeNode.class.isInstance(valueStack.peek())) {
                    cppType = Optional.of((CppTypeNode) valueStack.pop());
                } else {
                    cppType = Optional.empty();
                }

                ListTypeNode node = new ListTypeNode(fieldType, cppType);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushCppTypeNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                LiteralNode value = (LiteralNode) valueStack.pop();
                CppTypeNode node = new CppTypeNode(value);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushConstValueNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                if (valueStack.size() > 0) {
                    ConstValueNode node = new ConstValueNode(context.getMatch());
                    valueStack.pop(); // pop whatever the match was
                    valueStack.push(node);
                }
                return true;
            }
        };
    }

    Action pushIntConstNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                Integer value = Integer.parseInt(context.getMatch());
                IntConstNode node = new IntConstNode(value);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushDoubleConstNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                Double value = Double.parseDouble(valueStack.pop().toString());
                DoubleConstNode node = new DoubleConstNode(value);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushConstListNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                List<ConstListItemNode> values = new ArrayList<>();
                while (valueStack.size() > 0) {
                    AstNode value = (AstNode) valueStack.pop();
                    if (ConstListItemNode.class.isInstance(value)) {
                        values.add((ConstListItemNode) value);
                    } else {
                        valueStack.push(value);
                        break;
                    }
                }
                ConstListNode node = new ConstListNode(values);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushConstListItemNode(Class nodeType) {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                ConstListItemNode node = new ConstListItemNode((AstNode) valueStack.pop());
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushConstMapNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                List<ConstMapEntryNode> mappings = new ArrayList<>();
                ValueStack<AstNode> valueStack = context.getValueStack();
                while (valueStack.size() > 0) {
                    AstNode value = (AstNode) valueStack.pop();
                    if (ConstMapEntryNode.class.isInstance(value)) {
                        mappings.add((ConstMapEntryNode) value);
                    } else {
                        valueStack.push(value);
                        break;
                    }
                }
                ConstMapNode node = new ConstMapNode(mappings);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushConstMapEntryNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                ValueStack valueStack = context.getValueStack();
                AstNode value = (AstNode) valueStack.pop();
                AstNode key = (AstNode) valueStack.pop();
                ConstMapEntryNode node = new ConstMapEntryNode(key, value);
                valueStack.push(node);
                return true;
            }
        };
    }

    Action pushIdentifierNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                IdentifierNode node = new IdentifierNode(context.getMatch());
                context.getValueStack().push(node);
                return true;
            }
        };
    }

    Action pushLiteralNode() {
        return new Action() {
            @Override
            public boolean run(Context context) {
                LiteralNode node = new LiteralNode(context.getMatch().trim());
                context.getValueStack().push(node);
                return true;
            }
        };
    }
}
