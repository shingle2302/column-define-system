<template>
  <!-- 未登录：全屏登陆页 -->
  <div v-if="!authToken" class="login-gate">
    <div class="login-gate__card">
      <div class="login-gate__brand">
        <TeamOutlined style="font-size: 36px; color: #1677ff" />
        <h1>CRM 系统</h1>
        <p>请先登录后使用</p>
      </div>
      <LoginPanel
        ref="loginPanelRef"
        @login="onLogin"
        @logout="onLogout"
      />
    </div>
  </div>

  <!-- 已登录：主界面 -->
  <a-layout v-else class="crm">
    <!-- 侧边栏 -->
    <a-layout-sider v-model:collapsed="sidebarCollapsed" :trigger="null" collapsible class="crm__sider">
      <div class="crm__logo">
        <TeamOutlined v-if="sidebarCollapsed" style="font-size: 22px" />
        <span v-else>CRM 系统</span>
      </div>
      <a-menu v-model:selectedKeys="activeModule" mode="inline" class="crm__menu">
        <a-menu-item key="customer">
          <UserOutlined /><span>客户管理</span>
        </a-menu-item>
        <a-menu-item key="lead">
          <AimOutlined /><span>线索管理</span>
        </a-menu-item>
        <a-menu-item key="contract">
          <FileTextOutlined /><span>合同管理</span>
        </a-menu-item>
        <a-menu-divider />
        <a-menu-item key="about">
          <InfoCircleOutlined /><span>关于组件</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <!-- 顶部 -->
      <a-layout-header class="crm__header">
        <div class="crm__header-left">
          <MenuFoldOutlined
            v-if="!sidebarCollapsed"
            class="crm__trigger"
            @click="sidebarCollapsed = true"
          />
          <MenuUnfoldOutlined
            v-else
            class="crm__trigger"
            @click="sidebarCollapsed = false"
          />
          <a-breadcrumb class="crm__breadcrumb">
            <a-breadcrumb-item>工作台</a-breadcrumb-item>
            <a-breadcrumb-item>{{ moduleName }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <a-space size="middle">
          <!-- 登录状态 -->
          <template v-if="authToken">
            <a-tag :color="authRole === 'admin' ? 'red' : authRole === 'user' ? 'blue' : 'default'">
              {{ authRole }}
            </a-tag>
            <a-avatar style="background-color: #1677ff">{{ authDisplayName?.[0] || 'U' }}</a-avatar>
            <span style="color: #333">{{ authDisplayName || authUsername }}</span>
            <span style="color: #bbb; font-size: 12px">系统: {{ authSystemId || '全系统' }}</span>
            <a-button type="text" danger @click="onLogout">
              <LogoutOutlined />退出
            </a-button>
          </template>
          <span v-else style="color: #bbb; font-size: 13px">未登录</span>
        </a-space>
      </a-layout-header>

      <!-- 内容区 -->
      <a-layout-content class="crm__content">
        <!-- CRM 数据表格（列自定义全部封装在 CrmTable 中） -->
        <div class="crm__table-wrap" v-if="isCrmModule">
          <div style="margin-bottom:12px;display:flex;align-items:center;justify-content:space-between">
            <span class="crm__table-title">{{ moduleName }}数据列表</span>
            <a-input-search
              v-model:value="searchText"
              placeholder="全局搜索..."
              style="width: 260px"
              allow-clear
            />
          </div>
          <CrmTable
            :key="activeModule[0]"
            :server-url="serverUrl"
            :token="authToken"
            :user="authUsername"
            :password="authPassword"
            :system-id="authSystemId"
            :business-id="crmBusinessId"
            :data="filteredData"
          >
            <template #header="{ column }">
              <strong>{{ column.label }}</strong>
            </template>
            <template #cell="{ column, value }">
              <template v-if="column.field === 'level' || column.field === 'leadLevel'">
                <a-tag :color="levelColor(value)">{{ value }}</a-tag>
              </template>
              <template v-else-if="column.field === 'status' || column.field === 'leadStatus'">
                <a-tag :color="statusColor(value)">{{ value || '-' }}</a-tag>
              </template>
              <template v-else-if="column.type === 'number' && column.field.includes('Amount') || column.field.includes('Revenue') || column.field.includes('Limit') || column.field.includes('Paid') || column.field.includes('Unpaid')">
                <span style="font-variant-numeric: tabular-nums">¥{{ formatNumber(value) }}</span>
              </template>
              <template v-else>
                <span :style="{ textAlign: column.align || 'left', display: 'inline-block', width: '100%' }">
                  {{ value ?? '-' }}
                </span>
              </template>
            </template>
          </CrmTable>
        </div>

        <!-- 关于页面 -->
        <template v-if="activeModule[0] === 'about'">
          <a-card :bordered="false" class="crm__about">
            <a-typography-title :level="3">列自定义系统 — CRM 集成验证</a-typography-title>
            <a-divider />
            <a-typography-title :level="5">本 Demo 验证的核心功能：</a-typography-title>
            <a-row :gutter="[16, 16]">
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><ColumnWidthOutlined /> 列显示/隐藏</template>
                  <p>用户自由控制列的显隐</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><ColumnHeightOutlined /> 列宽调整</template>
                  <p>自定义每列的宽度</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><PushpinOutlined /> 固定列</template>
                  <p>将列固定在左侧或右侧</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><SortAscendingOutlined /> 排序功能</template>
                  <p>点击表头进行升降序排列</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><SwapOutlined /> 预设方案切换</template>
                  <p>在多个预设方案间快速切换</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><LockOutlined /> 锁定列保护</template>
                  <p>管理员锁定关键列，不可隐藏</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><AlignLeftOutlined /> 文本对齐</template>
                  <p>支持居左 / 居中 / 居右</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><SaveOutlined /> 持久化保存</template>
                  <p>列配置保存到后端，下次登录自动恢复</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><ReloadOutlined /> 一键恢复</template>
                  <p>恢复为预设默认列配置</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><SettingOutlined /> 列自定义弹窗</template>
                  <p>集中管理列的显隐、宽度、固定等所有设置</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><TableOutlined /> 多场景适配</template>
                  <p>同一组件适配客户 / 线索 / 合同</p>
                </a-card>
              </a-col>
              <a-col :span="8">
                <a-card size="small" hoverable>
                  <template #title><UserOutlined /> 多角色权限</template>
                  <p>admin 管理预设，user 自定义列，不同角色不同能力</p>
                </a-card>
              </a-col>
            </a-row>
            <a-divider />
            <a-typography-title :level="5">集成方式：</a-typography-title>
            <a-typography-paragraph>
              <pre class="crm__code"><code>import CrmTable from '@cds/core/ColumnDataTable'
import type { ColumnPreset } from '@cds/core'

// 1. 定义预设方案（由管理后台配置，运行时可从后端拉取）
const presets: ColumnPreset[] = [ /* 列元数据 */ ]

// 2. 使用 &lt;CrmTable&gt; 组件，传入后端地址和认证信息
&lt;CrmTable
  server-url="http://localhost:8080"
  :token="authToken"
  user="alice"
  password="alice123"
  system-id="sys001"
  business-id="biz_crm_customer"
  :data="tableData"
&gt;
  &lt;template #header="{ column }"&gt;...&lt;/template&gt;
  &lt;template #cell="{ column, value }"&gt;...&lt;/template&gt;
&lt;/CrmTable&gt;

// 3. CrmTable 内置：预设选择器 + 列自定义弹窗 + 表格渲染
// 用户点击「自定义列」即可调整列的显隐/宽度/固定等</code></pre>
            </a-typography-paragraph>
          </a-card>
        </template>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  UserOutlined, AimOutlined, FileTextOutlined, InfoCircleOutlined,
  MenuFoldOutlined, MenuUnfoldOutlined,
  TeamOutlined,
  ColumnWidthOutlined, ColumnHeightOutlined,
  PushpinOutlined, SortAscendingOutlined, SwapOutlined, LockOutlined,
  AlignLeftOutlined, TableOutlined, LogoutOutlined,
  SettingOutlined, SaveOutlined, ReloadOutlined,
} from '@ant-design/icons-vue'
import LoginPanel from './components/LoginPanel.vue'
import CrmTable from '@cds/core/ColumnDataTable'

// -------------------- 模块选择 --------------------
const sidebarCollapsed = ref(false)
const activeModule = ref<string[]>(['customer'])
const moduleName = computed(() => ({
  customer: '客户管理',
  lead: '线索管理',
  contract: '合同管理',
  about: '关于组件',
}[activeModule.value[0] || ''] || ''))

/** 当前是否在 CRM 模块（客户/线索/合同），需要显示统计/表格 */
const isCrmModule = computed(() => {
  const key = activeModule.value[0] || ''
  return ['customer', 'lead', 'contract'].includes(key)
})

// -------------------- 认证状态（来自 LoginPanel emit） --------------------
const serverUrl = ref('http://localhost:8080')
const authToken = ref('')
const authRole = ref('')
const authUserId = ref('')
const authUsername = ref('')
const authPassword = ref('')
const authDisplayName = ref('')
const authSystemId = ref('')

const loginPanelRef = ref<InstanceType<typeof LoginPanel> | null>(null)

function onLogin(user: { username: string; role: string; token: string; systemId: string; password: string }) {
  authToken.value = user.token
  authRole.value = user.role
  authUsername.value = user.username
  authPassword.value = user.password
  authDisplayName.value = user.username
  authSystemId.value = user.systemId
  // 从 LoginPanel 暴露的 getServerUrl 获取服务器地址
  if (loginPanelRef.value) {
    serverUrl.value = loginPanelRef.value.getServerUrl()
  }
}

function onLogout() {
  authToken.value = ''
  authRole.value = ''
  authUserId.value = ''
  authUsername.value = ''
  authPassword.value = ''
  authDisplayName.value = ''
  authSystemId.value = ''
}

// -------------------- CRM 业务 ID 映射 --------------------
const crmBusinessId = computed(() => {
  const m = activeModule.value[0]
  if (m === 'customer') return 'biz_crm_customer'
  if (m === 'lead') return 'biz_crm_lead'
  if (m === 'contract') return 'biz_crm_contract'
  return ''
})

// -------------------- Mock 数据 --------------------
const customerData = [
  { id: '1', name: '腾讯科技有限公司', code: 'C001', level: 'VIP', industry: '互联网', owner: '张经理', phone: '13800000001', email: 'zhang@tencent.com', address: '深圳市南山区科技园', createdAt: '2023-01-15' },
  { id: '2', name: '华为技术有限公司', code: 'C002', level: 'VIP', industry: '通信设备', owner: '李总监', phone: '13900000002', email: 'li@huawei.com', address: '深圳市龙岗区坂田', createdAt: '2022-06-01' },
  { id: '3', name: '小米科技有限公司', code: 'C003', level: '高级', industry: '硬件制造', owner: '王总', phone: '13700000003', email: 'wang@xiaomi.com', address: '北京市海淀区清河', createdAt: '2023-03-20' },
  { id: '4', name: '阿里巴巴集团', code: 'C004', level: 'VIP', industry: '电子商务', owner: '陈总监', phone: '13600000004', email: 'chen@alibaba.com', address: '杭州市余杭区', createdAt: '2021-08-10' },
  { id: '5', name: '字节跳动有限公司', code: 'C005', level: '高级', industry: '互联网', owner: '赵经理', phone: '13500000005', email: 'zhao@bytedance.com', address: '北京市海淀区知春路', createdAt: '2022-11-25' },
  { id: '6', name: '京东集团', code: 'C006', level: '高级', industry: '电子商务', owner: '刘总', phone: '13400000006', email: 'liu@jd.com', address: '北京市大兴区亦庄', createdAt: '2021-04-12' },
  { id: '7', name: '美团科技有限公司', code: 'C007', level: 'VIP', industry: '本地生活', owner: '孙经理', phone: '13300000007', email: 'sun@meituan.com', address: '北京市朝阳区望京', createdAt: '2023-07-08' },
  { id: '8', name: '网易集团', code: 'C008', level: '普通', industry: '互联网', owner: '周总监', phone: '13200000008', email: 'zhou@netease.com', address: '杭州市滨江区', createdAt: '2023-09-14' },
  { id: '9', name: '比亚迪股份有限公司', code: 'C009', level: '高级', industry: '汽车制造', owner: '吴总', phone: '13100000009', email: 'wu@byd.com', address: '深圳市坪山区', createdAt: '2022-02-28' },
  { id: '10', name: '宁德时代新能源', code: 'C010', level: 'VIP', industry: '新能源', owner: '郑经理', phone: '13000000010', email: 'zheng@catl.com', address: '福建省宁德市', createdAt: '2022-05-18' },
  { id: '11', name: '三一重工股份有限公司', code: 'C011', level: '普通', industry: '机械制造', owner: '钱总', phone: '18100000011', email: 'qian@sany.com', address: '长沙市经开区', createdAt: '2023-10-01' },
  { id: '12', name: '海康威视数字技术', code: 'C012', level: '高级', industry: '安防', owner: '冯经理', phone: '18200000012', email: 'feng@hikvision.com', address: '杭州市滨江区', createdAt: '2022-08-20' },
  { id: '13', name: '科大讯飞股份有限公司', code: 'C013', level: '普通', industry: '人工智能', owner: '曹总监', phone: '18300000013', email: 'cao@iflytek.com', address: '合肥市高新区', createdAt: '2024-01-05' },
  { id: '14', name: '顺丰控股股份有限公司', code: 'C014', level: 'VIP', industry: '物流运输', owner: '蒋总', phone: '18400000014', email: 'jiang@sf-express.com', address: '深圳市宝安区', createdAt: '2021-12-03' },
  { id: '15', name: '招商银行股份有限公司', code: 'C015', level: 'VIP', industry: '金融', owner: '沈总', phone: '18500000015', email: 'shen@cmbchina.com', address: '深圳市福田区', createdAt: '2020-05-20' },
]

const leadData = [
  { id: 'L1', name: '某电商平台采购需求', source: '官网表单', product: 'ERP 系统', leadLevel: '高', contact: '陈先生', phone: '15600000100', email: 'c@example.com', company: '某电商科技', leadStatus: '初次联系', follower: '张经理', nextFollowAt: '2024-07-10', createdAt: '2024-07-01' },
  { id: 'L2', name: '某制造企业数字化改造', source: '线下展会', product: 'MES 系统', leadLevel: '高', contact: '刘女士', phone: '15600000200', email: 'l@example.com', company: '某精密制造', leadStatus: '需求确认', follower: '李经理', nextFollowAt: '2024-07-08', createdAt: '2024-07-02' },
  { id: 'L3', name: '某物流公司系统升级', source: '电话咨询', product: 'WMS 系统', leadLevel: '中', contact: '赵先生', phone: '15600000300', email: 'z@example.com', company: '某物流集团', leadStatus: '方案制定', follower: '王经理', nextFollowAt: '2024-07-12', createdAt: '2024-07-03' },
  { id: 'L4', name: '某教育机构管理平台', source: '客户推荐', product: 'CRM 系统', leadLevel: '中', contact: '孙女士', phone: '15600000400', email: 's@example.com', company: '某教育科技', leadStatus: '初步接触', follower: '赵经理', nextFollowAt: '2024-07-15', createdAt: '2024-07-04' },
  { id: 'L5', name: '某银行数字化转型', source: '招投标', product: '数据分析平台', leadLevel: '高', contact: '周总', phone: '15600000500', email: 'z2@example.com', company: '某商业银行', leadStatus: '商务谈判', follower: '陈经理', nextFollowAt: '2024-07-06', createdAt: '2024-06-28' },
  { id: 'L6', name: '某房地产CRM需求', source: '广告投放', product: 'CRM 系统', leadLevel: '低', contact: '吴先生', phone: '15600000600', email: 'w@example.com', company: '某地产集团', leadStatus: '已放弃', follower: '刘经理', nextFollowAt: null, createdAt: '2024-06-15' },
  { id: 'L7', name: '某医疗机构信息化', source: '合作伙伴', product: 'HIS 系统', leadLevel: '中', contact: '郑女士', phone: '15600000700', email: 'z3@example.com', company: '某综合医院', leadStatus: '需求确认', follower: '马经理', nextFollowAt: '2024-07-18', createdAt: '2024-07-05' },
  { id: 'L8', name: '某零售企业选型', source: '官网表单', product: 'ERP 系统', leadLevel: '低', contact: '冯先生', phone: '15600000800', email: 'f@example.com', company: '某零售连锁', leadStatus: '初步接触', follower: '黄经理', nextFollowAt: '2024-07-20', createdAt: '2024-07-04' },
]

const contractData = [
  { id: 'CT01', contractNo: 'HT-2024-001', name: '腾讯ERP系统年度运维合同', customer: '腾讯科技有限公司', contractType: '服务合同', status: '执行中', amount: 8500000, paid: 5000000, unpaid: 3500000, startDate: '2024-01-01', endDate: '2025-12-31', signDate: '2023-12-15', owner: '张经理' },
  { id: 'CT02', contractNo: 'HT-2024-002', name: '华为MES系统实施合同', customer: '华为技术有限公司', contractType: '实施合同', status: '执行中', amount: 12000000, paid: 8000000, unpaid: 4000000, startDate: '2024-03-01', endDate: '2025-06-30', signDate: '2024-02-20', owner: '李经理' },
  { id: 'CT03', contractNo: 'HT-2024-003', name: '小米CRM系统采购合同', customer: '小米科技有限公司', contractType: '销售合同', status: '已完成', amount: 3500000, paid: 3500000, unpaid: 0, startDate: '2024-02-01', endDate: '2024-08-31', signDate: '2024-01-18', owner: '王经理' },
  { id: 'CT04', contractNo: 'HT-2024-004', name: '阿里数据分析平台合同', customer: '阿里巴巴集团', contractType: '销售合同', status: '执行中', amount: 15000000, paid: 10000000, unpaid: 5000000, startDate: '2024-04-01', endDate: '2025-03-31', signDate: '2024-03-10', owner: '陈经理' },
  { id: 'CT05', contractNo: 'HT-2024-005', name: '字节安全审计服务合同', customer: '字节跳动有限公司', contractType: '服务合同', status: '待签署', amount: 2000000, paid: 0, unpaid: 2000000, startDate: null, endDate: null, signDate: null, owner: '赵经理' },
  { id: 'CT06', contractNo: 'HT-2024-006', name: '京东供应链系统升级合同', customer: '京东集团', contractType: '实施合同', status: '执行中', amount: 6800000, paid: 4000000, unpaid: 2800000, startDate: '2024-05-01', endDate: '2025-04-30', signDate: '2024-04-20', owner: '刘经理' },
  { id: 'CT07', contractNo: 'HT-2024-007', name: '美团配送系统维护合同', customer: '美团科技有限公司', contractType: '服务合同', status: '执行中', amount: 4800000, paid: 3000000, unpaid: 1800000, startDate: '2024-06-01', endDate: '2025-05-31', signDate: '2024-05-15', owner: '孙经理' },
  { id: 'CT08', contractNo: 'HT-2024-008', name: '比亚迪MES系统合同', customer: '比亚迪股份有限公司', contractType: '实施合同', status: '待签署', amount: 9500000, paid: 0, unpaid: 9500000, startDate: null, endDate: null, signDate: null, owner: '吴经理' },
  { id: 'CT09', contractNo: 'HT-2023-009', name: '宁德WMS系统合同', customer: '宁德时代新能源', contractType: '销售合同', status: '已终止', amount: 5500000, paid: 3000000, unpaid: 2500000, startDate: '2023-09-01', endDate: '2024-02-28', signDate: '2023-08-10', owner: '郑经理' },
  { id: 'CT10', contractNo: 'HT-2024-010', name: '顺丰物流平台升级合同', customer: '顺丰控股股份有限公司', contractType: '实施合同', status: '执行中', amount: 11000000, paid: 7000000, unpaid: 4000000, startDate: '2024-07-01', endDate: '2025-12-31', signDate: '2024-06-15', owner: '蒋经理' },
  { id: 'CT11', contractNo: 'HT-2024-011', name: '招行数据分析合同', customer: '招商银行股份有限公司', contractType: '服务合同', status: '执行中', amount: 20000000, paid: 12000000, unpaid: 8000000, startDate: '2024-01-15', endDate: '2025-01-14', signDate: '2023-12-28', owner: '沈经理' },
  { id: 'CT12', contractNo: 'HT-2024-012', name: '海康视频AI平台合同', customer: '海康威视数字技术', contractType: '销售合同', status: '已完成', amount: 4200000, paid: 4200000, unpaid: 0, startDate: '2024-03-15', endDate: '2024-09-14', signDate: '2024-03-01', owner: '冯经理' },
]

const currentData = computed<Record<string, unknown>[]>(() => {
  const m = activeModule.value[0]
  if (m === 'customer') return customerData
  if (m === 'lead') return leadData
  if (m === 'contract') return contractData
  return []
})

// -------------------- 搜索 --------------------
const searchText = ref('')
const filteredData = computed(() => {
  if (!searchText.value.trim()) return currentData.value
  const q = searchText.value.toLowerCase()
  return currentData.value.filter(row =>
    Object.values(row).some(v => v != null && String(v).toLowerCase().includes(q))
  )
})

// -------------------- 格式化 --------------------
function formatNumber(v: unknown): string {
  if (v == null) return '-'
  const n = Number(v)
  if (isNaN(n)) return '-'
  return n.toLocaleString('zh-CN')
}

function levelColor(level: unknown): string {
  const s = String(level ?? '')
  if (s.includes('VIP') || s.includes('高')) return 'red'
  if (s.includes('高级') || s.includes('中')) return 'orange'
  return 'default'
}

function statusColor(status: unknown): string {
  const s = String(status ?? '')
  if (s.includes('执行中') || s.includes('需求确认') || s.includes('方案制定') || s.includes('商务谈判')) return 'green'
  if (s.includes('已完成') || s.includes('初次联系') || s.includes('初步接触')) return 'blue'
  if (s.includes('待签署') || s.includes('已放弃')) return 'orange'
  if (s.includes('已终止')) return 'red'
  return 'default'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  background: #f0f2f5;
  min-height: 100vh;
}
</style>

<style scoped>
/* 登陆页门禁 */
.login-gate {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #1677ff 100%);
}
.login-gate__card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 440px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
.login-gate__brand {
  text-align: center;
  margin-bottom: 28px;
}
.login-gate__brand h1 {
  font-size: 22px;
  margin: 12px 0 4px;
  color: #333;
}
.login-gate__brand p {
  font-size: 13px;
  color: #999;
  margin: 0;
}
.login-gate__card :deep(.login-panel) {
  max-width: none;
}

.crm {
  min-height: 100vh;
}

/* 侧边栏 */
.crm__sider {
  background: #fff;
  border-right: 1px solid #f0f0f0;
}
.crm__logo {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #1677ff;
  border-bottom: 1px solid #f0f0f0;
  white-space: nowrap;
  overflow: hidden;
}
.crm__menu {
  border-inline-end: none !important;
  margin-top: 8px;
}

/* 顶部 */
.crm__header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  height: 48px;
  line-height: 48px;
}
.crm__header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.crm__trigger {
  font-size: 16px;
  cursor: pointer;
  color: #666;
  transition: color 0.2s;
}
.crm__trigger:hover {
  color: #1677ff;
}

/* 内容区 */
.crm__content {
  padding: 20px 24px;
  overflow-y: auto;
}

/* 表格 */
.crm__table-wrap {
  margin-top: 0;
}
.crm__table-title {
  font-weight: 600;
  font-size: 15px;
}

/* 关于页 */
.crm__about {
  border-radius: 8px;
}
.crm__code {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 16px 20px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.7;
  overflow-x: auto;
}
</style>
