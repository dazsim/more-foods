package com.workshopcraft.morefood.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
//? if >=26.1 {
import net.minecraft.client.particle.SingleQuadParticle;
//?} else {
/*import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
*///?}
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

//? if >=26.1 {
public class SteamParticle extends SingleQuadParticle {
//?} else {
/*public class SteamParticle extends TextureSheetParticle {
*///?}
    private final SpriteSet sprites;
    private final float startSize;

    protected SteamParticle(ClientLevel level, double x, double y, double z, double ya, SpriteSet sprites) {
        //? if >=26.1 {
        super(level, x, y, z, sprites.first());
        //?} else {
        /*super(level, x, y, z);
        *///?}
        this.sprites = sprites;
        xd = (random.nextDouble() - 0.5) * 0.006;
        zd = (random.nextDouble() - 0.5) * 0.006;
        yd = ya;
        gravity = 0;
        friction = 1;
        hasPhysics = false;
        lifetime = 40 + random.nextInt(21);
        startSize = quadSize = 0.15F + random.nextFloat() * 0.1F;
        alpha = 0.6F;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) return;
        final float life = (float) age / lifetime;
        alpha = 0.6F * (1 - life);
        quadSize = startSize * (1 + 0.5F * life);
        setSpriteFromAge(sprites);
    }

    //? if >=26.1 {
    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }
    //?} else {
    /*@Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    *///?}

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        //? if >=26.1 {
        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
                                       double xa, double ya, double za, RandomSource random) {
        //?} else {
        /*@Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z,
                                       double xa, double ya, double za) {
        *///?}
            return new SteamParticle(level, x, y, z, ya, sprites);
        }
    }
}
