package io.dico.dicore.config.serializers;

import io.dico.dicore.Logging;
import io.dico.dicore.SetBasedWhitelist;
import io.dico.dicore.Whitelist;
import io.dico.dicore.config.ConfigLogging;
import io.dico.dicore.inventory.ItemProperties;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.dico.dicore.config.serializers.SerializationUtil.toMap;
import static java.lang.Integer.parseInt;

public class ConfigSerializers {
    
    public static IConfigSerializer<Boolean> forBoolean() {
        return BooleanSerializer.defaultToFalse;
    }
    
    public static IConfigSerializer<Boolean> forBoolean(boolean defaultValue) {
        return defaultValue ? BooleanSerializer.defaultToTrue : forBoolean();
    }
    
    public static IConfigSerializer<Integer> forInt() {
        return IntSerializer.defaultToZero;
    }
    
    public static IConfigSerializer<Integer> forInt(int defaultValue) {
        return defaultValue == 0 ? forInt() : new IntSerializer(defaultValue);
    }
    
    public static IConfigSerializer<Long> forLong() {
        return LongSerializer.defaultToZero;
    }
    
    public static IConfigSerializer<Long> forLong(long defaultValue) {
        return defaultValue == 0 ? forLong() : new LongSerializer(defaultValue);
    }
    
    public static IConfigSerializer<Float> forFloat() {
        return FloatSerializer.defaultToZero;
    }
    
    public static IConfigSerializer<Float> forFloat(float defaultValue) {
        return defaultValue == 0 ? forFloat() : new FloatSerializer(defaultValue);
    }
    
    public static IConfigSerializer<Double> forDouble() {
        return DoubleSerializer.defaultToZero;
    }
    
    public static IConfigSerializer<Double> forDouble(double defaultValue) {
        return defaultValue == 0 ? forDouble() : new DoubleSerializer(defaultValue);
    }
    
    public static IConfigSerializer<String> forString() {
        return StringSerializer.defaultToNull;
    }
    
    public static IConfigSerializer<String> forString(String defaultValue) {
        return defaultValue == null ? forString() : new StringSerializer(defaultValue);
    }
    
    public static IConfigSerializer<Double> forChance() {
        return forChance(0);
    }
    
    public static IConfigSerializer<Double> forChance(double defaultChance) {
        return forDouble(defaultChance).map(DoubleAsChanceMapper.instance);
    }
    
    public static IConfigSerializer<Color> forColor() {
        return ColorSerializer.defaultToRed;
    }
    
    public static IConfigSerializer<Color> forColor(Color defaultValue) {
        return new ColorSerializer(defaultValue);
    }
    
    public static IConfigSerializer<Whitelist> forMaterialWhitelist() {
        return MaterialWhitelistSerializer.defaultToEmpty;
    }
    
    public static IConfigSerializer<Whitelist> forMaterialWhitelist(boolean blacklist, Material... defaults) {
        return new MaterialWhitelistSerializer(new SetBasedWhitelist(defaults, blacklist));
    }
    
    public static IConfigSerializer<ItemStack> forItem(ItemStack defaultItem) {
        return new ItemStackSerializer(defaultItem);
    }
    
    public static IConfigSerializer<PotionEffect> forPotionEffect(PotionEffectType type) {
        return forPotionEffect(new PotionEffect(type, 100, 0, false, true), true);
    }
    
    public static IConfigSerializer<PotionEffect> forPotionEffect(PotionEffect defaults, boolean forceType) {
        return new PotionEffectSerializer(defaults, forceType);
    }
    
    @SuppressWarnings("unchecked")
    public static <TComp, TArray> ArrayConfigSerializer<TComp, TArray> forArray(IConfigSerializer<TComp> delegate, int size, boolean forceSize) {
        Class<TComp> type = delegate.type();
        ArrayConfigSerializer<?, ?> rv;
        if (type.isPrimitive()) {
            switch (type.getSimpleName()) {
                case "int":
                    rv = new ArrayConfigSerializer.OfInt((IConfigSerializer<Integer>) delegate, size, forceSize);
                    break;
                case "long":
                    rv = new ArrayConfigSerializer.OfLong((IConfigSerializer<Long>) delegate, size, forceSize);
                    break;
                case "float":
                    rv = new ArrayConfigSerializer.OfFloat((IConfigSerializer<Float>) delegate, size, forceSize);
                    break;
                case "double":
                    rv = new ArrayConfigSerializer.OfDouble((IConfigSerializer<Double>) delegate, size, forceSize);
                    break;
                case "boolean":
                    rv = new ArrayConfigSerializer.OfBoolean((IConfigSerializer<Boolean>) delegate, size, forceSize);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        } else {
            rv = new ArrayConfigSerializer.OfReference<>(delegate, size, forceSize);
        }
        return (ArrayConfigSerializer<TComp, TArray>) rv;
    }
    
    @SuppressWarnings("unchecked")
    public static <TComp, TArray> ArrayConfigSerializer<TComp, TArray> forArray(IConfigSerializer<TComp> delegate, TArray arrayDefaultSuggest, boolean forceSize) {
        Class<TComp> type = delegate.type();
        ArrayConfigSerializer<?, ?> rv;
        if (type.isPrimitive()) {
            switch (type.getSimpleName()) {
                case "int":
                    rv = new ArrayConfigSerializer.OfInt((IConfigSerializer<Integer>) delegate, (int[]) arrayDefaultSuggest, forceSize);
                    break;
                case "long":
                    rv = new ArrayConfigSerializer.OfLong((IConfigSerializer<Long>) delegate, (long[]) arrayDefaultSuggest, forceSize);
                    break;
                case "float":
                    rv = new ArrayConfigSerializer.OfFloat((IConfigSerializer<Float>) delegate, (float[]) arrayDefaultSuggest, forceSize);
                    break;
                case "double":
                    rv = new ArrayConfigSerializer.OfDouble((IConfigSerializer<Double>) delegate, (double[]) arrayDefaultSuggest, forceSize);
                    break;
                case "boolean":
                    rv = new ArrayConfigSerializer.OfBoolean((IConfigSerializer<Boolean>) delegate, (boolean[]) arrayDefaultSuggest, forceSize);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        } else {
            rv = new ArrayConfigSerializer.OfReference<>(delegate, (TComp[]) arrayDefaultSuggest, forceSize);
        }
        return (ArrayConfigSerializer<TComp, TArray>) rv;
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    
    private static final class BooleanSerializer extends SimpleConfigSerializer<Boolean> {
        private static final BooleanSerializer defaultToFalse = new BooleanSerializer(false);
        private static final BooleanSerializer defaultToTrue = new BooleanSerializer(true);
        
        public BooleanSerializer(Boolean defaultValue) {
            super(Boolean.TYPE, defaultValue);
        }
        
        @Override
        public SerializerResult<Boolean> load(Object source, ConfigLogging logger) {
            Boolean rv = null;
            if (source instanceof Boolean) {
                rv = (Boolean) source;
            } else if (source instanceof String) {
                if ("true".equalsIgnoreCase((String) source)) {
                    rv = true;
                } else if ("false".equalsIgnoreCase((String) source)) {
                    rv = false;
                }
            }
            if (rv == null) {
                logger.error("Expected boolean");
                return defaultValueResult();
            }
            return new SerializerResult<>(rv);
        }
    }
    
    private static final class IntSerializer extends NumberSerializer<Integer> {
        private static final IntSerializer defaultToZero = new IntSerializer(0);
        
        public IntSerializer(Integer defaultValue) {
            super(Integer.TYPE, defaultValue);
        }
        
        @Override
        protected Integer parse(String string) {
            return Integer.parseInt(string);
        }
        
        @Override
        protected Integer select(Number number) {
            return number instanceof Integer ? (Integer) number : number.intValue();
        }
        
    }
    
    private static final class LongSerializer extends NumberSerializer<Long> {
        private static final LongSerializer defaultToZero = new LongSerializer(0L);
        
        public LongSerializer(Long defaultValue) {
            super(Long.TYPE, defaultValue);
        }
        
        @Override
        protected Long parse(String string) {
            return Long.parseLong(string);
        }
        
        @Override
        protected Long select(Number number) {
            return number instanceof Long ? (Long) number : number.longValue();
        }
        
    }
    
    private static final class FloatSerializer extends NumberSerializer<Float> {
        private static final FloatSerializer defaultToZero = new FloatSerializer(0F);
        
        public FloatSerializer(Float defaultValue) {
            super(Float.TYPE, defaultValue);
        }
        
        @Override
        protected Float parse(String string) {
            return Float.parseFloat(string);
        }
        
        @Override
        protected Float select(Number number) {
            return number instanceof Float ? (Float) number : number.floatValue();
        }
        
    }
    
    private static final class DoubleSerializer extends NumberSerializer<Double> {
        private static final DoubleSerializer defaultToZero = new DoubleSerializer(0D);
        
        public DoubleSerializer(Double defaultValue) {
            super(Double.TYPE, defaultValue);
        }
        
        @Override
        protected Double parse(String string) {
            return Double.parseDouble(string);
        }
        
        @Override
        protected Double select(Number number) {
            return number instanceof Double ? (Double) number : number.doubleValue();
        }
        
    }
    
    private static final class StringSerializer extends SimpleConfigSerializer<String> {
        private static final StringSerializer defaultToNull = new StringSerializer(null);
        
        public StringSerializer(String defaultValue) {
            super(String.class, defaultValue);
        }
        
        @Override
        public SerializerResult<String> load(Object source, ConfigLogging logger) {
            if (source == null) {
                logger.error("Expected string");
                return defaultValueResult();
            }
            return new SerializerResult<>(source.toString());
        }
        
    }
    
    private static final class DoubleAsChanceMapper implements IConfigSerializerMapper<Double, Double> {
        private static final DoubleAsChanceMapper instance = new DoubleAsChanceMapper();
        
        @Override
        public SerializerResult<Double> postLoad(SerializerResult<Double> in) {
            return new SerializerResult<>(in.value / 100, in.isDefault);
        }
        
        @Override
        public Double preSave(Double value) {
            return value * 100;
        }
        
        @Override
        public Class<Double> type() {
            return Double.TYPE;
        }
    }
    
    private static final class ColorSerializer extends SimpleConfigSerializer<Color> {
        private static final ColorSerializer defaultToRed = new ColorSerializer(Color.RED);
        
        public ColorSerializer(Color defaultValue) {
            super(Color.class, defaultValue);
        }
        
        @Override
        public SerializerResult<Color> load(Object source, ConfigLogging logger) {
            if (!(source instanceof String)) {
                logger.error("Expected a color");
                return defaultValueResult();
            }
            
            try {
                return new SerializerResult<>(load((String) source));
            } catch (IllegalArgumentException ex) {
                logger.error("Illegal color: " + ex.getMessage());
                return defaultValueResult();
            }
        }
        
        @Override
        public Object serialize(Color value) {
            return toString(value);
        }
        
        private static final Pattern funcPattern = Pattern.compile("(\\w+)\\((.+)\\)");
        
        private static Color load(String input) throws IllegalArgumentException {
            try {
                return (Color) Color.class.getField(input.toUpperCase()).get(null);
            } catch (Exception ex) {
                Matcher matcher = funcPattern.matcher(input.trim());
                if (matcher.matches()) {
                    String function = matcher.group(1).toLowerCase();
                    if (function.equals("rgb")) {
                        String argument = matcher.group(2);
                        String[] inputs = argument.split(",");
                        if (inputs.length != 3) {
                            throw new IllegalArgumentException("rgb color must have 3 values");
                        }
                        
                        int[] colors = new int[3];
                        for (int i = 0; i < 3; i++) {
                            String arg = inputs[i].trim();
                            try {
                                colors[i] = Integer.parseInt(arg);
                                if (colors[i] < 0 || colors[i] > 255) {
                                    throw new IllegalArgumentException("rgb color's values are between 0 and 256 exclusive");
                                }
                            } catch (NumberFormatException ex2) {
                                try {
                                    float color = Float.parseFloat(arg);
                                    if (color < 0 || color > 1) {
                                        throw new IllegalArgumentException("rgb color's float value must be between 0 and 1 inclusive");
                                    }
                                    colors[i] = Math.max(0, Math.min(255, (int) (255 * color)));
                                } catch (NumberFormatException ex3) {
                                    throw new IllegalArgumentException("rgb color's values are numbers");
                                }
                            }
                        }
                        
                        return Color.fromRGB(colors[0], colors[1], colors[2]);
                    }
                    
                    throw new IllegalArgumentException("color function name " + function + " was not recognized");
                } else {
                    throw new IllegalArgumentException("color does not match " + funcPattern.pattern());
                }
            }
        }
        
        private static String toString(Color color) {
            return "rgb(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")";
        }
        
    }
    
    
    private static final class MaterialWhitelistSerializer extends SimpleConfigSerializer<Whitelist> {
        private static final MaterialWhitelistSerializer defaultToEmpty = new MaterialWhitelistSerializer(new SetBasedWhitelist(Collections.emptyList(), true));
        
        public MaterialWhitelistSerializer(Whitelist defaultValue) {
            super(Whitelist.class, defaultValue);
        }
        
        @Override
        public SerializerResult<Whitelist> load(Object source, ConfigLogging logger) {
            if (!(source instanceof ConfigurationSection)) {
                logger.error("Expected section mapping for whitelist");
                return defaultValueResult();
            }
            
            SetBasedWhitelist rv = new SetBasedWhitelist((ConfigurationSection) source, materialParser(logger));
            return new SerializerResult<>(rv);
        }
        
        private static Function<String, Material> materialParser(ConfigLogging logger) {
            return inputString -> {
                Material result = Material.matchMaterial(inputString);
                if (result == null) {
                    logger.error("Invalid material '" + inputString + "'");
                }
                return result;
            };
        }
        
        @Override
        public Object serialize(Whitelist state) {
            Map<String, Object> map = new LinkedHashMap<>(2);
            if (state instanceof SetBasedWhitelist) {
                map.put("blacklist", ((SetBasedWhitelist) state).isBlacklist());
                //noinspection unchecked
                map.put("listed", ((SetBasedWhitelist) state).getSet().stream()
                        .map(o -> ((Enum) o).name())
                        .collect(Collectors.toList()));
            } else {
                // NOTHING
                map.put("blacklist", false);
                map.put("listed", new ArrayList<String>());
            }
            return map;
        }
        
    }
    
    private static final class ItemStackSerializer extends SimpleConfigSerializer<ItemStack> {
        
        public ItemStackSerializer(ItemStack defaultValue) {
            super(ItemStack.class, defaultValue);
        }
        
        @Override
        public SerializerResult<ItemStack> load(Object source, ConfigLogging logger) {
            Map<String, Object> map = toMap(source, false);
            if (map == null) {
                logger.error("Expected a key-value mapping. Using default item.");
                return defaultValueResult();
            }
            
            ItemStack rv = new ItemProperties(true).loadFrom(map).toItemStack();
            return new SerializerResult<>(rv);
        }
        
        @Override
        public Object serialize(ItemStack value) {
            Map<String, Object> rv = new LinkedHashMap<>();
            new ItemProperties(value, true).writeTo(rv);
            return rv;
        }
        
    }
    
    private static final class PotionEffectSerializer extends SimpleConfigSerializer<PotionEffect> {
        private final boolean forceType;
        
        public PotionEffectSerializer(PotionEffect defaultValue, boolean forceType) {
            super(PotionEffect.class, defaultValue);
            this.forceType = forceType;
        }
        
        @Override
        public SerializerResult<PotionEffect> load(Object source, ConfigLogging logger) {
            if (source == null) {
                logger.error("Expected potion effect");
                return defaultValueResult();
            }
            
            String[] input = source.toString().split(" ");
            PotionEffect rv = parsePotionEffect(input, defaultValue(), forceType, logger);
            return new SerializerResult<>(rv);
        }
        
        @Override
        public Object serialize(PotionEffect state) {
            return state.getType().getName().toLowerCase().replace('_', '-') + ' '
                    + state.getDuration() + ' '
                    + state.getAmplifier() + ' '
                    + Boolean.toString(state.isAmbient()) + ' '
                    + Boolean.toString(state.hasParticles());
        }
        
        private static PotionEffect parsePotionEffect(String[] input, PotionEffect defaults, boolean forceType, Logging logger) {
            PotionEffectType type = defaults.getType();
            int duration = defaults.getDuration();
            int amplifier = defaults.getAmplifier();
            boolean ambient = defaults.isAmbient();
            boolean hasParticles = defaults.hasParticles();
            
            int index = 0;
            if (!forceType && index < input.length) {
                type = PotionEffectType.getByName(input[index]);
                if (defaults.getType() == null) {
                    if (type == null) {
                        logger.error("type not recognized: " + input[index] + ", defaulting to speed");
                        type = PotionEffectType.SPEED;
                    }
                    index++;
                } else if (type == null) {
                    type = defaults.getType();
                } else {
                    index++;
                }
            }
            
            if (index < input.length) {
                try {
                    duration = parseInt(input[index++]);
                } catch (NumberFormatException ex) {
                    boolean success = false;
                    if (forceType && index < input.length) try {
                        duration = parseInt(input[index++]);
                        success = true;
                    } catch (NumberFormatException ignored) {
                    
                    }
                    
                    if (!success) {
                        logger.error("invalid number for the duration");
                    }
                }
            }
            
            if (index < input.length) {
                try {
                    amplifier = parseInt(input[index++]);
                } catch (NumberFormatException ex) {
                    logger.error("invalid number for the amplifier");
                }
            }
            
            if (index < input.length) {
                try {
                    ambient = Boolean.parseBoolean(input[index++]);
                } catch (NumberFormatException ex) {
                    logger.error("invalid boolean for ambience");
                }
            }
            
            if (index < input.length) {
                try {
                    hasParticles = Boolean.parseBoolean(input[index]);
                } catch (NumberFormatException ex) {
                    logger.error("invalid boolean for particles");
                }
            }
            
            return new PotionEffect(type, duration, amplifier, ambient, hasParticles);
        }
    }
    
}
