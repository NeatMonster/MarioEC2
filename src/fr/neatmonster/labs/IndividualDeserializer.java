package fr.neatmonster.labs;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import fr.neatmonster.neato.Gene;
import fr.neatmonster.neato.Individual;

public class IndividualDeserializer implements JsonDeserializer<Individual> {

    @Override
    public Individual deserialize(final JsonElement creatureSrc,
            final Type typeOfSrc, final JsonDeserializationContext context)
                    throws JsonParseException {
        final Individual creature = new Individual();
        final JsonObject creatureObj = (JsonObject) creatureSrc;

        final JsonArray genotypeArr = creatureObj.get("genotype")
                .getAsJsonArray();
        for (final JsonElement geneSrc : genotypeArr) {
            final Gene gene = new Gene();
            final JsonObject geneObj = (JsonObject) geneSrc;
            gene.input = geneObj.get("input").getAsInt();
            gene.output = geneObj.get("output").getAsInt();
            gene.weight = geneObj.get("weight").getAsDouble();
            gene.enabled = geneObj.get("enabled").getAsBoolean();
            gene.innovation = geneObj.get("innov").getAsInt();
            creature.genotype.put(gene.innovation, gene);
        }

        final JsonObject biasesObj = creatureObj.get("biases")
                .getAsJsonObject();
        for (final Entry<String, JsonElement> bias : biasesObj.entrySet()) {
            final int neuron = Integer.parseInt(bias.getKey());
            final double value = bias.getValue().getAsDouble();
            creature.biases.put(neuron, value);
        }

        creature.generate();

        final JsonArray inputsArr = creatureObj.get("inputs").getAsJsonArray();
        for (int i = 0; i < inputsArr.size(); ++i)
            creature.inputs.get(i).value = inputsArr.get(i).getAsDouble();

        final JsonArray hiddenArr = creatureObj.get("hidden").getAsJsonArray();
        for (int i = 0; i < hiddenArr.size(); ++i)
            creature.hidden.get(i).value = hiddenArr.get(i).getAsDouble();

        final JsonArray outputsArr = creatureObj.get("outputs")
                .getAsJsonArray();
        for (int i = 0; i < outputsArr.size(); ++i)
            creature.outputs.get(i).value = outputsArr.get(i).getAsDouble();

        return creature;
    }
}
