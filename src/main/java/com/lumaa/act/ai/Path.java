package com.lumaa.act.ai;

import com.lumaa.act.ActMod;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Path {
    private Pathfinder pathfinder;
    private BlockPos origin;
    private BlockPos destination;
    private int iterations = 0;
    private boolean stopped = false;

    private BlockPos lastStep;
    private BlockPos currentStep;
    private ArrayList<BlockPos> steps = new ArrayList<>();

    private PathDirection lastDir;
    private PathDirection currentDir;

    private final int maxIterations = 500;

    public Path(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
        this.origin = pathfinder.origin;
        this.destination = pathfinder.destination;
        this.lastStep = pathfinder.origin;
        this.currentStep = pathfinder.origin;
        this.lastDir = null;
    }

    public void tick() {
        if (!stopped) this.nextStep();
    }

    private void nextStep() {
        this.lastStep = this.currentStep;

        if (this.lastStep.equals(this.destination)) {
            if (!this.stopped) {
                this.stopPath();
                ActMod.print("STOPPED PATH");
                return;
            }
        }

        if (this.lastStep.getY() != destination.getY()) {
            BlockPos step = this.lastStep.add(0, destination.getY() > origin.getY() ? 1 : -1, 0);
            if (walkable(step)) {
                this.currentStep = step;
                this.steps.add(step);
            }
        } else {
            this.lastDir = this.currentDir;
            this.currentDir = findDirection();

            int x = 0;
            int z = 0;
            switch (this.currentDir) {
                case NORTH -> x = 1;
                case SOUTH -> x = -1;
                case EAST -> z = 1;
                case WEST -> z = -1;
            }

            BlockPos step = this.lastStep.add(x, 0, z);
            if (walkable(step)) {
                this.currentStep = step;
                this.steps.add(step);
            }
        }
    }

    private PathDirection findDirection() {
        int x = destination.getX() - this.lastStep.getX();
        int z = destination.getZ() - this.lastStep.getZ();

        if (x == 0) {
            return z > 0 ? PathDirection.EAST : PathDirection.WEST;
        } else if (z == 0) {
            return x > 0 ? PathDirection.NORTH : PathDirection.SOUTH;
        } else {
            if (x > z) {
                return x > 0 ? PathDirection.NORTH : PathDirection.SOUTH;
            } else {
                return z > 0 ? PathDirection.EAST : PathDirection.WEST;
            }
        }
    }

    private boolean walkable(BlockPos pos) {
        BlockState block = this.getPathfinder().world.getBlockState(pos);
        BlockState block2 = this.getPathfinder().world.getBlockState(pos.add(0, 1, 0));
        return block.isAir() || block2.isAir();
    }

    private void stopPath() {
        this.iterations = this.steps.size();
        if (this.iterations < this.maxIterations) {
            this.getPathfinder().path = this;
            this.stopped = true;
        } else {
            ActMod.print("Too many iterations");
        }
    }

    public int getIterations() {
        return iterations;
    }

    public Pathfinder getPathfinder() {
        return pathfinder;
    }

    public List<BlockPos> getSteps() {
        return steps.stream().toList();
    }

    public boolean isStopped() {
        return stopped;
    }

    public enum PathDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    /*private PathDirection findRandomDirection(@Nullable PathDirection lastDir) {
        if (this.getPathfinder().paths.size() < 1) {
            PathDirection[] l = PathDirection.values();
            PathDirection i =  l[new Random().nextInt(l.length)];
            if (this.startDir == null) this.startDir = i;
            return i;
        } else {
            ArrayList<PathDirection> unusedDirections = new ArrayList<>();
            unusedDirections.add(PathDirection.NORTH);
            unusedDirections.add(PathDirection.SOUTH);
            unusedDirections.add(PathDirection.EAST);
            unusedDirections.add(PathDirection.WEST);

            if (lastDir != null) unusedDirections.remove(lastDir);

            this.getPathfinder().paths.forEach(path -> {
                if (unusedDirections.contains(path.startDir)) {
                    unusedDirections.remove(path.startDir);
                } else {
                    ActMod.print("Anomaly paths");
                }
            });

            if (unusedDirections.size() > 0) {
                PathDirection l = unusedDirections.get(unusedDirections.size());
                if (this.startDir == null) this.startDir = l;
                return l;
            } else {
                this.stopPath();
                return null;
            }
        }
    }*/
}
