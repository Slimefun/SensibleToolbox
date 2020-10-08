package io.github.thebusybiscuit.sensibletoolbox.core.energy;

import javax.annotation.Nullable;

import io.github.thebusybiscuit.sensibletoolbox.blocks.machines.SCURelay;

/**
 * This is a simple data class that represents a connection between one
 * {@link SCURelay} and another.
 * 
 * @author desht
 * @author TheBusyBiscuit
 *
 */
public final class SCURelayConnection {

    private SCURelay block1;
    private SCURelay block2;
    private double chargeLevel;

    public void setChargeLevel(double charge) {
        this.chargeLevel = charge;
    }

    public double getChargeLevel() {
        return chargeLevel;
    }

    @Nullable
    public SCURelay getFirst() {
        return block1;
    }

    @Nullable
    public SCURelay getSecond() {
        return block2;
    }

    public void setFirstBlock(@Nullable SCURelay relay) {
        this.block1 = relay;
    }

    public void setSecondBlock(@Nullable SCURelay relay) {
        this.block2 = relay;
    }

}