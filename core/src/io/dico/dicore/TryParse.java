package io.dico.dicore;

import java.util.function.Function;
import java.util.function.BiFunction;

/**
 * This class provides a checked (returns true if parse was successfull)
 version of int32 class parsers.
 * @author SBPrime
 */
public final class TryParse {        
    /**
     * Perform the actual string parse.
     * @param <T> The type of the parse result
     * @param s The string to parse
     * @param parserMethod Method used to parse the string
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    private static <T> boolean parse(String s, Function<String, T> parserMethod, InOutParam<T> result) {
        if (s == null || result == null || parserMethod == null) {
            return false;
        }                
        
        try {
            result.setValue(parserMethod.apply(s));
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Perform the actual string parse.
     * @param <T> The type of the parse result
     * @param s The string to parse
     * @param radix the radix to be used while parsing
     * @param parserMethod Method used to parse the string
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    private static <T> boolean parse(String s, int radix, BiFunction<String, Integer, T> parserMethod, InOutParam<T> result) {
        if (s == null || result == null || parserMethod == null) {
            return false;
        }                
        
        try {
            result.setValue(parserMethod.apply(s, radix));
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
        
    /**
     * Perform the actual string parse.
     * @param <T> The type of the parse result
     * @param s The string to parse
     * @param parserMethod Method used to parse the string
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    private static <T> T parse(String s, Function<String, T> parserMethod) {
        if (s == null || parserMethod == null) {
            return null;
        }                
        
        try {
            return parserMethod.apply(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Perform the actual string parse.
     * @param <T> The type of the parse result
     * @param s The string to parse
     * @param radix the radix to be used while parsing
     * @param parserMethod Method used to parse the string
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    private static <T> T parse(String s, int radix, BiFunction<String, Integer, T> parserMethod) {
        if (s == null || parserMethod == null) {
            return null;
        }                
        
        try {
            return parserMethod.apply(s, radix);
        } catch (NumberFormatException e) {
            return null;
        }
    }
   
    
    /**
     * Try to parse a string to byte
     *
     * @param s The string to parse
     * @param radix the radix to be used while parsing
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    public static boolean int8(String s, int radix, InOutParam<Byte> result) {
        return parse(s, radix, (ss, r) -> Byte.parseByte(ss, r), result);
    }
    
    /**
     * Try to parse a string to short
     *
     * @param s The string to parse
     * @param radix the radix to be used while parsing
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    public static boolean int16(String s, int radix, InOutParam<Short> result) {
        return parse(s, radix, (ss, r) -> Short.parseShort(ss, r), result);
    }
    
    /**
     * Try to parse a string to int
     *
     * @param s The string to parse
     * @param radix the radix to be used while parsing
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    public static boolean int32(String s, int radix, InOutParam<Integer> result) {
        return parse(s, radix, (ss, r) -> Integer.parseInt(ss, r), result);
    }
    
    /**
     * Try to parse a string to long
     *
     * @param s The string to parse
     * @param radix the radix to be used while parsing
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    public static boolean int64(String s, int radix, InOutParam<Long> result) {
        return parse(s, radix, (ss, r) -> Long.parseLong(ss, r), result);
    }
    
    
    /**
     * Try to parse a string to byte
     *
     * @param s The string to parse
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    public static boolean int8(String s, InOutParam<Byte> result) {
        return parse(s, ss -> Byte.parseByte(ss), result);
    }    
    
    /**
     * Try to parse a string to short
     *
     * @param s The string to parse
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    public static boolean int16(String s, InOutParam<Short> result) {
        return parse(s, ss -> Short.parseShort(ss), result);
    }
    
    
    /**
     * Try to parse a string to int32.
     *
     * @param s The string to parse
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    public static boolean int32(String s, InOutParam<Integer> result) {
        return parse(s, ss -> Integer.parseInt(ss), result);
    }
    
    /**
     * Try to parse a string to long.
     *
     * @param s The string to parse
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    public static boolean int64(String s, InOutParam<Long> result) {
        return parse(s, ss -> Long.parseLong(ss), result);
    }
    
    
    /**
     * Try to parse a string to float.
     *
     * @param s The string to parse
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    public static boolean float32(String s, InOutParam<Float> result) {
        return parse(s, ss -> Float.parseFloat(ss), result);
    }
    
    /**
     * Try to parse a string to float.
     *
     * @param s The string to parse
     * @param result An output parameter to store the parse result.
     * @return Returns true if the string was parsed.
     */
    public static boolean float64(String s, InOutParam<Double> result) {
        return parse(s, ss -> Double.parseDouble(ss), result);
    }
    
    /**
     * Try to parse a string to byte
     *
     * @param s The string to parse
     * @param radix the radix to be used while parsing
     * @return Returns the parsed value or null if parse failed.
     */
    public static Byte int8(String s, int radix) {
        return parse(s, radix, (ss, r) -> Byte.parseByte(ss, r));
    }

    /**
     * Try to parse a string to short
     *
     * @param s The string to parse
     * @param radix the radix to be used while parsing
     * @return Returns the parsed value or null if parse failed.
     */
    public static Short int16(String s, int radix) {
        return parse(s, radix, (ss, r) -> Short.parseShort(ss, r));
    }
    
    /**
     * Try to parse a string to int
     *
     * @param s The string to parse
     * @param radix the radix to be used while parsing
     * @return Returns the parsed value or null if parse failed.
     */
    public static Integer int32(String s, int radix) {
        return parse(s, radix, (ss, r) -> Integer.parseInt(ss, r));
    }
    
    /**
     * Try to parse a string to long
     *
     * @param s The string to parse
     * @param radix the radix to be used while parsing
     * @return Returns the parsed value or null if parse failed.
     */
    public static Long int64(String s, int radix) {
        return parse(s, radix, (ss, r) -> Long.parseLong(ss, r));
    }
            
    /**
     * Try to parse a string to byte
     *
     * @param s The string to parse
     * @return Returns the parsed value or null if parse failed.
     */
    public static Byte int8(String s) {
        return parse(s, ss -> Byte.parseByte(ss));
    }
    
    /**
     * Try to parse a string to short
     *
     * @param s The string to parse
     * @return Returns the parsed value or null if parse failed.
     */
    public static Short int16(String s) {
        return parse(s, ss -> Short.parseShort(ss));
    }
    
    /**
     * Try to parse a string to int
     *
     * @param s The string to parse
     * @return Returns the parsed value or null if parse failed.
     */
    public static Integer int32(String s) {
        return parse(s, ss -> Integer.parseInt(ss));
    }
    
    /**
     * Try to parse a string to long
     *
     * @param s The string to parse
     * @return Returns the parsed value or null if parse failed.
     */
    public static Long int64(String s) {
        return parse(s, ss -> Long.parseLong(ss));
    }
    
    
    /**
     * Try to parse a string to float.
     *
     * @param s The string to parse
     * @return Returns the parsed value or null if parse failed.
     */
    public static Float float32(String s) {
        return parse(s, ss -> Float.parseFloat(ss));
    }
    
    /**
     * Try to parse a string to float.
     *
     * @param s The string to parse
     * @return Returns the parsed value or null if parse failed.
     */
    public static Double float64(String s) {
        return parse(s, ss -> Double.parseDouble(ss));
    }
}
