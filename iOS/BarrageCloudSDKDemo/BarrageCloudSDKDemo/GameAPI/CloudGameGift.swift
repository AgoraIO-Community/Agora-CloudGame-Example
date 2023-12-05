//
//  Gift.swift
//  MyFramework
//
//  Created by FanPengpeng on 2023/9/19.
//

import UIKit

public class CloudGameGift: NSObject, Codable {
    public var giftId: String?
    public var name: String?
    public var price: Int = 0
    public var thumbnail: String?
    public var vendorGiftId: String?
    public var gameId: String?
}
