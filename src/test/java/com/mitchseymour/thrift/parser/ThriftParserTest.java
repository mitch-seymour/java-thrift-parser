package com.mitchseymour.thrift.parser;

import static org.junit.Assert.assertTrue;
import static com.mitchseymour.thrift.parser.ThriftParser.*;

import com.mitchseymour.thrift.parser.ast.Nodes;
import com.mitchseymour.thrift.parser.ast.Nodes.DocumentNode;
import java.io.IOException;
import java.util.Optional;

import org.junit.Test;

public class ThriftParserTest {

    @Test
    public void recognizer() throws IOException {
        assertTrue(parseThriftFile("/testmonkey.thrift").matched);
    }

    @Test
    public void ast() throws IOException {
        Optional<DocumentNode> parsedDocument = parseThriftFileAst("/testmonkey.thrift");
        assert(parsedDocument.isPresent());
        DocumentNode document = parsedDocument.get();
        assert(document.headers.size() > 0);
        assert(document.definitions.size() > 0);
        System.out.println(document.printTree());
    }

    @Test
    public void structAast() throws IOException {
        Optional<DocumentNode> parsedDocument = parseThriftFileAst("/struct.thrift");
        assert (parsedDocument.isPresent());
        DocumentNode document = parsedDocument.get();
        assert (document.headers.size() > 0);
        assert (document.definitions.size() == 1);
        assert(document.definitions.get(0).type.equals(Nodes.StructNode.class));
        final Nodes.StructNode structNode = (Nodes.StructNode) document.definitions.get(0).value;
        assert(structNode.fields.size() == 3);
        System.out.println(document.printTree());
    }

    @Test
    public void enumAast() throws IOException {
        Optional<DocumentNode> parsedDocument = parseThriftFileAst("/enum.thrift");
        assert(parsedDocument.isPresent());
        DocumentNode document = parsedDocument.get();
        assert(document.headers.size() > 0);
        assert(document.definitions.size() == 1);
        assert(document.definitions.get(0).type.equals(Nodes.EnumNode.class));
        final Nodes.EnumNode enumNode = (Nodes.EnumNode) document.definitions.get(0).value;
        assert(enumNode.values.size() == 3);
        assert(enumNode.values.get(0).getClass().equals(Nodes.EnumValueNode.class));
        final Nodes.EnumValueNode enumValueNode = enumNode.values.get(0);
        assert("VALUE3".equalsIgnoreCase(enumValueNode.identifier.name));
        assert(enumValueNode.value.map(intConstNode -> intConstNode.value).orElse(-1) == 2);
        System.out.println(document.printTree());
    }
}
