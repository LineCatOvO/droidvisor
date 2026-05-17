@echo off
REM 本地环境配置脚本 (Windows)
REM 用于快速设置国内镜像源，不影响仓库原有配置

echo ==========================================
echo    本地开发环境配置工具
echo ==========================================
echo.

echo [1/4] 配置 Gradle 镜像...
if not exist "%USERPROFILE%\.gradle" mkdir "%USERPROFILE%\.gradle"

if exist "init.gradle.kts" (
    copy /Y init.gradle.kts "%USERPROFILE%\.gradle\init.gradle.kts" >nul
    echo    √ Gradle 初始化脚本已配置
) else (
    echo    ⚠ init.gradle.kts 不存在，跳过
)

if exist "gradle.properties.local" (
    if not exist "gradle.properties" (
        copy /Y gradle.properties.local gradle.properties >nul
        echo    √ Gradle 属性配置已创建
    ) else (
        echo    ⚠ gradle.properties 已存在，跳过
    )
)

echo.
echo [2/4] Docker 镜像配置...
echo    ⚠ Windows 请在 Docker Desktop 中手动配置镜像源
echo.

echo [3/4] 备份原始 gradle-wrapper.properties...
set WRAPPER_FILE=gradle\wrapper\gradle-wrapper.properties
if exist "%WRAPPER_FILE%" (
    if not exist "%WRAPPER_FILE%.bak" (
        copy /Y "%WRAPPER_FILE%" "%WRAPPER_FILE%.bak" >nul
        echo    √ 已备份原始配置
    )
)

echo.
echo [4/4] 配置完成！
echo.
echo ==========================================
echo 使用说明：
echo   - Gradle 镜像已配置到 %%USERPROFILE%%\.gradle\init.gradle.kts
echo   - 如需使用 Gradle Wrapper 镜像，请编辑：
echo     %WRAPPER_FILE%
echo.
echo 验证命令：
echo   gradlew.bat tasks --init-script init.gradle.kts
echo.
echo 更多详情请查看 LOCAL_SETUP.md
echo ==========================================
echo.
pause
