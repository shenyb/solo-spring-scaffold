-- ─── V3__add_user_password.sql ───
-- 为 app_user 表添加密码字段（Security 插件）

ALTER TABLE app_user ADD COLUMN IF NOT EXISTS `password` VARCHAR(128) DEFAULT NULL COMMENT '密码（BCrypt 加密）' AFTER `username`;

-- 为初始用户设置默认密码（BCrypt of "123456"）
UPDATE app_user SET `password` = '$2a$10$EqKcp1WFKVQISheBxmXNOe9r6YkiVQupMHrSRXzM8YfDFO5vs3J2O' WHERE `password` IS NULL;
