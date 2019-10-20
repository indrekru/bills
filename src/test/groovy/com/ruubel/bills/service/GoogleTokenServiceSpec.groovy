import com.ruubel.bills.model.GoogleToken
import com.ruubel.bills.model.User
import com.ruubel.bills.repository.GoogleNonceRepository
import com.ruubel.bills.repository.GoogleTokenRepository
import com.ruubel.bills.service.GoogleTokenService
import com.ruubel.bills.service.HttpService
import org.apache.http.HttpEntity
import org.apache.http.entity.BasicHttpEntity
import spock.lang.Specification

import java.time.Instant

class GoogleTokenServiceSpec extends Specification {

    GoogleTokenService service
    GoogleTokenRepository repository
    GoogleNonceRepository nonceRepository
    HttpService httpService

    def setup () {
        repository = Mock(GoogleTokenRepository)
        nonceRepository = Mock(GoogleNonceRepository)
        httpService = Mock(HttpService)
        service = new GoogleTokenService(repository, nonceRepository, httpService)
    }

    def "when invalid code, then returns null" () {
        given:
            User user = new User()
        when:
            GoogleToken token = service.getAccessToken(user, "code")
        then:
            1 * httpService.post(_, _) >> Optional.empty()
            0 * repository.findTopByOrderByUpdatedAtDesc()
            0 * repository.save(_)
            token == null
    }

    def "when no values in json, then returns null" () {
        given:
            User user = new User()
            HttpEntity entity = new BasicHttpEntity()
            entity.content = new ByteArrayInputStream("{}".getBytes())
        when:
            GoogleToken token = service.getAccessToken(user, "code")
        then:
            1 * httpService.post(_, _) >> Optional.of(entity)
            0 * repository.findTopByOrderByUpdatedAtDesc()
            0 * repository.save(_)
            token == null
    }

    def "when access_token in json, then runs all services" () {
        given:
            User user = new User()
            HttpEntity entity = new BasicHttpEntity()
            entity.content = new ByteArrayInputStream("{\"access_token\" : \"token\"}".getBytes())
        when:
            service.getAccessToken(user, "code")
        then:
            1 * httpService.post(_, _) >> Optional.of(entity)
            1 * repository.findTopByOrderByUpdatedAtDesc() >> null
            1 * repository.save(_)
    }

    def "when is not present, then dont call services and return null" () {
        given:
            GoogleToken token = new GoogleToken()
        when:
            GoogleToken updatedToken = service.refreshToken(token)
        then:
            1 * httpService.post(_, _) >> Optional.empty()
            0 * repository.save(_)
            updatedToken == null
    }

    def "when no access_token, then dont call services and return null" () {
        given:
            HttpEntity entity = new BasicHttpEntity()
            entity.content = new ByteArrayInputStream("{}".getBytes())
            GoogleToken token = new GoogleToken()
        when:
            GoogleToken updatedToken = service.refreshToken(token)
        then:
            1 * httpService.post(_, _) >> Optional.of(entity)
            0 * repository.save(_)
            updatedToken == null
    }

    def "when access_token, then call services" () {
        given:
            HttpEntity entity = new BasicHttpEntity()
            entity.content = new ByteArrayInputStream("{\"access_token\" : \"token\"}".getBytes())
            GoogleToken token = new GoogleToken()
        when:
            service.refreshToken(token)
        then:
            1 * httpService.post(_, _) >> Optional.of(entity)
            1 * repository.save(_)
    }

    def "when no token in DB, then return null" () {
        given:
            User user = new User()
        when:
            GoogleToken token = service.getValidToken(user)
        then:
            1 * repository.findByUser(_) >> null
            0 * httpService.post(*_)
            token == null
    }

    def "when token in DB but expired, then calls refresh" () {
        given:
            User user = new User()
            GoogleToken tokenInDb = new GoogleToken(expiresAt: Instant.now().minusSeconds(2))
        when:
            service.getValidToken(user)
        then:
            1 * repository.findByUser(user) >> tokenInDb
            1 * httpService.post(*_)
    }

    def "when token in DB and not expired, then not call refresh" () {
        given:
            User user = new User()
            GoogleToken tokenInDb = new GoogleToken(expiresAt: Instant.now().plusSeconds(2))
        when:
            GoogleToken token = service.getValidToken(user)
        then:
            1 * repository.findByUser(user) >> tokenInDb
            0 * httpService.post(*_)
            token != null
    }

}