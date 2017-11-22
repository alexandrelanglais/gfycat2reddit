package io.trailermaker.gfycat2reddit.reddit

import java.net.URL

import net.dean.jraw.RedditClient
import net.dean.jraw.fluent.FluentRedditClient
import net.dean.jraw.http.UserAgent
import net.dean.jraw.http.oauth.Credentials

object RedditLib {

  def submitLink(redditClient: RedditClient, subreddit: String, link: String, title: String) = {
    val fluent = new FluentRedditClient(redditClient)
    fluent
      .subreddit(subreddit)
      .submit(new URL(link), title)
  }

  def initOAuth(username: String, passwd: String, clientId: String, clientSecret: String): RedditClient = {

    val myUserAgent  = UserAgent.of("desktop", "io.trailermaker", "v0.1", username)
    val redditClient = new RedditClient(myUserAgent)
    val credentials  = Credentials.script(username, passwd, clientId, clientSecret)
    val authData     = redditClient.getOAuthHelper.easyAuth(credentials)
    redditClient.authenticate(authData)
    redditClient
  }
}
