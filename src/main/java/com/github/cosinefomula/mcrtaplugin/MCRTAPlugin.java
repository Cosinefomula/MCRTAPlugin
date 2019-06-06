package com.github.cosinefomula.mcrtaplugin;



import com.sun.istack.internal.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;


public final class MCRTAPlugin extends JavaPlugin implements Listener {
    private static boolean rtaplay;
    private Objective objective;
    private RTAManager rtaManager;
    private Scoreboard scoreboard;
    @Override
    public void onEnable() {
        // Plugin startup logic
        ScoreboardManager manager = Bukkit.getServer().getScoreboardManager();
        scoreboard = manager. getNewScoreboard();
        if(scoreboard.getObjective("RTATimer") != null){
            objective = scoreboard.getObjective("RTATimer");
        }else {
            objective = scoreboard.registerNewObjective("RTATimer", "dummy", "RTATimer");
            Score score = objective.getScore("RTA-Time");
            Score score2 = objective.getScore("RTA-FinishTime");
            score.setScore(0);
            score2.setScore(0);
        }
        getServer().getPluginManager().registerEvents(this,this);
        getLogger().info("起動しました");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("終了しました");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command,String label,String[] args){
        if ("rta".equals(label)) {
            switch (args[0]) {
                case "start":
                    if(rtaplay) {
                        rtaStart(sender);
                    }
                    break;
                case "stop":
                    if (rtaManager != null) {
                        rtaManager.cancel();
                        rtaManager = null;
                    } else {
                        sender.sendMessage("タイマーは止まっています");
                    }
                    break;
                case "lap":
                    if(rtaplay) {
                        sender.sendMessage(timeChange(objective.getScore("RTA-Time").getScore()));
                    }
                    break;
                case "reset":
                    if (rtaManager == null) {
                        objective.getScore("RTA-Time").setScore(0);
                        objective.getScore("RTA-FinishTime").setScore(0);
                    } else {
                        sender.sendMessage("まだカウントが行われています");
                    }
                    break;
                case "ready":
                    objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                    getServer().getOnlinePlayers().forEach(player -> player.setScoreboard(scoreboard));
                    rtaplay = true;
                    break;
                case "off":
                    objective.setDisplaySlot(null);
                    rtaplay = false;
                    break;
            }
        } else {
            return false;
        }
        return false;
    }

    private void rtaStart(CommandSender sender){
        if(rtaManager == null) {
            if (objective.getScore("RTA-Time").getScore() == 0){
                new BukkitRunnable(){
                    int i = 6;
                    @Override
                    public void run(){
                        if(i>0){
                            int s =i;
                            getServer().getOnlinePlayers().forEach(
                                    player -> player.sendTitle
                                            (ChatColor.BLUE +String.valueOf(s-1),"",
                                                    5,10,5));
                            i--;
                        }
                        if(i==0){
                            getServer().getOnlinePlayers().forEach(
                                    player -> player.sendTitle
                                            (ChatColor.BLUE +"Start",
                                                    "手段は問わない、ヤツを倒せ",
                                                    5,20,10));
                            this.cancel();
                        }
                    }
                }.runTaskTimer(this,0L,20L);
                rtaManager = new RTAManager(objective);
                rtaManager.runTaskTimer(this,100L,20L);
            }else{
            rtaManager = new RTAManager(objective);
            rtaManager.runTaskTimer(this,0L,20L);
            sender.sendMessage("タイマー起動");
            }

        }else{
            sender.sendMessage("タイマーはもう起動してます");
        }
    }
    @EventHandler
    public void rtaFinish(EntityDeathEvent event){
        if(event.getEntity() instanceof EnderDragon && rtaManager != null){
            rtaManager.cancel();
            rtaManager =null;
            int finish = objective.getScore("RTA-Time").getScore();
            objective.getScore("RTA-FinishTime").setScore(finish);
            String time = timeChange(finish);
            getServer().getOnlinePlayers().forEach(player -> player.sendTitle(
                            "§aFinish","§eTime:"+time,10,100,10));
        }
    }
    @EventHandler
    public void joinEvent(PlayerJoinEvent event){
        if(rtaplay){
            event.getPlayer().setScoreboard(scoreboard);
        }
    }
    private static String timeChange(int i){
        int hour = i/3600;
        int min = i%3600/60;
        int sec = i%3600%60;
        return hour+"hour"+min+"min"+sec+"sec.";
    }
}

