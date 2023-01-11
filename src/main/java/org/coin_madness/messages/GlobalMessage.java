package org.coin_madness.messages;

public class GlobalMessage {
    // (CLIENT_TO_SERVER_KEEP_ALIVE, Integer clientId)
    // ping the server every second to keep the connection alive.
    // If we have not heard from a client in 5 seconds assume that they have disconnected.
    public static final String CLIENT_TO_SERVER_KEEP_ALIVE = "client_to_server_keep_alive";

    // (KEEP_ALIVE, Integer clientId)
    // ping the clients every second so the client knows there .
    // If we have not heard from the server in 5 seconds assume that the server has died
    public static final String SERVER_TO_CLIENT_KEEP_ALIVE = "server_to_client_keep_alive";

    // (CLIENTS, Integer clientId)
    // clients is used to keep a record of who is connected to the server
    public static final String CLIENTS = "clients";

    // (DISCONNECT, Integer clientId)
    // A client voluntarily disconnect itself
    public static final String DISCONNECT = "disconnect";
}
