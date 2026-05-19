/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.apps.web
 * @ClassName: index
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: Expo Web 应用注册入口
 * @Version: 1.0
 */
import { registerRootComponent } from "expo";
import { App } from "@family-ai-butler/app-core";
import "antd/dist/reset.css";
import "./src/styles.css";

registerRootComponent(App);
