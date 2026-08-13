package vip.appap.suxin.module.sales.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分销链路架构契约测试（任务 3.3 / 6.2）
 *
 * 通过源码扫描保证：
 * 1. Sales 分销 Controller/Service/DO/Mapper 不依赖 Marriage 与 Rongjh；
 * 2. Rongjh 只能单向消费 Sales 订单事实，不复制分销实现（无 SalesBrokerage* 类），
 *    也不允许 Sales 分销反向依赖 Rongjh。
 */
class SalesBrokerageArchitectureContractTest {

    private static final Path MODULE_SRC = resolveModuleSrc();

    private static Path resolveModuleSrc() {
        // Maven 单测工作目录可能是模块目录或仓库根目录，兼容两种布局
        Path fromModule = Paths.get("src/main/java/vip/appap/suxin/module");
        if (Files.isDirectory(fromModule)) {
            return fromModule;
        }
        return Paths.get("suxin-module/src/main/java/vip/appap/suxin/module");
    }

    private static List<String> readLines(Path file) throws IOException {
        return Files.readAllLines(file);
    }

    private static List<Path> listJavaFiles(String packagePath) throws IOException {
        Path dir = MODULE_SRC.resolve(packagePath);
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        }
    }

    @Test
    void salesBrokerageCode_mustNotDependOnMarriageOrRongjh() throws IOException {
        List<Path> brokerageFiles = listJavaFiles("sales").stream()
                .filter(p -> p.getFileName().toString().contains("Brokerage"))
                .collect(Collectors.toList());
        assertTrue(brokerageFiles.size() >= 12, "应存在 Sales 分销类文件，实际：" + brokerageFiles.size());

        List<String> violations = brokerageFiles.stream().flatMap(file -> {
            try {
                return readLines(file).stream()
                        .filter(line -> line.contains("module.marriage") || line.contains("module.rongjh"))
                        .map(line -> file.getFileName() + ": " + line.trim());
            } catch (IOException e) {
                return Stream.of(file.getFileName() + ": READ_ERROR");
            }
        }).collect(Collectors.toList());
        assertTrue(violations.isEmpty(), "Sales 分销代码不得依赖 Marriage/Rongjh：\n" + String.join("\n", violations));
    }

    @Test
    void rongjhModule_mustNotReplicateBrokerageImplementation() throws IOException {
        List<Path> rongjhFiles = listJavaFiles("rongjh");
        List<String> brokerageClones = rongjhFiles.stream()
                .filter(p -> p.getFileName().toString().contains("Brokerage")
                        || p.getFileName().toString().contains("brokerage"))
                .map(Path::toString)
                .collect(Collectors.toList());
        assertTrue(brokerageClones.isEmpty(), "Rongjh 不得复制 SalesBrokerage* 实现：\n" + String.join("\n", brokerageClones));

        List<String> rongjhSalesImports = rongjhFiles.stream().flatMap(file -> {
            try {
                return readLines(file).stream()
                        .filter(line -> line.contains("import vip.appap.suxin.module.sales"))
                        .map(line -> file.getFileName() + ": " + line.trim());
            } catch (IOException e) {
                return Stream.of(file.getFileName() + ": READ_ERROR");
            }
        }).collect(Collectors.toList());
        assertTrue(rongjhSalesImports.stream().anyMatch(l -> l.contains("SalesOrder")),
                "Rongjh 应单向消费 Sales 订单事实（import SalesOrder）");
    }

    @Test
    void salesOrderFacts_areConsumedByRongjhInOneDirection() throws IOException {
        // Rongjh 消费 SalesOrder 事实（订单级），不消费分销关系/佣金/提现
        Path loveService = MODULE_SRC.resolve("rongjh/service/LoveValueServiceImpl.java");
        assertTrue(Files.exists(loveService), "LoveValueServiceImpl 应存在");
        String source = Files.readString(loveService);
        assertTrue(source.contains("import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO"),
                "LoveValueServiceImpl 应读取 SalesOrderDO 事实");
        assertTrue(source.contains("SalesOrderMapper"), "LoveValueServiceImpl 应通过 SalesOrderMapper 查询订单");
        assertTrue(!source.contains("Brokerage"), "LoveValueServiceImpl 不得引用分销实现");
    }

}
