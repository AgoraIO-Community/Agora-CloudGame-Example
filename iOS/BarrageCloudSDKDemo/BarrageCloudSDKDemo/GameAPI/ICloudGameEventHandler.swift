//
//  IAgoraEventHandler.swift
//  MyFramework
//
//  Created by FanPengpeng on 2023/9/19.
//

import Foundation

public enum CloudGameState: Int {
    case start, end
}

public protocol ICloudGameEventHandler: NSObjectProtocol {
    func onServiceInitEvent()
    func onGamesResults(_ games: [CloudGameBaseInfo])
    func onGameInformationResult(_ game: CloudGameDetailInfo)
    func onGameState(_ state: CloudGameState, message: String)
}
