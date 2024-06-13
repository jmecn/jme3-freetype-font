package io.github.jmecn.math;

import com.jme3.math.Vector3f;

/**
 *
 */
public interface CanTransformVec3d {

    Vector3f transform(Vector3f point, Vector3f pointOut);

}
