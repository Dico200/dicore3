package io.dico.dicore;

/**
 * This class represents a version, as the name suggests.
 * <p>
 * Class suggested by SB:
 * https://msdn.microsoft.com/en-us/library/system.version(v=vs.110).aspx Based
 * on:
 * https://github.com/mono/mono/blob/master/mcs/class/referencesource/mscorlib/system/version.cs
 */
public final class Version implements Comparable<Version>, Cloneable {
    
    private final int major;
    private final int minor;
    private final int build;
    private final int revision;
    
    /**
     * Initializes a new instance of the Version class, with major=0, minor=0,
     * build=undefined(-1), revision=undefined(-1)
     */
    public Version() {
        this.major = 0;
        this.minor = 0;
        this.build = -1;
        this.revision = -1;
    }
    
    /**
     * Initializes a new instance of the Version class using the specified major
     * and minor values.
     *
     * @param major major value
     * @param minor minor value
     * @throws IllegalArgumentException if major or minor is less than 0
     */
    public Version(int major, int minor) {
        this.major = validateRange("major", major);
        this.minor = validateRange("minor", minor);
        this.build = -1;
        this.revision = -1;
    }
    
    /**
     * Initializes a new instance of the Version class using the specified
     * major, minor, and build values.
     *
     * @param major major value
     * @param minor minor value
     * @param build build value
     * @throws NullPointerException if major, minor or build is less than 0
     */
    public Version(int major, int minor, int build) {
        this.major = validateRange("major", major);
        this.minor = validateRange("minor", minor);
        this.build = validateRange("build", build);
        this.revision = -1;
    }
    
    /**
     * Initializes a new instance of the Version class based on string
     *
     * @param version the string to parse
     */
    public Version(String version) {
        Version v = parseVersion(version);
        
        this.major = v.major;
        this.minor = v.minor;
        this.build = v.build;
        this.revision = v.revision;
    }
    
    /**
     * Initializes a new instance of the Version class with the specified major,
     * minor, build, and revision numbers.
     *
     * @param major    major value
     * @param minor    minor value
     * @param build    build value
     * @param revision revision value
     * @throws IllegalArgumentException if major, minor, build or revision is
     *                                  less than 0
     */
    public Version(int major, int minor, int build, int revision) {
        this.major = validateRange("major", major);
        this.minor = validateRange("minor", minor);
        this.build = validateRange("build", build);
        this.revision = validateRange("revision", revision);
    }
    
    /**
     * Validates the given component
     *
     * @param componentName the name of the component, used for exception message
     * @param component     the component value
     * @return the component value
     * @throws IllegalArgumentException if the component value is out of range // less than 0.
     */
    private static int validateRange(String componentName, int component) {
        if (component < 0) {
            throw new IllegalArgumentException('\'' + componentName + "' component out of range: " + component);
        }
        return component;
    }
    
    /**
     * Gets the value of the major component of the version number for the
     * current Version object.
     *
     * @return major value
     */
    public int getMajor() {
        return major;
    }
    
    /**
     * Gets the value of the minor component of the version number for the
     * current Version object.
     *
     * @return minor value
     */
    public int getMinor() {
        return minor;
    }
    
    /**
     * Gets the value of the build component of the version number for the
     * current Version object.
     *
     * @return build value, -1 if undefined
     */
    public int getBuild() {
        return build;
    }
    
    /**
     * Gets the value of the revision component of the version number for the
     * current Version object.
     *
     * @return revision value, -1 if undefined
     */
    public int getRevision() {
        return revision;
    }
    
    /**
     * Gets the high 16 bits of the revision number.
     *
     * @return major revision value, or -1 if revision is undefined
     */
    public int getMajorRevision() {
        return revision == -1 ? -1 : revision >>> 16 & 0xFFFF;
    }
    
    /**
     * Gets the low 16 bits of the revision number.
     *
     * @return minor revision value, or -1 if revision is undefined
     */
    public int getMinorRevision() {
        return revision == -1 ? -1 : revision & 0xFFFF;
    }
    
    /**
     * Compares the current Version object to a specified Version object and
     * returns an indication of their relative values.
     *
     * @param other The version object to compare to
     * @return 1 if this version is later than other, 0 if this version is equal
     * to other, and -1 otherwise
     * @throws NullPointerException if other is null
     */
    @Override
    public int compareTo(Version other) {
        if (other == null) {
            return 1;
        }
        
        if (this.major != other.major) {
            return this.major > other.major ? 1 : -1;
        }
        if (this.minor != other.minor) {
            return this.minor > other.minor ? 1 : -1;
        }
        if (this.build != other.build) {
            return this.build > other.build ? 1 : -1;
        }
        if (this.revision != other.revision) {
            return this.revision > other.revision ? 1 : -1;
        }
        return 0;
    }
    
    /**
     * @return a new Version object whose value is the same as the current
     * Version object.
     */
    @Override
    public Version clone() {
        try {
            return (Version) super.clone();
        } catch (Exception ex) {
            throw new InternalError("This should never happen", ex);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (o == null || !(o instanceof Version)) {
            return false;
        }
        
        Version version = (Version) o;
        
        return major == version.major
                && minor == version.minor
                && build == version.build
                && revision == version.revision;
    }
    
    @Override
    public int hashCode() {
        // rotate each component to start from a different byte
        int major = this.major;
        int minor = (this.minor >>> 8 | this.minor << 24);
        int build = (this.build >>> 16 | this.build << 16);
        int revision = (this.revision >>> 24 | this.revision << 8);
        return major ^ minor ^ build ^ revision;
    }
    
    /**
     * Gets a String representation of this Version object. The String
     * representation of two distinct Version objects is equal if and only if
     * the two Version objects are equal as declared by
     * {@link Object#equals(Object)} The returned representation includes at
     * least 1 component of this version, but will include any components that
     * are in use.
     *
     * @return a String representation of this Version object.
     */
    @Override
    public String toString() {
        return toString(getComponentCount());
    }
    
    /**
     * Converts the value of the current Version object to its equivalent String
     * representation. A specified count indicates the number of components to
     * return.
     *
     * @param components the number of components to include in the string
     *                   representation of this Version object.
     * @return A string representation of this version object.
     * @throws IllegalArgumentException if components is not between 0 and 4
     *                                  inclusive, or components exceeds {@link #getComponentCount()}
     */
    public String toString(int components) {
        if (!(0 <= components && components <= 4)) {
            throw new IllegalArgumentException();
        }
        StringBuilder result = new StringBuilder();
        if (0 < components) {
            result.append(major);
        }
        if (1 < components) {
            result.append('.').append(minor);
        }
        if (2 < components) {
            result.append('.').append(build);
        }
        if (3 < components) {
            result.append('.').append(revision);
        }
        return result.toString();
    }
    
    /**
     * Computes the amount of used components. That is, the number of components
     * counted up to and including the last defined component.
     *
     * @return the amount of used components
     */
    public int getComponentCount() {
        if (revision != -1) {
            return 4;
        }
        if (build != -1) {
            return 3;
        }
        return 2;
    }
    
    /**
     * Gets a Version object from the given String representation
     * <p>
     * A string representation of a Version must follow the regex:
     * ([0-9]+\.){1-3}[0-9]+ without any numbers in it exceeding
     * {@link Integer#MAX_VALUE}
     * <p>
     * For a simpler representation format: major.minor[.build[.revision]]
     *
     * @param input The String representation of a Version
     * @return a Version
     * @throws IllegalArgumentException if the input does not follow a Version
     *                                  syntax.
     * @throws NullPointerException     if input is null
     */
    public static Version parseVersion(String input) {
        int length = input.length();
        int[] result = new int[]{-1, -1, -1, -1};
        int index = 0;
        int currentNumber = -1;
        int resultIndex = 0;
        
        while (index < length) {
            if (resultIndex == 4) {
                
                throw new IllegalArgumentException("Found a version component beyond the fourth at " + input + "[" + index + "]");
            }
            
            char c = input.charAt(index);
            
            // the ascii code for the character '0' is 0x30.
            // used in favor of Character.digit(char, int) for performance.
            int digit = c - 0x30;
            
            if (c == '.') {
                if (currentNumber < 0) {
                    throw new IllegalArgumentException("Expected a digit at " + input + "[" + index + "], but found " + c);
                }
                
                result[resultIndex] = currentNumber;
                resultIndex++;
                currentNumber = -1;
            } else if (!(0 <= digit && digit <= 9)) {
                throw new IllegalArgumentException("Version components must be separated by a '.', but found " + c + " at " + input + "[" + index + "]");
            } else if (currentNumber < 0) {
                //Got digit
                currentNumber = digit;
            } else {
                // any digit subsequent to the first
                if (currentNumber == 0) {
                    // ensure we don't have a component that started with 0 and has any additional digits.
                    // The first 0 would be obsolete to represent its value, meaning that there would be multiple valid string representations for the same Version
                    throw new IllegalArgumentException("Unneeded 0 in version component number is illegal at " + input + "[" + index + "]");
                }
                
                //Got digit
                currentNumber = currentNumber * 10 + digit;
            }
            
            index++;
        }
        
        if (currentNumber >= 0) {
            result[resultIndex] = currentNumber;
            resultIndex++;
        }
        
        switch (resultIndex) {
            default:
                throw new IllegalArgumentException("Version requires major and minor value: " + input);
            case 2:
                return new Version(result[0], result[1]);
            case 3:
                return new Version(result[0], result[1], result[2]);
            case 4:
                return new Version(result[0], result[1], result[2], result[3]);
        }
    }
    
    /**
     * Tries to convert the string representation of a version number to an
     * equivalent Version object, and returns the result value or null if parse
     * failed.
     *
     * @param input The input to parse
     * @return The parsed version or null if parse failed.
     */
    public static Version tryParse(String input) {
        try {
            return parseVersion(input);
        } catch (Exception ex) {
            //Simply ignore the error
            return null;
        }
    }
    
    /**
     * Tries to convert the string representation of a version number to an
     * equivalent Version object, and returns a value that indicates whether the
     * conversion succeeded.
     *
     * @param input   The input to parse
     * @param version The result version
     * @return true if the input was parsed successfully and the result of
     * parsing the input is equal to the given version
     * @throws NullPointerException if input is null or version is null
     */
    public static boolean tryParse(String input, InOutParam<Version> version) {
        try {
            version.setValue(parseVersion(input));
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
