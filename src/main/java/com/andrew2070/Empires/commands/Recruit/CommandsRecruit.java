package com.andrew2070.Empires.commands.Recruit;

import com.andrew2070.Empires.API.commands.ChatComponentContainer;
import com.andrew2070.Empires.API.commands.ChatComponentFormatted;
import com.andrew2070.Empires.API.commands.ChatComponentList;
import com.andrew2070.Empires.API.commands.ChatManager;
import com.andrew2070.Empires.entities.*;
import com.andrew2070.Empires.entities.Empire.AdminEmpire;
import com.andrew2070.Empires.entities.Empire.Citizen;
import com.andrew2070.Empires.entities.Empire.Empire;
import com.andrew2070.Empires.entities.Empire.EmpireBlock;
import com.andrew2070.Empires.entities.Empire.Plot;
import com.andrew2070.Empires.entities.Empire.Rank;
import com.andrew2070.Empires.entities.Empire.Wild;
import com.andrew2070.Empires.API.commands.CommandResponse;
import com.andrew2070.Empires.API.commands.Command;
import com.andrew2070.Empires.API.commands.LocalManager;
import com.andrew2070.Empires.Config.Config;
import com.andrew2070.Empires.Datasource.EmpiresUniverse;
import com.andrew2070.Empires.Proxies.EconomyProxy;
import com.andrew2070.Empires.entities.Tools.WhitelisterTool;
import com.andrew2070.Empires.entities.Flags.Flag;
import com.andrew2070.Empires.entities.Flags.FlagType;
import com.andrew2070.Empires.entities.Position.ChunkPos;
import com.andrew2070.Empires.entities.Managers.ToolManager;
import com.andrew2070.Empires.entities.Misc.Tool;
import com.andrew2070.Empires.API.commands.ChatManager;
import com.andrew2070.Empires.exceptions.EmpiresCommandException;
import com.andrew2070.Empires.entities.Tools.PlotSelectionTool;
import com.andrew2070.Empires.entities.Tools.PlotSellTool;
import com.andrew2070.Empires.entities.Signs.SellSign;
import com.andrew2070.Empires.exceptions.EmpiresCommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

import java.util.List;

import com.andrew2070.Empires.API.commands.CommandsEMP;

/**
 * Process methods for all commands that can be used by everyone
 */
public class CommandsRecruit extends CommandsEMP {

    @Command(
            name = "Empires",
            permission = "Empires.cmd",
            alias = {"emp", "empire"},
            syntax = "/empire <command>")
    public static CommandResponse empireCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "leave",
            permission = "Empires.cmd.everyone.leave",
            parentName = "Empires.cmd",
            syntax = "/empire leave [delete]")
    public static CommandResponse leaveCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        if (empire.citizensMap.get(res) != null && empire.citizensMap.get(res).getType() == Rank.Type.LEADER) {
            throw new EmpiresCommandException("Empires.notification.empire.left.asLeader");
        }

        getDatasource().unlinkCitizenFromEmpire(res, empire);

        ChatManager.send(sender, "Empires.notification.empire.left.self", empire);
        empire.notifyEveryone(getLocal().getLocalization("Empires.notification.empire.left", res, empire));
        return CommandResponse.DONE;
    }

    @Command(
            name = "spawn",
            permission = "Empires.cmd.everyone.spawn",
            parentName = "Empires.cmd",
            syntax = "/empire spawn [empire]",
            completionKeys = {"empireCompletion"})
    public static CommandResponse spawnCommand(ICommandSender sender, List<String> args) {
        EntityPlayer player = (EntityPlayer)sender;
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire;
        int amount;

        if (args.isEmpty()) {
            empire = getEmpireFromCitizen(res);
            amount = Config.instance.costAmountSpawn.get();
        } else {
            empire = getEmpireFromName(args.get(0));
            amount = Config.instance.costAmountOtherSpawn.get();
        }

        if (!empire.hasSpawn()) {
            throw new EmpiresCommandException("Empires.cmd.err.spawn.missing", empire);
        }

        if(!empire.hasPermission(res, FlagType.ENTER, empire.getSpawn().getDim(), (int) empire.getSpawn().getX(), (int) empire.getSpawn().getY(), (int) empire.getSpawn().getZ())) {
            throw new EmpiresCommandException("Empires.cmd.err.spawn.protected", empire);
        }

        if(res.getTeleportCooldown() > 0) {
            throw new EmpiresCommandException("Empires.cmd.err.spawn.cooldown", res.getTeleportCooldown(), res.getTeleportCooldown() / 20);
        }

        makePayment(player, amount);
        empire.bank.addAmount(amount);
        getDatasource().saveEmpireBank(empire.bank);
        empire.sendToSpawn(res);
        return CommandResponse.DONE;
    }

    /*@Command(
            name = "select",
            permission = "Empires.cmd.everyone.select",
            parentName = "Empires.cmd",
            syntax = "/empire select <empire>",
            completionKeys = {"empireCompletion"})
    public static CommandResponse selectCommand(ICommandSender sender, List<String> args) {
        if (args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromName(args.get(0));
        if (!empire.citizensMap.containsKey(res)) {
            throw new EmpiresCommandException("Empires.cmd.err.select.notCitizen", empire);
        }
        getDatasource().saveSelectedEmpire(res, empire);
        ChatManager.send(sender, "Empires.notification.empire.select", empire);
        return CommandResponse.DONE;
    }

*/
    @Command(
            name = "blocks",
            permission = "Empires.cmd.everyone.blocks",
            parentName = "Empires.cmd",
            syntax = "/empire blocks <command>")
    public static CommandResponse blocksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "Empires.cmd.everyone.blocks.list",
            parentName = "Empires.cmd.everyone.blocks",
            syntax = "/empire blocks list")
    public static CommandResponse blocksListCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        IChatComponent root = new ChatComponentList();
        root.appendSibling(LocalManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|CLAIMS}")));
        root.appendSibling(empire.empireBlocksContainer.toChatMessage());

        ChatManager.send(sender, root);
        return CommandResponse.DONE;
    }

    @Command(
            name = "info",
            permission = "Empires.cmd.everyone.blocks.info",
            parentName = "Empires.cmd.everyone.blocks",
            syntax = "/empire blocks info")
    public static CommandResponse blocksInfoCommand(ICommandSender sender, List<String> args) {
        Citizen res = getUniverse().getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        IChatComponent header = LocalManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|BLOCKS}"));
        String blocks = empire.empireBlocksContainer.size() + "/" + empire.getMaxBlocks();
        String extraBlocks = empire.getExtraBlocks() + "";
        String farBlocks = empire.empireBlocksContainer.getFarClaims() + "/" + empire.getMaxFarClaims();

        ChatComponentContainer extraBlocksSources = new ChatComponentContainer();
        extraBlocksSources.add(LocalManager.get("Empires.notification.blocks.info.extra", new ChatComponentFormatted("{9|EMPIRE}"), empire.empireBlocksContainer.getExtraBlocks()));
        for(Citizen citizen : empire.citizensMap.keySet()) {
            extraBlocksSources.add(LocalManager.get("Empires.notification.blocks.info.extra", citizen, citizen.getExtraBlocks()));
        }

        ChatManager.send(sender, "Empires.notification.blocks.info", header, blocks, extraBlocks, extraBlocksSources, farBlocks);

        return CommandResponse.DONE;
    }

    @Command(
            name = "perm",
            permission = "Empires.cmd.everyone.perm",
            parentName = "Empires.cmd",
            syntax = "/empire perm <command>")
    public static CommandResponse permCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "Empires.cmd.everyone.perm.list",
            parentName = "Empires.cmd.everyone.perm",
            syntax = "/empire perm list")
    public static CommandResponse permListCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);
        ChatManager.send(sender, empire.flagsContainer.toChatMessage());
        return CommandResponse.DONE;
    }

    public static class Plots {

        @Command(
                name = "perm",
                permission = "Empires.cmd.everyone.plot.perm",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot perm <command>")
        public static CommandResponse plotPermCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "set",
                permission = "Empires.cmd.everyone.plot.perm.set",
                parentName = "Empires.cmd.everyone.plot.perm",
                syntax = "/empire plot perm set <flag> <value>",
                completionKeys = {"flagCompletion"})
        public static CommandResponse plotPermSetCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 2) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Plot plot = getPlotAtCitizen(res);
            if (!plot.ownersContainer.contains(res) && !plot.getEmpire().hasPermission(res, "Empires.bypass.plot")) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.perm.set.noPermission");
            }

            Flag flag = getFlagFromName(plot.flagsContainer, args.get(0));

            if (flag.setValue(args.get(1))) {
                ChatManager.send(sender, "Empires.notification.perm.success");
            } else {
                throw new EmpiresCommandException("Empires.cmd.err.perm.valueNotValid");
            }

            getDatasource().saveFlag(flag, plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "toggle",
                permission = "Empires.cmd.everyone.plot.perm.toggle",
                parentName = "Empires.cmd.everyone.plot.perm",
                syntax = "/empire plot perm set <flag>",
                completionKeys = {"flagCompletion"})
        public static CommandResponse plotPermToggleCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Plot plot = getPlotAtCitizen(res);
            if (!plot.ownersContainer.contains(res) && !plot.getEmpire().hasPermission(res, "Empires.bypass.plot")) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.perm.set.noPermission");
            }

            Flag flag = getFlagFromName(plot.flagsContainer, args.get(0));

            if (flag.toggle()) {
                ChatManager.send(sender, "Empires.notification.perm.success");
            } else {
                throw new EmpiresCommandException("Empires.cmd.err.perm.valueNotValid");
            }

            getDatasource().saveFlag(flag, plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "list",
                permission = "Empires.cmd.everyone.plot.perm.list",
                parentName = "Empires.cmd.everyone.plot.perm",
                syntax = "/empire plot perm list")
        public static CommandResponse plotPermListCommand(ICommandSender sender, List<String> args) {
            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Plot plot = getPlotAtCitizen(res);
            ChatManager.send(sender, plot.flagsContainer.toChatMessage());
            return CommandResponse.DONE;
        }

        @Command(
                name = "whitelist",
                permission = "Empires.cmd.everyone.plot.perm.whitelist",
                parentName = "Empires.cmd.everyone.plot.perm",
                syntax = "/empire plot perm whitelist")
        public static CommandResponse plotPermWhitelistCommand(ICommandSender sender, List<String> args) {
            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);

            ToolManager.instance.register(new WhitelisterTool(res));
            ChatManager.send(sender, "Empires.notification.perm.whitelist.start");
            return CommandResponse.DONE;
        }

        @Command(
                name = "plot",
                permission = "Empires.cmd.everyone.plot",
                parentName = "Empires.cmd",
                syntax = "/empire plot <command>")
        public static CommandResponse plotCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "rename",
                permission = "Empires.cmd.everyone.plot.rename",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot rename <name>")
        public static CommandResponse plotRenameCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Plot plot = getPlotAtCitizen(res);

            if (!plot.ownersContainer.contains(res) && !plot.getEmpire().hasPermission(res, "Empires.bypass.plot")) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.perm.set.noPermission");
            }

            plot.setName(args.get(0));
            getDatasource().savePlot(plot);

            ChatManager.send(sender, "Empires.notification.plot.renamed");

            return CommandResponse.DONE;
        }

        @Command(
                name = "new",
                permission = "Empires.cmd.everyone.plot.new",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot new <plot>")
        public static CommandResponse plotNewCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            ToolManager.instance.register(new PlotSelectionTool(res, args.get(0)));
            return CommandResponse.DONE;
        }

        @Command(
                name = "select",
                permission = "Empires.cmd.everyone.plot.select",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot select <command>")
        public static CommandResponse plotSelectCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "reset",
                permission = "Empires.cmd.everyone.plot.select.reset",
                parentName = "Empires.cmd.everyone.plot.select",
                syntax = "/empire plot select reset")
        public static CommandResponse plotSelectResetCommand(ICommandSender sender, List<String> args) {
            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Tool currentTool = ToolManager.instance.get(res.getPlayer());
            if(currentTool == null || !(currentTool instanceof PlotSelectionTool)) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.noPermission");
            }
            ((PlotSelectionTool) currentTool).resetSelection(true, 0);
            ChatManager.send(sender, "Empires.notification.plot.selectionReset");
            return CommandResponse.DONE;
        }

        @Command(
                name = "show",
                permission = "Empires.cmd.everyone.plot.show",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot show")
        public static CommandResponse plotShowCommand(ICommandSender sender, List<String> args) {
            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);
            empire.plotsContainer.show(res);
            ChatManager.send(sender, "Empires.notification.plot.showing");
            return CommandResponse.DONE;
        }

        @Command(
                name = "hide",
                permission = "Empires.cmd.everyone.plot.hide",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot hide")
        public static CommandResponse plotHideCommand(ICommandSender sender, List<String> args) {
            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);
            empire.plotsContainer.hide(res);
            ChatManager.send(sender, "Empires.notification.plot.vanished");
            return CommandResponse.DONE;
        }

        @Command(
                name = "add",
                permission = "Empires.cmd.everyone.plot.add",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot add <command>")
        public static CommandResponse plotAddCommand(ICommandSender sender, List<String> args) {
            return CommandResponse.SEND_HELP_MESSAGE;
        }

        @Command(
                name = "owner",
                permission = "Empires.cmd.everyone.plot.add.owner",
                parentName = "Empires.cmd.everyone.plot.add",
                syntax = "/empire plot add owner <citizen>",
                completionKeys = {"citizenCompletion"})
        public static CommandResponse plotAddOwnerCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Citizen target = getCitizenFromName(args.get(0));

            Empire empire = getEmpireFromCitizen(res);
            if (!target.empiresContainer.contains(empire)) {
                throw new EmpiresCommandException("Empires.cmd.err.citizen.notInEmpire", target);
            }

            Plot plot = getPlotAtCitizen(res);

            if(!plot.ownersContainer.contains(res) && !empire.hasPermission(res, "Empires.bypass.plot")) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.noPermission");
            }

            if(plot.ownersContainer.contains(target) || plot.membersContainer.contains(target)) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.add.alreadyInPlot");
            }

            if (!empire.plotsContainer.canCitizenMakePlot(target)) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.limit.toPlayer", target);
            }

            getDatasource().linkCitizenToPlot(target, plot, true);

            ChatManager.send(sender, "Empires.notification.plot.owner.sender.added", target, plot);
            ChatManager.send(target.getPlayer(), "Empires.notification.plot.owner.target.added", plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "member",
                permission = "Empires.cmd.everyone.plot.add.member",
                parentName = "Empires.cmd.everyone.plot.add",
                syntax = "/empire plot add member <citizen>",
                completionKeys = {"citizenCompletion"})
        public static CommandResponse plotAddMemberCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Citizen target = getCitizenFromName(args.get(0));
            Plot plot = getPlotAtCitizen(res);

            if(!plot.ownersContainer.contains(res) && !plot.getEmpire().hasPermission(res, "Empires.bypass.plot")) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.notOwner");
            }

            if(plot.ownersContainer.contains(target) || plot.membersContainer.contains(target)) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.add.alreadyInPlot");
            }

            getDatasource().linkCitizenToPlot(target, plot, false);

            ChatManager.send(sender, "Empires.notification.plot.member.sender.added", target, plot);
            ChatManager.send(target.getPlayer(), "Empires.notification.plot.member.target.added", plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "remove",
                permission = "Empires.cmd.everyone.plot.remove",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot remove <citizen>",
                completionKeys = {"citizenCompletion"})
        public static CommandResponse plotRemoveCommand(ICommandSender sender, List<String> args) {
            if (args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Citizen target = getCitizenFromName(args.get(0));
            Plot plot = getPlotAtCitizen(res);

            if(!plot.ownersContainer.contains(res) && !plot.getEmpire().hasPermission(res, "Empires.bypass.plot")) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.noPermission");
            }

            if(!plot.ownersContainer.contains(target) && !plot.membersContainer.contains(target)) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.remove.notInPlot");
            }

            if(plot.ownersContainer.contains(target) && plot.ownersContainer.size() == 1) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.remove.onlyOwner");
            }

            getDatasource().unlinkCitizenFromPlot(target, plot);

            ChatManager.send(sender, "Empires.notification.plot.sender.removed", target, plot);
            ChatManager.send(target.getPlayer(), "Empires.notification.plot.target.removed", plot);
            return CommandResponse.DONE;

        }

        @Command(
                name = "info",
                permission = "Empires.cmd.everyone.plot.info",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot info")
        public static CommandResponse plotInfoCommand(ICommandSender sender, List<String> args) {
            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Plot plot = getPlotAtCitizen(res);

            IChatComponent header = LocalManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|%s}", plot.getName()));
            ChatManager.send(sender, "Empires.format.plot.long", header, plot.ownersContainer, plot.toVolume());
            return CommandResponse.DONE;
        }

        @Command(
                name = "delete",
                permission = "Empires.cmd.everyone.plot.delete",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot delete")
        public static CommandResponse plotDeleteCommand(ICommandSender sender, List<String> args) {
            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Plot plot = getPlotAtCitizen(res);
            if (!plot.ownersContainer.contains(res) && !plot.getEmpire().hasPermission(res, "Empires.bypass.plot")) {
                throw new EmpiresCommandException("Empires.cmd.err.plot.noPermission");
            }

            World world;
            if(sender instanceof EntityPlayer) {
                world = ((EntityPlayer) sender).worldObj;
            } else {
                world = MinecraftServer.getServer().worldServerForDimension(plot.getDim());
            }

            plot.deleteSignBlocks(SellSign.SellSignType.instance, world);

            getDatasource().deletePlot(plot);
            ChatManager.send(sender, "Empires.notification.plot.deleted", plot);
            return CommandResponse.DONE;
        }

        @Command(
                name = "sell",
                permission = "Empires.cmd.everyone.plot.sell",
                parentName = "Empires.cmd.everyone.plot",
                syntax = "/empire plot sell <price>")
        public static CommandResponse plotSellCommand(ICommandSender sender, List<String> args) {
            if(args.size() < 1) {
                return CommandResponse.SEND_SYNTAX;
            }

            Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
            Empire empire = getEmpireFromCitizen(res);

            checkPositiveInteger(args.get(0));

            int price = Integer.parseInt(args.get(0));
            ToolManager.instance.register(new PlotSellTool(res, price));
            return CommandResponse.DONE;
        }
    }

    @Command(
            name = "ranks",
            permission = "Empires.cmd.everyone.ranks",
            parentName = "Empires.cmd",
            syntax = "/empire ranks <command>")
    public static CommandResponse ranksCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "list",
            permission = "Empires.cmd.everyone.ranks.list",
            parentName = "Empires.cmd.everyone.ranks",
            syntax = "/empire ranks list")
    public static CommandResponse listRanksCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        IChatComponent root = new ChatComponentList();
        root.appendSibling(LocalManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|RANKS}")));
        for (Rank rank : empire.ranksContainer) {
            root.appendSibling(new ChatComponentFormatted("{7| - }").appendSibling(LocalManager.get("Empires.format.rank.long", rank.getName(), rank.getType())));
        }

        ChatManager.send(sender, root);
        return CommandResponse.DONE;
    }

    @Command(
            name = "borders",
            permission = "Empires.cmd.everyone.borders",
            parentName = "Empires.cmd",
            syntax = "/empire borders <command>")
    public static CommandResponse bordersCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "show",
            permission = "Empires.cmd.everyone.borders.show",
            parentName = "Empires.cmd.everyone.borders",
            syntax = "/empire borders show")
    public static CommandResponse bordersShowCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        empire.empireBlocksContainer.show(res);
        ChatManager.send(sender, "Empires.notification.empire.borders.show", empire);
        return CommandResponse.DONE;
    }

    @Command(
            name = "hide",
            permission = "Empires.cmd.everyone.borders.hide",
            parentName = "Empires.cmd.everyone.borders",
            syntax = "/empire borders hide")
    public static CommandResponse bordersHideCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        empire.empireBlocksContainer.hide(res);
        ChatManager.send(sender, "Empires.notification.empire.borders.hide");
        return CommandResponse.DONE;
    }

    @Command(
            name = "bank",
            permission = "Empires.cmd.everyone.bank",
            parentName = "Empires.cmd",
            syntax = "/empire bank <command>")
    public static CommandResponse bankCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "info",
            permission = "Empires.cmd.everyone.bank.info",
            parentName = "Empires.cmd.everyone.bank",
            syntax = "/empire bank info")
    public static CommandResponse bankAmountCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        if(empire instanceof AdminEmpire) {
            throw new EmpiresCommandException("Empires.cmd.err.adminEmpire", empire);
        }

        ChatManager.send(sender, "Empires.notification.empire.bank.info", EconomyProxy.getCurrency(empire.bank.getAmount()), EconomyProxy.getCurrency(empire.bank.getNextPaymentAmount()));
        return CommandResponse.DONE;
    }

    @Command(
            name = "deposit",
            permission = "Empires.cmd.everyone.bank.deposit",
            parentName = "Empires.cmd.everyone.bank",
            syntax = "/empire bank deposit <amount>")
    public static CommandResponse bankPayCommand(ICommandSender sender, List<String> args) {
        if(args.size() < 1) {
            return CommandResponse.SEND_SYNTAX;
        }

        checkPositiveInteger(args.get(0));

        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        Empire empire = getEmpireFromCitizen(res);

        if(empire instanceof AdminEmpire) {
            throw new EmpiresCommandException("Empires.cmd.err.adminEmpire", empire);
        }

        int amount = Integer.parseInt(args.get(0));
        makePayment(res.getPlayer(), amount);
        empire.bank.addAmount(amount);
        getDatasource().saveEmpireBank(empire.bank);
        return CommandResponse.DONE;
    }

    @Command(
            name = "wild",
            permission = "Empires.cmd.everyone.wild",
            parentName = "Empires.cmd",
            syntax = "/empire wild <command>")
    public static CommandResponse permWildCommand(ICommandSender sender, List<String> args) {
        return CommandResponse.SEND_HELP_MESSAGE;
    }

    @Command(
            name = "perm",
            permission = "Empires.cmd.everyone.wild.perm",
            parentName = "Empires.cmd.everyone.wild",
            syntax = "/empire wild perm")
    public static CommandResponse permWildListCommand(ICommandSender sender, List<String> args) {
        Citizen res = EmpiresUniverse.instance.getOrMakeCitizen(sender);
        ChatManager.send(sender, Wild.instance.flagsContainer.toChatMessage());
        return CommandResponse.DONE;
    }
}