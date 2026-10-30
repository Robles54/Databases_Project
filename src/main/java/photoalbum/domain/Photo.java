package photoalbum.domain;
/**
* One photo in a user's album: a row of the ALBUM table.
*
* id is null for a photo that hasn't been inserted yet. caption may be null
* (nullable columns). The owner (ALBUM.username) isn't stored here: the DAO
* only returns photos belonging to the current user, so every Photo already has
* a known owner.
*/
public record Photo(Integer id, String url, String caption) {
}