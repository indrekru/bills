package com.ruubel.bills.model

import spock.lang.Specification

class GoogleTokenSpec extends Specification {

    def "when not expired, then isExpired returns false" () {
        given:
            GoogleToken token = new GoogleToken(null, null, null, null, 5l)
        when:
            boolean isExpired = token.isExpired()
        then:
            !isExpired
    }

    def "when expired, then isExpired returns true" () {
        given:
            GoogleToken token = new GoogleToken(null, null, null, null, -1l)
        when:
            boolean isExpired = token.isExpired()
        then:
            isExpired
    }

    def "when expired, then isExpired returns true 2" () {
        given:
            GoogleToken token = new GoogleToken(null, null, null, null, 0l)
        when:
            boolean isExpired = token.isExpired()
        then:
            isExpired
    }

}
