@echo off
echo ============================================
echo View Live Test Logs
echo ============================================
echo.
echo This will show real-time logs from the tests.
echo Run this WHILE tests are executing in another window.
echo.
echo Press Ctrl+C to stop viewing logs.
echo.
pause

adb logcat -v time -s WirelessMapRepoTest:D WirelessMapRepository:D MapSyncWorker:D MapSyncWorkerTest:D

