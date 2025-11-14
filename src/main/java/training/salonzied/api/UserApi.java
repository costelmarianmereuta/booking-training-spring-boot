package training.salonzied.api;

import com.salonized.dto.CreateUserRequest;
import com.salonized.dto.UpdateUserRequest;
import com.salonized.dto.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import training.salonzied.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserApi {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getUsers(){
        List<User> users=userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request){
        User user=userService.addUser(request);
        return ResponseEntity.ok(user);
    }
    @PutMapping("/{email}")
    public ResponseEntity<User> updateUser(@Valid @RequestBody UpdateUserRequest request, @PathVariable String email){
        User user=userService.updateUser(request, email);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email){
        User user=userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email){
        userService.deleteUser(email);
        return ResponseEntity.status(204).build();
    }
}
