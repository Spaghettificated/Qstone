package net.eli.elimod.utils;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

public enum OptDirection implements StringIdentifiable {
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH),
    EAST(Direction.EAST),
    WEST(Direction.WEST),
    UP(Direction.UP),
    DOWN(Direction.DOWN),
    NONE(null);

    private final Direction direction;

    OptDirection(@Nullable Direction direction) {
        this.direction = direction;
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    public boolean isNone() {
        return this == NONE;
    }
    public boolean isSome() {
        return this != NONE;
    }

    public Optional<Direction> asOptional(){
        return isNone() ? Optional.empty() : Optional.of(getDirection());
    }
    public static OptDirection fromOptional(Optional<Direction> dir){
        return dir.isEmpty() ? from(null) : from(dir.get());
    }

    public static OptDirection from(@Nullable Direction direction) {
        if (direction == null) return NONE;
        for (OptDirection opt : values()) {
            if (opt.direction == direction) return opt;
        }
        return NONE;
    }

    @Override
    public String asString() {
        return this == NONE ? "none" : direction.asString();
    }
}