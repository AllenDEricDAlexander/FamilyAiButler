/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.packages.app-core
 * @ClassName: App
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: FamilyAiButler 企业级前端应用
 * @Version: 1.0
 */
import { useMemo, useState, type Key } from "react";
import {
  App as AntdApp,
  Button,
  Card,
  ConfigProvider,
  Form,
  Input,
  Layout,
  Modal,
  Popconfirm,
  Select,
  Space,
  Statistic,
  Switch,
  Table,
  Tabs,
  Tag,
  Typography
} from "antd";
import type { ColumnsType } from "antd/es/table";
import {
  buildRequestPreview,
  familyCoreContracts,
  previewGeneratePassword,
  previewPasswordList,
  requestJson,
  serviceEndpoints,
  type ApiContract,
  type ApiResult,
  type Category,
  type PasswordView
} from "@family-ai-butler/api-client";
import appIcon from "./assets/icon.png";

type AppSection = "password" | "category" | "security";
type PasswordModalMode = "create" | "edit";
type CategoryModalMode = "create" | "edit";

interface PasswordRecord extends PasswordView {
  key: string;
  id: number;
  name: string;
  accountNumber: string;
  password: string;
  websit: string;
  category: string;
  description: string;
  likeStatus: boolean;
  updatedAt: string;
  status: "强" | "中" | "弱";
}

interface CategoryRecord extends Category {
  key: string;
  id: number;
  name: string;
  description: string;
  parentId: number;
  typeName: string;
  updatedAt: string;
}

interface PasswordFormValues {
  name: string;
  accountNumber: string;
  password: string;
  websit: string;
  category: string;
  description: string;
  likeStatus: boolean;
}

interface CategoryFormValues {
  name: string;
  description: string;
  parentId: number;
  typeName: string;
}

const { Header, Content, Footer } = Layout;
const { Text } = Typography;

const DEFAULT_GATEWAY_URL = getDefaultGatewayUrl();
const appIconUrl = typeof appIcon === "string" ? appIcon : appIcon.uri;

const passwordSeedRows: PasswordRecord[] = [
  {
    key: "1",
    id: 1,
    businessId: "PW202605190001",
    name: "家庭路由器",
    accountNumber: "admin",
    password: "Test123*.+",
    websit: "http://192.168.1.1",
    category: "网络设备",
    description: "家庭网络设备后台账号",
    likeStatus: true,
    updatedAt: "2026-05-19 09:20:00",
    status: "强"
  },
  {
    key: "2",
    id: 2,
    businessId: "PW202605190002",
    name: "NAS 控制台",
    accountNumber: "family",
    password: "FamilyNas2026!",
    websit: "http://nas.local",
    category: "家庭服务",
    description: "家庭 NAS 管理后台",
    likeStatus: false,
    updatedAt: "2026-05-18 22:14:00",
    status: "中"
  },
  {
    key: "3",
    id: 3,
    businessId: "PW202605180001",
    name: "物业缴费平台",
    accountNumber: "mario",
    password: "Property2026#",
    websit: "https://pay.example.com",
    category: "生活缴费",
    description: "物业费和停车费入口",
    likeStatus: false,
    updatedAt: "2026-05-18 18:02:00",
    status: "强"
  },
  {
    key: "4",
    id: 4,
    businessId: "PW202605170001",
    name: "儿童学习 App",
    accountNumber: "parent",
    password: "Learn2026@Home",
    websit: "https://study.example.com",
    category: "教育服务",
    description: "儿童学习平台家长账号",
    likeStatus: true,
    updatedAt: "2026-05-17 21:36:00",
    status: "强"
  }
];

const categorySeedRows: CategoryRecord[] = [
  {
    key: "1",
    id: 1,
    name: "网络设备",
    description: "路由器、NAS、家庭服务器等设备入口",
    parentId: 0,
    typeName: "资产账号",
    updatedAt: "2026-05-19 10:00:00"
  },
  {
    key: "2",
    id: 2,
    name: "生活缴费",
    description: "物业、水电、燃气等生活服务平台",
    parentId: 0,
    typeName: "公共服务",
    updatedAt: "2026-05-18 18:30:00"
  },
  {
    key: "3",
    id: 3,
    name: "教育服务",
    description: "学习平台、校园系统、家长端账号",
    parentId: 0,
    typeName: "成员账号",
    updatedAt: "2026-05-17 21:40:00"
  }
];

/**
 * FamilyAiButler 企业级前端应用。
 *
 * @returns Web 和 Tauri 桌面端共用页面
 */
export function App() {
  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: "#1677ff",
          borderRadius: 4,
          fontSize: 13
        },
        components: {
          Layout: {
            bodyBg: "#f3f5f8",
            headerBg: "#ffffff",
            footerBg: "#ffffff"
          },
          Table: {
            headerBg: "#f4f6fb",
            headerColor: "#3d4b63",
            rowSelectedBg: "#dbeafe",
            rowSelectedHoverBg: "#c7dcff"
          }
        }
      }}
    >
      <AntdApp>
        <FamilyAdminApp />
      </AntdApp>
    </ConfigProvider>
  );
}

/**
 * 渲染家庭助手后台管理主应用。
 *
 * @returns 后台管理页面
 */
function FamilyAdminApp() {
  const { message } = AntdApp.useApp();
  const [passwordForm] = Form.useForm<PasswordFormValues>();
  const [categoryForm] = Form.useForm<CategoryFormValues>();
  const [activeSection, setActiveSection] = useState<AppSection>("password");
  const [gatewayBaseUrl, setGatewayBaseUrl] = useState(DEFAULT_GATEWAY_URL);
  const [passwordRows, setPasswordRows] = useState(passwordSeedRows);
  const [categoryRows, setCategoryRows] = useState(categorySeedRows);
  const [selectedPasswordKeys, setSelectedPasswordKeys] = useState<Key[]>([]);
  const [selectedCategoryKeys, setSelectedCategoryKeys] = useState<Key[]>([]);
  const [passwordKeyword, setPasswordKeyword] = useState("");
  const [categoryKeyword, setCategoryKeyword] = useState("");
  const [passwordCategoryFilter, setPasswordCategoryFilter] = useState<string | undefined>();
  const [loading, setLoading] = useState(false);
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);
  const [passwordModalMode, setPasswordModalMode] = useState<PasswordModalMode>("create");
  const [editingPassword, setEditingPassword] = useState<PasswordRecord | undefined>();
  const [categoryModalOpen, setCategoryModalOpen] = useState(false);
  const [categoryModalMode, setCategoryModalMode] = useState<CategoryModalMode>("create");
  const [editingCategory, setEditingCategory] = useState<CategoryRecord | undefined>();
  const [passwordLength, setPasswordLength] = useState("16");
  const [passwordToCheck, setPasswordToCheck] = useState("Test123*.+");
  const [latestResult, setLatestResult] = useState("页面已就绪。后端通过 IDEA 启动后，可直接查询、保存和删除数据。");

  const filteredPasswordRows = useMemo(
    () =>
      passwordRows.filter((row) => {
        const keywordMatched =
          passwordKeyword.trim().length === 0 ||
          `${row.name}${row.accountNumber}${row.websit}${row.description}`.toLowerCase().includes(passwordKeyword.trim().toLowerCase());
        const categoryMatched = passwordCategoryFilter === undefined || row.category === passwordCategoryFilter;
        return keywordMatched && categoryMatched;
      }),
    [passwordRows, passwordKeyword, passwordCategoryFilter]
  );
  const filteredCategoryRows = useMemo(
    () =>
      categoryRows.filter((row) =>
        categoryKeyword.trim().length === 0
          ? true
          : `${row.name}${row.description}${row.typeName}`.toLowerCase().includes(categoryKeyword.trim().toLowerCase())
      ),
    [categoryRows, categoryKeyword]
  );
  const passwordCategories = useMemo(() => Array.from(new Set(passwordRows.map((row) => row.category))).filter(Boolean), [passwordRows]);

  const passwordColumns: ColumnsType<PasswordRecord> = [
    {
      title: "序号",
      width: 64,
      render: (_value, _record, index) => index + 1
    },
    {
      title: "名称",
      dataIndex: "name",
      width: 180,
      render: (value: string, record) => (
        <Space direction="vertical" size={0}>
          <Button type="link" className="table-link-button" onClick={() => openPasswordModal("edit", record)}>
            {value}
          </Button>
          <Text type="secondary">{record.websit}</Text>
        </Space>
      )
    },
    {
      title: "账号",
      dataIndex: "accountNumber",
      width: 140
    },
    {
      title: "分类",
      dataIndex: "category",
      width: 130,
      render: (value: string) => <Tag color="blue">{value}</Tag>
    },
    {
      title: "强度",
      dataIndex: "status",
      width: 90,
      render: (value: PasswordRecord["status"]) => <Tag color={value === "弱" ? "red" : value === "中" ? "gold" : "green"}>{value}</Tag>
    },
    {
      title: "收藏",
      dataIndex: "likeStatus",
      width: 90,
      render: (value: boolean) => (value ? <Tag color="processing">是</Tag> : <Text type="secondary">否</Text>)
    },
    {
      title: "更新时间",
      dataIndex: "updatedAt",
      width: 180
    },
    {
      title: "操作",
      fixed: "right",
      width: 130,
      render: (_value, record) => (
        <Space>
          <Button type="link" onClick={() => openPasswordModal("edit", record)}>
            编辑
          </Button>
          <Popconfirm title="确认删除该账号？" okText="删除" cancelText="取消" onConfirm={() => deletePasswords([record.id])}>
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  const categoryColumns: ColumnsType<CategoryRecord> = [
    {
      title: "序号",
      width: 64,
      render: (_value, _record, index) => index + 1
    },
    {
      title: "分类名称",
      dataIndex: "name",
      width: 180,
      render: (value: string, record) => (
        <Button type="link" className="table-link-button" onClick={() => openCategoryModal("edit", record)}>
          {value}
        </Button>
      )
    },
    {
      title: "类型",
      dataIndex: "typeName",
      width: 140,
      render: (value: string) => <Tag color="geekblue">{value}</Tag>
    },
    {
      title: "父级 ID",
      dataIndex: "parentId",
      width: 110
    },
    {
      title: "说明",
      dataIndex: "description"
    },
    {
      title: "更新时间",
      dataIndex: "updatedAt",
      width: 180
    },
    {
      title: "操作",
      fixed: "right",
      width: 130,
      render: (_value, record) => (
        <Space>
          <Button type="link" onClick={() => openCategoryModal("edit", record)}>
            编辑
          </Button>
          <Popconfirm title="确认删除该分类？" okText="删除" cancelText="取消" onConfirm={() => deleteCategory(record.id)}>
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  /**
   * 查询账号密码列表。
   *
   * @returns 请求完成状态
   */
  async function refreshPasswordList() {
    const preview = previewPasswordList(gatewayBaseUrl, 1, 50);
    setLoading(true);
    setLatestResult(`GET ${preview.url}`);
    try {
      const result = await requestJson<ApiResult<unknown>>(preview);
      const rows = pickList<PasswordView>(result).map((item, index) => toPasswordRecord(item, index));
      setPasswordRows(rows.length > 0 ? rows : passwordSeedRows);
      setLatestResult(JSON.stringify(result, null, 2));
      message.success("密码列表已刷新");
    } catch (error) {
      setLatestResult(error instanceof Error ? error.message : String(error));
      message.warning("后端暂不可用，继续使用本地数据");
    } finally {
      setLoading(false);
    }
  }

  /**
   * 查询分类列表。
   *
   * @returns 请求完成状态
   */
  async function refreshCategoryList() {
    const contract = getContract("category-list");
    const preview = buildRequestPreview(gatewayBaseUrl, contract);
    setLoading(true);
    setLatestResult(`GET ${preview.url}`);
    try {
      const result = await requestJson<ApiResult<unknown>>(preview);
      const rows = pickList<Category>(result).map((item, index) => toCategoryRecord(item, index));
      setCategoryRows(rows.length > 0 ? rows : categorySeedRows);
      setLatestResult(JSON.stringify(result, null, 2));
      message.success("分类列表已刷新");
    } catch (error) {
      setLatestResult(error instanceof Error ? error.message : String(error));
      message.warning("后端暂不可用，继续使用本地数据");
    } finally {
      setLoading(false);
    }
  }

  /**
   * 打开账号编辑弹窗。
   *
   * @param mode 弹窗模式
   * @param record 当前账号记录
   */
  function openPasswordModal(mode: PasswordModalMode, record?: PasswordRecord) {
    setPasswordModalMode(mode);
    setEditingPassword(record);
    passwordForm.setFieldsValue(
      record ?? {
        name: "",
        accountNumber: "",
        password: "",
        websit: "",
        category: passwordCategories[0] ?? "OTHER",
        description: "",
        likeStatus: false
      }
    );
    setPasswordModalOpen(true);
  }

  /**
   * 打开分类编辑弹窗。
   *
   * @param mode 弹窗模式
   * @param record 当前分类记录
   */
  function openCategoryModal(mode: CategoryModalMode, record?: CategoryRecord) {
    setCategoryModalMode(mode);
    setEditingCategory(record);
    categoryForm.setFieldsValue(
      record ?? {
        name: "",
        description: "",
        parentId: 0,
        typeName: "资产账号"
      }
    );
    setCategoryModalOpen(true);
  }

  /**
   * 保存账号密码记录。
   *
   * @returns 请求完成状态
   */
  async function savePassword() {
    const values = await passwordForm.validateFields();
    const isCreate = passwordModalMode === "create";
    const contract = getContract(isCreate ? "password-add" : "password-update");
    const body = {
      ...editingPassword,
      ...values
    };
    const preview = buildRequestPreview(gatewayBaseUrl, contract, {}, body);
    setLoading(true);
    setLatestResult(`${preview.method} ${preview.url}\n${JSON.stringify(body, null, 2)}`);
    try {
      const result = await requestJson<ApiResult<unknown>>(preview);
      setLatestResult(JSON.stringify(result, null, 2));
      message.success(isCreate ? "账号已新增" : "账号已更新");
    } catch (error) {
      setLatestResult(error instanceof Error ? error.message : String(error));
      message.warning("后端暂不可用，已先更新本地页面数据");
    } finally {
      const nextRecord = toPasswordRecord(
        {
          ...editingPassword,
          ...values,
          id: editingPassword?.id ?? Date.now(),
          updateTime: nowText()
        },
        passwordRows.length
      );
      setPasswordRows((rows) =>
        isCreate ? [nextRecord, ...rows] : rows.map((row) => (row.id === editingPassword?.id ? { ...nextRecord, key: row.key } : row))
      );
      setPasswordModalOpen(false);
      setLoading(false);
    }
  }

  /**
   * 保存分类记录。
   *
   * @returns 请求完成状态
   */
  async function saveCategory() {
    const values = await categoryForm.validateFields();
    const isCreate = categoryModalMode === "create";
    const contract = getContract(isCreate ? "category-add" : "category-update");
    const body = {
      ...editingCategory,
      ...values,
      categoryType: {
        typeName: values.typeName
      }
    };
    const preview = buildRequestPreview(gatewayBaseUrl, contract, {}, body);
    setLoading(true);
    setLatestResult(`${preview.method} ${preview.url}\n${JSON.stringify(body, null, 2)}`);
    try {
      const result = await requestJson<ApiResult<unknown>>(preview);
      setLatestResult(JSON.stringify(result, null, 2));
      message.success(isCreate ? "分类已新增" : "分类已更新");
    } catch (error) {
      setLatestResult(error instanceof Error ? error.message : String(error));
      message.warning("后端暂不可用，已先更新本地页面数据");
    } finally {
      const nextRecord = toCategoryRecord(
        {
          ...editingCategory,
          ...values,
          id: editingCategory?.id ?? Date.now(),
          updateTime: nowText(),
          categoryType: {
            typeName: values.typeName
          }
        },
        categoryRows.length
      );
      setCategoryRows((rows) =>
        isCreate ? [nextRecord, ...rows] : rows.map((row) => (row.id === editingCategory?.id ? { ...nextRecord, key: row.key } : row))
      );
      setCategoryModalOpen(false);
      setLoading(false);
    }
  }

  /**
   * 删除账号密码记录。
   *
   * @param idList 待删除账号 ID 集合
   * @returns 请求完成状态
   */
  async function deletePasswords(idList: number[]) {
    if (idList.length === 0) {
      message.info("请先选择账号记录");
      return;
    }
    const preview = buildRequestPreview(gatewayBaseUrl, getContract("password-delete"), {}, idList);
    setLoading(true);
    setLatestResult(`${preview.method} ${preview.url}\n${JSON.stringify(idList)}`);
    try {
      const result = await requestJson<ApiResult<unknown>>(preview);
      setLatestResult(JSON.stringify(result, null, 2));
      message.success("账号已删除");
    } catch (error) {
      setLatestResult(error instanceof Error ? error.message : String(error));
      message.warning("后端暂不可用，已先删除本地页面数据");
    } finally {
      setPasswordRows((rows) => rows.filter((row) => !idList.includes(row.id)));
      setSelectedPasswordKeys([]);
      setLoading(false);
    }
  }

  /**
   * 删除分类记录。
   *
   * @param id 待删除分类 ID
   * @returns 请求完成状态
   */
  async function deleteCategory(id: number) {
    const preview = buildRequestPreview(gatewayBaseUrl, getContract("category-delete"), { id });
    setLoading(true);
    setLatestResult(`${preview.method} ${preview.url}`);
    try {
      const result = await requestJson<ApiResult<unknown>>(preview);
      setLatestResult(JSON.stringify(result, null, 2));
      message.success("分类已删除");
    } catch (error) {
      setLatestResult(error instanceof Error ? error.message : String(error));
      message.warning("后端暂不可用，已先删除本地页面数据");
    } finally {
      setCategoryRows((rows) => rows.filter((row) => row.id !== id));
      setSelectedCategoryKeys([]);
      setLoading(false);
    }
  }

  /**
   * 调用安全工具接口。
   *
   * @param contractKey 接口契约 key
   * @returns 请求完成状态
   */
  async function runSecurityAction(contractKey: string) {
    const contract = getContract(contractKey);
    const preview =
      contractKey === "password-generate"
        ? previewGeneratePassword(gatewayBaseUrl, Number(passwordLength) || 16)
        : buildRequestPreview(gatewayBaseUrl, contract, { password: passwordToCheck });
    setLoading(true);
    setLatestResult(`${preview.method} ${preview.url}`);
    try {
      const result = await requestJson<ApiResult<unknown>>(preview);
      setLatestResult(JSON.stringify(result, null, 2));
      message.success("安全工具接口已返回");
    } catch (error) {
      setLatestResult(error instanceof Error ? error.message : String(error));
      message.warning("安全工具接口暂不可用");
    } finally {
      setLoading(false);
    }
  }

  const tabs = [
    {
      key: "password",
      label: "密码管理",
      children: (
        <Card className="table-card" title="账号列表">
          <div className="toolbar-row">
            <Space wrap>
              <Button type="primary" onClick={() => openPasswordModal("create")}>
                新增账号
              </Button>
              <Button loading={loading} onClick={refreshPasswordList}>
                查询
              </Button>
              <Popconfirm
                title="确认删除选中的账号？"
                okText="删除"
                cancelText="取消"
                onConfirm={() => deletePasswords(selectedPasswordKeys.map(Number))}
              >
                <Button danger disabled={selectedPasswordKeys.length === 0}>
                  批量删除
                </Button>
              </Popconfirm>
            </Space>
            <Text type="secondary">共 {filteredPasswordRows.length} 条</Text>
          </div>
          <Table<PasswordRecord>
            bordered
            size="middle"
            rowKey="id"
            loading={loading}
            columns={passwordColumns}
            dataSource={filteredPasswordRows}
            rowSelection={{
              selectedRowKeys: selectedPasswordKeys,
              onChange: setSelectedPasswordKeys
            }}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showTotal: (total) => `共${total}条`
            }}
            scroll={{ x: 1120 }}
          />
        </Card>
      )
    },
    {
      key: "category",
      label: "分类管理",
      children: (
        <Card className="table-card" title="分类列表">
          <div className="toolbar-row">
            <Space wrap>
              <Button type="primary" onClick={() => openCategoryModal("create")}>
                新增分类
              </Button>
              <Button loading={loading} onClick={refreshCategoryList}>
                查询
              </Button>
              <Button
                danger
                disabled={selectedCategoryKeys.length === 0}
                onClick={() => selectedCategoryKeys.forEach((key) => deleteCategory(Number(key)))}
              >
                批量删除
              </Button>
            </Space>
            <Text type="secondary">共 {filteredCategoryRows.length} 条</Text>
          </div>
          <Table<CategoryRecord>
            bordered
            size="middle"
            rowKey="id"
            loading={loading}
            columns={categoryColumns}
            dataSource={filteredCategoryRows}
            rowSelection={{
              selectedRowKeys: selectedCategoryKeys,
              onChange: setSelectedCategoryKeys
            }}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showTotal: (total) => `共${total}条`
            }}
            scroll={{ x: 980 }}
          />
        </Card>
      )
    },
    {
      key: "security",
      label: "安全工具",
      children: (
        <Card className="table-card" title="密码安全工具">
          <div className="security-grid">
            <Card size="small" title="密码生成">
              <Space.Compact className="compact-input">
                <Input value={passwordLength} onChange={(event) => setPasswordLength(event.target.value)} placeholder="密码长度" />
                <Button type="primary" loading={loading} onClick={() => runSecurityAction("password-generate")}>
                  生成
                </Button>
              </Space.Compact>
            </Card>
            <Card size="small" title="密码校验">
              <Space.Compact className="compact-input">
                <Input.Password value={passwordToCheck} onChange={(event) => setPasswordToCheck(event.target.value)} placeholder="请输入密码" />
                <Button loading={loading} onClick={() => runSecurityAction("password-strength")}>
                  强度
                </Button>
                <Button loading={loading} onClick={() => runSecurityAction("password-valid")}>
                  合法性
                </Button>
              </Space.Compact>
            </Card>
          </div>
        </Card>
      )
    }
  ];

  return (
    <Layout className="family-admin-layout">
      <Header className="family-header">
        <div className="family-header-inner">
          <div className="brand-area">
            <img className="brand-logo" src={appIconUrl} alt="FamilyAiButler" />
            <div>
              <div className="brand-title">FamilyAiButler</div>
              <div className="brand-subtitle">家庭 AI 管理平台</div>
            </div>
          </div>
          <Space className="header-actions" size={12}>
            <Text type="secondary">后端网关</Text>
            <Input value={gatewayBaseUrl} onChange={(event) => setGatewayBaseUrl(event.target.value)} className="gateway-input" />
            <Tag color="green">本地联调</Tag>
          </Space>
        </div>
      </Header>
      <Content className="family-content">
        <Card className="query-card">
          <Form layout="inline" className="query-form">
            <Form.Item label="业务模块">
              <Select<AppSection> value={activeSection} onChange={setActiveSection} className="query-select">
                <Select.Option value="password">密码管理</Select.Option>
                <Select.Option value="category">分类管理</Select.Option>
                <Select.Option value="security">安全工具</Select.Option>
              </Select>
            </Form.Item>
            {activeSection === "password" ? (
              <>
                <Form.Item label="关键字">
                  <Input value={passwordKeyword} onChange={(event) => setPasswordKeyword(event.target.value)} placeholder="名称 / 账号 / 站点" />
                </Form.Item>
                <Form.Item label="分类">
                  <Select
                    allowClear
                    value={passwordCategoryFilter}
                    onChange={setPasswordCategoryFilter}
                    placeholder="请选择"
                    className="query-select"
                    options={passwordCategories.map((category) => ({ label: category, value: category }))}
                  />
                </Form.Item>
              </>
            ) : null}
            {activeSection === "category" ? (
              <Form.Item label="关键字">
                <Input value={categoryKeyword} onChange={(event) => setCategoryKeyword(event.target.value)} placeholder="分类 / 类型 / 说明" />
              </Form.Item>
            ) : null}
            <Form.Item label="更新时间">
              <Space.Compact>
                <Input className="date-input" placeholder="开始日期" />
                <Input className="date-input" placeholder="结束日期" />
              </Space.Compact>
            </Form.Item>
            <Form.Item>
              <Space>
                <Button type="primary" loading={loading} onClick={activeSection === "category" ? refreshCategoryList : refreshPasswordList}>
                  查询
                </Button>
                <Button
                  onClick={() => {
                    setPasswordKeyword("");
                    setCategoryKeyword("");
                    setPasswordCategoryFilter(undefined);
                  }}
                >
                  清空条件
                </Button>
              </Space>
            </Form.Item>
          </Form>
        </Card>

        <div className="metric-row">
          <Card>
            <Statistic title="密码条目" value={passwordRows.length} />
          </Card>
          <Card>
            <Statistic title="家庭分类" value={categoryRows.length} />
          </Card>
          <Card>
            <Statistic title="Core 接口" value={familyCoreContracts.length} />
          </Card>
          <Card>
            <Statistic title="当前筛选" value={activeSection === "category" ? filteredCategoryRows.length : filteredPasswordRows.length} />
          </Card>
        </div>

        <Tabs activeKey={activeSection} onChange={(key) => setActiveSection(key as AppSection)} items={tabs} className="module-tabs" />

        <Card className="api-card" title="接口信息">
          <pre>{latestResult}</pre>
        </Card>
      </Content>
      <Footer className="family-footer">
        <div className="family-footer-inner">
          <span>FamilyAiButler · 大前端应用 · Expo / React / Ant Design / Tauri</span>
          <span>{serviceEndpoints.map((service) => `${service.name} ${service.basePath}`).join("  ")}</span>
        </div>
      </Footer>

      <Modal
        title={passwordModalMode === "create" ? "新增账号" : "编辑账号"}
        open={passwordModalOpen}
        confirmLoading={loading}
        onOk={savePassword}
        onCancel={() => setPasswordModalOpen(false)}
        okText="保存"
        cancelText="取消"
        width={680}
      >
        <Form form={passwordForm} layout="vertical">
          <div className="modal-form-grid">
            <Form.Item name="name" label="名称" rules={[{ required: true, message: "请输入名称" }]}>
              <Input placeholder="例如：家庭路由器" />
            </Form.Item>
            <Form.Item name="accountNumber" label="账号" rules={[{ required: true, message: "请输入账号" }]}>
              <Input placeholder="例如：admin" />
            </Form.Item>
            <Form.Item name="password" label="密码" rules={[{ required: true, message: "请输入密码" }]}>
              <Input.Password placeholder="请输入密码" />
            </Form.Item>
            <Form.Item name="category" label="分类" rules={[{ required: true, message: "请选择分类" }]}>
              <Select
                showSearch
                options={[...new Set([...passwordCategories, "OTHER"])].map((category) => ({ label: category, value: category }))}
              />
            </Form.Item>
            <Form.Item name="websit" label="站点">
              <Input placeholder="https://example.com" />
            </Form.Item>
            <Form.Item name="likeStatus" label="收藏" valuePropName="checked">
              <Switch checkedChildren="是" unCheckedChildren="否" />
            </Form.Item>
          </div>
          <Form.Item name="description" label="说明">
            <Input.TextArea rows={3} placeholder="请输入账号说明" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={categoryModalMode === "create" ? "新增分类" : "编辑分类"}
        open={categoryModalOpen}
        confirmLoading={loading}
        onOk={saveCategory}
        onCancel={() => setCategoryModalOpen(false)}
        okText="保存"
        cancelText="取消"
        width={620}
      >
        <Form form={categoryForm} layout="vertical">
          <div className="modal-form-grid">
            <Form.Item name="name" label="分类名称" rules={[{ required: true, message: "请输入分类名称" }]}>
              <Input placeholder="例如：网络设备" />
            </Form.Item>
            <Form.Item name="typeName" label="分类类型" rules={[{ required: true, message: "请输入分类类型" }]}>
              <Input placeholder="例如：资产账号" />
            </Form.Item>
            <Form.Item name="parentId" label="父级 ID" rules={[{ required: true, message: "请输入父级 ID" }]}>
              <Input type="number" />
            </Form.Item>
          </div>
          <Form.Item name="description" label="说明">
            <Input.TextArea rows={3} placeholder="请输入分类说明" />
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
}

/**
 * 根据接口 key 获取契约。
 *
 * @param key 接口契约 key
 * @returns 接口契约
 */
function getContract(key: string): ApiContract {
  const contract = familyCoreContracts.find((item) => item.key === key);
  if (contract === undefined) {
    throw new Error(`Unknown api contract: ${key}`);
  }
  return contract;
}

/**
 * 从后端响应中提取列表数据。
 *
 * @param payload 后端响应
 * @returns 列表数据
 */
function pickList<T>(payload: unknown): T[] {
  if (Array.isArray(payload)) {
    return payload as T[];
  }
  if (payload !== null && typeof payload === "object") {
    const record = payload as Record<string, unknown>;
    const data = record.data;
    if (Array.isArray(data)) {
      return data as T[];
    }
    if (data !== null && typeof data === "object") {
      const dataRecord = data as Record<string, unknown>;
      if (Array.isArray(dataRecord.records)) {
        return dataRecord.records as T[];
      }
      if (Array.isArray(dataRecord.list)) {
        return dataRecord.list as T[];
      }
      if (Array.isArray(dataRecord.rows)) {
        return dataRecord.rows as T[];
      }
    }
    if (Array.isArray(record.records)) {
      return record.records as T[];
    }
  }
  return [];
}

/**
 * 转换后端账号对象为表格记录。
 *
 * @param item 后端账号对象
 * @param index 默认序号
 * @returns 表格记录
 */
function toPasswordRecord(item: PasswordView, index: number): PasswordRecord {
  const id = Number(item.id ?? index + 1);
  const password = item.password ?? "";
  return {
    ...item,
    key: String(id),
    id,
    name: item.name ?? "-",
    accountNumber: item.accountNumber ?? "-",
    password,
    websit: item.websit ?? "-",
    category: item.category ?? "OTHER",
    description: item.description ?? "",
    likeStatus: Boolean(item.likeStatus),
    updatedAt: item.updateTime ?? item.lastViewTime ?? nowText(),
    status: password.length >= 12 ? "强" : password.length >= 8 ? "中" : "弱"
  };
}

/**
 * 转换后端分类对象为表格记录。
 *
 * @param item 后端分类对象
 * @param index 默认序号
 * @returns 表格记录
 */
function toCategoryRecord(item: Category, index: number): CategoryRecord {
  const id = Number(item.id ?? index + 1);
  return {
    ...item,
    key: String(id),
    id,
    name: item.name ?? "-",
    description: item.description ?? "",
    parentId: Number(item.parentId ?? 0),
    typeName: item.categoryType?.typeName ?? "-",
    updatedAt: item.updateTime ?? nowText()
  };
}

/**
 * 获取当前展示时间。
 *
 * @returns 格式化时间
 */
function nowText() {
  return new Date().toLocaleString("zh-CN", { hour12: false }).replace(/\//g, "-");
}

/**
 * 获取默认网关地址。
 *
 * @returns 本地开发、Nginx 托管和 Tauri 打包下可用的默认地址
 */
function getDefaultGatewayUrl() {
  const runtime = globalThis as { process?: { env?: Record<string, string | undefined> } };
  const envGatewayUrl = runtime.process?.env?.EXPO_PUBLIC_API_BASE_URL;
  if (envGatewayUrl !== undefined && envGatewayUrl.trim().length > 0) {
    return envGatewayUrl;
  }
  return "http://localhost:9527";
}
