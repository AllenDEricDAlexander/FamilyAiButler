# FamilyAiButler Kubernetes 存储规划

`ops/k8s/base` 中的 Postgres、Redis、Nacos 都通过 StatefulSet 的 `volumeClaimTemplates` 申请 PVC。PVC 默认不写死
`storageClassName`，由集群默认 StorageClass 决定实际存储后端，方便本地、测试集群和 vSphere/vSAN 集群复用同一套基础 YAML。

## 推荐路径

1. 当前未启用 vSAN 时，先用集群已有的默认 StorageClass 验证流程；不要在生产使用 `hostPath`。
2. vSAN 准备好后，在 Rocky Linux VM 自建 K8s 集群里安装 vSphere CSI Driver，并创建 `vsan-retain` StorageClass。
3. 数据库类 PVC 使用 `ReadWriteOnce`，回收策略用 `Retain`，避免误删 StatefulSet 时一起删除真实数据。
4. 需要固定存储类别时，在 prod overlay 里 patch StatefulSet 的 `volumeClaimTemplates[*].spec.storageClassName`，不要直接改
   base。
5. Postgres、Redis、Nacos 默认不接 HPA；扩容需要先设计复制、主从、备份和恢复策略。

## vSphere CSI 样例

样例文件：

```bash
kubectl apply -f ops/k8s/storage/vsphere-csi-storageclass.yaml
```

创建后可以把它设为默认 StorageClass，或者在 prod overlay 中显式引用：

```bash
kubectl patch storageclass vsan-retain -p '{"metadata":{"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

## 容量默认值

| 组件       | PVC 名称模板        | 默认容量 | 访问模式          |
|----------|-----------------|------|---------------|
| Postgres | `postgres-data` | 20Gi | ReadWriteOnce |
| Redis    | `redis-data`    | 5Gi  | ReadWriteOnce |
| Nacos    | `nacos-data`    | 10Gi | ReadWriteOnce |

生产环境上线前还需要单独补齐备份策略、快照策略和恢复演练；PVC 本身只解决持久化，不等于高可用。
