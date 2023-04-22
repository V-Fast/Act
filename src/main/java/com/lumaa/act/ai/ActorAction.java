package com.lumaa.act.ai;

import net.minecraft.entity.EntityPose;

public class ActorAction {
    public ActorAI actorAI;

    public ActorAction(ActorAI actorAI) {
        this.actorAI = actorAI;
    }

    public void changePose() {
        if (this.actorAI.movement.isSneaking())  {
            this.actorAI.actor.setPose(EntityPose.CROUCHING);
        } else if (this.actorAI.movement.isCrawling()) {
            this.actorAI.actor.setPose(EntityPose.SWIMMING);
        } else {
            this.actorAI.actor.setPose(EntityPose.STANDING);
        }
    }
}
