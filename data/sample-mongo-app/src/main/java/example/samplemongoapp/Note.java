package example.samplemongoapp;

import org.springframework.data.annotation.Id;

public class Note {

  @Id
  public String id;

  public String description;

  public void setDescription(String newDesc) {
    this.description = newDesc;
  }

  public Note(String description) {
    this.description = description;
  }
}