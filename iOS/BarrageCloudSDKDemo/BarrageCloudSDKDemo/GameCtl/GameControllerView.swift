//
//  GameControllerView.swift
//  BarrageCloudSDKDemo
//
//  Created by FanPengpeng on 2023/9/20.
//

import UIKit

protocol GameControllerViewDelegate: NSObjectProtocol {
    func onDirectionChanged(_ direction: Direction)
    func onJoystickMoved(distance: CGFloat, angle: CGFloat)
}

// 定义方向的枚举
enum Direction {
    case up, down, left, right, idle
}

class GameControllerView: UIView {

    let joystickRadius: CGFloat = 30

    var joystickCenter: CGPoint = .zero

    
    weak var delegate: GameControllerViewDelegate?

    override init(frame: CGRect) {
        super.init(frame: frame)
        
        // 设置背景颜色和边框
        backgroundColor = .darkGray
        layer.cornerRadius = frame.width * 0.5
        
        // 添加上下左右按钮
        let upButton = createButton(withTitle: "🔼")
        upButton.center = CGPoint(x: frame.width / 2, y: frame.height / 4)
        upButton.addTarget(self, action: #selector(upButtonPressed), for: .touchUpInside)
        addSubview(upButton)
        
        let downButton = createButton(withTitle: "🔽")
        downButton.center = CGPoint(x: frame.width / 2, y: frame.height / 4 * 3)
        downButton.addTarget(self, action: #selector(downButtonPressed), for: .touchUpInside)
        addSubview(downButton)
        
        let leftButton = createButton(withTitle: "◀️")
        leftButton.center = CGPoint(x: frame.width / 4, y: frame.height / 2)
        leftButton.addTarget(self, action: #selector(leftButtonPressed), for: .touchUpInside)
        addSubview(leftButton)
        
        let rightButton = createButton(withTitle: "▶️")
        rightButton.center = CGPoint(x: frame.width / 4 * 3, y: frame.height / 2)
        rightButton.addTarget(self, action: #selector(rightButtonPressed), for: .touchUpInside)
        addSubview(rightButton)
        
        // 添加摇杆视图
        let joystickView = UIView(frame: CGRect(origin: .zero, size: CGSize(width: joystickRadius * 2, height: joystickRadius * 2)))
        joystickView.backgroundColor = .red
        joystickView.layer.cornerRadius = joystickRadius
        joystickView.alpha = 0.5
        addSubview(joystickView)
        
        // 设置摇杆视图的初始位置并添加拖动手势
        joystickView.center = CGPoint(x: frame.width / 2, y: frame.height / 2)
        joystickView.addGestureRecognizer(UIPanGestureRecognizer(target: self, action: #selector(handlePan(_:))))
    }

    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // 创建一个带标题的按钮
    private func createButton(withTitle title: String) -> UIButton {
        let button = UIButton(type: .system)
        button.frame = CGRect(x: 0, y: 0, width: 60, height: 60)
        button.setTitle(title, for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 40)
        return button
    }
    
    @objc private func handlePan(_ gesture: UIPanGestureRecognizer) {
        switch gesture.state {
        case .began, .changed:
            // 更新摇杆视图的位置
            joystickCenter = gesture.view?.center ?? .zero
            
            let translation = gesture.translation(in: self)
            let newX = joystickCenter.x + translation.x
            let newY = joystickCenter.y + translation.y
            let newLocation = CGPoint(x: newX, y: newY)
            
            let centerX = bounds.width * 0.5
            let centerY = bounds.height * 0.5
            
            let distance = sqrt(pow(newX - centerX, 2) + pow( newY - centerY, 2))
            
            if isPointInsideCircle(centerX: bounds.width * 0.5, centerY:bounds.height * 0.5, radius: bounds.width * 0.5, x: newX, y: newY) {
                gesture.view?.center = newLocation
            }
            
            let offsetX = newX - centerX
            let offsetY = newY - centerY
            let angle = atan2(offsetY, offsetX)
            
            delegate?.onJoystickMoved(distance: distance, angle: angle)
            
            /*
            // 计算手指偏移向量和角度
            let offsetX = gesture.view?.center.x ?? 0 - frame.width / 2
            let offsetY = gesture.view?.center.y ?? 0 - frame.height / 2
            let angle = atan2(offsetY, offsetX)
            let distance = hypot(offsetX, offsetY)
            
             onJoystickMoved?(distance,angle)
            
            // 根据手指偏移角度和距离更新方向
            
            var direction: Direction = .idle
            if distance > joystickRadius / 2 {
                if angle > -CGFloat.pi/4 && angle < CGFloat.pi/4 {
                    direction = .right
                } else if angle > CGFloat.pi/4 && angle < CGFloat.pi*3/4 {
                    direction = .down
                } else if angle > CGFloat.pi*3/4 || angle < -CGFloat.pi*3/4 {
                    direction = .left
                } else if angle > -CGFloat.pi*3/4 && angle < -CGFloat.pi/4 {
                    direction = .up
                }
            }
             
            
            // 触发方向改变事件
            if direction != .idle {
                onDirectionChanged?(direction)
            }
             */
        case .ended, .cancelled:
            // 重置摇杆视图的位置并触发方向改变事件为idle
            joystickCenter = gesture.view?.center ?? .zero
            gesture.view?.center = CGPoint(x: frame.width / 2, y: frame.height / 2)
//            onDirectionChanged?(.idle)
            delegate?.onDirectionChanged(.idle)
            
        default:
            break
        }
        
        gesture.setTranslation(.zero, in: self)
    }
    
    @objc private func upButtonPressed() {
        delegate?.onDirectionChanged(.up)
    }
    
    @objc private func downButtonPressed() {
        delegate?.onDirectionChanged(.down)
    }
    
    @objc private func leftButtonPressed() {
        delegate?.onDirectionChanged(.left)
    }
    
    @objc private func rightButtonPressed() {
        delegate?.onDirectionChanged(.right)

    }
    
    func isPointInsideCircle(centerX: Double, centerY: Double, radius: Double, x: Double, y: Double) -> Bool {
        let distance = sqrt(pow(x - centerX, 2) + pow(y - centerY, 2))
        print("distance == \(distance), radius = \(radius)")
        return distance <= radius
    }
}
