/**
 * base 模块，系统基础模块，负责系统层对业务层提供的功能服务。
 * 包含：基础设施运维与管理（定时任务、服务器信息）、研发工具（代码生成器、接口文档）、
 * 以及通用业务（system 用户/部门/权限/字典、partner 客商管理、product 产品中心）。
 *
 * 1. Controller URL：以 /infra/ 开头，避免和其它 Module 冲突
 * 2. DataObject 表名：以 infra_ 开头，方便在数据库中区分
 */
package vip.appap.suxin.module.infra;
