package com.example.recipemanager.benchmark;

import com.example.recipemanager.dto.LoginRequest;
import com.example.recipemanager.dto.RecipeRequest;
import com.example.recipemanager.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * Simple Java latency benchmark for Recipe Manager API.
 *
 * Automatically saves baseline on first run and compares on second run.
 * Run BEFORE and AFTER performance changes to verify 40% latency drop.
 *
 * Usage:
 *   mvn exec:java -Dexec.mainClass="com.example.recipemanager.benchmark.LatencyBenchmark"
 */
public class LatencyBenchmark {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final Path BASELINE_FILE = Paths.get("baseline.json");

    private final String baseUrl;
    private final int iterations;
    private final List<EndpointResult> results = new ArrayList<>();

    private static String testEmail;
    private static String testPassword;
    private static RecipeRequest testRecipe;

    public LatencyBenchmark(String baseUrl, int iterations) {
        this.baseUrl = baseUrl;
        this.iterations = iterations;
        initTestData();
    }

    private static void initTestData() {
        testEmail = "bench_" + System.currentTimeMillis() + "@test.com";
        testPassword = "bench123456";
        testRecipe = new RecipeRequest(
                "Benchmark Recipe",
                "Test recipe for latency benchmarking",
                List.of("item1", "item2"),
                15,
                "Benchmark"
        );
    }

    public static void main(String[] args) throws Exception {
        String baseUrl = args.length > 0 ? args[0] : "http://localhost:8080";
        int iterations = args.length > 1 ? Integer.parseInt(args[1]) : 10;

        System.out.println("============================================");
        System.out.println("  RECIPE MANAGER - LATENCY BENCHMARK");
        System.out.println("  Base URL:   " + baseUrl);
        System.out.println("  Iterations: " + iterations);
        System.out.println("============================================");

        LatencyBenchmark bench = new LatencyBenchmark(baseUrl, iterations);

        if (!bench.isServerUp()) {
            System.err.println("  [ERROR] Server is not reachable at " + baseUrl);
            System.err.println("  Start the app first: docker compose up -d");
            System.exit(1);
        }
        System.out.println("  Server is UP!\n");

        String token = bench.registerAndLogin();
        if (token == null) {
            System.err.println("  [ERROR] Could not authenticate");
            System.exit(1);
        }
        System.out.println("  Authenticated successfully\n");

        String recipeId = bench.createTestRecipe(token);

        System.out.println(">> Running benchmarks (" + iterations + " iterations each)...\n");

        bench.benchmarkEndpoint("Health Check",       "GET",  "/api/health",                    null,   null);
        bench.benchmarkEndpoint("Login",              "POST", "/api/auth/login",                null,   new LoginRequest(testEmail, testPassword));

        if (token != null) {
            bench.benchmarkEndpoint("Create Recipe",    "POST", "/api/recipes",                   token,  testRecipe);
            bench.benchmarkEndpoint("Get All Recipes",  "GET",  "/api/recipes",                   token,  null);

            if (recipeId != null) {
                bench.benchmarkEndpoint("Get One Recipe",   "GET",  "/api/recipes/" + recipeId,      token,  null);
                bench.benchmarkEndpoint("Update Recipe",    "PUT",  "/api/recipes/" + recipeId,      token,  testRecipe);
                bench.benchmarkEndpoint("Delete Recipe",    "DELETE","/api/recipes/" + recipeId,      token,  null);
            }

            bench.benchmarkEndpoint("Unauthorized",      "GET",  "/api/recipes",                   "bad_token", null);
        }

        bench.printSummary();

        // === AUTO-SAVE / AUTO-COMPARE ===
        boolean baselineExists = Files.exists(BASELINE_FILE);

        if (!baselineExists) {
            // FIRST RUN: save as baseline.json
            String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(bench.results);
            Files.writeString(BASELINE_FILE, json);
            System.out.println("\n============================================");
            System.out.println("  BASELINE SAVED to baseline.json");
            System.out.println("  Make your performance changes, then run again!");
            System.out.println("  The tool will auto-compare and check for 40% drop.");
            System.out.println("============================================");
        } else {
            // SECOND RUN: compare with baseline
            String baselineJson = Files.readString(BASELINE_FILE);
            List<EndpointResult> baseline = MAPPER.readValue(
                    baselineJson,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, EndpointResult.class)
            );
            compareResults(baseline, bench.results);
        }
    }

    // ====== COMPARISON ======

    public static void compareResults(List<EndpointResult> before, List<EndpointResult> after) {
        System.out.println("\n============================================");
        System.out.println("  LATENCY COMPARISON (Before vs After)");
        System.out.println("============================================");
        System.out.printf("%-30s %10s %10s %12s %10s%n", "Endpoint", "Before(ms)", "After(ms)", "Change", "40% Target");
        System.out.println("-".repeat(75));

        boolean allHitTarget = true;

        for (EndpointResult b : before) {
            var a = after.stream()
                    .filter(r -> r.name.equals(b.name) && r.path.equals(b.path))
                    .findFirst()
                    .orElse(null);
            if (a == null) continue;

            double change = ((double) (a.avgMs - b.avgMs) / b.avgMs) * 100;
            boolean hitTarget = change <= -40;
            if (!hitTarget) allHitTarget = false;

            String symbol = hitTarget ? "✓" : (change < 0 ? "~" : "✗");
            String color;
            if (hitTarget) {
                color = "\u001B[32m"; // green
            } else if (change < 0) {
                color = "\u001B[33m"; // yellow
            } else {
                color = "\u001B[31m"; // red
            }

            System.out.printf("%-30s %10d %10d %s%+.1f%% %s %s%n",
                    b.name, b.avgMs, a.avgMs, color, change, symbol,
                    hitTarget ? "\u001B[32m✓ HIT\u001B[0m" : "\u001B[31mMISS\u001B[0m");
        }

        System.out.println("-".repeat(75));
        if (allHitTarget) {
            System.out.println("\u001B[32m  ✓ 40% LATENCY TARGET ACHIEVED on ALL endpoints!\u001B[0m");
        } else {
            System.out.println("\u001B[31m  ⚠ 40% LATENCY TARGET NOT YET ACHIEVED on all endpoints\u001B[0m");
            System.out.println("\u001B[33m  Some endpoints still need improvement. Review the MISS entries above.\u001B[0m");
        }
        System.out.println("============================================\u001B[0m");
    }

    // ====== HELPERS ======

    private boolean isServerUp() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/health"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 && resp.body().contains("UP");
        } catch (Exception e) {
            return false;
        }
    }

    private String registerAndLogin() throws Exception {
        RegisterRequest reg = new RegisterRequest("bench_user", testEmail, testPassword);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(reg)))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 201) {
            JsonNode json = MAPPER.readTree(resp.body());
            return json.get("token").asText();
        }

        LoginRequest login = new LoginRequest(testEmail, testPassword);
        req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(login)))
                .timeout(Duration.ofSeconds(5))
                .build();
        resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 200) {
            JsonNode json = MAPPER.readTree(resp.body());
            return json.get("token").asText();
        }
        return null;
    }

    private String createTestRecipe(String token) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/recipes"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(testRecipe)))
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 201) {
            JsonNode json = MAPPER.readTree(resp.body());
            return json.get("id").asText();
        }
        return null;
    }

    private void benchmarkEndpoint(String name, String method, String path, String token, Object body) {
        System.out.print("  " + name + " (" + method + " " + path + ") ... ");

        List<Long> latencies = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + path))
                        .timeout(Duration.ofSeconds(10));

                if (token != null) {
                    builder.header("Authorization", "Bearer " + token);
                }

                switch (method.toUpperCase()) {
                    case "GET" -> builder.GET();
                    case "DELETE" -> builder.DELETE();
                    case "POST" -> {
                        builder.header("Content-Type", "application/json");
                        builder.POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)));
                    }
                    case "PUT" -> {
                        builder.header("Content-Type", "application/json");
                        builder.PUT(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)));
                    }
                }

                long start = System.nanoTime();
                HttpResponse<String> resp = CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                long elapsed = System.nanoTime() - start;
                latencies.add(elapsed / 1_000_000);
            } catch (Exception e) {
                // skip failed
            }

            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }

        if (latencies.isEmpty()) {
            System.out.println("ALL FAILED");
            return;
        }

        Collections.sort(latencies);
        double avg = latencies.stream().mapToLong(Long::valueOf).average().orElse(0);
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);
        long p95 = latencies.get((int) Math.ceil(latencies.size() * 0.95) - 1);
        long p99 = latencies.get((int) Math.ceil(latencies.size() * 0.99) - 1);

        System.out.printf("min=%dms avg=%.1fms max=%dms p95=%dms p99=%dms (samples=%d)%n",
                min, avg, max, p95, p99, latencies.size());

        results.add(new EndpointResult(name, method, path, min, Math.round(avg), max, p95, p99, latencies.size()));
    }

    private void printSummary() {
        System.out.println("\n============================================");
        System.out.println("  SUMMARY");
        System.out.println("============================================");
        System.out.printf("%-30s %8s %10s %8s%n", "Endpoint", "Avg(ms)", "p95(ms)", "p99(ms)");
        System.out.println("-".repeat(60));

        double totalAvg = 0;
        for (EndpointResult r : results) {
            System.out.printf("%-30s %8d %10d %8d%n",
                    r.name, r.avgMs, r.p95Ms, r.p99Ms);
            totalAvg += r.avgMs;
        }

        System.out.println("-".repeat(60));
        System.out.printf("%-30s %8.1f%n", "Overall Average", totalAvg / results.size());
        System.out.println("============================================");
    }

    // --- Result POJO ---
    public record EndpointResult(
            String name,
            String method,
            String path,
            long minMs,
            long avgMs,
            long maxMs,
            long p95Ms,
            long p99Ms,
            int samples
    ) {}
}

