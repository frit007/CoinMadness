package org.coin_madness.messages;

public class LobbyMessage {
    // (JOIN)
    // send by a client to join a lobby
    public static final String JOIN = "join";

    // (GAME_STARTED)
    // used to tell all client that the game has started
    public static final String GAME_STARTED = "game_started";

    // (WELCOME, String clientId)
    // welcome is used to tell a client their new client_id
    // (Technically client can steal each other ids. But it doesn't matter who gets what id)
    public static final String WELCOME = "welcome";

    // (LOBBY_UPDATED, String clientId)
    // is used to message every client that the lobby has been updated
    public static final String LOBBY_UPDATED = "lobby_updated";

    // (READY, String clientId)
    // is used to mark all clients that are ready
    public static final String READY = "ready";

    // (READY_LOCK)
    // lock used to protect against people changing their ready state and the game starting at the same time.
    public static final String READY_LOCK = "ready_lock";

}
