package com.cuppa.CuppaApp.service;

import org.springframework.stereotype.Service;

/**
 * Сервис для защиты от XSS (Cross-Site Scripting) атак.
 * Предоставляет методы для очистки пользовательского ввода и проверки на наличие XSS угроз.
 * Реализует базовые меры защиты путем санитизации входных данных.
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 28.10.2025
 */
@Service
public class XSSProtectionService {

    /**
     * Очищает строку от потенциально опасного HTML/JS кода.
     * Заменяет опасные конструкции на безопасные эквиваленты.
     *
     * @param input входная строка, которую необходимо очистить
     * @return очищенная строка или null, если входная строка была null
     */
    public String sanitize(String input) {
        if (input == null) return null;

        // Удаляем опасные теги и атрибуты
        return input.replaceAll("<script>", "&lt;script&gt;")
                .replaceAll("</script>", "&lt;/script&gt;")
                .replaceAll("javascript:", "")
                .replaceAll("onclick=", "data-onclick=")
                .replaceAll("onload=", "data-onload=")
                .replaceAll("onerror=", "data-onerror=");
    }

    /**
     * Проверяет строку на наличие потенциальных XSS угроз.
     * Определяет наличие опасных паттернов в строке.
     *
     * @param input входная строка для проверки на XSS угрозы
     * @return true если в строке обнаружены потенциальные XSS угрозы, иначе false
     */
    public boolean hasXSSThreats(String input) {
        if (input == null) return false;

        String lowerInput = input.toLowerCase();
        return lowerInput.contains("<script>") ||
                lowerInput.contains("javascript:") ||
                lowerInput.contains("onclick") ||
                lowerInput.contains("onload") ||
                lowerInput.contains("onerror");
    }
}