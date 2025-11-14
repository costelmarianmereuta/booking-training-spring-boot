package training.salonzied.api;

import com.salonized.dto.Treatment;
import com.salonized.dto.Category;
import com.salonized.dto.CategoryRequest;
import com.salonized.dto.TreatmentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import training.salonzied.service.TreatmentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/treatments")
public class TreatmentApi {

    private final TreatmentService treatmentService;

    @GetMapping
    public ResponseEntity<List<Treatment>> getTreatments(){
        List<Treatment> treatments = treatmentService.getTreatments();
        return ResponseEntity.ok(treatments);
    }
    @PostMapping
    public ResponseEntity<Treatment> addTreatment(@RequestBody TreatmentRequest requestTreatment){
        Treatment treatment= treatmentService.addTreatment(requestTreatment);
        return ResponseEntity.status(HttpStatus.CREATED).body(treatment);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Treatment> getTreatmentByName(@PathVariable String name){
       Treatment treatment = treatmentService.getTreatmentByName(name);
        return ResponseEntity.ok(treatment);
    }
    @PutMapping("/{name}")
    public ResponseEntity<Treatment> getTreatmentByName(@RequestBody TreatmentRequest treatment, @PathVariable String name){
        Treatment treatmentUpdated = treatmentService.updateTreatment(name, treatment);
        return ResponseEntity.ok(treatmentUpdated);
    }
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteTreatment(@PathVariable String name){
        treatmentService.deleteTreatment(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/category")
    public ResponseEntity<Category> creatCategory(@Valid @RequestBody CategoryRequest CategoryRequest){
        Category Category=treatmentService.createCategory(CategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Category);
    }

    @GetMapping("/category")
    public ResponseEntity<List<Category>> getTreatmentCategories(){
        List<Category> categories= treatmentService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/category/{name}")
    public ResponseEntity<Category> updateCategory(@Valid @RequestBody CategoryRequest CategoryRequest, @PathVariable String name){
        Category Category=treatmentService.updateCategory(CategoryRequest, name);
        return ResponseEntity.ok(Category);
    }
    @DeleteMapping("/category/{name}")
    public ResponseEntity<Category> deleteCategory(@PathVariable String name){
        treatmentService.deleteCategory(name);
        return ResponseEntity.noContent().build();
    }


}
