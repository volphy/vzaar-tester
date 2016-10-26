package com.krzysztofwilk.vzaar

import com.vzaar.AccountDetails
import com.vzaar.UserDetails
import com.vzaar.Video
import com.vzaar.VideoDetails
import com.vzaar.VideoListQuery
import com.vzaar.Vzaar
import groovy.util.logging.Slf4j
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by Krzysztof Wilk on 23/09/16.
 */

@Slf4j
@Stepwise
class APISpecification extends Specification {

    @Shared def username
    @Shared def token
    @Shared Vzaar api

    @Shared def authorAccountId
    @Shared def videoId

    def setupSpec() {
        username = System.getProperty("vzaarUsername", System.getenv("vzaarUsername"))
        log.info("username={}", username)
        assert username

        token = System.getProperty("vzaarToken", System.getenv("vzaarToken"))
        log.info("token={}", token)
        assert token

        api = new Vzaar(username, token);
        assert api
    }

    def "should username match"() {
        given:
            api.whoAmI()

        when:
            def user = api.username

        then:
            user == username
    }

    def "should author name and user details match"() {
        given:
            api.whoAmI()

        when:
            UserDetails details = api.getUserDetails(username)
            authorAccountId = details.authorAccount

        then:
            details.authorName == username
    }

    def "should account be a trial account"() {
        given:
            api.whoAmI()
            authorAccountId

        when:
            AccountDetails details = api.getAccountDetails(authorAccountId)

        then:
            details.with {
                accountId == 0
                title == 'Self-Service (Trial)'
            }
    }

    def "should video list contains videos"() {
        given:
            api.whoAmI()

        when:
            List<Video> list = api.getVideoList(new VideoListQuery())

        then:
            list.size() > 0
    }

    def "should touch the sky video be available"() {
        given:
            api.whoAmI()

        when:
            VideoListQuery query = new VideoListQuery()
            query.title = "Touch_The_Sky.mp4"
            Video video = api.getVideoList(query)[0]
            videoId = video.id

        then:
            video
            video.with {
                title == "Touch_The_Sky.mp4"
                user.name == username
            }
    }

    def "should touch the sky video have detailed information"() {
        given:
            api.whoAmI()

        when:
            VideoDetails details = api.getVideoDetails(videoId)

        then:
            details.with {
                title == "Touch_The_Sky.mp4"
                type == "video"
            }
    }
}
