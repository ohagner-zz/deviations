package com.ohagner.deviations.parser

class LineNumberParser {

    final static String PATTERN = /[\w\s]*\s((?:\d*[\,\s]?)+)/

    List<String> extractLineNumbers(String scopeElements) {
        def matcher=(scopeElements =~ PATTERN)
        def lineNumbers = matcher[0][1]
        return lineNumbers.split(',').collect { it.trim() }
    }
}
