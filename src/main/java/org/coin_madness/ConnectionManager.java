package org.coin_madness;

import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.io.IOException;

public class ConnectionManager {
    Space lobby ;
    public void host(){
        SpaceRepository repository = new SpaceRepository();

        lobby = new SequentialSpace();

        repository.add("lobby", lobby);

        repository.addGate("tcp://0.0.0.0:9001/?keep");
        System.out.println("Server Created ! ");
    }
    public void join(String ip) throws IOException {
        lobby = new RemoteSpace("tcp://" + ip + ":9001/lobby?keep");
    }

}
