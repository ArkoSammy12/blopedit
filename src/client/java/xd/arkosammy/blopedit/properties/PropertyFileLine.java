package xd.arkosammy.blopedit.properties;

import java.util.*;

/**
 * Represents a single property file line in the block.properties file.
 * It is composed of a block integer key, formatted as "block.\{numerical_id}",
 * followed by an equal sign, followed by a whitespace separated list of property entries.
 */
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
            String entriesString = line.substring(indexOfFirstEquals + 1);
            if(!entriesString.isBlank()){
                String[] entries = entriesString.split("\\s+");
                for(String value : entries){
                    propertyEntries.add(new PropertyEntry(value));
                }
            }
        }
    }

    public void appendProperty(PropertyEntry propertyEntry){
        this.propertyEntries.add(propertyEntry);
    }

    public boolean containsPropertyValue(PropertyEntry propertyEntry){
        return this.propertyEntries.contains(propertyEntry);
    }

    public boolean containsMatching(PropertyEntry propertyEntry, boolean matchProperties){
        return this.propertyEntries.stream().anyMatch(entry -> entry.matches(propertyEntry, matchProperties));
    }

    public Set<PropertyEntry> removeIfMatching(PropertyEntry propertyEntry, boolean matchProperties){
        Set<PropertyEntry> deletedEntries = new HashSet<>();
        this.propertyEntries.removeIf(entry -> {
            if(entry.matches(propertyEntry, matchProperties)) {
                deletedEntries.add(entry);
                return true;
            } else {
                return false;
            }
        });
        return deletedEntries;
    }

    public Optional<PropertyEntry> getFirstMatchingValue(PropertyEntry propertyEntry, boolean matchProperties){
        for(PropertyEntry entry : this.propertyEntries){
            if(entry.matches(propertyEntry, matchProperties)){
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getString() {
        return this.key + "=" + String.join(" ", propertyEntries.stream().map(PropertyEntry::toString).toList());
    }
}
