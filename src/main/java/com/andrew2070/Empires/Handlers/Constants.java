package com.andrew2070.Empires.Handlers;
import net.minecraft.util.EnumChatFormatting;
import com.andrew2070.Empires.entities.Flags.FlagType;

public class Constants {
    public static String CONFIG_FOLDER = "";
    public static String DATABASE_FOLDER = "";

    public static final String VERSION = "@VERSION@";
    public static final String MODID = "Empires";
    public static final String MODNAME = MODID;
    public static final String DEPENDENCIES = "";

    public static final String EDIT_TOOL_NAME = EnumChatFormatting.BLUE + "Selector"; // TODO: Get localization for it, maybe?
    public static final String EDIT_TOOL_DESCRIPTION_PLOT = EnumChatFormatting.DARK_AQUA + "Select 2 blocks to make a plot.";
    public static final String WHITELISTER_TOOL_DESCRIPTION_HEADER = EnumChatFormatting.DARK_AQUA + "Select block for bypassing protection. Shift right-click to change flag.";
    public static final String WHITELISTER_TOOL_DESCRIPTION_FLAG = EnumChatFormatting.DARK_AQUA + "Flag: " + FlagType.ACCESS.toString().toLowerCase() + " & " + FlagType.ACTIVATE.toString().toLowerCase();

    public static final String SIGN_SHOP_NAME = EnumChatFormatting.BLUE + "Shop Sign";
    public static final String SIGN_ID_TEXT = "" + EnumChatFormatting.ITALIC + EnumChatFormatting.GRAY + "ID: ";

    public static final String PLOT_SELL_NAME = EnumChatFormatting.BLUE + "Plot Sell";
    public static final String PLOT_SELL_IDENTIFIER = EnumChatFormatting.DARK_BLUE + "Plot Sale";

    private Constants() {
    }
}