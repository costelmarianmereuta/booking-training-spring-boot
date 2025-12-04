package training.salonzied.service;

import com.salonized.dto.Category;
import com.salonized.dto.CategoryRequest;
import com.salonized.dto.Treatment;
import com.salonized.dto.TreatmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import training.salonzied.dao.entities.CategoryEntity;
import training.salonzied.dao.entities.SalonEntity;
import training.salonzied.dao.entities.TreatmentEntity;
import training.salonzied.dao.repo.SalonRepository;
import training.salonzied.dao.repo.TreatmentCategoryRepository;
import training.salonzied.dao.repo.TreatmentRepository;
import training.salonzied.error.EntityNotFoundException;
import training.salonzied.error.TreatmentAlreadyExistsException;
import training.salonzied.mapper.TreatmentMapper;
import util.TestData;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TreatmentService Unit Tests")
class TreatmentServiceTest {

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private TreatmentCategoryRepository treatmentCategoryRepository;

    @Mock
    private TreatmentMapper treatmentMapper;

    @Mock
    private SalonRepository salonRepository;

    @InjectMocks
    private TreatmentService treatmentService;

    private TreatmentEntity treatmentEntity;
    private TreatmentRequest treatmentRequest;
    private Treatment treatmentDto;
    private CategoryEntity categoryEntity;
    private CategoryRequest categoryRequest;
    private SalonEntity salonEntity;

    @BeforeEach
    void setUp() {
        treatmentEntity = TestData.getTreatmentEntity();
        treatmentRequest = TestData.getTreatmentRequest();
        treatmentDto = TestData.getTreatment();
        categoryEntity = TestData.getCategoryEntity();
        categoryRequest = TestData.getCategoryRequest();
        salonEntity = TestData.getSalonEntity();
    }

    @Test
    @DisplayName("Should get all treatments successfully")
    void getTreatments_Success() {
        // Given
        List<TreatmentEntity> entities = List.of(treatmentEntity);
        when(treatmentRepository.findAll()).thenReturn(entities);
        when(treatmentMapper.treatmentToTreatmentDto(treatmentEntity)).thenReturn(treatmentDto);

        // When
        List<Treatment> result = treatmentService.getTreatments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(treatmentRepository, times(1)).findAll();
        verify(treatmentMapper, times(1)).treatmentToTreatmentDto(treatmentEntity);
    }

    @Test
    @DisplayName("Should add treatment successfully")
    void addTreatment_Success() {
        // Given
        when(treatmentRepository.existsByName(treatmentRequest.getName())).thenReturn(false);
        when(treatmentCategoryRepository.findByName(treatmentRequest.getCategoryName()))
                .thenReturn(Optional.of(categoryEntity));
        when(treatmentMapper.toTreatmentEntity(treatmentRequest)).thenReturn(treatmentEntity);
        when(treatmentRepository.save(treatmentEntity)).thenReturn(treatmentEntity);
        when(treatmentMapper.treatmentToTreatmentDto(treatmentEntity)).thenReturn(treatmentDto);

        // When
        Treatment result = treatmentService.addTreatment(treatmentRequest);

        // Then
        assertNotNull(result);
        verify(treatmentRepository, times(1)).existsByName(treatmentRequest.getName());
        verify(treatmentCategoryRepository, times(1)).findByName(treatmentRequest.getCategoryName());
        verify(treatmentRepository, times(1)).save(treatmentEntity);
        verify(treatmentMapper, times(1)).treatmentToTreatmentDto(treatmentEntity);
    }

    @Test
    @DisplayName("Should throw TreatmentAlreadyExistsException when treatment name already exists")
    void addTreatment_AlreadyExists() {
        // Given
        when(treatmentRepository.existsByName(treatmentRequest.getName())).thenReturn(true);

        // When & Then
        assertThrows(
                TreatmentAlreadyExistsException.class,
                () -> treatmentService.addTreatment(treatmentRequest)
        );

        verify(treatmentRepository, times(1)).existsByName(treatmentRequest.getName());
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when category not found")
    void addTreatment_CategoryNotFound() {
        // Given
        when(treatmentRepository.existsByName(treatmentRequest.getName())).thenReturn(false);
        when(treatmentCategoryRepository.findByName(treatmentRequest.getCategoryName()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                EntityNotFoundException.class,
                () -> treatmentService.addTreatment(treatmentRequest)
        );

        verify(treatmentRepository, times(1)).existsByName(treatmentRequest.getName());
        verify(treatmentCategoryRepository, times(1)).findByName(treatmentRequest.getCategoryName());
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get treatment by name successfully")
    void getTreatmentByName_Success() {
        // Given
        String name = treatmentEntity.getName();
        when(treatmentRepository.findByName(name)).thenReturn(Optional.of(treatmentEntity));
        when(treatmentMapper.treatmentToTreatmentDto(treatmentEntity)).thenReturn(treatmentDto);

        // When
        Treatment result = treatmentService.getTreatmentByName(name);

        // Then
        assertNotNull(result);
        verify(treatmentRepository, times(1)).findByName(name);
        verify(treatmentMapper, times(1)).treatmentToTreatmentDto(treatmentEntity);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when treatment not found")
    void getTreatmentByName_NotFound() {
        // Given
        String name = "non-existent-treatment";
        when(treatmentRepository.findByName(name)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                EntityNotFoundException.class,
                () -> treatmentService.getTreatmentByName(name)
        );

        verify(treatmentRepository, times(1)).findByName(name);
        verify(treatmentMapper, never()).treatmentToTreatmentDto(any());
    }

    @Test
    @DisplayName("Should update treatment successfully")
    void updateTreatment_Success() {
        // Given
        String name = treatmentEntity.getName();
        // Setăm categoria din request să fie aceeași cu numele treatment-ului pentru a evita schimbarea categoriei
        // (codul compară treatment.getCategoryName() cu treatmentEntity.getName())
        treatmentRequest.setCategoryName(treatmentEntity.getName());
        
        when(treatmentRepository.findByName(name)).thenReturn(Optional.of(treatmentEntity));
        when(treatmentRepository.save(treatmentEntity)).thenReturn(treatmentEntity);
        when(treatmentMapper.treatmentToTreatmentDto(treatmentEntity)).thenReturn(treatmentDto);

        // When
        Treatment result = treatmentService.updateTreatment(name, treatmentRequest);

        // Then
        assertNotNull(result);
        verify(treatmentRepository, times(1)).findByName(name);
        verify(treatmentRepository, times(1)).save(treatmentEntity);
        verify(treatmentMapper, times(1)).treatmentToTreatmentDto(treatmentEntity);
        // Nu se apelează findByName pentru categorie pentru că categoria nu se schimbă
        verify(treatmentCategoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating non-existent treatment")
    void updateTreatment_NotFound() {
        // Given
        String name = "non-existent-treatment";
        when(treatmentRepository.findByName(name)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                EntityNotFoundException.class,
                () -> treatmentService.updateTreatment(name, treatmentRequest)
        );

        verify(treatmentRepository, times(1)).findByName(name);
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete treatment successfully")
    void deleteTreatment_Success() {
        // Given
        String name = treatmentEntity.getName();
        when(treatmentRepository.deleteByName(name)).thenReturn(1L);

        // When
        treatmentService.deleteTreatment(name);

        // Then
        verify(treatmentRepository, times(1)).deleteByName(name);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when deleting non-existent treatment")
    void deleteTreatment_NotFound() {
        // Given
        String name = "non-existent-treatment";
        when(treatmentRepository.deleteByName(name)).thenReturn(0L);

        // When & Then
        assertThrows(
                EntityNotFoundException.class,
                () -> treatmentService.deleteTreatment(name)
        );

        verify(treatmentRepository, times(1)).deleteByName(name);
    }

    @Test
    @DisplayName("Should create category successfully")
    void createCategory_Success() {
        // Given
        when(treatmentCategoryRepository.existsByName(categoryRequest.getName())).thenReturn(false);
        when(salonRepository.findByPublicId(categoryRequest.getSalonPublicId()))
                .thenReturn(Optional.of(salonEntity));
        when(treatmentMapper.toTreatmentCategoryEntity(categoryRequest)).thenReturn(categoryEntity);
        when(treatmentCategoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(treatmentMapper.toTreatmentCategory(categoryEntity)).thenReturn(new Category());

        // When
        Category result = treatmentService.createCategory(categoryRequest);

        // Then
        assertNotNull(result);
        verify(treatmentCategoryRepository, times(1)).existsByName(categoryRequest.getName());
        verify(salonRepository, times(1)).findByPublicId(categoryRequest.getSalonPublicId());
        verify(treatmentCategoryRepository, times(1)).save(categoryEntity);
    }

    @Test
    @DisplayName("Should throw TreatmentAlreadyExistsException when category name already exists")
    void createCategory_AlreadyExists() {
        // Given
        when(treatmentCategoryRepository.existsByName(categoryRequest.getName())).thenReturn(true);

        // When & Then
        assertThrows(
                TreatmentAlreadyExistsException.class,
                () -> treatmentService.createCategory(categoryRequest)
        );

        verify(treatmentCategoryRepository, times(1)).existsByName(categoryRequest.getName());
        verify(treatmentCategoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get all categories successfully")
    void getCategories_Success() {
        // Given
        List<CategoryEntity> entities = List.of(categoryEntity);
        when(treatmentCategoryRepository.findAll()).thenReturn(entities);
        when(treatmentMapper.toTreatmentCategory(categoryEntity)).thenReturn(new Category());

        // When
        List<Category> result = treatmentService.getCategories();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(treatmentCategoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update category successfully")
    void updateCategory_Success() {
        // Given
        String name = categoryEntity.getName();
        when(treatmentCategoryRepository.findByName(name)).thenReturn(Optional.of(categoryEntity));
        when(treatmentCategoryRepository.save(categoryEntity)).thenReturn(categoryEntity);
        when(treatmentMapper.toTreatmentCategory(categoryEntity)).thenReturn(new Category());

        // When
        Category result = treatmentService.updateCategory(categoryRequest, name);

        // Then
        assertNotNull(result);
        verify(treatmentCategoryRepository, times(1)).findByName(name);
        verify(treatmentCategoryRepository, times(1)).save(categoryEntity);
    }

    @Test
    @DisplayName("Should delete category successfully")
    void deleteCategory_Success() {
        // Given
        String name = categoryEntity.getName();
        when(treatmentCategoryRepository.deleteByName(name)).thenReturn(1L);

        // When
        treatmentService.deleteCategory(name);

        // Then
        verify(treatmentCategoryRepository, times(1)).deleteByName(name);
    }
}

