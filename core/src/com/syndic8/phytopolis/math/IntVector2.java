package com.syndic8.phytopolis.math;

/**
 * Encapsulates a 2D integer vector. Allows chaining methods by returning a
 * reference to itself.
 */
public class IntVector2 {

    public final static IntVector2 Zero = new IntVector2(0, 0);
    /**
     * The x-component of this vector.
     **/
    public int x;
    /**
     * The y-component of this vector.
     **/
    public int y;

    /**
     * Constructs a new vector at (0,0)
     */
    public IntVector2() {
    }

    /**
     * Constructs a vector with the given components.
     *
     * @param x The x-component
     * @param y The y-component
     */
    public IntVector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs a vector from the given vector.
     *
     * @param v The vector
     */
    public IntVector2(IntVector2 v) {
        this.x = v.x;
        this.y = v.y;
    }

    /**
     * Sets this vector's components to the given components. Returns this
     * vector for chaining.
     *
     * @param x The x-component
     * @param y The y-component
     */
    public IntVector2 set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets this vector's components from the given vector. Returns this
     * vector for chaining.
     *
     * @param v The vector
     */
    public IntVector2 set(IntVector2 v) {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

}
