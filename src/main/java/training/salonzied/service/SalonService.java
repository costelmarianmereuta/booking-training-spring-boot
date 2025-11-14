package training.salonzied.service;

import com.salonized.dto.CreateSalonRequest;
import com.salonized.dto.Salon;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.salonized.dto.UpdateSalonRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import training.salonzied.dao.entities.SalonEntity;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.error.EntityNotFoundException;
import training.salonzied.mapper.SalonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalonService {
  private final SalonRepository salonRepository;
  private final SalonMapper salonMapper;

  @Transactional
  public Salon createSalon(CreateSalonRequest request) {

    SalonEntity entity = salonMapper.toEntity(request);
    salonRepository.save(entity);
    return salonMapper.entityToSalonDto(entity);
  }

  public List<Salon> getSalons() {
    List<SalonEntity> salonEntities = salonRepository.findAll();
    return salonEntities.stream().map(salonMapper::entityToSalonDto).collect(Collectors.toList());
  }

  public Salon getSalonByPublicId(String publicId) {
    Optional<SalonEntity> salonEntity = salonRepository.findByPublicId(publicId);
    if (salonEntity.isEmpty()) {
      log.error("Salon not found with id: " + publicId);
      throw new EntityNotFoundException("Salon", publicId);
    }
    return salonEntity.map(salonMapper::entityToSalonDto).orElseThrow();
  }

  @Transactional
  public Salon updateSalon(String publicId, @Valid UpdateSalonRequest request) {
    SalonEntity salonEntity = salonRepository.findByPublicId(publicId)
            .orElseThrow(()->{
              log.error("Salon not found with id: {}", publicId);
              return new EntityNotFoundException("Salon", publicId);
            });

    updateSalon(request, salonEntity);
    salonRepository.save(salonEntity);

    return salonMapper.entityToSalonDto(salonEntity);
  }


  @Transactional
  public void deleteSalon(String publicId) {
    long deleted = salonRepository.deleteByPublicId(publicId);
    if (deleted == 0){
      throw new EntityNotFoundException("Salon", publicId);
    }
    log.info("Salon deleted with id: " + publicId);
  }

  private void updateSalon(UpdateSalonRequest request, SalonEntity salonEntity) {
    salonEntity.setName(request.getName());
    salonEntity.getAddress().setCity(request.getAddress().getCity());
    salonEntity.getAddress().setStreet(request.getAddress().getStreet());
    salonEntity.getAddress().setHouseNumber(request.getAddress().getHouseNumber());
    salonEntity.getAddress().setPostalBox(request.getAddress().getPostalBox());
    salonEntity.getAddress().setPostcode(request.getAddress().getPostcode());
  }
}
