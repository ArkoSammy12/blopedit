package xd.arkosammy.blopedit.files;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import xd.arkosammy.blopedit.Blopedit;
import xd.arkosammy.blopedit.properties.PropertyEntry;
import xd.arkosammy.blopedit.util.MatchingCondition;

import java.util.Optional;

public class FilePropertiesEditContext {

    private final CommandContext<? extends FabricClientCommandSource> commandSource;
    private final PropertyEntry source;
    private final PropertyEntry destination;
    private final MatchingCondition matchingCondition;
    private final boolean moveSourceIfFound;

    FilePropertiesEditContext(PropertyEntry source, PropertyEntry destination, MatchingCondition matchingCondition, boolean moveSourceIfFound, CommandContext<? extends FabricClientCommandSource> commandSource){
        this.source = source;
        this.destination = destination;
        this.commandSource = commandSource;
        this.matchingCondition = matchingCondition;
        this.moveSourceIfFound = moveSourceIfFound;
    }

    public PropertyEntry getSource(){
        return this.source;
    }

    public PropertyEntry getDestination(){
        return this.destination;
    }

    public MatchingCondition getMatchingCondition() {
        return this.matchingCondition;
    }

    public boolean moveSourceIfFound() {
        return this.moveSourceIfFound;
    }

    public static Optional<FilePropertiesEditContext> create(CommandContext<? extends FabricClientCommandSource> ctx) {
        return create(MatchingCondition.MATCH_IDENTIFIERS, ctx);
    }

    public static Optional<FilePropertiesEditContext> create(MatchingCondition matchingCondition, CommandContext<? extends FabricClientCommandSource> ctx){
        return create(matchingCondition, false, ctx);
    }

    public static Optional<FilePropertiesEditContext> create(MatchingCondition matchingCondition, boolean moveSourceIfFound, CommandContext<? extends FabricClientCommandSource> ctx){
        HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
        if(!(hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK){
            Blopedit.addMessageToHud(Text.literal("You are not currently looking at a block").formatted(Formatting.RED));
            return Optional.empty();
        }
        BlockState sourceState = ctx.getSource().getWorld().getBlockState(blockHitResult.getBlockPos());
        Optional<RegistryKey<Block>> optionalSourceKey = sourceState.getRegistryEntry().getKey();
        if(optionalSourceKey.isEmpty()){
            Blopedit.addMessageToHud(Text.literal("Resource key for source block " + sourceState.getBlock().getName() + " not found!").formatted(Formatting.RED));
            return Optional.empty();
        }
        PropertyEntry sourceEntry = matchingCondition.matchingPropertiesForSource() ? new PropertyEntry(sourceState) : new PropertyEntry(optionalSourceKey.get().getValue());
        BlockState destinationState = ctx.getArgument("destination", BlockStateArgument.class).getBlockState();
        Optional<RegistryKey<Block>> optionalDestinationKey = destinationState.getRegistryEntry().getKey();
        if(optionalDestinationKey.isEmpty()){
            Blopedit.addMessageToHud(Text.literal("Resource key for destination block " + destinationState.getBlock().getName() + " not found!").formatted(Formatting.RED));
            return Optional.empty();
        }
        PropertyEntry destinationEntry = matchingCondition.matchingPropertiesForDestination() ? new PropertyEntry(destinationState) : new PropertyEntry(optionalDestinationKey.get().getValue());
        return Optional.of(new FilePropertiesEditContext(sourceEntry, destinationEntry, matchingCondition, moveSourceIfFound, ctx));
    }

}
