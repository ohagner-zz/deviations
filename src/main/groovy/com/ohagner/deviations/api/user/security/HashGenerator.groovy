package com.ohagner.deviations.api.user.security

import com.google.common.base.Stopwatch
import groovy.util.logging.Slf4j

import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.concurrent.TimeUnit

@Slf4j
class HashGenerator {

    private static final int SALT_SIZE = 64
    private static final int KEY_LENGTH = 256
    private static final int ITERATIONS = 10000

    static String generateHash(String password, String salt) {
        Stopwatch timer = Stopwatch.createStarted()

        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
            PBEKeySpec spec = new PBEKeySpec(password as char[], salt as byte[], ITERATIONS, KEY_LENGTH)
            SecretKey key = skf.generateSecret(spec)
            byte[] hash = key.getEncoded()
            log.debug "Password hashing took ${timer.elapsed(TimeUnit.MILLISECONDS)} milliseconds with $ITERATIONS iterations"
            return String.format("%h", new BigInteger(hash))
        } catch( NoSuchAlgorithmException | InvalidKeySpecException e ) {
            throw new RuntimeException(e);
        }

    }

    static String createSalt(int size=SALT_SIZE) {
        SecureRandom random = new SecureRandom()
        byte[] salt = new byte[size]
        random.nextBytes(salt)
        return String.format("%h", new BigInteger(salt))
    }

}
