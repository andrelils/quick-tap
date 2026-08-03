# QuickTap Node vs Java 快速参考卡

## 概览统计

| 指标 | Node | Java | 差距 |
|------|------|------|------|
| 总接口数 | 114+ | 104+ | ±10 |
| 完整功能 | ~90 | ~90 | 0 |
| 缺失功能 | 0 | 15-20 | Java 缺 15-20 |
| 新增功能 | 0 | 10+ | Node 缺 10+ |
| 表数量 | 16 | 16 | 0 (基本) |

---

## Java 缺失的 15-20 个功能 (按优先级)

### 高优先级 (需立即补充)

```
1. 语料库分类管理 (GET/POST/PUT/DELETE /corpus/categories)
   - 工作量: 6h
   - 难度: 中等
   - 相关表: corpus_category

2. 绑定二维码到商户 (POST /qrcode/bind)
   - 工作量: 4h
   - 难度: 中等
   - 相关表: qrcode, merchant

3. 批量删除语料库 (POST /corpus/batch-delete)
   - 工作量: 2h
   - 难度: 简单
   - 相关表: corpus

4. 移动语料库到回收站 (POST /corpus/trash)
   - 工作量: 2h
   - 难度: 简单
   - 相关表: corpus

5. AI 配置管理 (GET/PUT /ai/config/:merchantId)
   - 工作量: 5h
   - 难度: 中等
   - 相关表: ai_config

6. 商户配额列表 (GET /merchant/quota/list)
   - 工作量: 2h
   - 难度: 简单
   - 相关表: merchant_quota
```

**小计**: 18-21 小时, 6 个功能

### 中优先级 (2-3 周内完成)

```
7. 获取全部推广平台 (GET /promotion/platforms/all)
   - 工作量: 1h
   - 难度: 简单

8. 批量生成 NFC 二维码 (POST /qrcode/generate/batch-nfc)
   - 工作量: 4h
   - 难度: 中等

9. 删除二维码 (DELETE /qrcode/:id)
   - 工作量: 1h
   - 难度: 简单

10. 获取二维码历史 (GET /qrcode/history)
    - 工作量: 2h
    - 难度: 简单
```

**小计**: 8 小时, 4 个功能

### 低优先级 (用户端功能)

```
11. 商户列表（用户端）(GET /user/merchant/list)
12. 商户详情（用户端）(GET /user/merchant/:id)
13. 设备信息 (GET /device/info/:deviceNo)
14. 扫描二维码 (GET /scan)
15. WiFi 信息 (GET /wifi)
```

**小计**: 6-8 小时, 5 个功能

---

## 快速实施路线图

```
Week 1:
  Day 1-2: 语料库分类管理 (数据库 + Service + Controller)
  Day 3-4: 绑定二维码 + 批量删除语料库
  Day 5:   测试 + 小修复

Week 2:
  Day 1-2: AI 配置管理
  Day 3-4: 推广平台增强 + 二维码管理增强
  Day 5:   测试 + 集成

Week 3:
  Day 1-2: 架构完善 (日志、缓存、定时任务)
  Day 3-4: C端功能或测试
  Day 5:   部署准备

Week 4:
  灰度发布 + 监控
```

---

## 核心文件位置参考

### Java 项目结构
```
src/main/java/com/quicktap/
├── controller/          # API 接口层
│   ├── DeviceController.java         # ← 添加 bindQrCode 接口
│   ├── CorpusController.java         # ← 添加分类管理接口
│   ├── AiGenerateController.java     # ← 添加 AI 配置接口
│   └── ...
├── service/            # 业务逻辑层
│   ├── DeviceService.java
│   ├── CorpusService.java
│   └── ...
├── entity/            # 数据模型
│   ├── Corpus.java
│   ├── Device.java
│   ├── AiConfig.java
│   └── CorpusCategory.java          # ← 新建
├── mapper/            # 数据访问层
│   ├── CorpusMapper.java
│   └── CorpusCategoryMapper.java     # ← 新建
└── config/           # 配置层
    ├── SecurityConfig.java
    ├── RedisConfig.java
    └── ...

src/main/resources/
├── db/
│   └── init.sql        # ← 更新建表脚本
└── application.yml     # ← 更新配置

src/test/java/         # 测试代码
```

### Node 项目对应位置
```
src/routes/
├── admin/
│   ├── ai.js           # AI 和语料库路由
│   ├── device.js       # 设备和二维码路由
│   └── ...
└── user/
    └── ...

src/controllers/
├── admin/
│   ├── ai.js
│   └── device.js
└── user/

src/sql/
└── init.sql
```

---

## 快速代码模板

### Entity 模板
```java
@Data
@Entity
@Table(name = "corpus_category")
public class CorpusCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer merchantId;

    @Column(nullable = false)
    private String name;

    private String description;

    private Integer sort = 0;

    private Byte status = 1;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### Service 模板
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CorpusCategoryService {
    private final CorpusCategoryMapper categoryMapper;

    public List<CorpusCategory> getCategories(Integer merchantId) {
        return categoryMapper.selectByMerchantId(merchantId);
    }

    public CorpusCategory createCategory(Integer merchantId, CorpusCategoryRequest request) {
        CorpusCategory category = new CorpusCategory();
        category.setMerchantId(merchantId);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        categoryMapper.insert(category);
        return category;
    }

    @Transactional
    public void deleteCategory(Integer id) {
        // 检查是否有语料库关联
        int corpusCount = corpusMapper.countByCategory(id);
        if (corpusCount > 0) {
            throw new BusinessException("该分类下有语料库，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
```

### Controller 模板
```java
@GetMapping("/categories")
@PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
public ApiResponse<List<CorpusCategory>> getCategories(
        @RequestParam Integer merchantId) {
    return ApiResponse.success(corpusCategoryService.getCategories(merchantId));
}

@PostMapping("/categories")
@PreAuthorize("hasRole('MERCHANT')")
public ApiResponse<CorpusCategory> createCategory(
        @RequestParam Integer merchantId,
        @Valid @RequestBody CorpusCategoryRequest request) {
    return ApiResponse.success(corpusCategoryService.createCategory(merchantId, request));
}

@DeleteMapping("/categories/{id}")
@PreAuthorize("hasRole('MERCHANT')")
public ApiResponse<Void> deleteCategory(@PathVariable Integer id) {
    corpusCategoryService.deleteCategory(id);
    return ApiResponse.success("删除成功");
}
```

---

## 测试用例参考

### Service 层测试
```java
@SpringBootTest
public class CorpusCategoryServiceTest {

    @MockBean
    private CorpusCategoryMapper categoryMapper;

    @Autowired
    private CorpusCategoryService categoryService;

    @Test
    public void testCreateCategory() {
        // Arrange
        Integer merchantId = 1;
        CorpusCategoryRequest request = new CorpusCategoryRequest();
        request.setName("测试分类");

        // Act
        CorpusCategory result = categoryService.createCategory(merchantId, request);

        // Assert
        assertNotNull(result.getId());
        assertEquals("测试分类", result.getName());
        verify(categoryMapper, times(1)).insert(any());
    }
}
```

### Controller 层测试
```java
@SpringBootTest
@AutoConfigureMockMvc
public class CorpusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CorpusCategoryService categoryService;

    @Test
    public void testGetCategories() throws Exception {
        List<CorpusCategory> mockData = new ArrayList<>();
        when(categoryService.getCategories(1)).thenReturn(mockData);

        mockMvc.perform(get("/api/corpus/categories?merchantId=1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

---

## SQL 迁移脚本参考

### 创建新表
```sql
-- 1. 创建语料库分类表
CREATE TABLE corpus_category (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  merchant_id INT UNSIGNED NOT NULL,
  name VARCHAR(128) NOT NULL,
  description TEXT,
  sort INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_merchant_category (merchant_id, name),
  KEY idx_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 修改 corpus 表
ALTER TABLE corpus ADD COLUMN category_id INT UNSIGNED AFTER merchant_id;
ALTER TABLE corpus ADD KEY idx_category_id (category_id);

-- 3. 创建二维码历史表
CREATE TABLE qrcode_history (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  merchant_id INT UNSIGNED,
  qrcode_id INT UNSIGNED,
  type VARCHAR(32),
  code VARCHAR(255),
  qrcode_url VARCHAR(255),
  params JSON,
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_merchant_id (merchant_id),
  KEY idx_qrcode_id (qrcode_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 关键配置参考

### application.yml 补充配置
```yaml
# Redis 缓存
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    jedis:
      pool:
        max-active: 20

# 定时任务
scheduling:
  pool:
    size: 5
  thread-name-prefix: quicktap-scheduler-

# 日志
logging:
  level:
    root: INFO
    com.quicktap: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%X{traceId}] %-5level %logger{36} - %msg%n"
  file:
    name: logs/quicktap.log
```

---

## 与 Node 兼容性检查清单

实现每个功能后需验证：

- [ ] 请求路径与 Node 一致
- [ ] 请求参数与 Node 相同
- [ ] 响应格式与标准格式一致
- [ ] 错误码与 Node 一致
- [ ] 权限检查与 Node 一致
- [ ] 业务逻辑与 Node 一致

**验证方法**:
```bash
# 1. 对比 Node 源代码中的实现
grep -r "POST.*binding" node/server/server/dist/*/src/routes/

# 2. 使用 API 测试工具 (Postman/Insomnia)
# 3. 编写集成测试确保两端行为一致
```

---

## 性能基准参考

实现后应达到的性能指标：

| 操作 | 目标 QPS | 目标延迟 | 缓存策略 |
|------|---------|--------|---------|
| GET 列表 | > 1000 | < 100ms | Redis 1h |
| GET 详情 | > 2000 | < 50ms | Redis 30m |
| POST 创建 | > 500 | < 200ms | 清除相关缓存 |
| PUT 更新 | > 500 | < 200ms | 清除相关缓存 |
| DELETE | > 300 | < 300ms | 清除相关缓存 |
| 分页查询 | > 800 | < 150ms | Redis key:page |

---

## 常见问题解答 (FAQ)

**Q: 什么时候需要创建新表?**
A: 当需要存储新的数据维度时(如语料库分类、二维码历史)。修改现有表字段时需要考虑已有数据迁移。

**Q: 权限检查怎样与 Node 保持一致?**
A: 使用 @PreAuthorize 注解检查角色，保证与 Node 的认证逻辑相同。

**Q: 缓存什么时候清除?**
A: 在任何修改(POST/PUT/DELETE)操作后清除相关缓存，确保数据一致性。

**Q: 如何处理事务?**
A: 在修改多个表的操作上添加 @Transactional，确保原子性。

**Q: 测试覆盖率要求?**
A: Service 层 > 85%, Controller 层 > 75%, 整体 > 80%。

---

## 性能优化 Quick Tips

```java
// 1. 使用索引
CREATE INDEX idx_merchant_status ON corpus(merchant_id, status);

// 2. 分页查询
Page<Corpus> page = service.getCorpusPage(merchantId, pageable);

// 3. 缓存
@Cacheable(value = "corpus", key = "#id")
public Corpus getById(Integer id) { }

// 4. 异步处理
@Async
public CompletableFuture<Void> batchGenerate() { }

// 5. 批量操作
List<Corpus> batchInsert = corpusMapper.batchInsert(list);
```

---

## 监控和告警建议

在生产环境配置以下监控：

```yaml
monitoring:
  metrics:
    - api.response.time    # API 响应时间
    - api.error.rate       # 错误率
    - db.connection.pool   # 数据库连接池
    - cache.hit.rate       # 缓存命中率
    - task.execution.time  # 定时任务执行时间

  alerts:
    - api.error.rate > 5%         # 错误率超过5%
    - api.response.time > 500ms   # 响应超过500ms
    - db.connection.pool.size < 5 # 连接池不足
    - cache.hit.rate < 60%        # 缓存命中率低
```

---

## 快速清单 - 完成一个功能的步骤

```
[ ] 1. 分析 Node 中的实现
[ ] 2. 设计数据库表结构 (如需要)
[ ] 3. 编写 Entity 和 Mapper
[ ] 4. 编写 Service 业务逻辑
[ ] 5. 编写 Controller 接口
[ ] 6. 编写单元测试 (Service + Mapper)
[ ] 7. 编写集成测试 (Controller)
[ ] 8. 更新 Swagger 文档
[ ] 9. 编写迁移脚本 (如需要)
[ ] 10. 代码审查
[ ] 11. 部署测试
[ ] 12. 与 Node 进行对等测试
```

---

**最后更新**: 2024-07-30
**状态**: 待实施
**预计总工作量**: 90-100 小时
**建议团队规模**: 2-3 人
**建议周期**: 4 周

