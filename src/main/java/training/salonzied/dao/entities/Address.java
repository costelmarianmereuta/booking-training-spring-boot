package training.salonzied.dao.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

  @Column(name = "street")
  private String street;

  @Column(name = "house_number", length = 10)
  private String houseNumber;

  @Column(name = "postal_box", length = 10)
  private String postalBox;

  @Column(name = "postcode", length = 10, nullable = false)
  private String postcode;

  @Column(name = "city", length = 120)
  private String city;
}
