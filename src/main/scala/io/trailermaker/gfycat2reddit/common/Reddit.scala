package io.trailermaker.gfycat2reddit.common

final case class RedditSubmit(
    kind:             Option[String]  = None, // one of link, self, image, video, videogif
    sr:               Option[String]  = None, // subreddit name
    title:            Option[String]  = None, // up to 300 chars
    uh:               Option[String]  = None, // modhash https://www.reddit.com/dev/api/#modhashes
    url:              Option[String]  = None, // validurl,
    resubmit:         Option[Boolean] = Some(false),
    sendreplies:      Option[Boolean] = Some(true),
    nsfw:             Option[Boolean] = Some(true),
    ad:               Option[Boolean] = None,
    api_type:         Option[String]  = None,
    app:              Option[String]  = None,
    extension:        Option[String]  = None,
    flair_id:         Option[String]  = None, // max 36 chars
    flair_text:       Option[String]  = None, // max 64 chars
    richtext_json:    Option[String]  = None,
    spoiler:          Option[Boolean] = None,
    text:             Option[String]  = None, //raw markdown text
    video_poster_url: Option[String]  = None,
)
