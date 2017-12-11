/*
 * Copyright (c) 2017, ProjectOreville
 *
 * All rights reserved.
 *
 * Author(s):
 *  Sławomir Belter
 */

package io.dico.dicore;

/**
 * This is a helper class that allows you to add output and input-output
 * parameters to java functions.
 * Warning: This class is no thread safe.
 * 
 * @author Sławomir Belter
 * @param <T> The type of the parameter
 * 
 */
public final class InOutParam<T> {

    /**
     * Initialize reference parame (in and out value)
     *
     * @param <T> The type of the parameter
     * @param value Initial value of the pref parameter.
     * @return New instance of the {@link InOutParam} initialized as ref.
     */
    public static <T> InOutParam<T> Ref(T value) {
        return new InOutParam<T>(value);
    }

    /**
     * Initialize new instance of the output param (out only)
     *
     * @param <T> The type of the parameter
     * @return New instance of the {@link InOutParam} initialized as out.
     */
    public static <T> InOutParam<T> Out() {
        return new InOutParam<T>();
    }

    /**
     * Is the value set
     */
    private boolean m_isSet;

    /**
     * The parameter value
     */
    private T m_value;

    /**
     * Create new instance of ref param
     *
     * @param value
     */
    private InOutParam(T value) {
        m_value = value;
        m_isSet = true;
    }

    /**
     * Create new instance of out param
     */
    private InOutParam() {
        m_isSet = false;
    }
    
    /**
     * Is the value set
     *
     * @return Returns true if the value of the parameter was set.
     * This is always true for ref parameters.
     */    
    public boolean isSet() {
        return m_isSet;
    }
    

    /**
     * Get the parameter value
     *
     * @return The current value of the parameter
     * @throws IllegalStateException if the value is not set. 
     */
    public T getValue() {
        if (m_isSet) {
            return m_value;
        }

        throw new IllegalStateException("Output parameter not set");
    }

    /**
     * Set the parameter value
     *
     * @param value The new value of the parameter
     */
    public void setValue(T value) {
        m_isSet = true;
        m_value = value;
    }
}
