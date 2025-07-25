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
package com.replica.replicaisland.component;

import com.replica.replicaisland.misc.BaseObject;
import com.replica.replicaisland.systems.CameraSystem;
import com.replica.replicaisland.game_objects.GameObject;

public class CameraBiasComponent extends GameComponent {
    public CameraBiasComponent() {
        super();
        setPhase(GameComponent.ComponentPhases.THINK.ordinal());
    }

    @Override
    public void reset() {
    }

    @Override
    public void update(float timeDelta, BaseObject parent) {
        GameObject parentObject = (GameObject) parent;
        CameraSystem camera = sSystemRegistry.cameraSystem;
        if (camera != null) {
            camera.addCameraBias(parentObject.getPosition());
        }
    }
}
