package tn.recruti.recruti_backend.model;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonIgnore
    private Offer offer;

    @OneToMany(mappedBy = "quiz")
    @JsonIgnore
    private List<Passage> passage;

}
