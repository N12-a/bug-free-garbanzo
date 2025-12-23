//package com.farm.product;
//
//import com.baomidou.mybatisplus.generator.FastAutoGenerator;
//import com.baomidou.mybatisplus.generator.config.OutputFile;
//import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
//
//import java.util.Collections;
//
//public class MybatisPlusGenerator {
//    public static void main(String[] args) {
//        // 数据库连接信息
//        FastAutoGenerator.create("jdbc:mysql://localhost:3306/farm_product",
//                        "root", "1190")
//                .globalConfig(builder -> {
//                    builder.author("fn") // 设置作者
//                            .outputDir(System.getProperty("user.dir") +
//                                    "/src/main/java") // 指定输出目录
//                            .commentDate("yyyy-MM-dd");
//                })
//                // 包配置
//                .packageConfig(builder -> {
//                    builder.parent("com.farm.product") // 设置父包名
//                            .moduleName("")
//                            .entity("entity") // 实体类包名
//                            .mapper("mapper") // Mapper接口包名
//                            .service("service") // Service接口包名
//                            .serviceImpl("service.impl") // Service实现类包名
//                            .controller("controller") // Controller包名
//                            .xml("mapper") // Mapper XML文件包名
//                            .pathInfo(Collections.singletonMap(OutputFile.xml,
//                                    System.getProperty("user.dir") + "/src/main/resources/mapper")); // 设置mapperXml文件的生成路径
//                })
//                .strategyConfig(builder -> {
//                    builder.addInclude("user") // 设置需要生成的表名
//                            .addTablePrefix("t_", "c_") // 设置过滤表前缀
//                            .entityBuilder()
//                            .enableLombok() // 启用 Lombok
//                            .controllerBuilder()
//                            .enableRestStyle(); // 启用 REST 风格
//                })
//                .templateEngine(new VelocityTemplateEngine()) // 使用 Velocity 引擎模板
//                .execute(); // 执行
//    }
//}