package org.coin_madness.helpers;

import org.coin_madness.exceptions.NotConnectedException;
import org.coin_madness.messages.GlobalMessage;
import org.coin_madness.model.DisconnectReason;
import org.jspace.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionManager {
    private Space lobby = null;
    private Space positionsSpace = null;
    private String remoteIp = null;
    private SpaceRepository repository = null;
    private String clientId;
    // Used by the server to be notified which client have disconnected
    private Action2<String, DisconnectReason> onClientDisconnect;
    // used by the client to be notified when it lost connection to the server
    private Action1<DisconnectReason> onClientTimeout;

    ScopedThreads connectionThreads = new ScopedThreads(() -> {});

    public void setOnClientDisconnect(Action2<String, DisconnectReason> onClientDisconnect) {
        this.onClientDisconnect = onClientDisconnect;
    }
    public void setOnClientTimeout(Action1<DisconnectReason> onClientTimeout) {
        this.onClientTimeout = onClientTimeout;
    }

    public boolean isHost() {
        return repository != null;
    }

    public String getClientId() {
        return clientId;
    }

    public Space getLobby() {
        return lobby;
    }

    public Space getPositionsSpace() {
        return positionsSpace;
    }

    public void host() {
        repository = new SpaceRepository();

        lobby = new SequentialSpace();

        repository.add("lobby", lobby);

        repository.addGate("tcp://0.0.0.0:9001/?keep");
        System.out.println("Server Created ! ");

        startHostTimeoutThreads();
    }

    /**
     * Create the spaces that are to be used in the game. Called by the host.
     */
    public void createGameSpaces() {
        // Right now there is only one that we need to create
        positionsSpace = new SequentialSpace();
        repository.add("positions", positionsSpace);
    }

    /**
     * Join the spaces that are to be used in the game. Called by the clients.
     */
    public void joinGameSpaces() {
        // This means we are the host
        if (remoteIp == null) return;
        try {
            // Right now we only join one space
            positionsSpace = new RemoteSpaceWithDisconnect(new RemoteSpace("tcp://" + remoteIp + ":9001/positions?keep"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not join a remote game space");
        }
    }

    private void startHostTimeoutThreads() {
        // listen and send keep alive messages to clients
        connectionThreads.startHandledThread(() -> {
            // keep a history about for how long a client has failed to send keep alives
            Map<String, Integer> timeoutHistory = new HashMap<>();
            while(true) {
                // get all connected clients and increment their timeout counter
                List<Object[]> connectedClients = lobby.queryAll(new ActualField(GlobalMessage.CLIENTS), new FormalField(String.class));
                for (Object[] connectedClient: connectedClients) {
                    String connectedClientId = connectedClient[1].toString();
                    if(timeoutHistory.containsKey(connectedClientId)) {
                        timeoutHistory.put(connectedClientId, timeoutHistory.get(connectedClientId) + 1);
                    } else {
                        timeoutHistory.put(connectedClientId, 1);
                    }
                }

                // Remove any client that has sent a keep alive from the timeout list
                List<Object[]> keepAlives = lobby.getAll(new ActualField(GlobalMessage.CLIENT_TO_SERVER_KEEP_ALIVE), new FormalField(String.class));
                for (Object[] keepAlive: keepAlives) {
                    String keepAliveClientId = keepAlive[1].toString();
                    timeoutHistory.remove(keepAliveClientId);
                }

                // Check if any client has timed out. Disconnect any timed out client
                for (var timeout: timeoutHistory.entrySet()) {
                    if(timeout.getValue() > 5) {
                        disconnectClient(timeout.getKey(), DisconnectReason.TIMEOUT);
                    }
                }

                Thread.sleep(1000);
            }
        });

        // Send keep alives to clients, so they know they are still connected
        connectionThreads.startHandledThread(() -> {
            while (true) {
                List<Object[]> connectedClients = lobby.queryAll(new ActualField(GlobalMessage.CLIENTS), new FormalField(String.class));
                for (Object[] connectedClient: connectedClients) {
                    String connectedClientId = connectedClient[1].toString();
                    lobby.put(GlobalMessage.SERVER_TO_CLIENT_KEEP_ALIVE, connectedClientId);
                }
                // inform the client that they are still connected to the server
                Thread.sleep(1000);
            }
        });

        // listen for voluntary disconnects
        connectionThreads.startHandledThread(() -> {
            while (true) {
                String disconnectedClientId = lobby.get(new ActualField(GlobalMessage.DISCONNECT), new FormalField(String.class))[1].toString();
                disconnectClient(disconnectedClientId, DisconnectReason.DISCONNECT);
            }
        });
    }

    private void disconnectClient(String disconnectedClient, DisconnectReason reason) {
        try {
            // remove the client from the list of clients
            lobby.getp(new ActualField(GlobalMessage.CLIENTS), new ActualField(disconnectedClient));
            if(onClientDisconnect != null) {
                onClientDisconnect.handle(disconnectedClient, reason);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void join(String ip) throws IOException {
        remoteIp = ip;
        lobby = new RemoteSpaceWithDisconnect(new RemoteSpace("tcp://" + ip + ":9001/lobby?keep"));
    }

    public void startClientTimeoutThread(String clientId) {
        this.clientId = clientId;
        // inform the server that we have not disconnected
        connectionThreads.startHandledThread(() -> {
            while (true) {
                lobby.put(GlobalMessage.CLIENT_TO_SERVER_KEEP_ALIVE, clientId);
                Thread.sleep(1000);
            }
        });
        var wrapper = new Object(){int missedKeepAlives = 0;};
        // listen for server keep alives
        connectionThreads.startHandledThread(() -> {
            while(true) {
                lobby.get(new ActualField(GlobalMessage.SERVER_TO_CLIENT_KEEP_ALIVE), new ActualField(clientId));
                synchronized (wrapper) {
                    wrapper.missedKeepAlives = 0;
                }
            }
        });
        // check if we haven't received a keep alive in 5 seconds
        connectionThreads.startHandledThread(() -> {
            while (true) {
                synchronized (wrapper) {
                    wrapper.missedKeepAlives++;
                    if(wrapper.missedKeepAlives > 1000) {
                        System.out.println("Client lost connection to the server");
                        if(onClientTimeout != null) {
                            onClientTimeout.handle(DisconnectReason.TIMEOUT);
                        }
                    }
                }
                Thread.sleep(1000);
            }
        });
    }

    public Space joinRoom(String roomName) throws IOException, NotConnectedException {
        if(repository != null) {
            // the host creates a new room
            Space room = new SequentialSpace();
            repository.add(roomName, room);
            return room;
        } else if(remoteIp != null) {
            // the client connects to an existing room.
            return new RemoteSpace("tcp://" + remoteIp + ":9001/lobby?keep");
        } else {
            throw new NotConnectedException("Not connected");
        }
    }

    public void stop() {
        if(isHost()) {
            repository.shutDown();
            repository = null;
        } else {
            if(lobby instanceof RemoteSpaceWithDisconnect) {
                RemoteSpaceWithDisconnect remoteSpace = (RemoteSpaceWithDisconnect) lobby;
                remoteSpace.interruptAllThreads();
            }
        }

        remoteIp = null;
        lobby = null;
        setOnClientDisconnect(null);

        connectionThreads.cleanup();
    }

}
