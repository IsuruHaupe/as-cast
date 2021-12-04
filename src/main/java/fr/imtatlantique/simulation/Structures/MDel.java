package fr.imtatlantique.simulation.Structures;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.imtatlantique.simulation.Interface.IMessage;
import fr.imtatlantique.simulation.Service.ServerService;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;

@Setter
@Getter
public class MDel implements IMessage {

    //private int messageID;
    //private int messageCounter;
    private final Identifier identifier;
    private final ArrayList<ServerService> path;
    private boolean cancel;

    /*
    DO NOT REMOVE THIS CONSTRUCTOR
    it is used when deserializing incoming JSON object
     */
    public MDel() {
        this.identifier = new Identifier();
        this.path = new ArrayList<ServerService>();
        this.cancel = false;
    }

    public MDel (long id, int counter) {
        this.identifier = new Identifier(id, counter);
        this.path = new ArrayList<>();
        this.cancel = false;
    }

    public MDel (Identifier id) {
        this.identifier = id;
        this.path = new ArrayList<>();
        this.cancel = false;
    }

    public MDel (Identifier id, ArrayList<ServerService> path) {
        this.identifier = id;
        this.path = path;
        this.cancel = false;
    }

    public MDel (Identifier id, ArrayList<ServerService> path, boolean cancel) {
        this.identifier = id;
        this.path = path;
        this.cancel = cancel;
    }

    public MDel setCancel() {
        this.cancel = true;
        return this;
    }

    public ServerService last() {
        if (this.path.isEmpty()) {
            return null;
        } else {
            return this.path.get(this.path.size() - 1);
        }
    }

    public MDel fwd(ServerService forwarder) {
        MDel f = this.clone();
        f.path.add(forwarder);
        return f;
    }

    public MDel clone() {
        Identifier idCopy = new Identifier(this.identifier.getId(), this.identifier.getCounter());
        ArrayList<ServerService> pathCopy = new ArrayList<>(this.path);

        return new MDel(idCopy, pathCopy, this.cancel);
    }

    public boolean isLooping(ServerService receiver) {
        return this.path.contains(receiver);
    }

    public String toString() {
        String p = "";
        for (ServerService n : this.path){
            p = String.format("%s %s",p , n.getServerID());
        }
        p = String.format("[%s ]", p);

        return String.format("(DEL %s; %s; %s)", this.identifier.toString(), p, this.cancel);
    }

    public boolean equalsTo(MDel o) {
        return this.identifier.equals(o.identifier);
    }

    public MAdd target () {
        if (this.cancel) { // delete generated from dynamic network
            return new MAdd(new Identifier(this.identifier.getId(), this.identifier.getCounter()), 0);
        } else {
            return new MAdd(new Identifier(this.identifier.getId(), this.identifier.getCounter() - 1), 0);
        }
    }

    public boolean shouldDel (MAdd best) {
        // return (this.cancel && best.isVersion(this.id) && this.last().equals(best.last()) ) ||
        // (!this.cancel && best.isVersion(this.target().id));

        return ((this.cancel && this.isCancelling(best)) && this.identifier.equals(best.getIdentifier()) ) ||
                (!this.cancel && best.isVersion(this.target().getIdentifier()));
    }

    public boolean isCancelling(MAdd best) {
        Iterator<ServerService> itThis = this.path.iterator();
        Iterator<ServerService> itBest = best.getPath().iterator();

        boolean sameSignature = this.path.size() == best.getPath().size();

        int i = 0;
        while (sameSignature && i < this.path.size()) {
            sameSignature = this.path.get(i).getServerID() == best.getPath().get(i).getServerID();
            ++i;
        }

        return best.isVersion(this.target().getIdentifier()) &&
                this.cancel &&
                // best.path.equals(this.path);
                sameSignature;
    }

    public boolean getCancel() {
        return this.cancel;
    }
}