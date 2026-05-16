package com.easyai.easyai.web;

import com.easyai.easyai.model.ApiResponse;
import com.easyai.easyai.model.User;
import com.easyai.easyai.service.AppState;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CoinController {
    private final AppState state;

    public CoinController(AppState state) {
        this.state = state;
    }

    @PostMapping("/coins/buy")
    public ApiResponse<Map<String, Object>> buyCoins(@RequestBody Map<String, Object> body, HttpSession session) {
        User user = state.getBySession(session);
        if (user == null) return ApiResponse.fail("Login required");

        int coins = Integer.parseInt(String.valueOf(body.getOrDefault("coins", 0)));
        int price = Integer.parseInt(String.valueOf(body.getOrDefault("price", 0)));
        if (coins <= 0) return ApiResponse.fail("Invalid coin pack");

        user.setCoins(user.getCoins() + coins);
        Map<String, Object> data = new HashMap<>();
        data.put("coins", user.getCoins());
        data.put("added", coins);
        data.put("price", price);
        return ApiResponse.ok("Coins purchased", data);
    }

    @PostMapping("/premium/buy")
    public ApiResponse<Map<String, Object>> buyPremium(@RequestBody Map<String, Object> body, HttpSession session) {
        User user = state.getBySession(session);
        if (user == null) return ApiResponse.fail("Login required");

        int plan = Integer.parseInt(String.valueOf(body.getOrDefault("plan", 0)));
        if (plan <= 0) return ApiResponse.fail("Select a premium plan");

        user.setPremium(true);
        Map<String, Object> data = new HashMap<>();
        data.put("plan", plan);
        data.put("premium", true);
        return ApiResponse.ok("Premium activated", data);
    }

    @GetMapping("/me/balance")
    public ApiResponse<Map<String, Object>> balance(HttpSession session) {
        User user = state.getBySession(session);
        if (user == null) return ApiResponse.fail("Login required");
        Map<String, Object> data = new HashMap<>();
        data.put("coins", user.getCoins());
        data.put("premium", user.isPremium());
        return ApiResponse.ok("Balance", data);
    }
}
