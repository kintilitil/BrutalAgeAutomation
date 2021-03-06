package game;

import com.android.ddmlib.AdbCommandRejectedException;
import com.github.cliftonlabs.json_simple.JsonObject;
import dispatcher.EventDispatcher;
import events.LogProcess;
import events.common.Event;
import events.handler.*;
import store.Account;
import store.AccountUpdateListener;
import store.Store;
import util.Logger;
import util.MyDebugger;

import java.net.ConnectException;
import java.time.Duration;
import java.time.LocalDateTime;


public class GameInstance {

    public LocalDateTime lastRound = null;
    public LogProcess log;
    public StatusIndicator status;
    public EventDispatcher dispatch;
    public Store store;
    public JsonObject posTarget;
    public AccountUpdateListener updateListener;
    public Account account;
    public int restarting = 0;
    public boolean debug;

    public GameInstance(Store store, boolean debug) {
        this.debug = debug;
        this.store = store;
        this.dispatch = new EventDispatcher(this);
    }

    public void start() {
        this.status = new StatusIndicator();
        this.log = new LogProcess(this);

        new Thread(() -> {
            if(debug){
                this.account = store.getAccountGroup().getNextAccount();
                new MyDebugger(this);
            }
            else{
                startEvent(GameStatus.initiate);
            }
            log.startLog();
        }).start();

    }

    public void setAccountUpdateListener(AccountUpdateListener updateListener){
        this.updateListener = updateListener;
    }

    public void startEvent(GameStatus nStatus) {
        startEvent(nStatus, "");
    }

    public void startEvent(GameStatus nStatus, String finishStatus) {
        status.set(nStatus);
        dispatch.resetExecuteTime();
        Logger.log("******************  Perform: " + status.get()+"  ********************");

        new Thread(()-> {
            try {
                dispatch.delay(1);

                if(status.get() == GameStatus.initiate){
                    if (lastRound != null) {
                        //  store.getAccountGroup().updateCompletedBuildingQueue();
                        Logger.log("**End at " + LocalDateTime.now().toString());
                        Logger.recordRound(account,Duration.between(lastRound, LocalDateTime.now()).toMinutes()+" minutes ");
                    }
                    lastRound = LocalDateTime.now();
                    Logger.log("**Start at " + LocalDateTime.now().toString());
                    Initiate.fire(this);
                }

                if(posTarget != null){
                    startPosModeEvent(finishStatus);
                }else{
                    switch (status.get()) {
                        case starting:
                            Starting.fire(this);
                            break;
                        case change_server:
                            ChangeServer.fire(this);
                            break;
                        case when_start:
                            WhenStart.fire(this);
                            break;
                        case city_work:
                            CityWork.fire(this);
                            break;
                        case world_map:
                            WorldMap.fire(this);
                            break;
                        default:
                            break;
                    }
                }
            }
            catch(AdbCommandRejectedException | ConnectException e){
                if(restarting == 0 || restarting == 2) {
                    restarting += 1;
                    Logger.log("Emulator Stopped!! Attempt to restart..");
                    if (store.restartEmulator()) {
                        Logger.log("Restart success");
                        startEvent(GameStatus.initiate);
                    } else {
                        Logger.log("Restart failed");
                    }
                    restarting += 1;
                }

            }
            catch(Exception e){
                if(store.isClose){
                    Logger.log("Closed");
                }else{

                    if(store.isForceStop){
                        Logger.log("Force stopped, start new round!");
                        store.setForceStop(false);
                    }
                    else{
                        GameException.fire(this, e);
                    }

                    startEvent(GameStatus.initiate);
                }
            }
        }).start();
    }

    public void startAccountID(String accID) throws Exception {
        dispatch.stopGame();
        dispatch.changeAccount(accID, true);
        dispatch.startGame();
    }

    public void startPosModeEvent(String finishStatus) throws Exception {
        if(posTarget != null && !finishStatus.equalsIgnoreCase("") && !posTarget.containsKey("temp")) {
            posTarget.put("status", finishStatus);
            store.sendDataBack("update", posTarget);
        }
        updateListener.onUpdate(null);
        switch (status.get()) {
            case starting:
                Starting.fire(this);
                break;
            case change_server:
                ChangeServer.fire(this);
                break;
            case when_start:
                WhenStart.firePosMode(this);
                break;
            case city_work:
                CityWork.firePosMode(this);
                break;
            case world_map:
                WorldMap.firePosMode(this);
                break;
            default:
                break;
        }
    }

    public void updateAccount(){
        updateListener.onUpdate(account);
        store.updateAccount(account);
    }

    public boolean dispatch(String name) throws Exception {
        return dispatch.sendEvent(name);
    }
    public boolean dispatch(Event event) throws Exception {
        return dispatch.sendEvent(event);
    }

}
