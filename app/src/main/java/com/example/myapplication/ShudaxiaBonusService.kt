package com.example.myapplication

class ShudaxiaBonusService:BonusService {
    override fun useBonusPlan(bonus: Int): String {
        return "去蜀大侠吃" + bonus + "元的火锅"
    }
}