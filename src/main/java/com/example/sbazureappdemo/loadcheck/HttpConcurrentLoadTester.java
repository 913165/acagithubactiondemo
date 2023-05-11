package com.example.sbazureappdemo.loadcheck;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpConcurrentLoadTester {

    private static final String TEST_URL = "https://scaledemo.calmpond-2cd7a51f.australiaeast.azurecontainerapps.io/employees/";
    public static void main1(String[] args) throws InterruptedException {
        // Number of concurrent requests
        int numRequests = 100;
        // ExecutorService for managing threads
        ExecutorService executor = Executors.newFixedThreadPool(numRequests);
        // CountdownLatch for synchronizing the start of all threads
        CountDownLatch latch = new CountDownLatch(1);
        // Send concurrent requests
        for (int i = 0; i < numRequests; i++) {
            executor.submit(() -> {
                try {
                    // Wait for all threads to start simultaneously
                    latch.await();

                    // Send HTTP request and receive response
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    HttpGet request = new HttpGet(TEST_URL);
                    HttpResponse response = httpClient.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();

                    System.out.println("Thread " + Thread.currentThread().getId() + ": Response code = " + statusCode);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        latch.countDown();
        executor.shutdown();
    }
}
