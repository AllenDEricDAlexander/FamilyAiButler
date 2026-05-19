/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.packages.app-core.assets
 * @ClassName: assets
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: 前端静态资源类型声明
 * @Version: 1.0
 */
declare module "*.png" {
  const source: string | { uri: string; width?: number; height?: number };

  export default source;
}
