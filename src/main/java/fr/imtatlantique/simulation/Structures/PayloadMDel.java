package fr.imtatlantique.simulation.Structures;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.imtatlantique.simulation.Service.ServerService;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayloadMDel {
    private ServerService server;
    private MDel message;

    public PayloadMDel() {
        this.server = new ServerService();
        this.message = new MDel();
    }

    public PayloadMDel(ServerService server, MDel message) {
        this.server = server;
        this.message = message;
    }

    @Override
    public String toString() {
        return "PayloadMDel{" +
                "server=" + server.toString() +
                ", message=" + message.toString() +
                '}';
    }
}
