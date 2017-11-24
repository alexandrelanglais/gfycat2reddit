package io.trailermaker.gfycat2reddit.common

final case class GfyOAuthRequest(client_id: String, client_secret: String, grant_type: String, username: String, password: String)
final case class GfyOAuthResponse(
    token_type:               String,
    refresh_token_expires_in: Long,
    refresh_token:            String,
    resource_owner:           String,
    expires_in:               Long,
    access_token:             String
)

final case class GfyCat(
    gfyId:        String,
    gfyNumber:    String,
    webmUrl:      String,
    gifUrl:       Option[String],
    posterUrl:    String,
    mjpgUrl:      String,
    frameRate:    Int,
    numFrames:    Int,
    mp4Size:      Int,
    webmSize:     Int,
    gifSize:      Option[Int],
    nsfw:         String,
    mp4Url:       String,
    tags:         Option[List[String]],
    gfyName:      String,
    title:        String,
    description:  String,
    sentToReddit: Option[Boolean] = Some(false)
)

final case class GfyCats(gfycats: List[GfyCat], cursor: String)

final case class GfyCatUploadRequest(
    isOk:       Boolean,
    gfyname:    String,
    secret:     String,
    uploadType: String
)
