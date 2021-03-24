package com.example.myapplication

class XiabuBonusService :BonusService  {
    override fun useBonusPlan(bonus: Int): String {
        return "去呷哺呷哺吃$bonus 元的火锅"
    }
}