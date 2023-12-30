package com.selfdot.broadcaster;

import net.minecraft.entity.player.PlayerEntity;

import static net.minecraft.util.Formatting.*;

public class ChatColourUtils {

    public static String format(String s) {
        return s.replace("&0", "" + BLACK).replace("&1", "" + DARK_BLUE)
            .replace("&2", "" + DARK_GREEN).replace("&3", "" + DARK_AQUA)
            .replace("&4", "" + DARK_RED).replace("&5", "" + DARK_PURPLE)
            .replace("&6", "" + GOLD).replace("&7", "" + GRAY)
            .replace("&8", "" + DARK_GRAY).replace("&9", "" + BLUE)
            .replace("&a", "" + GREEN).replace("&b", "" + AQUA)
            .replace("&c", "" + RED).replace("&d", "" + LIGHT_PURPLE)
            .replace("&e", "" + YELLOW).replace("&f", "" + WHITE)
            .replace("&k", "" + OBFUSCATED).replace("&l", "" + BOLD)
            .replace("&m", "" + STRIKETHROUGH).replace("&n", "" + UNDERLINE)
            .replace("&o", "" + ITALIC).replace("&r", "" + RESET)
            .replace("&A", "" + GREEN).replace("&B", "" + AQUA)
            .replace("&C", "" + RED).replace("&D", "" + LIGHT_PURPLE)
            .replace("&E", "" + YELLOW).replace("&F", "" + WHITE)
            .replace("&K", "" + OBFUSCATED).replace("&L", "" + BOLD)
            .replace("&M", "" + STRIKETHROUGH).replace("&N", "" + UNDERLINE)
            .replace("&O", "" + ITALIC).replace("&R", "" + RESET);
    }

}
