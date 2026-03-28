package tn.recruti.recruti_backend.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="quiz")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String contenu;         // "[{'question':'...','choix':['A','B','C']}]"

    @Column(columnDefinition = "TEXT")
    private String vraiesReponses;  // "['A','C','B']"

    @OneToOne(mappedBy = "quiz")
    @JsonBackReference("1")
    private Offer offer;

    @OneToMany(mappedBy = "quiz")
    @JsonManagedReference("2")
    private List<Passage> passage;

}
