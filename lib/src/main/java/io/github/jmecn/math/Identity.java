//package io.github.jmecn.math;
//
//import com.jme3.math.Vector2f;
//import com.jme3.math.Vector3f;
//import io.github.jmecn.text.BaseBounds;
//
//import java.awt.*;
//import java.awt.geom.Path2D;
//
///**
// *
// */
//public final class Identity extends BaseTransform {
//    @Override
//    public Degree getDegree() {
//        return Degree.IDENTITY;
//    }
//
//    @Override
//    public int getType() {
//        return TYPE_IDENTITY;
//    }
//
//    @Override
//    public boolean isIdentity() {
//        return true;
//    }
//
//    @Override
//    public boolean isTranslateOrIdentity() {
//        return true;
//    }
//
//    @Override
//    public boolean is2D() {
//        return true;
//    }
//
//    @Override
//    public double getDeterminant() {
//        return 1.0;
//    }
//
//    @Override
//    public Vector2f transform(Vector2f src, Vector2f dst) {
//        if (dst == null) {
//            dst = makePoint(src, dst);
//        }
//        dst.set(src);
//        return dst;
//    }
//
//    @Override
//    public Vector2f inverseTransform(Vector2f src, Vector2f dst) {
//        if (dst == null) {
//            dst = makePoint(src, dst);
//        }
//        dst.set(src);
//        return dst;
//    }
//
//    @Override
//    public Vector3f transform(Vector3f src, Vector3f dst) {
//        if (dst == null) {
//            return new Vector3f(src);
//        }
//        dst.set(src);
//        return dst;
//    }
//
//    @Override
//    public Vector3f deltaTransform(Vector3f src, Vector3f dst) {
//        if (dst == null) {
//            return new Vector3f(src);
//        }
//        dst.set(src);
//        return dst;
//    }
//
//    @Override
//    public Vector3f inverseTransform(Vector3f src, Vector3f dst) {
//        if (dst == null) {
//            return new Vector3f(src);
//        }
//        dst.set(src);
//        return dst;
//    }
//
//    @Override
//    public Vector3f inverseDeltaTransform(Vector3f src, Vector3f dst) {
//        if (dst == null) {
//            return new Vector3f(src);
//        }
//        dst.set(src);
//        return dst;
//    }
//
//    @Override
//    public void transform(float[] srcPts, int srcOff,
//                          float[] dstPts, int dstOff,
//                          int numPts)
//    {
//        if (srcPts != dstPts || srcOff != dstOff) {
//            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
//        }
//    }
//
//    @Override
//    public void transform(double[] srcPts, int srcOff,
//                          double[] dstPts, int dstOff,
//                          int numPts)
//    {
//        if (srcPts != dstPts || srcOff != dstOff) {
//            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
//        }
//    }
//
//    @Override
//    public void transform(float[] srcPts, int srcOff,
//                          double[] dstPts, int dstOff,
//                          int numPts)
//    {
//        for (int i = 0; i < numPts; i++) {
//            dstPts[dstOff++] = srcPts[srcOff++];
//            dstPts[dstOff++] = srcPts[srcOff++];
//        }
//    }
//
//    @Override
//    public void transform(double[] srcPts, int srcOff,
//                          float[] dstPts, int dstOff,
//                          int numPts)
//    {
//        for (int i = 0; i < numPts; i++) {
//            dstPts[dstOff++] = (float) srcPts[srcOff++];
//            dstPts[dstOff++] = (float) srcPts[srcOff++];
//        }
//    }
//
//    @Override
//    public void deltaTransform(float[] srcPts, int srcOff,
//                               float[] dstPts, int dstOff,
//                               int numPts)
//    {
//        if (srcPts != dstPts || srcOff != dstOff) {
//            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
//        }
//    }
//
//    @Override
//    public void deltaTransform(double[] srcPts, int srcOff,
//                               double[] dstPts, int dstOff,
//                               int numPts)
//    {
//        if (srcPts != dstPts || srcOff != dstOff) {
//            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
//        }
//    }
//
//    @Override
//    public void inverseTransform(float[] srcPts, int srcOff,
//                                 float[] dstPts, int dstOff,
//                                 int numPts)
//    {
//        if (srcPts != dstPts || srcOff != dstOff) {
//            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
//        }
//    }
//
//    @Override
//    public void inverseDeltaTransform(float[] srcPts, int srcOff,
//                                      float[] dstPts, int dstOff,
//                                      int numPts)
//    {
//        if (srcPts != dstPts || srcOff != dstOff) {
//            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
//        }
//    }
//
//    @Override
//    public void inverseTransform(double[] srcPts, int srcOff,
//                                 double[] dstPts, int dstOff,
//                                 int numPts)
//    {
//        if (srcPts != dstPts || srcOff != dstOff) {
//            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
//        }
//    }
//
//    @Override
//    public BaseBounds transform(BaseBounds bounds, BaseBounds result) {
//        if (result != bounds) {
//            result = result.deriveWithNewBounds(bounds);
//        }
//        return result;
//    }
//
//    @Override
//    public void transform(Rectangle rect, Rectangle result) {
//        if (result != rect) {
//            result.setBounds(rect);
//        }
//    }
//
//    @Override
//    public BaseBounds inverseTransform(BaseBounds bounds, BaseBounds result) {
//        if (result != bounds) {
//            result = result.deriveWithNewBounds(bounds);
//        }
//        return result;
//    }
//
//    @Override
//    public void inverseTransform(Rectangle rect, Rectangle result) {
//        if (result != rect) {
//            result.setBounds(rect);
//        }
//    }
//
//    @Override
//    public Shape createTransformedShape(Shape s) {
//        // TODO: Can we just return s? (RT-26884)
//        return s;
//    }
//
//    @Override
//    public void setToIdentity() {
//    }
//
//    @Override
//    public void setTransform(BaseTransform xform) {
//        if (!xform.isIdentity()) {
//            degreeError(Degree.IDENTITY);
//        }
//    }
//
//    @Override
//    public void invert() {
//    }
//
//    @Override
//    public void restoreTransform(double mxx, double myx,
//                                 double mxy, double myy,
//                                 double mxt, double myt)
//    {
//        if (mxx != 1.0 || myx != 0.0 ||
//            mxy != 0.0 || myy != 1.0 ||
//            mxt != 0.0 || myt != 0.0)
//        {
//            degreeError(Degree.IDENTITY);
//        }
//    }
//
//    @Override
//    public void restoreTransform(double mxx, double mxy, double mxz, double mxt,
//                                 double myx, double myy, double myz, double myt,
//                                 double mzx, double mzy, double mzz, double mzt)
//    {
//        if (mxx != 1.0 || mxy != 0.0 || mxz != 0.0 || mxt != 0.0 ||
//            myx != 0.0 || myy != 1.0 || myz != 0.0 || myt != 0.0 ||
//            mzx != 0.0 || mzy != 0.0 || mzz != 1.0 || mzt != 0.0)
//        {
//            degreeError(Degree.IDENTITY);
//        }
//    }
//
//    @Override
//    public BaseTransform deriveWithTranslation(double mxt, double myt) {
//        return Translate2D.getInstance(mxt, myt);
//    }
//
//    @Override
//    public BaseTransform deriveWithPreTranslation(double mxt, double myt) {
//        return Translate2D.getInstance(mxt, myt);
//    }
//
//    @Override
//    public BaseTransform deriveWithTranslation(double mxt, double myt, double mzt) {
//        if (mzt == 0.0) {
//            if (mxt == 0.0 && myt == 0.0) {
//                return this;
//            }
//            return new Translate2D(mxt, myt);
//        }
//        Affine3D a = new Affine3D();
//        a.translate(mxt, myt, mzt);
//        return a;
//    }
//
//    @Override
//    public BaseTransform deriveWithScale(double mxx, double myy, double mzz) {
//        if (mzz == 1.0) {
//            if (mxx == 1.0 && myy == 1.0) {
//                return this;
//            }
//            Affine2D a = new Affine2D();
//            a.scale(mxx, myy);
//            return a;
//        }
//        Affine3D a = new Affine3D();
//        a.scale(mxx, myy, mzz);
//        return a;
//
//    }
//
//    @Override
//    public BaseTransform deriveWithRotation(double theta,
//            double axisX, double axisY, double axisZ) {
//        if (theta == 0.0) {
//            return this;
//        }
//        if (almostZero(axisX) && almostZero(axisY)) {
//            if (axisZ == 0.0) {
//                return this;
//            }
//            Affine2D a = new Affine2D();
//            if (axisZ > 0) {
//                a.rotate(theta);
//            } else if (axisZ < 0) {
//                a.rotate(-theta);
//            }
//            return a;
//        }
//        Affine3D a = new Affine3D();
//        a.rotate(theta, axisX, axisY, axisZ);
//        return a;
//    }
//
//    @Override
//    public BaseTransform deriveWithConcatenation(double mxx, double myx,
//                                                 double mxy, double myy,
//                                                 double mxt, double myt)
//    {
//        return getInstance(mxx, myx,
//                           mxy, myy,
//                           mxt, myt);
//    }
//
//    @Override
//    public BaseTransform deriveWithConcatenation(
//            double mxx, double mxy, double mxz, double mxt,
//            double myx, double myy, double myz, double myt,
//            double mzx, double mzy, double mzz, double mzt) {
//        return getInstance(mxx, mxy, mxz, mxt,
//                           myx, myy, myz, myt,
//                           mzx, mzy, mzz, mzt);
//    }
//
//    @Override
//    public BaseTransform deriveWithConcatenation(BaseTransform tx) {
//        return getInstance(tx);
//    }
//
//    @Override
//    public BaseTransform deriveWithPreConcatenation(BaseTransform tx) {
//        return getInstance(tx);
//    }
//
//    @Override
//    public BaseTransform deriveWithNewTransform(BaseTransform tx) {
//        return getInstance(tx);
//    }
//
//    @Override
//    public BaseTransform createInverse() {
//        return this;
//    }
//
//    @Override
//    public String toString() {
//        return ("Identity[]");
//    }
//
//    @Override
//    public BaseTransform copy() {
//        return this;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        return (obj instanceof BaseTransform &&
//                ((BaseTransform) obj).isIdentity());
//    }
//
//    @Override
//    public int hashCode() {
//        return 0;
//    }
//}