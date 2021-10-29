package zapsolutions.zap.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class EncryptionUtil {
    public static final String TAG = EncryptionUtil.class.getName();
    private static final String ENCRYPTION_ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC";

    /**
     * This functions encrypts a message using a password based AES Encryption.
     * <p>
     * It creates a cipher message that contains a 4 bit integer representing the iterations count for the password hashing, followed by a 12 bit salt, followed by a 16 bit initialization vector, followed by the encrypted message.
     * The random salt makes sure that even the use of the same password will always result in a different key. This will make pre-generated rainbow tables for brute force attacks impossible.
     * The random initialization vector makes sure that even with the same key the encrypted message will be different.
     * To also slow down brute force attacks, the function allows to define how many times (iterations) the password will be hashed. Use a value that is as high as possible without annoying the user.
     *
     * @param dataToEncrypt
     * @param password
     * @return
     */
    public static byte[] PasswordEncryptData(byte[] dataToEncrypt, String password, int iterations) {
        try {
            // convert iterations to byte array
            byte[] iterationsByte = UtilFunctions.intToByteArray(iterations);

            // generate salt
            byte[] salt = new byte[12];
            new SecureRandom().nextBytes(salt);

            // generate secret key
            SecretKey key = generateSecretKey(password.toCharArray(), salt, iterations);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);

            // generate initialization vector
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);

            // generate encrypted cipher message
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] encryptedData = cipher.doFinal(dataToEncrypt);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(iterationsByte);
            outputStream.write(salt);
            outputStream.write(iv);
            outputStream.write(encryptedData);
            return outputStream.toByteArray();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IOException | IllegalArgumentException e) {
            e.printStackTrace();
            ZapLog.e(TAG, "Error encrypting data.");
        }
        return null;
    }

    /**
     * This function decrypts a cypher message created with the PasswordEncryptData function.
     *
     * @param dataToDecrypt
     * @param password
     * @return
     */
    public static byte[] PasswordDecryptData(byte[] dataToDecrypt, String password) {
        try {
            // extract iterations, salt, initialization vector & message from dataToDecrypt
            int iterations = UtilFunctions.intFromByteArray(Arrays.copyOfRange(dataToDecrypt, 0, 4));
            byte[] salt = Arrays.copyOfRange(dataToDecrypt, 4, 16);
            byte[] iv = Arrays.copyOfRange(dataToDecrypt, 16, 32);
            byte[] message = Arrays.copyOfRange(dataToDecrypt, 32, dataToDecrypt.length);

            // generate secret key
            SecretKey key = generateSecretKey(password.toCharArray(), salt, iterations);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);

            // decrypt the message
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IllegalArgumentException e) {
            e.printStackTrace();
            ZapLog.e(TAG, "Error decrypting data.");
        }
        return null;
    }

    private static SecretKey generateSecretKey(char[] password, byte[] salt, int iterations) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalArgumentException {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, iterations);
        SecretKeyFactory secretKeyFactory;
        secretKeyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
        return secretKeyFactory.generateSecret(pbeKeySpec);
    }

}
