package tn.recruti.recruti_backend.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "passages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Passage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate datePassage;
    private int score;

    @ManyToOne
    @JoinColumn(name="quiz_id")
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name="user_id")
    @JsonBackReference("1")
    private User candidat;

    @OneToOne(mappedBy = "passage",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JsonBackReference("2")
    private Plannification plannification;






}
