//
//  CloudGameMsgHandler.swift
//  CloudGameFramework
//
//  Created by FanPengpeng on 2023/10/10.
//

import UIKit

class CloudGameMsgHandler: NSObject {
    
    @objc public static let shared = CloudGameMsgHandler()
    
    private var msgId = 0
    
    private var lastDate: Date?
    
    private var msgesArray = [Agora_Pb_Rctrl_RctrlMsg]()
    
    private override init() {}
    
    
    func createMouseEvent(type: Agora_Pb_Rctrl_MouseEventType, point: CGPoint, gameViewSize: CGSize) -> Data?{

        msgId += 1
        let currentDate = Date()
        
        print("=== 原始坐标： \(point)")
        let x = (Int(point.x) << 16) / Int(gameViewSize.width)
        let y = (Int(point.y) << 16) / Int(gameViewSize.height)
        print("=== 处理后: x = \(x), y = \(y)")
        var event = Agora_Pb_Rctrl_MouseEventMsg()
        event.mouseEvent = UInt32(type.rawValue)
        event.x = Int32(x)
        event.y = Int32(y)
        event.extData = 1
        
        var msg = Agora_Pb_Rctrl_RctrlMsg()
        msg.type = .mouseEventType
        msg.msgID = UInt32(msgId)
        msg.timestamp = UInt64(currentDate.timeIntervalSince1970)
        if let eventData = try? event.serializedData() {
            msg.payload = eventData
        }
        
        return createMsg(msg, currentDate: currentDate)
    }
    
    func createKeyboardEvent(type: Agora_Pb_Rctrl_KeyboardEventType, key:Character) -> Data?{
        msgId += 1
        let currentDate = Date()
        var event = Agora_Pb_Rctrl_KeyboardEventMsg()
        event.vkey = UInt32(key.asciiValue ?? 0)
        event.keyboardEvent = UInt32(type.rawValue)
        event.state = type == .keyboardEventKeyDown ? 1 : 0xC0000001
        
        var msg = Agora_Pb_Rctrl_RctrlMsg()
        msg.type = .keyboardEventType
        msg.msgID = UInt32(msgId)
        msg.timestamp = UInt64(currentDate.timeIntervalSince1970)
        if let eventData = try? event.serializedData() {
            msg.payload = eventData
        }
        
        return createMsg(msg, currentDate: currentDate)
    }
    
    private func createMsg(_ msg: Agora_Pb_Rctrl_RctrlMsg, currentDate: Date) -> Data?{
        if lastDate != nil && currentDate.timeIntervalSince(lastDate!) * 1000 > 30 {
            msgesArray.removeAll()
        }
        msgesArray.append(msg)
        
        var msgs = Agora_Pb_Rctrl_RctrlMsges()
        msgs.msges = msgesArray
        lastDate = currentDate
        let data = try? msgs.serializedData()
        return data
    }
}
