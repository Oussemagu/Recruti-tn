package tn.recruti.recruti_backend.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.recruti.recruti_backend.enums.statuAnalyse;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidatures")
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate datePostulation;

    @NotNull
    @Column(name = "cv_path")
    private String cvPath; // chemin vers le fichier PDF

    private int scoreCv;

    @NotNull
    @Enumerated(EnumType.STRING)
   private statuAnalyse status;// commence par majuscule par convention

    @Column(name = "invited_to_quiz")
    private boolean invitedToQuiz = false;

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private User candidat;

    @ManyToOne
    @JoinColumn(name = "id_offre")
    @JsonBackReference
    private Offer offre;
}