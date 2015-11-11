package fr.neatmonster.labs;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fr.neatmonster.neato.Ensemble;
import fr.neatmonster.neato.Gene;
import fr.neatmonster.neato.Individual;

public class EnsembleSerializer implements JsonSerializer<Ensemble> {
    @Override
    public JsonElement serialize(final Ensemble src, final Type typeOfSrc,
            final JsonSerializationContext context) {
        final JsonArray elementsArr = new JsonArray();
        for (final Individual element : src.elements) {
            final JsonObject elementObj = new JsonObject();
            final JsonArray genotypeArr = new JsonArray();
            for (final Gene gene : element.genotype) {
                final JsonObject geneObj = new JsonObject();
                geneObj.add("input", new JsonPrimitive(gene.input));
                geneObj.add("output", new JsonPrimitive(gene.output));
                geneObj.add("weight", new JsonPrimitive(gene.weight));
                geneObj.add("enabled", new JsonPrimitive(gene.enabled));
                genotypeArr.add(geneObj);
            }
            elementObj.add("genotype", genotypeArr);
            elementsArr.add(elementObj);
        }
        return elementsArr;
    }
}
