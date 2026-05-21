/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.packages.app-core
 * @ClassName: App
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: FamilyAiButler 多端前端应用
 * @Version: 1.0
 */
import {type Key, type ReactNode, useEffect, useMemo, useState} from "react";
import {
    Alert,
    App as AntdApp,
    Button,
    Card,
    ConfigProvider,
    Descriptions,
    Empty,
    Form,
    Input,
    InputNumber,
    Layout,
    Modal,
    Popconfirm,
    Result,
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
    type AccountResponse,
    type ApiContract,
    type ApiResult,
    type AuthorizationDecisionResponse,
    buildRequestPreview,
    type Category,
    type CategoryType,
    createApiClient,
    findApiContract,
    type HttpMethod,
    type OAuthClientResponse,
    type PageApiResult,
    type PasswordLoginRequest,
    type PasswordView,
    previewPasswordList,
    previewPasswordLogin,
    type ProfileResponse,
    type QueryValue,
    requestJson,
    type RoleResponse,
    type StrengthInfo,
    type TokenPairResponse,
    type UserPermissionResponse,
    type VerifyCodeLoginRequest
} from "@family-ai-butler/api-client";
import appIcon from "./assets/icon.png";

declare const process: { env: { EXPO_PUBLIC_API_BASE_URL?: string } };
const COMPILED_GATEWAY_URL = "__FAMILY_AI_BUTLER_API_BASE_URL__";
const LOCAL_GATEWAY_URL = "http://localhost/api";
const AUTH_SESSION_STORAGE_KEY = "family-ai-butler-auth-session";

type AppSection = "password" | "category" | "ai" | "my" | "profile" | "access" | "device";
type AuthView = "login" | "codeLogin" | "register" | "recover" | "reset";
type AccountTab = "all" | "favorite" | "recent";
type CategoryTab = "category" | "categoryType";
type PasswordModalMode = "create" | "edit";
type RiskLevel = "high" | "middle" | "low";
type VerifyCodeChannel = "email" | "sms";

interface AuthSession {
    accessToken: string;
    refreshToken?: string;
    tokenType?: string;
    expiresIn?: number;
    accountId?: string;
    principal?: string;
}

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

interface CategoryTypeRecord extends CategoryType {
    key: string;
    id: number;
    typeName: string;
    description: string;
    createdAt: string;
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

interface CategoryTypeFormValues {
    typeName: string;
    description: string;
}

interface LoginFormValues {
    principal: string;
    password: string;
    clientId?: string;
    clientSecret?: string;
    deviceName?: string;
    deviceFingerprint?: string;
}

interface VerifyCodeLoginFormValues {
    principal: string;
    verifyCode: string;
    channel: VerifyCodeChannel;
    clientId?: string;
    deviceName?: string;
    deviceFingerprint?: string;
}

interface RegisterFormValues {
    username?: string;
    email?: string;
    phone?: string;
    password: string;
    confirmPassword: string;
}

interface RecoveryFormValues {
    principal: string;
    channel?: string;
}

interface ResetPasswordFormValues {
    principal: string;
    verificationCode: string;
    newPassword: string;
}

interface ProfileFormValues {
    profileId?: string;
    accountId?: string;
    nickname?: string;
    avatar?: string;
    language?: string;
    region?: string;
    profileType?: string;
}

interface AiImageFormValues {
    file: string;
    method: HttpMethod;
}

interface AuthorizationFormValues {
    accessToken?: string;
    resourceService?: string;
    resourcePath?: string;
    action?: string;
}

interface AccountPermissionFormValues {
    accountId: string;
    resourceType?: string;
}

interface RbacRoleFormValues {
    roleId?: string;
    roleCode?: string;
    roleName?: string;
    description?: string;
    status?: string;
}

interface RbacResourceFormValues {
    resourceId?: string;
    resourceCode?: string;
    resourceName?: string;
    resourceService?: string;
    resourcePath?: string;
    resourceType?: string;
    action?: string;
    description?: string;
    status?: string;
}

interface RbacBindFormValues {
    accountId?: string;
    roleId?: string;
    roleCode?: string;
    resourceId?: string;
    resourceCode?: string;
}

interface OAuthClientFormValues {
    clientId?: string;
    clientSecret?: string;
    clientName?: string;
    redirectUris?: string;
    scopes?: string;
    grantTypes?: string;
    status?: string;
}

interface ManualDeleteFormValues {
    sessionId?: string;
    deviceId?: string;
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
const {Paragraph, Text, Title} = Typography;

const DEFAULT_GATEWAY_URL = getDefaultGatewayUrl();
const appIconUrl = typeof appIcon === "string" ? appIcon : appIcon.uri;

const navItems: NavItem[] = [
    {key: "password", label: "密码管理", icon: "▣"},
    {key: "category", label: "分类管理", icon: "♢"},
    {key: "ai", label: "AI 图片理解", icon: "◈"},
    {key: "profile", label: "个人资料", icon: "◎"},
    {key: "access", label: "权限与集成", icon: "▤"},
    {key: "device", label: "会话与设备", icon: "⌁"}
];

const mobileNavItems: NavItem[] = [
    {key: "password", label: "密码", icon: "▣"},
    {key: "category", label: "分类", icon: "♢"},
    {key: "ai", label: "AI", icon: "◈"},
    {key: "my", label: "我的", icon: "◎"}
];

const sectionMeta: Record<AppSection, { title: string; description: string }> = {
    password: {
        title: "密码管理",
        description: "统一管理家庭常用账号与密码"
    },
    category: {
        title: "分类管理",
        description: "维护密码分类和分类类型"
    },
    ai: {
        title: "AI 图片理解",
        description: "调用 Qwen 图片转文本描述接口"
    },
    profile: {
        title: "个人资料",
        description: "维护账号附属 Profile"
    },
    my: {
        title: "我的",
        description: "个人资料、权限集成、会话设备入口"
    },
    access: {
        title: "权限与集成",
        description: "受限的 RBAC、OAuth 和授权决策能力"
    },
    device: {
        title: "会话与设备",
        description: "后端缺少列表接口，仅保留手动删除能力"
    }
};

const passwordTabItems = [
    {key: "all", label: "全部"},
    {key: "favorite", label: "收藏"},
    {key: "recent", label: "最近更新"}
];

/**
 * FamilyAiButler 多端前端应用。
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
                    borderRadius: 8,
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
                        borderRadiusLG: 12
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
                <FamilyAiButlerApp/>
            </AntdApp>
        </ConfigProvider>
    );
}

/**
 * 渲染 FamilyAiButler 应用主体。
 *
 * @returns 应用主体页面
 */
function FamilyAiButlerApp() {
    const {message} = AntdApp.useApp();
    const [loginForm] = Form.useForm<LoginFormValues>();
    const [verifyCodeLoginForm] = Form.useForm<VerifyCodeLoginFormValues>();
    const [registerForm] = Form.useForm<RegisterFormValues>();
    const [recoveryForm] = Form.useForm<RecoveryFormValues>();
    const [resetPasswordForm] = Form.useForm<ResetPasswordFormValues>();
    const [passwordForm] = Form.useForm<PasswordFormValues>();
    const [categoryForm] = Form.useForm<CategoryFormValues>();
    const [categoryTypeForm] = Form.useForm<CategoryTypeFormValues>();
    const [profileForm] = Form.useForm<ProfileFormValues>();
    const [aiImageForm] = Form.useForm<AiImageFormValues>();
    const [authorizationForm] = Form.useForm<AuthorizationFormValues>();
    const [accountPermissionForm] = Form.useForm<AccountPermissionFormValues>();
    const [rbacRoleForm] = Form.useForm<RbacRoleFormValues>();
    const [rbacResourceForm] = Form.useForm<RbacResourceFormValues>();
    const [rbacBindForm] = Form.useForm<RbacBindFormValues>();
    const [oauthClientForm] = Form.useForm<OAuthClientFormValues>();
    const [manualDeleteForm] = Form.useForm<ManualDeleteFormValues>();
    const [authSession, setAuthSession] = useState<AuthSession | undefined>(() => readStoredAuthSession());
    const [authView, setAuthView] = useState<AuthView>("login");
    const [authError, setAuthError] = useState("");
    const [authLoading, setAuthLoading] = useState(false);
    const [activeSection, setActiveSection] = useState<AppSection>("password");
    const [gatewayBaseUrl] = useState(DEFAULT_GATEWAY_URL);
    const [passwordRows, setPasswordRows] = useState<PasswordRecord[]>([]);
    const [categoryRows, setCategoryRows] = useState<CategoryRecord[]>([]);
    const [categoryTypeRows, setCategoryTypeRows] = useState<CategoryTypeRecord[]>([]);
    const [passwordTotal, setPasswordTotal] = useState(0);
    const [categoryTotal, setCategoryTotal] = useState(0);
    const [categoryTypeTotal, setCategoryTypeTotal] = useState(0);
    const [passwordListError, setPasswordListError] = useState("");
    const [categoryListError, setCategoryListError] = useState("");
    const [categoryTypeListError, setCategoryTypeListError] = useState("");
    const [passwordPage, setPasswordPage] = useState(1);
    const [passwordPageSize, setPasswordPageSize] = useState(10);
    const [selectedPasswordKeys, setSelectedPasswordKeys] = useState<Key[]>([]);
    const [accountTab, setAccountTab] = useState<AccountTab>("all");
    const [categoryTab, setCategoryTab] = useState<CategoryTab>("category");
    const [keyword, setKeyword] = useState("");
    const [categoryFilter, setCategoryFilter] = useState<string | undefined>();
    const [loadingPassword, setLoadingPassword] = useState(false);
    const [loadingCategory, setLoadingCategory] = useState(false);
    const [loadingCategoryType, setLoadingCategoryType] = useState(false);
    const [saving, setSaving] = useState(false);
    const [passwordModalOpen, setPasswordModalOpen] = useState(false);
    const [passwordModalMode, setPasswordModalMode] = useState<PasswordModalMode>("create");
    const [editingPassword, setEditingPassword] = useState<PasswordRecord | undefined>();
    const [passwordStrength, setPasswordStrength] = useState<StrengthInfo | undefined>();
    const [categoryModalOpen, setCategoryModalOpen] = useState(false);
    const [editingCategory, setEditingCategory] = useState<CategoryRecord | undefined>();
    const [categoryTypeModalOpen, setCategoryTypeModalOpen] = useState(false);
    const [editingCategoryType, setEditingCategoryType] = useState<CategoryTypeRecord | undefined>();
    const [profileAccountId, setProfileAccountId] = useState(authSession?.accountId ?? "");
    const [profileRows, setProfileRows] = useState<ProfileResponse[]>([]);
    const [profileError, setProfileError] = useState("");
    const [loadingProfile, setLoadingProfile] = useState(false);
    const [aiResult, setAiResult] = useState("");
    const [aiError, setAiError] = useState("");
    const [aiLoading, setAiLoading] = useState(false);
    const [authorizationResult, setAuthorizationResult] = useState<AuthorizationDecisionResponse | undefined>();
    const [permissionResult, setPermissionResult] = useState<UserPermissionResponse | undefined>();
    const [rbacResult, setRbacResult] = useState<unknown>();
    const [oauthRows, setOauthRows] = useState<OAuthClientResponse[]>([]);
    const [oauthResult, setOauthResult] = useState<unknown>();
    const [accessError, setAccessError] = useState("");
    const [accessLoading, setAccessLoading] = useState(false);
    const [deviceResult, setDeviceResult] = useState("");
    const [deviceError, setDeviceError] = useState("");
    const [deviceLoading, setDeviceLoading] = useState(false);

    const apiClient = useMemo(
        () => createApiClient({gatewayBaseUrl, accessToken: authSession?.accessToken}),
        [gatewayBaseUrl, authSession?.accessToken]
    );
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
    const filteredCategoryTypeRows = useMemo(
        () =>
            categoryTypeRows.filter((row) => {
                const searchText = `${row.typeName}${row.description}`.toLowerCase();
                return keyword.trim().length === 0 || searchText.includes(keyword.trim().toLowerCase());
            }),
        [categoryTypeRows, keyword]
    );
    const highRiskCount = useMemo(() => passwordRows.filter((row) => row.riskLevel === "high").length, [passwordRows]);
    const favoriteCount = useMemo(() => passwordRows.filter((row) => row.likeStatus).length, [passwordRows]);
    const currentSectionMeta = sectionMeta[activeSection];

    useEffect(() => {
        if (authSession === undefined) {
            return;
        }
        persistAuthSession(authSession);
        setProfileAccountId(authSession.accountId ?? "");
        void refreshPasswordList(1, passwordPageSize);
        void refreshCategoryList();
        void refreshCategoryTypeList();
    }, [authSession]);

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
            width: 190,
            render: (_value, record) => (
                <Space size={8}>
                    <Button type="link" onClick={() => void openPasswordDetail(record)}>
                        查看
                    </Button>
                    <Button type="link" onClick={() => openPasswordModal("edit", record)}>
                        编辑
                    </Button>
                    <Popconfirm title="确认删除该账号？" okText="删除" cancelText="取消"
                                onConfirm={() => deletePasswords([record.id])}>
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

    const categoryTypeColumns: ColumnsType<CategoryTypeRecord> = [
        {
            title: "类型名称",
            dataIndex: "typeName",
            width: 180,
            render: (value: string, record) => (
                <Button type="link" className="table-link-button" onClick={() => openCategoryTypeModal(record)}>
                    {value}
                </Button>
            )
        },
        {
            title: "说明",
            dataIndex: "description"
        },
        {
            title: "创建时间",
            dataIndex: "createdAt",
            width: 180
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
                    <Button type="link" onClick={() => openCategoryTypeModal(record)}>
                        编辑
                    </Button>
                    <Popconfirm title="确认删除该分类类型？" okText="删除" cancelText="取消"
                                onConfirm={() => deleteCategoryType(record.id)}>
                        <Button type="link" danger>
                            删除
                        </Button>
                    </Popconfirm>
                </Space>
            )
        }
    ];

    /**
     * 请求受保护接口并处理未认证状态。
     *
     * @param contract 接口契约
     * @param pathParams 路径参数
     * @param body 请求体
     * @param queryParams 查询参数
     * @returns 后端响应
     */
    async function requestProtected<T>(
        contract: ApiContract,
        pathParams: Record<string, string | number | boolean> = {},
        body?: unknown,
        queryParams: Record<string, QueryValue | QueryValue[]> = {}
    ) {
        try {
            return await apiClient.request<T>(contract, pathParams, body, queryParams);
        } catch (error) {
            if (isUnauthorizedError(error)) {
                clearAuthSession();
                message.error("登录状态已失效，请重新登录");
            }
            throw error;
        }
    }

    /**
     * 清理本地登录态。
     */
    function clearAuthSession() {
        setAuthSession(undefined);
        setPasswordRows([]);
        setCategoryRows([]);
        setCategoryTypeRows([]);
        setPasswordTotal(0);
        setCategoryTotal(0);
        setCategoryTypeTotal(0);
        setProfileRows([]);
        removeStoredAuthSession();
    }

    /**
     * 提交密码登录。
     *
     * @returns 登录请求状态
     */
    async function submitPasswordLogin() {
        const values = await loginForm.validateFields();
        const body: PasswordLoginRequest = {
            principal: values.principal,
            password: values.password,
            clientId: values.clientId,
            clientSecret: values.clientSecret,
            deviceName: values.deviceName,
            deviceFingerprint: values.deviceFingerprint
        };
        const preview = previewPasswordLogin(gatewayBaseUrl, body);
        setAuthLoading(true);
        setAuthError("");
        try {
            const result = await requestJson<ApiResult<TokenPairResponse>>(preview);
            assertApiSuccess(result);
            const tokenPair = extractData<TokenPairResponse>(result);
            if (tokenPair.accessToken === undefined || tokenPair.accessToken.length === 0) {
                throw new Error("登录响应未返回 accessToken");
            }
            const session = buildAuthSession(tokenPair, values.principal);
            setAuthSession(session);
            message.success("登录成功");
            loginForm.setFieldValue("password", "");
        } catch (error) {
            const errorMessage = toErrorMessage(error);
            setAuthError(errorMessage);
            loginForm.setFieldValue("password", "");
        } finally {
            setAuthLoading(false);
        }
    }

    /**
     * 提交验证码登录。
     *
     * @returns 登录请求状态
     */
    async function submitVerifyCodeLogin() {
        const values = await verifyCodeLoginForm.validateFields();
        const body: VerifyCodeLoginRequest = {
            principal: values.principal,
            verifyCode: values.verifyCode,
            clientId: values.clientId,
            deviceName: values.deviceName,
            deviceFingerprint: values.deviceFingerprint
        };
        const contractKey = values.channel === "sms" ? "auth-login-sms-code" : "auth-login-email-code";
        const preview = buildRequestPreview(gatewayBaseUrl, getContract(contractKey), {}, body);
        setAuthLoading(true);
        setAuthError("");
        try {
            const result = await requestJson<ApiResult<TokenPairResponse>>(preview);
            assertApiSuccess(result);
            const tokenPair = extractData<TokenPairResponse>(result);
            if (tokenPair.accessToken === undefined || tokenPair.accessToken.length === 0) {
                throw new Error("验证码登录响应未返回 accessToken");
            }
            const session = buildAuthSession(tokenPair, values.principal);
            setAuthSession(session);
            message.success("登录成功");
            verifyCodeLoginForm.setFieldValue("verifyCode", "");
        } catch (error) {
            setAuthError(toErrorMessage(error));
            verifyCodeLoginForm.setFieldValue("verifyCode", "");
        } finally {
            setAuthLoading(false);
        }
    }

    /**
     * 提交账号注册。
     *
     * @returns 注册请求状态
     */
    async function submitRegister() {
        const values = await registerForm.validateFields();
        if (values.password !== values.confirmPassword) {
            setAuthError("两次输入的密码不一致");
            return;
        }
        const preview = buildRequestPreview(gatewayBaseUrl, getContract("account-register"), {}, {
            username: values.username,
            email: values.email,
            phone: values.phone,
            password: values.password
        });
        setAuthLoading(true);
        setAuthError("");
        try {
            const result = await requestJson<ApiResult<AccountResponse>>(preview);
            assertApiSuccess(result);
            message.success("注册已提交，请使用账号登录");
            setAuthView("login");
        } catch (error) {
            setAuthError(toErrorMessage(error));
        } finally {
            setAuthLoading(false);
        }
    }

    /**
     * 提交密码恢复。
     *
     * @returns 密码恢复请求状态
     */
    async function submitPasswordRecovery() {
        const values = await recoveryForm.validateFields();
        const preview = buildRequestPreview(gatewayBaseUrl, getContract("auth-password-recovery"), {}, values);
        setAuthLoading(true);
        setAuthError("");
        try {
            const result = await requestJson<ApiResult<string>>(preview);
            assertApiSuccess(result);
            message.success("密码恢复请求已提交，请根据后端返回继续重置");
            setAuthError(extractData<string>(result) ?? "");
        } catch (error) {
            setAuthError(toErrorMessage(error));
        } finally {
            setAuthLoading(false);
        }
    }

    /**
     * 提交密码重置。
     *
     * @returns 密码重置请求状态
     */
    async function submitPasswordReset() {
        const values = await resetPasswordForm.validateFields();
        const preview = buildRequestPreview(gatewayBaseUrl, getContract("auth-password-reset"), {}, values);
        setAuthLoading(true);
        setAuthError("");
        try {
            const result = await requestJson<ApiResult<boolean>>(preview);
            assertApiSuccess(result);
            message.success("密码已重置，请重新登录");
            setAuthView("login");
        } catch (error) {
            setAuthError(toErrorMessage(error));
        } finally {
            setAuthLoading(false);
        }
    }

    /**
     * 退出当前会话。
     *
     * @returns 退出请求状态
     */
    async function logoutCurrentSession() {
        if (authSession === undefined) {
            clearAuthSession();
            return;
        }
        setAuthLoading(true);
        try {
            const result = await requestProtected<ApiResult<boolean>>(getContract("auth-logout-current"), {}, {
                accessToken: authSession.accessToken,
                refreshToken: authSession.refreshToken
            });
            assertApiSuccess(result);
        } catch (error) {
            if (!isUnauthorizedError(error)) {
                message.warning(`退出接口返回异常：${toErrorMessage(error)}`);
            }
        } finally {
            clearAuthSession();
            setAuthLoading(false);
        }
    }

    /**
     * 查询账号密码列表。
     *
     * @param pageNum 页码
     * @param pageSize 页大小
     * @returns 请求完成状态
     */
    async function refreshPasswordList(pageNum = passwordPage, pageSize = passwordPageSize) {
        const authorizationHeaders = authSession?.accessToken ? {Authorization: `Bearer ${authSession.accessToken}`} : undefined;
        const preview = {
            ...previewPasswordList(gatewayBaseUrl, pageNum, pageSize),
            headers: authorizationHeaders
        };
        setLoadingPassword(true);
        try {
            const result = await requestJson<PageApiResult<unknown>>(preview);
            assertApiSuccess(result);
            const rows = pickList<PasswordView>(result).map((item, index) => toPasswordRecord(item, index));
            setPasswordRows(rows);
            setPasswordTotal(readTotal(result, rows.length));
            setPasswordPage(Number(result.pageNum ?? result.current ?? pageNum));
            setPasswordPageSize(Number(result.pageSize ?? result.size ?? pageSize));
            setPasswordListError("");
        } catch (error) {
            if (isUnauthorizedError(error)) {
                clearAuthSession();
            }
            const errorMessage = toErrorMessage(error);
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
        setLoadingCategory(true);
        try {
            const result = await requestProtected<PageApiResult<unknown>>(getContract("category-list"));
            assertApiSuccess(result);
            const rows = pickList<Category>(result).map((item, index) => toCategoryRecord(item, index));
            setCategoryRows(rows);
            setCategoryTotal(readTotal(result, rows.length));
            setCategoryListError("");
        } catch (error) {
            setCategoryRows([]);
            setCategoryTotal(0);
            setCategoryListError(toErrorMessage(error));
        } finally {
            setLoadingCategory(false);
        }
    }

    /**
     * 查询分类类型列表。
     *
     * @returns 请求完成状态
     */
    async function refreshCategoryTypeList() {
        setLoadingCategoryType(true);
        try {
            const result = await requestProtected<PageApiResult<unknown>>(getContract("category-type-list"));
            assertApiSuccess(result);
            const rows = pickList<CategoryType>(result).map((item, index) => toCategoryTypeRecord(item, index));
            setCategoryTypeRows(rows);
            setCategoryTypeTotal(readTotal(result, rows.length));
            setCategoryTypeListError("");
        } catch (error) {
            setCategoryTypeRows([]);
            setCategoryTypeTotal(0);
            setCategoryTypeListError(toErrorMessage(error));
        } finally {
            setLoadingCategoryType(false);
        }
    }

    /**
     * 打开账号详情弹窗。
     *
     * @param record 当前账号记录
     * @returns 详情请求状态
     */
    async function openPasswordDetail(record: PasswordRecord) {
        setLoadingPassword(true);
        try {
            const result = await requestProtected<ApiResult<PasswordView>>(getContract("password-detail"), {id: record.id});
            assertApiSuccess(result);
            openPasswordModal("edit", toPasswordRecord(extractData<PasswordView>(result), 0));
        } catch (error) {
            message.error(`账号详情加载失败：${toErrorMessage(error)}`);
        } finally {
            setLoadingPassword(false);
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
        setPasswordStrength(undefined);
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
     * 打开分类类型编辑弹窗。
     *
     * @param record 当前分类类型记录
     */
    function openCategoryTypeModal(record?: CategoryTypeRecord) {
        setEditingCategoryType(record);
        categoryTypeForm.setFieldsValue(
            record ?? {
                typeName: "",
                description: ""
            }
        );
        setCategoryTypeModalOpen(true);
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
        setSaving(true);
        try {
            assertApiSuccess(await requestProtected<ApiResult<unknown>>(getContract(isCreate ? "password-add" : "password-update"), {}, body));
            message.success(isCreate ? "账号已新增" : "账号已更新");
            setPasswordModalOpen(false);
            await refreshPasswordList(isCreate ? 1 : passwordPage, passwordPageSize);
        } catch (error) {
            message.error(`保存失败：${toErrorMessage(error)}`);
        } finally {
            setSaving(false);
        }
    }

    /**
     * 生成随机密码并检查强度。
     *
     * @returns 请求完成状态
     */
    async function generatePassword() {
        setSaving(true);
        try {
            const result = await requestProtected<ApiResult<string>>(getContract("password-generate"), {passwordLength: 16});
            assertApiSuccess(result);
            const generatedPassword = String(extractData<string>(result) ?? "");
            passwordForm.setFieldValue("password", generatedPassword);
            await checkPasswordStrength(generatedPassword);
        } catch (error) {
            message.error(`随机密码生成失败：${toErrorMessage(error)}`);
        } finally {
            setSaving(false);
        }
    }

    /**
     * 检查密码强度。
     *
     * @param password 待检查密码
     * @returns 请求完成状态
     */
    async function checkPasswordStrength(password: string) {
        if (password.trim().length === 0) {
            setPasswordStrength(undefined);
            return;
        }
        try {
            const result = await requestProtected<ApiResult<StrengthInfo>>(getContract("password-strength"), {password});
            assertApiSuccess(result);
            setPasswordStrength(extractData<StrengthInfo>(result));
        } catch (error) {
            setPasswordStrength({warning: toErrorMessage(error)});
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
            categoryType: values.typeName ? {typeName: values.typeName} : undefined
        };
        setSaving(true);
        try {
            assertApiSuccess(await requestProtected<ApiResult<unknown>>(getContract(editingCategory ? "category-update" : "category-add"), {}, body));
            message.success(editingCategory ? "分类已更新" : "分类已新增");
            setCategoryModalOpen(false);
            await refreshCategoryList();
        } catch (error) {
            message.error(`保存失败：${toErrorMessage(error)}`);
        } finally {
            setSaving(false);
        }
    }

    /**
     * 保存分类类型记录。
     *
     * @returns 请求完成状态
     */
    async function saveCategoryType() {
        const values = await categoryTypeForm.validateFields();
        const body = {
            ...editingCategoryType,
            ...values
        };
        setSaving(true);
        try {
            assertApiSuccess(await requestProtected<ApiResult<unknown>>(getContract(editingCategoryType ? "category-type-update" : "category-type-add"), {}, body));
            message.success(editingCategoryType ? "分类类型已更新" : "分类类型已新增");
            setCategoryTypeModalOpen(false);
            await refreshCategoryTypeList();
            await refreshCategoryList();
        } catch (error) {
            message.error(`保存失败：${toErrorMessage(error)}`);
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
        setLoadingPassword(true);
        try {
            assertApiSuccess(await requestProtected<ApiResult<unknown>>(getContract("password-delete"), {}, idList));
            message.success("账号已删除");
            setSelectedPasswordKeys([]);
            const targetPage = filteredPasswordRows.length === idList.length && passwordPage > 1 ? passwordPage - 1 : passwordPage;
            await refreshPasswordList(targetPage, passwordPageSize);
        } catch (error) {
            message.error(`删除失败：${toErrorMessage(error)}`);
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
        setLoadingCategory(true);
        try {
            assertApiSuccess(await requestProtected<ApiResult<unknown>>(getContract("category-delete"), {id}));
            message.success("分类已删除");
            await refreshCategoryList();
        } catch (error) {
            message.error(`删除失败：${toErrorMessage(error)}`);
        } finally {
            setLoadingCategory(false);
        }
    }

    /**
     * 删除分类类型记录。
     *
     * @param id 待删除分类类型 ID
     * @returns 请求完成状态
     */
    async function deleteCategoryType(id: number) {
        setLoadingCategoryType(true);
        try {
            assertApiSuccess(await requestProtected<ApiResult<unknown>>(getContract("category-type-delete"), {id}));
            message.success("分类类型已删除");
            await refreshCategoryTypeList();
            await refreshCategoryList();
        } catch (error) {
            message.error(`删除失败：${toErrorMessage(error)}`);
        } finally {
            setLoadingCategoryType(false);
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

    /**
     * 重置当前模块筛选条件。
     */
    function resetCurrentFilter() {
        setKeyword("");
        setCategoryFilter(undefined);
    }

    /**
     * 查询 Profile 列表。
     *
     * @returns 请求完成状态
     */
    async function refreshProfileList() {
        const accountId = profileAccountId.trim();
        if (accountId.length === 0) {
            setProfileRows([]);
            setProfileError("当前 OpenAPI 未提供 me 接口，无法稳定获取当前账号 ID");
            return;
        }
        setLoadingProfile(true);
        setProfileError("");
        try {
            const result = await requestProtected<ApiResult<ProfileResponse[]>>(getContract("profile-list-by-account"), {accountId});
            assertApiSuccess(result);
            setProfileRows(asArray<ProfileResponse>(extractData<ProfileResponse[] | ProfileResponse>(result)));
        } catch (error) {
            setProfileRows([]);
            setProfileError(toErrorMessage(error));
        } finally {
            setLoadingProfile(false);
        }
    }

    /**
     * 保存 Profile。
     *
     * @returns 请求完成状态
     */
    async function saveProfile() {
        const values = await profileForm.validateFields();
        const body = {
            ...values,
            accountId: values.accountId ?? profileAccountId
        };
        if (!body.accountId || body.accountId.trim().length === 0) {
            setProfileError("缺少 accountId，无法保存 Profile");
            return;
        }
        setLoadingProfile(true);
        try {
            assertApiSuccess(await requestProtected<ApiResult<ProfileResponse>>(getContract(values.profileId ? "profile-update" : "profile-add"), {}, body));
            message.success(values.profileId ? "Profile 已更新" : "Profile 已创建");
            await refreshProfileList();
        } catch (error) {
            setProfileError(toErrorMessage(error));
        } finally {
            setLoadingProfile(false);
        }
    }

    /**
     * 删除 Profile。
     *
     * @param profileId Profile ID
     * @returns 请求完成状态
     */
    async function deleteProfile(profileId?: string) {
        if (profileId === undefined || profileId.length === 0) {
            return;
        }
        setLoadingProfile(true);
        try {
            assertApiSuccess(await requestProtected<ApiResult<boolean>>(getContract("profile-delete"), {profileId}));
            message.success("Profile 已删除");
            await refreshProfileList();
        } catch (error) {
            setProfileError(toErrorMessage(error));
        } finally {
            setLoadingProfile(false);
        }
    }

    /**
     * 调用 AI 图片理解接口。
     *
     * @returns 请求完成状态
     */
    async function submitAiImage() {
        const values = await aiImageForm.validateFields();
        setAiLoading(true);
        setAiError("");
        setAiResult("");
        try {
            const contract = getContract(`qwen-image-message-${values.method.toLowerCase()}`);
            const result = await requestProtected<ApiResult<string> | string>(contract, {}, undefined, {file: values.file});
            assertApiSuccess(result as ApiResult<unknown>);
            setAiResult(String(extractData<string>(result) ?? result ?? ""));
        } catch (error) {
            const errorMessage = toErrorMessage(error);
            setAiError(isNotFoundError(error) ? `AI 接口路径需联调确认：${errorMessage}` : errorMessage);
        } finally {
            setAiLoading(false);
        }
    }

    /**
     * 提交授权决策。
     *
     * @returns 请求完成状态
     */
    async function submitAuthorizationDecision() {
        const values = await authorizationForm.validateFields();
        setAccessLoading(true);
        setAccessError("");
        try {
            const result = await requestProtected<ApiResult<AuthorizationDecisionResponse>>(getContract("authorization-decide"), {}, {
                ...values,
                accessToken: values.accessToken ?? authSession?.accessToken
            });
            assertApiSuccess(result);
            setAuthorizationResult(extractData<AuthorizationDecisionResponse>(result));
        } catch (error) {
            setAccessError(toErrorMessage(error));
        } finally {
            setAccessLoading(false);
        }
    }

    /**
     * 查询账号权限。
     *
     * @returns 请求完成状态
     */
    async function queryAccountPermissions() {
        const values = await accountPermissionForm.validateFields();
        setAccessLoading(true);
        setAccessError("");
        try {
            const result = await requestProtected<ApiResult<UserPermissionResponse>>(
                getContract("rbac-account-permissions"),
                {accountId: values.accountId},
                undefined,
                {resourceType: values.resourceType}
            );
            assertApiSuccess(result);
            setPermissionResult(extractData<UserPermissionResponse>(result));
        } catch (error) {
            setAccessError(toErrorMessage(error));
        } finally {
            setAccessLoading(false);
        }
    }

    /**
     * 新增或更新 RBAC 角色。
     *
     * @returns 请求完成状态
     */
    async function upsertRbacRole() {
        const values = await rbacRoleForm.validateFields();
        await submitRbacWrite("rbac-role-upsert", values, "角色已提交");
    }

    /**
     * 新增或更新 RBAC 资源。
     *
     * @returns 请求完成状态
     */
    async function upsertRbacResource() {
        const values = await rbacResourceForm.validateFields();
        await submitRbacWrite("rbac-resource-upsert", values, "资源已提交");
    }

    /**
     * 绑定账号角色。
     *
     * @returns 请求完成状态
     */
    async function bindAccountRole() {
        const values = await rbacBindForm.validateFields();
        await submitRbacWrite("rbac-bind-account-role", values, "账号角色绑定已提交");
    }

    /**
     * 绑定角色资源。
     *
     * @returns 请求完成状态
     */
    async function bindRoleResource() {
        const values = await rbacBindForm.validateFields();
        await submitRbacWrite("rbac-bind-role-resource", values, "角色资源绑定已提交");
    }

    /**
     * 提交 RBAC 写接口。
     *
     * @param contractKey 接口契约 key
     * @param body 请求体
     * @param successMessage 成功提示
     * @returns 请求完成状态
     */
    async function submitRbacWrite(contractKey: string, body: unknown, successMessage: string) {
        setAccessLoading(true);
        setAccessError("");
        try {
            const result = await requestProtected<ApiResult<unknown>>(getContract(contractKey), {}, body);
            assertApiSuccess(result);
            setRbacResult(extractData<unknown>(result));
            message.success(successMessage);
        } catch (error) {
            setAccessError(toErrorMessage(error));
        } finally {
            setAccessLoading(false);
        }
    }

    /**
     * 查询 OAuth Client 列表。
     *
     * @returns 请求完成状态
     */
    async function refreshOAuthClients() {
        setAccessLoading(true);
        setAccessError("");
        try {
            const result = await requestProtected<ApiResult<OAuthClientResponse[]>>(getContract("oauth-client-list"));
            assertApiSuccess(result);
            setOauthRows(asArray<OAuthClientResponse>(extractData<OAuthClientResponse[] | OAuthClientResponse>(result)));
        } catch (error) {
            setOauthRows([]);
            setAccessError(toErrorMessage(error));
        } finally {
            setAccessLoading(false);
        }
    }

    /**
     * 新增 OAuth Client。
     *
     * @returns 请求完成状态
     */
    async function createOAuthClient() {
        const values = await oauthClientForm.validateFields();
        const body = {
            ...values,
            redirectUris: splitCommaValues(values.redirectUris),
            scopes: splitCommaValues(values.scopes),
            grantTypes: splitCommaValues(values.grantTypes)
        };
        setAccessLoading(true);
        setAccessError("");
        try {
            const result = await requestProtected<ApiResult<OAuthClientResponse>>(getContract("oauth-client-add"), {}, body);
            assertApiSuccess(result);
            setOauthResult(extractData<OAuthClientResponse>(result));
            message.success("OAuth Client 已新增");
            await refreshOAuthClients();
        } catch (error) {
            setAccessError(toErrorMessage(error));
        } finally {
            setAccessLoading(false);
        }
    }

    /**
     * 查询 OAuth Client 详情。
     *
     * @returns 请求完成状态
     */
    async function queryOAuthClientDetail() {
        const values = await oauthClientForm.validateFields(["clientId"]);
        if (values.clientId === undefined || values.clientId.trim().length === 0) {
            return;
        }
        setAccessLoading(true);
        setAccessError("");
        try {
            const result = await requestProtected<ApiResult<OAuthClientResponse>>(getContract("oauth-client-detail"), {clientId: values.clientId});
            assertApiSuccess(result);
            setOauthResult(extractData<OAuthClientResponse>(result));
        } catch (error) {
            setAccessError(toErrorMessage(error));
        } finally {
            setAccessLoading(false);
        }
    }

    /**
     * 手动删除会话。
     *
     * @returns 请求完成状态
     */
    async function deleteManualSession() {
        const values = await manualDeleteForm.validateFields(["sessionId"]);
        if (values.sessionId === undefined || values.sessionId.trim().length === 0) {
            return;
        }
        await submitManualDelete("session-delete", {sessionId: values.sessionId}, "会话删除请求已提交");
    }

    /**
     * 手动删除设备。
     *
     * @returns 请求完成状态
     */
    async function deleteManualDevice() {
        const values = await manualDeleteForm.validateFields(["deviceId"]);
        if (values.deviceId === undefined || values.deviceId.trim().length === 0) {
            return;
        }
        await submitManualDelete("device-delete", {deviceId: values.deviceId}, "设备删除请求已提交");
    }

    /**
     * 提交手动删除接口。
     *
     * @param contractKey 接口契约 key
     * @param pathParams 路径参数
     * @param successMessage 成功提示
     * @returns 请求完成状态
     */
    async function submitManualDelete(contractKey: string, pathParams: Record<string, string>, successMessage: string) {
        setDeviceLoading(true);
        setDeviceError("");
        setDeviceResult("");
        try {
            const result = await requestProtected<ApiResult<boolean>>(getContract(contractKey), pathParams);
            assertApiSuccess(result);
            setDeviceResult(successMessage);
        } catch (error) {
            setDeviceError(toErrorMessage(error));
        } finally {
            setDeviceLoading(false);
        }
    }

    /**
     * 渲染认证入口。
     *
     * @returns 认证页面
     */
    function renderAuthPage() {
        const authTabs = [
            {key: "login", label: "密码登录"},
            {key: "codeLogin", label: "验证码登录"},
            {key: "register", label: "注册"},
            {key: "recover", label: "找回密码"},
            {key: "reset", label: "重置密码"}
        ];
        return (
            <div className="auth-shell">
                <Card className="auth-card">
                    <div className="auth-brand">
                        <img src={appIconUrl} alt="FamilyAiButler"/>
                        <div>
                            <Title level={3}>FamilyAiButler</Title>
                            <Text type="secondary">登录后访问受保护业务接口</Text>
                        </div>
                    </div>
                    <Tabs activeKey={authView} onChange={(key) => setAuthView(key as AuthView)} items={authTabs}/>
                    {authError.length > 0 ? (
                        <Alert className="auth-alert" type={authError.includes("Request failed") ? "error" : "info"}
                               showIcon message={authError} role="alert"/>
                    ) : null}
                    {authView === "login" ? renderLoginForm() : null}
                    {authView === "codeLogin" ? renderVerifyCodeLoginForm() : null}
                    {authView === "register" ? renderRegisterForm() : null}
                    {authView === "recover" ? renderRecoveryForm() : null}
                    {authView === "reset" ? renderResetPasswordForm() : null}
                    <Text type="secondary" className="auth-gateway-text">
                        后端网关：{gatewayBaseUrl}
                    </Text>
                </Card>
            </div>
        );
    }

    /**
     * 渲染登录表单。
     *
     * @returns 登录表单
     */
    function renderLoginForm() {
        return (
            <Form form={loginForm} layout="vertical" onFinish={submitPasswordLogin}>
                <Form.Item name="principal" label="账号" rules={[{required: true, message: "请输入账号"}]}>
                    <Input autoComplete="username" placeholder="用户名、邮箱或手机号"/>
                </Form.Item>
                <Form.Item name="password" label="密码" rules={[{required: true, message: "请输入密码"}]}>
                    <Input.Password autoComplete="current-password" placeholder="请输入密码"/>
                </Form.Item>
                <div className="auth-advanced-grid">
                    <Form.Item name="clientId" label="Client ID">
                        <Input placeholder="可选"/>
                    </Form.Item>
                    <Form.Item name="deviceName" label="设备名称">
                        <Input placeholder="可选"/>
                    </Form.Item>
                    <Form.Item name="deviceFingerprint" label="设备指纹">
                        <Input placeholder="可选"/>
                    </Form.Item>
                    <Form.Item name="clientSecret" label="Client Secret">
                        <Input.Password placeholder="可选"/>
                    </Form.Item>
                </div>
                <Button block type="primary" htmlType="submit" loading={authLoading}>
                    登录
                </Button>
            </Form>
        );
    }

    /**
     * 渲染验证码登录表单。
     *
     * @returns 验证码登录表单
     */
    function renderVerifyCodeLoginForm() {
        return (
            <Form
                form={verifyCodeLoginForm}
                layout="vertical"
                initialValues={{channel: "email"}}
                onFinish={submitVerifyCodeLogin}
            >
                <Form.Item name="channel" label="验证码方式" rules={[{required: true, message: "请选择验证码方式"}]}>
                    <Select
                        options={[
                            {label: "邮箱验证码", value: "email"},
                            {label: "短信验证码", value: "sms"}
                        ]}
                    />
                </Form.Item>
                <Form.Item name="principal" label="账号" rules={[{required: true, message: "请输入账号"}]}>
                    <Input autoComplete="username" placeholder="邮箱或手机号"/>
                </Form.Item>
                <Form.Item label="验证码" required>
                    <Space.Compact className="full-compact">
                        <Form.Item name="verifyCode" noStyle rules={[{required: true, message: "请输入验证码"}]}>
                            <Input placeholder="请输入已获取的验证码"/>
                        </Form.Item>
                        <Button disabled>获取验证码</Button>
                    </Space.Compact>
                </Form.Item>
                <Alert
                    className="auth-tip"
                    type="warning"
                    showIcon
                    message="后端暂未提供验证码发送接口，获取验证码按钮已禁用；提交时仅调用真实验证码登录接口。"
                    role="alert"
                />
                <div className="auth-advanced-grid">
                    <Form.Item name="clientId" label="Client ID">
                        <Input placeholder="可选"/>
                    </Form.Item>
                    <Form.Item name="deviceName" label="设备名称">
                        <Input placeholder="可选"/>
                    </Form.Item>
                    <Form.Item name="deviceFingerprint" label="设备指纹">
                        <Input placeholder="可选"/>
                    </Form.Item>
                </div>
                <Button block type="primary" htmlType="submit" loading={authLoading}>
                    验证码登录
                </Button>
            </Form>
        );
    }

    /**
     * 渲染注册表单。
     *
     * @returns 注册表单
     */
    function renderRegisterForm() {
        return (
            <Form form={registerForm} layout="vertical" onFinish={submitRegister}>
                <Form.Item name="username" label="用户名">
                    <Input autoComplete="username" placeholder="可选"/>
                </Form.Item>
                <Form.Item name="email" label="邮箱">
                    <Input autoComplete="email" placeholder="可选"/>
                </Form.Item>
                <Form.Item name="phone" label="手机号">
                    <Input autoComplete="tel" placeholder="可选"/>
                </Form.Item>
                <Form.Item name="password" label="密码" rules={[{required: true, message: "请输入密码"}]}>
                    <Input.Password autoComplete="new-password"/>
                </Form.Item>
                <Form.Item name="confirmPassword" label="确认密码"
                           rules={[{required: true, message: "请再次输入密码"}]}>
                    <Input.Password autoComplete="new-password"/>
                </Form.Item>
                <Button block type="primary" htmlType="submit" loading={authLoading}>
                    注册
                </Button>
            </Form>
        );
    }

    /**
     * 渲染密码恢复表单。
     *
     * @returns 密码恢复表单
     */
    function renderRecoveryForm() {
        return (
            <Form form={recoveryForm} layout="vertical" onFinish={submitPasswordRecovery}>
                <Form.Item name="principal" label="账号" rules={[{required: true, message: "请输入账号"}]}>
                    <Input placeholder="用户名、邮箱或手机号"/>
                </Form.Item>
                <Form.Item name="channel" label="恢复渠道">
                    <Select
                        allowClear
                        placeholder="可选"
                        options={[
                            {label: "邮箱", value: "email"},
                            {label: "短信", value: "sms"}
                        ]}
                    />
                </Form.Item>
                <Button block type="primary" htmlType="submit" loading={authLoading}>
                    发起恢复
                </Button>
            </Form>
        );
    }

    /**
     * 渲染密码重置表单。
     *
     * @returns 密码重置表单
     */
    function renderResetPasswordForm() {
        return (
            <Form form={resetPasswordForm} layout="vertical" onFinish={submitPasswordReset}>
                <Form.Item name="principal" label="账号" rules={[{required: true, message: "请输入账号"}]}>
                    <Input placeholder="用户名、邮箱或手机号"/>
                </Form.Item>
                <Form.Item name="verificationCode" label="验证码" rules={[{required: true, message: "请输入验证码"}]}>
                    <Input placeholder="请输入后端校验所需验证码"/>
                </Form.Item>
                <Form.Item name="newPassword" label="新密码" rules={[{required: true, message: "请输入新密码"}]}>
                    <Input.Password autoComplete="new-password"/>
                </Form.Item>
                <Button block type="primary" htmlType="submit" loading={authLoading}>
                    重置密码
                </Button>
            </Form>
        );
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
                            loading={activeSection === "password" ? loadingPassword : loadingCategory || loadingCategoryType}
                            onClick={() => {
                                if (activeSection === "password") {
                                    void refreshPasswordList(1, passwordPageSize);
                                } else if (categoryTab === "category") {
                                    void refreshCategoryList();
                                } else {
                                    void refreshCategoryTypeList();
                                }
                            }}
                        >
                            查询
                        </Button>
                        <Button onClick={resetCurrentFilter}>重置</Button>
                        <Button type="primary"
                                onClick={() => (activeSection === "password" ? openPasswordModal("create") : categoryTab === "category" ? openCategoryModal() : openCategoryTypeModal())}>
                            {activeSection === "password" ? "新增账号" : categoryTab === "category" ? "新增分类" : "新增类型"}
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
                            type={passwordListError.includes("401") ? "warning" : "error"}
                            showIcon
                            message={passwordListError.includes("401") ? "401 未认证" : "账号列表接口返回异常"}
                            description={<Text className="api-error-detail">{passwordListError}</Text>}
                        />
                    ) : null}
                    {renderPasswordMobileList()}
                    <Table<PasswordRecord>
                        className="responsive-table"
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
     * 渲染移动端密码卡片列表。
     *
     * @returns 移动端密码卡片列表
     */
    function renderPasswordMobileList() {
        if (loadingPassword) {
            return <div className="mobile-card-list"><Card loading className="mobile-list-card"/></div>;
        }
        if (filteredPasswordRows.length === 0) {
            return (
                <div className="mobile-card-list">
                    <Empty description="后端暂无账号数据"/>
                </div>
            );
        }
        return (
            <div className="mobile-card-list">
                {filteredPasswordRows.map((record) => (
                    <Card key={record.id} className="mobile-list-card">
                        <div className="mobile-card-main">
                            <div>
                                <Text strong>{record.name}</Text>
                                <div className="mobile-card-sub">{record.accountNumber}</div>
                            </div>
                            <Tag className={`risk-tag ${record.riskLevel}`}>{getRiskText(record.riskLevel)}</Tag>
                        </div>
                        <div className="mobile-card-meta">
                            <span>{record.websit || "-"}</span>
                            <span>{record.category || "-"}</span>
                            <span>{record.updatedAt}</span>
                        </div>
                        <Space wrap className="mobile-card-actions">
                            <Button onClick={() => void openPasswordDetail(record)}>查看</Button>
                            <Button onClick={() => openPasswordModal("edit", record)}>编辑</Button>
                            <Popconfirm title="确认删除该账号？" okText="删除" cancelText="取消"
                                        onConfirm={() => deletePasswords([record.id])}>
                                <Button danger>删除</Button>
                            </Popconfirm>
                        </Space>
                    </Card>
                ))}
            </div>
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
                    <Title level={5}>分类管理</Title>
                    <Tabs
                        activeKey={categoryTab}
                        onChange={(key) => setCategoryTab(key as CategoryTab)}
                        items={[
                            {key: "category", label: "分类"},
                            {key: "categoryType", label: "分类类型"}
                        ]}
                        className="inline-tabs"
                    />
                    <Space>
                        <Button type="primary"
                                onClick={() => (categoryTab === "category" ? openCategoryModal() : openCategoryTypeModal())}>
                            {categoryTab === "category" ? "新增分类" : "新增类型"}
                        </Button>
                        <Button loading={categoryTab === "category" ? loadingCategory : loadingCategoryType}
                                onClick={() => categoryTab === "category" ? void refreshCategoryList() : void refreshCategoryTypeList()}>
                            刷新
                        </Button>
                    </Space>
                </div>
                {categoryTab === "category" && categoryListError.length > 0 ? (
                    <Alert className="api-error-alert" type="error" showIcon message="分类列表接口返回异常"
                           description={<Text className="api-error-detail">{categoryListError}</Text>}/>
                ) : null}
                {categoryTab === "categoryType" && categoryTypeListError.length > 0 ? (
                    <Alert className="api-error-alert" type="error" showIcon message="分类类型列表接口返回异常"
                           description={<Text className="api-error-detail">{categoryTypeListError}</Text>}/>
                ) : null}
                {categoryTab === "category" ? (
                    <>
                        {renderCategoryMobileList()}
                        <Table<CategoryRecord>
                            className="responsive-table"
                            rowKey="id"
                            loading={loadingCategory}
                            columns={categoryColumns}
                            dataSource={filteredCategoryRows}
                            locale={{emptyText: <Empty description="后端暂无分类数据"/>}}
                            pagination={{total: categoryTotal, pageSize: 10, showTotal: (total) => `共 ${total} 条`}}
                            scroll={{x: 960}}
                        />
                    </>
                ) : (
                    <>
                        {renderCategoryTypeMobileList()}
                        <Table<CategoryTypeRecord>
                            className="responsive-table"
                            rowKey="id"
                            loading={loadingCategoryType}
                            columns={categoryTypeColumns}
                            dataSource={filteredCategoryTypeRows}
                            locale={{emptyText: <Empty description="后端暂无分类类型数据"/>}}
                            pagination={{
                                total: categoryTypeTotal,
                                pageSize: 10,
                                showTotal: (total) => `共 ${total} 条`
                            }}
                            scroll={{x: 860}}
                        />
                    </>
                )}
            </Card>
        );
    }

    /**
     * 渲染移动端分类卡片列表。
     *
     * @returns 移动端分类卡片列表
     */
    function renderCategoryMobileList() {
        if (loadingCategory) {
            return <div className="mobile-card-list"><Card loading className="mobile-list-card"/></div>;
        }
        if (filteredCategoryRows.length === 0) {
            return (
                <div className="mobile-card-list">
                    <Empty description="后端暂无分类数据"/>
                </div>
            );
        }
        return (
            <div className="mobile-card-list">
                {filteredCategoryRows.map((record) => (
                    <Card key={record.id} className="mobile-list-card">
                        <div className="mobile-card-main">
                            <Text strong>{record.name}</Text>
                            <Tag>{record.typeName}</Tag>
                        </div>
                        <div className="mobile-card-meta">
                            <span>父级 ID：{record.parentId}</span>
                            <span>{record.description || "无说明"}</span>
                            <span>{record.updatedAt}</span>
                        </div>
                        <Space wrap className="mobile-card-actions">
                            <Button onClick={() => openCategoryModal(record)}>编辑</Button>
                            <Popconfirm title="确认删除该分类？" okText="删除" cancelText="取消"
                                        onConfirm={() => deleteCategory(record.id)}>
                                <Button danger>删除</Button>
                            </Popconfirm>
                        </Space>
                    </Card>
                ))}
            </div>
        );
    }

    /**
     * 渲染移动端分类类型卡片列表。
     *
     * @returns 移动端分类类型卡片列表
     */
    function renderCategoryTypeMobileList() {
        if (loadingCategoryType) {
            return <div className="mobile-card-list"><Card loading className="mobile-list-card"/></div>;
        }
        if (filteredCategoryTypeRows.length === 0) {
            return (
                <div className="mobile-card-list">
                    <Empty description="后端暂无分类类型数据"/>
                </div>
            );
        }
        return (
            <div className="mobile-card-list">
                {filteredCategoryTypeRows.map((record) => (
                    <Card key={record.id} className="mobile-list-card">
                        <div className="mobile-card-main">
                            <Text strong>{record.typeName}</Text>
                            <Tag>类型</Tag>
                        </div>
                        <div className="mobile-card-meta">
                            <span>{record.description || "无说明"}</span>
                            <span>{record.updatedAt}</span>
                        </div>
                        <Space wrap className="mobile-card-actions">
                            <Button onClick={() => openCategoryTypeModal(record)}>编辑</Button>
                            <Popconfirm title="确认删除该分类类型？" okText="删除" cancelText="取消"
                                        onConfirm={() => deleteCategoryType(record.id)}>
                                <Button danger>删除</Button>
                            </Popconfirm>
                        </Space>
                    </Card>
                ))}
            </div>
        );
    }

    /**
     * 渲染 AI 图片理解模块。
     *
     * @returns AI 图片理解内容
     */
    function renderAiSection() {
        return (
            <div className="two-column-section">
                <Card className="workspace-card">
                    <Title level={5}>图片参数</Title>
                    <Alert type="info" showIcon className="section-alert"
                           message="当前 OpenAPI 只提供 query file 字符串参数，未提供标准文件上传契约。"/>
                    <Form form={aiImageForm} layout="vertical" initialValues={{method: "POST"}}
                          onFinish={submitAiImage}>
                        <Form.Item name="file" label="file" rules={[{required: true, message: "请输入图片参数字符串"}]}>
                            <Input.TextArea rows={5} placeholder="图片 URL、base64 或后端约定的文件标识"/>
                        </Form.Item>
                        <Form.Item name="method" label="调用方式">
                            <Select
                                options={["POST", "GET", "PUT", "PATCH", "DELETE"].map((method) => ({
                                    label: method,
                                    value: method
                                }))}
                            />
                        </Form.Item>
                        <Button type="primary" htmlType="submit" loading={aiLoading}>
                            识别图片
                        </Button>
                    </Form>
                </Card>
                <Card className="workspace-card result-card">
                    <Title level={5}>后端返回结果</Title>
                    {aiError.length > 0 ? (
                        <Alert type="error" showIcon message="图片理解接口返回异常"
                               description={<Text className="api-error-detail">{aiError}</Text>}/>
                    ) : null}
                    {aiResult.length > 0 ? (
                        <Paragraph copyable className="result-text">{aiResult}</Paragraph>
                    ) : (
                        <Empty description={aiLoading ? "正在识别图片" : "暂无识别结果"}/>
                    )}
                </Card>
            </div>
        );
    }

    /**
     * 渲染个人资料模块。
     *
     * @returns 个人资料内容
     */
    function renderProfileSection() {
        const profileColumns: ColumnsType<ProfileResponse> = [
            {title: "Profile ID", dataIndex: "profileId", width: 180},
            {title: "昵称", dataIndex: "nickname", width: 160},
            {title: "语言", dataIndex: "language", width: 100},
            {title: "地区", dataIndex: "region", width: 100},
            {title: "类型", dataIndex: "profileType", width: 120},
            {
                title: "操作",
                fixed: "right",
                width: 150,
                render: (_value, record) => (
                    <Space>
                        <Button type="link" onClick={() => profileForm.setFieldsValue(record)}>编辑</Button>
                        <Popconfirm title="确认删除该 Profile？" okText="删除" cancelText="取消"
                                    onConfirm={() => deleteProfile(record.profileId)}>
                            <Button type="link" danger>删除</Button>
                        </Popconfirm>
                    </Space>
                )
            }
        ];
        return (
            <div className="two-column-section profile-section">
                <Card className="workspace-card">
                    <Title level={5}>账号 Profile</Title>
                    <Space.Compact className="full-compact">
                        <Input
                            value={profileAccountId}
                            onChange={(event) => setProfileAccountId(event.target.value)}
                            placeholder="accountId"
                        />
                        <Button loading={loadingProfile} onClick={() => void refreshProfileList()}>
                            查询
                        </Button>
                    </Space.Compact>
                    {profileError.length > 0 ? (
                        <Alert className="section-alert" type={profileError.includes("me 接口") ? "warning" : "error"}
                               showIcon message={profileError}/>
                    ) : null}
                    {renderProfileMobileList()}
                    <Table<ProfileResponse>
                        className="compact-table responsive-table"
                        rowKey={(record) => record.profileId ?? `${record.accountId}-${record.nickname}`}
                        loading={loadingProfile}
                        columns={profileColumns}
                        dataSource={profileRows}
                        locale={{emptyText: <Empty description="后端暂无 Profile 数据"/>}}
                        pagination={false}
                        scroll={{x: 760}}
                    />
                </Card>
                <Card className="workspace-card">
                    <Title level={5}>新建或编辑 Profile</Title>
                    <Form form={profileForm} layout="vertical" onFinish={saveProfile}>
                        <Form.Item name="profileId" label="Profile ID">
                            <Input placeholder="编辑时填写"/>
                        </Form.Item>
                        <Form.Item name="accountId" label="Account ID">
                            <Input placeholder={profileAccountId || "缺 me 接口时需手动填写"}/>
                        </Form.Item>
                        <Form.Item name="nickname" label="昵称">
                            <Input/>
                        </Form.Item>
                        <Form.Item name="avatar" label="头像">
                            <Input placeholder="头像 URL 或字符串"/>
                        </Form.Item>
                        <div className="modal-form-grid">
                            <Form.Item name="language" label="语言">
                                <Input/>
                            </Form.Item>
                            <Form.Item name="region" label="地区">
                                <Input/>
                            </Form.Item>
                            <Form.Item name="profileType" label="类型">
                                <Input/>
                            </Form.Item>
                        </div>
                        <Button type="primary" htmlType="submit" loading={loadingProfile}>
                            保存 Profile
                        </Button>
                    </Form>
                </Card>
            </div>
        );
    }

    /**
     * 渲染移动端 Profile 卡片列表。
     *
     * @returns 移动端 Profile 卡片列表
     */
    function renderProfileMobileList() {
        if (loadingProfile) {
            return <div className="mobile-card-list compact-mobile-list"><Card loading className="mobile-list-card"/>
            </div>;
        }
        if (profileRows.length === 0) {
            return (
                <div className="mobile-card-list compact-mobile-list">
                    <Empty description="后端暂无 Profile 数据"/>
                </div>
            );
        }
        return (
            <div className="mobile-card-list compact-mobile-list">
                {profileRows.map((record) => (
                    <Card key={record.profileId ?? `${record.accountId}-${record.nickname}`}
                          className="mobile-list-card">
                        <div className="mobile-card-main">
                            <Text strong>{record.nickname ?? record.profileId ?? "未命名 Profile"}</Text>
                            <Tag>{record.profileType ?? "Profile"}</Tag>
                        </div>
                        <div className="mobile-card-meta">
                            <span>Profile ID：{record.profileId ?? "-"}</span>
                            <span>语言：{record.language ?? "-"}</span>
                            <span>地区：{record.region ?? "-"}</span>
                        </div>
                        <Space wrap className="mobile-card-actions">
                            <Button onClick={() => profileForm.setFieldsValue(record)}>编辑</Button>
                            <Popconfirm title="确认删除该 Profile？" okText="删除" cancelText="取消"
                                        onConfirm={() => deleteProfile(record.profileId)}>
                                <Button danger>删除</Button>
                            </Popconfirm>
                        </Space>
                    </Card>
                ))}
            </div>
        );
    }

    /**
     * 渲染权限与集成模块。
     *
     * @returns 权限与集成内容
     */
    function renderAccessSection() {
        return (
            <Card className="accounts-card">
                <div className="card-title-row">
                    <Title level={5}>权限与集成</Title>
                    <Button loading={accessLoading} onClick={() => void refreshOAuthClients()}>刷新 OAuth
                        Client</Button>
                </div>
                {accessError.length > 0 ? (
                    <Alert className="api-error-alert" type="error" showIcon message="权限或集成接口返回异常"
                           description={<Text className="api-error-detail">{accessError}</Text>}/>
                ) : null}
                <Tabs className="workspace-tabs" items={[
                    {key: "decision", label: "授权决策", children: renderAuthorizationTab()},
                    {key: "permission", label: "账号权限查询", children: renderPermissionTab()},
                    {key: "rbac", label: "RBAC 写入", children: renderRbacTab()},
                    {key: "oauth", label: "OAuth Client", children: renderOAuthTab()}
                ]}/>
            </Card>
        );
    }

    /**
     * 渲染授权决策 Tab。
     *
     * @returns 授权决策内容
     */
    function renderAuthorizationTab() {
        return (
            <div className="two-column-section tab-panel-section">
                <Form form={authorizationForm} layout="vertical" onFinish={submitAuthorizationDecision}>
                    <Form.Item name="accessToken" label="Access Token">
                        <Input.TextArea rows={3} placeholder="默认使用当前登录令牌"/>
                    </Form.Item>
                    <Form.Item name="resourceService" label="resourceService">
                        <Input/>
                    </Form.Item>
                    <Form.Item name="resourcePath" label="resourcePath">
                        <Input/>
                    </Form.Item>
                    <Form.Item name="action" label="action">
                        <Input/>
                    </Form.Item>
                    <Button type="primary" htmlType="submit" loading={accessLoading}>提交决策</Button>
                </Form>
                <ResultPanel title="授权决策结果" value={authorizationResult}/>
            </div>
        );
    }

    /**
     * 渲染账号权限查询 Tab。
     *
     * @returns 账号权限查询内容
     */
    function renderPermissionTab() {
        return (
            <div className="two-column-section tab-panel-section">
                <Form form={accountPermissionForm} layout="vertical" onFinish={queryAccountPermissions}>
                    <Form.Item name="accountId" label="accountId"
                               rules={[{required: true, message: "请输入 accountId"}]}>
                        <Input/>
                    </Form.Item>
                    <Form.Item name="resourceType" label="resourceType">
                        <Input placeholder="可选"/>
                    </Form.Item>
                    <Button type="primary" htmlType="submit" loading={accessLoading}>查询权限</Button>
                </Form>
                <div>
                    <Title level={5}>真实权限返回</Title>
                    {permissionResult === undefined ? (
                        <Empty description="暂无账号权限查询结果"/>
                    ) : (
                        <Descriptions bordered column={1} size="small">
                            <Descriptions.Item label="accountId">{permissionResult.accountId}</Descriptions.Item>
                            <Descriptions.Item
                                label="roles">{renderRoleTags(permissionResult.roles)}</Descriptions.Item>
                            <Descriptions.Item
                                label="resources">{renderResourceTags(permissionResult.resources ?? permissionResult.permissions)}</Descriptions.Item>
                        </Descriptions>
                    )}
                </div>
            </div>
        );
    }

    /**
     * 渲染 RBAC 写入 Tab。
     *
     * @returns RBAC 写入内容
     */
    function renderRbacTab() {
        return (
            <div className="rbac-grid">
                <Alert type="warning" showIcon className="section-alert"
                       message="后端当前缺角色、资源和绑定关系列表接口，本页仅提交真实写接口，不伪造列表回显。"/>
                <Form form={rbacRoleForm} layout="vertical" onFinish={upsertRbacRole}>
                    <Title level={5}>角色</Title>
                    <Form.Item name="roleCode" label="roleCode"
                               rules={[{required: true, message: "请输入 roleCode"}]}><Input/></Form.Item>
                    <Form.Item name="roleName" label="roleName"><Input/></Form.Item>
                    <Form.Item name="description" label="description"><Input/></Form.Item>
                    <Form.Item name="status" label="status"><Input/></Form.Item>
                    <Button htmlType="submit" loading={accessLoading}>提交角色</Button>
                </Form>
                <Form form={rbacResourceForm} layout="vertical" onFinish={upsertRbacResource}>
                    <Title level={5}>资源</Title>
                    <Form.Item name="resourceCode" label="resourceCode"
                               rules={[{required: true, message: "请输入 resourceCode"}]}><Input/></Form.Item>
                    <Form.Item name="resourceName" label="resourceName"><Input/></Form.Item>
                    <Form.Item name="resourceService" label="resourceService"><Input/></Form.Item>
                    <Form.Item name="resourcePath" label="resourcePath"><Input/></Form.Item>
                    <Form.Item name="resourceType" label="resourceType"><Input/></Form.Item>
                    <Form.Item name="action" label="action"><Input/></Form.Item>
                    <Button htmlType="submit" loading={accessLoading}>提交资源</Button>
                </Form>
                <Form form={rbacBindForm} layout="vertical">
                    <Title level={5}>绑定</Title>
                    <Form.Item name="accountId" label="accountId"><Input/></Form.Item>
                    <Form.Item name="roleId" label="roleId"><Input/></Form.Item>
                    <Form.Item name="roleCode" label="roleCode"><Input/></Form.Item>
                    <Form.Item name="resourceId" label="resourceId"><Input/></Form.Item>
                    <Form.Item name="resourceCode" label="resourceCode"><Input/></Form.Item>
                    <Space wrap>
                        <Button onClick={() => void bindAccountRole()} loading={accessLoading}>绑定账号角色</Button>
                        <Button onClick={() => void bindRoleResource()} loading={accessLoading}>绑定角色资源</Button>
                    </Space>
                </Form>
                <ResultPanel title="RBAC 写入返回" value={rbacResult}/>
            </div>
        );
    }

    /**
     * 渲染 OAuth Client Tab。
     *
     * @returns OAuth Client 内容
     */
    function renderOAuthTab() {
        const columns: ColumnsType<OAuthClientResponse> = [
            {title: "clientId", dataIndex: "clientId", width: 180},
            {title: "clientName", dataIndex: "clientName", width: 180},
            {title: "status", dataIndex: "status", width: 120},
            {title: "更新时间", dataIndex: "updateTime", width: 180}
        ];
        return (
            <div className="two-column-section tab-panel-section">
                <div>
                    <Form form={oauthClientForm} layout="vertical" onFinish={createOAuthClient}>
                        <Form.Item name="clientId" label="clientId"
                                   rules={[{required: true, message: "请输入 clientId"}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item name="clientSecret" label="clientSecret">
                            <Input.Password/>
                        </Form.Item>
                        <Form.Item name="clientName" label="clientName">
                            <Input/>
                        </Form.Item>
                        <Form.Item name="redirectUris" label="redirectUris">
                            <Input placeholder="多个值用英文逗号分隔"/>
                        </Form.Item>
                        <Form.Item name="scopes" label="scopes">
                            <Input placeholder="多个值用英文逗号分隔"/>
                        </Form.Item>
                        <Form.Item name="grantTypes" label="grantTypes">
                            <Input placeholder="多个值用英文逗号分隔"/>
                        </Form.Item>
                        <Form.Item name="status" label="status">
                            <Input/>
                        </Form.Item>
                        <Space wrap>
                            <Button type="primary" htmlType="submit" loading={accessLoading}>新增 Client</Button>
                            <Button onClick={() => void queryOAuthClientDetail()}
                                    loading={accessLoading}>查详情</Button>
                        </Space>
                    </Form>
                    <ResultPanel title="OAuth 返回" value={oauthResult}/>
                </div>
                {renderOAuthMobileList()}
                <Table<OAuthClientResponse>
                    className="responsive-table"
                    rowKey={(record) => record.clientId ?? record.clientName ?? ""}
                    loading={accessLoading}
                    columns={columns}
                    dataSource={oauthRows}
                    locale={{emptyText: <Empty description="后端暂无 OAuth Client 数据"/>}}
                    scroll={{x: 720}}
                />
            </div>
        );
    }

    /**
     * 渲染移动端 OAuth Client 卡片列表。
     *
     * @returns 移动端 OAuth Client 卡片列表
     */
    function renderOAuthMobileList() {
        if (accessLoading) {
            return <div className="mobile-card-list"><Card loading className="mobile-list-card"/></div>;
        }
        if (oauthRows.length === 0) {
            return (
                <div className="mobile-card-list">
                    <Empty description="后端暂无 OAuth Client 数据"/>
                </div>
            );
        }
        return (
            <div className="mobile-card-list">
                {oauthRows.map((record) => (
                    <Card key={record.clientId ?? record.clientName} className="mobile-list-card">
                        <div className="mobile-card-main">
                            <Text strong>{record.clientName ?? record.clientId ?? "OAuth Client"}</Text>
                            <Tag>{record.status ?? "status -"}</Tag>
                        </div>
                        <div className="mobile-card-meta">
                            <span>clientId：{record.clientId ?? "-"}</span>
                            <span>更新时间：{record.updateTime ?? "-"}</span>
                        </div>
                    </Card>
                ))}
            </div>
        );
    }

    /**
     * 渲染会话与设备模块。
     *
     * @returns 会话与设备内容
     */
    function renderDeviceSection() {
        return (
            <div className="two-column-section">
                <Card className="workspace-card">
                    <Result
                        status="info"
                        title="当前后端未提供会话/设备列表"
                        subTitle="OpenAPI 仅暴露删除会话和删除设备接口，页面不能展示可管理列表或伪造设备数据。"
                    />
                </Card>
                <Card className="workspace-card">
                    <Title level={5}>手动删除调试</Title>
                    <Form form={manualDeleteForm} layout="vertical">
                        <Form.Item name="sessionId" label="sessionId">
                            <Input placeholder="输入后调用会话删除接口"/>
                        </Form.Item>
                        <Button loading={deviceLoading} onClick={() => void deleteManualSession()}>
                            删除会话
                        </Button>
                        <Form.Item name="deviceId" label="deviceId" className="form-item-spaced">
                            <Input placeholder="输入后调用设备删除接口"/>
                        </Form.Item>
                        <Button loading={deviceLoading} onClick={() => void deleteManualDevice()}>
                            删除设备
                        </Button>
                    </Form>
                    {deviceResult.length > 0 ?
                        <Alert className="section-alert" type="success" showIcon message={deviceResult}/> : null}
                    {deviceError.length > 0 ?
                        <Alert className="section-alert" type="error" showIcon message={deviceError}/> : null}
                </Card>
            </div>
        );
    }

    /**
     * 渲染移动端“我的”二级入口。
     *
     * @returns 我的入口内容
     */
    function renderMySection() {
        const sessionLabel = authSession?.principal ?? authSession?.accountId ?? "已登录";
        const myEntries: NavItem[] = [
            {key: "profile", label: "个人资料", icon: "◎"},
            {key: "access", label: "权限与集成", icon: "▤"},
            {key: "device", label: "会话与设备", icon: "⌁"}
        ];
        return (
            <Card className="accounts-card my-card">
                <div className="card-title-row">
                    <Title level={5}>我的</Title>
                    <Text type="secondary">{sessionLabel}</Text>
                </div>
                <div className="my-entry-list">
                    {myEntries.map((entry) => (
                        <button
                            key={entry.key}
                            type="button"
                            className="my-entry-button"
                            onClick={() => setActiveSection(entry.key)}
                        >
                            <span className="nav-icon">{entry.icon}</span>
                            <span>{entry.label}</span>
                            <span aria-hidden="true">›</span>
                        </button>
                    ))}
                </div>
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
        if (activeSection === "ai") {
            return renderAiSection();
        }
        if (activeSection === "my") {
            return renderMySection();
        }
        if (activeSection === "profile") {
            return renderProfileSection();
        }
        if (activeSection === "access") {
            return renderAccessSection();
        }
        return renderDeviceSection();
    }

    /**
     * 判断移动端主导航是否激活。
     *
     * @param key 移动端导航项
     * @returns 是否激活
     */
    function isMobileNavActive(key: AppSection) {
        if (key === "my") {
            return activeSection === "my" || activeSection === "profile" || activeSection === "access" || activeSection === "device";
        }
        return activeSection === key;
    }

    if (authSession === undefined) {
        return renderAuthPage();
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
                            aria-current={activeSection === item.key ? "page" : undefined}
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
                    <Space className="header-actions">
                        <Tag color="blue">{authSession.principal ?? authSession.accountId ?? "已登录"}</Tag>
                        <Button loading={authLoading} onClick={() => void logoutCurrentSession()}>
                            退出
                        </Button>
                    </Space>
                </Header>
                <div className="mobile-nav-bar" aria-label="模块导航">
                    {mobileNavItems.map((item) => (
                        <button
                            key={item.key}
                            className={isMobileNavActive(item.key) ? "mobile-nav-button active" : "mobile-nav-button"}
                            type="button"
                            aria-current={isMobileNavActive(item.key) ? "page" : undefined}
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
                            <Input.Password placeholder="请输入密码"
                                            onBlur={(event) => void checkPasswordStrength(event.target.value)}/>
                        </Form.Item>
                        <Form.Item label="密码工具">
                            <Button loading={saving} onClick={() => void generatePassword()}>生成随机密码</Button>
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
                    {passwordStrength !== undefined ? (
                        <Alert className="section-alert" type={passwordStrength.warning ? "warning" : "info"} showIcon
                               message={passwordStrength.warning ?? `密码强度分：${passwordStrength.score ?? "-"}`}
                               description={(passwordStrength.suggestions ?? passwordStrength.feedback?.suggestions ?? []).join("；")}/>
                    ) : null}
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
                            <InputNumber min={0} className="full-input"/>
                        </Form.Item>
                    </div>
                    <Form.Item name="description" label="说明">
                        <Input.TextArea rows={3} placeholder="请输入分类说明"/>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title={editingCategoryType ? "编辑分类类型" : "新增分类类型"}
                open={categoryTypeModalOpen}
                confirmLoading={saving}
                onOk={saveCategoryType}
                onCancel={() => setCategoryTypeModalOpen(false)}
                okText="保存"
                cancelText="取消"
                width={520}
            >
                <Form form={categoryTypeForm} layout="vertical">
                    <Form.Item name="typeName" label="类型名称" rules={[{required: true, message: "请输入类型名称"}]}>
                        <Input placeholder="请输入类型名称"/>
                    </Form.Item>
                    <Form.Item name="description" label="说明">
                        <Input.TextArea rows={3} placeholder="请输入类型说明"/>
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
 * 渲染 JSON 结果面板。
 *
 * @param props 面板参数
 * @returns JSON 结果面板
 */
function ResultPanel(props: { title: string; value: unknown }) {
    return (
        <div>
            <Title level={5}>{props.title}</Title>
            {props.value === undefined ? (
                <Empty description="暂无后端返回"/>
            ) : (
                <pre className="json-result">{JSON.stringify(props.value, null, 2)}</pre>
            )}
        </div>
    );
}

/**
 * 根据接口 key 获取契约。
 *
 * @param key 接口契约 key
 * @returns 接口契约
 */
function getContract(key: string): ApiContract {
    return findApiContract(key);
}

/**
 * 校验后端业务响应是否成功。
 *
 * @param result 后端响应
 */
function assertApiSuccess(result: ApiResult<unknown>) {
    if (result === null || typeof result !== "object") {
        return;
    }
    if (result.code !== undefined && result.code !== 10000) {
        throw new Error(result.message ?? result.msg ?? `业务响应失败：${result.code}`);
    }
    if (result.success === false) {
        throw new Error(result.message ?? result.msg ?? "业务响应失败");
    }
}

/**
 * 从后端响应中提取业务 data。
 *
 * @param payload 后端响应
 * @returns 业务 data 或原始响应
 */
function extractData<T>(payload: unknown): T {
    if (payload !== null && typeof payload === "object" && "data" in payload) {
        return (payload as ApiResult<T>).data as T;
    }
    return payload as T;
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
        if (Array.isArray(record.list)) {
            return flattenList<T>(record.list);
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
 * 将响应值转换为数组。
 *
 * @param value 响应值
 * @returns 数组数据
 */
function asArray<T>(value: T | T[] | undefined): T[] {
    if (value === undefined) {
        return [];
    }
    return Array.isArray(value) ? value : [value];
}

/**
 * 读取分页总数。
 *
 * @param payload 后端响应
 * @param fallback 兜底数量
 * @returns 分页总数
 */
function readTotal(payload: PageApiResult<unknown>, fallback: number) {
    const data = payload.data;
    if (data !== null && typeof data === "object") {
        const dataRecord = data as Record<string, unknown>;
        const dataTotal = dataRecord.total ?? dataRecord.totalElements;
        if (dataTotal !== undefined) {
            return Number(dataTotal);
        }
    }
    return Number(payload.total ?? fallback);
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
        name: item.name ?? item.categoryName ?? "-",
        description: item.description ?? "",
        parentId: Number(item.parentId ?? 0),
        typeName: item.categoryType?.typeName ?? "-",
        updatedAt: item.updateTime ?? "-"
    };
}

/**
 * 转换后端分类类型对象为表格记录。
 *
 * @param item 后端分类类型对象
 * @param index 默认序号
 * @returns 表格记录
 */
function toCategoryTypeRecord(item: CategoryType, index: number): CategoryTypeRecord {
    const id = Number(item.id ?? index + 1);
    return {
        ...item,
        key: String(id),
        id,
        typeName: item.typeName ?? "-",
        description: item.description ?? "",
        createdAt: item.createTime ?? "-",
        updatedAt: item.updateTime ?? "-"
    };
}

/**
 * 根据接口错误判断未认证。
 *
 * @param error 错误对象
 * @returns 是否未认证
 */
function isUnauthorizedError(error: unknown) {
    return toErrorMessage(error).includes("401");
}

/**
 * 根据接口错误判断资源不存在。
 *
 * @param error 错误对象
 * @returns 是否资源不存在
 */
function isNotFoundError(error: unknown) {
    return toErrorMessage(error).includes("404");
}

/**
 * 转换错误对象为文案。
 *
 * @param error 错误对象
 * @returns 错误文案
 */
function toErrorMessage(error: unknown) {
    return error instanceof Error ? error.message : String(error);
}

/**
 * 构建登录会话。
 *
 * @param tokenPair 后端令牌响应
 * @param principal 登录账号
 * @returns 登录会话
 */
function buildAuthSession(tokenPair: TokenPairResponse, principal: string): AuthSession {
    const payload = parseJwtPayload(tokenPair.accessToken);
    return {
        accessToken: tokenPair.accessToken ?? "",
        refreshToken: tokenPair.refreshToken,
        tokenType: tokenPair.tokenType,
        expiresIn: tokenPair.expiresIn,
        accountId: readPayloadText(payload, ["accountId", "account_id", "sub", "userId"]),
        principal
    };
}

/**
 * 读取本地登录会话。
 *
 * @returns 本地登录会话
 */
function readStoredAuthSession() {
    if (typeof window === "undefined") {
        return undefined;
    }
    const rawValue = window.localStorage.getItem(AUTH_SESSION_STORAGE_KEY);
    if (rawValue === null) {
        return undefined;
    }
    try {
        const session = JSON.parse(rawValue) as AuthSession;
        return session.accessToken ? session : undefined;
    } catch (_error) {
        return undefined;
    }
}

/**
 * 保存本地登录会话。
 *
 * @param session 登录会话
 */
function persistAuthSession(session: AuthSession) {
    if (typeof window === "undefined") {
        return;
    }
    window.localStorage.setItem(AUTH_SESSION_STORAGE_KEY, JSON.stringify(session));
}

/**
 * 删除本地登录会话。
 */
function removeStoredAuthSession() {
    if (typeof window === "undefined") {
        return;
    }
    window.localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
}

/**
 * 解析 JWT payload。
 *
 * @param token 访问令牌
 * @returns payload 对象
 */
function parseJwtPayload(token?: string): Record<string, unknown> {
    if (token === undefined || token.split(".").length < 2 || typeof window === "undefined") {
        return {};
    }
    try {
        const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
        const decoded = window.atob(payload.padEnd(Math.ceil(payload.length / 4) * 4, "="));
        return JSON.parse(decoded) as Record<string, unknown>;
    } catch (_error) {
        return {};
    }
}

/**
 * 从 JWT payload 中读取字符串字段。
 *
 * @param payload JWT payload
 * @param keys 候选字段名
 * @returns 字符串字段值
 */
function readPayloadText(payload: Record<string, unknown>, keys: string[]) {
    for (const key of keys) {
        const value = payload[key];
        if (typeof value === "string" && value.length > 0) {
            return value;
        }
        if (typeof value === "number") {
            return String(value);
        }
    }
    return undefined;
}

/**
 * 拆分英文逗号分隔值。
 *
 * @param value 原始输入
 * @returns 字符串数组
 */
function splitCommaValues(value?: string) {
    if (value === undefined || value.trim().length === 0) {
        return undefined;
    }
    return value.split(",").map((item) => item.trim()).filter((item) => item.length > 0);
}

/**
 * 渲染角色标签。
 *
 * @param roles 角色集合
 * @returns 角色标签
 */
function renderRoleTags(roles?: RoleResponse[]) {
    if (roles === undefined || roles.length === 0) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="无角色数据"/>;
    }
    return roles.map((role) => <Tag key={role.roleId ?? role.roleCode}>{role.roleName ?? role.roleCode}</Tag>);
}

/**
 * 渲染权限资源标签。
 *
 * @param resources 权限资源集合
 * @returns 权限资源标签
 */
function renderResourceTags(resources?: Array<{ resourceId?: string; resourceCode?: string; resourceName?: string }>) {
    if (resources === undefined || resources.length === 0) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="无资源数据"/>;
    }
    return resources.map((resource) => (
        <Tag key={resource.resourceId ?? resource.resourceCode}>{resource.resourceName ?? resource.resourceCode}</Tag>
    ));
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
