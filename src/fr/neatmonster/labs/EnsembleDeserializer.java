package fr.neatmonster.labs;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import fr.neatmonster.neato.Ensemble;
import fr.neatmonster.neato.Gene;
import fr.neatmonster.neato.Individual;

public class EnsembleDeserializer implements JsonDeserializer<Ensemble> {

    @Override
    public Ensemble deserialize(final JsonElement src, final Type typeOfSrc,
            final JsonDeserializationContext context)
                    throws JsonParseException {
        final Ensemble ensemble = new Ensemble();
        final JsonArray elementsArr = (JsonArray) src;
        for (final JsonElement elementSrc : elementsArr) {
            final Individual element = new Individual();
            final JsonObject elementObj = (JsonObject) elementSrc;
            final JsonArray genotypeArr = elementObj.get("genotype")
                    .getAsJsonArray();
            for (final JsonElement geneSrc : genotypeArr) {
                final Gene gene = new Gene();
                final JsonObject geneObj = (JsonObject) geneSrc;
                gene.input = geneObj.get("input").getAsInt();
                gene.output = geneObj.get("output").getAsInt();
                gene.weight = geneObj.get("weight").getAsDouble();
                gene.enabled = geneObj.get("enabled").getAsBoolean();
                element.genotype.add(gene);
            }
            element.generate();
            ensemble.elements.add(element);
        }
        return ensemble;
    }
}
