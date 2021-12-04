package fr.imtatlantique.simulation.Structures;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Random;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Weights {

    // weights are integers, for they might be useful for latency
    public int[][] weights;
    @JsonIgnore
    private Random weightGenerator;

    public Weights() {
        this.weights = new int[1][1];
        this.weightGenerator = new Random();
    }

    public Weights(int numberOfPeers) {
        this.weights = new int[numberOfPeers][numberOfPeers];
        this.weightGenerator = new Random();
    }

    public void generateRandomWeights(int from, int to) {
        for (int i = 0; i < weights.length; ++i) {
            for (int j = 0; j < weights[i].length; ++j) {
                if (i != j) {
                    weights[i][j] = weightGenerator.nextInt(to - from) + from; // next int for now
                    weights[j][i] = weights[i][j];
                } else {
                    weights[i][j] = 0;
                }
            }
        }
    }

    public int get(long from, long to) {
        return this.weights[(int) from][(int) to];
    }

    @Override
    public String toString() {
        return "Weights{" +
                "weights=" + Arrays.toString(weights) +
                '}';
    }
}
