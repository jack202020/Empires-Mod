package com.andrew2070.Empires.entities.Empire;


import com.google.common.collect.ImmutableList;
import com.google.gson.*;


import com.andrew2070.Empires.API.commands.ChatComponentFormatted;
import com.andrew2070.Empires.API.commands.LocalManager;
import com.andrew2070.Empires.API.commands.IChatFormat;
import com.andrew2070.Empires.API.JSON.API.SerializerTemplate;

import com.andrew2070.Empires.API.container.PermissionsContainer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Iterator;

public class Rank implements IChatFormat {

    /**
     * All the default ranks that are added to each empire on creation (except AdminEmpires)
     */
    public static final Container defaultRanks = new Container();

    public static void initDefaultRanks() {

        Rank leaderRank = new Rank("Leader", null, Type.LEADER);
        Rank officerRank = new Rank("Officer", null, Type.REGULAR);
        Rank citizenRank = new Rank("Citizen", null, Type.DEFAULT);

        leaderRank.permissionsContainer.add("Empires.cmd*");
        leaderRank.permissionsContainer.add("Empires.bypass.*");

        officerRank.permissionsContainer.add("Empires.cmd*");
        officerRank.permissionsContainer.add("-Empires.cmd.leader");
        officerRank.permissionsContainer.add("Empires.bypass.plot");
        officerRank.permissionsContainer.add("Empires.bypass.flag.*");

        citizenRank.permissionsContainer.add("Empires.cmd.everyone.*");
        citizenRank.permissionsContainer.add("Empires.cmd.outsider.*");
        citizenRank.permissionsContainer.add("Empires.bypass.flag.*");
        citizenRank.permissionsContainer.add("Empires.bypass.flag.restrictions");

        Rank.defaultRanks.clear();
        Rank.defaultRanks.add(leaderRank);
        Rank.defaultRanks.add(officerRank);
        Rank.defaultRanks.add(citizenRank);
    }

    private String name, newName = null;
    private Empire empire;
    private Type type;

    public final PermissionsContainer permissionsContainer = new PermissionsContainer();

    public Rank(String name, Empire empire, Type type) {
        this.name = name;
        this.empire = empire;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        this.newName = newName;
    }

    public void resetNewName() {
        this.name = newName;
        this.newName = null;
    }

    public String getNewName() {
        return this.newName;
    }

    public Empire getEmpire() {
        return empire;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.color + getName();
    }

    @Override
    public IChatComponent toChatMessage() {
        return LocalManager.get("Empires.format.rank", name).setChatStyle(new ChatStyle().setColor(type.color));
    }

    public enum Type implements IChatFormat {
        /**
         * Rank that can do anything
         */
        LEADER(EnumChatFormatting.RED, true),

        /**
         * Rank that is assigned to players on joining the empire
         */
        DEFAULT(EnumChatFormatting.GREEN, true),

        /**
         * Nothing special to this rank
         */
        REGULAR(EnumChatFormatting.BLUE, false);

        @Override
        public IChatComponent toChatMessage() {
            IChatComponent name = new ChatComponentFormatted("{" + color.getFormattingCode() + "|%s}", name());
            return LocalManager.get("Empires.format.rank.type.short", name);
        }

        public final EnumChatFormatting color;
        public final boolean unique;

        Type(EnumChatFormatting color, boolean unique) {
            this.color = color;
            this.unique = unique;
        }
    }

    public static class Serializer extends SerializerTemplate<Rank> {

        @Override
        public void register(GsonBuilder builder) {
            builder.registerTypeAdapter(Rank.class, this);
        }

        @Override
        public Rank deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            String name = jsonObject.get("name").getAsString();
            Rank.Type rankType = Type.valueOf(jsonObject.get("type").getAsString());
            Rank rank = new Rank(name, null, rankType);
            if (jsonObject.has("permissions")) {
                rank.permissionsContainer.addAll(ImmutableList.copyOf(context.<String[]>deserialize(jsonObject.get("permissions"), String[].class)));
            }
            return rank;
        }

        @Override
        public JsonElement serialize(Rank rank, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.addProperty("name", rank.name);
            json.addProperty("type", rank.type.toString());
            json.add("permissions", context.serialize(rank.permissionsContainer, ArrayList.class));

            return json;
        }
    }

    public static class Container extends ArrayList<Rank> implements IChatFormat {

        public boolean contains(String rankName) {
            for (Rank r : this) {
                if (r.getName().equals(rankName))
                    return true;
            }
            return false;
        }

        public Rank get(String rankName) {
            for (Rank r : this) {
                if (r.getName().equals(rankName))
                    return r;
            }
            return null;
        }

        public Rank get(Type type) {
            if(!type.unique) {
                throw new RuntimeException("The rank you are trying to get is not unique!");
            }

            for(Rank rank : this) {
                if(rank.getType() == type) {
                    return rank;
                }
            }
            return null;
        }

        public Rank getLeaderRank() {
            for(Rank rank : this) {
                if(rank.getType() == Type.LEADER) {
                    return rank;
                }
            }
            return null;
        }

        public Rank getDefaultRank() {
            for(Rank rank : this) {
                if(rank.getType() == Type.DEFAULT) {
                    return rank;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return toChatMessage().getUnformattedText();
        }

        @Override
        public IChatComponent toChatMessage() {
            IChatComponent root = new ChatComponentText("");

            for (Rank rank : this) {
                if (root.getSiblings().size() > 0) {
                    root.appendSibling(new ChatComponentFormatted("{7|, }"));
                }
                root.appendSibling(rank.toChatMessage());
            }

            return root;
        }
    }
}