package fr.imtatlantique.simulation.Structures;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.imtatlantique.simulation.Interface.IMessage;
import fr.imtatlantique.simulation.Service.ServerService;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MAdd implements IMessage {

    // on n'a plus de lien donc c'est l'ID du message et le compteur du message
    //private int messageID;
    //private int messageCounter;

    private final Identifier identifier;
    private double weight;
    private final ArrayList<ServerService> path;

    public MAdd(Identifier identifier, double weight, ArrayList<ServerService> path) {
        this.identifier = new Identifier(identifier);
        this.weight = weight;
        this.path = path;
    }

    public MAdd(long identifier, int counter, double weight) {
        this.identifier = new Identifier(identifier, counter);
        this.weight = weight;
        this.path = new ArrayList<ServerService>();
    }

    public MAdd(Identifier id, double weight) {
        this.identifier = new Identifier(id);
        this.weight = weight;
        this.path = new ArrayList<ServerService>();
    }

    /*
    DO NOT REMOVE THIS CONSTRUCTOR
    it is used when deserializing incoming JSON object
     */
    public MAdd() {
        this.identifier = new Identifier();
        this.weight = 0;
        this.path = new ArrayList<ServerService>();
    }

    public ServerService last() {
        if (!this.path.isEmpty()) {
            return this.path.get(this.path.size() - 1);
        } else {
            return null;
        }
    }

    public MAdd fwd(ServerService forwarder, double weight) {
        MAdd f = this.clone();
        f.weight += weight;
        f.path.add(forwarder);
        return f;
    }

    public MAdd clone() {
        Identifier idCopy = new Identifier(this.identifier.getId(), this.identifier.getCounter());
        ArrayList<ServerService> pathCopy = new ArrayList<>(this.path);

        Double weightCopy = new Double(this.weight);
        return new MAdd(idCopy, weightCopy, pathCopy);
    }

    /**
     * Cancel the propagation of the current best partition downstream
     **/
    public MDel cancel() {
        MAdd f = this.clone();
        return new MDel(f.identifier, f.path, true); // cloned at fwd time
    }

    public boolean isLooping(ServerService receiver) {
        // TODO count this number of mistakes
        return this.path.contains(receiver);
        // if this.path.contains(receiver)
        // return this.signature.contains(receiver);
    }

    public String toString() {
        String p = "";
        for (ServerService s : this.path) {
            p = String.format("%s %s", p, s.getServerID());
        }
        p = String.format("[%s ]", p);

        return String.format("(ADD %s; %s; %s)", this.identifier.toString(), this.weight, p);
    }

    public int compareTo(MAdd o) {
        if (this.weight > o.weight) {
            return -1;
        } else if (this.weight < o.weight) {
            return 1;
        } else {
            if (this.identifier.getId() < o.identifier.getId()) {
                return -1;
            } else if (this.identifier.getId() > o.identifier.getId()) {
                return 1;
            }
        }
        return 0;
    }

    public boolean isBetterThan(MAdd o) {
        return this.compareTo(o) > 0;
    }

    public boolean isVersion(Identifier id) {
        return this.identifier.equals(id);
    }

    public boolean isNothing() {
        return this.identifier.equals(Identifier.NOTHING());
    }

    public static MAdd NOTHING() {
        return new MAdd(Identifier.NOTHING(), Double.POSITIVE_INFINITY, new ArrayList<ServerService>());
    }

    /*public int fromID() {
        return this.messageID;
    }

    public void setID(int id) {
        this.messageID = id;
    }

    public int getCounter() {
        return this.messageCounter;
    }

    public void setCounter(int counter) {
        this.messageCounter = counter;
    }

     */

}
