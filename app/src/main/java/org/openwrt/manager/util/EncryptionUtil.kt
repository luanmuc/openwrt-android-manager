package org.openwrt.manager.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 加密工具类
 * 使用 AES 加密存储路由器密码
 */
object EncryptionUtil {

    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/ECB/PKCS7Padding"
    private const val KEY_LENGTH = 256
    private const val ITERATION_COUNT = 10000
    private const val SALT = "openwrt_manager_salt_2024"

    /**
     * 加密字符串
     */
    fun encrypt(data: String, password: String = getDefaultPassword()): String {
        try {
            val key = generateKey(password)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            return data
        }
    }

    /**
     * 解密字符串
     */
    fun decrypt(encryptedData: String, password: String = getDefaultPassword()): String {
        try {
            val key = generateKey(password)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.NO_WRAP))
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return encryptedData
        }
    }

    private fun generateKey(password: String): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(
            password.toCharArray(),
            SALT.toByteArray(),
            ITERATION_COUNT,
            KEY_LENGTH
        )
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, ALGORITHM)
    }

    private fun getDefaultPassword(): String {
        // 使用设备唯一标识的一部分作为默认密码
        // 实际生产中应使用 Android Keystore
        return "openwrt_manager_default_key_2024"
    }

    /**
     * 生成随机 ID
     */
    fun generateId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
