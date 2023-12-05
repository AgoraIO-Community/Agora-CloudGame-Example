//
//  Game.swift
//  MyFramework
//
//  Created by FanPengpeng on 2023/9/19.
//

import UIKit

public class CloudGameBaseInfo: NSObject, Codable {
    public var gameId: String?
    public var name: String?
    public var vendor: String?
    public var thumbnail: String?
    public var introduce: String?
    public var vendorGameId: String?
}
