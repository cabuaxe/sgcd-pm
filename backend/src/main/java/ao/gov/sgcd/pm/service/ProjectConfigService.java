package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.entity.ProjectConfig;
import ao.gov.sgcd.pm.repository.ProjectConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectConfigService {

    private final ProjectConfigRepository configRepository;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadCache() {
        configRepository.findAll().forEach(c -> cache.put(c.getConfigKey(), c.getConfigValue()));
        log.info("Configurações do projecto carregadas: {} entradas", cache.size());
    }

    public String get(String key, String defaultValue) {
        return cache.getOrDefault(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String val = cache.get(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public LocalDate getDate(String key, LocalDate defaultValue) {
        String val = cache.get(key);
        if (val == null) return defaultValue;
        try {
            return LocalDate.parse(val);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public int getTotalSessions() {
        return getInt("total_sessions", 204);
    }

    public int getTotalHoursPlanned() {
        return getInt("total_hours_planned", 680);
    }

    public LocalDate getStartDate() {
        return getDate("start_date", LocalDate.parse("2026-03-02"));
    }

    public LocalDate getTargetDate() {
        return getDate("target_date", LocalDate.parse("2026-12-20"));
    }
}
