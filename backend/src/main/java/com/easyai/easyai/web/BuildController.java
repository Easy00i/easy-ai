package com.easyai.easyai.web;

import com.easyai.easyai.model.ApiResponse;
import com.easyai.easyai.model.User;
import com.easyai.easyai.service.AppState;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/build")
public class BuildController {
    private final AppState state;

    public BuildController(AppState state) {
        this.state = state;
    }

    public record BuildRequest(String pluginName, String version, String mainClass, String packageName) {}

    @PostMapping
    public ApiResponse<Map<String, Object>> build(@RequestBody BuildRequest req, HttpSession session) {
        User user = state.getBySession(session);
        if (user == null) return ApiResponse.fail("Login required");

        try {
            Path work = Files.createTempDirectory("easyai-build-");
            Path src = work.resolve("src/main/java");
            Path res = work.resolve("src/main/resources");
            Path pkg = src.resolve((req.packageName() == null || req.packageName().isBlank() ? "com.easyai.generated" : req.packageName().trim()).replace('.', '/'));
            Files.createDirectories(pkg);
            Files.createDirectories(res);

            String pkgName = req.packageName() == null || req.packageName().isBlank() ? "com.easyai.generated" : req.packageName().trim();
            String mainClass = req.mainClass() == null || req.mainClass().isBlank() ? "GeneratedPlugin" : req.mainClass().trim();
            String pluginName = req.pluginName() == null || req.pluginName().isBlank() ? "GeneratedPlugin" : req.pluginName().trim();
            String version = req.version() == null || req.version().isBlank() ? "1.20.1" : req.version().trim();

            Files.writeString(work.resolve("pom.xml"), buildPom(pluginName), StandardCharsets.UTF_8);
            Files.writeString(res.resolve("plugin.yml"), pluginYml(pluginName, version, pkgName + "." + mainClass), StandardCharsets.UTF_8);
            Files.writeString(pkg.resolve(mainClass + ".java"), javaClass(pkgName, mainClass), StandardCharsets.UTF_8);

            boolean mvnOk = runMavenIfAvailable(work);

            Map<String, Object> data = new HashMap<>();
            data.put("workspace", work.toString());
            data.put("jarReady", mvnOk);
            data.put("message", mvnOk ? "Jar build completed" : "Source generated. Maven not available on this machine.");
            data.put("pluginName", pluginName);
            data.put("version", version);
            return ApiResponse.ok("Build finished", data);
        } catch (Exception ex) {
            return ApiResponse.fail("Build failed: " + ex.getMessage());
        }
    }

    private String buildPom(String name) {
        return """<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.easyai</groupId>
  <artifactId>generated-plugin</artifactId>
  <version>1.0.0</version>
  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
  </properties>
  <dependencies/>
</project>""";
    }

    private String pluginYml(String name, String version, String main) {
        return "name: " + name + "\nversion: " + version + "\nmain: " + main + "\napi-version: '1.20'\n";
    }

    private String javaClass(String pkg, String main) {
        return "package " + pkg + ";\n\npublic class " + main + " {\n    public String name() { return \"" + main + "\"; }\n}\n";
    }

    private boolean runMavenIfAvailable(Path work) throws IOException, InterruptedException {
        Process probe = new ProcessBuilder("mvn", "-v").redirectErrorStream(true).start();
        int probeCode = probe.waitFor();
        if (probeCode != 0) return false;
        Process p = new ProcessBuilder("mvn", "-q", "-DskipTests", "package")
                .directory(work.toFile())
                .redirectErrorStream(true)
                .start();
        return p.waitFor() == 0;
    }
}
