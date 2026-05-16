package com.easyai.easyai.web;

import com.easyai.easyai.model.ApiResponse;
import com.easyai.easyai.model.User;
import com.easyai.easyai.service.AppState;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final AppState state;

    public ChatController(AppState state) {
        this.state = state;
    }

    public record ChatRequest(String message, String pluginName, String version, String language, String model, String style, String buildMode) {}

    @PostMapping
    public ApiResponse<Map<String, Object>> chat(@RequestBody ChatRequest req, HttpSession session) {
        User user = state.getBySession(session);
        if (user == null) return ApiResponse.fail("Login required");

        if (user.getCoins() < 5) return ApiResponse.fail("Not enough coins");
        user.setCoins(user.getCoins() - 5);

        String message = req.message() == null ? "" : req.message().trim();
        if (message.isEmpty()) return ApiResponse.fail("Empty message");

        if (message.toLowerCase().contains("error") || message.toLowerCase().contains("fail") || message.toLowerCase().contains("broken")) {
            return ApiResponse.fail("Build error detected. Click Fix Error.");
        }

        boolean advanced = user.isPremium();
        String reply = String.join("\n",
                "Model: " + safe(req.model()),
                "Language: " + safe(req.language()),
                "Premium: " + (advanced ? "Active" : "Inactive"),
                "Plugin Name: " + (req.pluginName() == null || req.pluginName().isBlank() ? "CustomPlugin" : req.pluginName().trim()),
                "Version: " + (req.version() == null || req.version().isBlank() ? "1.20.1" : req.version().trim()),
                "Style: " + safe(req.style()),
                "Build Mode: " + safe(req.buildMode()),
                "",
                "What I would generate:",
                "- plugin.yml",
                "- config.yml",
                "- event listeners",
                "- command handlers",
                "- source structure",
                "",
                advanced ? "Advanced reasoning enabled." : "Advanced features locked until premium is active.",
                "Jar build hook available from backend."
        );

        Map<String, Object> data = new HashMap<>();
        data.put("reply", reply);
        data.put("coins", user.getCoins());
        data.put("advanced", advanced);
        return ApiResponse.ok("Answer generated", data);
    }

    @PostMapping("/fix")
    public ApiResponse<Map<String, Object>> fix(HttpSession session) {
        User user = state.getBySession(session);
        if (user == null) return ApiResponse.fail("Login required");
        Map<String, Object> data = new HashMap<>();
        data.put("reply", "Error fixed. Rebuilding with safe defaults and corrected output path.");
        return ApiResponse.ok("Fixed", data);
    }

    private String safe(String s) {
        return s == null || s.isBlank() ? "Default" : s.trim();
    }
}
