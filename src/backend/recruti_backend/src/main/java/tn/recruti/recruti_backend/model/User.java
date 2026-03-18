package tn.recruti.recruti_backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.recruti.recruti_backend.enums.Role;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nom;

    @NotNull
    private String prenom;

    @NotNull
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    private LocalDate dateNaissance;

    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    private String skills;

    private String sexe;

    private String gouvernorat;

    private String poste;

    private String nomSociete;

    @NotNull
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] cvGenerique;

    @OneToMany(mappedBy ="recruteur", fetch=FetchType.LAZY)
    @JsonManagedReference("1")
    private List<Offer> offers;


    @OneToMany(mappedBy = "candidat",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JsonManagedReference("2")
    private List<Passage> passages;


    @OneToMany(mappedBy = "candidat",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JsonManagedReference("3")
    private List<Plannification> plannificationCandidat;


    @OneToMany(mappedBy = "recruteur",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JsonManagedReference("4")
    private List<Plannification> plannificationRecruteur;


    @OneToMany(mappedBy = "recruteur",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JsonManagedReference("5")
    private List<Notification> notificationsRecruteur;

    @OneToMany(mappedBy = "candidat",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JsonManagedReference("6")
    private List<Notification> notificationsCandidat;






}