package com.example.sbazureappdemo.controller;

import com.example.sbazureappdemo.dto.Employee;
import com.example.sbazureappdemo.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@EnableScheduling
@RequestMapping("/employees")
public class EmployeeController {

    Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private static final String FOLDER_PATH = "/var/tmp/empdata/";

    @Autowired
    private EmployeeService employeeService;


    @GetMapping("/")
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        logger.info("getEmployeeById called");
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        return employee.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/")
    public ResponseEntity<String> addEmployee(@RequestBody Employee employee) {
        logger.info("addEmployee called");
        employeeService.addEmployee(employee);
        return new ResponseEntity<>("Employee created successfully", HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        logger.info("deleteEmployee called");
        boolean removed = employeeService.deleteEmployee(id);
        if (removed) {
            return new ResponseEntity<>("Employee deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Employee not found", HttpStatus.NOT_FOUND);
        }
    }

    private static final String PAYTOLL_URL = "https://payroll001.internal.calmmeadow-c4ce7408.westus2.azurecontainerapps.io/payroll/";

    //private static final String PAYTOLL_URL = "http://localhost:8080/payroll/";

    @GetMapping("/salary/{employeeId}")
    public String getSalary(@PathVariable long employeeId) throws URISyntaxException, IOException, InterruptedException {
        logger.info("getSalary called");
        HttpClient client = HttpClient.newHttpClient();
        String url = PAYTOLL_URL + "salary/" + employeeId;
        logger.info("url for salary {}", url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            logger.info("response {}", response.body());
            return "The salary of emp id " + employeeId + " is " + response.body();
        } else {
            throw new RuntimeException("Failed to retrieve employee salary: " + response.body());
        }
    }

    @GetMapping("/testlogs")
    public String addEmployee() {
        logger.trace("A TRACE Message");
        logger.debug("A DEBUG Message");
        logger.info("An INFO Message");
        logger.warn("A WARN Message");
        logger.error("An ERROR Message");
        return "test called";
    }

    @PostMapping("/adddata")
    public ResponseEntity<?> uploadEmployeeData(@RequestBody List<Employee> employees) {
        if (employees.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No employee data provided");
        }

        // Create the folder if it does not exist
        File folder = new File(FOLDER_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // Save the employee data to files
        for (Employee employee : employees) {
            String fileName = employee.getId() + ".txt";
            File file = new File(FOLDER_PATH + fileName);
            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
                String content = String.format("Name: %s\nSalary: %f", employee.getName(), employee.getSalary());
                stream.write(content.getBytes());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving employee data");
            }
        }
        return ResponseEntity.ok("Employee data saved successfully");
    }

    @GetMapping("/employees")
    public ResponseEntity<?> getEmployeeData() {
        File folder = new File(FOLDER_PATH);
        if (!folder.exists() || !folder.isDirectory()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Employee data folder not found");
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return ResponseEntity.ok(new ArrayList<Employee>());
        }
        List<Employee> employees = new ArrayList<>();
        for (File file : files) {
            try {
                String fileName = file.getName();
                List<String> content = Files.readAllLines(file.toPath());
                System.out.println("content : " + content);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading employee data");
            }
        }
        return ResponseEntity.ok(employees);
    }
    //@Scheduled(fixedDelay = 60000) // run every minute
    public void refreshEmployeeData() {
        System.out.println("Refreshing employee data...");
        getEmployeeData();
        System.out.println("Employee data refreshed.");
    }

    @GetMapping("/apiversion")
    public String apiVersion(){
        return "API Revision Version 1";
    }
}
