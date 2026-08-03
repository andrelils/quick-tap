package com.quicktap.service;

import com.quicktap.dto.PageResponse;
import com.quicktap.entity.AiGenerateRecord;
import com.quicktap.exception.BusinessException;
import com.quicktap.mapper.AiGenerateRecordMapper;
import com.quicktap.service.ai.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI 内容生成服务
 * 处理文本、图片、视频生成及历史记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerateService {

    private final AiGenerateRecordMapper aiGenerateRecordMapper;

    @Autowired(required = false)
    private OpenAiClient openAiClient;

    /**
     * 文本内容生成
     * 调用真实的 OpenAI API（或 Mock 实现）
     *
     * @param merchantId 商户ID
     * @param prompt 提示词
     * @return 生成结果
     */
    public AiGenerateRecord generateText(Integer merchantId, String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new BusinessException(400, "提示词不能为空");
        }

        // 创建生成记录
        AiGenerateRecord record = new AiGenerateRecord();
        record.setRecordId(UUID.randomUUID().toString());
        record.setMerchantId(merchantId);
        record.setType("text");
        record.setPrompt(prompt);
        record.setStatus(1);

        try {
            // 调用真实的 OpenAI API
            OpenAiClient client = openAiClient != null ? openAiClient : new OpenAiClient.MockOpenAiClient();
            OpenAiClient.TextGenerationResponse apiResponse = client.generateText(prompt);

            record.setResult(apiResponse.getContent());
            record.setTokenUsage(apiResponse.getTokenUsage());
            record.setCost(apiResponse.getCost());

            // 保存记录
            int result = aiGenerateRecordMapper.insert(record);
            if (result <= 0) {
                log.error("文本生成记录保存失败: prompt={}", prompt);
                throw new BusinessException(500, "生成失败，请稍后重试");
            }

            log.info("✓ 文本生成成功: recordId={}, merchantId={}, tokens={}, cost=${}",
                record.getRecordId(), merchantId, apiResponse.getTokenUsage(),
                String.format("%.6f", apiResponse.getCost()));

            return record;

        } catch (Exception e) {
            log.error("❌ 文本生成异常: {}", e.getMessage(), e);
            record.setStatus(0); // 失败状态
            aiGenerateRecordMapper.insert(record);
            throw new BusinessException(500, "生成失败: " + e.getMessage());
        }
    }

    /**
     * 图片内容生成
     * 调用真实的 DALL-E API（或 Mock 实现）
     *
     * @param merchantId 商户ID
     * @param prompt 提示词
     * @return 生成结果
     */
    public AiGenerateRecord generateImage(Integer merchantId, String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new BusinessException(400, "提示词不能为空");
        }

        AiGenerateRecord record = new AiGenerateRecord();
        record.setRecordId(UUID.randomUUID().toString());
        record.setMerchantId(merchantId);
        record.setType("image");
        record.setPrompt(prompt);
        record.setStatus(1);

        try {
            // 调用真实的 DALL-E API
            OpenAiClient client = openAiClient != null ? openAiClient : new OpenAiClient.MockOpenAiClient();
            OpenAiClient.ImageGenerationResponse apiResponse = client.generateImage(prompt);

            record.setResult(apiResponse.getImageUrl());

            int result = aiGenerateRecordMapper.insert(record);
            if (result <= 0) {
                log.error("图片生成记录保存失败: prompt={}", prompt);
                throw new BusinessException(500, "生成失败，请稍后重试");
            }

            log.info("✓ 图片生成成功: recordId={}, merchantId={}, url={}",
                record.getRecordId(), merchantId, apiResponse.getImageUrl());

            return record;

        } catch (Exception e) {
            log.error("❌ 图片生成异常: {}", e.getMessage(), e);
            record.setStatus(0); // 失败状态
            aiGenerateRecordMapper.insert(record);
            throw new BusinessException(500, "生成失败: " + e.getMessage());
        }
    }

    /**
     * 视频内容生成
     * 调用真实的 Synthesia/Runway API（当前使用 Mock 实现，需要单独集成）
     *
     * @param merchantId 商户ID
     * @param prompt 提示词
     * @return 生成结果
     */
    public AiGenerateRecord generateVideo(Integer merchantId, String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new BusinessException(400, "提示词不能为空");
        }

        AiGenerateRecord record = new AiGenerateRecord();
        record.setRecordId(UUID.randomUUID().toString());
        record.setMerchantId(merchantId);
        record.setType("video");
        record.setPrompt(prompt);
        record.setStatus(1);

        try {
            // 视频生成通常是异步操作，需要单独的 API 集成
            // 当前使用 Mock 实现
            String videoUrl = generateVideoMock(prompt);
            record.setResult(videoUrl);

            int result = aiGenerateRecordMapper.insert(record);
            if (result <= 0) {
                log.error("视频生成记录保存失败: prompt={}", prompt);
                throw new BusinessException(500, "生成失败，请稍后重试");
            }

            log.info("✓ 视频生成成功: recordId={}, merchantId={}, url={}",
                record.getRecordId(), merchantId, videoUrl);

            return record;

        } catch (Exception e) {
            log.error("❌ 视频生成异常: {}", e.getMessage(), e);
            record.setStatus(0); // 失败状态
            aiGenerateRecordMapper.insert(record);
            throw new BusinessException(500, "生成失败: " + e.getMessage());
        }
    }

    /**
     * 视频生成 Mock 实现
     * TODO: 完成 Synthesia/Runway API 集成
     */
    private String generateVideoMock(String prompt) {
        // 实际应用中应调用真实的视频生成 API（Synthesia, Runway 等）
        // 这里使用模拟的视频 URL
        return "https://example.com/generated-video-" + UUID.randomUUID().toString() + ".mp4";
    }

    /**
     * 获取生成历史
     * 根据 merchantId 和 type 过滤数据
     *
     * @param merchantId 商户ID
     * @param type 生成类型（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public PageResponse<AiGenerateRecord> getGenerationHistory(Integer merchantId, String type, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;

        // 获取总数（按 merchantId 和 type 过滤）
        long total = aiGenerateRecordMapper.countByMerchantAndType(merchantId, type);

        // 获取分页数据（按 merchantId 和 type 过滤）
        List<AiGenerateRecord> records = aiGenerateRecordMapper.selectPageByMerchantAndType(
            merchantId, type, offset, pageSize);

        log.debug("查询生成历史: merchantId={}, type={}, total={}, pageNum={}, pageSize={}",
            merchantId, type, total, pageNum, pageSize);

        return PageResponse.<AiGenerateRecord>builder()
            .list(records)
            .pageNum(pageNum)
            .pageSize(pageSize)
            .total(total)
            .totalPage((int) Math.ceil((double) total / pageSize))
            .build();
    }

    /**
     * 获取单个生成记录
     * @param recordId 记录ID
     * @return 生成记录
     */
    public AiGenerateRecord getRecord(String recordId) {
        AiGenerateRecord record = aiGenerateRecordMapper.selectByRecordId(recordId);
        if (record == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return record;
    }

    /**
     * 删除生成记录
     * @param recordId 记录ID
     */
    public void deleteRecord(String recordId) {
        AiGenerateRecord record = aiGenerateRecordMapper.selectByRecordId(recordId);
        if (record == null) {
            throw new BusinessException(404, "记录不存在");
        }

        int result = aiGenerateRecordMapper.deleteById(record.getId());
        if (result <= 0) {
            log.error("删除记录失败: recordId={}", recordId);
            throw new BusinessException(500, "删除失败，请稍后重试");
        }

        log.info("删除记录成功: recordId={}", recordId);
    }

    /**
     * 获取生成统计
     * @return 统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // TODO: 实现真实的统计查询
        stats.put("totalGenerations", aiGenerateRecordMapper.countAll());
        stats.put("textGenerations", 0);
        stats.put("imageGenerations", 0);
        stats.put("videoGenerations", 0);
        stats.put("successRate", 100.0);

        return stats;
    }
}
