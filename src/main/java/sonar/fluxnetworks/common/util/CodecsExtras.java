package sonar.fluxnetworks.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CodecsExtras {
    public static final Codec<Long> NON_NEGATIVE_LONG = longRangeWithMessage(0, Long.MAX_VALUE, l -> "Value must be non-negative: " + l);

    public static final Codec<Integer> NETWORK_ID = intRangeWithMessage(-1, Integer.MAX_VALUE, i -> "Invalid network ID: " + i);
    public static final Codec<Integer> INTEGER = intRangeWithMessage(Integer.MIN_VALUE, Integer.MAX_VALUE, i -> "Invalid integer value: " + i);

    private static Codec<Long> longRangeWithMessage(long min, long max, Function<Long, String> errorMessage) {
        return Codec.LONG.validate(
            value -> value.compareTo(min) >= 0 && value.compareTo(max) <= 0
                    ? DataResult.success(value)
                    : DataResult.error(() -> errorMessage.apply(value))
        );
    }

    private static Codec<Integer> intRangeWithMessage(int min, int max, Function<Integer, String> errorMessage) {
        return Codec.INT.validate(
            value -> value.compareTo(min) >= 0 && value.compareTo(max) <= 0
                    ? DataResult.success(value)
                    : DataResult.error(() -> errorMessage.apply(value))
        );
    }
}
