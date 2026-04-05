package tn.recruti.recruti_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "offres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate dateEmission;

    @NotNull
    private String titre;

    @NotNull
    private String description;

    private String tags;

    private boolean available;

    @ManyToOne
    @JsonBackReference("3")
    @JoinColumn(name = "recruteur_id")
    private User recruteur;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonManagedReference("1")
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @OneToMany(mappedBy = "offre",cascade = CascadeType.ALL)
    @JsonManagedReference("2")
    private List<Candidature> candidatures;
}