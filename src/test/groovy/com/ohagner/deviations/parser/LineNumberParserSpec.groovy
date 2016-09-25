package com.ohagner.deviations.parser

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
}
