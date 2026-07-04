/**
 * 使用 Puppeteer-core + 系统 Chrome 截取项目页面截图。
 * 前置条件：demo (5176) + admin (5174) + backend (8080) 已启动。
 * 用法：node scripts/screenshots.cjs
 */
const puppeteer = require('puppeteer-core');
const path = require('path');

const DEMO = 'http://localhost:5176';
const ADMIN = 'http://localhost:5174';
const OUT = path.join(__dirname, '..', 'docs', 'screenshots');

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

(async () => {
  const browser = await puppeteer.launch({
    headless: true,
    executablePath: '/usr/bin/google-chrome',
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
    defaultViewport: { width: 1440, height: 900 },
  });

  async function shot(name, page) {
    await delay(600);
    await page.screenshot({ path: path.join(OUT, `${name}.png`), fullPage: false });
    console.log(`  ✔ ${name}.png`);
  }

  async function clickMenuItem(page, text) {
    await page.evaluate((t) => {
      const items = document.querySelectorAll('.ant-menu-item');
      for (const el of items) {
        if (el.textContent && el.textContent.includes(t)) {
          el.click();
          return;
        }
      }
    }, text);
    await delay(1200);
  }

  async function fillFormField(page, labelText, value) {
    await page.evaluate(({ label, val }) => {
      const items = document.querySelectorAll('.ant-form-item');
      for (const item of items) {
        const lblEl = item.querySelector('.ant-form-item-label label');
        if (!lblEl || !lblEl.textContent) continue;
        if (!lblEl.textContent.includes(label)) continue;
        const input = item.querySelector('input, textarea');
        if (!input) continue;
        const nativeInputValueSetter = Object.getOwnPropertyDescriptor(
          window.HTMLInputElement.prototype, 'value'
        ).set;
        nativeInputValueSetter.call(input, val);
        input.dispatchEvent(new Event('input', { bubbles: true }));
        input.dispatchEvent(new Event('change', { bubbles: true }));
        return;
      }
    }, { label: labelText, val: value });
    await delay(200);
  }

  /** 通过 placeholder 填充 input */
  async function fillByPlaceholder(page, placeholder, value) {
    await page.evaluate(({ ph, val }) => {
      const input = document.querySelector(`input[placeholder="${ph}"]`);
      if (!input) return;
      const setter = Object.getOwnPropertyDescriptor(
        window.HTMLInputElement.prototype, 'value'
      ).set;
      setter.call(input, val);
      input.dispatchEvent(new Event('input', { bubbles: true }));
      input.dispatchEvent(new Event('change', { bubbles: true }));
    }, { ph: placeholder, val: value });
    await delay(200);
  }

  /** 通过文本匹配点击按钮 */
  async function clickButton(page, text) {
    await page.evaluate((t) => {
      const btns = document.querySelectorAll('button');
      for (const b of btns) {
        if ((b.textContent || '').includes(t)) { b.click(); return; }
      }
    }, text);
    await delay(500);
  }

  /** 打开新增弹窗、截图、关闭 */
  async function captureAddModal(page, buttonText, screenshotName, cb = null) {
    await clickButton(page, buttonText);
    await delay(1000);
    try { await page.waitForSelector('.ant-modal', { timeout: 5000 }); } catch {}
    await delay(600);
    await shot(screenshotName, page);
    // 关闭弹窗
    await page.keyboard.press('Escape');
    await delay(400);
    await page.evaluate(() => {
      const m = document.querySelector('.ant-modal:not(.ant-modal-hidden)');
      if (!m) return;
      const btns = m.querySelectorAll('.ant-btn');
      for (const b of btns) {
        if ((b.textContent || '').includes('取消') || (b.textContent || '').includes('Cancel')) {
          b.click(); return;
        }
      }
    });
    await delay(600);
    if (cb) await cb();
  }

  try {
    // ==================== Demo 截图 ====================
    console.log('=== Demo 项目截图 ===');
    const p1 = await browser.newPage();

    // 1. 登录页
    console.log('1/28 Demo 登录页');
    await p1.goto(DEMO, { waitUntil: 'networkidle2' });
    await p1.waitForSelector('.login-gate', { timeout: 10000 });
    await shot('login-page', p1);

    // 2. 登录
    console.log('2/28 Demo 登录中...');
    await fillFormField(p1, '用户名', 'admin');
    await fillFormField(p1, '密码', 'admin123');
    await delay(300);
    await clickButton(p1, '登 录');
    await delay(3000);
    await p1.waitForSelector('.crm__sider', { timeout: 15000 });
    console.log('  Demo 登录成功');

    // 3. 客户管理
    console.log('3/28 客户管理');
    await delay(1500);
    await shot('crm-customer', p1);

    // 4. 线索管理
    console.log('4/28 线索管理');
    await clickMenuItem(p1, '线索管理');
    await delay(1500);
    await shot('crm-lead', p1);

    // 5. 合同管理
    console.log('5/28 合同管理');
    await clickMenuItem(p1, '合同管理');
    await delay(1500);
    await shot('crm-contract', p1);

    // 6. 列自定义弹窗
    console.log('6/28 列自定义弹窗');
    await clickMenuItem(p1, '客户管理');
    await delay(2000);
    await clickButton(p1, '自定义列');
    await delay(1500);
    await p1.waitForSelector('.ant-modal', { timeout: 8000 });
    await delay(600);
    await shot('column-configurator', p1);

    // 7. 关于组件
    console.log('7/28 关于组件');
    await p1.evaluate(() => {
      const btns = document.querySelectorAll('.ant-modal .ant-btn');
      for (const b of btns) {
        if ((b.textContent || '').includes('取消')) { b.click(); return; }
      }
    });
    await delay(800);
    if (await p1.$('.ant-modal')) {
      await p1.keyboard.press('Escape');
      await delay(500);
    }
    await clickMenuItem(p1, '关于组件');
    await delay(1000);
    await shot('about-page', p1);

    await p1.close();
    console.log('Demo 截图完成\n');

    // ==================== Admin 截图 ====================
    console.log('=== Admin 管理后台截图 ===');
    const p2 = await browser.newPage();

    // 8. Admin 登录页
    console.log('8/28 Admin 登录页');
    await p2.goto(`${ADMIN}/#/login`, { waitUntil: 'networkidle2' });
    await p2.waitForSelector('.login-card', { timeout: 10000 });
    await delay(600);
    await shot('admin-login', p2);

    // 9. Admin 登录
    console.log('9/28 Admin 登录中...');
    await fillByPlaceholder(p2, '用户名', 'admin');
    await fillByPlaceholder(p2, '密码', 'admin123');
    await delay(300);
    await clickButton(p2, '登 录');
    await delay(3000);
    await p2.waitForSelector('.admin-sider', { timeout: 15000 });
    console.log('  Admin 登录成功');

    // 10. 仪表盘
    console.log('10/28 仪表盘');
    await delay(2000);
    await shot('admin-dashboard', p2);

    // ---- 系统管理 ----
    console.log('11/28 系统管理');
    await clickMenuItem(p2, '系统管理');
    await delay(2000);
    await shot('admin-systems', p2);

    console.log('12/28 系统管理 - 新增');
    await captureAddModal(p2, '新增系统', 'admin-systems-add');

    // ---- 业务管理 ----
    console.log('13/28 业务管理');
    await clickMenuItem(p2, '业务管理');
    await delay(2000);
    await shot('admin-businesses', p2);

    console.log('14/28 业务管理 - 新增');
    await captureAddModal(p2, '新增业务', 'admin-businesses-add');

    // ---- 预设管理 ----
    console.log('15/28 预设管理');
    await clickMenuItem(p2, '预设管理');
    await delay(2000);
    await shot('admin-presets', p2);

    console.log('16/28 预设管理 - 新增');
    await captureAddModal(p2, '新增预设', 'admin-presets-add');

    // ---- 列定义管理 ----
    console.log('17/28 列定义管理');
    await clickMenuItem(p2, '列定义管理');
    await delay(2000);
    await shot('admin-columns', p2);

    console.log('18/28 列定义管理 - 选择预设后新增列');
    // 列定义管理需要先选择预设方案
    await p2.evaluate(() => {
      const sel = document.querySelector('.ant-select-selector');
      if (sel) sel.click();
    });
    await delay(600);
    await p2.evaluate(() => {
      const opts = document.querySelectorAll('.ant-select-item-option');
      for (const o of opts) { o.click(); return; }
    });
    await delay(2000);
    await captureAddModal(p2, '加列', 'admin-columns-add');

    // ---- 用户覆盖管理 ----
    console.log('19/28 用户覆盖管理');
    await clickMenuItem(p2, '用户覆盖管理');
    await delay(2000);
    await shot('admin-overrides', p2);

    console.log('20/28 用户覆盖管理 - 新增');
    await captureAddModal(p2, '新增用户覆盖', 'admin-overrides-add');

    // ---- 角色预设管理 ----
    console.log('21/28 角色预设管理');
    await clickMenuItem(p2, '角色预设管理');
    await delay(2000);
    await shot('admin-role-presets', p2);

    console.log('22/28 角色预设管理 - 新增');
    await captureAddModal(p2, '新增角色覆盖', 'admin-role-presets-add');

    // ---- 用户管理 ----
    console.log('23/28 用户管理');
    await clickMenuItem(p2, '用户管理');
    await delay(2000);
    await shot('admin-users', p2);

    console.log('24/28 用户管理 - 新增');
    await captureAddModal(p2, '新增用户', 'admin-users-add');

    // ---- 角色管理 ----
    console.log('25/28 角色管理');
    await clickMenuItem(p2, '角色管理');
    await delay(2000);
    await shot('admin-roles', p2);

    console.log('26/28 角色管理 - 新增');
    await captureAddModal(p2, '新增角色', 'admin-roles-add');

    await p2.close();

    console.log('\n全部截图完成！');
  } catch (err) {
    console.error('截图过程出错:', err);
    process.exit(1);
  } finally {
    await browser.close();
  }
})();
