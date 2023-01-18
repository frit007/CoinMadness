package org.coin_madness.messages;

public class LobbyMessage {
    // (JOIN)
    // send by a client to join a lobby
    public static final String JOIN = "join";

    // (GAME_STARTED, bool personalGhosts)
    // used to tell all client that the game has started
    public static final String GAME_STARTED = "game_started";

    // (WELCOME, Integer clientId)
    // welcome is used to tell a client their new client_id
    // (Technically clients can steal each other ids. But it doesn't matter who gets which id)
    public static final String WELCOME = "welcome";

    // (LOBBY_UPDATED, Integer clientId)
    // is used to message every client that the lobby has been updated
    public static final String LOBBY_UPDATED = "lobby_updated";

    // (READY, Integer clientId)
    // is used to mark all clients that are ready
    public static final String READY = "ready";
    public static final String NOT_READY = "not_ready";

    // (READY_LOCK)
    // lock used to protect against people changing their ready state and the game starting at the same time.
    public static final String READY_LOCK = "ready_lock";

}
