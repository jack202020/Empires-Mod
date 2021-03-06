package com.andrew2070.Empires.API.permissions;



import com.andrew2070.Empires.API.commands.ChatComponentFormatted;
import com.andrew2070.Empires.API.commands.ChatComponentList;
import com.andrew2070.Empires.API.commands.IChatFormat;
import com.andrew2070.Empires.API.commands.LocalManager;
import com.andrew2070.Empires.entities.Permissions.PermissionLevel;

import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Iterator;

public class PermissionsContainer extends ArrayList<String> implements IChatFormat {

    public PermissionLevel hasPermission(String permission) {
        PermissionLevel permLevel = PermissionLevel.NONE;
        if (contains(permission)) {
            permLevel = PermissionLevel.ALLOWED;
        }

        for (String p : this) {
            if (p.endsWith("*")) {
                if (permission.startsWith(p.substring(0, p.length() - 1))) {
                    permLevel = PermissionLevel.ALLOWED;
                } else if(p.startsWith("-") && permission.startsWith(p.substring(1, p.length() - 1))) {
                    permLevel = PermissionLevel.DENIED;
                }
            } else {
                if (permission.equals(p)) {
                    permLevel = PermissionLevel.ALLOWED;
                } else if(p.startsWith("-") && permission.equals(p.substring(1))) {
                    permLevel = PermissionLevel.DENIED;
                }
            }
        }

        return permLevel;
    }


    public boolean remove(String perm) {
        for(Iterator<String> it = iterator(); it.hasNext(); ) {
            String p = it.next();
            if(p.equals(perm)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public IChatComponent toChatMessage() {
        IChatComponent root = new ChatComponentList();
        root.appendSibling(LocalManager.get("Empires.format.list.header", new ChatComponentFormatted("{9|PERMISSIONS}")));
        for(String perm : this) {
            root.appendSibling(LocalManager.get("Empires.format.permission", perm));
        }
        return root;
    }
}