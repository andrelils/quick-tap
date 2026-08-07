package com.quicktap.service;

import com.quicktap.dto.PageResponse;
import com.quicktap.entity.Corpus;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.CorpusMapper;
import com.quicktap.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 知识库内容管理服务 - 企业知识库和文档管理系统
 *
 * 职责：
 * - 管理知识库内容的完整生命周期（创建、读取、更新、删除）
 * - 支持软删除和硬删除两种删除策略
 * - 实现内容分类和分类浏览功能
 * - 提供全文搜索能力（关键词匹配、相关性排序）
 * - 管理回收站（已删除内容的恢复）
 * - 跟踪内容访问统计（浏览次数）
 *
 * 核心方法：
 * - createCorpus(Corpus) - 创建新的知识库内容
 * - getCorpus(String) - 获取单条内容详情（自动增加浏览次数）
 * - updateCorpus(Corpus) - 更新内容信息
 * - deleteCorpus(String) - 软删除内容（可恢复）
 * - permanentDelete(String) - 硬删除内容（永久删除）
 * - getMerchantCorpus(Integer, int, int) - 分页查询商户知识库
 * - getByCategory(String, int, int) - 按分类浏览内容
 * - getTrash(Integer, int, int) - 查看回收站内容
 * - restoreCorpus(String) - 从回收站恢复内容
 * - search(String, int, int) - 全文搜索和相关性排序
 *
 * 内容生命周期：
 * {@code
 * 创建
 *   ├─ 验证标题和内容不为空
 *   ├─ 生成唯一 corpusId (UUID)
 *   ├─ 设置初始状态为 1 (正常)
 *   └─ 插入数据库
 *
 * 更新
 *   ├─ 验证内容存在
 *   ├─ 更新指定字段（标题、内容等）
 *   └─ 持久化到数据库
 *
 * 软删除
 *   ├─ 标记 status = 0 (已删除)
 *   ├─ 不从数据库移除物理行
 *   ├─ 可通过 restoreCorpus() 恢复
 *   └─ 在主列表中隐藏
 *
 * 硬删除
 *   ├─ 从数据库永久删除物理行
 *   ├─ 无法恢复
 *   └─ 仅在需要时使用
 *
 * 恢复
 *   ├─ 从回收站选择内容
 *   ├─ 标记 status = 1 (正常)
 *   └─ 重新在主列表中显示
 * }</n
 *
 * 搜索功能详解：
 * {@code
 * // 全文搜索流程
 * 1. 验证关键词不为空
 * 2. 标准化关键词（小写、去除空白）
 * 3. 加载所有内容（仅包含 status=1 的活跃内容）
 * 4. 计算每个内容的相关性评分
 *    - 标题精确匹配：+10分
 *    - 标题开头匹配：+5分
 *    - 内容匹配基础：+3分
 *    - 每个关键词出现：+1分
 * 5. 按相关性从高到低排序
 * 6. 应用分页逻辑
 * 7. 返回分页结果
 * }</n
 *
 * 相关性评分算法：
 * {@code
 * 评分 = 标题权重 + 内容权重 + 频次奖励
 *
 * 标题权重：
 *   - 标题包含关键词：+10分（高权重）
 *   - 标题以关键词开头：+5分（位置奖励）
 *
 * 内容权重：
 *   - 内容包含关键词：+3分（基础分）
 *   - 每个额外出现：+1分（频次奖励）
 *
 * 示例：
 *   标题"Java编程指南"，搜索"Java"
 *   → 10分（标题精确匹配）+ 5分（标题开头）= 15分
 *
 *   内容中"Java"出现5次
 *   → 3分（基础）+ 5分（频次）= 8分
 * }</n
 *
 * 使用场景：
 * {@code
 * // 场景1: 创建知识库内容
 * Corpus corpus = new Corpus();
 * corpus.setTitle("Java并发编程最佳实践");
 * corpus.setContent("在Java中处理并发的最佳方式...");
 * corpus.setCategory("技术文档");
 * corpus.setMerchantId(merchantId);
 * Corpus created = corpusService.createCorpus(corpus);
 *
 * // 场景2: 搜索内容
 * PageResponse<Corpus> results = corpusService.search("Java", 1, 20);
 * // 返回匹配"Java"的内容，按相关性排序
 *
 * // 场景3: 浏览分类
 * PageResponse<Corpus> category = corpusService.getByCategory("技术文档", 1, 10);
 *
 * // 场景4: 获取内容详情（自动增加浏览次数）
 * Corpus detail = corpusService.getCorpus(corpusId);
 * // viewCount 自动加1
 *
 * // 场景5: 软删除和恢复
 * corpusService.deleteCorpus(corpusId);  // 标记为删除
 * List<Corpus> trash = corpusService.getTrash(merchantId, 1, 20);  // 查看删除的
 * corpusService.restoreCorpus(corpusId);  // 从回收站恢复
 * }</n
 *
 * 状态管理：
 * - status = 1: 正常状态，在主列表和搜索结果中可见
 * - status = 0: 已删除状态，仅在回收站中可见，搜索时被排除
 *
 * 唯一标识：
 * - id: 数据库主键（自增长）
 * - corpusId: 业务唯一标识（UUID），用于对外接口
 *
 * 浏览统计：
 * - viewCount: 每次 getCorpus() 调用时自动增加
 * - 用于追踪热门内容和用户兴趣
 *
 * 性能考虑：
 * - 搜索功能在内存中进行相关性排序
 * - 大量内容情况下应考虑数据库全文搜索（Elasticsearch等）
 * - 分类浏览在内存中过滤，建议添加数据库索引
 *
 * 错误处理：
 * - 标题/内容为空：400 (Bad Request)
 * - 内容不存在：404 (Not Found)
 * - 数据库操作失败：500 (Internal Server Error)
 * - 搜索异常：500 (Internal Server Error)，返回安全的错误消息
 *
 * 数据库交互：
 * - CorpusMapper.insert() - 插入新内容
 * - CorpusMapper.selectById() - 按数据库ID查询
 * - CorpusMapper.selectByCorpusId() - 按业务ID查询
 * - CorpusMapper.selectByMerchantId() - 查询商户的所有内容
 * - CorpusMapper.selectByCategory() - 按分类查询
 * - CorpusMapper.selectAll() - 加载所有内容（用于搜索）
 * - CorpusMapper.countByMerchantId() - 统计商户内容数
 * - CorpusMapper.update() - 更新内容
 * - CorpusMapper.deleteByCorpusId() - 硬删除内容
 *
 * 集成点：
 * - SearchController: 搜索API接口
 * - ContentController: 内容管理接口
 * - PageResponse: 分页响应包装类
 *
 * @author QuickTap Corpus Team
 * @version 1.0
 * @since 1.0
 * @see CorpusMapper
 * @see Corpus
 * @see PageResponse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CorpusService {

    private final CorpusMapper corpusMapper;

    /**
     * 创建知识库内容
     * @param corpus 内容实体
     * @return 创建的内容
     */
    public Corpus createCorpus(Corpus corpus) {
        if (corpus.getTitle() == null || corpus.getTitle().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "标题不能为空");
        }
        if (corpus.getContent() == null || corpus.getContent().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "内容不能为空");
        }

        corpus.setCorpusId(UUID.randomUUID().toString());
        corpus.setStatus(1);
        corpus.setViewCount(0);

        int result = corpusMapper.insert(corpus);
        if (result <= 0) {
            log.error("知识库内容创建失败: title={}", corpus.getTitle());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "创建失败，请稍后重试");
        }

        log.info("知识库内容创建成功: corpusId={}, merchantId={}", corpus.getCorpusId(), corpus.getMerchantId());
        return corpus;
    }

    /**
     * 获取知识库内容详情
     * @param corpusId 内容ID
     * @return 内容详情
     */
    public Corpus getCorpus(String corpusId) {
        Corpus corpus = findCorpus(corpusId);
        if (corpus == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "内容不存在");
        }

        // 增加浏览次数
        corpus.setViewCount(corpus.getViewCount() + 1);
        corpusMapper.update(corpus);

        return corpus;
    }

    /**
     * 更新知识库内容
     * @param corpus 内容实体
     * @return 更新后的内容
     */
    public Corpus updateCorpus(Corpus corpus) {
        if (corpus.getId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "ID不能为空");
        }

        Corpus existing = corpusMapper.selectById(corpus.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "内容不存在");
        }

        int result = corpusMapper.update(corpus);
        if (result <= 0) {
            log.error("知识库内容更新失败: corpusId={}", corpus.getCorpusId());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "更新失败，请稍后重试");
        }

        log.info("知识库内容更新成功: corpusId={}", corpus.getCorpusId());
        return corpus;
    }

    /**
     * 删除知识库内容（软删除）
     * @param corpusId 内容ID
     */
    public void deleteCorpus(String corpusId) {
        Corpus corpus = findCorpus(corpusId);
        if (corpus == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "内容不存在");
        }

        corpus.setStatus(0);  // 标记为删除
        corpusMapper.update(corpus);

        log.info("知识库内容删除成功: corpusId={}", corpusId);
    }

    /**
     * 永久删除知识库内容
     * @param corpusId 内容ID
     */
    public void permanentDelete(String corpusId) {
        Corpus corpus = findCorpus(corpusId);
        if (corpus == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "内容不存在");
        }

        int result = corpusMapper.deleteById(corpus.getId());
        if (result <= 0) {
            log.error("知识库内容永久删除失败: corpusId={}", corpusId);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "删除失败，请稍后重试");
        }

        log.info("知识库内容永久删除成功: corpusId={}", corpusId);
    }

    /**
     * 兼容数字 id 与 UUID corpusId 两种定位方式
     */
    private Corpus findCorpus(String corpusId) {
        if (corpusId == null || corpusId.trim().isEmpty()) {
            return null;
        }
        Corpus corpus = null;
        try {
            Long numId = Long.parseLong(corpusId.trim());
            corpus = corpusMapper.selectById(numId.intValue());
        } catch (NumberFormatException ignored) {
            // 非数字，按 UUID corpus_id 查询
        }
        if (corpus == null) {
            corpus = corpusMapper.selectByCorpusId(corpusId.trim());
        }
        return corpus;
    }

    /**
     * 获取商户的知识库内容列表
     * @param merchantId 商户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PageResponse<Corpus> getMerchantCorpus(Integer merchantId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;

        long total = corpusMapper.countByMerchantId(merchantId);
        List<Corpus> data = corpusMapper.selectByMerchantId(merchantId);
        return PageResponse.<Corpus>builder()
            .list(data)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .total(total)
            .totalPage((int) Math.ceil((double) total / pageSize))
            .build();
    }

    /**
     * 获取所有知识库内容（管理员端，分页）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PageResponse<Corpus> getAllCorpus(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Corpus> data = corpusMapper.selectPage(offset, pageSize);
        long total = corpusMapper.countAll();
        return PageResponse.<Corpus>builder()
            .list(data)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .total(total)
            .totalPage((int) Math.ceil((double) total / pageSize))
            .build();
    }

    /**
     * 按分类获取知识库内容
     * @param category 分类
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PageResponse<Corpus> getByCategory(String category, int pageNum, int pageSize) {
        if (category == null || category.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "分类不能为空");
        }

        int offset = (pageNum - 1) * pageSize;
        // 使用 SQL 分页，避免全量加载到内存
        List<Corpus> data = corpusMapper.selectByCategoryPage(category, offset, pageSize);
        long total = corpusMapper.countByCategory(category);

        return PageResponse.<Corpus>builder()
            .list(data)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .total(total)
            .totalPage((int) Math.ceil((double) total / pageSize))
            .build();
    }

    /**
     * 获取回收站内容（已删除的内容）
     * @param merchantId 商户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PageResponse<Corpus> getTrash(Integer merchantId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;

        // 使用 SQL 分页查询回收站（status=0），避免全量加载后内存过滤
        List<Corpus> trashData = corpusMapper.selectTrashByMerchantIdPage(merchantId, offset, pageSize);
        long total = corpusMapper.countTrashByMerchantId(merchantId);

        return PageResponse.<Corpus>builder()
            .list(trashData)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .total(total)
            .totalPage((int) Math.ceil((double) total / pageSize))
            .build();
    }

    /**
     * 恢复删除的内容
     * @param corpusId 内容ID
     */
    public void restoreCorpus(String corpusId) {
        Corpus corpus = corpusMapper.selectByCorpusId(corpusId);
        if (corpus == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "内容不存在");
        }

        corpus.setStatus(1);  // 标记为正常
        corpusMapper.update(corpus);

        log.info("知识库内容恢复成功: corpusId={}", corpusId);
    }

    /**
     * 批量软删除（移入回收站）
     * @param corpusIds 内容ID列表
     * @return 成功删除的条数
     */
    public int batchDelete(List<String> corpusIds) {
        if (corpusIds == null || corpusIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "ids 不能为空");
        }
        int success = 0;
        for (String id : corpusIds) {
            try {
                Corpus corpus = corpusMapper.selectByCorpusId(id);
                if (corpus == null) continue;
                corpus.setStatus(0);
                corpusMapper.update(corpus);
                success++;
            } catch (Exception e) {
                log.warn("批量删除失败 corpusId={}: {}", id, e.getMessage());
            }
        }
        log.info("批量软删除完成: 总数={}, 成功={}", corpusIds.size(), success);
        return success;
    }

    /**
     * 批量永久删除
     * @param corpusIds 内容ID列表
     * @return 成功删除的条数
     */
    public int batchPermanentDelete(List<String> corpusIds) {
        if (corpusIds == null || corpusIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "ids 不能为空");
        }
        int success = 0;
        for (String id : corpusIds) {
            try {
                int rows = corpusMapper.deleteByCorpusId(id);
                if (rows > 0) success++;
            } catch (Exception e) {
                log.warn("批量永久删除失败 corpusId={}: {}", id, e.getMessage());
            }
        }
        log.info("批量永久删除完成: 总数={}, 成功={}", corpusIds.size(), success);
        return success;
    }

    /**
     * 批量恢复（从回收站恢复）
     * @param corpusIds 内容ID列表
     * @return 成功恢复的条数
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchRestore(List<String> corpusIds) {
        if (corpusIds == null || corpusIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "ids 不能为空");
        }
        int success = 0;
        for (String id : corpusIds) {
            try {
                Corpus corpus = corpusMapper.selectByCorpusId(id);
                if (corpus == null) continue;
                corpus.setStatus(1);
                corpusMapper.update(corpus);
                success++;
            } catch (Exception e) {
                log.warn("批量恢复失败 corpusId={}: {}", id, e.getMessage());
            }
        }
        log.info("批量恢复完成: 总数={}, 成功={}", corpusIds.size(), success);
        return success;
    }

    /**
     * 语料存储统计
     * 用于 CorpusController#getStorage，统计商户语料条目数和估算占用大小
     * @param merchantId 商户ID
     * @return { totalSize, usedSize, fileCount, merchantId }
     */
    public Map<String, Object> getStorage(Integer merchantId) {
        Map<String, Object> result = new HashMap<>();
        if (merchantId == null) {
            result.put("totalSize", 0);
            result.put("usedSize", 0);
            result.put("fileCount", 0);
            result.put("merchantId", null);
            return result;
        }

        List<Corpus> list = corpusMapper.selectByMerchantId(merchantId);
        // 仅统计正常状态的语料
        long fileCount = list.stream().filter(c -> c.getStatus() != null && c.getStatus() == 1).count();

        // 估算占用大小（字节）：标题长度 + 内容长度，按 UTF-8 估算
        long usedBytes = 0;
        for (Corpus c : list) {
            if (c.getStatus() == null || c.getStatus() != 1) continue;
            if (c.getTitle() != null) usedBytes += c.getTitle().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            if (c.getContent() != null) usedBytes += c.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        }
        // 转 KB（向上取整，至少 1KB）
        long usedKB = usedBytes == 0 ? 0 : Math.max(1, (usedBytes + 1023) / 1024);

        // totalSize 暂无套餐维度信息，前端可结合 merchant-quota 接口获取
        result.put("totalSize", 0);
        result.put("usedSize", usedKB);
        result.put("fileCount", fileCount);
        result.put("merchantId", merchantId);
        return result;
    }

    /**
     * 搜索知识库内容 (全文搜索)
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PageResponse<Corpus> search(String keyword, int pageNum, int pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "关键词不能为空");
        }

        try {
            // 优化：先用 SQL LIKE 过滤候选集，避免全表加载到内存（selectAll）
            String normalizedKeyword = keyword.toLowerCase().trim();
            List<Corpus> allData = corpusMapper.selectByKeyword(normalizedKeyword);

            // 对候选集做相关性计分（候选集已通过 SQL 过滤，规模远小于全表）
            List<SearchResult> searchResults = new ArrayList<>();
            for (Corpus c : allData) {
                int relevanceScore = calculateRelevance(c, normalizedKeyword);
                if (relevanceScore > 0) {
                    searchResults.add(new SearchResult(c, relevanceScore));
                }
            }

            // 按相关性从高到低排序
            searchResults.sort((a, b) -> Integer.compare(b.score, a.score));

            // 提取排序后的内容列表
            List<Corpus> results = new ArrayList<>();
            for (SearchResult sr : searchResults) {
                results.add(sr.corpus);
            }

            long total = results.size();
            int startIndex = (pageNum - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, results.size());

            // 应用分页
            List<Corpus> pageData = results.subList(startIndex, endIndex);

            log.info("✅ 全文搜索完成: keyword={}, 总结果数={}, 页码={}/{}",
                keyword, total, pageNum, (int) Math.ceil((double) total / pageSize));

            return PageResponse.<Corpus>builder()
                .list(pageData)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .totalPage((int) Math.ceil((double) total / pageSize))
                .build();
        } catch (Exception e) {
            log.error("❌ 全文搜索失败: keyword={}, {}", keyword, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "搜索失败，请稍后重试");
        }
    }

    /**
     * 计算搜索结果的相关性评分
     * 标题匹配权重高于内容匹配
     */
    private int calculateRelevance(Corpus corpus, String keyword) {
        int score = 0;

        if (corpus.getTitle() != null) {
            String titleLower = corpus.getTitle().toLowerCase();
            if (titleLower.contains(keyword)) {
                score += 10;  // 标题精确匹配 +10分
                if (titleLower.startsWith(keyword)) {
                    score += 5;   // 标题开头匹配 +5分
                }
            }
        }

        if (corpus.getContent() != null) {
            String contentLower = corpus.getContent().toLowerCase();
            if (contentLower.contains(keyword)) {
                score += 3;   // 内容匹配 +3分
                // 计算关键词在内容中出现的次数
                int occurrences = countOccurrences(contentLower, keyword);
                score += occurrences;  // 每次出现 +1分
            }
        }

        return score;
    }

    /**
     * 统计关键词在文本中出现的次数
     */
    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        return count;
    }

    /**
     * 内部类：搜索结果包装，用于存储内容和相关性评分
     */
    private static class SearchResult {
        Corpus corpus;
        int score;

        SearchResult(Corpus corpus, int score) {
            this.corpus = corpus;
            this.score = score;
        }
    }
}
