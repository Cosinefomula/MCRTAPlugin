package com.github.cosinefomula.mcrtaplugin;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class RTAManager extends BukkitRunnable {
    Objective objective;

    @Override
    public void run() {
        objective.getScore("RTA-Time").setScore(objective.getScore("RTA-Time").getScore() + 1);
    }
    public RTAManager(Objective scoreData){
        objective = scoreData;
    }
}

