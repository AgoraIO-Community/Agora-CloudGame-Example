# 弹幕云游戏接入-Android接入

本文档主要介绍如何快速跑通弹幕云游戏Example示例工程及相关功能业务

# 0 总体介绍

        客户在接入弹幕云游戏的时候，推荐的系统接入框架为：[弹幕云游戏接入-总体方案](https://confluence.agoralab.co/pages/viewpage.action?pageId=1373341067)

        该框架和K歌房类似，遵循一个原则，业务相关的信息，尤其是会产生费用的交互（比如弹幕里面的送礼），都强烈建议通过客户的app Server来和我司的后台服务对接；客户的app 不直接和我司的后台服务对接。

       本Example是一个使用的sample code，不是正式的对外演示app，因此在demo的框架上，并没有遵循接入框架准则。而是采用简单的【无app server】的方式来演示如何弹幕云游戏的一些接入技术点。

本Example是期望体现如下信息：

- 1 弹幕云游戏玩法的时序关系
    
    包括游戏开启关闭、rtc的接入、游戏控制指令的操作、弹幕玩法的操作、游戏画面的接收显示（关注点！！这个和普通rtc有点不一样）
    
- 2 弹幕云游戏restful api的使用方法
- 3 弹幕云游戏app端如何对游戏做控制

Example框架图

![框架](https://github.com/AgoraIO-Community/Agora-CloudGame-Example/assets/3213611/70aa6b7c-267b-43d4-bce4-e553db7a33b1)


客户接入弹幕云游戏的注意事项：

如上所述，本Example没有遵循正式的云游戏接入框架，因此，客户如果要接入的时候，需要做如下：

- 2: 替换： 是客户的app通过客户自己定义的restful 访问客户的appserver，然后由客户的app server来实现#2
- 3: 代码复制级的参考：这个部分，客户的app是可以完全用source code的方式来参考
- 1: 流程参考：除了对#2的实现需要用客户自己定义的restful协议外，时序完全可以参考本demo

# 1. **前提条件**

- [Git](https://git-scm.com/downloads)
- [Java Development Kit](https://www.oracle.com/java/technologies/javase-downloads.html)
- [Android Studio](https://developer.android.com/studio/) 4.2 及以上。
- Android API 级别 21 及以上
- Android 设备，版本 Android 5.0 及以上
    
    声网推荐使用真机运行项目。部分模拟机可能存在功能缺失或者性能问题。
    
- 有效的[声网账号](https://docportal.shengwang.cn/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E5%88%9B%E5%BB%BA%E5%A3%B0%E7%BD%91%E8%B4%A6%E5%8F%B7)

# 2、**创建声网项目**

（1）进入声网控制台的[项目管理](https://console.agora.io/projects)页面。

（2）在项目管理页面，点击**创建**按钮。

（3）在弹出的对话框内输入项目名称、使用场景，然后选择**安全模式：** **APP ID + Token**。

（4）点击**提交**按钮。新建的项目会显示在项目管理页中。

# 3、**获取示例项目**

（1）运行以下命令克隆仓库到本地：

```bash
git clone git@github.com:AgoraIO-Community/Agora-CloudGame-Example.git
```

（2）运行以下命令切换到 `m`ain 分支：

```bash
git checkout main
```

# 4、**配置示例项目**

## 4.1 集成 SDK 及依赖库

参考项目中build.gradle集成rtc sdk

rtc sdk版本无特殊要求，可根据客户实际情况集成对应的版本

```java
implementation 'io.agora.rtc:full-sdk:4.2.2'

```

## 4.2 **设置 ID 和证书**

运行示例项目前，你需要在 `./Agora-CloudGame-Example/Android/Agora-CloudGame-Example/local.properties` 文件中添加并设置如下参数：

```java
APP_CERTIFICATE=<#Certificate#>
APP_ID=<#AppId#>
```

在创建声网项目后，从控制台获取这些参數的值，详情如下：

| 参数 | 描述 | 获取方式 |
| --- | --- | --- |
| Certificate | 声网项目的 App 证书 | https://docportal.shengwang.cn/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-%E8%AF%81%E4%B9%A6 |
| AppId | 声网项目的 App ID | https://docportal.shengwang.cn/cn/Agora%20Platform/get_appid_token?platform=All%20Platforms#%E8%8E%B7%E5%8F%96-app-id |

注：设置AppId和Certificate是为了端侧demo动态生成token，正式项目需要在服务器生成token，参考**[如何在 RESTful API 中进行 HTTP 基本认证和 Token 认证?](https://docportal.shengwang.cn/cn/video-legacy/faq/restful_authentication)**

# 5、**编译并运行示例项目**

1. 用 Android Studio 打开 `Agora-CloudGame-Example/Android/Agora-CloudGame-Example` 文件夹。
2. 开启 Android 设备的开发者选项，打开 USB 调试，通过 USB 连接线将 Android 设备接入电脑，并在 Android 设备选项中勾选你的 Android 设备。
3. 在 Android Studio 中，点击 **Sync Project with Gradle Files** 按钮，以让项目与 Gradle 文件同步。
4. 待同步成功后，点击  开始编译。
5. 编译成功后，你的 Android 设备上会出现 `Agora-CloudGame-Example` 应用。
6. 打开应用，体验 `Agora-CloudGame-Example` 场景。

# 6、主要业务流程

## 6.1 主播端

![主播端](https://github.com/AgoraIO-Community/Agora-CloudGame-Example/assets/3213611/caf0a92b-f950-4c05-a467-671eb74c0049)


## 6.2 观众端

![观众端](https://github.com/AgoraIO-Community/Agora-CloudGame-Example/assets/3213611/0e2571ad-6dd6-4dca-8d67-dde666a0a905)



# 7、主要功能介绍

## 7.1 发送控制指令

7.1.1 proto协议文件

```java
syntax = "proto3";

package agora.pb.rctrl;
option java_outer_classname = "RemoteCtrlMsg";

enum MsgType {
  UNKNOWN_TYPE = 0;
  MOUSE_EVENT_TYPE = 1001;
  KEYBOARD_EVENT_TYPE = 1002;
}

message RctrlMsg {
  MsgType type = 1;
  int32 version = 2;
  uint32 msgId = 3;
  uint64 timestamp = 4;
  bytes payload = 5;
}

message RctrlMsges {
  repeated RctrlMsg msges = 1;
}

enum MouseEventType {
  MOUSE_EVENT_UNKNOWN = 0;
  MOUSE_EVENT_MOVE = 1;
  MOUSE_EVENT_LBUTTON_DOWN = 2;
  MOUSE_EVENT_LBUTTON_UP = 3;
  MOUSE_EVENT_RBUTTON_DOWN = 4;
  MOUSE_EVENT_RBUTTON_UP = 5;
  MOUSE_EVENT_MBUTTON_DOWN = 6;
  MOUSE_EVENT_MBUTTON_UP = 7;
  MOUSE_EVENT_WHEEL = 8;
  MOUSE_EVENT_XBUTTON_DOWN = 9;
  MOUSE_EVENT_XBUTTON_UP = 10;
  MOUSE_EVENT_HWHEEL = 11;
}

enum MouseEventFlag {
  MOUSE_EVENT_FLAG_NONE = 0;
  MOUSE_EVENT_FLAG_LBUTTON = 0x0001;
  MOUSE_EVENT_FLAG_RBUTTON = 0x0002;
  MOUSE_EVENT_FLAG_SHIFT = 0x0004;
  MOUSE_EVENT_FLAG_CONTROL = 0x0008;
  MOUSE_EVENT_FLAG_MBUTTON = 0x0010;
  MOUSE_EVENT_FLAG_XBUTTON1 = 0x0020;
  MOUSE_EVENT_FLAG_XBUTTON2 = 0x0040;
}

enum KeyboardEventType {
  KEYBOARD_EVENT_UNKNOWN = 0;
  KEYBOARD_EVENT_KEY_DOWN = 1;
  KEYBOARD_EVENT_KEY_UP = 2;
  KEYBOARD_EVENT_KEY_CHAR = 3;
}

// extData for MOUSE_EVENT_WHEEL, high 16 bits is delta, low 16 bits is state
// extData low 16 bits is combined key state
message MouseEventMsg {
  uint32 mouseEvent = 1;
  int32 x = 2;
  int32 y = 3;
  int32 extData = 4;
}

// vkey is vitual key code, 
// see https://docs.microsoft.com/en-us/windows/win32/inputdev/virtual-key-codes
// state is key repeat count, scan code and flags, see https://learn.microsoft.com/en-us/windows/win32/inputdev/wm-keydown
// and see: https://learn.microsoft.com/en-us/windows/win32/inputdev/about-keyboard-input#keystroke-message-flags
// vkey and state may be generated by external tools, and mapped with mobile-phone app button, and configed in DB
message KeyboardEventMsg {
  uint32 keyboardEvent = 1;
  uint32 vkey = 2;
  uint32 state = 3;
}
```

7.1.2 添加protobuf协议

鼠标和键盘消息指令，通过protobuf协议，需要将Example中的RctrlMsg.proto文件导入到工程中，并在build.gradle中配置

```java
//指定 protobuf 文件所在路径
sourceSets {
        main {
            //实际测试指不指定无所谓，不影响 Java 文件生成
            proto {
                srcDir 'src/main'
            }
        }
    }

//配置 protobuf 编译任务
protobuf {
    //配置 protoc 编译器
    protoc {
        artifact = 'com.google.protobuf:protoc:3.19.2'
    }
    //配置生成目录，编译后会在 build 的目录下生成对应的java文件
    generateProtoTasks {
        all().each { task ->
            task.builtins {
                remove java
            }
            task.builtins {
                java {}
            }
        }
    }
}
```

7.1.3 构造`RemoteCtrlMsg`*对象*

```java
RemoteCtrlMsg.KeyboardEventMsg eventMsg = RemoteCtrlMsg.KeyboardEventMsg.newBuilder()
                .setVkey((int) key)
                .setState(eventType == RemoteCtrlMsg.KeyboardEventType.KEYBOARD_EVENT_KEY_DOWN ? 1 : 0xC0000001)
                .setKeyboardEvent(eventType.getNumber())
                .build();

        RemoteCtrlMsg.RctrlMsg rctrlMsg = RemoteCtrlMsg.RctrlMsg.newBuilder()
                .setType(RemoteCtrlMsg.MsgType.KEYBOARD_EVENT_TYPE)
                .setTimestamp(System.currentTimeMillis())
                .setPayload(eventMsg.toByteString())
                .build();

        RemoteCtrlMsg.RctrlMsges rctrlMsges = RemoteCtrlMsg.RctrlMsges.newBuilder()
                .addMsges(rctrlMsg)
                .build();
```

7.1.4 通过rtc的dataStream发送data

```java
mRtcEngine.sendStreamMessage(mStreamId, rctrlMsges.toByteArray());
```

## 7.2 鼠标位置转换

```java
int x = ((int) (event.getX()) << 16) / mBinding.frameLayout.getMeasuredWidth();
int y = ((int) (event.getY()) << 16) / mBinding.frameLayout.getMeasuredHeight();
```

其中event是手指触摸点在游戏视图上的相对坐标，将point的x和y坐标分别左移16位后，除以游戏视图的宽和高，就得到协议中需要的鼠标的坐标。

## 7.3 发送鼠标指令

参考以下代码：

```java
mBinding.frameLayout.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sendMouseMessage(event, RemoteCtrlMsg.MouseEventType.MOUSE_EVENT_LBUTTON_DOWN.getNumber());
                    break;
                case MotionEvent.ACTION_UP:
                    sendMouseMessage(event, RemoteCtrlMsg.MouseEventType.MOUSE_EVENT_LBUTTON_UP.getNumber());
                    break;
                default:
                    break;
            }
            return true;
        });

private synchronized void sendMouseMessage(MotionEvent event, int value) {
        if (!isLiveRole) {
            return;
        }
        Log.i(TAG, "sendMouseMessage:event x:" + event.getX() + ",event y:" + event.getY() + ",value:" + value);
        Log.i(TAG, "sendMouseMessage:event x/width:" + event.getX() / mBinding.frameLayout.getMeasuredWidth() + ",event y/height:" + event.getY() / mBinding.frameLayout.getMeasuredHeight());

        int x = ((int) (event.getX()) << 16) / mBinding.frameLayout.getMeasuredWidth();
        int y = ((int) (event.getY()) << 16) / mBinding.frameLayout.getMeasuredHeight();

        RemoteCtrlMsg.MouseEventMsg eventMsg = RemoteCtrlMsg.MouseEventMsg.newBuilder()
                .setMouseEvent(value)
                .setX(x)
                .setY(y)
                .build();

        RemoteCtrlMsg.RctrlMsg rctrlMsg = RemoteCtrlMsg.RctrlMsg.newBuilder()
                .setType(RemoteCtrlMsg.MsgType.MOUSE_EVENT_TYPE)
                .setTimestamp(System.currentTimeMillis())
                .setPayload(eventMsg.toByteString())
                .build();

        mEventMessagelist.add(rctrlMsg);
        if (System.currentTimeMillis() - mLastSendEventMessageTime > INTERVAL_SEND_EVENT_MESSAGE) {
            sendEventMessages();
        } else {
            mHandler.sendEmptyMessageDelayed(MESSAGE_SEND_EVENT_MESSAGE, INTERVAL_SEND_EVENT_MESSAGE - (System.currentTimeMillis() - mLastSendEventMessageTime));
        }
    }

private void sendEventMessages() {
        if (mEventMessagelist.size() == 0) {
            return;
        }
        mHandler.removeMessages(MESSAGE_SEND_EVENT_MESSAGE);
        Log.i(TAG, "sendEventMessages:" + mEventMessagelist.size());
        RemoteCtrlMsg.RctrlMsges rctrlMsges = RemoteCtrlMsg.RctrlMsges.newBuilder()
                .addAllMsges(mEventMessagelist)
                .build();

        mLastSendEventMessageTime = System.currentTimeMillis();

        mRtcEngine.sendStreamMessage(mStreamId, rctrlMsges.toByteArray());
        mEventMessagelist.clear();
    }
```

## 7.4 发送键盘指令

参考以下代码：

```java
mBinding.zView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setControllerView(mBinding.zView, false);
                        sendKeyboardMessage(RemoteCtrlMsg.KeyboardEventType.KEYBOARD_EVENT_KEY_DOWN, 'Z');
                        break;
                    case MotionEvent.ACTION_UP:
                        setControllerView(mBinding.zView, true);
                        sendKeyboardMessage(RemoteCtrlMsg.KeyboardEventType.KEYBOARD_EVENT_KEY_UP, 'Z');
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

private synchronized void sendKeyboardMessage(RemoteCtrlMsg.KeyboardEventType eventType, char key) {
        if (!isLiveRole) {
            return;
        }

        Log.i(TAG, "sendKeyboardMessage:" + eventType + ",key:" + key);

        RemoteCtrlMsg.KeyboardEventMsg eventMsg = RemoteCtrlMsg.KeyboardEventMsg.newBuilder()
                .setVkey((int) key)
                .setState(eventType == RemoteCtrlMsg.KeyboardEventType.KEYBOARD_EVENT_KEY_DOWN ? 1 : 0xC0000001)
                .setKeyboardEvent(eventType.getNumber())
                .build();

        RemoteCtrlMsg.RctrlMsg rctrlMsg = RemoteCtrlMsg.RctrlMsg.newBuilder()
                .setType(RemoteCtrlMsg.MsgType.KEYBOARD_EVENT_TYPE)
                .setTimestamp(System.currentTimeMillis())
                .setPayload(eventMsg.toByteString())
                .build();

        mEventMessagelist.add(rctrlMsg);
        
        if (System.currentTimeMillis() - mLastSendEventMessageTime > INTERVAL_SEND_EVENT_MESSAGE) {
            sendEventMessages();
        } else {
            mHandler.sendEmptyMessageDelayed(MESSAGE_SEND_EVENT_MESSAGE, INTERVAL_SEND_EVENT_MESSAGE - (System.currentTimeMillis() - mLastSendEventMessageTime));
        }
    }

private void sendEventMessages() {
        if (mEventMessagelist.size() == 0) {
            return;
        }
        mHandler.removeMessages(MESSAGE_SEND_EVENT_MESSAGE);
        Log.i(TAG, "sendEventMessages:" + mEventMessagelist.size());
        RemoteCtrlMsg.RctrlMsges rctrlMsges = RemoteCtrlMsg.RctrlMsges.newBuilder()
                .addAllMsges(mEventMessagelist)
                .build();

        mLastSendEventMessageTime = System.currentTimeMillis();

        mRtcEngine.sendStreamMessage(mStreamId, rctrlMsges.toByteArray());
        mEventMessagelist.clear();
    }
```

## 7.5 RestfulAPI使用

- 1.开始游戏
- 2.获取游戏状态
- 3.停止游戏
- 4.发送礼物
- 5.发送评论
- 6.点赞

以上都是通过RestfulAPI实现，详见RestfulAPI接口文档
