package org.coin_madness.helpers;

import org.coin_madness.exceptions.NotConnectedException;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.io.IOException;

public class ConnectionManager {
    private Space lobby = null;
    private String remoteIp = null;
    private SpaceRepository repository = null;

    public boolean isHost() {
        return repository != null;
    }

    public Space getLobby() {
        return lobby;
    }

    public void host() {
        repository = new SpaceRepository();

        lobby = new SequentialSpace();

        repository.add("lobby", lobby);

        repository.addGate("tcp://0.0.0.0:9001/?keep");
        System.out.println("Server Created ! ");
    }

    public void join(String ip) throws IOException {
        remoteIp = ip;
        lobby = new RemoteSpace("tcp://" + ip + ":9001/lobby?keep");
    }

    public Space joinRoom(String roomName) throws IOException, NotConnectedException {
        if(repository != null) {
            Space room = new SequentialSpace();
            repository.add(roomName, room);
            return room;
        } else if(remoteIp != null) {
            return new RemoteSpace("tcp://" + remoteIp + ":9001/lobby?keep");
        } else {
            throw new NotConnectedException("Not connected");
        }
    }

    public void stop() {
        if(isHost()) {
            repository.shutDown();
            repository = null;
        }

        remoteIp = null;
        lobby = null;
    }

}
