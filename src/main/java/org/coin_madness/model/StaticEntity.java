package org.coin_madness.model;

public class StaticEntity extends Entity {

    public StaticEntity(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof StaticEntity))
            return false;
        StaticEntity entity = (StaticEntity) obj;
        return x == entity.x && y == entity.y;
    }

}
