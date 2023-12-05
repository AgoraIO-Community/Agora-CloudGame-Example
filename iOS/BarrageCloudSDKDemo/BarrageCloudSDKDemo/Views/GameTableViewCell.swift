//
//  GameTableViewCell.swift
//  BarrageCloudSDKDemo
//
//  Created by FanPengpeng on 2023/9/20.
//

import UIKit

class GameTableViewCell: UITableViewCell {
    
    @IBOutlet weak var iconImageView: UIImageView!
    
    @IBOutlet weak var titleLabel: UILabel!
    
    @IBOutlet weak var introduceLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        titleLabel.shadowOffset = CGSize(width: 0.3, height: 0.5)
        titleLabel.shadowColor = .white
        
        introduceLabel.shadowOffset = CGSize(width: 0.3, height: 0.5)
        introduceLabel.shadowColor = .white
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }

}
