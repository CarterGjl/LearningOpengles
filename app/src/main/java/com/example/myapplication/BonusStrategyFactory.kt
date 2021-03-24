package com.example.myapplication

import java.util.HashMap

class BonusStrategyFactory {
    companion object{
        private val strategyMap = HashMap<Int,BonusService>()
        fun getByBonus(bonus: Int): BonusService? {
            return strategyMap[bonus]
        }
        fun register(bonus: Int,bonusService: BonusService) {
            strategyMap[bonus] = bonusService
        }
    }

}