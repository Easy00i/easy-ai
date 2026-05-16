package com.easyai.easyai.web;

import com.easyai.easyai.model.ApiResponse;
import com.easyai.easyai.model.User;
import com.easyai.easyai.service.AppState;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AppState state;

    public AuthController(AppState state) {
        this.state = state;
    }

    public record AuthRequest(@NotBlank String username, @NotBlank String email, @NotBlank String password) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody AuthRequest req, HttpSession session) {
        if (state.users().containsKey(req.username())) {
            return ApiResponse.fail("Username already exists");
        }
        User user = new User(req.username(), req.email(), req.password(), "user");
        user.setCoins(50);
        state.users().put(user.getUsername(), user);
        state.setSession(session, user);

        Map<String, Object> data = publicUser(user);
        return ApiResponse.ok("Registered", data);
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest req, HttpSession session) {
        if ("admin@easyai.local".equalsIgnoreCase(req.email()) && "admin123".equals(req.password())) {
            User admin = state.users().computeIfAbsent("admin", k -> new User("admin", req.email(), req.password(), "admin"));
            admin.setRole("admin");
            admin.setPremium(true);
            admin.setCoins(999999);
            state.setSession(session, admin);
            return ApiResponse.ok("Admin login successful", publicUser(admin));
        }

        User found = state.users().values().stream()
                .filter(u -> (u.getEmail() != null && u.getEmail().equalsIgnoreCase(req.email())) || req.password().equals(u.getPassword()))
                .findFirst().orElse(null);

        if (found == null) {
            return ApiResponse.fail("Account not found. Please register first.");
        }

        state.setSession(session, found);
        return ApiResponse.ok("Login successful", publicUser(found));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(HttpSession session) {
        User user = state.getBySession(session);
        if (user == null) return ApiResponse.fail("Not logged in");
        return ApiResponse.ok("Current user", publicUser(user));
    }

    private Map<String, Object> publicUser(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("premium", user.isPremium());
        data.put("coins", user.getCoins());
        return data;
    }
}
