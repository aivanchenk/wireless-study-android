@echo off
echo ============================================
echo Wireless Map API Integration - Test Runner
echo ============================================
echo.

echo Step 1: Building the project...
echo.
call gradlew.bat clean assembleDebug assembleDebugAndroidTest
if %ERRORLEVEL% NEQ 0 (
    echo Build failed! Please check the errors above.
    pause
    exit /b 1
)

echo.
echo ============================================
echo Build successful!
echo ============================================
echo.
echo Step 2: Running instrumented tests...
echo.
echo Make sure:
echo   1. Your local API service is running on http://localhost:9000
echo   2. An Android emulator or device is connected
echo.
pause

echo.
echo Running tests...
echo.
call gradlew.bat connectedDebugAndroidTest
if %ERRORLEVEL% NEQ 0 (
    echo Tests failed or device not connected!
    echo Check the error messages above.
) else (
    echo.
    echo ============================================
    echo Tests completed!
    echo ============================================
    echo.
    echo Test report available at:
    echo app\build\reports\androidTests\connected\index.html
)

echo.
pause

