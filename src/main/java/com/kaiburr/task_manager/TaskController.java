// src/main/java/com/kaiburr/taskmanager/TaskController.java
package com.kaiburr.task_manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private KubernetesService kubernetesService;
    
    // Simple health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Task Manager is running!");
    }

    // Endpoint to get all tasks or a single task by ID [cite: 65, 66]
    @GetMapping
    public ResponseEntity<List<Task>> getTasks(@RequestParam(required = false) String id) {
        try {
            if (id != null) {
                Optional<Task> task = taskRepository.findById(id);
                return task.map(t -> ResponseEntity.ok(List.of(t)))
                           .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
            }
            return ResponseEntity.ok(taskRepository.findAll());
        } catch (Exception e) {
            System.err.println("Error in getTasks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint to find tasks by name [cite: 74, 75]
    @GetMapping("/findByName")
    public ResponseEntity<List<Task>> findTasksByName(@RequestParam String name) {
        List<Task> tasks = taskRepository.findByNameContainingIgnoreCase(name);
        if (tasks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // [cite: 76]
        }
        return ResponseEntity.ok(tasks);
    }

    // Endpoint to create or update a task [cite: 67]
    @PutMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        // Simple validation to prevent malicious commands [cite: 68]
        // A real-world app would need a more robust allow-list approach.
        String command = task.getCommand();
        if (command == null || command.matches(".*[;&|`<>\\$].*")) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Task savedTask = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    // Endpoint to delete a task by ID [cite: 73]
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        if (!taskRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    // Endpoint to execute a task by its ID using Kubernetes pods
    @PutMapping("/{id}/execute")
    public ResponseEntity<Task> executeTask(@PathVariable String id) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Task task = optionalTask.get();
        TaskExecution execution = new TaskExecution();
        execution.setStartTime(new Date());

        try {
            // Execute command in Kubernetes pod using busybox image
            String output = kubernetesService.executeCommandInPod(task.getCommand());
            execution.setOutput(output);
        } catch (Exception e) {
            execution.setOutput("Error executing command in Kubernetes pod: " + e.getMessage());
        } finally {
            execution.setEndTime(new Date());
            if (task.getTaskExecutions() == null) {
                task.setTaskExecutions(new ArrayList<>());
            }
            task.getTaskExecutions().add(execution);
            taskRepository.save(task);
        }

        return ResponseEntity.ok(task);
    }
}