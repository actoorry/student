package vip.appap.suxin;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.util.collection.SetUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.io.File.separator;

/**
 * 项目修改器，一键替换 Maven 的 groupId、artifactId，项目的 package 等
 * <p>
 * 通过修改 main() 中 groupIdNew、artifactIdNew、packageNameNew、titleNew 等变量
 * <p>
 * 安全增强：排除 @TableName、URL、YAML 属性键名、spring.application.name 等不该替换的内容
 *
 * @author 芋道源码（安全增强版）
 */
@Slf4j
public class ProjectReactor {

    // ==================== 旧值（源），对齐官方 ruoyi-vue-pro，不要修改 ====================

    private static final String GROUP_ID = "vip.appap.suxin";
    private static final String ARTIFACT_ID = "suxin";
    private static final String PACKAGE_NAME = "vip.appap.suxin";
    private static final String TITLE_SWAGGER = "书心快速开发平台";
    private static final String TITLE_BRAND = "书心软件";
    private static final String TITLE_LEGACY = "书心快速开发平台";

    /**
     * 白名单文件，不进行重写，避免出问题
     */
    private static final Set<String> WHITE_FILE_TYPES = SetUtils.asSet("gif", "jpg", "svg", "png", // 图片
            "eot", "woff2", "ttf", "woff",  // 字体
            "xdb"); // IP 库

    /**
     * 排除文件：SQL 文件中的 INSERT 数据不应被替换（包含 URL、类名等运行时数据）
     */
    private static final Set<String> SKIP_CONTENT_REPLACEMENT_EXTENSIONS = SetUtils.asSet("sql");

    // ==================== 安全保护：正则模式 ====================

    /**
     * 保护 @TableName("xxx") 中的表名不被替换
     */
    private static final Pattern TABLE_NAME_PATTERN =
            Pattern.compile("(@TableName\\s*\\(\\s*(?:value\\s*=\\s*)?\"[^\"]*\")");

    /**
     * 保护 YAML 中 suxin 键名及 ${suxin.xxx} 占位符（键名不参与 artifactId 替换）
     */
    private static final Pattern YAML_YUDAO_REF_PATTERN = Pattern.compile(
            "(\\$\\{suxin\\.[^}]+\\})|(?m)(^\\s*suxin(?:\\.[\\w-]+)*\\s*:)");

    /**
     * 保护 URL 中的 suxin（如 test.suxin.iocoder.cn、mall.suxin.appap.vip）
     */
    private static final Pattern URL_PATTERN =
            Pattern.compile("(https?://[^\\s\"'<>]*suxin[^\\s\"'<>]*)", Pattern.CASE_INSENSITIVE);

    /**
     * 保护 spring.application.name 的值
     */
    private static final Pattern APP_NAME_PATTERN =
            Pattern.compile("(spring\\.application\\.name\\s*[:=]\\s*[^\n\r]+)");

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        String projectBaseDir = getProjectBaseDir();
        log.info("[main][原项目路径地址 ({})]", projectBaseDir);

        // ========== 配置，需要你手动修改 ==========
        String groupIdNew = "vip.appap.suxin";
        String artifactIdNew = "suxin";
        String packageNameNew = "vip.appap.suxin";
        String titleNew = "书心快速开发平台";
        String titleBrandNew = "书心软件";
        String projectBaseDirNew = projectBaseDir + "-new"; // 一键改名后，"新"项目所在的目录

        // ========== 改包前预览 ==========
        log.info("╔══════════════════════════════════════════╗");
        log.info("║         一键改包 - 执行预览              ║");
        log.info("╠══════════════════════════════════════════╣");
        log.info("║ groupId:    {} → {}", GROUP_ID, groupIdNew);
        log.info("║ artifactId: {} → {}", ARTIFACT_ID, artifactIdNew);
        log.info("║ package:    {} → {}", PACKAGE_NAME, packageNameNew);
        log.info("║ title:      {} → {}", TITLE_SWAGGER, titleNew);
        log.info("║ titleBrand: {} → {}", TITLE_BRAND, titleBrandNew);
        log.info("╠══════════════════════════════════════════╣");
        log.info("║ 排除规则：                              ║");
        log.info("║   - @TableName 注解值                   ║");
        log.info("║   - URL 中的 suxin                      ║");
        log.info("║   - YAML 属性键名 / ${suxin.xxx}        ║");
        log.info("║   - spring.application.name 值          ║");
        log.info("║   - SQL 文件中的 INSERT 数据            ║");
        log.info("║   - 二进制文件（图片/字体/IP库）        ║");
        log.info("╚══════════════════════════════════════════╝");

        log.info("[main][检测新项目目录 ({})是否存在]", projectBaseDirNew);
        if (FileUtil.exist(projectBaseDirNew)) {
            log.error("[main][新项目目录检测 ({})已存在，请更改新的目录！程序退出]", projectBaseDirNew);
            return;
        }
        if (StrUtil.containsAny(projectBaseDirNew, PACKAGE_NAME, ARTIFACT_ID, StrUtil.upperFirst(ARTIFACT_ID))) {
            log.error("[main][新项目目录 `projectBaseDirNew` 检测 ({}) 存在冲突名称「{}」或者「{}」，请更改新的目录！程序退出]",
                    projectBaseDirNew, PACKAGE_NAME, ARTIFACT_ID);
            return;
        }
        log.info("[main][完成新项目目录检测，新项目路径地址 ({})]", projectBaseDirNew);

        log.info("[main][开始获得需要重写的文件，预计需要 10-20 秒]");
        Collection<File> files = listFiles(projectBaseDir);
        log.info("[main][需要重写的文件数量：{}，预计需要 15-30 秒]", files.size());

        int skippedBinary = 0;
        int skippedSql = 0;
        int processed = 0;

        for (File file : files) {
            String fileType = getFileType(file);
            if (WHITE_FILE_TYPES.contains(fileType)) {
                copyFile(file, projectBaseDir, projectBaseDirNew, packageNameNew, artifactIdNew);
                skippedBinary++;
                continue;
            }
            String fileName = file.getName().toLowerCase();
            if (SKIP_CONTENT_REPLACEMENT_EXTENSIONS.stream().anyMatch(ext -> fileName.endsWith("." + ext))) {
                copyFile(file, projectBaseDir, projectBaseDirNew, packageNameNew, artifactIdNew);
                skippedSql++;
                continue;
            }
            String content = replaceFileContent(file, groupIdNew, artifactIdNew, packageNameNew,
                    titleNew, titleBrandNew);
            writeFile(file, content, projectBaseDir, projectBaseDirNew, packageNameNew, artifactIdNew);
            processed++;
        }

        log.info("╔══════════════════════════════════════════╗");
        log.info("║         一键改包 - 执行结果              ║");
        log.info("╠══════════════════════════════════════════╣");
        log.info("║ 处理文件数：{}                           ║", processed);
        log.info("║ 跳过二进制：{}                           ║", skippedBinary);
        log.info("║ 跳过 SQL：  {}                           ║", skippedSql);
        log.info("║ 总文件数：  {}                           ║", files.size());
        log.info("║ 耗时：{} 秒                              ║", (System.currentTimeMillis() - start) / 1000);
        log.info("╚══════════════════════════════════════════╝");

        renamePackageDirectories(projectBaseDirNew, packageNameNew, artifactIdNew);
        scanRemainingReferences(projectBaseDirNew);
    }

    // ==================== 核心替换逻辑（带安全保护）====================

    private static String replaceFileContent(File file, String groupIdNew,
                                             String artifactIdNew, String packageNameNew,
                                             String titleSwaggerNew, String titleBrandNew) {
        String content = FileUtil.readString(file, StandardCharsets.UTF_8);
        String fileType = getFileType(file);
        if (WHITE_FILE_TYPES.contains(fileType)) {
            return content;
        }

        java.util.Map<String, String> protectedTokens = new java.util.LinkedHashMap<>();
        content = protectContent(content, protectedTokens);

        // 对齐官方替换顺序：GROUP_ID → PACKAGE_NAME → ARTIFACT_ID → Suxin → TITLE
        content = content.replaceAll(Pattern.quote(GROUP_ID), Matcher.quoteReplacement(groupIdNew))
                .replaceAll(Pattern.quote(PACKAGE_NAME), Matcher.quoteReplacement(packageNameNew))
                .replaceAll(Pattern.quote(ARTIFACT_ID), Matcher.quoteReplacement(artifactIdNew))
                .replaceAll(Pattern.quote(StrUtil.upperFirst(ARTIFACT_ID)), Matcher.quoteReplacement(StrUtil.upperFirst(artifactIdNew)))
                .replaceAll(Pattern.quote(TITLE_SWAGGER), Matcher.quoteReplacement(titleSwaggerNew))
                .replaceAll(Pattern.quote(TITLE_BRAND), Matcher.quoteReplacement(titleBrandNew))
                .replaceAll(Pattern.quote(TITLE_LEGACY), Matcher.quoteReplacement(titleSwaggerNew));

        for (java.util.Map.Entry<String, String> entry : protectedTokens.entrySet()) {
            content = content.replace(entry.getKey(), entry.getValue());
        }

        return content;
    }

    private static String protectContent(String content, java.util.Map<String, String> protectedTokens) {
        int counter = 0;

        Matcher tableNameMatcher = TABLE_NAME_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (tableNameMatcher.find()) {
            String token = "___PROTECTED_TABLENAME_" + counter + "___";
            protectedTokens.put(token, tableNameMatcher.group(1));
            tableNameMatcher.appendReplacement(sb, token);
            counter++;
        }
        tableNameMatcher.appendTail(sb);
        content = sb.toString();

        Matcher yamlMatcher = YAML_YUDAO_REF_PATTERN.matcher(content);
        sb = new StringBuffer();
        while (yamlMatcher.find()) {
            String token = "___PROTECTED_YAML_" + counter + "___";
            protectedTokens.put(token, yamlMatcher.group());
            yamlMatcher.appendReplacement(sb, Matcher.quoteReplacement(token));
            counter++;
        }
        yamlMatcher.appendTail(sb);
        content = sb.toString();

        Matcher urlMatcher = URL_PATTERN.matcher(content);
        sb = new StringBuffer();
        while (urlMatcher.find()) {
            String token = "___PROTECTED_URL_" + counter + "___";
            protectedTokens.put(token, urlMatcher.group(1));
            urlMatcher.appendReplacement(sb, Matcher.quoteReplacement(token));
            counter++;
        }
        urlMatcher.appendTail(sb);
        content = sb.toString();

        Matcher appNameMatcher = APP_NAME_PATTERN.matcher(content);
        sb = new StringBuffer();
        while (appNameMatcher.find()) {
            String token = "___PROTECTED_APPNAME_" + counter + "___";
            protectedTokens.put(token, appNameMatcher.group(1));
            appNameMatcher.appendReplacement(sb, Matcher.quoteReplacement(token));
            counter++;
        }
        appNameMatcher.appendTail(sb);
        content = sb.toString();

        return content;
    }

    // ==================== 后处理：目录重命名 ====================

    private static void renamePackageDirectories(String projectBaseDirNew, String packageNameNew, String artifactIdNew) {
        log.info("[renamePackageDirectories][开始重命名 Java 包目录]");

        String oldPackagePath = PACKAGE_NAME.replaceAll("\\.", Matcher.quoteReplacement(separator));
        String newPackagePath = packageNameNew.replaceAll("\\.", Matcher.quoteReplacement(separator));

        Collection<File> allDirs = FileUtil.loopFiles(new File(projectBaseDirNew));
        java.util.List<File> dirsToRename = allDirs.stream()
                .filter(File::isDirectory)
                .filter(dir -> dir.getPath().contains(oldPackagePath))
                .filter(dir -> {
                    String path = dir.getPath();
                    return path.contains("src" + separator + "main" + separator + "java")
                            || path.contains("src" + separator + "test" + separator + "java");
                })
                .sorted((a, b) -> b.getPath().length() - a.getPath().length())
                .collect(Collectors.toList());

        for (File dir : dirsToRename) {
            String newPath = dir.getPath().replace(oldPackagePath, newPackagePath);
            File newDir = new File(newPath);
            if (!newDir.exists()) {
                FileUtil.mkParentDirs(newDir);
                boolean success = dir.renameTo(newDir);
                if (success) {
                    log.info("[renamePackageDirectories][重命名] {} → {}", dir.getPath(), newPath);
                } else {
                    log.error("[renamePackageDirectories][重命名失败] {}", dir.getPath());
                }
            }
        }

        cleanupEmptyDirs(projectBaseDirNew, oldPackagePath);
        log.info("[renamePackageDirectories][包目录重命名完成]");
    }

    private static void cleanupEmptyDirs(String basePath, String oldPackagePath) {
        String[] srcRoots = {"src" + separator + "main" + separator + "java",
                "src" + separator + "test" + separator + "java"};
        for (String srcRoot : srcRoots) {
            File baseDir = new File(basePath + separator + srcRoot);
            if (baseDir.exists()) {
                deleteEmptyDirsRecursively(baseDir);
            }
        }
    }

    private static boolean deleteEmptyDirsRecursively(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return false;
        }

        boolean allDeleted = true;
        for (File file : files) {
            if (file.isDirectory()) {
                if (!deleteEmptyDirsRecursively(file)) {
                    allDeleted = false;
                }
            } else {
                allDeleted = false;
            }
        }

        if (allDeleted && dir.listFiles() != null && dir.listFiles().length == 0) {
            boolean deleted = dir.delete();
            if (deleted) {
                log.info("[cleanupEmptyDirs][删除空目录] {}", dir.getPath());
            }
            return deleted;
        }
        return false;
    }

    // ==================== 后处理：扫描残留引用 ====================

    private static void scanRemainingReferences(String projectBaseDirNew) {
        log.info("╔══════════════════════════════════════════╗");
        log.info("║         残留旧包名扫描                   ║");
        log.info("╚══════════════════════════════════════════╝");

        Collection<File> files = FileUtil.loopFiles(new File(projectBaseDirNew));
        int matchCount = 0;

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            String fileName = file.getName().toLowerCase();
            if (WHITE_FILE_TYPES.stream().anyMatch(ext -> fileName.endsWith("." + ext))) {
                continue;
            }
            if (fileName.endsWith(".class") || fileName.endsWith(".jar")) {
                continue;
            }

            try {
                String content = FileUtil.readString(file, StandardCharsets.UTF_8);
                if (!content.contains(PACKAGE_NAME) && !content.contains(GROUP_ID)
                        && !content.contains(ARTIFACT_ID)) {
                    continue;
                }
                boolean hasNonUrlMatch = false;
                String[] lines = content.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if ((line.contains(PACKAGE_NAME) || line.contains(GROUP_ID)
                            || containsArtifactIdAsWord(line))
                            && !line.contains("http://") && !line.contains("https://")) {
                        log.warn("[scanRemainingReferences][残留引用] {} 第 {} 行: {}",
                                file.getPath().replace(projectBaseDirNew, ""), i + 1,
                                line.trim().substring(0, Math.min(line.trim().length(), 80)));
                        hasNonUrlMatch = true;
                    }
                }
                if (hasNonUrlMatch) {
                    matchCount++;
                }
            } catch (Exception ignored) {
                // 跳过无法读取的文件
            }
        }

        if (matchCount > 0) {
            log.warn("[scanRemainingReferences][发现 {} 个文件有残留旧包名引用，请手动检查！]", matchCount);
        } else {
            log.info("[scanRemainingReferences][未发现残留旧包名引用 ✓]");
        }
    }

    private static boolean containsArtifactIdAsWord(String line) {
        return Pattern.compile("\\bsuxin\\b", Pattern.CASE_INSENSITIVE).matcher(line).find()
                || line.contains("Suxin");
    }

    // ==================== 工具方法 ====================

    private static String getProjectBaseDir() {
        String baseDir = System.getProperty("user.dir");
        if (StrUtil.isEmpty(baseDir)) {
            throw new NullPointerException("项目基础路径不存在");
        }
        return baseDir;
    }

    private static Collection<File> listFiles(String projectBaseDir) {
        Collection<File> files = FileUtil.loopFiles(projectBaseDir);
        files = files.stream()
                .filter(file -> !file.getPath().contains(separator + "target" + separator)
                        && !file.getPath().contains(separator + "node_modules" + separator)
                        && !file.getPath().contains(separator + ".idea" + separator)
                        && !file.getPath().contains(separator + ".git" + separator)
                        && !file.getPath().contains(separator + "dist" + separator)
                        && !file.getPath().contains(".iml")
                        && !file.getPath().contains(".html.gz"))
                .collect(Collectors.toList());
        return files;
    }

    private static void writeFile(File file, String fileContent, String projectBaseDir,
                                  String projectBaseDirNew, String packageNameNew, String artifactIdNew) {
        String newPath = buildNewFilePath(file, projectBaseDir, projectBaseDirNew, packageNameNew, artifactIdNew);
        FileUtil.writeUtf8String(fileContent, newPath);
    }

    private static void copyFile(File file, String projectBaseDir,
                                 String projectBaseDirNew, String packageNameNew, String artifactIdNew) {
        String newPath = buildNewFilePath(file, projectBaseDir, projectBaseDirNew, packageNameNew, artifactIdNew);
        FileUtil.copyFile(file, new File(newPath));
    }

    private static String buildNewFilePath(File file, String projectBaseDir,
                                           String projectBaseDirNew, String packageNameNew, String artifactIdNew) {
        return file.getPath().replace(projectBaseDir, projectBaseDirNew)
                .replace(PACKAGE_NAME.replaceAll("\\.", Matcher.quoteReplacement(separator)),
                        packageNameNew.replaceAll("\\.", Matcher.quoteReplacement(separator)))
                .replace(ARTIFACT_ID, artifactIdNew)
                .replaceAll(StrUtil.upperFirst(ARTIFACT_ID), StrUtil.upperFirst(artifactIdNew));
    }

    private static String getFileType(File file) {
        return file.length() > 0 ? FileTypeUtil.getType(file) : "";
    }

}
