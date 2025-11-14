package util;

import com.salonized.dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.experimental.UtilityClass;
import training.salonzied.dao.entities.SalonEntity;
import training.salonzied.dao.entities.CategoryEntity;
import training.salonzied.dao.entities.TreatmentEntity;
import training.salonzied.dao.entities.UserEntity;

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
                            .street("str proiectantului")
                            .city("Bacau")
                            .houseNumber("12B")
                            .postalBox("a12")
                            .postcode("1235")
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
                            .street("str proiectantului")
                            .houseNumber("12B")
                            .postalBox("a12")
                            .postcode("1080")
                            .build())
            .createdAt(LocalDateTime.now())
            .build();
  }

  public UpdateSalonRequest getUpdateSalonRequest() {
    return UpdateSalonRequest.builder()
            .name("Lys Salon")
            .address(Address.builder()
                    .street("str proiectantului")
                    .city("Bacau")
                    .houseNumber("12B")
                    .postalBox("a12")
                    .postcode("1235")
                    .build())
            .build();
  }

  public Treatment getTreatment(){
    return Treatment.builder()
            .name("Hydrafacial")
            .description("desc ...")
            .price(75.00)
            .duration(30)
            .timeBetweenTreatments(15)
            .categoryName("soin visage")
            .build();
  }

  public TreatmentRequest getTreatmentRequest(){
    return TreatmentRequest.builder()
            .name("Hydrafacial")
            .description("desc ...")
            .price(75.00)
            .duration(30)
            .timeBetweenTreatments(15)
            .categoryName("soin visage")
            .build();
  }
  public TreatmentEntity getTreatmentEntity(){
    return TreatmentEntity.builder()
            .name("Hydrafacial")
            .description("desc ...")
            .price(75.00)
            .duration(30)
            .timeBetweenTreatments(15)
            .category(CategoryEntity.builder().description("cat desc").name("soin visage").build())
            .build();
  }

  public CategoryRequest getCategoryRequest() {
    return CategoryRequest.builder()
            .name("Facial care")
            .description("Treatments focused on facial skin care and relaxation")
            .salonPublicId("b7a6c6f4-9d5a-4f91-8e71-39ef99e8c9c3")
            .build();
  }

  public CategoryEntity getCategoryEntity() {
    return CategoryEntity.builder()
            .name("soin visage")
            .description("cat desc")
            .salon(getSalonEntity())
            .build();
  }

  public CreateUserRequest getCreateUserRequest() {
    return CreateUserRequest.builder()
            .firstName("Maria")
            .lastName("Popescu")
            .email("maria.popescu@example.com")
            .phone("+32 470 12 34 56")
            .birthDate(LocalDate.of(1980, 1, 1))
            .gender("female")
            .build();
  }

  public UpdateUserRequest getUpdateUserRequest() {
    return UpdateUserRequest.builder()
            .firstName("Marian")
            .lastName("Popescu")
            .phone("+40 723 45 67 89")
            .gender("male")
            .build();
  }

  public UserEntity getUserEntity() {
    return UserEntity.builder()
            .id(1L)
            .publicId("4a2f1e8c-11aa-4d8c-bd88-b0c82a8ff21e")
            .firstName("Maria")
            .lastName("Popescu")
            .email("maria.popescu@example.com")
            .phone("+32 470 12 34 56")
            .birthDate("1995-03-21")
            .gender("female")
            .createdAt(LocalDateTime.now())
            .build();
  }

}
