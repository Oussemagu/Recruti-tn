package tn.recruti.recruti_backend.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="plannifications")
public class Plannification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private LocalDate datePhase3;
    private String lienMeet;
    private boolean accepte;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_passage")
    @JsonManagedReference
    private Passage passage;




    @ManyToOne
    @JoinColumn(name="candidat_id")
    @JsonBackReference("1")
    private User candidat;

    @ManyToOne
    @JoinColumn(name="recruteur_id")
    @JsonBackReference("2")
    private User recruteur;

    @OneToMany(mappedBy = "plannification",cascade = CascadeType.ALL)
    @JsonManagedReference("3")
    private List<Notification> notification;

}
