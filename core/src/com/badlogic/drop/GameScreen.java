package com.badlogic.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
    final Drop game;
    private final Texture dropImage;
    private final Texture bucketImage;
    private final Sound dropSound;
    private final Music rainMusic;
    private final OrthographicCamera camera;
    private final Rectangle bucket;
    private final Array<Rectangle> raindrops;
    private long lastDropTime;

    public GameScreen(final Drop game) {
        this.game = game;

        // load the images for the droplet and the bucket, 64x64 pixels each
        this.dropImage = new Texture(Gdx.files.internal("droplet.png"));
        this.bucketImage = new Texture(Gdx.files.internal("bucket.png"));

        // load the drop sound effect and the rain background "music"
        this.dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        this.rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // start the playback of the background music immediately
        this.rainMusic.setLooping(true);

        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, 800, 480);

        this.bucket = new Rectangle();
        this.bucket.x = 800 / 2.0f - 64 / 2.0f;
        this.bucket.y = 20;
        this.bucket.width = 64;
        this.bucket.height = 64;

        this.raindrops = new Array<>();
        this.spawnRaindrop();
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800-64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;

        this.raindrops.add(raindrop);
        this.lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void show() {
        this.rainMusic.play();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        this.camera.update();

        this.game.batch.setProjectionMatrix(this.camera.combined);
        this.game.batch.begin();
        this.game.batch.draw(this.bucketImage, this.bucket.x, this.bucket.y);
        for(Rectangle raindrop: this.raindrops) {
            this.game.batch.draw(this.dropImage, raindrop.x, raindrop.y);
        }
        this.game.batch.end();

        if(Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            this.camera.unproject(touchPos);
            this.bucket.x = touchPos.x - 64 / 2.0f;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            this.bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        }

        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            this.bucket.x += 200 * Gdx.graphics.getDeltaTime();
        }

        if(this.bucket.x < 0) {
            this.bucket.x = 0;
        }

        if(this.bucket.x > 800 - 64) {
            this.bucket.x = 800 - 64;
        }

        if(TimeUtils.nanoTime() - this.lastDropTime > 1000000000) {
            spawnRaindrop();
        }

        for (Iterator<Rectangle> iter = this.raindrops.iterator(); iter.hasNext(); ) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if(raindrop.y + 64 < 0) {
                iter.remove();
            } else if(raindrop.overlaps(this.bucket)) {
                this.dropSound.play();
                iter.remove();
            }
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }
}
