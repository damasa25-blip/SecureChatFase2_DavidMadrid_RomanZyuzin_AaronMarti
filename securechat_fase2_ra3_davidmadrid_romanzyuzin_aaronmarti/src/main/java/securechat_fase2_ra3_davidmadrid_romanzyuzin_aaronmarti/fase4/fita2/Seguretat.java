package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * FASE 4 - FITA 2
 * Clase de seguridad con cifrado AES/GCM + RSA para intercambio de claves
 * 
 * Características:
 * - AES/GCM para cifrado de mensajes (simétrico)
 * - RSA para intercambio seguro de claves (asimétrico)
 * - SHA-256 para hash e integridad
 * - Validación robusta de entradas
 */
public class Seguretat {
    
    // Configuración AES/GCM
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;
    
    // Configuración RSA
    private static final String RSA_ALGORITHM = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
    private static final int RSA_KEY_SIZE = 2048;
    
    // Configuración SHA-256
    private static final String HASH_ALGORITHM = "SHA-256";
    
    private SecretKey aesKey;
    private KeyPair rsaKeyPair;
    
    /**
     * Constructor vacío (para servidor que generará RSA)
     */
    public Seguretat() {
        // Se inicializará después
    }
    
    /**
     * Constructor con clave AES precompartida
     */
    public Seguretat(String base64Key) {
        if (base64Key == null || base64Key.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave no puede estar vacía");
        }
        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            this.aesKey = new SecretKeySpec(decodedKey, "AES");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Formato de clave inválido", e);
        }
    }
    
    /**
     * Constructor con objeto SecretKey
     */
    public Seguretat(SecretKey key) {
        if (key == null) {
            throw new IllegalArgumentException("La clave no puede ser null");
        }
        this.aesKey = key;
    }
    
    // ==================== MÉTODOS AES ====================
    
    /**
     * Genera una nueva clave AES-256
     */
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }
    
    /**
     * Establece la clave AES
     */
    public void setAESKey(SecretKey key) {
        if (key == null) {
            throw new IllegalArgumentException("La clave AES no puede ser null");
        }
        this.aesKey = key;
    }
    
    /**
     * Convierte clave AES a Base64
     */
    public static String keyToBase64(SecretKey key) {
        if (key == null) {
            throw new IllegalArgumentException("La clave no puede ser null");
        }
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    /**
     * Convierte Base64 a clave AES
     */
    public static SecretKey base64ToKey(String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave Base64 no puede estar vacía");
        }
        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64);
            return new SecretKeySpec(decodedKey, "AES");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Formato Base64 inválido", e);
        }
    }
    
    /**
     * Cifra texto con AES/GCM
     */
    public String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto a cifrar no puede estar vacío");
        }
        if (aesKey == null) {
            throw new IllegalStateException("Clave AES no inicializada");
        }
        
        try {
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);
            
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);
            
            byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] cipherBytes = cipher.doFinal(plainBytes);
            
            ByteBuffer byteBuffer = ByteBuffer.allocate(IV_SIZE + cipherBytes.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherBytes);
            
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new Exception("Error al cifrar: " + e.getMessage(), e);
        }
    }
    
    /**
     * Descifra texto con AES/GCM
     */
    public String decrypt(String cipherText) throws Exception {
        if (cipherText == null || cipherText.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto cifrado no puede estar vacío");
        }
        if (aesKey == null) {
            throw new IllegalStateException("Clave AES no inicializada");
        }
        
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            
            if (decodedBytes.length < IV_SIZE) {
                throw new IllegalArgumentException("Texto cifrado demasiado corto");
            }
            
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
            byte[] iv = new byte[IV_SIZE];
            byteBuffer.get(iv);
            
            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);
            
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);
            
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Exception("Error al descifrar: " + e.getMessage(), e);
        }
    }
    
    // ==================== MÉTODOS RSA ====================
    
    /**
     * Genera par de claves RSA (pública/privada)
     */
    public void generateRSAKeyPair() throws Exception {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(RSA_KEY_SIZE, new SecureRandom());
            this.rsaKeyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new Exception("Error al generar par de claves RSA: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene la clave pública RSA en Base64
     */
    public String getPublicKeyBase64() {
        if (rsaKeyPair == null) {
            throw new IllegalStateException("Par de claves RSA no generado");
        }
        return Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
    }
    
    /**
     * Cifra datos con clave pública RSA
     */
    public static String encryptRSA(String data, String publicKeyBase64) throws Exception {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("Los datos a cifrar no pueden estar vacíos");
        }
        if (publicKeyBase64 == null || publicKeyBase64.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave pública no puede estar vacía");
        }
        
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new Exception("Error al cifrar con RSA: " + e.getMessage(), e);
        }
    }
    
    /**
     * Descifra datos con clave privada RSA
     */
    public String decryptRSA(String encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            throw new IllegalArgumentException("Los datos cifrados no pueden estar vacíos");
        }
        if (rsaKeyPair == null) {
            throw new IllegalStateException("Par de claves RSA no generado");
        }
        
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Exception("Error al descifrar con RSA: " + e.getMessage(), e);
        }
    }
    
    // ==================== MÉTODOS SHA-256 ====================
    
    /**
     * Calcula hash SHA-256 de un texto
     */
    public static String calculateSHA256(String text) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto no puede estar vacío");
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new Exception("Error al calcular hash: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica si un hash coincide con un texto
     */
    public static boolean verifyHash(String text, String hashBase64) throws Exception {
        if (text == null || hashBase64 == null) {
            return false;
        }
        
        String calculatedHash = calculateSHA256(text);
        return calculatedHash.equals(hashBase64);
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Imprime información de cifrado
     */
    public void printInfo() {
        System.out.println("════════════════════════════════════════════");
        System.out.println("Información de Cifrado");
        System.out.println("════════════════════════════════════════════");
        System.out.println("AES: " + AES_ALGORITHM);
        System.out.println("Tamaño clave AES: " + AES_KEY_SIZE + " bits");
        System.out.println("RSA: " + RSA_ALGORITHM);
        System.out.println("Tamaño clave RSA: " + RSA_KEY_SIZE + " bits");
        System.out.println("Hash: " + HASH_ALGORITHM);
        System.out.println("════════════════════════════════════════════");
    }
}
