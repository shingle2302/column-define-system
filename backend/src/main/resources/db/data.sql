-- ============================================================
-- 种子数据：系统 / 业务 / 用户 / 预设
-- ============================================================

-- 接入系统（必须最先插入，因为 sys_user 和 cds_business 依赖它）
INSERT INTO cds_system (id, name, code, description) VALUES
('sys_crm',  'CRM 客户管理系统', 'crm',  '客户资料与跟进管理'),
('sys_hr',   'HR 人事管理系统',  'hr',   '员工档案与考勤管理');

-- 用户 (密码明文: admin123 / alice123 / bob123 / viewer123)
INSERT INTO sys_user (id, username, password, display_name, role, system_id) VALUES
('u001', 'admin', '$2b$12$uLNdiQjPEPkWmcYB3B.5T.AQm8u6AvOG/KXCxY7g4AnNk.dDMPacG', '管理员', 'admin', NULL),
('u002', 'alice', '$2b$12$O1imuTFCAtl4tY0pVgot4.lKEMLkGKGkWk/J1dSGI.GtKWvGhgGKC', 'Alice', 'user', 'sys_crm'),
('u003', 'bob',   '$2b$12$ipJIrg1n3n2RBMSZp32xcO6w/3Br3auEZ3TGuSX/g55ZJu9CoHq/a', 'Bob',   'user', 'sys_hr'),
('u004', 'viewer', '$2b$12$MrRq5cpVQsTMA/RKyjdj7.jbq0PU/NfgDyOYy0Bt8RqtB/QPcS9sO', '观察者', 'viewer', 'sys_crm');

-- 角色
INSERT INTO sys_role (id, name, description) VALUES
(1, 'admin', '系统管理员，拥有所有权限'),
(2, 'user', '普通用户，可使用基本功能'),
(3, 'viewer', '只读用户，仅可查看数据');

-- 业务模块
INSERT INTO cds_business (id, system_id, name, code, description, sort_order) VALUES
('biz_crm_customer',   'sys_crm', '客户管理',   'customer',   '客户信息的增删改查',         0),
('biz_crm_marketing',  'sys_crm', '营销活动',   'marketing',  '营销活动的计划与跟踪',       1),
('biz_crm_lead',       'sys_crm', '线索管理',   'lead',       '销售线索的收集与跟踪',       2),
('biz_crm_contract',   'sys_crm', '合同管理',   'contract',   '合同的创建与履约管理',       3),
('biz_hr_staff',       'sys_hr',  '员工档案',   'staff',      '员工基本信息的维护与查询',   0),
('biz_hr_attendance',  'sys_hr',  '考勤管理',   'attendance', '考勤数据的导入与统计',       1);

-- ========== 预设方案 ==========

-- CRM/客户管理: 默认方案
INSERT INTO column_preset (id, name, description, system_id, business_id, allow_cross_group, version, is_default, min_visible_columns, max_visible_columns) VALUES
('p_crm_customer_default', '标准客户视图', '客户管理的标准列视图，包含基本信息与联系方式', 'sys_crm', 'biz_crm_customer', FALSE, 1, TRUE, 2, 0);

-- CRM/客户管理: 简洁方案
INSERT INTO column_preset (id, name, description, system_id, business_id, allow_cross_group, version, is_default, min_visible_columns) VALUES
('p_crm_customer_compact', '简洁客户视图', '仅显示最核心的客户字段', 'sys_crm', 'biz_crm_customer', FALSE, 1, FALSE, 1);

-- CRM/线索管理: 默认方案
INSERT INTO column_preset (id, name, description, system_id, business_id, version, is_default) VALUES
('p_crm_lead_default', '标准线索视图', '线索管理的标准视图，包含线索信息与跟进状态', 'sys_crm', 'biz_crm_lead', 1, TRUE);

-- CRM/合同管理: 默认方案
INSERT INTO column_preset (id, name, description, system_id, business_id, version, is_default) VALUES
('p_crm_contract_default', '标准合同视图', '合同管理的标准视图，包含合同信息与财务数据', 'sys_crm', 'biz_crm_contract', 1, TRUE);

-- CRM/营销活动: 默认方案
INSERT INTO column_preset (id, name, description, system_id, business_id, version, is_default) VALUES
('p_crm_marketing_default', '标准活动视图', '营销活动的标准视图', 'sys_crm', 'biz_crm_marketing', 1, TRUE);

-- HR/员工档案: 默认方案
INSERT INTO column_preset (id, name, description, system_id, business_id, allow_cross_group, version, is_default, min_visible_columns) VALUES
('p_hr_staff_default', '标准员工视图', '员工档案标准视图', 'sys_hr', 'biz_hr_staff', FALSE, 1, TRUE, 2);

-- HR/考勤管理: 默认方案
INSERT INTO column_preset (id, name, description, system_id, business_id, version, is_default) VALUES
('p_hr_attendance_default', '标准考勤视图', '考勤数据标准视图', 'sys_hr', 'biz_hr_attendance', 1, TRUE);

-- ================================================================
-- 列分组 & 列定义
-- ================================================================

-- [CRM/客户管理/标准] 分组
INSERT INTO column_group (id, label, preset_id, collapsed, sort_order) VALUES
('g_crm_cust_info',   '基本信息', 'p_crm_customer_default', FALSE, 0),
('g_crm_cust_contact', '联系方式', 'p_crm_customer_default', FALSE, 1);

-- [CRM/客户管理/标准] 列
INSERT INTO column_definition (id, label, field, type, width, min_width, max_width, sortable, filterable, visible, pinned, locked, allow_hide, allow_sort, allow_resize, allow_pin, align, group_id, sort_order) VALUES
('d_c0', '客户名称', 'name',        'text',    160, 80,  400, TRUE,  TRUE,  TRUE,  'left', TRUE,  FALSE, FALSE, TRUE,  FALSE, 'left',   'g_crm_cust_info', 0),
('d_c1', '客户编号', 'code',        'text',    120, 60,  200, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_cust_info', 1),
('d_c2', '客户等级', 'level',       'select',  110, 80,  200, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_cust_info', 2),
('d_c3', '所属行业', 'industry',    'text',    140, 80,  300, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_crm_cust_info', 3),
('d_c4', '负责人',   'owner',       'text',    120, 80,  200, FALSE, TRUE,  TRUE,  NULL,   TRUE,  FALSE, FALSE, TRUE,  FALSE, 'center', 'g_crm_cust_info', 4),
('d_c5', '手机',     'phone',       'text',    140, 80,  200, FALSE, FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_cust_contact', 0),
('d_c6', '邮箱',     'email',       'text',    200, 100, 350, FALSE, FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_crm_cust_contact', 1),
('d_c7', '地址',     'address',     'text',    220, 100, 400, FALSE, FALSE, TRUE,  TRUE,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_crm_cust_contact', 2),
('d_c8', '创建时间', 'createdAt',   'date',    160, 100, 250, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_cust_info', 5);

-- 等级选项
UPDATE column_definition SET options_json = '[{"label":"VIP","value":"vip"},{"label":"高级","value":"premium"},{"label":"普通","value":"normal"}]' WHERE id = 'd_c2';

-- [CRM/客户管理/简洁] 分组 & 列
INSERT INTO column_group (id, label, preset_id, collapsed, sort_order) VALUES
('g_crm_cust_compact', '核心字段', 'p_crm_customer_compact', FALSE, 0);

INSERT INTO column_definition (id, label, field, type, width, min_width, max_width, sortable, filterable, visible, pinned, locked, allow_hide, allow_sort, allow_resize, allow_pin, align, group_id, sort_order) VALUES
('d_cc0', '客户名称', 'name',     'text',   180, 100, 400, TRUE,  TRUE,  TRUE, 'left', TRUE,  FALSE, FALSE, TRUE,  FALSE, 'left',   'g_crm_cust_compact', 0),
('d_cc1', '客户等级', 'level',    'select', 120, 80,  200, FALSE, TRUE,  TRUE, NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_cust_compact', 1),
('d_cc2', '负责人',   'owner',    'text',   120, 80,  200, FALSE, TRUE,  TRUE, NULL,   TRUE,  FALSE, FALSE, TRUE,  FALSE, 'center', 'g_crm_cust_compact', 2);

UPDATE column_definition SET options_json = '[{"label":"VIP","value":"vip"},{"label":"高级","value":"premium"},{"label":"普通","value":"normal"}]' WHERE id = 'd_cc1';

-- [CRM/营销活动] 分组 & 列
INSERT INTO column_group (id, label, preset_id, collapsed, sort_order) VALUES
('g_crm_mkt_base',  '基本信息', 'p_crm_marketing_default', FALSE, 0),
('g_crm_mkt_stats', '效果数据', 'p_crm_marketing_default', FALSE, 1);

INSERT INTO column_definition (id, label, field, type, width, min_width, max_width, sortable, filterable, visible, pinned, locked, allow_hide, allow_sort, allow_resize, allow_pin, align, group_id, sort_order) VALUES
('d_m0', '活动名称',   'name',       'text',   180, 100, 350, TRUE,  TRUE,  TRUE,  'left', TRUE,  FALSE, FALSE, TRUE,  FALSE, 'left',   'g_crm_mkt_base', 0),
('d_m1', '活动类型',   'type',       'select', 130, 80,  200, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_mkt_base', 1),
('d_m2', '开始日期',   'startDate',  'date',   130, 100, 200, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_mkt_base', 2),
('d_m3', '结束日期',   'endDate',    'date',   130, 100, 200, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_mkt_base', 3),
('d_m4', '预算',       'budget',     'number', 120, 80,  200, TRUE,  FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'right',  'g_crm_mkt_stats', 0),
('d_m5', '实际花费',   'spent',      'number', 120, 80,  200, TRUE,  FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'right',  'g_crm_mkt_stats', 1),
('d_m6', '转化率',     'conversion', 'number', 100, 60,  150, TRUE,  FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'right',  'g_crm_mkt_stats', 2),
('d_m7', '负责人',     'owner',      'text',   120, 80,  200, FALSE, TRUE,  TRUE,  NULL,   TRUE,  FALSE, FALSE, TRUE,  FALSE, 'center', 'g_crm_mkt_base', 4);

UPDATE column_definition SET options_json = '[{"label":"线上","value":"online"},{"label":"线下","value":"offline"},{"label":"混合","value":"hybrid"}]' WHERE id = 'd_m1';

-- [CRM/线索管理] 分组 & 列
INSERT INTO column_group (id, label, preset_id, collapsed, sort_order) VALUES
('g_crm_lead_base',   '基本信息',   'p_crm_lead_default', FALSE, 0),
('g_crm_lead_source', '来源信息',   'p_crm_lead_default', FALSE, 1),
('g_crm_lead_follow', '跟进信息',   'p_crm_lead_default', FALSE, 2);

INSERT INTO column_definition (id, label, field, type, width, min_width, max_width, sortable, filterable, visible, pinned, locked, allow_hide, allow_sort, allow_resize, allow_pin, align, group_id, sort_order) VALUES
('d_l0', '线索名称',   'name',         'text',   180, 100, 350, TRUE,  TRUE,  TRUE,  'left', TRUE,  FALSE, FALSE, TRUE,  FALSE, 'left',   'g_crm_lead_base', 0),
('d_l1', '线索来源',   'source',       'select', 130, 80,  200, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_lead_source', 0),
('d_l2', '意向产品',   'product',      'text',   140, 80,  250, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_crm_lead_base', 1),
('d_l3', '线索等级',   'leadLevel',    'select', 110, 80,  200, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_lead_base', 2),
('d_l4', '联系人',     'contact',      'text',   120, 80,  200, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_crm_lead_base', 3),
('d_l5', '手机',       'phone',        'text',   140, 80,  200, FALSE, FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_lead_base', 4),
('d_l6', '邮箱',       'email',        'text',   200, 100, 350, FALSE, FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_crm_lead_base', 5),
('d_l7', '公司名称',   'company',      'text',   160, 80,  300, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_crm_lead_source', 1),
('d_l8', '线索状态',   'leadStatus',   'select', 120, 80,  200, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_lead_follow', 0),
('d_l9', '跟进人',     'follower',     'text',   120, 80,  200, FALSE, TRUE,  TRUE,  NULL,   TRUE,  FALSE, FALSE, TRUE,  FALSE, 'center', 'g_crm_lead_follow', 1),
('d_l10', '下次跟进',  'nextFollowAt', 'date',   140, 100, 200, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_lead_follow', 2),
('d_l11', '创建时间',  'createdAt',    'date',   150, 100, 250, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_lead_base', 6);

UPDATE column_definition SET options_json = '[{"label":"官网表单","value":"官网表单"},{"label":"线下展会","value":"线下展会"},{"label":"电话咨询","value":"电话咨询"},{"label":"客户推荐","value":"客户推荐"},{"label":"招投标","value":"招投标"},{"label":"广告投放","value":"广告投放"},{"label":"合作伙伴","value":"合作伙伴"}]' WHERE id = 'd_l1';
UPDATE column_definition SET options_json = '[{"label":"高","value":"高"},{"label":"中","value":"中"},{"label":"低","value":"低"}]' WHERE id = 'd_l3';
UPDATE column_definition SET options_json = '[{"label":"初次联系","value":"初次联系"},{"label":"初步接触","value":"初步接触"},{"label":"需求确认","value":"需求确认"},{"label":"方案制定","value":"方案制定"},{"label":"商务谈判","value":"商务谈判"},{"label":"已签约","value":"已签约"},{"label":"已放弃","value":"已放弃"}]' WHERE id = 'd_l8';

-- [CRM/合同管理] 分组 & 列
INSERT INTO column_group (id, label, preset_id, collapsed, sort_order) VALUES
('g_crm_contract_base',   '基本信息', 'p_crm_contract_default', FALSE, 0),
('g_crm_contract_amount', '金额信息', 'p_crm_contract_default', FALSE, 1),
('g_crm_contract_date',   '日期信息', 'p_crm_contract_default', FALSE, 2);

INSERT INTO column_definition (id, label, field, type, width, min_width, max_width, sortable, filterable, visible, pinned, locked, allow_hide, allow_sort, allow_resize, allow_pin, align, group_id, sort_order) VALUES
('d_ct0', '合同编号',   'contractNo', 'text',   140, 80,  250, TRUE,  TRUE,  TRUE,  'left', TRUE,  FALSE, FALSE, TRUE,  FALSE, 'left',   'g_crm_contract_base', 0),
('d_ct1', '合同名称',   'name',       'text',   200, 100, 400, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_crm_contract_base', 1),
('d_ct2', '客户名称',   'customer',   'text',   180, 80,  350, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_crm_contract_base', 2),
('d_ct3', '合同类型',   'contractType', 'select', 120, 80,  200, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_contract_base', 3),
('d_ct4', '合同状态',   'status',     'select', 110, 80,  200, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_contract_base', 4),
('d_ct5', '合同金额',   'amount',     'number', 130, 80,  250, TRUE,  FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'right',  'g_crm_contract_amount', 0),
('d_ct6', '已付金额',   'paid',       'number', 130, 80,  250, TRUE,  FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'right',  'g_crm_contract_amount', 1),
('d_ct7', '未付金额',   'unpaid',     'number', 130, 80,  250, TRUE,  FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'right',  'g_crm_contract_amount', 2),
('d_ct8', '开始日期',   'startDate',  'date',   130, 100, 200, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_contract_date', 0),
('d_ct9', '结束日期',   'endDate',    'date',   130, 100, 200, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_contract_date', 1),
('d_ct10', '签署日期',  'signDate',   'date',   130, 100, 200, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_crm_contract_date', 2),
('d_ct11', '负责人',    'owner',      'text',   120, 80,  200, FALSE, TRUE,  TRUE,  NULL,   TRUE,  FALSE, FALSE, TRUE,  FALSE, 'center', 'g_crm_contract_base', 5);

UPDATE column_definition SET options_json = '[{"label":"销售合同","value":"销售合同"},{"label":"服务合同","value":"服务合同"},{"label":"实施合同","value":"实施合同"}]' WHERE id = 'd_ct3';
UPDATE column_definition SET options_json = '[{"label":"待签署","value":"待签署"},{"label":"执行中","value":"执行中"},{"label":"已完成","value":"已完成"},{"label":"已终止","value":"已终止"}]' WHERE id = 'd_ct4';

-- [HR/员工档案] 分组 & 列
INSERT INTO column_group (id, label, preset_id, collapsed, sort_order) VALUES
('g_hr_staff_base',  '基本信息', 'p_hr_staff_default', FALSE, 0),
('g_hr_staff_job',   '职位信息', 'p_hr_staff_default', FALSE, 1);

INSERT INTO column_definition (id, label, field, type, width, min_width, max_width, sortable, filterable, visible, pinned, locked, allow_hide, allow_sort, allow_resize, allow_pin, align, group_id, sort_order) VALUES
('d_h0', '姓名',     'name',        'text',    120, 80,  250, TRUE,  TRUE,  TRUE,  'left', TRUE,  FALSE, FALSE, TRUE,  FALSE, 'left',   'g_hr_staff_base', 0),
('d_h1', '工号',     'employeeId',  'text',    100, 60,  150, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_hr_staff_base', 1),
('d_h2', '部门',     'department',  'select',  140, 80,  250, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_hr_staff_job', 0),
('d_h3', '职位',     'title',       'text',    140, 80,  250, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_hr_staff_job', 1),
('d_h4', '入职日期', 'hireDate',    'date',    130, 100, 200, TRUE,  TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_hr_staff_job', 2),
('d_h5', '手机',     'phone',       'text',    140, 80,  200, FALSE, FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_hr_staff_base', 2),
('d_h6', '邮箱',     'email',       'text',    200, 100, 350, FALSE, FALSE, TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_hr_staff_base', 3),
('d_h7', '状态',     'status',      'select',  100, 80,  150, FALSE, TRUE,  TRUE,  NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_hr_staff_job', 3);

UPDATE column_definition SET options_json = '[{"label":"在职","value":"active"},{"label":"离职","value":"inactive"},{"label":"试用期","value":"probation"}]' WHERE id = 'd_h7';
UPDATE column_definition SET options_json = '[{"label":"技术部","value":"tech"},{"label":"产品部","value":"product"},{"label":"市场部","value":"marketing"},{"label":"人事部","value":"hr"}]' WHERE id = 'd_h2';

-- [HR/考勤管理] 分组 & 列
INSERT INTO column_group (id, label, preset_id, collapsed, sort_order) VALUES
('g_hr_att_base', '考勤记录', 'p_hr_attendance_default', FALSE, 0);

INSERT INTO column_definition (id, label, field, type, width, min_width, max_width, sortable, filterable, visible, pinned, locked, allow_hide, allow_sort, allow_resize, allow_pin, align, group_id, sort_order) VALUES
('d_a0', '姓名',        'name',        'text',   120, 80,  200, TRUE,  TRUE,  TRUE, 'left', TRUE,  FALSE, FALSE, TRUE,  FALSE, 'left',   'g_hr_att_base', 0),
('d_a1', '日期',        'date',        'date',   130, 100, 200, TRUE,  TRUE,  TRUE, NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_hr_att_base', 1),
('d_a2', '上班打卡',    'checkIn',     'text',   110, 80,  180, FALSE, FALSE, TRUE, NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_hr_att_base', 2),
('d_a3', '下班打卡',    'checkOut',    'text',   110, 80,  180, FALSE, FALSE, TRUE, NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_hr_att_base', 3),
('d_a4', '状态',        'status',      'select', 100, 80,  150, FALSE, TRUE,  TRUE, NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'center', 'g_hr_att_base', 4),
('d_a5', '备注',        'remark',      'text',   160, 80,  300, FALSE, FALSE, TRUE, NULL,   FALSE, TRUE,  TRUE,  TRUE,  TRUE,  'left',   'g_hr_att_base', 5);

UPDATE column_definition SET options_json = '[{"label":"正常","value":"normal"},{"label":"迟到","value":"late"},{"label":"早退","value":"early"},{"label":"缺勤","value":"absent"}]' WHERE id = 'd_a4';

-- 角色级预设：admin 角色看到的 HR/员工档案 额外保留「部门」「职位」不可隐藏
INSERT INTO cds_role_preset (preset_id, role, column_id, override_order, override_width, override_visible, override_pinned) VALUES
('p_hr_staff_default', 'admin', 'd_h2', NULL, NULL, TRUE, 'left');
