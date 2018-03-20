/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
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
package com.ray3k.superbug.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import com.ray3k.superbug.Core;
import com.ray3k.superbug.Entity;
import com.ray3k.superbug.EntityManager;
import com.ray3k.superbug.InputManager;
import com.ray3k.superbug.State;
import com.ray3k.superbug.entities.DishEntity;
import com.ray3k.superbug.entities.GameOverTimerEntity;
import com.ray3k.superbug.entities.GermEntity;

public class GameState extends State {
    private static GameState instance;
    private int score;
    private static int highscore = 0;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    public static EntityManager entityManager;
    public static TextureAtlas spineAtlas;
    public static TwoColorPolygonBatch twoColorPolygonBatch;
    public static DishEntity dish;
    private float time;
    
    public static GameState inst() {
        return instance;
    }
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        instance = this;
        
        spineAtlas = Core.assetManager.get(Core.DATA_PATH + "/spine/superbug.atlas", TextureAtlas.class);
        
        score = 0;
        
        inputManager = new InputManager();
        
        gameCamera = new OrthographicCamera();
        gameCamera.position.set(0.0f, 0.0f, 0.0f);
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        gameViewport.apply();
        
        skin = Core.assetManager.get(Core.DATA_PATH + "/ui/superbug-ui.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        
        entityManager = new EntityManager();
        
        twoColorPolygonBatch = new TwoColorPolygonBatch(3100);
        
        createStageElements();
        
        dish = new DishEntity();
        dish.setPosition(-150.0f, 0.0f);
        entityManager.addEntity(dish);
        
        GermEntity germ = new GermEntity();
        entityManager.addEntity(germ);
        
        time = 0.0f;
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Table table = new Table();
        root.add(table).expandX().right().spaceRight(30.0f);
        
        Label label = new Label("POPULATION", skin, "game-small");
        table.add(label).left();
        
        table.row();
        label = new Label("2000", skin, "game-big");
        label.setName("populationLabel");
        table.add(label).right().padLeft(25.0f).width(100.0f);
        
        table = new Table();
        root.add(table).expandX().left();
        
        label = new Label("RESISTANCE", skin, "game-small");
        table.add(label).left();
        
        table.row();
        label = new Label("99%", skin, "game-big");
        label.setName("resistanceLabel");
        table.add(label).right().padLeft(25.0f);
        
        root.row();
        table = new Table();
        root.add(table).colspan(2).right().expandY();
        
        TextButton textButton = new TextButton("New Sample", skin);
        textButton.setName("newSampleButton");
        textButton.setDisabled(true);
        table.add(textButton).right();
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                playSound("confirm", .5f);
                
                GermEntity germ = new GermEntity();
                entityManager.addEntity(germ);
            }
        });
        
        textButton = new TextButton("Add Agar", skin);
        table.add(textButton).left();
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                playSound("squirt", .5f);
                GermEntity.food = 1.0f;
                
                dish.getAnimationState().setAnimation(1, "agar", false);
            }
        });
        
        table.row();
        label = new Label("Temperature:", skin, "game");
        table.add(label);
        
        Slider slider = new Slider(0, 100, 1, false, skin);
        slider.setName("temperatureSlider");
        slider.setUserObject("netural");
        slider.setValue(50.0f);
        table.add(slider);
        
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Slider slider = (Slider) actor;
                
                GermEntity.temperature = slider.getValue() / 100;
                
                if (slider.getUserObject().equals("neutral")) {
                    if (slider.getValue() > 75f) {
                        playSound("flame", .5f);
                        slider.setUserObject("hot");
                        dish.getAnimationState().setAnimation(2, "hot", false);
                    } else if (slider.getValue() < 25f) {
                        playSound("fan", .5f);
                        slider.setUserObject("cold");
                        dish.getAnimationState().setAnimation(2, "cold", false);
                    }
                } else {
                    if (slider.getValue() > 40f && slider.getValue() < 60f) {
                        slider.setUserObject("neutral");
                        dish.getAnimationState().setAnimation(2, "normal", false);
                    }
                }
            }
        });
        
        table.row();
        label = new Label("Radiation:", skin, "game");
        table.add(label);
        
        slider = new Slider(0, 100, 1, false, skin);
        slider.setName("radiationSlider");
        slider.setUserObject("netural");
        slider.setValue(0.0f);
        table.add(slider);
        
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Slider slider = (Slider) actor;
                if (slider.getUserObject().equals("neutral")) {
                    if (slider.getValue() > 75f) {
                        playSound("whir", .5f);
                        slider.setUserObject("hot");
                    }
                } else {
                    if (slider.getValue() < 60f) {
                        slider.setUserObject("neutral");
                    }
                }
            }
        });
        
        table.row();
        textButton = new TextButton("Penicillin", skin);
        table.add(textButton).right();
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                playSound("drip", .5f);
                
                dish.getAnimationState().setAnimation(1, "medicine", false);
                
                for (Entity entity : entityManager.getEntities()) {
                    if (entity instanceof GermEntity) {
                        GermEntity germ = (GermEntity) entity;
                        
                        if (!germ.immunities.contains(GermEntity.Immunity.PENICILLIN, false)) {
                            if (MathUtils.randomBoolean(.75f)) germ.dispose();
                        } else {
                            if (MathUtils.randomBoolean(.05f)) germ.dispose();
                        }
                    }
                }
            }
        });
        
        textButton = new TextButton("Cephalexin", skin);
        table.add(textButton).left();
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                playSound("drip", .5f);
                
                dish.getAnimationState().setAnimation(1, "medicine", false);
                
                for (Entity entity : entityManager.getEntities()) {
                    if (entity instanceof GermEntity) {
                        GermEntity germ = (GermEntity) entity;
                        
                        if (!germ.immunities.contains(GermEntity.Immunity.CEPHALEXIN, false)) {
                            if (MathUtils.randomBoolean(.75f)) germ.dispose();
                        } else {
                            if (MathUtils.randomBoolean(.05f)) germ.dispose();
                        }
                    }
                }
            }
        });
        
        textButton = new TextButton("Tetracycline", skin);
        table.add(textButton).left();
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                playSound("drip", .5f);
                
                dish.getAnimationState().setAnimation(1, "medicine", false);
                
                for (Entity entity : entityManager.getEntities()) {
                    if (entity instanceof GermEntity) {
                        GermEntity germ = (GermEntity) entity;
                        
                        if (!germ.immunities.contains(GermEntity.Immunity.TETRACYCLINE, false)) {
                            if (MathUtils.randomBoolean(.75f)) germ.dispose();
                        } else {
                            if (MathUtils.randomBoolean(.05f)) germ.dispose();
                        }
                    }
                }
            }
        });
        
        table.row();
        textButton = new TextButton("Peroxide", skin);
        table.add(textButton).right();
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                playSound("spray", .5f);
                
                dish.getAnimationState().setAnimation(1, "peroxide", false);
                
                for (Entity entity : entityManager.getEntities()) {
                    if (entity instanceof GermEntity) {
                        GermEntity germ = (GermEntity) entity;
                        
                        if (!germ.immunities.contains(GermEntity.Immunity.PEROXIDE, false)) {
                            if (MathUtils.randomBoolean(.75f)) germ.dispose();
                        } else {
                            if (MathUtils.randomBoolean(.05f)) germ.dispose();
                        }
                    }
                }
            }
        });
        
        textButton = new TextButton("Alcohol", skin);
        table.add(textButton).left();
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                playSound("spray", .5f);
                
                dish.getAnimationState().setAnimation(1, "medicine", false);
                
                for (Entity entity : entityManager.getEntities()) {
                    if (entity instanceof GermEntity) {
                        GermEntity germ = (GermEntity) entity;
                        
                        if (!germ.immunities.contains(GermEntity.Immunity.ALCOHOL, false)) {
                            if (MathUtils.randomBoolean(.75f)) germ.dispose();
                        } else {
                            if (MathUtils.randomBoolean(.05f)) germ.dispose();
                        }
                    }
                }
            }
        });
        
        textButton = new TextButton("Bleach", skin);
        table.add(textButton).left();
        
        textButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                playSound("spray", .5f);
                
                dish.getAnimationState().setAnimation(1, "bleach", false);
                
                for (Entity entity : entityManager.getEntities()) {
                    if (entity instanceof GermEntity) {
                        GermEntity germ = (GermEntity) entity;
                        
                        if (!germ.immunities.contains(GermEntity.Immunity.BLEACH, false)) {
                            if (MathUtils.randomBoolean(.75f)) germ.dispose();
                        } else {
                            if (MathUtils.randomBoolean(.05f)) germ.dispose();
                        }
                    }
                }
            }
        });
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(255 / 255.0f, 255 / 255.0f, 255 / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        twoColorPolygonBatch.setProjectionMatrix(gameCamera.combined);
        twoColorPolygonBatch.begin();
        twoColorPolygonBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        entityManager.draw(spriteBatch, delta);
        twoColorPolygonBatch.end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        time += delta;
        
        Slider slider = stage.getRoot().findActor("radiationSlider");
        if (slider.getValue() > 75.0f) {
            for (Entity entity : entityManager.getEntities()) {
                if (entity instanceof GermEntity) {
                    GermEntity germ = (GermEntity) entity;
                    if (!germ.immunities.contains(GermEntity.Immunity.RADIATION, false)) {
                        if (MathUtils.randomBoolean(.01f)) {
                            germ.dispose();
                        }
                    }
                }
            }
        }
        
        slider = stage.getRoot().findActor("temperatureSlider");
        if (slider.getValue() > 75.0f) {
            for (Entity entity : entityManager.getEntities()) {
                if (entity instanceof GermEntity) {
                    GermEntity germ = (GermEntity) entity;
                    if (!germ.immunities.contains(GermEntity.Immunity.HOT, false)) {
                        if (MathUtils.randomBoolean(.01f)) {
                            germ.dispose();
                        }
                    }
                }
            }
        }
        
        if (slider.getValue() < 25.0f) {
            for (Entity entity : entityManager.getEntities()) {
                if (entity instanceof GermEntity) {
                    GermEntity germ = (GermEntity) entity;
                    if (!germ.immunities.contains(GermEntity.Immunity.COLD, false)) {
                        if (MathUtils.randomBoolean(.01f)) {
                            germ.dispose();
                        }
                    }
                }
            }
        }
        
        GermEntity.food -= .01f * delta;
        GermEntity.food = MathUtils.clamp(GermEntity.food, 0.0f, 1.0f);
        
        GermEntity.germCount = 0;
        int immunityCounter = 0;
        for (Entity entity : entityManager.getEntities()) {
            if (entity instanceof GermEntity) {
                GermEntity.germCount++;
                
                GermEntity germ = (GermEntity) entity;
                if (germ.immunities.size > immunityCounter) {
                    immunityCounter = germ.immunities.size;
                }
            }
        }
        
        if (immunityCounter == GermEntity.Immunity.values().length) {
            if (!dish.getAnimationState().getCurrent(0).getAnimation().getName().equals("cracked")) {
                dish.getAnimationState().setAnimation(0, "cracked", true);
                entityManager.addEntity(new GameOverTimerEntity(5.0f));
                ((GameOverState) Core.stateManager.getState("game-over")).setTime(time);
                playSound("crack");
            }
        }
        
        Label label = stage.getRoot().findActor("populationLabel");
        label.setText(Integer.toString(GermEntity.germCount));
        
        
        TextButton textButton = stage.getRoot().findActor("newSampleButton");
        textButton.setDisabled(GermEntity.germCount != 0);
        
        int resistance = (int) ((float) immunityCounter / GermEntity.Immunity.values().length * 100);
        
        label = stage.getRoot().findActor("resistanceLabel");
        label.setText(Integer.toString(resistance) + "%");
        
        entityManager.act(delta);
        
        stage.act(delta);
        
        if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            Core.stateManager.loadState("menu");
        }
    }

    @Override
    public void dispose() {
        if (twoColorPolygonBatch != null) {
            twoColorPolygonBatch.dispose();
        }
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, false);
        gameCamera.position.set(0.0f, 0.0f, 0.0f);
        stage.getViewport().update(width, height, true);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        if (score > highscore) {
            highscore = score;
        }
    }
    
    public void addScore(int score) {
        this.score += score;
        if (this.score > highscore) {
            highscore = this.score;
        }
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }

    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }
    
    public void playSound(String name) {
        playSound(name, 1.0f, 1.0f);
    }
    
    public void playSound (String name, float volume) {
        playSound(name, volume, 1.0f);
    }
    
    /**
     * 
     * @param name
     * @param volume
     * @param pitch .5 to 2. 1 is default
     */
    public void playSound(String name, float volume, float pitch) {
        Core.assetManager.get(Core.DATA_PATH + "/sfx/" + name + ".wav", Sound.class).play(volume, pitch, 0.0f);
    }
}