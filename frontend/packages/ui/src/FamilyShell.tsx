/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.packages.ui
 * @ClassName: FamilyShell
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: 家庭助手跨端应用壳
 * @Version: 1.0
 */
import type { ReactNode } from "react";
import { StyleSheet, View } from "react-native";

export interface FamilyShellProps {
  children?: ReactNode;
}

/**
 * 家庭助手跨端应用壳。
 *
 * @param props 应用主体内容
 * @returns React Native 跨端页面结构
 */
export function FamilyShell(props: FamilyShellProps) {
  return <View style={styles.page}>{props.children}</View>;
}

const styles = StyleSheet.create({
  page: {
    width: "100%",
    minHeight: "100%",
    backgroundColor: "#eef2f6"
  }
});
