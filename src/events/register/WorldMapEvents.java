package events.register;

import com.github.cliftonlabs.json_simple.JsonObject;
import events.common.Event;
import game.GameException;
import util.Logger;

import java.util.HashMap;

public class WorldMapEvents {

    private static String encryptName(JsonObject json) {
        if (json != null) {
            StringBuffer str = new StringBuffer();
            str.append(json.get("buiX"));
            str.append(json.get("buiY"));
            str.append(0);
            str.append(json.get("telX"));
            str.append(json.get("telY"));
            return str.reverse().toString();
        }
        return Math.random() + "";
    }


    public static void register(HashMap<String, Event> _map) {

        Event.builder(_map, "change_name")
                .setDelay(1)
                .setLoc(40, 56)
                .setListener(((event, game) -> {
                    game.dispatch(Event.builder().setLoc(360, 642).setDelay(1.5));
                    game.dispatch(Event.builder().setLoc(600, 450).setDelay(1.5));
                    game.dispatch.enterText(encryptName(game.posTarget));
                    game.dispatch.delay(1);
                    game.dispatch(Event.builder().setLoc(365, 717).setDelay(1.5));
                    game.dispatch.delay(1.5);
                    return null;
                }));

        Event.builder(_map, "tel_confirm")
                .setDelay(1.5)
                .setLoc(335, 664);

        Event.builder(_map, "change_outpost")
                .setDelay(1.5)
                .setLoc(47, 1100);

        Event.builder(_map, "safe_teleport")
                .setDelay(1.5)
                .setLoc(514, 655, 514, 694, 200)
                .setListener(((event, game) -> {
                    if (game.log.btnName.contains("buttons_2:btn_2")) {
                        game.dispatch(Event.builder().setLoc(514, 655));
                        game.dispatch.staticDelay(1.25);
                        game.dispatch(Event.builder().setLoc(611, 1158));
                        game.dispatch.staticDelay(1.25);
                        if (game.log.btnName.contains("btn_buyUse")) {
                            game.dispatch(Event.builder().setDelay(1).setLoc(67, 322));
                        } else if (game.log.btnName.contains("bottom_panel:btn_right")) {
                            game.dispatch("top_left");
                        }
                        return null;
                    }
                    return event;
                }));

        Event.builder(_map, "teleport")
                .setDelay(1.5)
                .setLoc(459, 655)
                .setListener(((event, game) -> {
                    game.dispatch(_map.get("tel_confirm"));
                    game.dispatch(Event.builder().setDelay(1.5).setLoc(611, 1158));
                    if (game.log.btnName.contains("btn_buyUse")) {
                        game.dispatch(Event.builder().setDelay(1).setLoc(67, 322));
                    } else if (game.log.btnName.contains("bottom_panel:btn_right")) {
                        game.dispatch("top_left");
                    }
                    return null;
                }));

        Event.builder(_map, "tap_build")
                .setDelay(1.5)
                .setLoc(160, 635)
                .setListener((event, game) -> {
                    if (game.log.btnName.contains("btn_1")) {
                        game.dispatch("tel_confirm");
                        Event tapEvent = Event.builder().setDelay(1.5).setLoc(474, 1169);
                        game.dispatch(tapEvent);
                        game.dispatch(tapEvent);
                        return null;
                    }
                    game.dispatch(Event.builder().setLoc(349, 1008).setDelay(1.5));
                    return event;
                });

        Event.builder(_map, "build_init_outpost")
                .setDelay(1.5)
                .setListener((event, game) -> {
                    int[] tapLoc = new int[]{
                            427, 549,
                            226, 453,
                            416, 467,
                            248, 577
                    };
                    Event tapEvent = Event.builder().setDelay(2);
                    for (int i = 0; i < tapLoc.length; i += 2) {
                        game.dispatch(tapEvent.setLoc(tapLoc[i], tapLoc[i + 1]));
                        if (game.dispatch("tap_build")) {
                            return null;
                        }
                    }
                    return event;
                });


        Event.builder(_map, "world_set")
                .setListener(((event, game) -> {
                    game.log.world_set[0] = ((game.log.world[0] + game.log.world[2]) / 2);
                    game.log.world_set[1] = ((game.log.world[1] + game.log.world[3]) / 2);
                    System.out.println("Set: " + game.log.world_set[0] + "," + game.log.world_set[1]);
                    return null;
                }));

        Event.builder(_map, "adjust_map")
                .setDelay(1.5)
                .setListener((event, game) -> {

                    System.out.println("******Start adjust map zoom");
                    game.dispatch.mapzoom();

                    Event testClick = Event.builder().setLoc(73, 104).setDelay(1.5);
                    int redo = 10;
                    do {
                        game.dispatch(testClick);
                        if (!game.log.btnName.contains("scene_tiles")) {
                            game.dispatch.mapzoomin();
                        } else {
                            break;
                        }
                    } while (--redo > 0);

                    if (redo <= 0) {
                        throw new GameException("Stuck at adjust map");
                    }

                    System.out.println("***** Adjust zoom ended");
                    return null;
                });


        Event.builder(_map, "quickSelect")
                .setLoc(214, 1175)
                .setDelay(1);

        Event.builder(_map, "attack_monster")
                .setDelay(2)
                .setListener(((event, game) -> {
                    int redo = 0;
                    game.dispatch(Event.builder().setLoc(332, 661).setDelay(2));
                    Logger.log("Current Idle: " + game.log.idleTroops);
                    Logger.log("Current Troops: " + game.log.currTroops);
                    if (game.log.idleTroops == game.log.currTroops)
                        game.dispatch("quickSelect");
                    while (game.log.idleTroops > 0 && game.log.currTroops <= 0 && redo++ < 10) {
                        game.dispatch("quickSelect");
                    }

                    game.dispatch(Event.builder().setLoc(520, 1198).setDelay(1));
                    if (!game.log.btnName.contains("btn_go")) {
                        game.dispatch("top_left");
                    }
                    return null;
                }));

        Event.builder(_map, "attack_monster_test")
                .setLoc(332, 661, 332, 693, 200)
                .setListener(((event, game) -> {
                    if (game.log.btnName.contains("btn_confirm")) {
                        game.dispatch(Event.builder().setLoc(75, 335).setDelay(1));
                    } else if (game.log.btnName.contains("list")) {
                        game.dispatch("top_left");
                    } else if (game.log.btnName.equalsIgnoreCase("buttons_1:btn_0")
                            || game.log.btnName.equalsIgnoreCase("buttons_1:btn_1")) {
                        return null;
                    }
                    return event;
                }));

        Event.builder(_map, "attackTemple")
                .setListener(((event, game) -> {
                    game.dispatch.staticDelay(1);
                    for (int redo =0 ; redo < 5; redo ++) {
                        if(game.log.currTroops > 0){
                            game.dispatch("quickSelect");
                        }else{
                            break;
                        }
                    }
                    game.dispatch(Event.builder().setLoc(570, 970, 680, 970, 300).setDelay(1));
                    game.dispatch(Event.builder().setLoc(570, 844, 680, 844, 300).setDelay(1));

                    int sendWarriorsCount = game.account.getTroops() - game.log.currTroops - 30000;

                    Logger.log(game.account.getTroops()+" / " + game.log.currTroops+", Send "+sendWarriorsCount+" warriors");
                    if (sendWarriorsCount > 0) {
                        game.dispatch(Event.builder().setLoc(566, 670).setDelay(1));
                        game.dispatch.enterText(String.valueOf(sendWarriorsCount));
                        game.dispatch(Event.builder().setLoc(521, 1200));
                    }
                    game.dispatch.delay(1);
                    game.dispatch(Event.builder().setLoc(521, 1200));
                    game.dispatch.delay(1);
                    int leftTroops = game.account.getTroops() - game.log.currTroops;
                    Logger.log("Left Troops: "+leftTroops);
                    game.account.setTroops(leftTroops);
                    game.updateAccount();

                    return null;
                }));

        Event.builder(_map, "feedTemple")
                .setDelay(1)
                .setLoc(659, 321)
                .setListener(((event, game) -> {
                    game.log.currTroops = 0;
                    game.dispatch.delay(1.25);
                    game.dispatch(Event.builder().setLoc(367, 1180).setDelay(1.25));
                    game.dispatch(Event.builder().setLoc(600, 200));


                    for(int i=0;i<3;i++){
                        game.dispatch.staticDelay(2.5);
                        game.dispatch(Event.builder().setLoc(500, 620).setDelay(1.25));
                        if(game.log.btnName.contains("buttons_5:btn_5")){
                            break;
                        }
                    }


                    game.dispatch("attackTemple");


                    return null;
                }));

        Event.builder(_map, "auto_repair")
                .setDelay(1)
                .setLoc(342, 500)
                .setDelay(1.25)
                .setListener((event, game) -> {
                    game.dispatch.delay(1);
                    game.dispatch(Event.builder().setLoc(237,645).setDelay(1.25));
                    game.dispatch(Event.builder().setLoc(363,456).setDelay(1.25));
                    if(game.log.btnName.contains("extinguish")){
                        game.dispatch(Event.builder().setLoc(363,456).setDelay(1.25));
                    }

                    if(game.log.btnName.contains("repair")){
                        game.dispatch(Event.builder().setLoc(377, 722).setDelay(1.25));
                    }

                    game.dispatch(Event.builder().setLoc(68, 291).setDelay(1.25));

                    game.dispatch("top_left");
                    return null;
                });

    }
}
