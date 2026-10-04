package soulsstats.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

/**
 * Cuánto da un efecto según los puntos sobre la base de su stat: base + gain(puntos). El admin elige la función con
 * "type" en config.json. Con puntos negativos (una clase que baja una stat) la función se aplica en espejo.
 */
public interface Scaling {
	Codec<Scaling> CODEC = Type.CODEC.dispatch("type", Scaling::type, Type::codec);

	float base();

	/** Lo que suman p ≥ 0 puntos. */
	double gain(int points);

	Type type();

	default float at(int points) {
		return base() + (float) (Math.signum(points) * gain(Math.abs(points)));
	}

	enum Type implements StringRepresentable {
		LINEAR(Linear.CODEC), POWER(Power.CODEC), LOGARITHMIC(Logarithmic.CODEC), HYPERBOLIC(Hyperbolic.CODEC), SOFT_CAPS(SoftCaps.CODEC);

		static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
		private final MapCodec<? extends Scaling> codec;

		Type(MapCodec<? extends Scaling> codec) {
			this.codec = codec;
		}

		MapCodec<? extends Scaling> codec() { // adr-skip ADR-0007: un solo tipo acotado, lo exige Codec.dispatch
			return codec;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	/** per_point × p: cada punto da lo mismo. */
	record Linear(float base, float perPoint) implements Scaling {
		static final MapCodec<Linear> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.FLOAT.optionalFieldOf("base", 0f).forGetter(Linear::base),
				Codec.FLOAT.fieldOf("per_point").forGetter(Linear::perPoint)).apply(i, Linear::new));

		public double gain(int points) {
			return perPoint * points;
		}

		public Type type() {
			return Type.LINEAR;
		}
	}

	/** per_point × p^exponent: con exponente > 1 cada punto da más que el anterior (curvas de nivel); < 1, menos. */
	record Power(float base, float perPoint, float exponent) implements Scaling {
		static final MapCodec<Power> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.FLOAT.optionalFieldOf("base", 0f).forGetter(Power::base),
				Codec.FLOAT.fieldOf("per_point").forGetter(Power::perPoint),
				Codec.FLOAT.fieldOf("exponent").forGetter(Power::exponent)).apply(i, Power::new));

		public double gain(int points) {
			return perPoint * Math.pow(points, exponent);
		}

		public Type type() {
			return Type.POWER;
		}
	}

	/** scale × ln(1 + p): rinde menos con cada punto, sin tope. */
	record Logarithmic(float base, float scale) implements Scaling {
		static final MapCodec<Logarithmic> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.FLOAT.optionalFieldOf("base", 0f).forGetter(Logarithmic::base),
				Codec.FLOAT.fieldOf("scale").forGetter(Logarithmic::scale)).apply(i, Logarithmic::new));

		public double gain(int points) {
			return scale * Math.log1p(points);
		}

		public Type type() {
			return Type.LOGARITHMIC;
		}
	}

	/** max × p / (p + half): llega a la mitad de max con half puntos y nunca lo alcanza (como la armadura de LoL). */
	record Hyperbolic(float base, float max, float half) implements Scaling {
		static final MapCodec<Hyperbolic> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.FLOAT.optionalFieldOf("base", 0f).forGetter(Hyperbolic::base),
				Codec.FLOAT.fieldOf("max").forGetter(Hyperbolic::max),
				Codec.FLOAT.fieldOf("half").forGetter(Hyperbolic::half)).apply(i, Hyperbolic::new));

		public double gain(int points) {
			return max * points / (points + half);
		}

		public Type type() {
			return Type.HYPERBOLIC;
		}
	}

	/**
	 * Tramos lineales con su propio per_point hasta until puntos, como los soft caps de Dark Souls; el último tramo
	 * (sin until) sigue para siempre.
	 */
	record SoftCaps(float base, List<Step> steps) implements Scaling {
		static final MapCodec<SoftCaps> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.FLOAT.optionalFieldOf("base", 0f).forGetter(SoftCaps::base),
				Step.CODEC.listOf().fieldOf("steps").forGetter(SoftCaps::steps)).apply(i, SoftCaps::new));

		public double gain(int points) {
			double total = 0;
			int from = 0;
			for (Step step : steps) {
				total += step.perPoint * Math.max(0, Math.min(points, step.until) - from);
				from = step.until;
			}
			return total;
		}

		public Type type() {
			return Type.SOFT_CAPS;
		}

		record Step(int until, float perPoint) {
			static final Codec<Step> CODEC = RecordCodecBuilder.create(i -> i.group(
					Codec.INT.optionalFieldOf("until", Integer.MAX_VALUE).forGetter(Step::until),
					Codec.FLOAT.fieldOf("per_point").forGetter(Step::perPoint)).apply(i, Step::new));
		}
	}
}
