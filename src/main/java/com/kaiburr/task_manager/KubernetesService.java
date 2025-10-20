package com.kaiburr.task_manager;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class KubernetesService {

    @Value("${kubernetes.namespace:default}")
    private String namespace;

    private CoreV1Api coreV1Api;

    @PostConstruct
    public void init() {
        // Try in-cluster configuration first
        ApiClient client;
        try {
            client = Config.fromCluster();
            System.out.println("Using in-cluster Kubernetes configuration");
        } catch (Exception e) {
            System.out.println("In-cluster config failed, trying default config: " + e.getMessage());
            try {
                client = Config.defaultClient();
            } catch (Exception defaultConfigException) {
                System.err.println("Default Kubernetes config also failed: " + defaultConfigException.getMessage());
                System.out.println("Running in local mode - Kubernetes features will simulate execution");
                return; // Don't initialize Kubernetes client if not available
            }
        }
        
        try {
            Configuration.setDefaultApiClient(client);
            this.coreV1Api = new CoreV1Api();
            System.out.println("Kubernetes client initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize Kubernetes client: " + e.getMessage());
            System.out.println("Running in local mode - Kubernetes features will simulate execution");
            // Don't throw exception, just log and continue without Kubernetes
        }
    }

    public String executeCommandInPod(String command) {
        // Check if Kubernetes client is available
        if (coreV1Api == null) {
            System.out.println("Kubernetes not available - simulating command execution locally: " + command);
            return simulateCommandExecution(command);
        }
        
        String podName = "task-execution-" + UUID.randomUUID().toString().substring(0, 8);
        
        try {
            System.out.println("Creating pod: " + podName + " with command: " + command);
            
            // Create pod
            V1Pod pod = createBusyboxPod(podName, command);
            V1Pod createdPod = coreV1Api.createNamespacedPod(namespace, pod, null, null, null, null);
            System.out.println("Pod created successfully: " + createdPod.getMetadata().getName());
            
            // Wait for pod to complete
            String output = waitForPodCompletion(podName);
            System.out.println("Pod execution completed with output: " + output);
            
            // Clean up pod
            deletePod(podName);
            
            return output;
        } catch (Exception e) {
            System.err.println("Error in executeCommandInPod: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Clean up pod in case of error
            try {
                deletePod(podName);
            } catch (Exception cleanupException) {
                System.err.println("Failed to cleanup pod: " + cleanupException.getMessage());
            }
            return "Error executing command in Kubernetes pod: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
    
    private String simulateCommandExecution(String command) {
        // Simulate command execution for local development
        try {
            Thread.sleep(1000); // Simulate execution time
            return "Simulated execution of: " + command + "\nOutput: Command executed successfully in local simulation mode";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Simulation interrupted";
        }
    }

    private V1Pod createBusyboxPod(String podName, String command) {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", "task-execution");
        labels.put("type", "temporary");

        V1ObjectMeta metadata = new V1ObjectMeta()
                .name(podName)
                .labels(labels);

        V1Container container = new V1Container()
                .name("busybox")
                .image("busybox:latest")
                .command(Collections.singletonList("sh"))
                .args(Arrays.asList("-c", command));

        V1PodSpec spec = new V1PodSpec()
                .restartPolicy("Never")
                .containers(Collections.singletonList(container));

        return new V1Pod()
                .apiVersion("v1")
                .kind("Pod")
                .metadata(metadata)
                .spec(spec);
    }

    private String waitForPodCompletion(String podName) throws ApiException, InterruptedException {
        int maxWaitTime = 60; // seconds
        int waitInterval = 2; // seconds
        int totalWaitTime = 0;

        while (totalWaitTime < maxWaitTime) {
            V1Pod pod = coreV1Api.readNamespacedPod(podName, namespace, null);
            V1PodStatus status = pod.getStatus();
            
            if (status != null && status.getPhase() != null) {
                String phase = status.getPhase();
                
                if ("Succeeded".equals(phase) || "Failed".equals(phase)) {
                    // Get pod logs
                    try {
                        String logs = coreV1Api.readNamespacedPodLog(
                                podName, namespace, null, null, null, null, null, null, null, null, null);
                        return logs != null ? logs : "Command completed with no output";
                    } catch (ApiException e) {
                        return "Command completed but could not retrieve logs: " + e.getMessage();
                    }
                }
            }
            
            Thread.sleep(TimeUnit.SECONDS.toMillis(waitInterval));
            totalWaitTime += waitInterval;
        }
        
        return "Command timed out after " + maxWaitTime + " seconds";
    }

    private void deletePod(String podName) {
        try {
            coreV1Api.deleteNamespacedPod(podName, namespace, null, null, null, null, null, null);
        } catch (ApiException e) {
            // Log error but don't throw - this is cleanup
            System.err.println("Failed to delete pod " + podName + ": " + e.getMessage());
        }
    }
}
