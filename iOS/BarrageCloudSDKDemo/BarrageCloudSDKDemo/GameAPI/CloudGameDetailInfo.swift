//
//  GameInfomation.swift
//  MyFramework
//
//  Created by FanPengpeng on 2023/9/19.
//

import UIKit

public class CloudGameFeature: NSObject {
    public var comment: Int = 0
    public var like: Int = 0
}

public class CloudGameDetailInfo: NSObject {
    public var gameId: String?
    public var name: String?
    public var vendor: String?
    public var thumbnail: String?
    public var introduce: String?
    public var vendorGameId: String?
    public var gifts: [CloudGameGift]?
    public var instrunct: [Any]?
    public var feature: CloudGameFeature?
}
