package xd.arkosammy.blopedit;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
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
import xd.arkosammy.blopedit.files.BlockPropertiesFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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

			LiteralCommandNode<FabricClientCommandSource> addSourceToPropertiesFile = ClientCommandManager
					.literal("addToPropertiesFile")
					.build();

			ArgumentCommandNode<FabricClientCommandSource, BlockStateArgument> addSourceToPropertiesFileArgumentNode = ClientCommandManager
					.argument("destination", BlockStateArgumentType.blockState(registryAccess))
					.executes((ctx) -> {

						HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
						LOGGER.info("1");
						if(hitResult instanceof BlockHitResult blockHitResult){
							LOGGER.info("2");
							Block block = ctx.getSource().getWorld().getBlockState(blockHitResult.getBlockPos()).getBlock();
							Optional<RegistryKey<Block>> optionalBlockRegistryKey = Registries.BLOCK.getEntry(block).getKey();
							if(optionalBlockRegistryKey.isPresent()){
								LOGGER.info("3");
								Identifier source = optionalBlockRegistryKey.get().getValue();
								BlockStateArgument blockStateArgument = ctx.getArgument("destination", BlockStateArgument.class);
								Block destinationBlock = blockStateArgument.getBlockState().getBlock();
								Optional<RegistryKey<Block>> optionalDestinationKey = Registries.BLOCK.getEntry(destinationBlock).getKey();
								if(optionalDestinationKey.isPresent()){
									LOGGER.info("4");
									Identifier destination = optionalDestinationKey.get().getValue();
									BlockPropertiesFile.getInstance(getCurrentShader()).addIdentifierToDestination(source, destination);
								} else {
									LOGGER.error("Destination block registry key not found");
								}
							} else {
								LOGGER.error("Source block registry key not found ");
							}
						} else {
							LOGGER.error("Hit result not of block hit result");
						}

						return Command.SINGLE_SUCCESS;
					})
					.build();

			dispatcher.getRoot().addChild(parentNode);
			parentNode.addChild(getSourceBlockNode);
			parentNode.addChild(getCurrentShaderNode);
			parentNode.addChild(addSourceToPropertiesFile);
			parentNode.addChild(printBlockPropertiesFileToConsole);
			addSourceToPropertiesFile.addChild(addSourceToPropertiesFileArgumentNode);

		});

	}


	private static String getCurrentShader(){
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve("iris.properties");
		String currentShaderName = "null";
		try(BufferedReader br = Files.newBufferedReader(configPath)) {
			String currentLine = br.readLine();
			while(currentLine != null){

				if(currentLine.contains("shaderPack")){
					String[] shaderPackProperty = currentLine.split("=");
					return shaderPackProperty[1];
				}
				currentLine = br.readLine();
			}

		} catch (IOException e){
			LOGGER.error(e.toString());
		} catch (ArrayIndexOutOfBoundsException e){
			LOGGER.error("No shaderpack selected");
		}
		return currentShaderName;
	}

	private static void printBlockPropertiesFileToConsole(){

		Path shaderPath = FabricLoader.getInstance().getGameDir().resolve("shaderpacks").resolve(getCurrentShader());

		try(FileSystem fs = FileSystems.newFileSystem(shaderPath);
			BufferedReader br = Files.newBufferedReader(fs.getPath("/shaders/block.properties"))) {

			LOGGER.info("BEGINNING OF BLOCK PROPERTIES FILE");

			String currentLine = br.readLine();
			while(currentLine != null){
				LOGGER.info(currentLine);
				currentLine = br.readLine();
			}
			LOGGER.info("END OF BLOCK PROPERTIES FILE");

		} catch (IOException e){

			LOGGER.info("Error printing block properties file: " + e);

		}

	}

}