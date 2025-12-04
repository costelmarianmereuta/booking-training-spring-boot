package training.salonzied.api;

import com.salonized.dto.Category;
import com.salonized.dto.CategoryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import training.salonzied.service.TreatmentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryApi {

    private final TreatmentService  treatmentService;

    @PostMapping
    public ResponseEntity<Category> creatCategory(@Valid @RequestBody CategoryRequest CategoryRequest){
        Category Category=treatmentService.createCategory(CategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Category);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getTreatmentCategories(){
        List<Category> categories= treatmentService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{name}")
    public ResponseEntity<Category> updateCategory(@Valid @RequestBody CategoryRequest CategoryRequest, @PathVariable String name){
        Category Category=treatmentService.updateCategory(CategoryRequest, name);
        return ResponseEntity.ok(Category);
    }
    @DeleteMapping("/{name}")
    public ResponseEntity<Category> deleteCategory(@PathVariable String name){
        treatmentService.deleteCategory(name);
        return ResponseEntity.noContent().build();
    }
}
