package io.trailermaker.gfycat2reddit.common

final case class GfyCat(
    gfyId:       String,
    gfyNumber:   String,
    webmUrl:     String,
    gifUrl:      Option[String],
    posterUrl:   String,
    mjpgUrl:     String,
    frameRate:   Int,
    numFrames:   Int,
    mp4Size:     Int,
    webmSize:    Int,
    gifSize:     Option[Int],
    nsfw:        String,
    mp4Url:      String,
    tags:        Option[List[String]],
    gfyName:     String,
    title:       String,
    description: String
)

final case class GfyCats(gfycats: List[GfyCat], cursor: String)

