// @BelongsProject: FamilyAiButler
// @BelongsPackage: frontend.apps.desktop.src-tauri.src
// @ClassName: lib
// @Author: atluofu
// @CreateTime: 2026Year-05Month-19Day
// @Description: Tauri 桌面客户端运行逻辑
// @Version: 1.0
#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .run(tauri::generate_context!())
        .expect("error while running FamilyAiButler desktop application");
}
