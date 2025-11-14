package training.salonzied.service;

import com.salonized.dto.Category;
import com.salonized.dto.CategoryRequest;
import com.salonized.dto.Treatment;

import com.salonized.dto.TreatmentRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import training.salonzied.dao.entities.CategoryEntity;
import training.salonzied.dao.entities.SalonEntity;
import training.salonzied.dao.entities.TreatmentEntity;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.dao.repo.TreatmentCategoryRepository;
import training.salonzied.dao.repo.TreatmentRepository;
import training.salonzied.error.EntityNotFoundException;
import training.salonzied.error.TreatmentAlreadyExistsException;
import training.salonzied.mapper.TreatmentMapper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentService {
    private final TreatmentRepository treatmentRepository;
    private final TreatmentCategoryRepository treatmentCategoryRepository;
    private final TreatmentMapper treatmentMapper;
    private final SalonRepository salonRepository;


    public List<Treatment> getTreatments() {
        List<TreatmentEntity> treatmentsEntities = treatmentRepository.findAll();
       return treatmentsEntities.stream()
                .map(treatmentMapper::treatmentToTreatmentDto)
                .toList();
    }

    @Transactional
    public Treatment addTreatment(TreatmentRequest requestTreatment) {
        if (treatmentRepository.existsByName(requestTreatment.getName())) {
            throw new TreatmentAlreadyExistsException(requestTreatment.getName());
        }
        CategoryEntity treatmentCategory = treatmentCategoryRepository.findByName(requestTreatment.getCategoryName())
                .orElseThrow(() -> new EntityNotFoundException("Category", requestTreatment.getCategoryName()));

        TreatmentEntity entity = treatmentMapper.toTreatmentEntity(requestTreatment);
        entity.setCategory(treatmentCategory);
        TreatmentEntity savedEntity = treatmentRepository.save(entity);
        return treatmentMapper.treatmentToTreatmentDto(savedEntity);
    }

    public Treatment getTreatmentByName(String name) {
        TreatmentEntity treatmentEntity = treatmentRepository.findByName(name)
                .orElseThrow(()->{
                    log.error("Treatment not found with name: {}", name);
                    throw new  EntityNotFoundException("treatment", name);
                });
        return treatmentMapper.treatmentToTreatmentDto(treatmentEntity);
    }

    @Transactional
    public Treatment updateTreatment(String name, TreatmentRequest treatment) {

        TreatmentEntity treatmentEntity = treatmentRepository.findByName(name)
                .orElseThrow(()->{
                    log.error("Treatment not found with name: {}", name);
                    throw new  EntityNotFoundException("treatment", name);
                });
        updateTreatment(treatment, treatmentEntity);
        treatmentRepository.save(treatmentEntity);
        return treatmentMapper.treatmentToTreatmentDto(treatmentEntity);
    }

    private void updateTreatment(TreatmentRequest treatment, TreatmentEntity treatmentEntity) {
        treatmentEntity.setName(treatment.getName());
        treatmentEntity.setDescription(treatment.getDescription());
        treatmentEntity.setPrice(treatment.getPrice());
        treatmentEntity.setDuration(treatment.getDuration());
        treatmentEntity.setTimeBetweenTreatments(treatment.getTimeBetweenTreatments());
        if (!treatment.getCategoryName().equals(treatmentEntity.getName())) {
            CategoryEntity treatmentCategory = treatmentCategoryRepository.findByName(treatment.getCategoryName())
                    .orElseThrow(() -> {
                        throw new EntityNotFoundException("Category", treatment.getCategoryName());
                    });
            treatmentEntity.setCategory(treatmentCategory);
        }
    }

    @Transactional
    public void deleteTreatment(String name) {
        long deleteByName = treatmentRepository.deleteByName(name);
        if (deleteByName == 0) {
            throw new EntityNotFoundException("treatment", name);
        }
        log.info("Treatment deleted with name: " + name);
    }

    @Transactional
    public Category createCategory(CategoryRequest treatmentCategoryRequest) {
        if (treatmentCategoryRepository.existsByName(treatmentCategoryRequest.getName())) {
            throw new TreatmentAlreadyExistsException(treatmentCategoryRequest.getName());
        }
        SalonEntity salonEntity = salonRepository.findByPublicId(treatmentCategoryRequest.getSalonPublicId())
                .orElseThrow(()->{
                    throw new  EntityNotFoundException("soin visage", treatmentCategoryRequest.getName());
                });

        CategoryEntity categoryEntity = treatmentMapper.toTreatmentCategoryEntity(treatmentCategoryRequest);
        categoryEntity.setSalon(salonEntity);

        CategoryEntity treatmentCategorySaved = treatmentCategoryRepository.save(categoryEntity);
        log.info("Created treatment category with name: {}", categoryEntity.getName());
        return treatmentMapper.toTreatmentCategory(treatmentCategorySaved);
    }


    public List<Category> getCategories() {
        List<CategoryEntity> categoryEntities = treatmentCategoryRepository.findAll();
        return categoryEntities.stream()
                .map(treatmentMapper::toTreatmentCategory)
                .toList();
    }

    @Transactional
    public Category updateCategory(CategoryRequest treatmentCategoryRequest, String name) {
        CategoryEntity treatmentCategory = treatmentCategoryRepository.findByName(name)
                .orElseThrow(() -> {
                    throw new EntityNotFoundException("Category", name);
                });
        treatmentCategory.setName(treatmentCategoryRequest.getName());
        treatmentCategory.setDescription(treatmentCategoryRequest.getDescription());
        CategoryEntity updatedCategory = treatmentCategoryRepository.save(treatmentCategory);
        log.info("Updated treatment category with name: {}", treatmentCategory.getName());
        return treatmentMapper.toTreatmentCategory(updatedCategory);
    }
    @Transactional
    public void deleteCategory(String name) {
        long deleteByName = treatmentCategoryRepository.deleteByName(name);
        if (deleteByName == 0) {
            throw new EntityNotFoundException("treatment", name);
        }
        log.info("Category deleted with name: " + name);
    }
}
