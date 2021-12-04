package fr.imtatlantique.simulation.Structures;

import java.util.TreeMap;

public class MonitorMessages {

    public TreeMap<String, Integer> typeToNbMessages;

    public MonitorMessages() {
        this.typeToNbMessages = new TreeMap<>();
    }

    public void received(Object message) {
        String key = message.getClass().getSimpleName();
        if (!this.typeToNbMessages.containsKey(key)){
            this.typeToNbMessages.put(key, 0);
        }
        this.typeToNbMessages.put(key, this.typeToNbMessages.get(key) + 1);
    }

    public String toString() {
        String result = "";
        for (String key : this.typeToNbMessages.keySet()) {
            result = String.format("%s %s %s", result,
                    key, this.typeToNbMessages.get(key));
        }

        return result;
    }

}
