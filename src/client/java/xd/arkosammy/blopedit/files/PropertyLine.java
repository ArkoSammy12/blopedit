package xd.arkosammy.blopedit.files;

import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PropertyLine implements FileLine {

    private final String originalLine;
    private final String key;
    private final List<String> values;

    PropertyLine(String line){
        this.originalLine = line;
        int indexOfFirstEquals = line.indexOf('=');
        this.key = line.substring(0, indexOfFirstEquals);
        this.values = Arrays.stream(line.substring(indexOfFirstEquals + 1).split(" ")).collect(Collectors.toList());
        //Blopedit.LOGGER.info(line);
        //Blopedit.LOGGER.info("Key: " + this.key);
        //Blopedit.LOGGER.info("Values " + this.values);
    }

    public void appendIdentifier(Identifier identifier){
        this.values.add(identifier.toString());
    }

    public void removeIdentifier(Identifier identifier){
        this.values.removeIf(value -> this.parseBlockIdentifier(value).equals(identifier));
    }

    public boolean containsIdentifier(Identifier identifier){
        return this.values.stream().anyMatch(value -> this.parseBlockIdentifier(value).equals(identifier));
    }

    public Optional<String> getFirstMatchingValue(String value){
        for(String val : this.values){
            if(val.contains(value)){
                return Optional.of(val);
            }

        }
        return Optional.empty();
    }

    @Override
    public String getString(){
        if (this.values.isEmpty()) {
            return this.key + "=";
        }
        return this.key + "=" + String.join(" ", this.values);
    }

    public List<Identifier> getValuesAsIdentifiers(){
        return this.values.stream().map(this::parseBlockIdentifier).collect(Collectors.toList());
    }

    private Identifier parseBlockIdentifier(String value){

        int indexOfFirstColon = value.indexOf(':');

        // If no colons are found, then we are looking at a raw block identifier value. Assume minecraft namespace
        if(indexOfFirstColon < 0){
            return new Identifier("minecraft", value);
        } else {

            // Split the string by the colons. If the second string contains an equal sign, then we reached the block state properties part, which means the first part of the value must be a raw block identifier value. We assume minecraft namespace again
            String[] valueParts = value.split(":");
            if(valueParts[1].contains("=")){
                return new Identifier("minecraft", valueParts[0]);
            } else { // If the second part doesn't contain an equal sign, then we are looking at the raw block identifier value, and the first part must be the namespace
                return new Identifier(valueParts[0], valueParts[1]);
            }

        }

    }

}
