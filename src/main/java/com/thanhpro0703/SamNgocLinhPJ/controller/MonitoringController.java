package com.thanhpro0703.SamNgocLinhPJ.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Controller cho phép admin giám sát hiệu suất hệ thống
 */
@RestController
@RequestMapping("/api/admin/monitoring")
@PreAuthorize("hasRole('ADMIN')")
public class MonitoringController {

    @Autowired
    private CacheManager cacheManager;

    /**
     * Lấy thông tin tổng quan về hiệu suất của hệ thống
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Thông tin JVM
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeBean.getUptime();
        
        info.put("uptime", formatDuration(uptime));
        info.put("jvm_version", runtimeBean.getVmName() + " " + runtimeBean.getVmVersion());
        
        // Thông tin bộ nhớ
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("heap_used", memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024) + " MB");
        memoryInfo.put("heap_max", memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024) + " MB");
        memoryInfo.put("non_heap_used", memoryBean.getNonHeapMemoryUsage().getUsed() / (1024 * 1024) + " MB");
        
        info.put("memory", memoryInfo);
        
        // Thông tin CPU
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        
        Map<String, Object> cpuInfo = new HashMap<>();
        cpuInfo.put("available_processors", osBean.getAvailableProcessors());
        cpuInfo.put("system_load_average", osBean.getSystemLoadAverage());
        
        info.put("cpu", cpuInfo);
        
        // Thông tin cache
        Map<String, Object> cacheInfo = new HashMap<>();
        cacheManager.getCacheNames().forEach(name -> {
            cacheInfo.put(name, "active");
        });
        
        info.put("caches", cacheInfo);
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * Format duration từ milliseconds thành dạng dễ đọc (ngày, giờ, phút, giây)
     */
    private String formatDuration(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        
        return String.format("%d ngày, %d giờ, %d phút, %d giây", 
                days, hours, minutes, seconds);
    }
} 