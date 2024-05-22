# adb-screenshot-api-demo

An example of testing whether the following functions work through ADB:

Test Device: Galaxy S24 Ultra, Android 14 (One UI 6.1)

| Feature           | URL                                          | Command                                                                                                                                    | 스크린샷                     |
|-------------------|----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|--------------------------|
| Screenshot        | screenshot?serial={serial}                   | adb -s {serial} exec-out screencap -p                                                                                                      | ![](docs/screenshot.gif) |
| Click x, y        | click?serial={serial}&x={x}&y={y}            | adb -s {serial} shell input tap 812 1617                                                                                                   | ![](docs/click.gif)      |
| Back to home      | home?serial={serial}                         | adb -s {serial} shell input keyevent 3                                                                                                     | ![](docs/home.gif)       |
| Push power button | power?serial={serial}                        | adb -s {serial} shell input keyevent 26                                                                                                    | ![](docs/power.gif)      |
| Change Brightness | brightness?serial={serial}&percent={percent} | *adb -s {serial} shell settings put system screen_brightness_mode 0 <br/>* adb -s {serial} shell settings put system screen_brightness 254 |                          |