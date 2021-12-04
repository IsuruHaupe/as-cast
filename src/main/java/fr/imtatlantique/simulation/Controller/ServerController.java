package fr.imtatlantique.simulation.Controller;

import fr.imtatlantique.simulation.Configuration.Routes;
import fr.imtatlantique.simulation.Service.ServerService;
import fr.imtatlantique.simulation.Structures.PayloadMAdd;
import fr.imtatlantique.simulation.Structures.PayloadMDel;
import fr.imtatlantique.simulation.Structures.Weights;
import fr.imtatlantique.simulation.Utils.JSONUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Getter
public class ServerController {

    @Autowired
    private ServerService serverService;

    @PostMapping(value = Routes.AS_CAST_ADD_NODE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> addNeighbors(@RequestBody String newNeighbor) throws IOException {
        // JSON conversion to object
        ServerService convertedServer = JSONUtils.covertFromJsonToObject(newNeighbor, ServerService.class);
        // Update the neighbors list
        this.serverService.getNeighbors().add(convertedServer);
        System.out.println("LIST OF NEIGHBORS FOR SERVER : " + this.serverService.getServerID());
        System.out.println(this.serverService.getNeighbors());
        return new ResponseEntity<String>("NEW NEIGHBOR ADDED SUCCESSFULLY FOR SERVER : " + this.serverService.getServerID() + "\n",
                HttpStatus.CREATED);
    }

    @PostMapping(value = Routes.AS_CAST_UPDATE_WEIGHTS,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> addWeights(@RequestBody String weights) throws IOException {
        // JSON conversion to object
        Weights convertedWeights = JSONUtils.covertFromJsonToObject(weights, Weights.class);
        // Update weights array
        this.serverService.setWeights(convertedWeights);
        System.out.println("MATRIX OF WEIGHTS FOR SERVER : " + this.serverService.getServerID());
        System.out.println(this.serverService.getWeights().toString());
        return new ResponseEntity<String>("WEIGHTS UPDATED SUCCESSFULLY FOR SERVER : " + this.serverService.getServerID() + "\n",
                HttpStatus.CREATED);
    }

    @PostMapping(value = Routes.AS_CAST_DEL_NODE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public void removeNeighbors() {

    }

    @PostMapping(value = Routes.AS_CAST_ADD,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> add() throws Exception {
        serverService.add();
        System.out.println("SOURCE FOR SERVER : " + this.serverService.getServerID());
        System.out.println(serverService.getBest());
        return new ResponseEntity<String>("Add OPERATION RECEIVED BY SERVER : " + this.serverService.getServerID() + "\n",
                HttpStatus.CREATED);
    }

    @PostMapping(value = Routes.AS_CAST_DEL,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> del() throws Exception {
        serverService.del();
        System.out.println("SOURCE FOR SERVER : " + this.serverService.getServerID());
        System.out.println(serverService.getBest());
        return new ResponseEntity<String>("Del OPERATION RECEIVED BY SERVER : " + this.serverService.getServerID() + "\n",
                HttpStatus.CREATED);
    }

    @PostMapping(value = Routes.AS_CAST_RECEIVE_ADD,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> receiveAdd(@RequestBody String payload) throws Exception {
        // JSON conversion to object
        PayloadMAdd convertedPayload = JSONUtils.covertFromJsonToObject(payload, PayloadMAdd.class);
        // Decompose payload into server and message ad call receiveAdd
        this.serverService.receiveAdd(convertedPayload.getServer(), convertedPayload.getMessage());
        System.out.println("SOURCE FOR SERVER : " + this.serverService.getServerID());
        System.out.println(serverService.getBest());
        return new ResponseEntity<String>("receiveAdd HANDLED SUCCESSFULLY BY SERVER : " + this.serverService.getServerID() + "\n",
                HttpStatus.CREATED);
    }


    @PostMapping(value = Routes.AS_CAST_RECEIVE_DEL,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> receiveDel(@RequestBody String payload) throws Exception {
        // JSON conversion to object
        PayloadMDel convertedServer = JSONUtils.covertFromJsonToObject(payload, PayloadMDel.class);
        // Decompose payload into server and message ad call receiveDel
        this.serverService.receiveDel(convertedServer.getServer(), convertedServer.getMessage());
        return new ResponseEntity<String>("receiveDel HANDLED SUCCESSFULLY BY SERVER : " + this.serverService.getServerID() + "\n",
                HttpStatus.CREATED);
    }
}
