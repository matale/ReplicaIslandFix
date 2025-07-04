/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.replica.replicaisland.drawable;

import com.replica.replicaisland.misc.AllocationGuard;
import com.replica.replicaisland.misc.Texture;
import com.replica.replicaisland.misc.Vector2;
import com.replica.replicaisland.objects.ObjectPool;

/**
 * DrawableObject is the base object interface for objects that can be
 * rendered to the screen. Note that objects derived from DrawableObject
 * are passed between threads, and that care must be taken when modifying
 * drawable parameters to avoid side-effects (for example, the DrawableFactory
 * class can be used to generate fire-and-forget drawables).
 */
public abstract class DrawableObject extends AllocationGuard {
    private float mPriority;
    private ObjectPool mParentPool;

    public DrawableObject() {
        super();
    }

    public abstract void draw(float x, float y, float scaleX, float scaleY);

    public float getPriority() {
        return mPriority;
    }

    public ObjectPool getParentPool() {
        return mParentPool;
    }

    public void setParentPool(ObjectPool pool) {
        mParentPool = pool;
    }

    // Override to allow drawables to be sorted by texture.
    public Texture getTexture() {
        return null;
    }

    // Function to allow drawables to specify culling rules.
    public boolean visibleAtPosition(Vector2 position) {
        return true;
    }
}
