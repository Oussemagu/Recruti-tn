package tn.recruti.recruti_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.recruti.recruti_backend.enums.Role;
import tn.recruti.recruti_backend.enums.statuAnalyse;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="candidatures")
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private LocalDate datePostulation;

    @NotNull
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] Cv;

    private int scoreCv;


    @NotNull
    @Enumerated(EnumType.STRING)
    private statuAnalyse status;

    @ManyToOne
    @JoinColumn(name="candidat_id")
    private User candidat;

    @ManyToOne
    @JoinColumn(name="id_offre")
    @JsonBackReference
    private Offer offre;










}
