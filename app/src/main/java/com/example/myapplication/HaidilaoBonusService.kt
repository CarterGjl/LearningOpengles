package com.example.myapplication

class HaidilaoBonusService:BonusService {
    override fun useBonusPlan(bonus: Int): String {
        return "去海底捞吃" + bonus + "元的火锅"
    }
}