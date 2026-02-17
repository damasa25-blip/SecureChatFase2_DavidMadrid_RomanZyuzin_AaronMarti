package securechat_fase2_ra3_davidmadrid_romanzyuzin_aaronmarti.fase3.fita3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FITA 3 - Script de Pruebas de Carga
 * Simula múltiples clientes conectándose y enviando mensajes simultáneamente
 * Utiliza ExecutorService para gestionar los clientes simulados
 */
public class LoadTestScript {
    private static final String HOST = "localhost";
    private static final int PORT = 12347;
    private static final Random RANDOM = new Random();
    
    // Contadores para estadísticas
    private static AtomicInteger successfulConnections = new AtomicInteger(0);
    private static AtomicInteger failedConnections = new AtomicInteger(0);
    private static AtomicInteger messagesSent = new AtomicInteger(0);
    private static AtomicInteger messagesReceived = new AtomicInteger(0);
    
    // Lista para almacenar tiempos de respuesta
    private static List<Long> responseTimes = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        printBanner();
        
        // Menú interactivo
        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            System.out.println("\n═══ CONFIGURACIÓN DE PRUEBA ═══");
            System.out.print("Número de clientes a simular [10]: ");
            String input = scanner.nextLine().trim();
            int numClients = input.isEmpty() ? 10 : Integer.parseInt(input);
            
            System.out.print("Mensajes por cliente [5]: ");
            input = scanner.nextLine().trim();
            int messagesPerClient = input.isEmpty() ? 5 : Integer.parseInt(input);
            
            System.out.print("Duración de la prueba en segundos [30]: ");
            input = scanner.nextLine().trim();
            int durationSeconds = input.isEmpty() ? 30 : Integer.parseInt(input);
            
            // Ejecutar prueba
            runLoadTest(numClients, messagesPerClient, durationSeconds);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void printBanner() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║          SCRIPT DE PRUEBAS DE CARGA - FITA 3         ║");
        System.out.println("║              SecureChat Server 1.0                    ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    /**
     * Ejecuta la prueba de carga
     */
    private static void runLoadTest(int numClients, int messagesPerClient, int durationSeconds) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("  INICIANDO PRUEBA DE CARGA");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("  Clientes: " + numClients);
        System.out.println("  Mensajes por cliente: " + messagesPerClient);
        System.out.println("  Duración: " + durationSeconds + " segundos");
        System.out.println("════════════════════════════════════════════════════════\n");

        // Crear pool de hilos para los clientes
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numClients);
        
        long startTime = System.currentTimeMillis();

        // Lanzar clientes simulados
        for (int i = 0; i < numClients; i++) {
            final int clientId = i + 1;
            executor.submit(() -> {
                try {
                    // Esperar señal de inicio
                    startLatch.await();
                    
                    // Ejecutar cliente simulado
                    runSimulatedClient(clientId, messagesPerClient, durationSeconds);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Dar señal de inicio a todos los clientes
        System.out.println("Lanzando " + numClients + " clientes simultáneos...\n");
        startLatch.countDown();

        // Esperar a que todos los clientes terminen
        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Cerrar executor
        executor.shutdown();

        // Imprimir resultados
        printResults(totalTime, numClients);
    }

    /**
     * Simula un cliente conectándose y enviando mensajes
     */
    private static void runSimulatedClient(int clientId, int numMessages, int durationSeconds) {
        String username = "User" + clientId;
        
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            successfulConnections.incrementAndGet();

            // Leer mensaje de bienvenida
            for (int i = 0; i < 5; i++) {
                input.readLine();
            }

            // LOGIN
            long loginStart = System.currentTimeMillis();
            output.println("LOGIN " + username);
            String response = input.readLine();
            long loginEnd = System.currentTimeMillis();
            responseTimes.add(loginEnd - loginStart);
            messagesReceived.incrementAndGet();

            if (!response.startsWith("OK")) {
                System.err.println("Error en LOGIN para " + username);
                return;
            }

            // Hilo para recibir mensajes
            Thread receiveThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = input.readLine()) != null) {
                        messagesReceived.incrementAndGet();
                    }
                } catch (IOException e) {
                    // Ignorar
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

            // Enviar mensajes
            long endTime = System.currentTimeMillis() + (durationSeconds * 1000);
            for (int i = 0; i < numMessages && System.currentTimeMillis() < endTime; i++) {
                String message = "Mensaje " + (i + 1) + " de " + username;
                
                long msgStart = System.currentTimeMillis();
                output.println("MSG " + message);
                messagesSent.incrementAndGet();
                long msgEnd = System.currentTimeMillis();
                responseTimes.add(msgEnd - msgStart);

                // Pausa aleatoria entre mensajes (100-500ms)
                Thread.sleep(100 + RANDOM.nextInt(400));
            }

            // QUIT
            output.println("QUIT");
            Thread.sleep(100);

        } catch (IOException e) {
            failedConnections.incrementAndGet();
            System.err.println("Error en cliente " + clientId + ": " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Imprime los resultados de la prueba
     */
    private static void printResults(long totalTime, int numClients) {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("           RESULTADOS DE LA PRUEBA DE CARGA            ");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("\n--- CONEXIONES ---");
        System.out.println("  Exitosas: " + successfulConnections.get());
        System.out.println("  Fallidas: " + failedConnections.get());
        System.out.println("  Total intentos: " + numClients);
        System.out.println("  Tasa de éxito: " + 
            String.format("%.2f%%", (successfulConnections.get() * 100.0 / numClients)));

        System.out.println("\n--- MENSAJES ---");
        System.out.println("  Enviados: " + messagesSent.get());
        System.out.println("  Recibidos: " + messagesReceived.get());
        System.out.println("  Mensajes/segundo: " + 
            String.format("%.2f", messagesSent.get() * 1000.0 / totalTime));

        System.out.println("\n--- RENDIMIENTO ---");
        System.out.println("  Tiempo total: " + totalTime + " ms (" + (totalTime / 1000.0) + " s)");
        
        if (!responseTimes.isEmpty()) {
            long avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .sum() / responseTimes.size();
            long minResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .min().orElse(0);
            long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max().orElse(0);

            System.out.println("  Tiempo de respuesta promedio: " + avgResponseTime + " ms");
            System.out.println("  Tiempo de respuesta mínimo: " + minResponseTime + " ms");
            System.out.println("  Tiempo de respuesta máximo: " + maxResponseTime + " ms");
        }

        System.out.println("\n--- USO DE RECURSOS ---");
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        
        System.out.println("  Memoria utilizada: " + usedMemory + " MB");
        System.out.println("  Memoria máxima: " + maxMemory + " MB");
        System.out.println("  Uso de memoria: " + 
            String.format("%.2f%%", usedMemory * 100.0 / maxMemory));

        System.out.println("\n════════════════════════════════════════════════════════");
        
        // Conclusión
        System.out.println("\n--- CONCLUSIÓN ---");
        if (failedConnections.get() == 0 && avgResponseTime(responseTimes) < 100) {
            System.out.println("  ✓ EXCELENTE: Todas las conexiones exitosas, tiempos de respuesta óptimos");
        } else if (failedConnections.get() < numClients * 0.1 && avgResponseTime(responseTimes) < 500) {
            System.out.println("  ✓ BUENO: La mayoría de conexiones exitosas, tiempos aceptables");
        } else {
            System.out.println("  ✗ NECESITA OPTIMIZACIÓN: Detectados problemas de rendimiento");
        }
        System.out.println();
    }

    private static long avgResponseTime(List<Long> times) {
        return times.isEmpty() ? 0 : 
            times.stream().mapToLong(Long::longValue).sum() / times.size();
    }
}
