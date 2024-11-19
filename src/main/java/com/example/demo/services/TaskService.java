package com.example.demo.services;

import com.example.demo.entities.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.repository.TaskRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public List<Task> findAll(String status) {
        return taskRepository.findByStatus(status);
    }

    public Task findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task save(Task task) {
        if (task.getStatus() == null) task.setStatus("To do");
        if ("High".equalsIgnoreCase(task.getPriority())) {
            task.setPriorityOrder(1);
        } else if ("Medium".equalsIgnoreCase(task.getPriority())) {
            task.setPriorityOrder(2);
        } else if ("Low".equalsIgnoreCase(task.getPriority())) {
            task.setPriorityOrder(3);
        }
        return taskRepository.save(task);
    }

    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }

    public Task updateTask(Long id, Task updatedTask) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            Task existingTask = task.get();
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setPriority(updatedTask.getPriority());
            existingTask.setDeadLine(updatedTask.getDeadLine());
            existingTask.setStatus(updatedTask.getStatus());
            return taskRepository.save(existingTask);
        }
        return null;
    }

    public void moveTask(Long id, String newStatus) {
        Task task = findById(id);
        if (task != null && isValidStatusTransition(task.getStatus(), newStatus)) {
            task.setStatus(newStatus);
            taskRepository.save(task);
        }
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals("To Do") && newStatus.equals("In Progress"))
            return true;
        if (currentStatus.equals("In Progress") && newStatus.equals("Done"))
            return true;
        return false;
    }

    public List<Task> orderByPriority() {
        return taskRepository.findAllByOrderByPriorityOrderAsc();
    }

    public List<Task> filterTasks(String priority, LocalDate date) {
        return taskRepository.findAll()
                .stream()
                .filter(task -> task.getPriority().equalsIgnoreCase(priority) && task.getDeadLine().equals(date))
                .collect(Collectors.toList());
    }

    public String generateReport() {
        List<Task> allTasks = taskRepository.findAll();
        StringBuilder report = new StringBuilder("Relatório de Tarefas:\n");

        allTasks.forEach(task -> {
            report.append("Coluna: ").append(task.getStatus()).append("\n");
            report.append(" - Título: ").append(task.getTitle()).append("\n");
            report.append(" - Descrição: ").append(task.getDescription()).append("\n");
            report.append(" - Data Limite: ").append(task.getDeadLine()).append("\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate date = LocalDate.parse(task.getDeadLine(), formatter);
            if (date.isBefore(LocalDate.now()) && !"Concluído".equalsIgnoreCase(task.getStatus())) {
                report.append(" ATRASADA\n");
            }
            report.append("\n");
        });

        return report.toString();
    }
}