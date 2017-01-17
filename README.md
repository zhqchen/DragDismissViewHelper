# DragDismissViewHelper
QQ的未读红点拖拽消失，
Android DragDismissView like the dismiss of circle red point of QQ

### 可设置参数
```
    1. setPaintColor(int color) ：设置拖拽时，连接线的颜色，默认Color.RED.
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
