package nissining.moretp;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import nissining.moretp.menu.TpaMenu;

import java.io.File;

public class MoreTp extends PluginBase implements Listener {

    public TpaMenu menu;

    @Override
    public void onEnable() {
        if (!getDataFolder().mkdirs()) {
            getLogger().notice("TpaUI Enabled!");
        }
        this.menu = new TpaMenu(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        MoreTpPlayer.players.forEach(MoreTpPlayer::save);
    }



    public Config getPlayerConfig(Player player) {
        return new Config(new File(getDataFolder(), "players/" + player.getName() + ".yml"), 2);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if ("tpa".equals(command.getName())) {
                menu.tpMenu((Player) sender);
            }
        }
        return true;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        MoreTpPlayer mtp = MoreTpPlayer.getMtp(player.getName());
        if (mtp != null && mtp.getDeadPos() == null) {
            mtp.setDeadPos(player.floor().add(0.5, 1, 0.5));
            player.sendMessage("已记录你的死亡位置！可输入 /tpa 返回死亡点！");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (MoreTpPlayer.addMtp(player)) {
            MoreTpPlayer mtp = MoreTpPlayer.getMtp(player);

            mtp.config = getPlayerConfig(player);
            mtp.load();

            player.sendMessage("输入 /tpa 打开传送菜单！");
        }
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        MoreTpPlayer mtp = MoreTpPlayer.getMtp(player);
        if (mtp != null && mtp.isSetPos()) {
            event.setCancelled();
            String msg = event.getMessage();
            if (msg.equals("quit")) {
                mtp.setSetPos(false);
                player.sendMessage("已退出创建点设置！");
            } else {
                mtp.addPos(msg);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        MoreTpPlayer.removeMtp(event.getPlayer());
    }

}
