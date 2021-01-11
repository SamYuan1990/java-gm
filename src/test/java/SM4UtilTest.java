import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Sean
 * @Description: 国密算法SM4工具类测试
 * @date 2020/9/18
 */

@RunWith(Parameterized.class)
public class SM4UtilTest {
    private static byte[] content = null;
    private static byte[] content16 = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8};
    //private static byte[] key = null;
    private static byte[] iv = null;
    private static SM4Util.SM4ModeAndPaddingEnum type;
    static int randomData = 128;
    static String message = RandomStringUtils.random(randomData);
    static String exceptionHappened = "Exception happened";

    @Parameters(name = "{index}: sm4({1})")
    public static Collection prepareData() throws NoSuchProviderException, NoSuchAlgorithmException {
        Object[][] object = {
                {content16, SM4Util.SM4ModeAndPaddingEnum.SM4_ECB_NoPadding, false},
                {message.getBytes(Charset.forName("utf8")), SM4Util.SM4ModeAndPaddingEnum.SM4_ECB_PKCS5Padding, false},
                {message.getBytes(Charset.forName("utf8")), SM4Util.SM4ModeAndPaddingEnum.SM4_ECB_PKCS7Padding, false},
                {content16, SM4Util.SM4ModeAndPaddingEnum.SM4_CBC_NoPadding, true},
                {message.getBytes(Charset.forName("utf8")), SM4Util.SM4ModeAndPaddingEnum.SM4_CBC_PKCS5Padding, true},
                {message.getBytes(Charset.forName("utf8")), SM4Util.SM4ModeAndPaddingEnum.SM4_CBC_PKCS7Padding, true}
        };
        return Arrays.asList(object);
    }

    public SM4UtilTest(byte[] content, SM4Util.SM4ModeAndPaddingEnum type, boolean flag) throws NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.content = content;
        this.type = type;
        if (flag) {
            SM4Util instance = new SM4Util();
            this.iv = instance.generateKey();
        }
    }

    @Test
    public void testingSM4() throws Exception {
        SM4Util instance = new SM4Util();
        byte[] key = instance.generateKey();
        System.out.println("===== " + type + " =====");
        // 加密
        byte[] v = instance.encrypt(content, key, type, iv);
        // 解密
        byte[] c = instance.decrypt(v, key, type, iv);

        System.out.println("解密内容：" + new String(c));
        Assert.assertArrayEquals(c, content);
    }

    @Test
    public void threadsafe() throws Exception {
        Queue<byte[]> results = new ConcurrentLinkedQueue<byte[]>();
        Queue<Exception> ex = new ConcurrentLinkedQueue<Exception>();

        SM4Util instance = new SM4Util();
        byte[] key = instance.generateKey();
        System.out.println("===== " + type + " =====");
        // 加密
        byte[] v = instance.encrypt(content, key, type, iv);
        // 解密
        byte[] c = instance.decrypt(v, key, type, iv);

        System.out.println("解密内容：" + new String(c));
        Assert.assertArrayEquals(c, content);
        for (int i = 0; i < 300; i++) {
            new Thread(() -> {
                try {
                    results.add(instance.decrypt(v, key, type, iv));
                } catch (Exception e) {
                    ex.add(e);
                }
            }).start();
        }
        Thread.sleep(5000);
        while (!ex.isEmpty()) {
            Exception e =  ex.poll();
            e.printStackTrace();
            Assert.fail(exceptionHappened);
        }
        Assert.assertTrue(results.size() == 300);
        while (!results.isEmpty()) {
            Assert.assertArrayEquals(results.poll(), content);
        }
    }
}
