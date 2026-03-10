package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * FASE 4 - FITA 1
 * Clase de seguridad para cifrado simétrico AES/GCM
 * 
 * AES (Advanced Encryption Standard) con modo GCM (Galois/Counter Mode)
 * - GCM proporciona autenticación y cifrado
 * - Tamaño de clave: 256 bits
 * - Tamaño de IV: 12 bytes (recomendado por NIST)
 * - Tamaño de Tag: 128 bits
 */
public class Seguretat {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256; // bits
    private static final int IV_SIZE = 12; // bytes
    private static final int TAG_SIZE = 128; // bits
    
    private SecretKey secretKey;
    
    /**
     * Constructor con clave precompartida
     * @param key Clave AES en formato Base64
     */
    public Seguretat(String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        this.secretKey = new SecretKeySpec(decodedKey, "AES");
    }
    
    /**
     * Constructor con objeto SecretKey
     */
    public Seguretat(SecretKey key) {
        this.secretKey = key;
    }
    
    /**
     * Genera una nueva clave AES-256
     * @return Clave AES
     */
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }
    
    /**
     * Convierte una clave a Base64 para transmisión
     * @param key Clave a convertir
     * @return String en Base64
     */
    public static String keyToBase64(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    /**
     * Cifra un texto plano usando AES/GCM
     * 
     * Formato del resultado:
     * [IV de 12 bytes] + [Texto cifrado + Tag de autenticación]
     * 
     * @param plainText Texto a cifrar
     * @return Texto cifrado en Base64
     */
    public String encrypt(String plainText) throws Exception {
        // Generar IV aleatorio
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        
        // Configurar GCM
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
        
        // Cifrar
        byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] cipherBytes = cipher.doFinal(plainBytes);
        
        // Combinar IV + CipherText
        ByteBuffer byteBuffer = ByteBuffer.allocate(IV_SIZE + cipherBytes.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherBytes);
        
        // Retornar en Base64
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }
    
    /**
     * Descifra un texto cifrado con AES/GCM
     * 
     * @param cipherText Texto cifrado en Base64
     * @return Texto plano
     */
    public String decrypt(String cipherText) throws Exception {
        // Decodificar de Base64
        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        
        // Extraer IV y texto cifrado
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
        byte[] iv = new byte[IV_SIZE];
        byteBuffer.get(iv);
        
        byte[] cipherBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherBytes);
        
        // Configurar GCM
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
        
        // Descifrar
        byte[] plainBytes = cipher.doFinal(cipherBytes);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Método de utilidad para mostrar información de cifrado
     */
    public void printInfo() {
        System.out.println("════════════════════════════════════════════");
        System.out.println("Información de Cifrado AES/GCM");
        System.out.println("════════════════════════════════════════════");
        System.out.println("Algoritmo: " + ALGORITHM);
        System.out.println("Tamaño de clave: " + KEY_SIZE + " bits");
        System.out.println("Tamaño de IV: " + IV_SIZE + " bytes");
        System.out.println("Tamaño de Tag: " + TAG_SIZE + " bits");
        System.out.println("════════════════════════════════════════════");
    }
}
