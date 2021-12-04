package fr.imtatlantique.simulation.Structures;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.imtatlantique.simulation.Interface.IMessage;
import fr.imtatlantique.simulation.Service.ServerService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayloadMAdd {
    private ServerService server;
    private MAdd message;

    public PayloadMAdd() {
        this.server = new ServerService();
        this.message = new MAdd();
    }

    public PayloadMAdd(ServerService server, MAdd message) {
        this.server = server;
        this.message = message;
    }

    @Override
    public String toString() {
        return "PayloadMAdd{" +
                "server=" + server.toString() +
                ", message=" + message.toString() +
                '}';
    }
}
