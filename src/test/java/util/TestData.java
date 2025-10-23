package util;

import com.salonized.dto.Address;
import com.salonized.dto.CreateSalonRequest;
import com.salonized.dto.Salon;
import java.time.LocalDateTime;
import lombok.experimental.UtilityClass;
import training.salonzied.dao.entities.SalonEntity;

@UtilityClass
public class TestData {

  public CreateSalonRequest getCreateSalonRequest() {
    return CreateSalonRequest.builder()
        .name("Salon")
        .address(
            Address.builder()
                .city("Bacau")
                .houseNumber("12B")
                .postalBox("a12")
                .postcode("1080")
                .build())
        .build();
  }

  public Salon getSalon() {
    return Salon.builder()
        .publicId("b7a6c6f4-9d5a-4f91-8e71-39ef99e8c9c3")
        .name("Salon Prestige")
        .address(
            Address.builder()
                .city("Bacau")
                .houseNumber("12B")
                .postalBox("a12")
                .postcode("1080")
                .build())
        .createdAt(LocalDateTime.of(2025, 10, 18, 10, 0))
        .build();
  }

  public SalonEntity getSalonEntity() {
    return SalonEntity.builder()
        .id(1L)
        .publicId("b7a6c6f4-9d5a-4f91-8e71-39ef99e8c9c3")
        .name("Salon Prestige")
        .address(
            training.salonzied.dao.entities.Address.builder()
                .city("Bacau")
                .houseNumber("12B")
                .postalBox("a12")
                .postcode("1080")
                .build())
        .createdAt(LocalDateTime.now())
        .build();
  }
}
