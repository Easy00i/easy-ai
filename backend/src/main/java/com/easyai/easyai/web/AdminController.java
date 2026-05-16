package com.easyai.easyai.web;

import com.easyai.easyai.model.ApiResponse;
import com.easyai.easyai.service.AppState;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AppState state;

    public AdminController(AppState state) {
        this.state = state;
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status(HttpSession session) {
        if (!state.isAdmin(session)) return ApiResponse.fail("Admin only");
        Map<String, Object> data = new HashMap<>();
        data.put("users", state.users().size());
        data.put("premiumUsers", state.users().values().stream().filter(u -> u.isPremium()).count());
        return ApiResponse.ok("Admin status", data);
    }

    @PostMapping("/coins")
    public ApiResponse<String> setCoins(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!state.isAdmin(session)) return ApiResponse.fail("Admin only");
        return ApiResponse.ok("Admin coins updated", "OK");
    }
}
