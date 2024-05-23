//
//  GameView.swift
//  BarrageCloudSDKDemo
//
//  Created by FanPengpeng on 2023/9/26.
//

import UIKit

protocol GameViewTouchDelegate: NSObjectProtocol {
    func touchDown(_ view: GameView, point: CGPoint)
    func touchMove(_ view: GameView, point: CGPoint)
    func touchUp(_ view: GameView, point: CGPoint)
}

class GameView: UIView {
    
    weak var touchDelegate: GameViewTouchDelegate?

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        if let touch = touches.first {
            let point = touch.location(in: self)
            touchDelegate?.touchDown(self, point: point)
        }
    }
    
    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        if let touch = touches.first {
            let point = touch.location(in: self)
            touchDelegate?.touchMove(self, point: point)
        }
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        if let touch = touches.first{
            let point = touch.location(in: self)
            touchDelegate?.touchUp(self, point: point)
        }
    }
    
}
