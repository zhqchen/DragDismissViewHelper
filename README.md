# DragDismissViewHelper
仿QQ的未读红点拖拽消失的动画, 拖拽对象可为单个view，也可为ListView的Item中的任意view
Android DragDismissView like the dismiss of circle red point of QQ

![效果](./screenshots/drag.gif)

### 可设置参数
```
    1. setPaintColor(int color) ：设置拖拽时拉伸线的颜色，默认Color.RED.
    2. setFarthestDistance(float distance) ：设置拖拽的最远距离，默认180.
```
### 使用方式
```
    DragDismissViewHelper helper = new DragDismissViewHelper(context, tvUnread);
    helper.setPaintColor(Color.RED);
    helper.setFarthestDistance(180);
    helper.setDragStateListener(new DragDismissViewHelper.DragStateListener() {

        @Override
        public void onOutFingerUp(View view) {
            // do something when finger up in range
        }

        @Override
        public void onInnerFingerUp(View view) {
            // do something when finger up out of range
        }
    });
```
### 局限
```
    库中使用了android.permission.SYSTEM_ALERT_WINDOW的权限，sample中targetSdkVersion=22，绕过了权限的问题。
    实际项目中，若targetSdkVersion >= 23时，需要自己提前进行权限的动态申请并确保用户允许。

    @TargetApi(Build.VERSION_CODES.M)
        private void requestPermission() {
            if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_ALERT_SETTING);
            }
        }

    @TargetApi(Build.VERSION_CODES.M)
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE_ALERT_SETTING) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(MainActivity.this, "SYSTEM_ALERT_WINDOW permission denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
```
### License

	Copyright 2017 Zhq Chen

	Licensed under the Apache License, Version 2.0 (the "License");	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
