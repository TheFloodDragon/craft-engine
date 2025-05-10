package net.momirealms.craftengine.core.world.chunk;

import net.momirealms.craftengine.core.world.SectionPos;

public interface InjectedHolder {

    boolean isActive();

    void setActive(boolean active);

    CESection ceSection();

    void ceSection(CESection section);

    CEChunk ceChunk();

    void ceChunk(CEChunk chunk);

    SectionPos cePos();

    void cePos(SectionPos pos);

    interface Section extends InjectedHolder {
    }

    interface Palette extends InjectedHolder {

        Object target();

        void setTarget(Object target);
    }
}
