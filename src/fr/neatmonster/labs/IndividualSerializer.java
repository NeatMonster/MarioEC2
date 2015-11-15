package fr.neatmonster.labs;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fr.neatmonster.neato.Gene;
import fr.neatmonster.neato.Individual;
import fr.neatmonster.neato.Neuron;

public class IndividualSerializer implements JsonSerializer<Individual> {

    @Override
    public JsonElement serialize(final Individual creatureSrc,
            final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject creatureObj = new JsonObject();

        final JsonArray genotypeArr = new JsonArray();
        for (final Gene gene : creatureSrc.genotype) {
            final JsonObject geneObj = new JsonObject();
            geneObj.add("input", new JsonPrimitive(gene.input));
            geneObj.add("output", new JsonPrimitive(gene.output));
            geneObj.add("weight", new JsonPrimitive(gene.weight));
            geneObj.add("enabled", new JsonPrimitive(gene.enabled));
            genotypeArr.add(geneObj);
        }
        creatureObj.add("genotype", genotypeArr);

        final JsonArray inputsArr = new JsonArray();
        for (final Neuron neuron : creatureSrc.inputs)
            inputsArr.add(neuron.value);
        creatureObj.add("inputs", inputsArr);

        final JsonArray hiddenArr = new JsonArray();
        for (final Neuron neuron : creatureSrc.hidden)
            hiddenArr.add(neuron.value);
        creatureObj.add("hidden", hiddenArr);

        final JsonArray outputsArr = new JsonArray();
        for (final Neuron neuron : creatureSrc.outputs)
            outputsArr.add(neuron.value);
        creatureObj.add("outputs", outputsArr);

        return creatureObj;
    }
}
