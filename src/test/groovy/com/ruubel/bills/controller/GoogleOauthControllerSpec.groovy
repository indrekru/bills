package com.ruubel.bills.controller

import com.ruubel.bills.model.GoogleNonce
import com.ruubel.bills.model.GoogleToken
import com.ruubel.bills.model.User
import com.ruubel.bills.service.GoogleTokenService
import com.ruubel.bills.service.UserService
import com.sun.net.httpserver.HttpPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.Principal
import java.time.Instant

class GoogleOauthControllerSpec extends Specification {

    GoogleTokenService googleTokenService
    UserService userService
    GoogleOauthController controller

    def setup () {
        googleTokenService = Mock(GoogleTokenService)
        userService = Mock(UserService)
        controller = new GoogleOauthController(googleTokenService, userService)
    }

    def "when no user found, then BAD_REQUEST" () {
        given:
            Principal principal = new HttpPrincipal("username", "realm")
        when:
            ResponseEntity response = controller.nonce(principal)
        then:
            1 * userService.loadUserByUsername(principal.getName()) >> null
            0 * googleTokenService.deleteAllUserNonces(_)
            response.statusCode == HttpStatus.BAD_REQUEST
    }

    def "when user found, then does all necessary" () {
        given:
            UUID nonceId = UUID.randomUUID()
            Principal principal = new HttpPrincipal("username", "realm")
        when:
            ResponseEntity response = controller.nonce(principal)
        then:
            1 * userService.loadUserByUsername(principal.getName()) >> new User()
            1 * googleTokenService.deleteAllUserNonces(_)
            1 * googleTokenService.createNonce(*_) >> new GoogleNonce(id: nonceId)
            response.statusCode == HttpStatus.OK
            response.body.toString() == "[nonce:" + nonceId + "]"
    }

    def "when nonce not found in DB, then fails" () {
        given:
            UUID nonceId = UUID.randomUUID()
            HttpServletResponse response = Mock(HttpServletResponse)
        when:
            String result = controller.connect(nonceId, response)
        then:
            1 * googleTokenService.findNonceById(nonceId) >> Optional.empty()
            result == "redirect:/error"
    }

    def "when nonce found in DB but expired, then fails" () {
        given:
            UUID nonceId = UUID.randomUUID()
            GoogleNonce nonce = new GoogleNonce(id: nonceId, expiresAt: Instant.now().minusSeconds(2))
            HttpServletResponse response = Mock(HttpServletResponse)
        when:
            String result = controller.connect(nonceId, response)
        then:
            1 * googleTokenService.findNonceById(nonceId) >> Optional.of(nonce)
            result == "redirect:/error"
    }

    def "when nonce found in DB and not expired, then succeeds" () {
        given:
            UUID nonceId = UUID.randomUUID()
            GoogleNonce nonce = new GoogleNonce(id: nonceId, expiresAt: Instant.now().plusSeconds(4))
            HttpServletResponse response = Mock(HttpServletResponse)
            controller.CLIENT_ID = "client"
            controller.SCOPE = "scope"
            controller.REDIRECT_URI = "https://rate.ee"
        when:
            String result = controller.connect(nonceId, response)
        then:
            1 * googleTokenService.findNonceById(nonceId) >> Optional.of(nonce)
            response.addCookie(_) >> {Cookie cookie ->
                assert cookie.getMaxAge() == 3
                assert cookie.getDomain() == "gmail-read.herokuapp.com"
                assert cookie.getPath() == "/"
                assert cookie.getSecure() == true
                assert cookie.isHttpOnly() == true
                assert cookie.getValue() == nonceId.toString()
            }
            result == "redirect:https://accounts.google.com/o/oauth2/v2/auth?client_id=client&response_type=code&scope=scope&redirect_uri=https://rate.ee&access_type=offline"
    }

    def "when no cookies found, then fails" () {
        given:
            HttpServletRequest request = new MockHttpServletRequest()
            HttpServletResponse response = new MockHttpServletResponse()
        when:
            String result = controller.callback("code", request, response)
        then:
            0 * googleTokenService.findNonceById(_)
            0 * googleTokenService.getAccessToken(*_)
            result == "redirect:/error"
    }

    def "when cookie found but not in DB, then fails" () {
        given:
            UUID nonceId = UUID.randomUUID();
            HttpServletRequest request = new MockHttpServletRequest()
            request.setCookies(new Cookie("nonce", nonceId.toString()))
            HttpServletResponse response = new MockHttpServletResponse()
        when:
            String result = controller.callback("code", request, response)
        then:
            1 * googleTokenService.findNonceById(_) >> Optional.empty()
            0 * googleTokenService.getAccessToken(*_)
            result == "redirect:/error"
    }

    def "when nonce found but expired, then fails" () {
        given:
            UUID nonceId = UUID.randomUUID()
            GoogleNonce nonce = new GoogleNonce(id: nonceId, expiresAt: Instant.now().minusSeconds(2))
            HttpServletRequest request = new MockHttpServletRequest()
            request.setCookies(new Cookie("nonce", nonceId.toString()))
            HttpServletResponse response = new MockHttpServletResponse()
        when:
            String result = controller.callback("code", request, response)
        then:
            1 * googleTokenService.findNonceById(_) >> Optional.of(nonce)
            0 * googleTokenService.getAccessToken(*_)
            result == "redirect:/error"
    }

    def "when nonce found but fails getting google token, then fails" () {
        given:
            UUID nonceId = UUID.randomUUID()
            GoogleNonce nonce = new GoogleNonce(id: nonceId, expiresAt: Instant.now().plusSeconds(2))
            HttpServletRequest request = new MockHttpServletRequest()
            request.setCookies(new Cookie("nonce", nonceId.toString()))
            HttpServletResponse response = new MockHttpServletResponse()
        when:
            String result = controller.callback("code", request, response)
        then:
            1 * googleTokenService.findNonceById(_) >> Optional.of(nonce)
            1 * googleTokenService.getAccessToken(*_) >> null
            0 * googleTokenService.deleteAllUserNonces(_)
            result == "redirect:/error"
    }

    def "when nonce found and gets google token, then deletes all nonces and cookie and succeeds" () {
        given:
            UUID nonceId = UUID.randomUUID()
            GoogleNonce nonce = new GoogleNonce(id: nonceId, expiresAt: Instant.now().plusSeconds(2))
            HttpServletRequest request = new MockHttpServletRequest()
            request.setCookies(new Cookie("nonce", nonceId.toString()))
            HttpServletResponse response = Mock(HttpServletResponse)
        when:
            String result = controller.callback("code", request, response)
        then:
            1 * googleTokenService.findNonceById(_) >> Optional.of(nonce)
            1 * googleTokenService.getAccessToken(*_) >> new GoogleToken()
            1 * googleTokenService.deleteAllUserNonces(_)
            1 * response.addCookie(_)
            result == "redirect:/"
    }

}
