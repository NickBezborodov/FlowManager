package com.example.flowmanager.service.impl;

import com.example.flowmanager.service.SubscriptionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionCacheServiceImpl implements SubscriptionCacheService {
    private final CacheManager cacheManager;

    @Override
    public void evictCache(String login) {
        log.info("🗑️ Очищаем кеш для пользователя: {}", login);

        try {
            cacheManager.getCache("subscriptions").evict(login);
            log.info("✅ Кеш очищен для: {}", login);
        } catch (Exception e) {
            log.error("❌ Ошибка при очистке кеша для {}: {}", login, e.getMessage());
        }
    }
}
