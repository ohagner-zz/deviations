package com.ohagner.deviations.parser

import com.ohagner.deviations.api.deviation.LineNumberParser
import spock.lang.Specification

import static org.junit.Assert.assertThat
import static org.hamcrest.CoreMatchers.is

class LineNumberParserSpec extends Specification {

    def parser = new LineNumberParser()

    //Do a matrix test here instead
    def 'parse multiple line numbers'() {
        given:
            def input = "Pendeltåg 35, 36, 37"
        when:
            def result = parser.extractLineNumbers(input)
        then:
            assertThat(result, is(["35", "36", "37"]))
    }

    void 'ignore when no match'() {
        given:
            def input = "Liljeholmen"
        when:
            def result = parser.extractLineNumbers(input)
        then:
            assertThat(result, is([]))
    }

    def 'parse single line numbers'() {
        given:
            def input = "Buss 745"
        when:
            def result = parser.extractLineNumbers(input)
        then:
            assertThat(result, is(["745"]))
    }

    def 'parse line numbers with letters'() {
        given:
            def input = "Buss 745B, 840, 842C"
        when:
            def result = parser.extractLineNumbers(input)
        then:
            assertThat(result, is(["745B", "840", "842C"]))
    }


    def 'parse multiple line numbers and transportTypes'() {
        given:
            def input = "Blåbuss 2, 3; Buss 53, 59;Pendeltåg 35"
        when:
            def result = parser.extractLineNumbers(input)
        then:
            assertThat(result, is(["2", "3", "53", "59", "35"]))
    }
}
