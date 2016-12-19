package com.mitchseymour.thrift.parser;

public class Preprocessor {

    public static String stripComments(String input) {
        String multiComment = "(?s)/\\*.*?\\*/";
        String trailingComment = "//.*\\n";
        return input.replaceAll(multiComment, "").replaceAll(trailingComment, "\n");
    }

}
