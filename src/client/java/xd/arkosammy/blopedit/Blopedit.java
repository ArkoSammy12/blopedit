package xd.arkosammy.blopedit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.blopedit.files.FilePropertiesEditContext;
import xd.arkosammy.blopedit.files.PropertiesFile;

import java.io.IOException;
import java.util.Optional;

// TODO: Remember to get rid of log lines
// TODO: Remember to handle cases where multiple destination entries are found, and when the source entry is already in the block.properties file
// TODO: Make automatic shader reloading toggleable
public class Blopedit implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("blopedit");
	@Override
	public void onInitializeClient() {

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

			LiteralCommandNode<FabricClientCommandSource> parentNode = ClientCommandManager
					.literal("blopedit")
					.build();

			LiteralCommandNode<FabricClientCommandSource> getSourceBlockNode = ClientCommandManager
					.literal("get")
					.executes((ctx) -> {
						HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
						if(hitResult instanceof BlockHitResult blockHitResult){
							Block block = ctx.getSource().getWorld().getBlockState(blockHitResult.getBlockPos()).getBlock();
							Optional<RegistryKey<Block>> optionalBlockRegistryKey = Registries.BLOCK.getEntry(block).getKey();
							if(optionalBlockRegistryKey.isPresent()){

								Identifier blockIdentifier = optionalBlockRegistryKey.get().getValue();
								MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Block identifier: " + blockIdentifier.toString()));

							}
						}
						return Command.SINGLE_SUCCESS;
					})
					.build();

			/*

			LiteralCommandNode<FabricClientCommandSource> getCurrentShaderNode = ClientCommandManager
					.literal("getCurrentShader")
					.executes((ctx) -> {
						String currentShader = getCurrentShader();
						MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Current shader name: " + currentShader));
						return Command.SINGLE_SUCCESS;
					})
					.build();


			LiteralCommandNode<FabricClientCommandSource> printBlockPropertiesFileToConsole = ClientCommandManager
					.literal("printBlockPropertiesFileToConsole")
					.executes((ctx) -> {
						printBlockPropertiesFileToConsole();
						return Command.SINGLE_SUCCESS;
					})
					.build();
			*/

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

			dispatcher.getRoot().addChild(parentNode);
			parentNode.addChild(getSourceBlockNode);
			//parentNode.addChild(getCurrentShaderNode);
			parentNode.addChild(addSourceToPropertiesFile);
			//parentNode.addChild(printBlockPropertiesFileToConsole);
			addSourceToPropertiesFile.addChild(addSourceToPropertiesFileArgumentNode);

		});

	}

	public static void addMessageToHud(Text text){
		MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
	}

}