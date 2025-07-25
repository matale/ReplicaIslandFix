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

package com.replica.replicaisland.objects;

import com.replica.replicaisland.debug.DebugSystem;
import com.replica.replicaisland.drawable.DrawableFactory;
import com.replica.replicaisland.game_objects.GameObjectCollisionSystem;
import com.replica.replicaisland.game_objects.GameObjectFactory;
import com.replica.replicaisland.game_objects.GameObjectManager;
import com.replica.replicaisland.input.InputGameInterface;
import com.replica.replicaisland.misc.BaseObject;
import com.replica.replicaisland.misc.BufferLibrary;
import com.replica.replicaisland.misc.ContextParameters;
import com.replica.replicaisland.misc.EventRecorder;
import com.replica.replicaisland.misc.HitPointPool;
import com.replica.replicaisland.misc.HudSystem;
import com.replica.replicaisland.misc.LevelBuilder;
import com.replica.replicaisland.misc.TextureLibrary;
import com.replica.replicaisland.misc.TimeSystem;
import com.replica.replicaisland.misc.VectorPool;
import com.replica.replicaisland.systems.CameraSystem;
import com.replica.replicaisland.systems.ChannelSystem;
import com.replica.replicaisland.systems.CollisionSystem;
import com.replica.replicaisland.systems.CustomToastSystem;
import com.replica.replicaisland.systems.HotSpotSystem;
import com.replica.replicaisland.systems.InputSystem;
import com.replica.replicaisland.systems.LevelSystem;
import com.replica.replicaisland.systems.OpenGLSystem;
import com.replica.replicaisland.systems.RenderSystem;
import com.replica.replicaisland.systems.SoundSystem;
import com.replica.replicaisland.systems.VibrationSystem;

import java.util.ArrayList;

/**
 * The object registry manages a collection of global singleton objects. However,
 * it differs from the standard singleton pattern in a few important ways:
 * - The objects managed by the registry have an undefined lifetime.  They may
 * become invalid at any time and they may not be valid at the beginning of the program.
 * - The only object that is always guaranteed to be valid is the ObjectRegistry itself.
 * - There may be more than one ObjectRegistry, and there may be more than one instance
 * of any of the systems managed by ObjectRegistry allocated at once.  For example,
 * separate threads may maintain their own separate ObjectRegistry instances.
 */
public class ObjectRegistry extends BaseObject {

    private final ArrayList<BaseObject> mItemsNeedingReset = new ArrayList<BaseObject>();
    public BufferLibrary bufferLibrary;
    public CameraSystem cameraSystem;
    public ChannelSystem channelSystem;
    public CollisionSystem collisionSystem;
    public ContextParameters contextParameters;
    public CustomToastSystem customToastSystem;
    public DebugSystem debugSystem;
    public DrawableFactory drawableFactory;
    public EventRecorder eventRecorder;
    public GameObjectCollisionSystem gameObjectCollisionSystem;
    public GameObjectFactory gameObjectFactory;
    public GameObjectManager gameObjectManager;
    public HitPointPool hitPointPool;
    public HotSpotSystem hotSpotSystem;
    public HudSystem hudSystem;
    public InputGameInterface inputGameInterface;
    public InputSystem inputSystem;
    public LevelBuilder levelBuilder;
    public LevelSystem levelSystem;
    public OpenGLSystem openGLSystem;
    public SoundSystem soundSystem;
    public TextureLibrary shortTermTextureLibrary;
    public TextureLibrary longTermTextureLibrary;
    public TimeSystem timeSystem;
    public RenderSystem renderSystem;
    public VectorPool vectorPool;
    public VibrationSystem vibrationSystem;

    public ObjectRegistry() {
        super();
    }

    public void registerForReset(BaseObject object) {
        final boolean contained = mItemsNeedingReset.contains(object);
        assert !contained;
        if (!contained) {
            mItemsNeedingReset.add(object);
        }
    }

    @Override
    public void reset() {
        final int count = mItemsNeedingReset.size();
        for (int x = 0; x < count; x++) {
            mItemsNeedingReset.get(x).reset();
        }
    }
}
