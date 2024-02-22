package xd.arkosammy.blopedit.properties;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import java.util.*;

/**
 * Represents a property entry, which is contained in a whitespace separated list of other property entries in a single property file line.
 * Each property entry is composed of a block identifier path, optionally prefixed by an identifier namespace and a colon, and optionally followed by a colon separated list of key entry pairs corresponding to the block's block state.
 * Each block-blockstate combination can only appear once in the block.properties file. We can compare different properties using only the identifiers, or also including the properties.
 * For the case where the property entry lacks an identifier namespace, it is assumed to be of "minecraft"
 */
public class PropertyEntry {

    private final Identifier blockIdentifier;
    private final Map<String, String> properties = new HashMap<>();

    public PropertyEntry(BlockState state){
        this.blockIdentifier = state.getRegistryEntry().getKey().orElseThrow().getValue();
        Collection<Property<?>> properties = state.getProperties();
        for(Property<?> property : properties){
            this.properties.put(property.getName().toLowerCase(), state.get(property).toString());
        }
    }

    public PropertyEntry(Identifier identifier){
        this.blockIdentifier = identifier;
    }

    PropertyEntry(String entry){

        int indexOfFirstColon = entry.indexOf(':');
        // If the entry doesn't contain a colon, then we only have the block identifier path
        // Ex: "farmland"
        if(indexOfFirstColon < 0){
            this.blockIdentifier = new Identifier("minecraft", entry);
        } else {
            // Split the entry by the colons
            String[] entrySections = entry.split(":");
            if(entrySections.length == 2){
                // If there are only 2 sections, and the second one contains an equal sign, then the second section corresponds to a property, and the first section must be the block identifier path
                // Ex: "farmland:moisture=3"
                // Otherwise, the second section is the block identifier path, and the first section is the block identifier namespace
                // Ex: "minecraft:farmland"
                if(entrySections[1].contains("=")){
                    this.blockIdentifier = new Identifier("minecraft", entrySections[0]);
                    String[] propertyKeyValuePair = entrySections[1].split("=");
                    this.properties.put(propertyKeyValuePair[0], propertyKeyValuePair[1]);
                } else {
                    this.blockIdentifier = new Identifier(entrySections[0], entrySections[1]);
                }
            } else {
                // If there are more than 3 sections, and the second section contains an equals, then the first section is the block identifier path, and all subsequent sections correspond to properties.
                // Ex: "farmland:moisture=3:wet=true"
                // Otherwise, the first section is the block identifier namespace, the second section is the block identifier path, and all subsequent sections are properties.
                // Ex: "minecraft:farmland:moisture=3:wet=true"
                if(entrySections[1].contains("=")) {
                    this.blockIdentifier = new Identifier("minecraft", entrySections[0]);
                    for(int i = 1; i < entrySections.length; i++){
                        String propertySection = entrySections[i];
                        String[] propertyKeyValuePair = propertySection.split("=");
                        this.properties.put(propertyKeyValuePair[0], propertyKeyValuePair[1]);
                    }
                } else {
                    this.blockIdentifier = new Identifier(entrySections[0], entrySections[1]);
                    for (int i = 2; i < entrySections.length; i++) {
                        String propertySection = entrySections[i];
                        String[] propertyKeyValuePair = propertySection.split("=");
                        this.properties.put(propertyKeyValuePair[0], propertyKeyValuePair[1]);
                    }
                }
            }

        }

    }

    public Identifier getBlockIdentifier(){
        return this.blockIdentifier;
    }

    public Map<String, String> getBlockStateProperties(){
        return this.properties;
    }

    public boolean matches(PropertyEntry other, boolean matchProperties){
        boolean matchesIdentifier = this.blockIdentifier.equals(other.blockIdentifier);
        if(!matchProperties){
            return matchesIdentifier;
        }
        return matchesIdentifier && other.properties.entrySet().containsAll(this.properties.entrySet());
    }

    @Override
    public String toString(){
        return this.properties.isEmpty() ? this.blockIdentifier.toString() : this.blockIdentifier.toString() + ":" + String.join(":", this.properties.entrySet().stream().map((entry) -> entry.getKey() + "=" + entry.getValue()).toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyEntry that)) return false;
        return Objects.equals(getBlockIdentifier(), that.getBlockIdentifier()) && this.getBlockStateProperties().equals(that.getBlockStateProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBlockIdentifier(), properties);
    }
}
