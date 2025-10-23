package training.salonzied.api;

import com.salonized.dto.CreateSalonRequest;
import com.salonized.dto.Salon;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import training.salonzied.service.SalonService;

@RestController
@RequestMapping("/salons")
@RequiredArgsConstructor
public class SalonApi {

  private final SalonService salonService;

  @GetMapping
  public ResponseEntity<List<Salon>> getAllSalons() {
    List<Salon> salons = salonService.getSalons();
    return ResponseEntity.status(HttpStatus.OK).body(salons);
  }

  @GetMapping("/{publicId}")
  public ResponseEntity<Salon> getSalonByPublicId(@PathVariable String publicId) {
    Salon salon = salonService.getSalonByPublicId(publicId);
    return ResponseEntity.status(HttpStatus.OK).body(salon);
  }

  @PostMapping
  public ResponseEntity<Salon> createSalon(@Valid @RequestBody CreateSalonRequest request) {
    Salon salon = salonService.createSalon(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(salon);
  }
}
