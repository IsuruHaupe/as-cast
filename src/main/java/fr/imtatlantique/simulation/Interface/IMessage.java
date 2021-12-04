package fr.imtatlantique.simulation.Interface;

import fr.imtatlantique.simulation.Service.ServerService;


public interface IMessage {
    public ServerService last();

    public boolean isLooping(ServerService receiver);
}
