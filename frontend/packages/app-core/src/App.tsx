/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.packages.app-core
 * @ClassName: App
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: FamilyAiButler 密码管理前端应用
 * @Version: 1.0
 */
import {type Key, type ReactNode, useEffect, useMemo, useState} from "react";
import {
    Alert,
    App as AntdApp,
    Button,
    Card,
    ConfigProvider,
    Dropdown,
    Empty,
    Form,
    Input,
    Layout,
    Modal,
    Popconfirm,
    Select,
    Space,
    Switch,
    Table,
    Tabs,
    Tag,
    Typography
} from "antd";
import type {ColumnsType, TablePaginationConfig} from "antd/es/table";
import {
    type ApiContract,
    type ApiResult,
    buildRequestPreview,
    type Category,
    familyCoreContracts,
    type PageApiResult,
    type PasswordView,
    previewPasswordList,
    requestJson
} from "@family-ai-butler/api-client";
import appIcon from "./assets/icon.png";

declare const process: { env: { EXPO_PUBLIC_API_BASE_URL?: string } };
const COMPILED_GATEWAY_URL = "__FAMILY_AI_BUTLER_API_BASE_URL__";
const LOCAL_GATEWAY_URL = "http://localhost/api";

type AppSection = "overview" | "password" | "category" | "security" | "member" | "setting";
type AccountTab = "all" | "favorite" | "recent";
type PasswordModalMode = "create" | "edit";
type RiskLevel = "high" | "middle" | "low";

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
    riskLevel: RiskLevel;
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

interface NavItem {
    key: AppSection;
    label: string;
    icon: string;
}

interface MetricCardProps {
    icon: string;
    label: string;
    value: number;
    desc: string;
    tone: "blue" | "green" | "red" | "purple";
}

const {Header, Sider, Content, Footer} = Layout;
const {Text, Title} = Typography;

const DEFAULT_GATEWAY_URL = getDefaultGatewayUrl();
const appIconUrl = typeof appIcon === "string" ? appIcon : appIcon.uri;

const navItems: NavItem[] = [
    {key: "overview", label: "总览", icon: "⌂"},
    {key: "password", label: "密码管理", icon: "▣"},
    {key: "category", label: "分类管理", icon: "♢"},
    {key: "security", label: "安全中心", icon: "◈"},
    {key: "member", label: "家庭成员", icon: "◎"},
    {key: "setting", label: "设置", icon: "⚙"}
];

const sectionMeta: Record<AppSection, { title: string; description: string }> = {
    overview: {
        title: "总览",
        description: "家庭账号数据总览"
    },
    password: {
        title: "密码管理",
        description: "统一管理家庭常用账号与密码"
    },
    category: {
        title: "分类管理",
        description: "维护家庭账号分类"
    },
    security: {
        title: "安全中心",
        description: "账号安全能力"
    },
    member: {
        title: "家庭成员",
        description: "家庭成员信息"
    },
    setting: {
        title: "设置",
        description: "系统配置"
    }
};

/**
 * FamilyAiButler 密码管理前端应用。
 *
 * @returns Web 和 Tauri 桌面端共用页面
 */
export function App() {
    useEffect(() => {
        syncBrowserFavicon(appIconUrl);
    }, []);

    return (
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: "#2f6df6",
                    borderRadius: 10,
                    fontSize: 14,
                    colorText: "#1d2939",
                    colorBgLayout: "#f3f6fb"
                },
                components: {
                    Button: {
                        controlHeight: 38,
                        borderRadius: 8
                    },
                    Card: {
                        borderRadiusLG: 14
                    },
                    Table: {
                        headerBg: "#f8fafd",
                        headerColor: "#536178",
                        rowHoverBg: "#f7fbff"
                    }
                }
            }}
        >
            <AntdApp>
                <PasswordAdminApp/>
            </AntdApp>
        </ConfigProvider>
    );
}

/**
 * 渲染密码管理后台主应用。
 *
 * @returns 密码管理后台页面
 */
function PasswordAdminApp() {
    const {message} = AntdApp.useApp();
    const [passwordForm] = Form.useForm<PasswordFormValues>();
    const [categoryForm] = Form.useForm<CategoryFormValues>();
    const [activeSection, setActiveSection] = useState<AppSection>("password");
    const [gatewayBaseUrl, setGatewayBaseUrl] = useState(DEFAULT_GATEWAY_URL);
    const [passwordRows, setPasswordRows] = useState<PasswordRecord[]>([]);
    const [categoryRows, setCategoryRows] = useState<CategoryRecord[]>([]);
    const [passwordTotal, setPasswordTotal] = useState(0);
    const [categoryTotal, setCategoryTotal] = useState(0);
    const [passwordListError, setPasswordListError] = useState("");
    const [passwordPage, setPasswordPage] = useState(1);
    const [passwordPageSize, setPasswordPageSize] = useState(10);
    const [selectedPasswordKeys, setSelectedPasswordKeys] = useState<Key[]>([]);
    const [accountTab, setAccountTab] = useState<AccountTab>("all");
    const [keyword, setKeyword] = useState("");
    const [categoryFilter, setCategoryFilter] = useState<string | undefined>();
    const [loadingPassword, setLoadingPassword] = useState(false);
    const [loadingCategory, setLoadingCategory] = useState(false);
    const [saving, setSaving] = useState(false);
    const [passwordModalOpen, setPasswordModalOpen] = useState(false);
    const [passwordModalMode, setPasswordModalMode] = useState<PasswordModalMode>("create");
    const [editingPassword, setEditingPassword] = useState<PasswordRecord | undefined>();
    const [categoryModalOpen, setCategoryModalOpen] = useState(false);
    const [editingCategory, setEditingCategory] = useState<CategoryRecord | undefined>();

    const filteredPasswordRows = useMemo(
        () =>
            passwordRows.filter((row) => {
                const searchText = `${row.name}${row.accountNumber}${row.websit}${row.category}${row.description}`.toLowerCase();
                const keywordMatched = keyword.trim().length === 0 || searchText.includes(keyword.trim().toLowerCase());
                const categoryMatched = categoryFilter === undefined || row.category === categoryFilter;
                const tabMatched = accountTab === "all" || (accountTab === "favorite" ? row.likeStatus : row.updatedAt !== "-");
                return keywordMatched && categoryMatched && tabMatched;
            }),
        [passwordRows, keyword, categoryFilter, accountTab]
    );
    const categoryOptions = useMemo(() => {
        const values = new Set<string>();
        passwordRows.forEach((row) => values.add(row.category));
        categoryRows.forEach((row) => values.add(row.name));
        return Array.from(values).filter((value) => value.length > 0 && value !== "-");
    }, [passwordRows, categoryRows]);
    const filteredCategoryRows = useMemo(
        () =>
            categoryRows.filter((row) => {
                const searchText = `${row.name}${row.typeName}${row.description}`.toLowerCase();
                return keyword.trim().length === 0 || searchText.includes(keyword.trim().toLowerCase());
            }),
        [categoryRows, keyword]
    );
    const highRiskCount = useMemo(() => passwordRows.filter((row) => row.riskLevel === "high").length, [passwordRows]);
    const favoriteCount = useMemo(() => passwordRows.filter((row) => row.likeStatus).length, [passwordRows]);
    const currentSectionMeta = sectionMeta[activeSection];

    useEffect(() => {
        void refreshPasswordList(1, passwordPageSize);
        void refreshCategoryList();
    }, []);

    const passwordColumns: ColumnsType<PasswordRecord> = [
        {
            title: "名称",
            dataIndex: "name",
            width: 190,
            render: (value: string, record) => (
                <Space size={10}>
                    <span className="account-icon">{getAccountIcon(record.category)}</span>
                    <Text strong>{value}</Text>
                </Space>
            )
        },
        {
            title: "网址",
            dataIndex: "websit",
            width: 220,
            ellipsis: true
        },
        {
            title: "账号",
            dataIndex: "accountNumber",
            width: 140
        },
        {
            title: "分类",
            dataIndex: "category",
            width: 120,
            render: (value: string) => <Tag className="soft-tag blue-tag">{value}</Tag>
        },
        {
            title: "安全等级",
            dataIndex: "riskLevel",
            width: 120,
            render: (value: RiskLevel) => <Tag className={`risk-tag ${value}`}>{getRiskText(value)}</Tag>
        },
        {
            title: "收藏",
            dataIndex: "likeStatus",
            width: 90,
            render: (value: boolean) => <span className={value ? "star active" : "star"}>★</span>
        },
        {
            title: "最近更新时间",
            dataIndex: "updatedAt",
            width: 180
        },
        {
            title: "操作",
            fixed: "right",
            width: 170,
            render: (_value, record) => (
                <Space size={14}>
                    <Button type="link" onClick={() => openPasswordModal("edit", record)}>
                        查看
                    </Button>
                    <Button type="link" onClick={() => openPasswordModal("edit", record)}>
                        编辑
                    </Button>
                    <Dropdown
                        menu={{
                            items: [
                                {
                                    key: "delete",
                                    danger: true,
                                    label: (
                                        <Popconfirm title="确认删除该账号？" okText="删除" cancelText="取消"
                                                    onConfirm={() => deletePasswords([record.id])}>
                                            删除
                                        </Popconfirm>
                                    )
                                }
                            ]
                        }}
                    >
                        <Button type="link">更多⌄</Button>
                    </Dropdown>
                </Space>
            )
        }
    ];

    const categoryColumns: ColumnsType<CategoryRecord> = [
        {
            title: "分类名称",
            dataIndex: "name",
            width: 180,
            render: (value: string, record) => (
                <Button type="link" className="table-link-button" onClick={() => openCategoryModal(record)}>
                    {value}
                </Button>
            )
        },
        {
            title: "类型",
            dataIndex: "typeName",
            width: 140,
            render: (value: string) => <Tag className="soft-tag green-tag">{value}</Tag>
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
                    <Button type="link" onClick={() => openCategoryModal(record)}>
                        编辑
                    </Button>
                    <Popconfirm title="确认删除该分类？" okText="删除" cancelText="取消"
                                onConfirm={() => deleteCategory(record.id)}>
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
     * @param pageNum 页码
     * @param pageSize 页大小
     * @returns 请求完成状态
     */
    async function refreshPasswordList(pageNum = passwordPage, pageSize = passwordPageSize) {
        const preview = previewPasswordList(gatewayBaseUrl, pageNum, pageSize);
        setLoadingPassword(true);
        try {
            const result = await requestJson<PageApiResult<unknown>>(preview);
            assertApiSuccess(result);
            const rows = pickList<PasswordView>(result).map((item, index) => toPasswordRecord(item, index));
            setPasswordRows(rows);
            setPasswordTotal(Number(result.total ?? rows.length));
            setPasswordPage(Number(result.pageNum ?? pageNum));
            setPasswordPageSize(Number(result.pageSize ?? pageSize));
            setPasswordListError("");
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : String(error);
            setPasswordRows([]);
            setPasswordTotal(0);
            setPasswordListError(errorMessage);
            message.error("账号列表加载失败，请查看页面错误详情");
        } finally {
            setLoadingPassword(false);
        }
    }

    /**
     * 查询分类列表。
     *
     * @returns 请求完成状态
     */
    async function refreshCategoryList() {
        const preview = buildRequestPreview(gatewayBaseUrl, getContract("category-list"));
        setLoadingCategory(true);
        try {
            const result = await requestJson<PageApiResult<unknown>>(preview);
            assertApiSuccess(result);
            const rows = pickList<Category>(result).map((item, index) => toCategoryRecord(item, index));
            setCategoryRows(rows);
            setCategoryTotal(Number(result.total ?? rows.length));
        } catch (error) {
            setCategoryRows([]);
            setCategoryTotal(0);
            message.error(`分类列表加载失败：${error instanceof Error ? error.message : String(error)}`);
        } finally {
            setLoadingCategory(false);
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
                category: categoryOptions[0] ?? "",
                description: "",
                likeStatus: false
            }
        );
        setPasswordModalOpen(true);
    }

    /**
     * 打开分类编辑弹窗。
     *
     * @param record 当前分类记录
     */
    function openCategoryModal(record?: CategoryRecord) {
        setEditingCategory(record);
        categoryForm.setFieldsValue(
            record ?? {
                name: "",
                description: "",
                parentId: 0,
                typeName: ""
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
        const body = {
            ...editingPassword,
            ...values
        };
        const preview = buildRequestPreview(gatewayBaseUrl, getContract(isCreate ? "password-add" : "password-update"), {}, body);
        setSaving(true);
        try {
            assertApiSuccess(await requestJson<ApiResult<unknown>>(preview));
            message.success(isCreate ? "账号已新增" : "账号已更新");
            setPasswordModalOpen(false);
            await refreshPasswordList(isCreate ? 1 : passwordPage, passwordPageSize);
        } catch (error) {
            message.error(`保存失败：${error instanceof Error ? error.message : String(error)}`);
        } finally {
            setSaving(false);
        }
    }

    /**
     * 保存分类记录。
     *
     * @returns 请求完成状态
     */
    async function saveCategory() {
        const values = await categoryForm.validateFields();
        const body = {
            ...editingCategory,
            ...values,
            categoryType: {
                typeName: values.typeName
            }
        };
        const preview = buildRequestPreview(gatewayBaseUrl, getContract(editingCategory ? "category-update" : "category-add"), {}, body);
        setSaving(true);
        try {
            assertApiSuccess(await requestJson<ApiResult<unknown>>(preview));
            message.success(editingCategory ? "分类已更新" : "分类已新增");
            setCategoryModalOpen(false);
            await refreshCategoryList();
        } catch (error) {
            message.error(`保存失败：${error instanceof Error ? error.message : String(error)}`);
        } finally {
            setSaving(false);
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
        setLoadingPassword(true);
        try {
            assertApiSuccess(await requestJson<ApiResult<unknown>>(preview));
            message.success("账号已删除");
            setSelectedPasswordKeys([]);
            await refreshPasswordList(passwordPage, passwordPageSize);
        } catch (error) {
            message.error(`删除失败：${error instanceof Error ? error.message : String(error)}`);
        } finally {
            setLoadingPassword(false);
        }
    }

    /**
     * 删除分类记录。
     *
     * @param id 待删除分类 ID
     * @returns 请求完成状态
     */
    async function deleteCategory(id: number) {
        const preview = buildRequestPreview(gatewayBaseUrl, getContract("category-delete"), {id});
        setLoadingCategory(true);
        try {
            assertApiSuccess(await requestJson<ApiResult<unknown>>(preview));
            message.success("分类已删除");
            await refreshCategoryList();
        } catch (error) {
            message.error(`删除失败：${error instanceof Error ? error.message : String(error)}`);
        } finally {
            setLoadingCategory(false);
        }
    }

    /**
     * 切换表格分页。
     *
     * @param pagination 分页配置
     */
    function changePasswordTable(pagination: TablePaginationConfig) {
        void refreshPasswordList(Number(pagination.current ?? 1), Number(pagination.pageSize ?? 10));
    }

    const passwordTabItems = [
        {
            key: "all",
            label: "全部"
        },
        {
            key: "favorite",
            label: "收藏"
        },
        {
            key: "recent",
            label: "最近更新"
        }
    ];

    /**
     * 重置当前模块筛选条件。
     */
    function resetCurrentFilter() {
        setKeyword("");
        setCategoryFilter(undefined);
    }

    /**
     * 渲染当前模块筛选区。
     *
     * @returns 筛选区
     */
    function renderFilterCard() {
        if (activeSection !== "password" && activeSection !== "category") {
            return null;
        }
        return (
            <Card className="filter-card">
                <div className="filter-form">
                    <FieldBlock label="关键词搜索">
                        <Input.Search
                            value={keyword}
                            onChange={(event) => setKeyword(event.target.value)}
                            placeholder={activeSection === "password" ? "搜索名称、账号或网址" : "搜索分类名称或说明"}
                            allowClear
                        />
                    </FieldBlock>
                    {activeSection === "password" ? (
                        <FieldBlock label="分类">
                            <Select
                                allowClear
                                value={categoryFilter}
                                onChange={setCategoryFilter}
                                placeholder="全部分类"
                                options={categoryOptions.map((value) => ({label: value, value}))}
                            />
                        </FieldBlock>
                    ) : null}
                    <div className="filter-actions">
                        <Button
                            type="primary"
                            loading={activeSection === "password" ? loadingPassword : loadingCategory}
                            onClick={() => {
                                if (activeSection === "password") {
                                    void refreshPasswordList(1, passwordPageSize);
                                } else {
                                    void refreshCategoryList();
                                }
                            }}
                        >
                            查询
                        </Button>
                        <Button onClick={resetCurrentFilter}>重置</Button>
                        <Button type="primary"
                                onClick={() => (activeSection === "password" ? openPasswordModal("create") : openCategoryModal())}>
                            {activeSection === "password" ? "+ 新增账号" : "+ 新增分类"}
                        </Button>
                    </div>
                </div>
            </Card>
        );
    }

    /**
     * 渲染密码管理模块内容。
     *
     * @returns 密码管理内容
     */
    function renderPasswordSection() {
        return (
            <>
                <div className="metric-strip">
                    <MetricCard icon="●" tone="blue" label="账号总数" value={passwordTotal}
                                desc={`当前页 ${passwordRows.length}`}/>
                    <MetricCard icon="▦" tone="green" label="分类数量" value={categoryTotal}
                                desc={`已加载 ${categoryRows.length}`}/>
                    <MetricCard icon="!" tone="red" label="高风险账号" value={highRiskCount} desc="来自当前账号页"/>
                    <MetricCard icon="★" tone="purple" label="收藏账号" value={favoriteCount} desc="来自当前账号页"/>
                </div>

                <Card className="accounts-card">
                    <div className="card-title-row">
                        <Space size={22}>
                            <Title level={5}>账号列表</Title>
                            <Tabs items={passwordTabItems} activeKey={accountTab}
                                  onChange={(key) => setAccountTab(key as AccountTab)} className="inline-tabs"/>
                        </Space>
                        <Space>
                            <Popconfirm title="确认删除选中的账号？" okText="删除" cancelText="取消"
                                        onConfirm={() => deletePasswords(selectedPasswordKeys.map(Number))}>
                                <Button disabled={selectedPasswordKeys.length === 0}>批量删除</Button>
                            </Popconfirm>
                            <Button
                                onClick={() => void refreshPasswordList(passwordPage, passwordPageSize)}>刷新</Button>
                        </Space>
                    </div>
                    {passwordListError.length > 0 ? (
                        <Alert
                            className="api-error-alert"
                            type="error"
                            showIcon
                            message="账号列表接口返回异常"
                            description={<Text className="api-error-detail">{passwordListError}</Text>}
                        />
                    ) : null}
                    <Table<PasswordRecord>
                        rowKey="id"
                        loading={loadingPassword}
                        columns={passwordColumns}
                        dataSource={filteredPasswordRows}
                        locale={{emptyText: <Empty description="后端暂无账号数据"/>}}
                        rowSelection={{selectedRowKeys: selectedPasswordKeys, onChange: setSelectedPasswordKeys}}
                        pagination={{
                            current: passwordPage,
                            pageSize: passwordPageSize,
                            total: passwordTotal,
                            showSizeChanger: true,
                            showTotal: (total) => `共 ${total} 条`
                        }}
                        scroll={{x: 1120}}
                        onChange={changePasswordTable}
                    />
                </Card>
            </>
        );
    }

    /**
     * 渲染分类管理模块内容。
     *
     * @returns 分类管理内容
     */
    function renderCategorySection() {
        return (
            <Card className="accounts-card">
                <div className="card-title-row">
                    <Title level={5}>分类列表</Title>
                    <Space>
                        <Button type="primary" onClick={() => openCategoryModal()}>
                            新增分类
                        </Button>
                        <Button loading={loadingCategory} onClick={() => void refreshCategoryList()}>
                            刷新
                        </Button>
                    </Space>
                </div>
                <Table<CategoryRecord>
                    rowKey="id"
                    loading={loadingCategory}
                    columns={categoryColumns}
                    dataSource={filteredCategoryRows}
                    locale={{emptyText: <Empty description="后端暂无分类数据"/>}}
                    pagination={{total: categoryTotal, pageSize: 10, showTotal: (total) => `共 ${total} 条`}}
                    scroll={{x: 960}}
                />
            </Card>
        );
    }

    /**
     * 渲染当前导航模块内容。
     *
     * @returns 当前模块内容
     */
    function renderSectionContent() {
        if (activeSection === "password") {
            return renderPasswordSection();
        }
        if (activeSection === "category") {
            return renderCategorySection();
        }
        return (
            <Card className="empty-module-card">
                <Empty description="暂无数据"/>
            </Card>
        );
    }

    return (
        <Layout className="password-admin-layout">
            <Sider width={224} className="password-sider">
                <div className="sider-brand">
                    <img src={appIconUrl} alt="FamilyAiButler"/>
                    <span>FamilyAiButler</span>
                </div>
                <div className="sider-nav">
                    {navItems.map((item) => (
                        <button
                            key={item.key}
                            className={activeSection === item.key ? "nav-item active" : "nav-item"}
                            type="button"
                            onClick={() => setActiveSection(item.key)}
                        >
                            <span className="nav-icon">{item.icon}</span>
                            <span>{item.label}</span>
                        </button>
                    ))}
                </div>
            </Sider>
            <Layout className="password-main-layout">
                <Header className="password-header">
                    <div>
                        <Title level={3}>{currentSectionMeta.title}</Title>
                        <Text type="secondary">{currentSectionMeta.description}</Text>
                    </div>
                </Header>
                <div className="mobile-nav-bar" aria-label="模块导航">
                    {navItems.map((item) => (
                        <button
                            key={item.key}
                            className={activeSection === item.key ? "mobile-nav-button active" : "mobile-nav-button"}
                            type="button"
                            onClick={() => setActiveSection(item.key)}
                        >
                            <span className="nav-icon">{item.icon}</span>
                            <span>{item.label}</span>
                        </button>
                    ))}
                </div>
                <Content className="password-content">
                    {renderFilterCard()}
                    <div className="section-body">{renderSectionContent()}</div>
                </Content>
                <Footer className="password-footer">
                    <span>FamilyAiButler 大前端 · React / Ant Design / Tauri</span>
                    <span>后端网关：{gatewayBaseUrl}</span>
                </Footer>
            </Layout>

            <Modal
                title={passwordModalMode === "create" ? "新增账号" : "编辑账号"}
                open={passwordModalOpen}
                confirmLoading={saving}
                onOk={savePassword}
                onCancel={() => setPasswordModalOpen(false)}
                okText="保存"
                cancelText="取消"
                width={720}
            >
                <Form form={passwordForm} layout="vertical">
                    <div className="modal-form-grid">
                        <Form.Item name="name" label="名称" rules={[{required: true, message: "请输入名称"}]}>
                            <Input placeholder="请输入账号名称"/>
                        </Form.Item>
                        <Form.Item name="accountNumber" label="账号" rules={[{required: true, message: "请输入账号"}]}>
                            <Input placeholder="请输入登录账号"/>
                        </Form.Item>
                        <Form.Item name="password" label="密码" rules={[{required: true, message: "请输入密码"}]}>
                            <Input.Password placeholder="请输入密码"/>
                        </Form.Item>
                        <Form.Item name="category" label="分类" rules={[{required: true, message: "请输入分类"}]}>
                            <Input placeholder="请输入分类"/>
                        </Form.Item>
                        <Form.Item name="websit" label="网址">
                            <Input placeholder="https://example.com"/>
                        </Form.Item>
                        <Form.Item name="likeStatus" label="收藏" valuePropName="checked">
                            <Switch checkedChildren="是" unCheckedChildren="否"/>
                        </Form.Item>
                    </div>
                    <Form.Item name="description" label="说明">
                        <Input.TextArea rows={3} placeholder="请输入账号说明"/>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title={editingCategory ? "编辑分类" : "新增分类"}
                open={categoryModalOpen}
                confirmLoading={saving}
                onOk={saveCategory}
                onCancel={() => setCategoryModalOpen(false)}
                okText="保存"
                cancelText="取消"
                width={620}
            >
                <Form form={categoryForm} layout="vertical">
                    <div className="modal-form-grid">
                        <Form.Item name="name" label="分类名称" rules={[{required: true, message: "请输入分类名称"}]}>
                            <Input placeholder="请输入分类名称"/>
                        </Form.Item>
                        <Form.Item name="typeName" label="分类类型">
                            <Input placeholder="请输入分类类型"/>
                        </Form.Item>
                        <Form.Item name="parentId" label="父级 ID">
                            <Input type="number"/>
                        </Form.Item>
                    </div>
                    <Form.Item name="description" label="说明">
                        <Input.TextArea rows={3} placeholder="请输入分类说明"/>
                    </Form.Item>
                </Form>
            </Modal>
        </Layout>
    );
}

/**
 * 渲染筛选字段块。
 *
 * @param props 字段内容
 * @returns 筛选字段块
 */
function FieldBlock(props: { label: string; children: ReactNode }) {
    return (
        <div className="filter-field">
            <Text type="secondary">{props.label}</Text>
            {props.children}
        </div>
    );
}

/**
 * 渲染指标卡片。
 *
 * @param props 指标配置
 * @returns 指标卡片
 */
function MetricCard(props: MetricCardProps) {
    return (
        <Card className="metric-card">
            <div className={`metric-icon ${props.tone}`}>{props.icon}</div>
            <div>
                <Text type="secondary">{props.label}</Text>
                <div className="metric-value">{props.value}</div>
                <Text type="secondary">{props.desc}</Text>
            </div>
        </Card>
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
 * 校验后端业务响应是否成功。
 *
 * @param result 后端响应
 */
function assertApiSuccess(result: ApiResult<unknown>) {
    if (result.code !== undefined && result.code !== 10000) {
        throw new Error(result.message ?? result.msg ?? `业务响应失败：${result.code}`);
    }
    if (result.success === false) {
        throw new Error(result.message ?? result.msg ?? "业务响应失败");
    }
}

/**
 * 从后端响应中提取列表数据。
 *
 * @param payload 后端响应
 * @returns 列表数据
 */
function pickList<T>(payload: unknown): T[] {
    if (Array.isArray(payload)) {
        return flattenList<T>(payload);
    }
    if (payload !== null && typeof payload === "object") {
        const record = payload as Record<string, unknown>;
        const data = record.data;
        if (Array.isArray(data)) {
            return flattenList<T>(data);
        }
        if (data !== null && typeof data === "object") {
            const dataRecord = data as Record<string, unknown>;
            if (Array.isArray(dataRecord.records)) {
                return flattenList<T>(dataRecord.records);
            }
            if (Array.isArray(dataRecord.list)) {
                return flattenList<T>(dataRecord.list);
            }
            if (Array.isArray(dataRecord.rows)) {
                return flattenList<T>(dataRecord.rows);
            }
        }
        if (Array.isArray(record.records)) {
            return flattenList<T>(record.records);
        }
    }
    return [];
}

/**
 * 拉平后端分页列表。
 *
 * @param value 列表数据
 * @returns 一维列表
 */
function flattenList<T>(value: unknown[]): T[] {
    if (value.length === 1 && Array.isArray(value[0])) {
        return value[0] as T[];
    }
    return value as T[];
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
        category: item.category ?? "-",
        description: item.description ?? "",
        likeStatus: Boolean(item.likeStatus),
        updatedAt: item.updateTime ?? item.lastViewTime ?? "-",
        riskLevel: password.length < 8 ? "high" : password.length < 12 ? "middle" : "low"
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
        updatedAt: item.updateTime ?? "-"
    };
}

/**
 * 获取账号类型图标。
 *
 * @param category 账号分类
 * @returns 展示图标
 */
function getAccountIcon(category: string) {
    if (category.includes("网络")) {
        return "▰";
    }
    if (category.includes("教育")) {
        return "◆";
    }
    if (category.includes("生活")) {
        return "▤";
    }
    return "●";
}

/**
 * 获取风险文案。
 *
 * @param level 风险等级
 * @returns 风险文案
 */
function getRiskText(level: RiskLevel) {
    if (level === "high") {
        return "高";
    }
    if (level === "middle") {
        return "中";
    }
    return "低";
}

/**
 * 同步浏览器页签图标。
 *
 * @param iconUrl 图标地址
 */
function syncBrowserFavicon(iconUrl: string) {
    if (typeof document === "undefined") {
        return;
    }
    let link = document.querySelector<HTMLLinkElement>("link[rel~='icon']");
    if (link === null) {
        link = document.createElement("link");
        link.rel = "icon";
        document.head.appendChild(link);
    }
    link.href = iconUrl;
}

/**
 * 获取默认网关地址。
 *
 * @returns 本地开发、Nginx 托管和 Tauri 打包下可用的默认地址
 */
function getDefaultGatewayUrl() {
    const envGatewayUrl = process.env.EXPO_PUBLIC_API_BASE_URL;
    if (envGatewayUrl !== undefined && envGatewayUrl.trim().length > 0) {
        return envGatewayUrl.trim();
    }
    if (!COMPILED_GATEWAY_URL.startsWith("__") && COMPILED_GATEWAY_URL.trim().length > 0) {
        return COMPILED_GATEWAY_URL.trim();
    }
    return LOCAL_GATEWAY_URL;
}
