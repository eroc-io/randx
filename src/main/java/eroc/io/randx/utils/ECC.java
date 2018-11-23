package eroc.io.randx.utils;

import java.math.BigInteger;
import java.security.spec.ECPoint;

public class ECC {


    static BigInteger TWO = new BigInteger("2");
    //secp256k1
//    static BigInteger p = new BigInteger("115792089237316195423570985008687907853269984665640564039457584007908834671663");
//    static BigInteger a = new BigInteger("0");
//    static BigInteger b = new BigInteger("7");
    //secp256r1
    static BigInteger p = new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951");
    static BigInteger a = new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853948");
    static BigInteger b = new BigInteger("41058363725152142129326129780047268409114441015993725554835256314039467401291");

    /**
     * 相加
     * s+r=-p
     *
     * @param r
     * @param s
     * @return
     */
    public static ECPoint addPoint(ECPoint r, ECPoint s) {

        if (r.equals(s))
            return doublePoint(r);
        else if (r.equals(ECPoint.POINT_INFINITY))
            return s;
        else if (s.equals(ECPoint.POINT_INFINITY))
            return r;
        //m=((yr-ys）*(1/(xr-xs))%p)%p
        BigInteger slope = (r.getAffineY().subtract(s.getAffineY())).multiply(r.getAffineX().subtract(s.getAffineX()).modInverse(p)).mod(p);
        //xout=((m^2%p-xr-xs)%p
        BigInteger Xout = (slope.modPow(TWO, p).subtract(r.getAffineX())).subtract(s.getAffineX()).mod(p);
        //BigInteger Yout = r.getAffineY().negate().mod(p); - incorrect
        BigInteger Yout = s.getAffineY().negate().mod(p);
        //Yout = Yout.add(slope.multiply(r.getAffineX().subtract(Xout))).mod(p); - incorrect
        //yout=-ys%p+m*(xs-xout)%p
        Yout = Yout.add(slope.multiply(s.getAffineX().subtract(Xout))).mod(p);
        ECPoint out = new ECPoint(Xout, Yout);
        return out;
    }

    /**
     * 倍乘
     * PQ为同一点时，P+Q=-2P
     *
     * @param r
     * @return
     */
    public static ECPoint doublePoint(ECPoint r) {
        if (r.equals(ECPoint.POINT_INFINITY))
            return r;
        //m=xr^2*3+a*(1/2yr)%p
        BigInteger slope = (r.getAffineX().pow(2)).multiply(new BigInteger("3"));
        //slope = slope.add(new BigInteger("3")); - incorrect
        slope = slope.add(a);
        slope = slope.multiply((r.getAffineY().multiply(TWO)).modInverse(p));
        //xout=(m^2-(2*xr))%p
        BigInteger Xout = slope.pow(2).subtract(r.getAffineX().multiply(TWO)).mod(p);
        //yout=-yr
        BigInteger Yout = (r.getAffineY().negate()).add(slope.multiply(r.getAffineX().subtract(Xout))).mod(p);
        ECPoint out = new ECPoint(Xout, Yout);
        return out;
    }

    /**
     * 标量乘
     * 1P+2P+2^2P+2^3P+2^4P...+2^(k.bitlength-1)P
     *
     * @param P
     * @param kin
     * @return
     */
    public static ECPoint scalmult(ECPoint P, BigInteger kin) {
        //ECPoint R=P; - incorrect
        ECPoint R = ECPoint.POINT_INFINITY, S = P;
        BigInteger k = kin.mod(p);//k=sk%p    9
        int length = k.bitLength();//   4
//        System.out.println("length is " + length);
        byte[] binarray = new byte[length];
        for(int i = 0; i <= length - 1; i++) {
            binarray[i] = k.mod(TWO).byteValue();//b[0]=9%2=1*2^0   b[1]=4%2=0*2^1  b[2]=2%2=0*2^2  b[3]=1%2=1*2^3
            k = k.divide(TWO);//9/2=4   4/2=2   2/2=1   1/2=0
        }
//        for(int i = length - 1; i >= 0; i--) {
//            System.out.print("" + binarray[i]);
//        }

        for(int i = length - 1; i >= 0; i--) {
            // i should start at length-1 not -2 because the MSB of binarry may not be 1
            //倍加相当于左移一位, b[3]=P   b[2]=2P    b[1]=4P     b[0]=8P
            //P+P=-2P
            //2P+2P=-4P
            //4P+4P=-8P
            //......
            //b[3]=S  b[2]=2P+S  b[1]=4P+2P+S  b[0]=8P+4P+2P+S
            R = doublePoint(R);
            if (binarray[i] == 1)
                R = addPoint(R, S);
        }
        return R;
    }

}
