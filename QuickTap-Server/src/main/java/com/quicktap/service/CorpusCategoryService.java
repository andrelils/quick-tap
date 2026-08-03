package com.quicktap.service;

import com.quicktap.dto.CorpusCategoryDTO;
import com.quicktap.dto.CreateCorpusCategoryRequest;
import com.quicktap.dto.UpdateCorpusCategoryRequest;
import com.quicktap.entity.CorpusCategory;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.CorpusCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 语料库分类服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CorpusCategoryService {

    private final CorpusCategoryMapper corpusCategoryMapper;

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "corpus_categories", key = "#merchantId")
    public CorpusCategoryDTO create(Long merchantId, CreateCorpusCategoryRequest request) {
        log.info("创建分类 | merchantId: {} | name: {}", merchantId, request.getName());
        CorpusCategory existing = corpusCategoryMapper.selectByName(request.getName(), merchantId);
        if (existing != null) {
            throw new BusinessException("分类名称已存在");
        }
        CorpusCategory category = CorpusCategory.builder()
                .merchantId(merchantId)
                .name(request.getName())
                .sortOrder(request.getSortOrder())
                .description(request.getDescription())
                .corpusCount(0)
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        corpusCategoryMapper.insert(category);
        log.info("分类创建成功 | id: {} | name: {}", category.getId(), category.getName());
        return convertToDTO(category);
    }

    public CorpusCategoryDTO getById(Long id, Long merchantId) {
        CorpusCategory category = corpusCategoryMapper.selectById(id, merchantId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return convertToDTO(category);
    }

    @Cacheable(value = "corpus_categories", condition = "#merchantId != null", key = "#merchantId", unless = "#result == null")
    public List<CorpusCategoryDTO> listByMerchantId(Long merchantId) {
        List<CorpusCategory> categories = corpusCategoryMapper.selectByMerchantId(merchantId);
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"corpus_categories", "corpus_category"}, allEntries = true)
    public CorpusCategoryDTO update(Long id, Long merchantId, UpdateCorpusCategoryRequest request) {
        log.info("更新分类 | id: {} | merchantId: {}", id, merchantId);
        CorpusCategory category = corpusCategoryMapper.selectById(id, merchantId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (request.getName() != null) category.setName(request.getName());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getEnabled() != null) category.setEnabled(request.getEnabled());
        category.setUpdatedAt(LocalDateTime.now());
        corpusCategoryMapper.update(category);
        log.info("分类更新成功 | id: {}", id);
        return convertToDTO(category);
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"corpus_categories", "corpus_category"}, allEntries = true)
    public void delete(Long id, Long merchantId) {
        log.info("删除分类 | id: {} | merchantId: {}", id, merchantId);
        CorpusCategory category = corpusCategoryMapper.selectById(id, merchantId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (category.getCorpusCount() != null && category.getCorpusCount() > 0) {
            throw new BusinessException("该分类下存在语料，无法删除");
        }
        corpusCategoryMapper.deleteById(id, merchantId);
        log.info("分类删除成功 | id: {}", id);
    }

    private CorpusCategoryDTO convertToDTO(CorpusCategory category) {
        return CorpusCategoryDTO.builder()
                .id(category.getId() != null ? Long.valueOf(category.getId()) : null)
                .name(category.getName())
                .merchantId(category.getMerchantId() != null ? Long.valueOf(category.getMerchantId().intValue()) : null)
                .sortOrder(category.getSortOrder())
                .description(category.getDescription())
                .corpusCount(category.getCorpusCount())
                .enabled(category.getEnabled())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
