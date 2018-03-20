/*
 * The MIT License
 *
 * Copyright 2018 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ray3k.superbug.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ray3k.superbug.Core;
import com.ray3k.superbug.Entity;
import com.ray3k.superbug.SpineTwoColorEntity;
import com.ray3k.superbug.states.GameState;

public class GermEntity extends SpineTwoColorEntity {
    private static final Vector2 temp = new Vector2();
    private static final Vector2 temp2 = new Vector2();
    private float faceAngle;
    private static final float PETRI_RADIUS = 160;
    private float directionTimer;
    private static final float DIRECTION_TIMER_TIME = 2.0f;
    private float splitTimer;
    private float splitTime = 5.0f;
    private static final float SPLIT_TIME_RANDOM = 2.0f;
    public static float food = 1f;
    public static float germLimit = 2000;
    public static int germCount = 0;
    public static float temperature = .5f;
    private float deathTimer;
    public float deathTime = 24.0f;
    public static final float DEATH_TIME_RANDOM = 10.0f;
    public static enum Immunity {
        PENICILLIN, CEPHALEXIN, TETRACYCLINE, PEROXIDE, ALCOHOL, BLEACH, HOT, COLD, RADIATION;
    }
    public Array<Immunity> immunities;

    public GermEntity() {
        super(Core.DATA_PATH + "/spine/bacteria.json", "animation", GameState.twoColorPolygonBatch);
        getAnimationState().getCurrent(0).setLoop(true);
        getSkeleton().setSkin("bacteria" + Integer.toString(MathUtils.random(1, 13)));
        getSkeleton().findBone("hip").setRotation(MathUtils.random(360.0f));
        faceAngle = MathUtils.random(360.0f);
        directionTimer = DIRECTION_TIMER_TIME;
        immunities = new Array<Immunity>();
        
        resetTimers();
    }
    
    public GermEntity(GermEntity other) {
        this();
        splitTime = other.splitTime;
        deathTime = other.deathTime;
        immunities.addAll(other.immunities);
        
        resetTimers();
    }
    
    public void resetTimers() {
        splitTimer = splitTime + MathUtils.random(SPLIT_TIME_RANDOM);
        deathTimer = deathTime + MathUtils.random(DEATH_TIME_RANDOM);
    }

    @Override
    public void actSub(float delta) {
        setMotion(20.0f * temperature, faceAngle);
        
        temp.set(getX(), getY());
        float distance = temp.dst(GameState.dish.getX(), GameState.dish.getY());
        
        if (distance > PETRI_RADIUS) {
            temp2.set(GameState.dish.getX(), GameState.dish.getY());
            
            float angle = temp.sub(temp2).angle();
            
            temp.set(PETRI_RADIUS, 0);
            temp.rotate(angle);
            temp.add(GameState.dish.getX(), GameState.dish.getY());
            
            setPosition(temp.x, temp.y);
            faceAngle = MathUtils.random(360.0f);
        }
        
        directionTimer -= delta;
        if (directionTimer <= 0) {
            directionTimer = DIRECTION_TIMER_TIME;
            faceAngle = MathUtils.random(360.0f);
        }
        
        splitTimer -= delta;
        if (splitTimer <= 0) {
            splitTimer = splitTime + MathUtils.random(SPLIT_TIME_RANDOM);
            if (MathUtils.randomBoolean((germLimit - germCount) / germLimit * food)) {
                GermEntity germ = new GermEntity(this);
                germ.setPosition(getX(), getY());
                GameState.entityManager.addEntity(germ);
                
                if (MathUtils.randomBoolean(.005f)) {
                    germ.getSkeleton().setSkin("bacteria" + Integer.toString(MathUtils.random(1, 13)));
                    
                    germ.generateNewImmunity();
                } else {
                    germ.getSkeleton().setSkin(getSkeleton().getSkin());
                }
                
            }
        }
        
        deathTimer -= delta;
        if (deathTimer <= 0) {
            dispose();
        }
    }
    
    public void generateNewImmunity() {
        Array<Immunity> list = new Array<Immunity>(Immunity.values());
        
        for (Immunity immunity : immunities) {
            list.removeValue(immunity, false);
        }
        
        if (list.size > 0) {
            immunities.add(list.random());
        }
    }

    @Override
    public void drawSub(SpriteBatch spriteBatch, float delta) {
    }

    @Override
    public void create() {
        
    }

    @Override
    public void actEnd(float delta) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }
}
