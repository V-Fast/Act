package com.lumaa.act.ai;

import com.lumaa.act.entity.ActorEntity;

public class ActorMovement implements IMovement {
    protected ActorEntity actor;
    private static final double runSpeed = 0.3;
    private static final double walkSpeed = 0.2;
    private static final double slowSpeed = 0.1;

    @Override
    public double forward() {
        return 0;
    }

    @Override
    public double right() {
        return 0;
    }

    @Override
    public void execute() {
    }
}
