package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase4.fita1;

import java.util.Scanner;

import javax.crypto.SecretKey;

/**
 * FASE 4 - FITA 1
 * Mini-Laboratorio de Criptografía AES/GCM
 * 
 * Este programa demuestra:
 * 1. Generación de claves AES
 * 2. Cifrado de texto con AES/GCM
 * 3. Descifrado de texto cifrado
 * 4. Formato Base64 para transmisión
 * 
 * Conceptos clave:
 * - AES: Advanced Encryption Standard (cifrado simétrico)
 * - GCM: Galois/Counter Mode (proporciona autenticación y cifrado)
 * - IV: Initialization Vector (debe ser único para cada mensaje)
 * - Tag: Authentication Tag (verifica integridad del mensaje)
 */
public class MiniLaboratorioAES {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        printBanner();
        
        try {
            // PASO 1: Generar clave AES
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("PASO 1: Generación de Clave AES-256");
            System.out.println("══════════════════════════════════════════════════════════");
            
            SecretKey secretKey = Seguretat.generateKey();
            String keyBase64 = Seguretat.keyToBase64(secretKey);
            
            System.out.println("✓ Clave generada exitosamente");
            System.out.println("Tamaño: 256 bits (32 bytes)");
            System.out.println("Formato Base64: " + keyBase64);
            System.out.println("\nℹ️  En producción, esta clave debe guardarse de forma segura");
            System.out.println("   y nunca debe compartirse en texto plano.");
            
            // Crear objeto de seguridad con la clave
            Seguretat seguretat = new Seguretat(secretKey);
            
            // PASO 2: Cifrado
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("PASO 2: Cifrado de Texto");
            System.out.println("══════════════════════════════════════════════════════════");
            
            System.out.print("Introduce un mensaje para cifrar: ");
            String plainText = scanner.nextLine();
            
            System.out.println("\n📝 Texto original: \"" + plainText + "\"");
            System.out.println("Longitud: " + plainText.length() + " caracteres");
            
            String cipherText = seguretat.encrypt(plainText);
            
            System.out.println("\n🔒 Texto cifrado (Base64):");
            System.out.println(cipherText);
            System.out.println("\nLongitud cifrada: " + cipherText.length() + " caracteres");
            System.out.println("\nℹ️  El texto cifrado incluye:");
            System.out.println("   - IV (12 bytes): Vector de inicialización único");
            System.out.println("   - Texto cifrado: Contenido encriptado");
            System.out.println("   - Tag (16 bytes): Autenticación GCM");
            
            // PASO 3: Descifrado
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("PASO 3: Descifrado de Texto");
            System.out.println("══════════════════════════════════════════════════════════");
            
            System.out.println("Descifrando el mensaje...\n");
            String decryptedText = seguretat.decrypt(cipherText);
            
            System.out.println("🔓 Texto descifrado: \"" + decryptedText + "\"");
            
            // Verificar integridad
            if (plainText.equals(decryptedText)) {
                System.out.println("✅ ¡Éxito! El texto descifrado coincide con el original");
            } else {
                System.out.println("❌ Error: Los textos no coinciden");
            }
            
            // PASO 4: Demostración de seguridad
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("PASO 4: Demostración de Seguridad GCM");
            System.out.println("══════════════════════════════════════════════════════════");
            
            System.out.println("\nIntentando descifrar con clave incorrecta...");
            
            try {
                // Generar nueva clave (incorrecta)
                SecretKey wrongKey = Seguretat.generateKey();
                Seguretat wrongSeguretat = new Seguretat(wrongKey);
                
                // Intentar descifrar con clave incorrecta
                wrongSeguretat.decrypt(cipherText);
                
                System.out.println("❌ No debería llegar aquí");
                
            } catch (Exception e) {
                System.out.println("✅ Error esperado: " + e.getClass().getSimpleName());
                System.out.println("   GCM detectó que la clave es incorrecta o el mensaje fue alterado");
                System.out.println("   Esto garantiza la integridad y autenticidad del mensaje");
            }
            
            // PASO 5: Cifrado múltiple
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("PASO 5: Cada Cifrado es Único (IV Aleatorio)");
            System.out.println("══════════════════════════════════════════════════════════");
            
            String message = "Mismo mensaje";
            System.out.println("\nCifrando el mismo mensaje 3 veces:\n");
            
            for (int i = 1; i <= 3; i++) {
                String encrypted = seguretat.encrypt(message);
                System.out.println("Cifrado " + i + ": " + encrypted.substring(0, Math.min(40, encrypted.length())) + "...");
            }
            
            System.out.println("\nℹ️  Cada cifrado es diferente debido al IV aleatorio único");
            System.out.println("   Esto previene ataques de análisis de patrones");
            
            // Resumen final
            System.out.println("\n══════════════════════════════════════════════════════════");
            System.out.println("RESUMEN DE CONCEPTOS");
            System.out.println("══════════════════════════════════════════════════════════");
            System.out.println("✓ AES-256: Estándar de cifrado avanzado con clave de 256 bits");
            System.out.println("✓ GCM: Modo que proporciona cifrado + autenticación");
            System.out.println("✓ IV: Vector aleatorio único por cada mensaje");
            System.out.println("✓ Tag: Verifica que el mensaje no ha sido alterado");
            System.out.println("✓ Base64: Formato seguro para transmitir datos binarios");
            System.out.println("\n🎓 ¡Laboratorio completado exitosamente!");
            
        } catch (Exception e) {
            System.err.println("\n❌ Error en el laboratorio: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                          ║");
        System.out.println("║        MINI-LABORATORIO DE CRIPTOGRAFÍA AES/GCM         ║");
        System.out.println("║                   FASE 4 - FITA 1                        ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("\n🔐 Este laboratorio demuestra el funcionamiento de:");
        System.out.println("   • Cifrado simétrico AES-256");
        System.out.println("   • Modo GCM (Galois/Counter Mode)");
        System.out.println("   • Generación de claves");
        System.out.println("   • Proceso de cifrado/descifrado");
        System.out.println("   • Autenticación de mensajes");
    }
}
