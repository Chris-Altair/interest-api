package pers.interest.util;

import com.lowagie.text.pdf.codec.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtils {
    private final static String ENCRYPTION_ALGORITHM = "RSA";
    private final static String SIGN_ALGORITHM = "SHA256withRSA";

    public static PublicKey getPubKey(String pubKeyEncode) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pk = pubKeyEncode.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("\n", "")
                .replace("-----END PUBLIC KEY-----", "");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(pk));
        KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey getPriKey(String priKeyEncode) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pk = priKeyEncode.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "");
        KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(pk));
        KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 加密（可选择公钥加密或私钥加密）
     */
    public static String encrypt(String plaintext, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        byte[] plaintextEncode = plaintext.getBytes("UTF-8");
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        String ciphertext = Base64.encodeBytes(cipher.doFinal(plaintextEncode));
        return ciphertext;
    }

    /**
     * 解密（可选择公钥解密或私钥解密）
     */
    public static String decrypt(String ciphertext, Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        String plaintext = new String(cipher.doFinal(Base64.decode(ciphertext)));
        return plaintext;
    }

    /**
     * 私钥签名
     */
    public static String sign(String text, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
        byte[] data = text.getBytes("UTF-8");
        /* 使用私钥生成签名 */
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);
        byte[] sign = signature.sign();
        return Base64.encodeBytes(sign);
    }

    /**
     * 公钥验签
     */
    public static boolean verify(String text, String signEncode,PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
        byte[] data = text.getBytes("UTF-8");
        byte[] sign = Base64.decode(signEncode);
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(sign);
    }

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException {
        String text = "我是谁";
        String privateKeyEncode = "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQD3IaZG32z4tdnO\n" +
                "msrTtmd0hv8fdtCgb1CUUS5q/WNfseZhlEgEd0V+z5cS7rrnBxWVTIxCZ5yuDo4O\n" +
                "RPRA/bPxZwhbCRqMoM9tJ+k5go/ASxMU0m5BNocDor3SyWzsA++bXdKd1B6EzjVY\n" +
                "cRfXqi5AUj3nEVyOqBwyrWijubF8zF+aPVItWxLsh3phX4FcNs8xEi7um3AP7SXg\n" +
                "PQg9L54QqzFnU1IWn9L1jLxNr4Apv5l17q1Lrw7vPyOgx8uM0NwbOSxSYuQ86ohH\n" +
                "EYn+T+einimgeeiwuNwlzWaivz9Nhgse5kwuVAuzTliHdoHtedkbPpsQ4Q7d4a60\n" +
                "4ypkRFiDAgMBAAECggEBALIh0B/P1lVjhw7UJpT6KpIk6Go+k1zBP9zYs1OtN173\n" +
                "9W6qmkqVykMA0Q11hMUYJyeJmKQY2SfhF+YTL9hUxsqfkLdF1Zw+IaW1mfe5M3LQ\n" +
                "6n0cvRpGeoVVXew9AkURBToBI9pd1m210V/hjOUIJvR4E2Sl8AhoBoNE2WMCa3ve\n" +
                "zgMqV5J7DJHX10NHHUsrOlwLNHLDhUu+J7yAYIQriA4VMlKsuKMFvsvzS5WzxErm\n" +
                "lv6bh6L8ZLVfuIuOLL1GFBBOTStDvhrtx6+Tb1EXkUkz6Mio4bekCqR7zjqf85uZ\n" +
                "Fu8hsxO26C8GXMwoLVpgoxDNR1RXu/53RYb0BPxYR3kCgYEA/BM5tyETMoAt4+EY\n" +
                "fc2PXymJNGtRSSF4h4OdsDpKUWgS4r+46tSV7tr1VxHBV0mGWsQmERpTyY4yYScv\n" +
                "uXtdVGQx5LVmq5DVwiS/p2aADNUxInKOr+oPkl2AGtP3hxCDSUjZ6LO+vGp4YHWL\n" +
                "6R6EWiZMQ9iPhX1jLZBq3rB9Yt0CgYEA+vq39bJd2fgxBjO5F2luix/MpskciFHE\n" +
                "K60SEx0IUUL4x09CHdxrkY86u/P189xz09u4aoRj3342w8TIQGUZMd83MxkplXvr\n" +
                "vzTSDnFXXzIsk7PwUFdtUpokzLPJL+GuiKEasx+SioBEMS+ivYPzf84l+uOLeNYX\n" +
                "KM1R6VdDgt8CgYEApmMeVqXrYQtAv0lmfCvCdkMZgNNRvV7tiy2yWY+wXJnA1ZFN\n" +
                "wxv9t8Mp7IYYw8ARgjo1Elb5dXJ6CKzwEJPAoCLe2CNxGNAqeNL++sVqwo26uA07\n" +
                "6BwsmSVEmPDPU4Tv0+DVjYeP4Bi7SphseL0fCc57ytDYs3l3jKzmDgYbTbUCgYEA\n" +
                "5hyqPiNj2U2t5TnXIpgQOo7VXLZX6sTNUB5GzC96SMdWwabxv98w4SypdVqgzw35\n" +
                "RO3XkBICYA+gtq3dzOzkxM0cF0Wi0uPjqlQLpCJXMXkXVfVNCNyf53Rc+TJ96+DH\n" +
                "3Y2YpVL1UPd5jn9WftxUjYG9YOLwJcJlSYu25nKT+5ECgYAnINUeczCbU5Nxw8nO\n" +
                "1GWNDG2+IX+NbFmAL1cimNeA6wxm7AaFcu4IkUgz9Axyjd0OqGa0lg8WpmvZQkb7\n" +
                "G66S16EiN+L4hCrl3KX/4J9FutwFoGGQeZgr4DwScWOE/Q4RSDKEY5MlYN7vv430\n" +
                "V2RvgnaZ1mYcwaQNsa76YJSBXA==\n" +
                "-----END PRIVATE KEY-----\n";
        String publicKeyEncode = "-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA9yGmRt9s+LXZzprK07Zn\n" +
                "dIb/H3bQoG9QlFEuav1jX7HmYZRIBHdFfs+XEu665wcVlUyMQmecrg6ODkT0QP2z\n" +
                "8WcIWwkajKDPbSfpOYKPwEsTFNJuQTaHA6K90sls7APvm13SndQehM41WHEX16ou\n" +
                "QFI95xFcjqgcMq1oo7mxfMxfmj1SLVsS7Id6YV+BXDbPMRIu7ptwD+0l4D0IPS+e\n" +
                "EKsxZ1NSFp/S9Yy8Ta+AKb+Zde6tS68O7z8joMfLjNDcGzksUmLkPOqIRxGJ/k/n\n" +
                "op4poHnosLjcJc1mor8/TYYLHuZMLlQLs05Yh3aB7XnZGz6bEOEO3eGutOMqZERY\n" +
                "gwIDAQAB\n" +
                "-----END PUBLIC KEY-----\n";

        try {
            PrivateKey priKey = getPriKey(privateKeyEncode);
            PublicKey pubKey = getPubKey(publicKeyEncode);
            String c = encrypt(text,priKey);
            System.out.println("密文 = " + c);
            String p = decrypt(c,pubKey);
            System.out.println("明文 = " + p);

            String s = sign(text,priKey);
            System.out.println("签名 = " + s);
            System.out.println("验证签名 = " + verify(text,s,pubKey));
//            byte[] textEncode = text.getBytes("UTF-8");
//            PublicKey pubKey = getPubKey(publicKeyEncode);
//            PrivateKey priKey = getPriKey(privateKeyEncode);
//            //RSA公钥加密
//            Cipher cipher = Cipher.getInstance("RSA");
//            cipher.init(Cipher.ENCRYPT_MODE, priKey);
//            String ciphertext = Base64.encodeBytes(cipher.doFinal(textEncode));
//            System.out.println("ciphertext = " + ciphertext);
//            //RSA私钥解密
//            cipher.init(Cipher.DECRYPT_MODE, pubKey);
//            String txt = new String(cipher.doFinal(Base64.decode(ciphertext)));
//            System.out.println("txt = " + txt);

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

    }
}
