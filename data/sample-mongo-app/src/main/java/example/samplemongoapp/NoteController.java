package example.samplemongoapp;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.jni.Global;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import datadog.trace.api.Trace;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.springframework.web.client.RestTemplate;

@RestController
public class NoteController {

  @Autowired
  private NoteRepository repository;

  @Autowired
  private RestTemplate restTemplate;

  // Home page
  @GetMapping("/")
  Map<String, String> home(HttpServletRequest request) {
    Map<String, String> headers = new HashMap<>();

    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      String headerValue = request.getHeader(headerName);
      headers.put(headerName, headerValue);
      System.out.println("*****debugging-headers*****");
      System.out.println(headerName + ": " + headerValue);
    }
    // return welcome;
    return headers;
  }

  @GetMapping("/propagate")
  public ResponseEntity<String> simulateClientCall(HttpServletRequest request) {
      // Create the URL for the next endpoint
      String url = "http://web:8080/";
      System.out.println("Calling URL: " + url);

      // Use the RestTemplate which already automatically handles baggage forwarding
      try {
          String response = restTemplate.getForObject(url, String.class);
          return ResponseEntity.ok("Simulated internal call -> " + response);
      } catch (Exception e) {
          System.out.println("Error occurred while calling " + url + ":" + e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Error occurred while making the internal call: " + e.getMessage());
      }
  }

  // Gets a single note
  @GetMapping("/notes/{id}")
  Note getNote(@PathVariable String id) {
    Note rn = repository.findById(id).orElse(null);
    return rn;
  }

  // Creates a note
  @PostMapping("/notes")
  Note newNote(@RequestBody String description) {
    return repository.save(new Note(description));
  }

  // Creates a note
  @PutMapping("/notes/{id}")
  Note updateNote(@RequestBody String description, @PathVariable String id) {
    Note updateNote = repository.findById(id).orElse(null);
    updateNote.setDescription(description);
    return repository.save(updateNote);
  }

  // Gets all notes
  @GetMapping("/notes")
  List<Note> allNotes() {
    return repository.findAll();
  }

  // Deletes a note
  @DeleteMapping("/notes/{id}")
  String deleteNote(@PathVariable String id) {
    repository.deleteById(id);
    return "deleted";
  }

}