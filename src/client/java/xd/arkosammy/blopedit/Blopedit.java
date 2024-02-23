package xd.arkosammy.blopedit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.blopedit.files.FilePropertiesEditContext;
import xd.arkosammy.blopedit.files.PropertiesFile;
import xd.arkosammy.blopedit.util.Config;
import xd.arkosammy.blopedit.util.MatchingCondition;

import java.util.Optional;

// TODO: Remember to handle the source entry is already in the block.properties file
public class Blopedit implements ClientModInitializer {

	public static final String MOD_ID = "blopedit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {

		Config.getInstance().readConfig();
		ClientLifecycleEvents.CLIENT_STOPPING.register((client -> Config.getInstance().writeConfig()));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

			LiteralCommandNode<FabricClientCommandSource> parentNode = ClientCommandManager
					.literal("blopedit")
					.build();
			LiteralCommandNode<FabricClientCommandSource> copyPropertiesFileToFolderNode = ClientCommandManager
					.literal("copyPropertiesFileToFolder")
					.executes((ctx) -> {
						PropertiesFile.getInstance().ifPresent(PropertiesFile::copyPropertiesFileToFolder);
						return Command.SINGLE_SUCCESS;
					})
					.build();
			LiteralCommandNode<FabricClientCommandSource> settingsNode = ClientCommandManager
					.literal("settings")
					.build();
			LiteralCommandNode<FabricClientCommandSource> doAutoReloadShadersNode = ClientCommandManager
					.literal("doAutoReloadShaders")
					.executes((ctx) -> {
						addMessageToHud(Text.literal("doAutoReloadShaders currently set to: " + Config.getInstance().doAutoReloadShaders()).formatted(Formatting.YELLOW));
						return Command.SINGLE_SUCCESS;
					})
					.build();
			ArgumentCommandNode<FabricClientCommandSource, Boolean> doAutoReloadShadersArgumentNode = ClientCommandManager
					.argument("value", BoolArgumentType.bool())
					.executes((ctx) -> {
						boolean doAutoReloadShaders = BoolArgumentType.getBool(ctx, "value");
						Config.getInstance().setDoAutoReloadShaders(doAutoReloadShaders);
						addMessageToHud(Text.literal("doAutoReloadShaders has been set to: " + Config.getInstance().doAutoReloadShaders()).formatted(Formatting.YELLOW));
						return Command.SINGLE_SUCCESS;
					})
					.build();
			LiteralCommandNode<FabricClientCommandSource> addSourceToPropertiesFile = ClientCommandManager
					.literal("addToPropertiesFile")
					.build();
			ArgumentCommandNode<FabricClientCommandSource, BlockStateArgument> addSourceToPropertiesFileArgumentNode = ClientCommandManager
					.argument("destination", BlockStateArgumentType.blockState(registryAccess))
					.executes((ctx) -> {
						Optional<FilePropertiesEditContext> optionalFilePropertiesEditContext = FilePropertiesEditContext.create(ctx);
						optionalFilePropertiesEditContext.ifPresent(fileEditContext -> PropertiesFile.getInstance().ifPresent(propertiesFile -> propertiesFile.processEditContext(fileEditContext)));
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
							optionalFilePropertiesEditContext.ifPresent(fileEditContext -> PropertiesFile.getInstance().ifPresent(propertiesFile -> propertiesFile.processEditContext(fileEditContext)));
						} else {
							addMessageToHud(Text.literal("Invalid matching condition argument!").formatted(Formatting.RED));
						}
						return Command.SINGLE_SUCCESS;
					})
					.build();

			ArgumentCommandNode<FabricClientCommandSource, Boolean> moveSourceIfFoundArgumentNode = ClientCommandManager
					.argument("moveSourceIfFound", BoolArgumentType.bool())
					.executes((ctx) -> {
						boolean moveSourceIfFound = BoolArgumentType.getBool(ctx, "moveSourceIfFound");
						Optional<MatchingCondition> optionalMatchingCondition = MatchingCondition.fromString(StringArgumentType.getString(ctx, "matching_condition"));
						if(optionalMatchingCondition.isPresent()){
							MatchingCondition matchingCondition = optionalMatchingCondition.get();
							Optional<FilePropertiesEditContext> optionalFilePropertiesEditContext = FilePropertiesEditContext.create(matchingCondition, moveSourceIfFound, ctx);
							optionalFilePropertiesEditContext.ifPresent(fileEditContext -> PropertiesFile.getInstance().ifPresent(propertiesFile -> propertiesFile.processEditContext(fileEditContext)));
						} else {
							addMessageToHud(Text.literal("Invalid matching condition argument!").formatted(Formatting.RED));
						}
						return Command.SINGLE_SUCCESS;
					})
					.build();

			dispatcher.getRoot().addChild(parentNode);
			parentNode.addChild(addSourceToPropertiesFile);
			parentNode.addChild(copyPropertiesFileToFolderNode);
			parentNode.addChild(settingsNode);
			settingsNode.addChild(doAutoReloadShadersNode);
			doAutoReloadShadersNode.addChild(doAutoReloadShadersArgumentNode);
			addSourceToPropertiesFile.addChild(addSourceToPropertiesFileArgumentNode);
			addSourceToPropertiesFileArgumentNode.addChild(matchingArgumentNode);
			matchingArgumentNode.addChild(moveSourceIfFoundArgumentNode);
		});

		LOGGER.info("Thanks to @spaceagle17 (https://github.com/SpacEagle17) for suggesting the idea for this mod, and thanks to the rest of the Complementary shaders community for the awesome support it has given me!");

	}

	public static void addMessageToHud(Text text){
		MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
	}

}