package com.selfdot.broadcaster;

import com.google.gson.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.profiling.jfr.event.ServerTickTimeEvent;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

public class Broadcaster implements ModInitializer {

    private static boolean DISABLED = false;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILENAME = "config/broadcaster/config.json";

    private int tickCount;
    private String prefix;
    private int interval;
    private Deque<String> messages;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(this::onCommandRegistration);
        reload();
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server) {
        if (!messages.isEmpty() && ++tickCount % (interval * 20) == 0) {
            server.getPlayerManager().broadcast(Text.literal(ChatColourUtils.format(
                prefix + messages.peekFirst()
            )), false);
            messages.addLast(messages.removeFirst());
        }
    }

    private void onCommandRegistration(
        CommandDispatcher<ServerCommandSource> dispatcher,
        CommandRegistryAccess commandRegistryAccess,
        CommandManager.RegistrationEnvironment registrationEnvironment
    ) {
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>
            literal("broadcaster")
            .requires(source -> !DISABLED)
            .requires(source -> source.hasPermissionLevel(4))
            .then(LiteralArgumentBuilder.<ServerCommandSource>
                literal("reload")
                .executes(this::runReload)
            )
        );
    }

    private int runReload(CommandContext<ServerCommandSource> ctx) {
        reload();
        ctx.getSource().sendMessage(Text.literal("Reloaded broadcaster config"));
        return 1;
    }

    public void reload() {
        prefix = "&f[&6Broadcast&f]";
        interval = 30;
        messages = new ArrayDeque<>();
        try {
            JsonElement jsonElement = JsonParser.parseReader(new FileReader(FILENAME));
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has(DataKeys.PREFIX)) prefix = jsonObject.get(DataKeys.PREFIX).getAsString();
                if (jsonObject.has(DataKeys.INTERVAL)) interval = jsonObject.get(DataKeys.INTERVAL).getAsInt();
                if (jsonObject.has(DataKeys.MESSAGES)) {
                    messages.addAll(jsonObject.getAsJsonArray(DataKeys.MESSAGES).asList().stream()
                        .map(JsonElement::getAsString).toList()
                    );
                }
                LOGGER.info("Loaded broadcaster config");

            } catch (Exception e) {
                DISABLED = true;
                LogUtils.getLogger().error("An exception occurred when loading broadcaster config:");
                LogUtils.getLogger().error(e.getMessage());
            }

        } catch (FileNotFoundException e) {
            LOGGER.warn("Broadcaster config file not found, attempting to generate");
        }

        try {
            Files.createDirectories(Paths.get(FILENAME).getParent());
            FileWriter writer = new FileWriter(FILENAME);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(DataKeys.PREFIX, prefix);
            jsonObject.addProperty(DataKeys.INTERVAL, interval);
            JsonArray messagesJson = new JsonArray();
            messages.forEach(messagesJson::add);
            jsonObject.add(DataKeys.MESSAGES, messagesJson);
            GSON.toJson(jsonObject, writer);
            writer.close();

        } catch (IOException ex) {
            DISABLED = true;
            LogUtils.getLogger().error("Unable to save broadcaster config file");
        }
    }

}
