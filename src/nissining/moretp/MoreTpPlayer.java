package nissining.moretp;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.List;

public class MoreTpPlayer {

    public static List<MoreTpPlayer> players = new ArrayList<>();

    public static MoreTpPlayer getMtp(String n) {
        return players.stream()
                .filter(mtp -> n.equals(mtp.getName()))
                .findFirst()
                .orElse(null);
    }

    public static MoreTpPlayer getMtp(Player player) {
        return getMtp(player.getName());
    }

    public static boolean addMtp(Player player) {
        if (getMtp(player.getName()) == null) {
            players.add(new MoreTpPlayer(player));
            return true;
        }
        return false;
    }

    public static void removeMtp(Player player) {
        if (getMtp(player.getName()) != null) {
            MoreTpPlayer mtp = getMtp(player.getName());
            mtp.save();
            players.remove(mtp);
        }
    }

    private final Player player;
    private Position deadPos = null; //死亡位置
    private ConfigSection tempPos = new ConfigSection(); //已记录的位置点
    private boolean isSetPos = false; //进行记录点

    public Config config;

    public MoreTpPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return player.getName();
    }

    public void setDeadPos(Position deadPos) {
        this.deadPos = deadPos;
    }

    public Position getDeadPos() {
        return deadPos;
    }

    public boolean backToDeadPos() {
        return getDeadPos() != null && getPlayer().teleport(getDeadPos());
    }

    public boolean isSetPos() {
        return isSetPos;
    }

    public void setSetPos(boolean setPos) {
        isSetPos = setPos;
    }

    public ConfigSection getTempPos() {
        return tempPos;
    }

    public void addPos(String posName) {
        if (tempPos.keySet().size() > 5) {
            player.sendMessage("§c最多只能设置5个点哦！可以覆盖之前设置的点！");
            return;
        }

        String toStringPos = player.getFloorX() + "/" +
                player.getFloorY() + "/" +
                player.getFloorZ() + "/" +
                player.level.getFolderName();

        String stat;
        if (tempPos.containsKey(posName)) {
            stat = TextFormat.RED + "已存在创建点： " + posName + " 为你覆盖原先的位置!";
        } else {
            stat = TextFormat.GREEN + "已创建记录点： " + posName;
        }
        tempPos.put(posName, toStringPos);
        player.sendMessage(stat + " 位置： " + toStringPos);
        player.sendMessage("输入 §aquit §f即可退出创建模式！");
    }

    public Position tpToTempPos(String posName) {
        if (tempPos.containsKey(posName)) {

            String[] ss = tempPos.getString(posName).split("/");
            Position pos = new Position(
                    Integer.parseInt(ss[0]),
                    Integer.parseInt(ss[1]),
                    Integer.parseInt(ss[2]),
                    player.getServer().getLevelByName(ss[3])
            );

            player.teleport(pos.floor().add(0.5, 1, 0.5));
            return pos;
        }

        return null;
    }

    public void load() {
        if (config != null)
            tempPos = config.getSection("pos");
    }

    public void save() {
        config.set("pos", tempPos);
        config.save();
    }

}
