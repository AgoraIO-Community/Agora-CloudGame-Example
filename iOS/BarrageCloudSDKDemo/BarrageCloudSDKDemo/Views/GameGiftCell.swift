//
//  GameGiftCell.swift
//  BarrageCloudSDKDemo
//
//  Created by FanPengpeng on 2023/9/21.
//

import UIKit
import CloudGameFramework
import Kingfisher

class GameGiftCell: UICollectionViewCell {
    
    @IBOutlet weak var iconImageView: UIImageView!
    
    @IBOutlet weak var titleLabel: UILabel!
    
    var gift: CloudGameGift? {
        didSet{
            guard let gift = gift else {
                return
            }
            let icon = "https://img1.baidu.com/it/u=3065967935,3641062035&fm=253&app=138&size=w931&n=0&f=PNG&fmt=auto?sec=1695402000&t=fbe7c496dbc7a82efa4c7d45c6394b36"
            iconImageView.kf.setImage(with: URL(string: icon))
//            iconImageView.kf.setImage(with: URL(string:gift.thumbnail ?? ""))
            titleLabel.text = "\(gift.name ?? "") \(gift.price)"
        }
    }
    
    override var isSelected: Bool {
        didSet{
            contentView.backgroundColor = isSelected ? .gray : .white
        }
    }
    
}
