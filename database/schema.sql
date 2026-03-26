-- MySQL dump 10.13  Distrib 8.0.13, for Win64 (x86_64)
--
-- Host: localhost    Database: induscore
-- ------------------------------------------------------
-- Server version	8.0.13

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8mb4 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `induscore`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `induscore` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;

USE `induscore`;

--
-- Table structure for table `agent_audit_log`
--

DROP TABLE IF EXISTS `agent_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `agent_audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `trace_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `client_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `request_summary` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `response_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `model_latency_ms` int(11) DEFAULT NULL,
  `prompt_tokens` int(11) DEFAULT NULL,
  `completion_tokens` int(11) DEFAULT NULL,
  `total_tokens` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_audit_trace` (`trace_id`),
  KEY `idx_agent_audit_session_created` (`session_id`,`created_at`),
  KEY `idx_agent_audit_user_created` (`user_id`,`created_at`),
  KEY `idx_agent_audit_status` (`response_status`),
  CONSTRAINT `fk_agent_audit_session` FOREIGN KEY (`session_id`) REFERENCES `agent_session` (`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agent_message`
--

DROP TABLE IF EXISTS `agent_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `agent_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `message_role` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `token_count` int(11) DEFAULT NULL,
  `trace_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_agent_message_session_created` (`session_id`,`created_at`),
  KEY `idx_agent_message_trace` (`trace_id`),
  CONSTRAINT `fk_agent_message_session` FOREIGN KEY (`session_id`) REFERENCES `agent_session` (`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agent_session`
--

DROP TABLE IF EXISTS `agent_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `agent_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `client_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active',
  `summary_text` mediumtext COLLATE utf8mb4_unicode_ci,
  `summary_updated_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agent_session_session_id` (`session_id`),
  KEY `idx_agent_session_user_updated` (`user_id`,`updated_at`),
  KEY `idx_agent_session_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `agent_tool_call_log`
--

DROP TABLE IF EXISTS `agent_tool_call_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `agent_tool_call_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `trace_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tool_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `input_json` longtext COLLATE utf8mb4_unicode_ci,
  `output_json` longtext COLLATE utf8mb4_unicode_ci,
  `success` tinyint(1) NOT NULL,
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latency_ms` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_agent_tool_call_session_created` (`session_id`,`created_at`),
  KEY `idx_agent_tool_call_trace` (`trace_id`),
  KEY `idx_agent_tool_call_tool` (`tool_name`),
  CONSTRAINT `fk_agent_tool_call_session` FOREIGN KEY (`session_id`) REFERENCES `agent_session` (`session_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_models`
--

DROP TABLE IF EXISTS `ai_models`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `ai_models` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '模型名称，如 YOLOv11-Industrial',
  `code` varchar(64) NOT NULL COMMENT '模型唯一编码',
  `description` varchar(512) DEFAULT NULL COMMENT '模型描述',
  `architecture` varchar(64) DEFAULT NULL COMMENT '架构类型，如 YOLOv11',
  `dataset` varchar(128) DEFAULT NULL COMMENT '训练数据集，如 NET-DET',
  `task_type` varchar(64) DEFAULT NULL COMMENT '任务类型，如 hot_rolled_steel_defect',
  `status` varchar(32) NOT NULL DEFAULT 'inactive' COMMENT '状态：inactive/active/deployed',
  `is_main` tinyint(1) DEFAULT '0' COMMENT '是否主模型',
  `current_version_id` bigint(20) DEFAULT NULL COMMENT '当前激活的版本ID',
  `current_version` varchar(64) DEFAULT NULL COMMENT '当前版本号冗余存储',
  `model_file_path` varchar(255) DEFAULT NULL COMMENT '模型文件路径',
  `config_file_path` varchar(255) DEFAULT NULL COMMENT '配置文件路径',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `fk_model_current_version` (`current_version_id`),
  KEY `idx_models_status` (`status`),
  KEY `idx_models_is_main` (`is_main`),
  KEY `idx_models_architecture` (`architecture`),
  CONSTRAINT `fk_model_current_version` FOREIGN KEY (`current_version_id`) REFERENCES `model_versions` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模型主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cameras`
--

DROP TABLE IF EXISTS `cameras`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `cameras` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `production_line_id` bigint(20) NOT NULL,
  `name` varchar(64) NOT NULL,
  `position` varchar(64) DEFAULT NULL,
  `online` tinyint(1) NOT NULL,
  `ip` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cameras_line` (`production_line_id`),
  CONSTRAINT `fk_cameras_line` FOREIGN KEY (`production_line_id`) REFERENCES `production_lines` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `departments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '部门名称',
  `code` varchar(64) NOT NULL COMMENT '部门编码',
  `description` varchar(255) DEFAULT NULL COMMENT '部门描述',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '上级部门ID（支持多级部门）',
  `manager_id` bigint(20) DEFAULT NULL COMMENT '部门负责人ID（关联users表）',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序号',
  `status` varchar(16) NOT NULL DEFAULT 'active' COMMENT '状态：active/inactive',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_departments_parent` (`parent_id`),
  KEY `idx_departments_status` (`status`),
  KEY `idx_departments_code` (`code`),
  CONSTRAINT `fk_dept_parent` FOREIGN KEY (`parent_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `detection_records`
--

DROP TABLE IF EXISTS `detection_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `detection_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `detection_no` varchar(64) NOT NULL,
  `serial_no` varchar(64) NOT NULL,
  `defect` varchar(255) DEFAULT NULL,
  `defect_type` varchar(64) DEFAULT NULL,
  `severity` varchar(32) DEFAULT NULL,
  `confidence` double DEFAULT NULL,
  `position_x` varchar(32) DEFAULT NULL,
  `position_y` varchar(32) DEFAULT NULL,
  `area` varchar(32) DEFAULT NULL,
  `impact_level` varchar(16) DEFAULT NULL,
  `production_line` varchar(64) DEFAULT NULL,
  `shift` varchar(64) DEFAULT NULL,
  `model_name` varchar(64) DEFAULT NULL,
  `status` varchar(16) DEFAULT NULL,
  `process_status` varchar(16) DEFAULT NULL,
  `status_note` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `timestamp` datetime NOT NULL,
  `product_id` bigint(20) DEFAULT NULL COMMENT '关联产品ID',
  `production_line_id` bigint(20) DEFAULT NULL COMMENT '关联生产线ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `detection_no` (`detection_no`),
  KEY `idx_detection_product_id` (`product_id`),
  KEY `idx_detection_line_id` (`production_line_id`),
  CONSTRAINT `fk_detection_line` FOREIGN KEY (`production_line_id`) REFERENCES `production_lines` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_detection_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=189 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int(11) NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_general_ci NOT NULL,
  `checksum` int(11) DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int(11) NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `model_versions`
--

DROP TABLE IF EXISTS `model_versions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `model_versions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `model_id` bigint(20) NOT NULL COMMENT '关联模型ID',
  `version` varchar(64) NOT NULL COMMENT '版本号，如 v1.0.0',
  `description` varchar(512) DEFAULT NULL COMMENT '版本描述',
  `status` varchar(32) NOT NULL DEFAULT 'draft' COMMENT '状态：draft/ready/deployed/deprecated',
  `is_current` tinyint(1) DEFAULT '0' COMMENT '是否当前激活版本',
  `accuracy` double DEFAULT NULL COMMENT '准确率(%)',
  `recall` double DEFAULT NULL COMMENT '召回率(%)',
  `f1_score` double DEFAULT NULL COMMENT 'F1分数',
  `precision_val` double DEFAULT NULL COMMENT '精确率(%)',
  `map_val` double DEFAULT NULL COMMENT 'mAP@0.5',
  `map50_95` double DEFAULT NULL COMMENT 'mAP@0.5:0.95',
  `inference_speed` double DEFAULT NULL COMMENT '推理速度(ms)',
  `model_size_mb` double DEFAULT NULL COMMENT '模型大小(MB)',
  `training_data_size` int(11) DEFAULT NULL COMMENT '训练数据量',
  `trained_at` date DEFAULT NULL COMMENT '训练完成日期',
  `trainer` varchar(64) DEFAULT NULL COMMENT '训练负责人',
  `training_epochs` int(11) DEFAULT NULL COMMENT '训练轮数',
  `batch_size` int(11) DEFAULT NULL COMMENT '批次大小',
  `learning_rate` varchar(32) DEFAULT NULL COMMENT '学习率',
  `model_file` varchar(255) DEFAULT NULL COMMENT '模型文件路径或URL',
  `config_file` varchar(255) DEFAULT NULL COMMENT '配置文件路径',
  `label_file` varchar(255) DEFAULT NULL COMMENT '标签文件路径',
  `deployed_at` datetime DEFAULT NULL COMMENT '部署时间',
  `deployed_by` varchar(64) DEFAULT NULL COMMENT '部署人',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model_version` (`model_id`,`version`),
  KEY `idx_versions_model_id` (`model_id`),
  KEY `idx_versions_status` (`status`),
  KEY `idx_versions_is_current` (`is_current`),
  CONSTRAINT `fk_version_model` FOREIGN KEY (`model_id`) REFERENCES `ai_models` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模型版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `operation_logs`
--

DROP TABLE IF EXISTS `operation_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `operation_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '操作用户ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '操作用户名',
  `operation_type` varchar(32) NOT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT/QUERY',
  `module` varchar(64) NOT NULL COMMENT '操作模块：user/role/product/detection等',
  `description` varchar(512) DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(16) DEFAULT NULL COMMENT '请求方法：GET/POST/PUT/DELETE',
  `request_url` varchar(255) DEFAULT NULL COMMENT '请求URL',
  `request_params` text COMMENT '请求参数（JSON格式）',
  `response_status` int(11) DEFAULT NULL COMMENT '响应状态码',
  `ip_address` varchar(64) DEFAULT NULL COMMENT '操作IP地址',
  `user_agent` varchar(255) DEFAULT NULL COMMENT '浏览器User-Agent',
  `duration_ms` int(11) DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `status` varchar(16) DEFAULT 'success' COMMENT '操作状态：success/fail',
  `error_message` text COMMENT '错误信息',
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_operation_user` (`user_id`),
  KEY `idx_operation_module` (`module`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operation_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permissions`
--

DROP TABLE IF EXISTS `permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `permissions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL,
  `name` varchar(128) NOT NULL,
  `module` varchar(64) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `process_records`
--

DROP TABLE IF EXISTS `process_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `process_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `detection_id` bigint(20) NOT NULL,
  `type` varchar(32) DEFAULT NULL,
  `action` varchar(255) DEFAULT NULL,
  `operator` varchar(64) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_process_records_detection` (`detection_id`),
  CONSTRAINT `fk_process_records_detection` FOREIGN KEY (`detection_id`) REFERENCES `detection_records` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=192 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_defect_configs`
--

DROP TABLE IF EXISTS `product_defect_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `product_defect_configs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL COMMENT '关联产品ID',
  `name` varchar(64) NOT NULL COMMENT '缺陷类型名称',
  `threshold` double DEFAULT NULL COMMENT '检测阈值(%)',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pdc_product_id` (`product_id`),
  CONSTRAINT `fk_pdc_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品缺陷类型配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_line_relations`
--

DROP TABLE IF EXISTS `product_line_relations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `product_line_relations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL COMMENT '产品ID',
  `production_line_id` bigint(20) NOT NULL COMMENT '生产线ID',
  `priority` int(11) DEFAULT '0' COMMENT '优先级（数字越小优先级越高）',
  `is_primary` tinyint(1) DEFAULT '0' COMMENT '是否为主生产线',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_line` (`product_id`,`production_line_id`),
  KEY `idx_plr_product_id` (`product_id`),
  KEY `idx_plr_line_id` (`production_line_id`),
  CONSTRAINT `fk_plr_line` FOREIGN KEY (`production_line_id`) REFERENCES `production_lines` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_plr_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品与生产线关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `production_lines`
--

DROP TABLE IF EXISTS `production_lines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `production_lines` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `status` varchar(32) NOT NULL,
  `location` varchar(128) DEFAULT NULL,
  `shift` varchar(64) DEFAULT NULL,
  `running_time` varchar(32) DEFAULT NULL,
  `today_output` int(11) DEFAULT NULL,
  `target_output` int(11) DEFAULT NULL,
  `qualified_count` int(11) DEFAULT NULL,
  `defect_count` int(11) DEFAULT NULL,
  `yield_rate` double DEFAULT NULL,
  `utilization_rate` double DEFAULT NULL,
  `cycle_time` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '产品名称',
  `model` varchar(64) NOT NULL COMMENT '产品型号',
  `category` varchar(32) DEFAULT NULL COMMENT '产品分类 steel/aluminum/plastic',
  `dimensions` varchar(128) DEFAULT NULL COMMENT '尺寸规格',
  `material` varchar(64) DEFAULT NULL COMMENT '材质',
  `weight` double DEFAULT NULL COMMENT '重量(kg)',
  `surface_treatment` varchar(64) DEFAULT NULL COMMENT '表面处理',
  `standard` varchar(128) DEFAULT NULL COMMENT '检测标准(国标)',
  `threshold` double DEFAULT NULL COMMENT '缺陷容忍阈值(%)',
  `target_yield` double DEFAULT NULL COMMENT '目标良率(%)',
  `status` varchar(16) NOT NULL DEFAULT 'active' COMMENT '状态 active/inactive',
  `image_url` varchar(255) DEFAULT NULL COMMENT '产品图片URL',
  `description` varchar(512) DEFAULT NULL COMMENT '产品描述',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `model` (`model`),
  KEY `idx_products_category` (`category`),
  KEY `idx_products_status` (`status`),
  KEY `idx_products_model` (`model`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_permissions`
--

DROP TABLE IF EXISTS `role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `role_permissions` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `fk_role_permissions_permission` (`permission_id`),
  CONSTRAINT `fk_role_permissions_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `code` varchar(64) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stations`
--

DROP TABLE IF EXISTS `stations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `stations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `production_line_id` bigint(20) NOT NULL,
  `name` varchar(64) NOT NULL,
  `status` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_stations_line` (`production_line_id`),
  CONSTRAINT `fk_stations_line` FOREIGN KEY (`production_line_id`) REFERENCES `production_lines` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user_roles` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_user_roles_role` (`role_id`),
  CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `email` varchar(128) NOT NULL,
  `name` varchar(128) NOT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `status` varchar(16) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `department_id` bigint(20) DEFAULT NULL COMMENT '所属部门ID',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `login_count` int(11) DEFAULT '0' COMMENT '登录次数',
  `online_status` tinyint(4) DEFAULT '0' COMMENT '在线状态：0离线 1在线',
  `employee_no` varchar(64) DEFAULT NULL COMMENT '员工编号',
  `position` varchar(128) DEFAULT NULL COMMENT '职位',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_users_department` (`department_id`),
  KEY `idx_users_status` (`status`),
  KEY `idx_users_phone` (`phone`),
  KEY `idx_users_employee_no` (`employee_no`),
  CONSTRAINT `fk_user_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'induscore'
--

--
-- Dumping routines for database 'induscore'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-26 20:42:01
