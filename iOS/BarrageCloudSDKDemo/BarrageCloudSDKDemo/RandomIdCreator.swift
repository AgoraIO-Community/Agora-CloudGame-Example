//
//  ChannelUtils.swift
//  CMCCDemo
//
//  Created by FanPengpeng on 2023/9/5.
//

import Foundation


class RandomIdCreator {
    
    @objc static var RTC_UID: UInt {
        let key = "RTC_UID"
        var uid = UserDefaults.standard.integer(forKey: key)
        if uid == 0 {
            uid = Int(arc4random()) % 100000
            UserDefaults.standard.set(uid, forKey: key)
        }
        return UInt(uid)
    }
    
    @objc static var OPEN_ID: String {
        let key = "OPEN_ID"
        var uid = UserDefaults.standard.integer(forKey: key)
        if uid == 0 {
            uid = Int(arc4random()) % 100000
            UserDefaults.standard.set(uid, forKey: key)
        }
        return "\(uid)"
    }
}

