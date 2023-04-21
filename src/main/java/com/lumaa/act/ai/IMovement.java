package com.lumaa.act.ai;

public interface IMovement {
    double forward();

    default double backward() {
        return -forward();
    }

    double right();

    default double left() {
        return -right();
    }

    void execute();
}
