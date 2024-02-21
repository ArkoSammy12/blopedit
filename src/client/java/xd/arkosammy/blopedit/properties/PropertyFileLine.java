package xd.arkosammy.blopedit.properties;


import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PropertyFileLine extends FileLine {

    private final String key;
    private final List<PropertyEntry> propertyEntries = new ArrayList<>();

    PropertyFileLine(String line){
        super(line);
        int indexOfFirstEquals = line.indexOf('=');
        if(indexOfFirstEquals < 0) {
            throw new IllegalArgumentException("Property line does not contain equals sign '='");
        } else {
            this.key = line.substring(0, indexOfFirstEquals);
            String valueString = line.substring(indexOfFirstEquals + 1);
            if(!valueString.isBlank()){
                String[] values = valueString.split("\\s+");
                for(String value : values){
                    propertyEntries.add(new PropertyEntry(value));
                }
            }
        }
    }

    public void appendValue(PropertyEntry propertyEntry){
        this.propertyEntries.add(propertyEntry);
    }

    public boolean containsPropertyValue(PropertyEntry propertyEntry){
        return this.propertyEntries.contains(propertyEntry);
    }

    public boolean containsIdentifier(Identifier identifier){
        return this.propertyEntries.stream().anyMatch(propertyEntry -> propertyEntry.matchesIdentifier(identifier));
    }

    public Optional<PropertyEntry> getFirstMatchingValueForIdentifier(PropertyEntry propertyEntry){
        for(PropertyEntry value : this.propertyEntries){
            if(value.matchesIdentifier(propertyEntry)){
                return Optional.of(value);
            }
        }
        return Optional.empty();

    }

    @Override
    public String getString() {
        return this.key + "=" + String.join(" ", propertyEntries.stream().map(PropertyEntry::toString).toList());
    }
}
