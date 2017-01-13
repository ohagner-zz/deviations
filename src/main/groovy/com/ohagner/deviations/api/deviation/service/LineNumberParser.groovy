package com.ohagner.deviations.api.deviation.service

class LineNumberParser {

    final static String PATTERN = /[A-Öa-ö\s]*\s((?:[\dA-Öa-ö]*[\,\s]?)+)/

    List<String> extractLineNumbers(String scopeElements) {
        def parts = scopeElements.split(";")
        def lineNumbers = []
        parts.each { part ->
            def matcher=(part =~ PATTERN)
            if(matcher.matches()) {
                def foundNumbers = matcher[0][1]
                lineNumbers.addAll(foundNumbers.split(",").collect { it.trim() })
            }
        }

        return lineNumbers
    }
}
