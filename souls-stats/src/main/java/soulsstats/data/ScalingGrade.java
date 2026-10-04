package soulsstats.data;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.util.StringRepresentable;

/** Letra de escalado de un arma por stat, como en Dark Souls; los cortes vienen de scaling_grades (config.json). */
public enum ScalingGrade implements StringRepresentable {
	S, A, B, C, D, E;

	static final Codec<ScalingGrade> CODEC = StringRepresentable.fromEnum(ScalingGrade::values);

	/** La mejor letra cuyo corte alcanza el escalado; E si ninguno. */
	public static ScalingGrade of(float scaling, Map<ScalingGrade, Float> cuts) {
		return Arrays.stream(values()).filter(grade -> scaling >= cuts.getOrDefault(grade, Float.MAX_VALUE)).findFirst().orElse(E);
	}

	@Override
	public String getSerializedName() {
		return name();
	}
}
