package xd.arkosammy.blopedit.properties;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import java.util.*;

public class PropertyEntry {

    private final Identifier blockIdentifier;
    private final Map<String, String> properties = new HashMap<>();

    PropertyEntry(BlockState state){
        this.blockIdentifier = state.getRegistryEntry().getKey().orElseThrow().getValue();
        Collection<Property<?>> properties = state.getProperties();
        for(Property<?> property : properties){
            this.properties.put(property.getName().toLowerCase(), state.get(property).toString());
        }
    }

    public PropertyEntry(Identifier identifier){
        this.blockIdentifier = identifier;
    }

    PropertyEntry(String value){

        int indexOfFirstColon = value.indexOf(':');
        // If the value doesn't contain a colon, then we only have the block identifier value
        if(indexOfFirstColon < 0){
            this.blockIdentifier = new Identifier("minecraft", value);
        } else {
            // Split the value by the colons
            String[] entrySections = value.split(":");
            if(entrySections.length == 2){
                // If there are only 2 sections, and the second one contains an equals, then the second property corresponds to a property, and the first section must be the block identifier value
                // Otherwise, the second section is the block identifier value. The first section is the block identifier namespace
                if(entrySections[1].contains("=")){
                    this.blockIdentifier = new Identifier("minecraft", entrySections[0]);
                    String[] propertyKeyValuePair = entrySections[1].split("=");
                    this.properties.put(propertyKeyValuePair[0], propertyKeyValuePair[1]);
                } else {
                    this.blockIdentifier = new Identifier(entrySections[0], entrySections[1]);
                }
            } else {
                // If there are more than 3 sections, and the second section contains an equals, then the first section is the block identifier value, and all subsequent ones correspond to properties.
                // Otherwise, the first section is the namespace, the second section is the block identifier value, and all subsequent ones are properties.
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

    public boolean matchesIdentifier(PropertyEntry other){
        return this.blockIdentifier.equals(other.blockIdentifier);
    }

    public boolean matchesIdentifier(Identifier identifier){
        return this.blockIdentifier.equals(identifier);
    }

    @Override
    public String toString(){
        return this.properties.isEmpty() ? this.blockIdentifier.toString() : this.blockIdentifier.toString() + ":" + String.join(" " ,this.properties.entrySet().stream().map((entry) -> entry.getKey() + "=" + entry.getValue()).toList());
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
