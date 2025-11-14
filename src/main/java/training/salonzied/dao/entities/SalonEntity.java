package training.salonzied.dao.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "salons")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
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
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CategoryEntity> treatmentCategories;
}
