package tn.recruti.recruti_backend.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.recruti.recruti_backend.enums.TypeNotif;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contenu;
    @Enumerated(EnumType.STRING)
    private TypeNotif type;

    //recruter
    @ManyToOne
    @JoinColumn(name="recruteur_id")
    @JsonBackReference("1")
    private User recruteur;


    //candidats
    @ManyToOne
    @JoinColumn(name="candidat_id")
    @JsonBackReference("2")
    private User candidat;


    //plannification
    @ManyToOne
    @JoinColumn(name="id_plannification")
    @JsonBackReference("3")
    private Plannification plannification;
}
