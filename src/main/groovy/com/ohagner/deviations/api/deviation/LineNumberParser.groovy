package com.ohagner.deviations.api.deviation

class LineNumberParser {

    final static String PATTERN = /[\w\s]*\s((?:\d*[\,\s]?)+)/

    List<String> extractLineNumbers(String scopeElements) {
        def parts = scopeElements.split(";")
        def lineNumbers = []
        parts.each { part ->
            def matcher=(part =~ PATTERN)
            def foundNumbers = matcher[0][1]
            lineNumbers.addAll(foundNumbers.split(",").collect { it.trim() })
        }

        return lineNumbers
    }
}
