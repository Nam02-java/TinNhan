package com.example.Telegram.service.console;

public class NetworkState {
    private boolean simulateNetworkLoss = false;

    public boolean isSimulateNetworkLoss() {
        return simulateNetworkLoss;
    }

    public void setSimulateNetworkLoss(boolean simulateNetworkLoss) {
        this.simulateNetworkLoss = simulateNetworkLoss;
    }
}
