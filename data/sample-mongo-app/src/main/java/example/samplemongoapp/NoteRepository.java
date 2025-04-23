package example.samplemongoapp;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface NoteRepository extends MongoRepository <Note, String> {

}