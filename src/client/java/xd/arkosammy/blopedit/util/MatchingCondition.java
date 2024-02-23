package xd.arkosammy.blopedit.util;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import xd.arkosammy.blopedit.Blopedit;

import java.util.Arrays;
import java.util.Optional;

public enum MatchingCondition implements StringIdentifiable {
    MATCH_IDENTIFIERS("matchIdentifiers"),
    MATCH_WITH_PROPERTIES_SOURCE("matchWithPropertiesSource"),
    MATCH_WITH_PROPERTIES_DESTINATION("matchWithPropertiesDestination"),
    MATCH_WITH_PROPERTIES("matchWithProperties");

    public static final SuggestionProvider<FabricClientCommandSource> SUGGESTION_PROVIDER = SuggestionProviders.register(new Identifier(Blopedit.MOD_ID, "matching_conditions_suggestions"), ((context, builder) -> CommandSource.suggestMatching(Arrays.stream(MatchingCondition.values()).map(MatchingCondition::asString), builder)));

    private final String identifier;

    MatchingCondition(String identifier){
        this.identifier = identifier;
    }


    public boolean matchingPropertiesForSource() {
        return switch (this) {
            case MATCH_WITH_PROPERTIES, MATCH_WITH_PROPERTIES_SOURCE -> true;
            case MATCH_IDENTIFIERS, MATCH_WITH_PROPERTIES_DESTINATION -> false;
        };
    }

    public boolean matchingPropertiesForDestination() {
        return switch (this) {
            case MATCH_WITH_PROPERTIES, MATCH_WITH_PROPERTIES_DESTINATION -> true;
            case MATCH_IDENTIFIERS, MATCH_WITH_PROPERTIES_SOURCE -> false;
        };
    }


    public static Optional<MatchingCondition> fromString(String string){
        for(MatchingCondition matchingCondition : MatchingCondition.values()){
            if (matchingCondition.asString().equals(string)){
                return Optional.of(matchingCondition);
            }
        }
        return Optional.empty();
    }

    @Override
    public String asString() {
        return this.identifier;
    }
}
