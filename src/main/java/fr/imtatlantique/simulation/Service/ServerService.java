package fr.imtatlantique.simulation.Service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.imtatlantique.simulation.Configuration.Routes;
import fr.imtatlantique.simulation.Structures.*;
import fr.imtatlantique.simulation.Utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerService {
    @Value("${serverId}") // see application.properties
    private Integer serverID;
    private List<ServerService> neighbors;
    private int counter;
    private Versions versions;
    private MAdd best;
    private MonitorMessages monitor;
    private String serviceURL;
    private Weights weights;


    public ServerService(int serverID, List<ServerService> neighbors, int counter, MAdd best, MonitorMessages monitor, String serviceURL, Weights weights) {
        this.serverID = serverID;
        this.neighbors = neighbors;
        this.counter = counter;
        this.best = best;
        this.monitor = monitor;
        this.serviceURL = serviceURL;
        this.weights = weights;
    }

    public ServerService() {
        this.neighbors = new ArrayList<>();
        this.counter = 0;
        this.versions = new Versions();
        this.monitor = new MonitorMessages();
        this.best = MAdd.NOTHING();
        this.serviceURL = "http://localhost:8080";
        this.weights = new Weights();
    }

    public ServerService(int pid) {
        this.serverID = pid;
        this.neighbors = new ArrayList<>();
        this.counter = 0;
        this.versions = new Versions();
        this.monitor = new MonitorMessages();
        this.best = MAdd.NOTHING();
        this.serviceURL = "http://localhost:8080";
        this.weights = new Weights();
    }

    public void add() throws Exception {
        this.counter += 1;
        this.receiveAdd(this, new MAdd(this.getServerID(), this.counter, 0));
    }

    public void del() throws Exception {
        this.counter += 1;
        this.receiveDel(this, new MDel(this.getServerID(), this.counter));
    }

    public void receiveAdd(ServerService q, MAdd m) throws Exception {
        if ((this.versions.isStale(m.getIdentifier()) || (!m.isLooping(this) && m.compareTo(this.best) < 0))
                && q.equals(this.best.last())) {
            // #A might be unconsistency, resolve this
            MDel c = this.best.cancel();
            this.receiveDel(this, c);
        } else if (!m.isLooping(this) && m.isBetterThan(this.best)) {
            // #B better partition detected
            this.versions.update(m.getIdentifier());
            this.best = m;
            for (ServerService s : this.neighbors) {
                MAdd f = m.fwd(this, this.weights.get(this.serverID, s.getServerID()));
                // create payload
                PayloadMAdd payload = new PayloadMAdd(this, f);
                // convert to JSON
                ObjectMapper objectMapper = new ObjectMapper();
                String inputJson = JSONUtils.covertFromObjectToJson(payload);
                //create a new http service
                HttpService httpService = new HttpService(s.getServiceURL());
                // preparation of request
                HttpRequest request = httpService.preparePostRequest(Routes.AS_CAST_RECEIVE_ADD, inputJson);
                // send request
                try {
                    CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
                    System.out.println(response.get().body());
                } catch (Exception e) {
                    throw new Exception("Erreur : " + e);
                }

                //s.receiveAdd(this, f); the original intent
            }
        }
    }

    public void receiveDel(ServerService q, MDel m) throws Exception {
        if (m.shouldDel(this.best) && !m.isLooping(this)) {
            // #A propagate the delete to remove stale information
            this.versions.update(m.getIdentifier());
            this.best = MAdd.NOTHING();
            // forward to everyone, for ones must delete while others must echo
            for (ServerService s : this.neighbors) {
                if (!q.equals(s) || m.getCancel()) { // important that cancel goes to parent as well
                    MDel f = m.fwd(this);

                    // create payload
                    PayloadMDel payload = new PayloadMDel(this, f);
                    // convert to JSON
                    ObjectMapper objectMapper = new ObjectMapper();
                    String inputJson = JSONUtils.covertFromObjectToJson(payload);
                    //create a new http service
                    HttpService httpService = new HttpService(s.getServiceURL());
                    // preparation of request
                    HttpRequest request = httpService.preparePostRequest(Routes.AS_CAST_RECEIVE_DEL, inputJson);
                    // send request
                    try {
                        CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
                        System.out.println(response.get().body());
                    } catch (Exception e) {
                        throw new Exception("Erreur : " + e);
                    }

                    //s.receiveDel(this, f); the original intent
                }
            }
        } else if (!this.best.isNothing()) {
            // #B compete with other partitions to fill gaps left open by deletes
            MAdd e = this.best.fwd(this, this.weights.get(this.getServerID(), q.getServerID()));
            // create payload
            PayloadMAdd payload = new PayloadMAdd(this, e);
            // convert to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String inputJson = JSONUtils.covertFromObjectToJson(payload);
            //create a new http service
            HttpService httpService = new HttpService(q.getServiceURL());
            // preparation of request
            HttpRequest request = httpService.preparePostRequest(Routes.AS_CAST_RECEIVE_ADD, inputJson);
            // send request
            try {
                CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
                System.out.println(response.get().body());
            } catch (Exception exception) {
                throw new Exception("Erreur : " + exception);
            }
            //q.receiveAdd(this, e); the original intent
        }
    }

    public void onEdgeUp(ServerService newNeighbor) throws Exception {
        assert (!newNeighbor.equals(this));

        // #2 send current partition if we have one
        if (!this.best.isNothing()) {
            // Might create a shortcut. Inform each other of current partition.
            MAdd f = this.best.fwd(this, this.weights.get(this.getServerID(), newNeighbor.getServerID()));

            // create payload
            PayloadMAdd payload = new PayloadMAdd(this, f);
            // convert to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String inputJson = JSONUtils.covertFromObjectToJson(payload);
            //create a new http service
            HttpService httpService = new HttpService(newNeighbor.getServiceURL());
            // preparation of request
            HttpRequest request = httpService.preparePostRequest(Routes.AS_CAST_RECEIVE_ADD, inputJson);
            // send request
            try {
                CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
                System.out.println(response.get().body());
            } catch (Exception exception) {
                throw new Exception("Erreur : " + exception);
            }


            //newNeighbor.receiveAdd(this, f); the original intent
        }
    }

    public void onEdgeDown(ServerService rip) throws Exception {
        System.out.println(String.format("RIP between %s and %s.", this.getServerID(), rip.getServerID()));
        if (!this.best.isNothing()) {
            if (!Objects.isNull(this.best.last()) && // not the source ourselves
                    this.best.last().getServerID() == rip.getServerID()) {
                MDel c = this.best.cancel();
                System.out.println(String.format("CANCELLING %s : %s", this.getServerID(), c.toString()));

                // create payload
                PayloadMDel payload = new PayloadMDel(rip, c);
                // convert to JSON
                ObjectMapper objectMapper = new ObjectMapper();
                String inputJson = JSONUtils.covertFromObjectToJson(payload);
                //create a new http service
                HttpService httpService = new HttpService(this.getServiceURL());
                // preparation of request
                HttpRequest request = httpService.preparePostRequest(Routes.AS_CAST_RECEIVE_DEL, inputJson);
                // send request
                try {
                    CompletableFuture<HttpResponse<String>> response = httpService.sendPostRequest(request);
                    System.out.println(response.get().body());
                } catch (Exception exception) {
                    throw new Exception("Erreur : " + exception);
                }


                this.receiveDel(rip, c);
            }
        }
    }

    public MonitorMessages getMonitor() {
        return this.monitor;
    }

    public long getBestPartition() {
        return this.best.getIdentifier().getId();
    }

    public double getBestDistance() {
        return this.best.getWeight();
    }

    @Override
    public Object clone() {
        return new ServerService();
    }


    @Override
    public String toString() {
        return "ServerService{" +
                "serverID=" + serverID +
                ", neighbors=" + neighbors +
                ", counter=" + counter +
                ", versions=" + versions +
                ", best=" + best +
                ", monitor=" + monitor +
                ", serviceURL='" + serviceURL + '\'' +
                ", weights=" + weights +
                '}';
    }
}
