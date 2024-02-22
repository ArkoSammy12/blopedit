package xd.arkosammy.blopedit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.blopedit.files.FilePropertiesEditContext;
import xd.arkosammy.blopedit.files.PropertiesFile;
import xd.arkosammy.blopedit.util.MatchingCondition;

import java.io.IOException;
import java.util.Optional;

// TODO: Remember to handle cases where multiple destination entries are found, and when the source entry is already in the block.properties file
// TODO: Make automatic shader reloading toggleable
public class Blopedit implements ClientModInitializer {

	public static final String MOD_ID = "blopedit";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitializeClient() {

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

			LiteralCommandNode<FabricClientCommandSource> parentNode = ClientCommandManager
					.literal("blopedit")
					.build();

			LiteralCommandNode<FabricClientCommandSource> addSourceToPropertiesFile = ClientCommandManager
					.literal("addToPropertiesFile")
					.build();

			ArgumentCommandNode<FabricClientCommandSource, BlockStateArgument> addSourceToPropertiesFileArgumentNode = ClientCommandManager
					.argument("destination", BlockStateArgumentType.blockState(registryAccess))
					.executes((ctx) -> {
						Optional<FilePropertiesEditContext> optionalFilePropertiesEditContext = FilePropertiesEditContext.create(ctx);
						optionalFilePropertiesEditContext.ifPresent(fileEditContext -> PropertiesFile.getInstance().ifPresent(propertiesFile -> {
                            try {
                                propertiesFile.processEditContext(fileEditContext);
                            } catch (IOException e) {
                                LOGGER.error("Error attempting to reload shaders: " + e);
                            }
                        }));
						return Command.SINGLE_SUCCESS;
					})
					.build();

			ArgumentCommandNode<FabricClientCommandSource, String> matchingArgumentNode = ClientCommandManager
					.argument("matching_condition", StringArgumentType.word())
					.suggests(MatchingCondition.SUGGESTION_PROVIDER)
					.executes((ctx) -> {
						Optional<MatchingCondition> optionalMatchingCondition = MatchingCondition.fromString(StringArgumentType.getString(ctx, "matching_condition"));
						if(optionalMatchingCondition.isPresent()){
							MatchingCondition matchingCondition = optionalMatchingCondition.get();
							Optional<FilePropertiesEditContext> optionalFilePropertiesEditContext = FilePropertiesEditContext.create(matchingCondition, ctx);
							optionalFilePropertiesEditContext.ifPresent(fileEditContext -> PropertiesFile.getInstance().ifPresent(propertiesFile -> {
								try {
									propertiesFile.processEditContext(fileEditContext);
								} catch (IOException e) {
									LOGGER.error("Error attempting to reload shaders: " + e);
								}
							}));
						} else {
							addMessageToHud(Text.literal("Invalid matching condition argument!").formatted(Formatting.RED));
						}
						return Command.SINGLE_SUCCESS;
					})
					.build();

			dispatcher.getRoot().addChild(parentNode);
			parentNode.addChild(addSourceToPropertiesFile);
			addSourceToPropertiesFile.addChild(addSourceToPropertiesFileArgumentNode);
			addSourceToPropertiesFileArgumentNode.addChild(matchingArgumentNode);

		});

	}

	public static void addMessageToHud(Text text){
		MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
	}

}