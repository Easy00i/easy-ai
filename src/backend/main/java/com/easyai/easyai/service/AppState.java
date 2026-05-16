package com.easyai.easyai.service;

import com.easyai.easyai.model.User;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AppState {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public Map<String, User> users() { return users; }

    public User getBySession(HttpSession session) {
        Object username = session.getAttribute("username");
        if (username == null) return null;
        return users.get(username.toString());
    }

    public void setSession(HttpSession session, User user) {
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());
    }

    public boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute("role");
        return role != null && "admin".equals(role.toString());
    }
}
