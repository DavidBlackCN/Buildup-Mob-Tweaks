package com.davidblackcn.buildupmobtweaks.rebuild.skeleton;

/** Target-version access to the two original attack Goal instances and animation state. */
public interface SkeletonAccess {
    void buildup$removeOriginalAttacks();
    int buildup$attackInterval(boolean hard);
    int buildup$special();
    void buildup$special(int ticks);
}
