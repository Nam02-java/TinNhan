package com.example.Telegram.service.console;

import com.example.Telegram.controller.client.IOServerHandle;

import java.util.Timer;
import java.util.TimerTask;

public class SimulateNetworkLossCommand implements Command {
    private NetworkState networkState;

    public SimulateNetworkLossCommand(NetworkState networkState) {
        this.networkState = networkState;
    }

    @Override
    public void execute() {
        networkState.setSimulateNetworkLoss(true);
        System.out.println("Internet connection loss simulation starts working");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (networkState.isSimulateNetworkLoss()) {
                    System.out.println("Lost internet beyond waiting time, exiting...");
                    System.exit(0);
                }
            }
        }, 50000);
    }
}