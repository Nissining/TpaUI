package nissining.moretp.menu;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.level.Position;
import nissining.moretp.MoreTp;
import nissining.moretp.MoreTpPlayer;
import nissining.moretp.utils.MyForm;

import java.util.ArrayList;
import java.util.List;

public class TpaMenu {

    private final MoreTp moreTp;

    private final List<TpaButton> buttons = new ArrayList<TpaButton>() {{
        add(new TpaButton("传送到玩家身边", "items/ender_eye"));
        add(new TpaButton("回到死亡点", "items/ender_eye"));
        add(new TpaButton("返回地图出生点", "items/armor_stand"));
        add(new TpaButton("我的记录点", "items/ender_eye"));
        add(new TpaButton("创建记录点", "items/armor_stand"));
    }};

    public TpaMenu(MoreTp moreTp) {
        this.moreTp = moreTp;
    }

    public void tpMenu(Player player) {
        MoreTpPlayer mtp = MoreTpPlayer.getMtp(player);
        if (mtp == null) {
            return;
        }

        FormWindowSimple f = new FormWindowSimple("TpaUI - 传送菜单", "");

        buttons.forEach(f::addButton);

        MyForm myForm = new MyForm(player, f) {
            @Override
            public void call() {
                switch (getButtonText()) {
                    case "传送到玩家身边":
                    default:
                        tpPlayer(player);
                        break;
                    case "回到死亡点":
                        if (mtp.backToDeadPos()) {
                            player.sendMessage("已返回到死亡位置！");
                        } else {
                            player.sendMessage("没有记录你的死亡位置！");
                        }
                        break;
                    case "返回地图出生点":
                        player.teleport(player.level.getSafeSpawn().floor().add(0.5, 1, 0.5));
                        player.sendMessage("已返回到出生点！");
                        break;
                    case "创建记录点":
                        mtp.setSetPos(true);
                        player.sendMessage("记录完成可使用 /tpa 进行传送该位置！");
                        player.sendMessage("输入 quit 可退出当前设置！");
                        player.sendMessage("请输入记录点名称！输入完成后将会记录当前位置！");
                        break;
                    case "我的记录点":
                        tpPosList(mtp);
                        break;
                }
            }
        };
        myForm.sendToPlayer(player);
    }

    private void tpPlayer(Player player) {
        Server server = moreTp.getServer();

        FormWindowSimple f = new FormWindowSimple("传送玩家", "");

        server.getOnlinePlayers().values()
                .stream()
                .filter(onp -> !onp.getName().equals(player.getName()))
                .forEach(onp -> f.addButton(
                        new ElementButton(onp.getNameTag(), new ElementButtonImageData("path", "textures/items/ender_eye"))
                ));

        MyForm myForm = new MyForm(player, f) {
            @Override
            public void call() {
                if (wasClosed()) {
                    return;
                }

                Player target = server.getPlayerExact(getButtonText());
                if (target != null) {
                    sureTp(target, player, player.getName() + "请求传送到你身边");
                    player.sendMessage("传送请求已发送！");
                } else {
                    player.sendMessage("目标不存在或不在线");
                }

            }
        };
        myForm.sendToPlayer(player);
    }

    private void sureTp(Player player, Position position, String text) {
        FormWindowSimple f = new FormWindowSimple("是否接受传送请求？", text);

        for (String s : new String[]{"§a接受", "§c不接受"}) {
            f.addButton(new ElementButton(s));
        }

        MyForm form = new MyForm(player, f) {
            @Override
            public void call() {
                if (wasClosed())
                    return;

                if (getButtonId() == 0) {
                    player.teleport(position);
                }
            }
        };
        form.sendToPlayer(player);
    }

    private void tpPosList(MoreTpPlayer mtp) {
        if (mtp == null)
            return;

        FormWindowSimple f = new FormWindowSimple("我的记录点", "点击进行传送");

        mtp.getTempPos().forEach((k, v) -> f.addButton(
                new ElementButton(k)
        ));

        MyForm myForm = new MyForm(mtp.getPlayer(), f) {
            @Override
            public void call() {
                if (wasClosed())
                    return;

                String stat;
                Position pos = mtp.tpToTempPos(getButtonText());
                if (pos == null) {
                    stat = "§c目标位置不存在";
                } else {
                    stat = "§a已传送到目标点";
                }
                player.sendMessage(stat + "： " + getButtonText() + " 位置信息：" + (pos == null ? "none" : pos.toString()));
            }
        };

        myForm.sendToPlayer(mtp.getPlayer());
    }


    public static class TpaButton extends ElementButton {

        public TpaButton(String s, String img) {
            super(s);
            this.addImage(new ElementButtonImageData("path", "textures/" + img));
        }
    }


}
