package com.andrew2070.Empires.Config;



import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.andrew2070.Empires.API.JSON.JsonConfig;
import com.andrew2070.Empires.API.permissions.Bridges.MyPermissionsBridge;
import com.andrew2070.Empires.entities.Permissions.Meta;
import com.andrew2070.Empires.entities.Permissions.User;

public class UserConfig extends JsonConfig<User, User.Container> {

    private MyPermissionsBridge permissionsManager;

    public UserConfig(String path, MyPermissionsBridge permissionsManager) {
        super(path, "UserConfig");
        this.permissionsManager = permissionsManager;
        this.gsonType = new TypeToken<User.Container>() {}.getType();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new User.Serializer())
                .registerTypeAdapter(Meta.Container.class, new Meta.Container.Serializer())
                .setPrettyPrinting().create();
    }

    @Override
    protected User.Container newList() {
        return new User.Container();
    }

    @Override
    public User.Container read() {
        User.Container users = super.read();
        permissionsManager.users.addAll(users);
        return users;
    }
}