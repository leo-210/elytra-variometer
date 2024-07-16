package me.leo21.elytra_variometer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElytraVariometer implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("elytra-variometer");
	public static final String MOD_ID = "elytra-variometer";

	public static final float VOLUME = 1f;

	public static final float VERTICAL_SPEED_POSITIVE_THRESHOLD = -0.01f;
	public static final float VERTICAL_SPEED_NEGATIVE_THRESHOLD = -0.5f;

	// Per second
	public static final float MAX_BEEP_FREQUENCY = 12f;
	public static final float MIN_BEEP_FREQUENCY = 1.4f;

	public static final float MAX_BEEP_PITCH = 2f;
	public static final float MIN_BEEP_PITCH = 0.6f;

	// Meters per tick
	public static final float MAX_ELYTRA_UPWARD_SPEED = 1.7f;

	private float secondsCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Elytra Variometer");

		ClientTickEvents.START_WORLD_TICK.register(world -> {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;

			if (player == null) return;
			if (!player.isFallFlying()) return;

			float verticalSpeed = (float) player.getVelocity().getY();

			if (verticalSpeed < VERTICAL_SPEED_POSITIVE_THRESHOLD && verticalSpeed > VERTICAL_SPEED_NEGATIVE_THRESHOLD) return;

			// 1 tick = 0.05 seconds
			secondsCounter += 0.05f;

			if (verticalSpeed >= VERTICAL_SPEED_POSITIVE_THRESHOLD) {
				float frequency = reverseExp(
						MIN_BEEP_FREQUENCY,
						MAX_BEEP_FREQUENCY,
						2/MAX_ELYTRA_UPWARD_SPEED,
						verticalSpeed
				);

				if (secondsCounter >= 1/frequency) {
					secondsCounter = 0;

					float pitch = reverseExp(
							MIN_BEEP_PITCH,
							MAX_BEEP_PITCH + 0.5f,
							2/MAX_ELYTRA_UPWARD_SPEED,
							verticalSpeed
					);

					player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, VOLUME, pitch);
				}
			}
		});
	}

	private static float reverseExp(float min, float max, float tau, float x) {
		return (float) (-(max - min) * Math.exp(-tau * x) + max);
	}
}