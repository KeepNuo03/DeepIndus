package com.induscore.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 缺陷类型映射工具。
 *
 * 统一内部存储为数据集原始类别（英文），对外展示为中文。
 * 标准类型：crazing, inclusion, patches, pitted_surface, rolled-in_scale, scratches。
 */
public final class DefectTypeMapper {
    private static final Map<String, String> DISPLAY_MAP = Map.of(
            "crazing", "龟裂",
            "inclusion", "夹杂",
            "patches", "斑块",
            "pitted_surface", "麻点",
            "rolled-in_scale", "氧化皮卷入",
            "scratches", "划伤"
    );

    private static final Map<String, String> NORMALIZE_MAP = buildNormalizeMap();

    private static Map<String, String> buildNormalizeMap() {
        Map<String, String> m = new HashMap<>();
        // 中文 → 标准英文
        m.put("龟裂", "crazing");
        m.put("夹杂", "inclusion");
        m.put("斑块", "patches");
        m.put("麻点", "pitted_surface");
        m.put("氧化皮卷入", "rolled-in_scale");
        m.put("划伤", "scratches");
        // 别名 / 错误拼写 → 标准英文
        m.put("scratch", "scratches");
        m.put("oversize", "patches");
        m.put("crack", "crazing");
        m.put("patch", "patches");
        m.put("pitted", "pitted_surface");
        m.put("pitted-surface", "pitted_surface");
        m.put("scale", "rolled-in_scale");
        m.put("表面划伤", "scratches");
        m.put("边缘裂纹", "crazing");
        return Map.copyOf(m);
    }

    private DefectTypeMapper() {}

    /**
     * 将输入类型规范化为数据集原始类别（标准英文）。
     * 支持中文、别名（如 scratch、oversize、crack）映射到 6 种标准类型。
     */
    public static String normalize(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String trimmed = input.trim();
        String lower = trimmed.toLowerCase();
        
        // 1. 如果已经是标准类型（小写），直接返回
        if (DISPLAY_MAP.containsKey(lower)) {
            return lower;
        }
        
        // 2. 先尝试原始值（保留大小写）的映射
        if (NORMALIZE_MAP.containsKey(trimmed)) {
            return NORMALIZE_MAP.get(trimmed);
        }
        
        // 3. 再尝试小写值的映射（处理大小写不一致的情况）
        if (NORMALIZE_MAP.containsKey(lower)) {
            return NORMALIZE_MAP.get(lower);
        }
        
        // 4. 如果都不匹配，返回小写后的值（统一格式）
        // 注意：如果原始值不在标准类型中，这里返回小写值，前端可能会显示原始值
        return lower;
    }

    /**
     * 将原始类别转换为中文展示名称。
     */
    public static String displayName(String type) {
        if (type == null || type.isBlank()) {
            return "";
        }
        String key = type.trim().toLowerCase();
        return DISPLAY_MAP.getOrDefault(key, type);
    }
}
