package com.andrew2070.Empires.Datasource;


import cpw.mods.fml.common.ICrashCallable;
import com.andrew2070.Empires.Empires;


import com.andrew2070.Empires.Datasource.EmpiresUniverse;
import com.andrew2070.Empires.Datasource.EmpiresDatasource;

import java.sql.Connection;
import java.sql.SQLWarning;

// TODO Add more info about the datasource?

/**
 * Adds datasource info to the crash report
 */
public class DatasourceCrashCallable implements ICrashCallable {
    @Override
    public String call() throws Exception {
        EmpiresDatasource datasource = Empires.instance.datasource;
        if (datasource == null) {
            return "Datasource is not initialized yet";
        }
        String str = "";

        str += String.format("Class: %s\n", datasource.getClass().getName());
        str += String.format("Stats (Empires: %s, Citizens: %s, Empires: %s, Blocks: %s, Ranks: %s, Plots: %s)\n", EmpiresUniverse.instance.empires.size(), EmpiresUniverse.instance.citizens.size(), 0 /*MyTownUniverse.instance.getNationsMap().size()*/, EmpiresUniverse.instance.blocks.size(), EmpiresUniverse.instance.ranks.size(), EmpiresUniverse.instance.plots.size());
        Connection conn = datasource.getBridge().getConnection();

        str += String.format("AutoCommit: %s%n", conn.getAutoCommit());
        str += String.format("----- SQL Warnings -----%n");
        str += String.format("%s8 | %s9 | %s%n", "SQLState", "ErrorCode", "Message");
        SQLWarning sqlWarning = conn.getWarnings();
        do {
            str += String.format("%s8 | %s9 | %s%n", sqlWarning.getSQLState(), sqlWarning.getErrorCode(), sqlWarning.getMessage());
        } while (sqlWarning.getNextWarning() != null);
        return str;
    }

    @Override
    public String getLabel() {
        return "Empires|Datasource";
    }
}