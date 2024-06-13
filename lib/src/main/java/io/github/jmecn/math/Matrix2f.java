/*
 * Copyright (c) 2009-2022 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.jmecn.math;

import com.jme3.export.*;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.util.BufferUtils;
import com.jme3.util.TempVars;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Logger;

/**
 * A 3x3 matrix composed of 9 single-precision elements, used to represent
 * linear transformations of 3-D coordinates, such as rotations, reflections,
 * and scaling.
 *
 * <p>Element numbering is (row, column), so m01 is the element in row 0,
 * column 1.
 *
 * <p>For pure rotations, the {@link com.jme3.math.Quaternion} class provides a
 * more efficient representation.
 *
 * <p>With one exception, the methods with names ending in "Local" modify the
 * current instance. They are used to avoid creating garbage.
 *
 * @author Mark Powell
 * @author Joshua Slack
 */
public final class Matrix2f implements Savable, Cloneable, java.io.Serializable {

    static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(Matrix2f.class.getName());
    /**
     * The element in row 0, column 0.
     */
    protected float m00;
    /**
     * The element in row 0, column 1.
     */
    protected float m01;
    /**
     * The element in row 1, column 0.
     */
    protected float m10;
    /**
     * The element in row 1, column 1.
     */
    protected float m11;
    /**
     * Shared instance of the all-zero matrix. Do not modify!
     */
    public static final Matrix2f ZERO = new Matrix2f(0, 0, 0, 0);
    /**
     * Shared instance of the identity matrix (diagonals = 1, other elements =
     * 0). Do not modify!
     */
    public static final Matrix2f IDENTITY = new Matrix2f();

    /**
     * Instantiates an identity matrix (diagonals = 1, other elements = 0).
     */
    public Matrix2f() {
        loadIdentity();
    }

    /**
     * Instantiates a matrix with specified elements.
     *
     * @param m00 the desired value for row 0, column 0
     * @param m01 the desired value for row 0, column 1
     * @param m10 the desired value for row 1, column 0
     * @param m11 the desired value for row 1, column 1
     */
    public Matrix2f(float m00, float m01, float m10, float m11) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
    }

    /**
     * Instantiates a copy of the matrix argument. If the argument is null, an
     * identity matrix is produced.
     *
     * @param mat the matrix to copy (unaffected) or null for identity
     */
    public Matrix2f(Matrix2f mat) {
        set(mat);
    }

    /**
     * Replaces all 9 elements with their absolute values.
     */
    public void absoluteLocal() {
        m00 = FastMath.abs(m00);
        m01 = FastMath.abs(m01);
        m10 = FastMath.abs(m10);
        m11 = FastMath.abs(m11);
    }

    /**
     * Copies the matrix argument. If the argument is null, the current instance
     * is set to identity (diagonals = 1, other elements = 0).
     *
     * @param matrix the matrix to copy (unaffected) or null for identity
     * @return the (modified) current instance (for chaining)
     */
    public Matrix2f set(Matrix2f matrix) {
        if (null == matrix) {
            loadIdentity();
        } else {
            m00 = matrix.m00;
            m01 = matrix.m01;
            m10 = matrix.m10;
            m11 = matrix.m11;
        }
        return this;
    }

    /**
     * Returns the element at the specified position. The matrix is unaffected.
     *
     * @param i the row index (0, or 1)
     * @param j the column index (0 or 1)
     * @return the value of the element at (i, j)
     * @throws IllegalArgumentException if either index isn't 0 or 1
     */
    @SuppressWarnings("fallthrough")
    public float get(int i, int j) {
        switch (i) {
            case 0:
                switch (j) {
                    case 0:
                        return m00;
                    case 1:
                        return m01;
                }
            case 1:
                switch (j) {
                    case 0:
                        return m10;
                    case 1:
                        return m11;
                }
        }

        logger.warning("Invalid matrix index.");
        throw new IllegalArgumentException("Invalid indices into matrix.");
    }

    /**
     * Copies the matrix to the specified array. The matrix is unaffected.
     *
     * <p>If the array has 16 elements, then the matrix is treated as if it
     * contained the 1st 2 rows and 1st 2 columns of a 4x4 matrix.
     *
     * @param data storage for the elements (not null, length=4, 9 or 16)
     * @param rowMajor true to store the elements in row-major order (m00, m01,
     *     ...) or false to store them in column-major order (m00, m10, ...)
     * @throws IndexOutOfBoundsException if {@code data} doesn't have 4, 9 or 16
     *     elements
     * @see #fillFloatArray(float[], boolean)
     */
    public void get(float[] data, boolean rowMajor) {
        if (data.length == 4) {
            if (rowMajor) {
                data[0] = m00;
                data[1] = m01;
                data[2] = m10;
                data[3] = m11;
            } else {
                data[0] = m00;
                data[1] = m10;
                data[2] = m01;
                data[3] = m11;
            }
        } else if (data.length == 9) {
            if (rowMajor) {
                data[0] = m00;
                data[1] = m01;
                data[3] = m10;
                data[4] = m11;
            } else {
                data[0] = m00;
                data[1] = m10;
                data[3] = m01;
                data[4] = m11;
            }
        } else if (data.length == 16) {
            if (rowMajor) {
                data[0] = m00;
                data[1] = m01;
                data[4] = m10;
                data[5] = m11;
            } else {
                data[0] = m00;
                data[1] = m10;
                data[4] = m01;
                data[5] = m11;
            }
        } else {
            throw new IndexOutOfBoundsException("Array size must be 4, 9 or 16 in Matrix2f.get().");
        }
    }

    /**
     * Normalizes the matrix and returns the result in the argument. The current
     * instance is unaffected, unless it's {@code store}.
     *
     * @param store storage for the result, or null for a new Matrix2f
     * @return either {@code store} or a new Matrix2f
     */
    public Matrix2f normalize(Matrix2f store) {
        if (store == null) {
            store = new Matrix2f();
        }

        float mag = 1.0f / FastMath.sqrt(
                  m00 * m00
                + m10 * m10);

        store.m00 = m00 * mag;
        store.m10 = m10 * mag;

        mag = 1.0f / FastMath.sqrt(
                  m01 * m01
                + m11 * m11);

        store.m01 = m01 * mag;
        store.m11 = m11 * mag;
        return store;
    }

    /**
     * Normalizes the matrix and returns the (modified) current instance.
     *
     * @return the (modified) current instance (for chaining)
     */
    public Matrix2f normalizeLocal() {
        return normalize(this);
    }

    /**
     * Returns the specified column. The matrix is unaffected.
     *
     * <p>If the matrix is a pure rotation, each column contains one of the
     * basis vectors.
     *
     * @param i the column index (0 or 1)
     * @return a new Vector2f
     * @throws IllegalArgumentException if {@code i} isn't 0 or 1
     */
    public Vector2f getColumn(int i) {
        return getColumn(i, null);
    }

    /**
     * Returns the specified column. The matrix is unaffected.
     *
     * <p>If the matrix is a pure rotation, each column contains one of the
     * basis vectors.
     *
     * @param i the column index (0 or 1)
     * @param store storage for the result, or null for a new Vector2f
     * @return either {@code store} or a new Vector2f
     * @throws IllegalArgumentException if {@code i} isn't 0 or 1
     */
    public Vector2f getColumn(int i, Vector2f store) {
        if (store == null) {
            store = new Vector2f();
        }
        switch (i) {
            case 0:
                store.x = m00;
                store.y = m10;
                break;
            case 1:
                store.x = m01;
                store.y = m11;
                break;
            default:
                logger.warning("Invalid column index.");
                throw new IllegalArgumentException("Invalid column index. " + i);
        }
        return store;
    }

    /**
     * Returns the specified row. The matrix is unaffected.
     *
     * @param i the row index (0, 1, or 2)
     * @return a new Vector2f
     * @throws IllegalArgumentException if {@code i} isn't 0, 1, or 2
     */
    public Vector2f getRow(int i) {
        return getRow(i, null);
    }

    /**
     * Returns the specified row. The matrix is unaffected.
     *
     * @param i the row index (0 or 1)
     * @param store storage for the result, or null for a new Vector2f
     * @return either {@code store} or a new Vector2f
     * @throws IllegalArgumentException if {@code i} isn't 0 or 1
     */
    public Vector2f getRow(int i, Vector2f store) {
        if (store == null) {
            store = new Vector2f();
        }
        switch (i) {
            case 0:
                store.x = m00;
                store.y = m01;
                break;
            case 1:
                store.x = m10;
                store.y = m11;
                break;
            default:
                logger.warning("Invalid row index.");
                throw new IllegalArgumentException("Invalid row index. " + i);
        }
        return store;
    }

    /**
     * Copies the matrix to a new FloatBuffer. The matrix is unaffected.
     *
     * @return a new, rewound FloatBuffer containing all 4 elements in row-major
     *     order (m00, m01, ...)
     */
    public FloatBuffer toFloatBuffer() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(4);

        fb.put(m00).put(m01);
        fb.put(m10).put(m11);
        fb.rewind();
        return fb;
    }

    /**
     * Copies the matrix to the specified FloatBuffer, starting at its current
     * position. The matrix is unaffected.
     *
     * @param fb storage for the elements (not null, must have space to put 4
     *     more floats)
     * @param columnMajor true to store the elements in column-major order (m00,
     *     m10, ...) or false to store them in row-major order (m00, m01, ...)
     * @return {@code fb}, its position advanced by 4
     */
    public FloatBuffer fillFloatBuffer(FloatBuffer fb, boolean columnMajor) {
        TempVars vars = TempVars.get();

        fillFloatArray(vars.matrixWrite, columnMajor);
        fb.put(vars.matrixWrite, 0, 4);

        vars.release();

        return fb;
    }

    /**
     * Copies the matrix to the 1st 4 elements of the specified array. The
     * matrix is unaffected.
     *
     * @param f storage for the elements (not null, length&ge;4)
     * @param columnMajor true to store the elements in column-major order (m00,
     *     m10, ...) or false to store them in row-major order (m00, m01, ...)
     * @see #get(float[], boolean)
     */
    public void fillFloatArray(float[] f, boolean columnMajor) {
        if (columnMajor) {
            f[0] = m00;
            f[1] = m10;
            f[2] = m01;
            f[3] = m11;
        } else {
            f[0] = m00;
            f[1] = m01;
            f[2] = m10;
            f[3] = m11;
        }
    }

    /**
     * Sets the specified column.
     *
     * @param i which column to set (0 or 1)
     * @param column the desired element values (unaffected) or null for none
     * @return the (modified) current instance (for chaining)
     * @throws IllegalArgumentException if {@code i} isn't 0 or 1
     */
    public Matrix2f setColumn(int i, Vector2f column) {
        if (column == null) {
            logger.warning("Column is null. Ignoring.");
            return this;
        }
        switch (i) {
            case 0:
                m00 = column.x;
                m10 = column.y;
                break;
            case 1:
                m01 = column.x;
                m11 = column.y;
                break;
            default:
                logger.warning("Invalid column index.");
                throw new IllegalArgumentException("Invalid column index. " + i);
        }
        return this;
    }

    /**
     * Sets the specified row.
     *
     * @param i which row to set (0 or 1)
     * @param row the desired element values (unaffected) or null for none
     * @return the (modified) current instance (for chaining)
     * @throws IllegalArgumentException if {@code i} isn't 0 or 1
     */
    public Matrix2f setRow(int i, Vector2f row) {
        if (row == null) {
            logger.warning("Row is null. Ignoring.");
            return this;
        }
        switch (i) {
            case 0:
                m00 = row.x;
                m01 = row.y;
                break;
            case 1:
                m10 = row.x;
                m11 = row.y;
                break;
            default:
                logger.warning("Invalid row index.");
                throw new IllegalArgumentException("Invalid row index. " + i);
        }
        return this;
    }

    /**
     * Sets the specified element.
     *
     * @param i the row index (0 or 1)
     * @param j the column index (0 or 1)
     * @param value desired value for the element at (i, j)
     * @return the (modified) current instance (for chaining)
     * @throws IllegalArgumentException if either index isn't 0 or 1
     */
    @SuppressWarnings("fallthrough")
    public Matrix2f set(int i, int j, float value) {
        switch (i) {
            case 0:
                switch (j) {
                    case 0:
                        m00 = value;
                        return this;
                    case 1:
                        m01 = value;
                        return this;
                }
            case 1:
                switch (j) {
                    case 0:
                        m10 = value;
                        return this;
                    case 1:
                        m11 = value;
                        return this;
                }
        }

        logger.warning("Invalid matrix index.");
        throw new IllegalArgumentException("Invalid indices into matrix.");
    }

    /**
     * Copies all 4 elements from the specified 2-dimensional array.
     *
     * @param matrix the input array (not null, length=2, the first element
     *     having length=2, the other elements having length&ge;2, unaffected)
     * @return the (modified) current instance (for chaining)
     * @throws IllegalArgumentException if the array is the wrong size
     */
    public Matrix2f set(float[][] matrix) {
        if (matrix.length != 2 || matrix[0].length != 2) {
            throw new IllegalArgumentException(
                    "Array must be of size 4.");
        }

        m00 = matrix[0][0];
        m01 = matrix[0][1];
        m10 = matrix[1][0];
        m11 = matrix[1][1];

        return this;
    }

    /**
     * Configures from the specified column vectors. If the vectors form an
     * orthonormal basis, the result will be a pure rotation matrix.
     *
     * @param uAxis the desired value for column 0 (not null, unaffected)
     * @param vAxis the desired value for column 1 (not null, unaffected)
     */
    public void fromAxes(Vector2f uAxis, Vector2f vAxis) {
        m00 = uAxis.x;
        m10 = uAxis.y;

        m01 = vAxis.x;
        m11 = vAxis.y;
    }

    /**
     * Copies all 4 elements from the array argument, in row-major order.
     *
     * @param matrix the input array (not null, length=4, unaffected)
     * @return the (modified) current instance (for chaining)
     * @throws IllegalArgumentException if the array has length != 4
     */
    public Matrix2f set(float[] matrix) {
        return set(matrix, true);
    }

    /**
     * Copies all 4 elements from the specified array.
     *
     * @param matrix the input array (not null, length=4, unaffected)
     * @param rowMajor true to read the elements in row-major order (m00, m01,
     *     ...) or false to read them in column-major order (m00, m10, ...)
     * @return the (modified) current instance (for chaining)
     * @throws IllegalArgumentException if the array has length != 4
     */
    public Matrix2f set(float[] matrix, boolean rowMajor) {
        if (matrix.length != 4) {
            throw new IllegalArgumentException(
                    "Array must be of size 9.");
        }

        if (rowMajor) {
            m00 = matrix[0];
            m01 = matrix[1];
            m10 = matrix[2];
            m11 = matrix[3];
        } else {
            m00 = matrix[0];
            m01 = matrix[2];
            m10 = matrix[1];
            m11 = matrix[3];
        }
        return this;
    }

    /**
     * Configures as an identity matrix (diagonals = 1, other elements = 0).
     */
    public void loadIdentity() {
        m01 = m10 = 0;
        m00 = m11 = 1;
    }

    /**
     * Tests for exact identity. The matrix is unaffected.
     *
     * @return true if all diagonals = 1 and all other elements = 0 or -0,
     * otherwise false
     */
    public boolean isIdentity() {
        return (m00 == 1 && m01 == 0) && (m10 == 0 && m11 == 1);
    }

    /**
     * Sets all 4 elements to form a rotation matrix with the specified rotation
     * angle.
     *
     * @param angle the desired rotation angle (in radians)
     */
    public void fromAngle(float angle) {
        float fCos = FastMath.cos(angle);
        float fSin = FastMath.sin(angle);
        m00 = fCos;
        m01 = -fSin;
        m10 = fSin;
        m11 = fCos;
    }

    /**
     * Multiplies with the argument matrix and returns the product as a new
     * instance. The current instance is unaffected.
     *
     * <p>Note that matrix multiplication is noncommutative, so generally
     * q * p != p * q.
     *
     * @param mat the right factor (not null, unaffected)
     * @return {@code this} times {@code mat} (a new Matrix2f)
     */
    public Matrix2f mult(Matrix2f mat) {
        return mult(mat, null);
    }

    /**
     * Multiplies with the specified matrix and returns the product in a 3rd
     * matrix. The current instance is unaffected unless it's {@code product}.
     *
     * <p>Note that matrix multiplication is noncommutative, so generally
     * q * p != p * q.
     *
     * <p>It is safe for {@code mat} and {@code product} to be the same object.
     *
     * @param mat the right factor (not null, unaffected unless it's {@code
     *     product})
     * @param product storage for the product, or null for a new Matrix2f
     * @return {@code this} times {@code mat} (either {@code product} or a new
     *     Matrix2f)
     */
    public Matrix2f mult(Matrix2f mat, Matrix2f product) {
        float temp00, temp01;
        float temp10, temp11;

        if (product == null) {
            product = new Matrix2f();
        }
        temp00 = m00 * mat.m00 + m01 * mat.m10;
        temp01 = m00 * mat.m01 + m01 * mat.m11;
        temp10 = m10 * mat.m00 + m11 * mat.m10;
        temp11 = m10 * mat.m01 + m11 * mat.m11;

        product.m00 = temp00;
        product.m01 = temp01;
        product.m10 = temp10;
        product.m11 = temp11;

        return product;
    }

    /**
     * Applies the linear transformation to the vector argument and returns the
     * result as a new vector. The matrix is unaffected.
     *
     * <p>This can also be described as multiplying the matrix by a column
     * vector.
     *
     * @param vec the coordinates to transform (not null, unaffected)
     * @return a new Vector2f
     */
    public Vector2f mult(Vector2f vec) {
        return mult(vec, null);
    }

    /**
     * Applies the linear transformation to specified vector and stores the
     * result in another vector. The matrix is unaffected.
     *
     * <p>This can also be described as multiplying the matrix by a column
     * vector.
     *
     * <p>It is safe for {@code vec} and {@code product} to be the same object.
     *
     * @param vec the coordinates to transform (not null, unaffected unless it's
     *     {@code product})
     * @param product storage for the result, or null for a new Vector2f
     * @return either {@code product} or a new Vector2f
     */
    public Vector2f mult(Vector2f vec, Vector2f product) {
        if (null == product) {
            product = new Vector2f();
        }

        float x = vec.x;
        float y = vec.y;

        product.x = m00 * x + m01 * y;
        product.y = m10 * x + m11 * y;
        return product;
    }

    /**
     * Multiplies by the scalar argument and returns the (modified) current
     * instance.
     *
     * @param scale the scaling factor
     * @return the (modified) current instance (for chaining)
     */
    public Matrix2f multLocal(float scale) {
        m00 *= scale;
        m01 *= scale;
        m10 *= scale;
        m11 *= scale;
        return this;
    }

    /**
     * Applies the linear transformation to the vector argument and returns the
     * (modified) argument. If the argument is null, null is returned.
     *
     * <p>Despite the name, the current instance is unaffected.
     *
     * @param vec the vector to transform (modified if not null)
     * @return {@code vec} or null
     */
    public Vector2f multLocal(Vector2f vec) {
        if (vec == null) {
            return null;
        }
        float x = vec.x;
        float y = vec.y;
        vec.x = m00 * x + m01 * y;
        vec.y = m10 * x + m11 * y;
        return vec;
    }

    /**
     * Multiplies by the matrix argument and returns the (modified) current
     * instance.
     *
     * <p>Note that matrix multiplication is noncommutative, so generally
     * q * p != p * q.
     *
     * @param mat the right factor (not null, unaffected unless it's
     *     {@code this})
     * @return the (modified) current instance
     */
    public Matrix2f multLocal(Matrix2f mat) {
        return mult(mat, this);
    }

    /**
     * Transposes the matrix and returns the (modified) current instance.
     *
     * @return the (modified) current instance
     */
    public Matrix2f transposeLocal() {
        float tmp = m01;
        m01 = m10;
        m10 = tmp;
        return this;
    }

    /**
     * Returns the multiplicative inverse as a new matrix. If the current
     * instance is singular, an all-zero matrix is returned. In either case, the
     * current instance is unaffected.
     *
     * @return a new Matrix2f
     */
    public Matrix2f invert() {
        return invert(null);
    }

    /**
     * Returns the multiplicative inverse in the specified storage. If the
     * current instance is singular, an all-zero matrix is returned. In either
     * case, the current instance is unaffected.
     *
     * <p>If {@code this} and {@code store} are the same object, the result is
     * undefined. Use {@link #invertLocal()} instead.
     *
     * @param store storage for the result, or null for a new Matrix2f
     * @return either {@code store} or a new Matrix2f
     */
    public Matrix2f invert(Matrix2f store) {
        if (store == null) {
            store = new Matrix2f();
        }

        float det = determinant();
        if (FastMath.abs(det) <= FastMath.FLT_EPSILON) {
            return store.zero();
        }

        store.m00 = m00 * m11 - m01 * m10;
        store.m01 = m01 * m10 - m01 * m11;
        store.m10 = m01 * m10 - m10 * m11;
        store.m11 = m00 * m11 - m01 * m10;

        store.multLocal(1f / det);
        return store;
    }

    /**
     * Inverts the matrix and returns the (modified) current instance. If the
     * current instance is singular, all elements will be set to zero.
     *
     * @return the (modified) current instance (for chaining)
     */
    public Matrix2f invertLocal() {
        float det = determinant();
        if (FastMath.abs(det) <= 0f) {
            return zero();
        }

        float f00 = m00 * m11 - m01 * m10;
        float f01 = m01 * m10 - m01 * m11;
        float f10 = m01 * m10 - m10 * m11;
        float f11 = m00 * m11 - m01 * m10;

        m00 = f00;
        m01 = f01;
        m10 = f10;
        m11 = f11;

        multLocal(1f / det);
        return this;
    }

    /**
     * Returns the adjoint as a new matrix. The current instance is unaffected.
     *
     * @return a new Matrix2f
     */
    public Matrix2f adjoint() {
        return adjoint(null);
    }

    /**
     * Returns the adjoint in the specified storage. The current instance is
     * unaffected.
     *
     * <p>If {@code this} and {@code store} are the same object, the result is
     * undefined.
     *
     * @param store storage for the result, or null for a new Matrix2f
     * @return either {@code store} or a new Matrix2f
     */
    public Matrix2f adjoint(Matrix2f store) {
        if (store == null) {
            store = new Matrix2f();
        }

        store.m00 = m00 * m11 - m01 * m10;
        store.m01 = m01 * m10 - m01 * m11;
        store.m10 = m01 * m10 - m10 * m11;
        store.m11 = m00 * m11 - m01 * m10;
        return store;
    }

    /**
     * Returns the determinant. The matrix is unaffected.
     *
     * @return the determinant
     */
    public float determinant() {
        return m00 * m11 - m01 * m10;
    }

    /**
     * Sets all elements to zero.
     *
     * @return the (modified) current instance (for chaining)
     */
    public Matrix2f zero() {
        m00 = m01 = m10 = m11 = 0.0f;
        return this;
    }

    /**
     * Transposes the matrix and returns the new instance.
     */
    public Matrix2f transpose() {
        return new Matrix2f(m00, m10, m01, m11);
    }

    /**
     * Returns a string representation of the matrix, which is unaffected. For
     * example, the identity matrix is represented by:
     * <pre>
     * Matrix2f
     * [
     *  1.0  0.0
     *  0.0  1.0
     * ]
     * </pre>
     *
     * @return the string representation (not null, not empty)
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Matrix2f\n[\n");
        result.append(" ");
        result.append(m00);
        result.append("  ");
        result.append(m01);
        result.append(" \n");
        result.append(" ");
        result.append(m10);
        result.append("  ");
        result.append(m11);
        result.append(" \n]");
        return result.toString();
    }

    /**
     * Returns a hash code. If two matrices have identical values, they will
     * have the same hash code. The matrix is unaffected.
     *
     * @return a 32-bit value for use in hashing
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 37;
        hash = 37 * hash + Float.floatToIntBits(m00);
        hash = 37 * hash + Float.floatToIntBits(m01);
        hash = 37 * hash + Float.floatToIntBits(m10);
        hash = 37 * hash + Float.floatToIntBits(m11);
        return hash;
    }

    /**
     * Tests for exact equality with the argument, distinguishing -0 from 0. If
     * {@code o} is null, false is returned. Either way, the current instance is
     * unaffected.
     *
     * @param o the object to compare (may be null, unaffected)
     * @return true if {@code this} and {@code o} have identical values,
     *     otherwise false
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass()) {
            return false;
        }

        if (this == o) {
            return true;
        }

        Matrix2f comp = (Matrix2f) o;
        if (Float.compare(m00, comp.m00) != 0) {
            return false;
        }
        if (Float.compare(m01, comp.m01) != 0) {
            return false;
        }
        if (Float.compare(m10, comp.m10) != 0) {
            return false;
        }
        if (Float.compare(m11, comp.m11) != 0) {
            return false;
        }

        return true;
    }

    /**
     * Serializes to the specified exporter, for example when saving to a J3O
     * file. The current instance is unaffected.
     *
     * @param e the exporter to use (not null)
     * @throws IOException from the exporter
     */
    @Override
    public void write(JmeExporter e) throws IOException {
        OutputCapsule cap = e.getCapsule(this);
        cap.write(m00, "m00", 1);
        cap.write(m01, "m01", 0);
        cap.write(m10, "m10", 0);
        cap.write(m11, "m11", 1);
    }

    /**
     * De-serializes from the specified importer, for example when loading from a
     * J3O file.
     *
     * @param importer the importer to use (not null)
     * @throws IOException from the importer
     */
    @Override
    public void read(JmeImporter importer) throws IOException {
        InputCapsule cap = importer.getCapsule(this);
        m00 = cap.readFloat("m00", 1);
        m01 = cap.readFloat("m01", 0);
        m10 = cap.readFloat("m10", 0);
        m11 = cap.readFloat("m11", 1);
    }

    /**
     * Scales each column by the corresponding element of the argument.
     *
     * @param scale the scale factors: X scales column 0, Y scales column 1, Z
     *     scales column 2 (not null, unaffected)
     */
    public void scale(Vector2f scale) {
        m00 *= scale.x;
        m10 *= scale.x;
        m01 *= scale.y;
        m11 *= scale.y;
    }

    /**
     * Tests for an identity matrix, with 0.0001 tolerance. The current instance
     * is unaffected.
     *
     * @return true if all elements are within 0.0001 of an identity matrix
     */
    static boolean equalIdentity(Matrix2f mat) {
        if (Math.abs(mat.m00 - 1) > 1e-4) {
            return false;
        }
        if (Math.abs(mat.m11 - 1) > 1e-4) {
            return false;
        }

        if (Math.abs(mat.m01) > 1e-4) {
            return false;
        }

        if (Math.abs(mat.m10) > 1e-4) {
            return false;
        }

        return true;
    }

    /**
     * Creates a copy. The current instance is unaffected.
     *
     * @return a new instance, equivalent to the current one
     */
    @Override
    public Matrix2f clone() {
        try {
            return (Matrix2f) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // can not happen
        }
    }
}
