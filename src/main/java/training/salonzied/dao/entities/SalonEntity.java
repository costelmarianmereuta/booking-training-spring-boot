package training.salonzied.dao.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "salons")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(exclude = "reservations")
@ToString(exclude = "reservations")
public class SalonEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "public_id", nullable = false, unique = true, updatable = false)
  private String publicId;

  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Embedded
  private Address address;


  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
  @Column
  @OneToMany(mappedBy = "salon",
          cascade = CascadeType.ALL,
          orphanRemoval = true)
  private List<ReservationEntity> reservations;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "salon")
  private List<CategoryEntity> categories;

  @ElementCollection
  @CollectionTable(
          name = "salon_opening_hours",
          joinColumns = @JoinColumn(name = "salon_id")
  )
  private List<WorkingHour> openingHours;

  @ElementCollection
  @CollectionTable(
          name = "salon_special_opening_hours",
          joinColumns = @JoinColumn(name = "salon_id")
  )
  private List<SpecialOpeningHours> specialOpeningHours;

}
