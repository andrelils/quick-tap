package com.quicktap.service;

import com.quicktap.dto.AiConfigDTO;
import com.quicktap.dto.CreateOrUpdateAiConfigRequest;
import com.quicktap.dto.PageResponse;
import com.quicktap.entity.AiConfig;
import com.quicktap.entity.Corpus;
import com.quicktap.entity.Merchant;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.AiConfigMapper;
import com.quicktap.mapper.CorpusMapper;
import com.quicktap.mapper.MerchantMapper;
import com.quicktap.utils.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 配置 Service
 */
@Slf4j
@Service
public class AiConfigService {

    @Autowired
    private AiConfigMapper aiConfigMapper;

    @Autowired
    private EncryptUtil encryptUtil;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private CorpusMapper corpusMapper;

    /**
     * 获取全局 AI 配置（不存在则返回默认空配置，避免前端报错）
     */
    @Cacheable(value = "ai_config_global", unless = "#result == null")
    public AiConfigDTO getGlobalConfig() {
        AiConfig config = aiConfigMapper.selectGlobalConfig();
        if (config == null) {
            return AiConfigDTO.builder()
                    .id(null)
                    .merchantId(null)
                    .textModel("")
                    .imageModel("")
                    .videoModel("")
                    .enabled(false)
                    .build();
        }
        return convertToDTO(config);
    }

    /**
     * 获取商户 AI 配置（如果没有则使用全局配置；都没有则返回默认空配置）
     * 只在 merchantId 不为 null 时缓存
     */
    @Cacheable(value = "ai_config", key = "#p0", condition = "#p0 != null", unless = "#result == null")
    public AiConfigDTO getConfigByMerchantId(Long merchantId) {
        AiConfig config = merchantId != null ? aiConfigMapper.selectByMerchantId(merchantId) : null;
        if (config == null) {
            // 使用全局配置
            config = aiConfigMapper.selectGlobalConfig();
        }
        if (config == null) {
            return AiConfigDTO.builder()
                    .id(null)
                    .merchantId(merchantId)
                    .textModel("")
                    .imageModel("")
                    .videoModel("")
                    .enabled(false)
                    .build();
        }
        return convertToDTO(config);
    }

    /**
     * 创建或更新商户 AI 配置
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "ai_config", key = "#p0", condition = "#p0 != null")
    public AiConfigDTO createOrUpdateMerchantConfig(Long merchantId, CreateOrUpdateAiConfigRequest request) {
        log.info("创建/更新商户 AI 配置 | merchantId: {}", merchantId);

        AiConfig existing = aiConfigMapper.selectByMerchantId(merchantId);
        AiConfig config;

        if (existing != null) {
            existing.setTextModel(request.getTextModel());
            existing.setImageModel(request.getImageModel());
            existing.setVideoModel(request.getVideoModel());
            if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
                existing.setApiKey(encryptUtil.encrypt(request.getApiKey()));
            }
            if (request.getApiSecret() != null && !request.getApiSecret().isEmpty()) {
                existing.setApiSecret(encryptUtil.encrypt(request.getApiSecret()));
            }
            if (request.getEnabled() != null) {
                existing.setEnabled(request.getEnabled());
            }
            existing.setUpdatedAt(LocalDateTime.now());
            aiConfigMapper.update(existing);
            config = existing;
        } else {
            config = AiConfig.builder()
                    .merchantId(merchantId)
                    .textModel(request.getTextModel())
                    .imageModel(request.getImageModel())
                    .videoModel(request.getVideoModel())
                    .apiKey(request.getApiKey() != null ? encryptUtil.encrypt(request.getApiKey()) : null)
                    .apiSecret(request.getApiSecret() != null ? encryptUtil.encrypt(request.getApiSecret()) : null)
                    .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            aiConfigMapper.insert(config);
        }

        log.info("商户 AI 配置保存成功 | merchantId: {}", merchantId);
        return convertToDTO(config);
    }

    /**
     * 更新全局 AI 配置（仅超级管理员），不存在则自动创建
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "ai_config_global")
    public AiConfigDTO updateGlobalConfig(CreateOrUpdateAiConfigRequest request) {
        log.info("更新全局 AI 配置");

        AiConfig config = aiConfigMapper.selectGlobalConfig();
        if (config == null) {
            // 全局配置不存在，自动创建
            log.info("全局 AI 配置不存在，自动创建");
            config = AiConfig.builder()
                    .merchantId(null)
                    .textModel(request.getTextModel())
                    .imageModel(request.getImageModel())
                    .videoModel(request.getVideoModel())
                    .apiKey(request.getApiKey() != null ? encryptUtil.encrypt(request.getApiKey()) : null)
                    .apiSecret(request.getApiSecret() != null ? encryptUtil.encrypt(request.getApiSecret()) : null)
                    .enabled(request.getEnabled() != null ? request.getEnabled() : false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            aiConfigMapper.insert(config);
        } else {
            config.setTextModel(request.getTextModel());
            config.setImageModel(request.getImageModel());
            config.setVideoModel(request.getVideoModel());
            if (request.getApiKey() != null && !request.getApiKey().isEmpty()) {
                config.setApiKey(encryptUtil.encrypt(request.getApiKey()));
            }
            if (request.getApiSecret() != null && !request.getApiSecret().isEmpty()) {
                config.setApiSecret(encryptUtil.encrypt(request.getApiSecret()));
            }
            if (request.getEnabled() != null) {
                config.setEnabled(request.getEnabled());
            }
            config.setUpdatedAt(LocalDateTime.now());
            aiConfigMapper.update(config);
        }

        log.info("全局 AI 配置更新成功");
        return convertToDTO(config);
    }

    /**
     * 删除商户 AI 配置
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "ai_config", key = "#p0", condition = "#p0 != null")
    public void deleteMerchantConfig(Long merchantId) {
        log.info("删除商户 AI 配置 | merchantId: {}", merchantId);
        aiConfigMapper.deleteByMerchantId(merchantId);
    }

    /**
     * 检查商户是否有自定义配置
     */
    public boolean hasMerchantConfig(Long merchantId) {
        return aiConfigMapper.countByMerchantId(merchantId) > 0;
    }

    /**
     * 分页查询所有商户级 AI 配置（管理员视角的总览）
     * 前端 ai.js#getMerchantConfigList 调用
     */
    public PageResponse<AiConfigDTO> getMerchantConfigList(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        int offset = (pageNum - 1) * pageSize;

        List<AiConfig> configs = aiConfigMapper.selectMerchantPage(offset, pageSize);
        long total = aiConfigMapper.countMerchantConfigs();

        List<AiConfigDTO> dtoList = configs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.<AiConfigDTO>builder()
                .list(dtoList)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .totalPage((int) Math.ceil((double) total / pageSize))
                .build();
    }

    /**
     * 商家 AI 配置总览（所有商家，含 AI 配置与语料统计）
     * 前端"商家配置总览"表格使用
     */
    public PageResponse<Map<String, Object>> getMerchantOverview(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        int offset = (pageNum - 1) * pageSize;

        List<Merchant> merchants = merchantMapper.selectPage(offset, pageSize);
        long total = merchantMapper.countAll();

        List<Map<String, Object>> list = merchants.stream().map(m -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", m.getId());
            item.put("name", m.getName());
            item.put("logo", m.getLogo());
            item.put("merchantStatus", m.getStatus());

            AiConfig config = aiConfigMapper.selectByMerchantId(m.getId().longValue());
            boolean hasConfig = config != null && Boolean.TRUE.equals(config.getEnabled());
            item.put("hasConfig", hasConfig);

            Map<String, Object> cfg = new HashMap<>();
            if (config != null) {
                cfg.put("textModel", config.getTextModel());
                cfg.put("imageModel", config.getImageModel());
                cfg.put("videoModel", config.getVideoModel());
                cfg.put("textPrompt", config.getTextPrompt());
                cfg.put("imagePrompt", config.getImagePrompt());
                cfg.put("videoPrompt", config.getVideoPrompt());
            }
            item.put("config", cfg);

            List<Corpus> corpora = corpusMapper.selectByMerchantId(m.getId());
            int textCount = 0, imageCount = 0, videoCount = 0;
            long totalSize = 0;
            if (corpora != null) {
                for (Corpus c : corpora) {
                    if ("text".equals(c.getType())) textCount++;
                    else if ("image".equals(c.getType())) imageCount++;
                    else if ("video".equals(c.getType())) videoCount++;
                    if (c.getFileSize() != null) totalSize += c.getFileSize();
                }
            }
            Map<String, Object> stats = new HashMap<>();
            stats.put("textCount", textCount);
            stats.put("imageCount", imageCount);
            stats.put("videoCount", videoCount);
            stats.put("totalSize", totalSize);
            item.put("corpusStats", stats);
            return item;
        }).collect(Collectors.toList());

        return PageResponse.<Map<String, Object>>builder()
                .list(list)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .totalPage((int) Math.ceil((double) total / pageSize))
                .build();
    }

    /**
     * 转换为 DTO
     */
    private AiConfigDTO convertToDTO(AiConfig config) {
        return AiConfigDTO.builder()
                .id(Long.valueOf(config.getId()))
                .merchantId(config.getMerchantId())
                .textModel(config.getTextModel())
                .imageModel(config.getImageModel())
                .videoModel(config.getVideoModel())
                .enabled(config.getEnabled())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
