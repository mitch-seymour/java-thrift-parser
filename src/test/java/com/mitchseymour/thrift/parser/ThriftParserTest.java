package com.mitchseymour.thrift.parser;

import static org.junit.Assert.assertTrue;
import static com.mitchseymour.thrift.parser.ThriftParser.*;
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
}
