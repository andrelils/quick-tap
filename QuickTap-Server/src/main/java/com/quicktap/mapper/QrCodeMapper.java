package com.quicktap.mapper;

import com.quicktap.entity.QrCode;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 二维码 Mapper (XML配置)
 */
@Mapper
public interface QrCodeMapper {

    /**
     * 插入二维码
     */
    int insert(QrCode qrCode);

    /**
     * 批量插入二维码（提升批量生成场景下的性能）
     */
    int batchInsert(List<QrCode> qrCodes);

    /**
     * 按 ID 查询
     */
    QrCode selectById(Long id);

    /**
     * 按编码查询
     */
    QrCode selectByCode(String code);

    /**
     * 按设备 ID 查询
     */
    List<QrCode> selectByDeviceId(Long deviceId);

    /**
     * 按商户 ID 查询
     */
    List<QrCode> selectByMerchantId(Long merchantId);

    /**
     * 查询所有二维码
     */
    List<QrCode> selectAll();

    /**
     * 更新二维码
     */
    int update(QrCode qrCode);

    /**
     * 更新状态
     */
    int updateStatus(Long id, String status);

    /**
     * 删除二维码
     */
    int deleteById(Long id);

    /**
     * 按设备 ID 删除
     */
    int deleteByDeviceId(Long deviceId);

    /**
     * 获取二维码总数
     */
    int countAll();

    /**
     * 获取指定状态的二维码数量
     */
    int countByStatus(Long merchantId, String status);

    /**
     * 检查编码是否已存在
     */
    int countByCode(String code);
}
