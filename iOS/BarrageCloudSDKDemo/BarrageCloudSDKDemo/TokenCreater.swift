//
//  TokenBuilder.swift
//  BarrageCloudSDKDemo
//
//  Created by FanPengpeng on 2023/10/19.
//

import UIKit
import RTMTokenBuilder

class TokenCreater {
    
    static var rtmToken: String = {
        let userUuid = arc4random() % 100000
        return RTMTokenBuilder.TokenBuilder.buildToken2(KeyCenter.AppId, appCertificate: KeyCenter.Certificate, userUuid: "\(userUuid)")
    }()
    
    static func createRctToken(uid: Int32, channelName: String, role: Int32) -> String {
        return RTMTokenBuilder.TokenBuilder.rtcToken2(KeyCenter.AppId, appCertificate: KeyCenter.Certificate, uid: uid, channelName: channelName)
    }
}
